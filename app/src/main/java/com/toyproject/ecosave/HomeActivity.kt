package com.toyproject.ecosave

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import com.toyproject.ecosave.api.APIClientForNaverMap
import com.toyproject.ecosave.api.APIInterface
import com.toyproject.ecosave.databinding.ActivityHomeBinding
import com.toyproject.ecosave.models.DeviceTypeList
import com.toyproject.ecosave.models.RelativeGradeData
import com.toyproject.ecosave.api.responsemodels.ReverseGeocodingResponse
import com.toyproject.ecosave.utilities.fromDpToPx
import com.toyproject.ecosave.widget.createDialog
import com.toyproject.ecosave.widget.defaultNegativeDialogInterfaceOnClickListener
import com.toyproject.ecosave.widget.simpleDialog

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var drawerLayout: DrawerLayout

    private var recyclerView: RecyclerView? = null
    private var recyclerViewRegisteredDeviceListAdapter: RecyclerViewRegisteredDeviceListAdapter? = null
    private var list = mutableListOf<RelativeGradeData>()

    private var currentLatitude = 0.0 // 위도
    private var currentLongitude = 0.0 // 경도

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val optionsForChangeMyResidence = arrayOf(
        "현재 위치를 거주지로 설정",
        "지도에서 검색"
    )

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
        private const val LOCATION_REQUEST_INTERVAL_MILLIS = (1000 * 100).toLong()
        private const val MARGIN_SIDE = 20.0F
        private const val MARGIN_BETWEEN_PYRAMIDS = 10
        private val MARGIN_TEXT = arrayOf(
            arrayOf(92.0F, 5.0F),
            arrayOf(100.0F, 31.0F),
            arrayOf(100.0F, 31.0F),
            arrayOf(108.0F, 54.0F),
            arrayOf(108.0F, 54.0F),
            arrayOf(118.0F, 76.0F),
            arrayOf(118.0F, 76.0F),
            arrayOf(128.0F, 100.0F),
            arrayOf(128.0F, 100.0F)
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            locationResult.let {
                val lastLocation = it.lastLocation
                lastLocation?.let { it2 ->
                    currentLatitude = it2.latitude
                    currentLongitude = it2.longitude
                    Log.d("위치", "$currentLatitude, $currentLongitude")
                }
            }
        }
    }

    private fun prepareListData() {
        var data = RelativeGradeData(
            DeviceTypeList.REFRIGERATOR,
            1, 3, 35.9F,
            1, 4, 21.0F
        )
        list.add(data)

        data = RelativeGradeData(
            DeviceTypeList.AIR_CONDITIONER,
            2, 8, 131.3F,
            3, 14, 52.3F
        )
        list.add(data)

        data = RelativeGradeData(
            DeviceTypeList.BOILER,
            3, 21, 83.0F,
            null, null, null
        )
        list.add(data)

        data = RelativeGradeData(
            DeviceTypeList.WASHING_MACHINE,
            3, 21, 62.8F,
            3, 17, 219.0F
        )
        list.add(data)

        data = RelativeGradeData(
            DeviceTypeList.MICROWAVE_OVEN,
            4, 28, 1200.0F,
            null, null, null
        )
        list.add(data)

        data = RelativeGradeData(
            DeviceTypeList.TV,
            6, 64, 153.0F,
            7, 81, 65.0F
        )
        list.add(data)

        data = RelativeGradeData(
            DeviceTypeList.AIR_CONDITIONER,
            8, 90, 195.2F,
            9, 97, 77.4F
        )
        list.add(data)

        // 피라미드에 종합 에너지 소비 효율 등급을 나타냄
        setPyramid()
    }

    // 피라미드에 종합 상대적 에너지 소비 효율 등급을 나타냄
    @SuppressLint("SetTextI18n")
    private fun setPyramid() {
        // 피라미드 크기 조정
        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val width_margin_px = fromDpToPx(resources, MARGIN_SIDE)
        val pyramidWidth = (screenWidth - 2 * width_margin_px - MARGIN_BETWEEN_PYRAMIDS) / 2
        val pyramidHeight = pyramidWidth * 1.055

        val paramsForCO2Pyramid = ConstraintLayout.LayoutParams(pyramidWidth, pyramidHeight.toInt())
        paramsForCO2Pyramid.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        paramsForCO2Pyramid.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        paramsForCO2Pyramid.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        binding.CO2Pyramid.layoutParams = paramsForCO2Pyramid

        // CO2 배출량이 표기되어 있는 기기의 개수
        var numOfCO2Devices = 0
        
        // 소비전력이 표기되어 있는 기기의 개수
        var numOfEnergyConsumeDevices = 0

        // CO2 배출량에 대한 상대적 에너지 소비 효율 백분위(%)의 합
        var sumOfRelativeCO2EmissionPercentage = 0.0F

        // CO2 배출량에 대한 종합 상대적 에너지 소비 효율 등급
        val totalRelativeCO2EmissionGrade: Int

        // 소비전력에 대한 상대적 에너지 소비 효율 백분위(%)의 합
        var sumOfRelativeEnergyConsumePercentage = 0.0F

        // 소비전력에 대한 상대적 에너지 소비 효율 등급
        val totalRelativeEnergyConsumeGrade: Int

        // list에 있는 각각의 상대적 에너지 소비 효율 데이터를 통해 평균치를 구함
        for (relativeGradeData in list) {
            if ((relativeGradeData.relativeCO2EmissionGrade != null)
                && (relativeGradeData.relativeCO2EmissionPercentage != null)
                && (relativeGradeData.amountOfCO2Emission != null)) {
                numOfCO2Devices += 1
                sumOfRelativeCO2EmissionPercentage += relativeGradeData.relativeCO2EmissionPercentage
            }

            numOfEnergyConsumeDevices += 1
            sumOfRelativeEnergyConsumePercentage += relativeGradeData.relativeElectricPowerConsumePercentage
        }

        if (numOfCO2Devices >= 1) {
            val averageRelativeCO2EmissionPercentage = sumOfRelativeCO2EmissionPercentage / numOfCO2Devices

            if (averageRelativeCO2EmissionPercentage <= 4.0F) { // 1등급 (CO2)
                binding.CO2Pyramid.setImageResource(R.drawable.pyramid1_co2)
                binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_1))
                totalRelativeCO2EmissionGrade = 1
            } else if (averageRelativeCO2EmissionPercentage <= 11.0F) { // 2등급 (CO2)
                binding.CO2Pyramid.setImageResource(R.drawable.pyramid2_co2)
                binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_2_and_3))
                totalRelativeCO2EmissionGrade = 2
            } else if (averageRelativeCO2EmissionPercentage <= 23.0F) { // 3등급 (CO2)
                binding.CO2Pyramid.setImageResource(R.drawable.pyramid2_co2)
                binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_2_and_3))
                totalRelativeCO2EmissionGrade = 3
            } else if (averageRelativeCO2EmissionPercentage <= 40.0F) { // 4등급 (CO2)
                binding.CO2Pyramid.setImageResource(R.drawable.pyramid3_co2)
                binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_4_and_5))
                totalRelativeCO2EmissionGrade = 4
            } else if (averageRelativeCO2EmissionPercentage <= 60.0F) { // 5등급 (CO2)
                binding.CO2Pyramid.setImageResource(R.drawable.pyramid3_co2)
                binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_4_and_5))
                totalRelativeCO2EmissionGrade = 5
            } else if (averageRelativeCO2EmissionPercentage <= 77.0F) { // 6등급 (CO2)
                binding.CO2Pyramid.setImageResource(R.drawable.pyramid4_co2)
                binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_6_and_7))
                totalRelativeCO2EmissionGrade = 6
            } else if (averageRelativeCO2EmissionPercentage <= 89.0F) { // 7등급 (CO2)
                binding.CO2Pyramid.setImageResource(R.drawable.pyramid4_co2)
                binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_6_and_7))
                totalRelativeCO2EmissionGrade = 7
            } else if (averageRelativeCO2EmissionPercentage <= 96.0F) { // 8등급 (CO2)
                binding.CO2Pyramid.setImageResource(R.drawable.pyramid5_co2)
                binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_8_and_9))
                totalRelativeCO2EmissionGrade = 8
            } else { // 9등급 (CO2)
                binding.CO2Pyramid.setImageResource(R.drawable.pyramid5_co2)
                binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_8_and_9))
                totalRelativeCO2EmissionGrade = 9
            }

            val paramsForCO2Grade = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
            )

            binding.textCO2EmissionGrade.text = "상위 ${averageRelativeCO2EmissionPercentage.toInt()}%"

            paramsForCO2Grade.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            paramsForCO2Grade.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            paramsForCO2Grade.marginStart = fromDpToPx(
                resources, MARGIN_TEXT[totalRelativeCO2EmissionGrade - 1][0]
            )
            paramsForCO2Grade.topMargin = fromDpToPx(
                resources, MARGIN_TEXT[totalRelativeCO2EmissionGrade - 1][1])

            binding.textCO2EmissionGrade.layoutParams = paramsForCO2Grade
        }

        if (numOfEnergyConsumeDevices >= 1) {
            val averageRelativeEnergyConsumePercentage = sumOfRelativeEnergyConsumePercentage / numOfEnergyConsumeDevices

            if (averageRelativeEnergyConsumePercentage <= 4.0F) { // 1등급 (소비전력)
                binding.energyConsumePyramid.setImageResource(R.drawable.pyramid1_energy)
                binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_1))
                totalRelativeEnergyConsumeGrade = 1
            } else if (averageRelativeEnergyConsumePercentage <= 11.0F) { // 2등급 (소비전력)
                binding.energyConsumePyramid.setImageResource(R.drawable.pyramid2_energy)
                binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_2_and_3))
                totalRelativeEnergyConsumeGrade = 2
            } else if (averageRelativeEnergyConsumePercentage <= 23.0F) { // 3등급 (소비전력)
                binding.energyConsumePyramid.setImageResource(R.drawable.pyramid2_energy)
                binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_2_and_3))
                totalRelativeEnergyConsumeGrade = 3
            } else if (averageRelativeEnergyConsumePercentage <= 40.0F) { // 4등급 (소비전력)
                binding.energyConsumePyramid.setImageResource(R.drawable.pyramid3_energy)
                binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_4_and_5))
                totalRelativeEnergyConsumeGrade = 4
            } else if (averageRelativeEnergyConsumePercentage <= 60.0F) { // 5등급 (소비전력)
                binding.energyConsumePyramid.setImageResource(R.drawable.pyramid3_energy)
                binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_4_and_5))
                totalRelativeEnergyConsumeGrade = 5
            } else if (averageRelativeEnergyConsumePercentage <= 77.0F) { // 6등급 (소비전력)
                binding.energyConsumePyramid.setImageResource(R.drawable.pyramid4_energy)
                binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_6_and_7))
                totalRelativeEnergyConsumeGrade = 6
            } else if (averageRelativeEnergyConsumePercentage <= 89.0F) { // 7등급 (소비전력)
                binding.energyConsumePyramid.setImageResource(R.drawable.pyramid4_energy)
                binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_6_and_7))
                totalRelativeEnergyConsumeGrade = 7
            } else if (averageRelativeEnergyConsumePercentage <= 96.0F) { // 8등급 (소비전력)
                binding.energyConsumePyramid.setImageResource(R.drawable.pyramid5_energy)
                binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_8_and_9))
                totalRelativeEnergyConsumeGrade = 8
            } else { // 9등급 (소비전력)
                binding.energyConsumePyramid.setImageResource(R.drawable.pyramid5_energy)
                binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_8_and_9))
                totalRelativeEnergyConsumeGrade = 9
            }

            val paramsForEnergyConsumeGrade = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
            )

            binding.textEnergyConsumeGrade.text = "상위 ${averageRelativeEnergyConsumePercentage.toInt()}%"

            paramsForEnergyConsumeGrade.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            paramsForEnergyConsumeGrade.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            paramsForEnergyConsumeGrade.marginStart =
                fromDpToPx(resources, MARGIN_TEXT[totalRelativeEnergyConsumeGrade - 1][0])
            paramsForEnergyConsumeGrade.topMargin =
                fromDpToPx(resources, MARGIN_TEXT[totalRelativeEnergyConsumeGrade - 1][1])
            binding.textEnergyConsumeGrade.layoutParams = paramsForEnergyConsumeGrade
        }
    }

    private fun showDialogForError() {
        simpleDialog(
            this,
            "내 거주지 변경",
            "현재 위치를 수신하지 못했습니다. 잠시 후 다시 시도해 주세요.",
        )
    }

    private fun setLocationRequest() {
        // 필요 권한이 허용되어 있는지 확인
        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            val locationRequest: LocationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_REQUEST_INTERVAL_MILLIS)
                    .setMinUpdateDistanceMeters(0.0F)
                    .build()

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            requestPermissions(permissions, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun getMyLocation() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // 위치 서비스가 켜져있는지 확인
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            turnOnGPS() // 위치 서비스 켜기
        } else {
            // 10초 마다 현재 위치 수신
            setLocationRequest()

            val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                if (currentLatitude == 0.0 || currentLongitude == 0.0) {
                    showDialogForError()
                } else {
                    searchAddress(currentLatitude, currentLongitude)
                }
            }

            createDialog(
                this,
                "내 거주지 변경",
                "현재 위치를 기준으로 거주지를 변경합니다.\n\n"
                        + "주의: 이전에 저장된 거주지 정보는 사라집니다.",
                positiveButtonOnClickListener,
                defaultNegativeDialogInterfaceOnClickListener
            )
        }
    }

    private fun turnOnGPS() {
        // 위치 설정이 켜져 있지 않으면 위치 설정창으로 이동
        val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivity(intent)
        }

        createDialog(
            this,
            "위치 서비스 권한 필요",
            "내 거주지 설정을 하기 위해서는 위치 서비스 권한이 필요합니다.",
            positiveButtonOnClickListener,
            defaultNegativeDialogInterfaceOnClickListener
        )
    }

    // 현재 위치를 기반으로 지번 주소와 도로명 주소를 찾음
    private fun searchAddress(latitude: Double, longitude: Double) {
        val coords = "$longitude,$latitude" // 경도, 위도
        Log.d("위치", coords)

        val apiInterface = APIClientForNaverMap
            .getClient()
            .create(APIInterface::class.java)
        val call = apiInterface.searchAddressByPoint(coords, "json", "addr,roadaddr")

        call.enqueue(
            object : Callback<ReverseGeocodingResponse> {
                override fun onResponse(
                    call: Call<ReverseGeocodingResponse>,
                    response: Response<ReverseGeocodingResponse>
                ) {
                    if (response.isSuccessful) {
                        try {
                            val result = response.body()
                            Log.d("위치", result.toString())

                            if (result != null) {
                                val addressList = mutableListOf<String>()

                                if (result.results.isEmpty()) {
                                    simpleDialog(
                                        this@HomeActivity,
                                        "내 거주지 변경",
                                        "해당 위치에 대한 데이터가 없습니다. 다른 지역에서 다시 시도해 주세요."
                                    )
                                    return
                                }

                                for (area in result.results) {
                                    val addrRegion = area.region
                                    val addrLand = area.land

                                    val address = if (addrLand.number2 == "") {
                                        "${addrRegion.area1.name} " +
                                                "${addrRegion.area2.name} " +
                                                "${addrRegion.area3.name} " +
                                                addrLand.number1
                                    } else {
                                        "${addrRegion.area1.name} " +
                                                "${addrRegion.area2.name} " +
                                                "${addrRegion.area3.name} " +
                                                "${addrLand.number1}-${addrLand.number2}"
                                    }

                                    addressList.add(address)
                                }

                                val finalAddressList = addressList.toTypedArray()
                                chooseAddress(finalAddressList)
                            }
                        } catch (e: Exception) {
                            simpleDialog(
                                this@HomeActivity,
                                "내 거주지 변경",
                                "현재 위치를 가져오지 못했습니다. 잠시 후 다시 시도해 주세요."
                            )
                            Log.d("위치", e.toString())
                            e.printStackTrace()
                        }
                    } else {
                        Log.d("위치", response.errorBody()?.string()!!)
                    }
                }

                override fun onFailure(call: Call<ReverseGeocodingResponse>, t: Throwable) {
                    Toast.makeText(this@HomeActivity, "주소 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    Log.d("위치", t.message.toString())
                }
            }
        )
    }

    // Dialog를 통해 거주지 설정
    private fun chooseAddress(addressList: Array<String>) {
        var selected = 0

        val alertDialogBuilderBtn = AlertDialog.Builder(this)
        alertDialogBuilderBtn.setTitle("거주지로 설정할 주소를 선택해 주세요.")
        alertDialogBuilderBtn.setSingleChoiceItems(addressList, selected) { _, which ->
            when (which) {
                which -> selected = which
            }
        }
        alertDialogBuilderBtn.setPositiveButton("확인") { _, _ ->
            binding.textMyResident.text = addressList[selected]
            when (selected) {
                0 -> {
                    Toast.makeText(this, addressList[selected], Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    Toast.makeText(this, addressList[selected], Toast.LENGTH_SHORT).show()
                }
            }
        }
        alertDialogBuilderBtn.setNegativeButton("취소") { _, _ -> }

        val alertDialogBox = alertDialogBuilderBtn.create()
        alertDialogBox.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 앱바에 back 버튼 활성화

        list = ArrayList()
        recyclerView = binding.recyclerView

        recyclerViewRegisteredDeviceListAdapter = RecyclerViewRegisteredDeviceListAdapter(this, list)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = recyclerViewRegisteredDeviceListAdapter

        // 서버로 부터 상대적 에너지 소비 효율 등급 관련 데이터를 받아옴
        prepareListData()

        drawerLayout = binding.drawerLayout
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.textMyResident.text = "등록된 정보가 없습니다."

        binding.btnChangeMyResident.setOnClickListener {
            val alertDialogBuilderBtn = AlertDialog.Builder(this)
            alertDialogBuilderBtn.setTitle("내 거주지 변경")
            alertDialogBuilderBtn.setItems(optionsForChangeMyResidence) { _, which ->
                when (which) {
                    0 -> getMyLocation()
                }
            }
            alertDialogBuilderBtn.setNegativeButton("취소") { _, _ -> }

            val alertDialogBox = alertDialogBuilderBtn.create()
            alertDialogBox.show()
        }

        binding.btnAddDevice.setOnClickListener {
            val intent = Intent(this, AddDeviceActivity::class.java)
            startActivity(intent)
        }
    }
}