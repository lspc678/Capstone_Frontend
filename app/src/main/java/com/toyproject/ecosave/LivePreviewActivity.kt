package com.toyproject.ecosave

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.toyproject.ecosave.databinding.ActivityLivePreviewBinding

class LivePreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLivePreviewBinding

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1000
    }

    private fun getPermissionForCamera() {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        )

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            // 카메라 권한이 없는 경우 권한 요청
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )
            } else {
                // 카메라 권한 거부 및 '다시 묻지 않음'인 경우
                val alertDialogBuilderBtn = AlertDialog.Builder(this)
                alertDialogBuilderBtn.setTitle("카메라 권한 요청")
                alertDialogBuilderBtn.setMessage("카메라 권한이 거부되었습니다. 설정(앱 정보)에서 카메라 권한을 허용해 주세요.")
                alertDialogBuilderBtn.setPositiveButton("확인") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                alertDialogBuilderBtn.setNegativeButton("취소") { _, _ -> }

                val alertDialogBox = alertDialogBuilderBtn.create()
                alertDialogBox.show()
            }
        } else {
            // 카메라 권한이 있음
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                runCatching {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview)
                }.onFailure {
                    Log.d("라이브프리뷰", it.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("라이브프리뷰", e.toString())
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLivePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getPermissionForCamera()
    }
}