package com.toyproject.ecosave

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
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
import com.toyproject.ecosave.utilities.checkLineUpHorizontal
import com.toyproject.ecosave.utilities.findPattern
import com.toyproject.ecosave.utilities.isRectReachesOtherRects
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@androidx.camera.core.ExperimentalGetImage
class TextReaderAnalyzer(
    private val textFoundListener: (String, Rect?) -> Unit
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
            for (line in block.lines) {
                textFoundListener(line.text, line.boundingBox)
            }
        }
    }
}

@androidx.camera.core.ExperimentalGetImage
class LivePreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLivePreviewBinding
    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    private var amountOfCO2DescriptionPosition: Rect? = null
    private var amountOfCO2UnitPosition: Rect? = null
    private var energyConsumptionDescriptionPosition: Rect? = null
    private var energyConsumptionUnitPosition: Rect? = null

    private var amountOfCO2Map = mutableMapOf<Float, Int>()
    private var energyConsumptionMap = mutableMapOf<Float, Int>()
    private var energyConsumption: Float = 0.0F
    private var amountOfCO2Emission: Float = 0.0F

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

    private fun onTextFound(foundText: String, lineFrame: Rect?) {
        when (deviceType) {
            DeviceTypeList.REFRIGERATOR -> {
                onTextFoundRefrigerator(foundText, lineFrame)
            }
            DeviceTypeList.TV -> {
                onTextFoundTV(foundText, lineFrame)
            }
            DeviceTypeList.WASHING_MACHINE -> {
                onTextFoundWashingMachine(foundText, lineFrame)
            }
            DeviceTypeList.MICROWAVE_OVEN -> {
                onTextFoundMicrowaveOven(foundText, lineFrame)
            }
            else -> {

            }
        }

        when (deviceType) {
            DeviceTypeList.REFRIGERATOR,
            DeviceTypeList.TV,
            DeviceTypeList.WASHING_MACHINE-> {
                if ((energyConsumption != 0.0F) && (amountOfCO2Emission != 0.0F)) {
                    // 에너지 소비전력과 CO2 배출량을 모두 찾았을 경우
                    val intent = Intent()

                    // 두 가지 정보를 intent에 저장
                    intent.putExtra("energyConsumption", energyConsumption)
                    intent.putExtra("amountOfCO2", amountOfCO2Emission)
                    setResult(GET_ENERGY_CONSUMPTION_AND_CO2, intent)
                    finish()
                }
            }
            DeviceTypeList.MICROWAVE_OVEN -> {
                if (energyConsumption != 0.0F) {
                    val intent = Intent()
                    intent.putExtra("energyConsumption", energyConsumption)
                    setResult(GET_ENERGY_CONSUMPTION_AND_CO2, intent)
                    finish()
                }
            }
            else -> {}
        }
    }

    // 에너지 소비 효율 등급 라벨(냉장고) 텍스트 인식
    private fun onTextFoundRefrigerator(foundText: String, lineFrame: Rect?) {
        if (lineFrame != null) {
            // foundText에 있는 공백 제거
            var text = foundText.replace(" ", "")

            // 로그 출력
            Log.d("라이브프리뷰", "텍스트: $text")

            var length = text.length

            // 아래와 같이 텍스트를 추출하는 경우가 발생
            // 1. 설명
            // 예: 월간소비전력량
            // 예: CO2
            // 2. 값
            // 예: 10.8
            // 예: 6
            // 3. 단위
            // 예: kWh/월
            // 예: g/시간
            // 4. 설명 + 값
            // 예: 월간소비전력량10.8
            // 예: CO26
            // 5. 값 + 단위
            // 예: 10.8kWh/월
            // 예: 6g/시간
            // 6. 설명 + 값 + 단위
            // 예: 월간소비전력량10.8kWh/월
            // 예: CO26g/시간

            // 에너지 소비전력 설명 텍스트 인식
            // 경우 1, 4, 6
            var res = findPattern(text, "월간소비전력량")
            if (res[0] != -1 && res[1] != -1) {
                // 에너지 소비전력 설명에 해당하는 텍스트를 찾음
                // 에너지 소비전력 설명이 있는 위치 저장
                energyConsumptionDescriptionPosition = lineFrame
                Log.d("라이브프리뷰", "에너지 소비전력 설명 위치 확인")
            }

            // 에너지 소비전력 단위 텍스트 인식
            // 경우 3, 5, 6
            res = findPattern(text, "kWh/월")
            if (res[0] != -1 && res[1] != -1) {
                // 에너지 소비전력 단위에 해당하는 텍스트를 찾음
                // 에너지 소비전력 단위가 있는 위치 저장
                energyConsumptionUnitPosition = lineFrame
                Log.d("라이브프리뷰", "에너지 소비전력 단위 위치 확인")
            } else {
                res = findPattern(text, "kWh월")
                if (res[0] != -1 && res[1] != -1) {
                    // 에너지 소비전력 단위에 해당하는 텍스트를 찾음
                    // 에너지 소비전력 단위가 있는 위치 저장
                    energyConsumptionUnitPosition = lineFrame
                    Log.d("라이브프리뷰", "에너지 소비전력 단위 위치 확인")
                }
            }

            // CO2 배출량 설명 텍스트 인식
            // 경우 1, 4, 6
            res = findPattern(text, "CO2")
            if (res[0] != -1 && res[1] != -1) {
                // CO2 배출량 설명에 해당하는 텍스트를 찾음
                // CO2 배출량 설명이 있는 위치 저장
                amountOfCO2DescriptionPosition = lineFrame
                Log.d("라이브프리뷰", "CO2 배출량 설명 위치 확인")
            }

            // CO2 배출량 단위 텍스트 인식
            // 경우 3, 5, 6
            res = findPattern(text, "g/시간")
            if (res[0] != -1 && res[1] != -1) {
                // CO2 배출량 단위에 해당하는 텍스트를 찾음
                // CO2 배출량 단위가 있는 위치 저장
                amountOfCO2UnitPosition = lineFrame
                Log.d("라이브프리뷰", "CO2 배출량 단위 위치 확인")
            } else {
                res = findPattern(text, "g시간")
                if (res[0] != -1 && res[1] != -1) {
                    // CO2 배출량 단위에 해당하는 텍스트를 찾음
                    // CO2 배출량 단위가 있는 위치 저장
                    amountOfCO2UnitPosition = lineFrame
                    Log.d("라이브프리뷰", "CO2 배출량 단위 위치 확인")
                }
            }

            // 에너지 소비전력 설명 및 단위가 있는 위치를 찾았을 경우
            // 에너지 소비전력에 해당하는 텍스트를 추출
            if (energyConsumptionDescriptionPosition != null && energyConsumptionUnitPosition != null) {
                if (checkLineUpHorizontal(energyConsumptionDescriptionPosition, lineFrame, energyConsumptionUnitPosition)
                    && isRectReachesOtherRects(energyConsumptionDescriptionPosition, lineFrame, energyConsumptionUnitPosition)) {
                    // 현재 인식된 텍스트의 위치는 에너지 소비전력 설명과 단위 사이에 있음
                    var energyConsumptionText = ""

                    for (idx in 0 until length) {
                        if (text[idx] == '.') {
                            // 소수점의 경우 정상적으로 인식
                            energyConsumptionText += '.'
                        } else if (text[idx].digitToIntOrNull() != null) {
                            energyConsumptionText += text[idx].digitToInt()
                        }
                    }

                    Log.d("라이브프리뷰", "에너지 소비전력 텍스트 추출")
                    Log.d("라이브프리뷰", energyConsumptionText)

                    val _energyConsumption = energyConsumptionText.toFloatOrNull()

                    if ((_energyConsumption != null) && (_energyConsumption > 0.0F)) {
                        energyConsumption = _energyConsumption
                        Log.d("라이브프리뷰", "pass(energy): $energyConsumption")
                        return
                    }
                }
            }

            // CO2 배출량 설명 및 단위가 있는 위치를 찾았을 경우
            // CO2 배출량에 해당하는 텍스트를 추출
            if (amountOfCO2DescriptionPosition != null && amountOfCO2UnitPosition != null) {
                if (checkLineUpHorizontal(amountOfCO2DescriptionPosition, lineFrame, amountOfCO2UnitPosition)
                    && isRectReachesOtherRects(energyConsumptionDescriptionPosition, lineFrame, energyConsumptionUnitPosition)) {
                    // 현재 인식된 텍스트의 위치는 CO2 배출량 설명과 단위 사이에 있음
                    var amountOfCO2EmissionText = ""

                    // text에서 CO2를 찾아서 제거
                    // 맨 마지막에 있는 2를 숫자로 인식하는 것을 방지
                    res = findPattern(text, "CO2")
                    if (res[0] != -1 && res[1] != -1) {
                        text = text.substring(res[1])

                        // 문자열 길이 재설정
                        length = text.length
                    }

                    for (idx in 0 until length) {
                        if (text[idx] == '.') {
                            // 소수점의 경우 정상적으로 인식
                            amountOfCO2EmissionText += '.'
                        } else if (text[idx].digitToIntOrNull() != null) {
                            amountOfCO2EmissionText += text[idx].digitToInt()
                        }
                    }

                    Log.d("라이브프리뷰", "CO2 배출량 텍스트 추출")
                    Log.d("라이브프리뷰", amountOfCO2EmissionText)

                    val _amountOfCO2EmissionText = amountOfCO2EmissionText.toFloatOrNull()

                    if ((_amountOfCO2EmissionText != null) && (_amountOfCO2EmissionText > 0.0F)) {
                        amountOfCO2Emission = _amountOfCO2EmissionText
                        Log.d("라이브프리뷰", "pass(CO2): $amountOfCO2Emission")
                        return
                    }
                }
            }
        }
    }

    private fun onTextFoundTV(foundText: String, lineFrame: Rect?) {
        if (lineFrame != null) {
            // foundText에 있는 공백 제거
            var text = foundText.replace(" ", "")

            // 로그 출력
            Log.d("라이브프리뷰", "텍스트: $text")

            var length = text.length

            // 아래와 같이 텍스트를 추출하는 경우가 발생
            // 1. 설명
            // 예: 소비전력
            // 예: CO2
            // 2. 값
            // 예: 28.2
            // 예: 10
            // 3. 단위
            // 예: W
            // 예: g/시간
            // 4. 설명 + 값
            // 예: 소비전력28.2
            // 예: CO210
            // 5. 값 + 단위
            // 예: 28.2W
            // 예: 10g/시간
            // 6. 설명 + 값 + 단위
            // 예: 소비전력28.2W
            // 예: CO210g/시간

            // 에너지 소비전력 설명 텍스트 인식
            // 경우 1, 4, 6
            var res = findPattern(text, "소비전력")
            if (res[0] != -1 && res[1] != -1) {
                // 에너지 소비전력 설명에 해당하는 텍스트를 찾음
                // 에너지 소비전력 설명이 있는 위치 저장
                energyConsumptionDescriptionPosition = lineFrame
                Log.d("라이브프리뷰", "에너지 소비전력 설명 위치 확인")
            }

            // 에너지 소비전력 단위 텍스트 인식
            // 경우 3, 5, 6
            // TV의 경우 맨 마지막에 W가 있는지만 확인하면 됨
            if (text[length - 1] == 'W') {
                energyConsumptionUnitPosition = lineFrame
                Log.d("라이브프리뷰", "에너지 소비전력 단위 위치 확인")
            }

            // CO2 배출량 설명 텍스트 인식
            // 경우 1, 4, 6
            res = findPattern(text, "CO2")
            if (res[0] != -1 && res[1] != -1) {
                // CO2 배출량 설명에 해당하는 텍스트를 찾음
                // CO2 배출량 설명이 있는 위치 저장
                amountOfCO2DescriptionPosition = lineFrame
                Log.d("라이브프리뷰", "CO2 배출량 설명 위치 확인")
            }

            // CO2 배출량 단위 텍스트 인식
            // 경우 3, 5, 6
            res = findPattern(text, "g/시간")
            if (res[0] != -1 && res[1] != -1) {
                // CO2 배출량 단위에 해당하는 텍스트를 찾음
                // CO2 배출량 단위가 있는 위치 저장
                amountOfCO2UnitPosition = lineFrame
                Log.d("라이브프리뷰", "CO2 배출량 단위 위치 확인")
            } else {
                res = findPattern(text, "g시간")
                if (res[0] != -1 && res[1] != -1) {
                    // CO2 배출량 단위에 해당하는 텍스트를 찾음
                    // CO2 배출량 단위가 있는 위치 저장
                    amountOfCO2UnitPosition = lineFrame
                    Log.d("라이브프리뷰", "CO2 배출량 단위 위치 확인")
                }
            }

            // 에너지 소비전력 설명 및 단위가 있는 위치를 찾았을 경우
            // 에너지 소비전력에 해당하는 텍스트를 추출
            if (energyConsumptionDescriptionPosition != null && energyConsumptionUnitPosition != null) {
                if (checkLineUpHorizontal(energyConsumptionDescriptionPosition, lineFrame, energyConsumptionUnitPosition)
                    && isRectReachesOtherRects(energyConsumptionDescriptionPosition, lineFrame, energyConsumptionUnitPosition)) {
                    // 현재 인식된 텍스트의 위치는 에너지 소비전력 설명과 단위 사이에 있음
                    var energyConsumptionText = ""

                    // text에서 소비전력을 찾음
                    // 소비전력 이후에 적혀있는 문자만 인정
                    res = findPattern(text, "소비전력")
                    if (res[0] != -1 && res[1] != -1) {
                        text = text.substring(res[1])

                        // 문자열 길이 재설정
                        length = text.length
                    }

                    for (idx in 0 until length) {
                        if (text[idx] == '.') {
                            // 소수점의 경우 정상적으로 인식
                            energyConsumptionText += '.'
                        } else if (text[idx].digitToIntOrNull() != null) {
                            energyConsumptionText += text[idx].digitToInt()
                        }
                    }

                    Log.d("라이브프리뷰", "에너지 소비전력 텍스트 추출")
                    Log.d("라이브프리뷰", energyConsumptionText)

                    val _energyConsumption = energyConsumptionText.toFloatOrNull()

                    if ((_energyConsumption != null) && (_energyConsumption > 0.0F)) {
                        energyConsumption = _energyConsumption
                        Log.d("라이브프리뷰", "pass(energy): $energyConsumption")
                        return
                    }
                }
            }

            // CO2 배출량 설명 및 단위가 있는 위치를 찾았을 경우
            // CO2 배출량에 해당하는 텍스트를 추출
            if (amountOfCO2DescriptionPosition != null && amountOfCO2UnitPosition != null) {
                if (checkLineUpHorizontal(amountOfCO2DescriptionPosition, lineFrame, amountOfCO2UnitPosition)
                    && isRectReachesOtherRects(amountOfCO2DescriptionPosition, lineFrame, amountOfCO2UnitPosition)) {
                    // 현재 인식된 텍스트의 위치는 CO2 배출량 설명과 단위 사이에 있음
                    var amountOfCO2EmissionText = ""

                    // text에서 CO2를 찾아서 제거
                    // 맨 마지막에 있는 2를 숫자로 인식하는 것을 방지
                    res = findPattern(text, "CO2")
                    if (res[0] != -1 && res[1] != -1) {
                        text = text.substring(res[1])

                        // 문자열 길이 재설정
                        length = text.length
                    }

                    for (idx in 0 until length) {
                        if (text[idx] == '.') {
                            // 소수점의 경우 정상적으로 인식
                            amountOfCO2EmissionText += '.'
                        } else if (text[idx].digitToIntOrNull() != null) {
                            amountOfCO2EmissionText += text[idx].digitToInt()
                        }
                    }

                    Log.d("라이브프리뷰", "CO2 배출량 텍스트 추출")
                    Log.d("라이브프리뷰", amountOfCO2EmissionText)

                    val _amountOfCO2EmissionText = amountOfCO2EmissionText.toFloatOrNull()

                    if ((_amountOfCO2EmissionText != null) && (_amountOfCO2EmissionText > 0.0F)) {
                        amountOfCO2Emission = _amountOfCO2EmissionText
                        Log.d("라이브프리뷰", "pass(CO2): $amountOfCO2Emission")
                        return
                    }
                }
            }
        }
    }

    private fun onTextFoundWashingMachine(foundText: String, lineFrame: Rect?) {
        if (lineFrame != null) {
            // foundText에 있는 공백 제거
            var text = foundText.replace(" ", "")

            // 로그 출력
            Log.d("라이브프리뷰", "텍스트: $text")

            var length = text.length

            // 아래와 같이 텍스트를 추출하는 경우가 발생
            // 1. 설명
            // 예: 1kg당소비전력량
            // 예: CO2
            // 2. 값
            // 예: 62.8
            // 예: 219
            // 3. 단위
            // 예: Wh/kg
            // 예: g/회
            // 4. 설명 + 값
            // 예: 1kg당소비전력량62.8
            // 예: CO2219
            // 5. 값 + 단위
            // 예: 62.8Wh/kg
            // 예: 219g/회
            // 6. 설명 + 값 + 단위
            // 예: 1kg당소비전력량62.8Wh/kg
            // 예: CO2219g/회

            // 에너지 소비전력 설명 텍스트 인식
            // 경우 1, 4, 6
            var res = findPattern(text, "1kg당소비전력량")
            if (res[0] != -1 && res[1] != -1) {
                // 에너지 소비전력 설명에 해당하는 텍스트를 찾음
                // 에너지 소비전력 설명이 있는 위치 저장
                energyConsumptionDescriptionPosition = lineFrame
                Log.d("라이브프리뷰", "에너지 소비전력 설명 위치 확인")
            }

            // 에너지 소비전력 단위 텍스트 인식
            // 경우 3, 5, 6
            res = findPattern(text, "Wh/kg")
            if (res[0] != -1 && res[1] != -1) {
                // 에너지 소비전력 단위에 해당하는 텍스트를 찾음
                // 에너지 소비전력 단위가 있는 위치 저장
                energyConsumptionUnitPosition = lineFrame
                Log.d("라이브프리뷰", "에너지 소비전력 단위 위치 확인")
            } else {
                res = findPattern(text, "Whkg")
                if (res[0] != -1 && res[1] != -1) {
                    // 에너지 소비전력 단위에 해당하는 텍스트를 찾음
                    // 에너지 소비전력 단위가 있는 위치 저장
                    energyConsumptionUnitPosition = lineFrame
                    Log.d("라이브프리뷰", "에너지 소비전력 단위 위치 확인")
                }
            }

            // CO2 배출량 설명 텍스트 인식
            // 경우 1, 4, 6
            res = findPattern(text, "CO2")
            if (res[0] != -1 && res[1] != -1) {
                // CO2 배출량 설명에 해당하는 텍스트를 찾음
                // CO2 배출량 설명이 있는 위치 저장
                amountOfCO2DescriptionPosition = lineFrame
                Log.d("라이브프리뷰", "CO2 배출량 설명 위치 확인")
            }

            // CO2 배출량 단위 텍스트 인식
            // 경우 3, 5, 6
            res = findPattern(text, "g/회")
            if (res[0] != -1 && res[1] != -1) {
                // CO2 배출량 단위에 해당하는 텍스트를 찾음
                // CO2 배출량 단위가 있는 위치 저장
                amountOfCO2UnitPosition = lineFrame
                Log.d("라이브프리뷰", "CO2 배출량 단위 위치 확인")
            } else {
                res = findPattern(text, "g회")
                if (res[0] != -1 && res[1] != -1) {
                    // CO2 배출량 단위에 해당하는 텍스트를 찾음
                    // CO2 배출량 단위가 있는 위치 저장
                    amountOfCO2UnitPosition = lineFrame
                    Log.d("라이브프리뷰", "CO2 배출량 단위 위치 확인")
                }
            }

            // 에너지 소비전력 설명 및 단위가 있는 위치를 찾았을 경우
            // 에너지 소비전력에 해당하는 텍스트를 추출
            if (energyConsumptionDescriptionPosition != null && energyConsumptionUnitPosition != null) {
                if (checkLineUpHorizontal(energyConsumptionDescriptionPosition, lineFrame, energyConsumptionUnitPosition)
                    && isRectReachesOtherRects(energyConsumptionDescriptionPosition, lineFrame, energyConsumptionUnitPosition)) {
                    // 현재 인식된 텍스트의 위치는 에너지 소비전력 설명과 단위 사이에 있음
                    var energyConsumptionText = ""

                    // text에서 1kg을 찾음
                    // 1kg 이후에 적혀있는 문자만 인정
                    res = findPattern(text, "1kg")
                    if (res[0] != -1 && res[1] != -1) {
                        text = text.substring(res[1])

                        // 문자열 길이 재설정
                        length = text.length
                    }

                    for (idx in 0 until length) {
                        if (text[idx] == '.') {
                            // 소수점의 경우 정상적으로 인식
                            energyConsumptionText += '.'
                        } else if (text[idx].digitToIntOrNull() != null) {
                            energyConsumptionText += text[idx].digitToInt()
                        }
                    }

                    Log.d("라이브프리뷰", "에너지 소비전력 텍스트 추출")
                    Log.d("라이브프리뷰", energyConsumptionText)

                    val _energyConsumption = energyConsumptionText.toFloatOrNull()

                    if ((_energyConsumption != null) && (_energyConsumption > 0.0F)) {
                        energyConsumption = _energyConsumption
                        Log.d("라이브프리뷰", "pass(energy): $energyConsumption")
                        return
                    }
                }
            }

            // CO2 배출량 설명 및 단위가 있는 위치를 찾았을 경우
            // CO2 배출량에 해당하는 텍스트를 추출
            if (amountOfCO2DescriptionPosition != null && amountOfCO2UnitPosition != null) {
                if (checkLineUpHorizontal(amountOfCO2DescriptionPosition, lineFrame, amountOfCO2UnitPosition)
                    && isRectReachesOtherRects(amountOfCO2DescriptionPosition, lineFrame, amountOfCO2UnitPosition)) {
                    // 현재 인식된 텍스트의 위치는 CO2 배출량 설명과 단위 사이에 있음
                    var amountOfCO2EmissionText = ""

                    // text에서 CO2를 찾아서 제거
                    // 맨 마지막에 있는 2를 숫자로 인식하는 것을 방지
                    res = findPattern(text, "CO2")
                    if (res[0] != -1 && res[1] != -1) {
                        text = text.substring(res[1])

                        // 문자열 길이 재설정
                        length = text.length
                    }

                    for (idx in 0 until length) {
                        if (text[idx] == '.') {
                            // 소수점의 경우 정상적으로 인식
                            amountOfCO2EmissionText += '.'
                        } else if (text[idx].digitToIntOrNull() != null) {
                            amountOfCO2EmissionText += text[idx].digitToInt()
                        }
                    }

                    Log.d("라이브프리뷰", "CO2 배출량 텍스트 추출")
                    Log.d("라이브프리뷰", amountOfCO2EmissionText)

                    val _amountOfCO2EmissionText = amountOfCO2EmissionText.toFloatOrNull()

                    if ((_amountOfCO2EmissionText != null) && (_amountOfCO2EmissionText > 0.0F)) {
                        amountOfCO2Emission = _amountOfCO2EmissionText
                        Log.d("라이브프리뷰", "pass(CO2): $amountOfCO2Emission")
                        return
                    }
                }
            }
        }
    }

    private fun onTextFoundMicrowaveOven(foundText: String, lineFrame: Rect?) {
        if (lineFrame != null) {
            val text = foundText.replace(" ", "")
            val length = text.length
            Log.d("라이브프리뷰", text)

            // 정격소비전력
            // 정격소비전력 : 1,200W
            val res = findPattern(text, "정격소비전력")
            if (res[0] != -1 && res[1] != -1) {
                energyConsumptionDescriptionPosition = lineFrame
                return
            }

            // 1,250W
            // 정격소비전력 : 1,200W
            if (text[length - 1] == 'W') {
                if (energyConsumptionDescriptionPosition != null) {
                    if (checkLineUpHorizontal(energyConsumptionDescriptionPosition, lineFrame)) {
                        energyConsumptionUnitPosition = lineFrame
                    }
                }
            }

            if (energyConsumptionDescriptionPosition != null && energyConsumptionUnitPosition != null) {
                if (checkLineUpHorizontal(energyConsumptionDescriptionPosition, lineFrame)
                    && checkLineUpHorizontal(lineFrame, energyConsumptionUnitPosition)) {
                    var energyConsumptionText = ""

                    for (idx in 0 until length - 1) {
                        if (text[idx].digitToIntOrNull() != null) {
                            energyConsumptionText += text[idx].digitToInt()
                        }
                    }

                    val _energyConsumption = energyConsumptionText.toFloatOrNull()

                    if (_energyConsumption != null) {
                        energyConsumption = _energyConsumption
                        Log.d("라이브프리뷰", "pass: $energyConsumption")
                    }
                }
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