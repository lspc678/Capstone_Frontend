package com.toyproject.ecosave

import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.net.Uri

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

import com.toyproject.ecosave.databinding.ActivityLivePreviewBinding
import com.toyproject.ecosave.models.DeviceTypeList
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@androidx.camera.core.ExperimentalGetImage
class TextReaderAnalyzer(
    private val textFoundListener: (String) -> Unit
) : ImageAnalysis.Analyzer{
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { process(it, imageProxy) }
    }

    private fun process(image: Image, imageProxy: ImageProxy) {
        try {
            readTextFromImage(InputImage.fromMediaImage(image, 90), imageProxy)
        } catch (e: IOException) {
            Log.d("라이브프리뷰", "Failed to load the image")
            e.printStackTrace()
        }
    }

    private fun readTextFromImage(image: InputImage, imageProxy: ImageProxy) {
        TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            .process(image)
            .addOnSuccessListener { visionText ->
                processTextFromImage(visionText, imageProxy)
                imageProxy.close()
            }
            .addOnFailureListener { error ->
                Log.d("라이브프리뷰", "Failed to process the image")
                error.printStackTrace()
                imageProxy.close()
            }
    }

    private fun processTextFromImage(visionText: Text, imageProxy: ImageProxy) {
        for (block in visionText.textBlocks) {
            // textFoundListener(block.text)
            for (line in block.lines) {
               textFoundListener(line.text)
            }
        }
    }
}

