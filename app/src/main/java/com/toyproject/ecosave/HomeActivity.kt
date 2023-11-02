package com.toyproject.ecosave

import android.Manifest
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

import com.toyproject.ecosave.apis.naverapi.ReverseGeocodingAPI
import com.toyproject.ecosave.databinding.ActivityHomeBinding
import com.toyproject.ecosave.models.DeviceTypeList
import com.toyproject.ecosave.models.RelativeElectricPowerConsumeGradeData
import com.toyproject.ecosave.models.ReverseGeocodingResponse
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
    private var list = mutableListOf<RelativeElectricPowerConsumeGradeData>()

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
        const val REQUEST_CODE_PERMISSIONS = 1001
        const val LOCATION_REQUEST_INTERVAL_MILLIS = (1000 * 100).toLong()
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
        var data = RelativeElectricPowerConsumeGradeData(DeviceTypeList.REFRIGERATOR, 1, 10, 35.9F, 0)
        list.add(data)

        data = RelativeElectricPowerConsumeGradeData(DeviceTypeList.AIR_CONDITIONER, 2, 15, 131.3F, 0)
        list.add(data)

        data = RelativeElectricPowerConsumeGradeData(DeviceTypeList.BOILER, 3, 28, 83.0F, 1)
        list.add(data)
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
        val api = ReverseGeocodingAPI.create()

        val coords = "$longitude,$latitude" // 경도, 위도
        Log.d("위치", coords)
        api.searchAddressByPoint(coords, "json", "addr,roadaddr").enqueue(
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

        recyclerViewRegisteredDeviceListAdapter = RecyclerViewRegisteredDeviceListAdapter(list)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = recyclerViewRegisteredDeviceListAdapter
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