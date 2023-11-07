package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

import com.toyproject.ecosave.databinding.ActivityDetailBinding
import com.toyproject.ecosave.models.DeviceTypeList
import com.toyproject.ecosave.utilities.fromDpToPx
import com.toyproject.ecosave.utilities.getCO2EmissionUnit
import com.toyproject.ecosave.utilities.getPowerOfConsumeUnit
import com.toyproject.ecosave.utilities.getTranslatedDeviceType

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    companion object {
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

    @SuppressLint("SetTextI18n")
    private fun showPyramids(
        relativeCO2EmissionGrade: Int,
        relativeCO2EmissionPercentage: Int,
        relativeElectricPowerConsumeGrade: Int,
        relativeElectricPowerConsumePercentage: Int
    ) {
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

        if ((relativeCO2EmissionGrade > 0)
            && (relativeCO2EmissionPercentage > 0)) {
            when (relativeCO2EmissionGrade) {
                1 -> {
                    binding.CO2Pyramid.setImageResource(R.drawable.pyramid1_co2)
                    binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_1))
                }
                2, 3 -> {
                    binding.CO2Pyramid.setImageResource(R.drawable.pyramid2_co2)
                    binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_2_and_3))
                }
                4, 5 -> {
                    binding.CO2Pyramid.setImageResource(R.drawable.pyramid3_co2)
                    binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_4_and_5))
                }
                6, 7 -> {
                    binding.CO2Pyramid.setImageResource(R.drawable.pyramid4_co2)
                    binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_6_and_7))
                }
                8, 9 -> {
                    binding.CO2Pyramid.setImageResource(R.drawable.pyramid5_co2)
                    binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_8_and_9))
                }
                else -> binding.CO2Pyramid.setImageResource(R.drawable.pyramid)
            }

            val paramsForCO2Grade = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
            )

            binding.textCO2EmissionGrade.text = "상위 $relativeCO2EmissionPercentage%"

            paramsForCO2Grade.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            paramsForCO2Grade.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            paramsForCO2Grade.marginStart = fromDpToPx(
                resources, MARGIN_TEXT[relativeCO2EmissionGrade - 1][0]
            )
            paramsForCO2Grade.topMargin = fromDpToPx(
                resources, MARGIN_TEXT[relativeCO2EmissionGrade - 1][1])

            binding.textCO2EmissionGrade.layoutParams = paramsForCO2Grade
        } else {
            binding.CO2Pyramid.visibility = View.GONE
            binding.textCO2EmissionGrade.text = ""
        }

        val paramsForEnergyConsumePyramid = ConstraintLayout.LayoutParams(pyramidWidth, pyramidHeight.toInt())
        paramsForEnergyConsumePyramid.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        paramsForEnergyConsumePyramid.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        paramsForEnergyConsumePyramid.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        binding.energyConsumePyramid.layoutParams = paramsForEnergyConsumePyramid

        if ((relativeElectricPowerConsumeGrade > 0) &&
            (relativeElectricPowerConsumePercentage > 0)) {
            when (relativeElectricPowerConsumeGrade) {
                1 -> {
                    binding.energyConsumePyramid.setImageResource(R.drawable.pyramid1_energy)
                    binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_1))
                }
                2, 3 -> {
                    binding.energyConsumePyramid.setImageResource(R.drawable.pyramid2_energy)
                    binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_2_and_3))
                }
                4, 5 -> {
                    binding.energyConsumePyramid.setImageResource(R.drawable.pyramid3_energy)
                    binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_4_and_5))
                }
                6, 7 -> {
                    binding.energyConsumePyramid.setImageResource(R.drawable.pyramid4_energy)
                    binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_6_and_7))
                }
                8, 9 -> {
                    binding.energyConsumePyramid.setImageResource(R.drawable.pyramid5_energy)
                    binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_8_and_9))
                }
                else -> binding.energyConsumePyramid.setImageResource(R.drawable.pyramid)
            }

            val paramsForEnergyConsumeGrade = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
            )

            binding.textEnergyConsumeGrade.text = "상위 $relativeElectricPowerConsumePercentage%"

            paramsForEnergyConsumeGrade.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            paramsForEnergyConsumeGrade.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            paramsForEnergyConsumeGrade.marginStart =
                fromDpToPx(resources, MARGIN_TEXT[relativeElectricPowerConsumeGrade - 1][0])
            paramsForEnergyConsumeGrade.topMargin =
                fromDpToPx(resources, MARGIN_TEXT[relativeElectricPowerConsumeGrade - 1][1])
            binding.textEnergyConsumeGrade.layoutParams = paramsForEnergyConsumeGrade
        } else {
            binding.energyConsumePyramid.visibility = View.GONE
            binding.textEnergyConsumeGrade.text = ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("deviceType", DeviceTypeList::class.java)
        } else {
            intent.getSerializableExtra("deviceType") as DeviceTypeList
        }

        val powerOfConsume = intent.getFloatExtra("powerOfConsume", -1.0F)
        val relativeElectricPowerConsumeGrade = intent.getIntExtra("relativeElectricPowerConsumeGrade", -1)
        val relativeElectricPowerConsumePercentage = intent.getIntExtra("relativeElectricPowerConsumePercentage", -1)
        val amountOfCO2Emission = intent.getFloatExtra("amountOfCO2Emission", -1.0F)
        val relativeCO2EmissionGrade = intent.getIntExtra("relativeCO2EmissionGrade", -1)
        val relativeCO2EmissionPercentage = intent.getIntExtra("relativeCO2EmissionPercentage", -1)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        if (powerOfConsume > 0.0F) {
            binding.textPowerOfConsume.text = powerOfConsume.toString()
        } else {
            binding.textPowerOfConsume.text = ""
        }

        if (amountOfCO2Emission > 0.0F) {
            binding.textCO2Emission.text = amountOfCO2Emission.toString()
        } else {
            binding.textCO2Emission.text = ""
        }

        when (deviceType) {
            deviceType -> {
                supportActionBar?.title = "상세 페이지 (${getTranslatedDeviceType(deviceType)})"
                binding.textPowerOfConsumeType.text = getPowerOfConsumeUnit(deviceType)["description"]
                binding.textPowerOfConsumeUnit.text = getPowerOfConsumeUnit(deviceType)["symbol"]
                binding.textCO2EmissionUnit.text = getCO2EmissionUnit(deviceType)
            }
            else -> {
                supportActionBar?.title = "상세 페이지"
            }
        }

        // CO2 배출량이 표기되어 있지 않은 제품(예: 보일러)의 경우 CO2 배출량에 관한 UI가 보이지 않도록 설정
        if (binding.textCO2EmissionUnit.text == "") {
            binding.relativeLayoutForCO2.visibility = View.GONE
        }

        showPyramids(
            relativeCO2EmissionGrade,
            relativeCO2EmissionPercentage,
            relativeElectricPowerConsumeGrade,
            relativeElectricPowerConsumePercentage
        )

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 앱바에 back 버튼 활성화
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish() // back 버튼을 누르면 이전 화면으로 돌아감
        }
        return true
    }
}