@androidx.camera.core.ExperimentalGetImage
class LivePreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLivePreviewBinding
    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    private var amountOfCO2Map = mutableMapOf<Float, Int>()
    private var energyConsumptionMap = mutableMapOf<Float, Int>()

    private var deviceType = DeviceTypeList.OTHERS

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1000
        private const val GET_ENERGY_CONSUMPTION_AND_CO2 = 50
    }

    private val imageAnalyzer by lazy {
        ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()
            .also {
                it.setAnalyzer(
                    cameraExecutor,
                    TextReaderAnalyzer(::onTextFound)
                )
            }
    }

    private fun onTextFound(foundText: String) {
        when (deviceType) {
            DeviceTypeList.TV -> {
                onTextFoundTV(foundText)
            }
            DeviceTypeList.WASHING_MACHINE -> {
                onTextFoundWashingMachine(foundText)
            }
            else -> {

            }
        }

        if (amountOfCO2Map.isNotEmpty() && energyConsumptionMap.isNotEmpty()) {
            var maxCount = 0
            var energyConsumption = 0.0F
            var amountOfCO2 = 0.0F

            for (entry in energyConsumptionMap) {
                if (entry.value > maxCount) {
                    maxCount = entry.value
                    energyConsumption = entry.key
                }
            }

            maxCount = 0
            for (entry in amountOfCO2Map) {
                if (entry.value > maxCount) {
                    maxCount = entry.value
                    amountOfCO2 = entry.key
                }
            }

            val intent = Intent()
            intent.putExtra("energyConsumption", energyConsumption)
            intent.putExtra("amountOfCO2", amountOfCO2)
            setResult(GET_ENERGY_CONSUMPTION_AND_CO2, intent)
            finish()
        }
    }

    private fun onTextFoundTV(foundText: String) {
        if (foundText.length >= 7) {
            try {
                val text = foundText.replace(" ", "") // 소비전력:28.2W
                if ((text.substring(0, 4) == "소비전력") && text.last() == 'W') {
                    val length = text.length
                    val energyConsumptionText = text.substring(5, length - 1).toFloatOrNull()
                    if (energyConsumptionText != null) { // 숫자로 변환이 가능한 경우
                        val cnt = energyConsumptionMap[energyConsumptionText]
                        if (cnt != null) {
                            energyConsumptionMap[energyConsumptionText] = cnt + 1
                        } else {
                            energyConsumptionMap[energyConsumptionText] = 1
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("라이브프리뷰", e.toString())
            }
        } else if (foundText.length >= 4) {
            try {
                val text = foundText.replace(" ", "") // 10g시간 or 10g/시간
                val length = text.length
                Log.d("라이브프리뷰", text)
                if (length >= 4) {
                    var amountOfCO2Text: Float? = null
                    if (text.substring(length - 4, length) == "g/시간") {
                        amountOfCO2Text = text.substring(0, length - 4).toFloatOrNull()
                    } else if (text.substring(length - 3, length) == "g시간") {
                        amountOfCO2Text = text.substring(0, length - 3).toFloatOrNull()
                    }

                    if ((amountOfCO2Text != null) && (amountOfCO2Text > 1.0F)) { // 숫자로 변환할 수 있으며 CO2 배출량이 1 보다 큰 경우
                        val cnt = amountOfCO2Map[amountOfCO2Text]
                        if (cnt != null) {
                            amountOfCO2Map[amountOfCO2Text] = cnt + 1
                        } else {
                            amountOfCO2Map[amountOfCO2Text] = 1
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("라이브프리뷰", e.toString())
            }
        }
    }

    private fun onTextFoundWashingMachine(foundText: String) {
        if (foundText.length >= 6) {
            try {
                val text = foundText.replace(" ", "") // 10Wh/kg or 10Whkg
                val length = text.length
                Log.d("라이브프리뷰", text)
                if (length >= 5) {
                    var energyConsumptionText: Float? = null

                    val unit1 = "Wh/kg"
                    val unit2 = "Whkg"

                    if (text.substring(length - unit1.length, length) == unit1) {
                        energyConsumptionText = text.substring(0, length - unit1.length).toFloatOrNull()
                    } else if (text.substring(length - unit2.length, length) == unit2) {
                        energyConsumptionText = text.substring(0, length - unit2.length).toFloatOrNull()
                    }

                    if ((energyConsumptionText != null) && (energyConsumptionText > 1.0F)) { // 숫자로 변환할 수 있으며 CO2 배출량이 1 보다 큰 경우
                        val cnt = energyConsumptionMap[energyConsumptionText]
                        if (cnt != null) {
                            energyConsumptionMap[energyConsumptionText] = cnt + 1
                        } else {
                            energyConsumptionMap[energyConsumptionText] = 1
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("라이브프리뷰", e.toString())
            }
        } else if (foundText.length >= 3) {
            try {
                val text = foundText.replace(" ", "") // 10g/회 or 10g회
                val length = text.length
                Log.d("라이브프리뷰", text)
                if (length >= 3) {
                    var amountOfCO2Text: Float? = null

                    val unit1 = "g/회"
                    val unit2 = "g회"

                    if (text.substring(length - unit1.length, length) == unit1) {
                        amountOfCO2Text = text.substring(0, length - unit1.length).toFloatOrNull()
                    } else if (text.substring(length - unit2.length, length) == unit2) {
                        amountOfCO2Text = text.substring(0, length - unit2.length).toFloatOrNull()
                    }

                    if ((amountOfCO2Text != null) && (amountOfCO2Text > 1.0F)) { // 숫자로 변환할 수 있으며 CO2 배출량이 1 보다 큰 경우
                        val cnt = amountOfCO2Map[amountOfCO2Text]
                        if (cnt != null) {
                            amountOfCO2Map[amountOfCO2Text] = cnt + 1
                        } else {
                            amountOfCO2Map[amountOfCO2Text] = 1
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("라이브프리뷰", e.toString())
            }
        }
    }

    private fun ProcessCameraProvider.bind(
        preview: Preview,
        imageAnalyzer: ImageAnalysis
    ) = try {
        unbindAll()
        bindToLifecycle(
            this@LivePreviewActivity,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalyzer
        )
    } catch (ise: IllegalStateException) {
        // Thrown if binding is not done from the main thread
        Log.e("라이브프리뷰", "Binding failed", ise)
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
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)
            cameraProviderFuture.get().bind(preview, imageAnalyzer)
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLivePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedItemPosition = intent.getIntExtra("selectedItemPosition", -1)

        if (selectedItemPosition == -1) {
            finish()
        } else {
            when (selectedItemPosition) {
                0 -> { // 냉장고
                    deviceType = DeviceTypeList.REFRIGERATOR
                }
                1 -> { // 에어컨
                    deviceType = DeviceTypeList.AIR_CONDITIONER
                }
                2 -> { // TV
                    deviceType = DeviceTypeList.TV
                }
                3 -> { // 세탁기
                    deviceType = DeviceTypeList.WASHING_MACHINE
                }
                4 -> { // 전자레인지
                    deviceType = DeviceTypeList.MICROWAVE_OVEN
                }
                5 -> { // 보일러
                    deviceType = DeviceTypeList.BOILER
                }
                else -> {
                    finish()
                }
            }
            getPermissionForCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}