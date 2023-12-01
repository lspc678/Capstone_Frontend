package com.toyproject.ecosave.utilities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import com.toyproject.ecosave.HomeActivity
import com.toyproject.ecosave.SignUpActivity
import com.toyproject.ecosave.widget.createDialog
import com.toyproject.ecosave.widget.defaultNegativeDialogInterfaceOnClickListener

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GPSLocation(private val activity: Activity, val context: Context) {
    companion object {
        var currentLatitude = 0.0 // 현재 위치 위도
        var currentLongitude = 0.0 // 현재 위치 경도
        const val REQUEST_CODE_PERMISSIONS = 1001
        const val LOCATION_REQUEST_INTERVAL_MILLIS = (1000 * 10).toLong()
    }

    // 현재 위치를 파악하기 위해 필요한 권한 목록
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        LOCATION_REQUEST_INTERVAL_MILLIS)
        .build()

    fun getLocation() : Boolean {
        // SignUpActivity 또는 HomeActivity에서 실행했을 경우에만 GPSLocation 사용 가능
        if (!isValidActivity()) {
            return false
        }

        // 위치 서비스가 켜져 있는지 확인
        if (!turnOnLocationSystem()) {
            turnOnGPS()
            return false
        }

        setLocationRequest()

        CoroutineScope(Dispatchers.Default).launch {
            while ((currentLatitude == 0.0)
                || (currentLongitude == 0.0)) {
                delay(10L)
            }

            // 현재 위치 정보 업데이트 종료
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }

        return true
    }

    private fun isValidActivity() : Boolean {
        return ((activity is SignUpActivity) || (activity is HomeActivity))
    }

    private fun turnOnLocationSystem() : Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // 위치 서비스가 켜져있는지 확인
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun setLocationRequest() {
        // 필요 권한이 허용되어 있는지 확인
        if (permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    currentLatitude = it.latitude
                    currentLongitude = it.longitude
                } else {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    )
                }
            }
        } else {
            requestPermissions(activity, permissions, REQUEST_CODE_PERMISSIONS)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            val lastLocation = locationResult.lastLocation
            if (lastLocation != null) {
                currentLatitude = lastLocation.latitude
                currentLongitude = lastLocation.longitude
            }
        }
    }

    private fun turnOnGPS() {
        // 위치 설정이 켜져 있지 않으면 위치 설정창으로 이동
        val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivity(context, intent, null)
        }

        createDialog(
            context,
            "위치 서비스 권한 필요",
            "내 거주지 설정을 하기 위해서는 위치 서비스 권한이 필요합니다.",
            positiveButtonOnClickListener,
            defaultNegativeDialogInterfaceOnClickListener
        )
    }
}