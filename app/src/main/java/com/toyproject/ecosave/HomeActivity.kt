package com.toyproject.ecosave

import android.Manifest
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
import com.google.android.gms.tasks.CancellationTokenSource
import com.toyproject.ecosave.apis.naverapi.ReverseGeocodingAPI
import com.toyproject.ecosave.databinding.ActivityHomeBinding
import com.toyproject.ecosave.models.RelativeElectricPowerConsumeGradeData
import com.toyproject.ecosave.models.ReverseGeocodingResponse

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

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 1001
    }

    private fun prepareListData() {
        var data = RelativeElectricPowerConsumeGradeData(0, 1, 10, 35.9F, 0)
        list.add(data)

        data = RelativeElectricPowerConsumeGradeData(1, 2, 15, 131.3F, 0)
        list.add(data)

        data = RelativeElectricPowerConsumeGradeData(5, 3, 28, 83.0F, 1)
        list.add(data)
    }

    private fun showWarningDialogForChangeMyResidence() {
        val alertDialogBuilderBtn = AlertDialog.Builder(this)
        alertDialogBuilderBtn.setTitle("내 거주지 변경")
        alertDialogBuilderBtn.setMessage("현재 위치를 기준으로 거주지를 변경합니다.\n주의: 이전에 저장된 거주지 정보는 사라집니다.")
        alertDialogBuilderBtn.setPositiveButton("확인") { _, _ ->
            getMyLocation()
        }
        alertDialogBuilderBtn.setNegativeButton("취소") { _, _ -> }

        val alertDialogBox = alertDialogBuilderBtn.create()
        alertDialogBox.show()
    }

    private fun setLocationRequest() {
        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            // 위치 서비스가 켜져있는지 확인
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                turnOnGPS() // 위치 서비스 켜기
                return
            }

            val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            val locationRequest: LocationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000 * 100)
                    .setMinUpdateDistanceMeters(0.0F)
                    .build()

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun getMyLocation() {
        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            // 위치 서비스가 켜져있는지 확인
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                turnOnGPS() // 위치 서비스 켜기
                return
            }

            // 10초 마다 현재 위치 수신
            val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            val locationRequest: LocationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000 * 100)
                    .setMinUpdateDistanceMeters(0.0F)
                    .build()

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

            val alertDialogBuilderBtn = AlertDialog.Builder(this)
            alertDialogBuilderBtn.setTitle("내 거주지 변경")
            alertDialogBuilderBtn.setMessage("위치 수신이 완료 되었습니다.")
            alertDialogBuilderBtn.setPositiveButton("확인") { _, _ ->
                searchAddress(currentLatitude, currentLongitude)
            }

            val alertDialogBox = alertDialogBuilderBtn.create()
            alertDialogBox.show()
        } else {
            requestPermissions(permissions, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun turnOnGPS() {
        // 위치 설정이 켜져 있지 않으면 위치 설정창으로 이동
        val alertDialogBuilderBtn = AlertDialog.Builder(this)
        alertDialogBuilderBtn.setTitle("위치 서비스 권한 필요")
        alertDialogBuilderBtn.setMessage("내 거주지 설정을 하기 위해서는 위치 서비스 권한이 필요합니다.")
        alertDialogBuilderBtn.setPositiveButton("확인") { _, _ ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivity(intent)
        }
        alertDialogBuilderBtn.setNegativeButton("취소") { _, _ -> }

        val alertDialogBox = alertDialogBuilderBtn.create()
        alertDialogBox.show()
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
                        val result = response.body()
                        Log.d("위치", result.toString())
                        if (result != null) {
                            val addrArea = result.results[0]
                            val addrRegion = addrArea.region
                            val addrLand = addrArea.land
                            val addr = if (addrLand.number2 == "") {
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

                            val roadAddrArea = result.results[1]
                            val roadAddrRegion = roadAddrArea.region
                            val roadAddrLand = roadAddrArea.land
                            val roadAddr = if (roadAddrLand.number2 == "") {
                                "${roadAddrRegion.area1.name} " +
                                        "${roadAddrRegion.area2.name} " +
                                        "${roadAddrLand.name} " +
                                        roadAddrLand.number1
                            } else {
                                "${roadAddrRegion.area1.name} " +
                                        "${roadAddrRegion.area2.name} " +
                                        "${roadAddrLand.name} " +
                                        "${roadAddrLand.number1}-${roadAddrLand.number2}"
                            }

                            chooseAddress(addr, roadAddr)
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

    // Dialog를 통해 지번 주소와 도로명 주소중 하나를 거주지로 설정
    private fun chooseAddress(addr: String, roadAddr: String) {
        val options = arrayOf(
            addr,
            roadAddr
        )

        var selected = 1

        val alertDialogBuilderBtn = AlertDialog.Builder(this)
        alertDialogBuilderBtn.setTitle("거주지로 설정할 주소를 선택해 주세요.")
        alertDialogBuilderBtn.setSingleChoiceItems(options, 1) { _, which ->
            when (which) {
                which -> selected = which
            }
        }
        alertDialogBuilderBtn.setPositiveButton("확인") { _, _ ->
            binding.textMyResident.text = options[selected]
            when (selected) {
                0 -> {
                    Toast.makeText(this, "지번 주소로 설정", Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    Toast.makeText(this, "도로명 주소로 설정", Toast.LENGTH_SHORT).show()
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