package com.toyproject.ecosave.utilities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.util.Log
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

class GPSLocation(val context: Context) {
    companion object {
        var currentLatitude = 0.0 // 현재 위치 위도
        var currentLongitude = 0.0 // 현재 위치 경도
        const val REQUEST_CODE_PERMISSIONS = 1001
    }

    // 현재 위치를 파악하기 위해 필요한 권한 목록
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun getMyLocation(activity: Activity) {
        if ((activity is SignUpActivity)
            || (activity is HomeActivity)) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // 위치 서비스가 켜져있는지 확인
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                turnOnGPS() // 위치 서비스 켜기
            } else {
                // 10초 마다 현재 위치 수신
                setLocationRequest(activity)
            }
        }
    }

    private fun setLocationRequest(activity: Activity) {
        // 필요 권한이 허용되어 있는지 확인
        if (permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(activity)
            val locationRequest: LocationRequest =
                LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    HomeActivity.LOCATION_REQUEST_INTERVAL_MILLIS)
                    .setMinUpdateDistanceMeters(0.0F)
                    .build()

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            requestPermissions(activity, permissions, REQUEST_CODE_PERMISSIONS)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            locationResult.let {
                val lastLocation = it.lastLocation
                lastLocation?.let { it2 ->
                    currentLatitude = it2.latitude
                    currentLongitude = it2.longitude
                    Log.d("위치", "${HomeActivity.currentLatitude}, ${HomeActivity.currentLongitude}")
                }
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