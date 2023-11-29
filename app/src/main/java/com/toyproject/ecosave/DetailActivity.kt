package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

import com.toyproject.ecosave.api.APIClientForServerByPassSSLCertificate
import com.toyproject.ecosave.api.APIInterface
import com.toyproject.ecosave.api.requestmodels.ApplianceDeleteRequest
import com.toyproject.ecosave.api.responsemodels.DefaultResponse
import com.toyproject.ecosave.databinding.ActivityDetailBinding
import com.toyproject.ecosave.models.DeviceTypeList
import com.toyproject.ecosave.utilities.fromDpToPx
import com.toyproject.ecosave.utilities.getCO2EmissionUnit
import com.toyproject.ecosave.utilities.getPowerOfConsumeUnit
import com.toyproject.ecosave.utilities.getTranslatedDeviceType
import com.toyproject.ecosave.widget.ProgressDialog
import com.toyproject.ecosave.widget.createDialog
import com.toyproject.ecosave.widget.defaultNegativeDialogInterfaceOnClickListener
import com.toyproject.ecosave.widget.simpleDialog

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigDecimal
import java.math.RoundingMode

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

    private fun callApplianceDelete(
        deviceType: DeviceTypeList?, id: Int) {
        // progress bar 불러오기
        val progressDialog = ProgressDialog.getProgressDialog(this, "처리 중 입니다")
        progressDialog.show()

        if (id == -1) {
            simpleDialog(
                this,
                "기기 삭제",
                "기기 삭제 도중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            )
            return
        }

        val type = getTranslatedDeviceType(deviceType)
        val apiInterface = APIClientForServerByPassSSLCertificate
            .getClient()
            .create(APIInterface::class.java)

        val call: Call<DefaultResponse>?

        when (deviceType) {
            DeviceTypeList.REFRIGERATOR -> {
                call = apiInterface.applianceRefrigeratorDelete(
                    ApplianceDeleteRequest(id)
                )
            }
            DeviceTypeList.AIR_CONDITIONER -> {
                call = apiInterface.applianceAirConditionerDelete(
                    ApplianceDeleteRequest(id)
                )
            }
            DeviceTypeList.TV -> {
                call = apiInterface.applianceTelevisionDelete(
                    ApplianceDeleteRequest(id)
                )
            }
            DeviceTypeList.WASHING_MACHINE -> {
                call = apiInterface.applianceWashingMachineDelete(
                    ApplianceDeleteRequest(id)
                )
            }
            DeviceTypeList.MICROWAVE_OVEN -> {
                call = apiInterface.applianceMicrowaveDelete(
                    ApplianceDeleteRequest(id)
                )
            }
            DeviceTypeList.BOILER -> {
                call = apiInterface.applianceBoilerDelete(
                    ApplianceDeleteRequest(id)
                )
            }
            DeviceTypeList.DRYER -> {
                call = apiInterface.applianceDryerDelete(
                    ApplianceDeleteRequest(id)
                )
            }
            else -> {
                call = null
            }
        }

        call?.enqueue(
            object : Callback<DefaultResponse> {
                override fun onResponse(
                    call: Call<DefaultResponse>,
                    response: Response<DefaultResponse>
                ) {
                    // progress bar 종료
                    progressDialog.dismiss()

                    if (response.isSuccessful) {
                        // status code가 200 ~ 299일 때
                        val result = response.body()

                        if (result != null) {
                            if (result.success) {
                                Log.d("기기삭제 ($type)", "결과: 성공")
                                Log.d("기기삭제 ($type)", result.toString())

                                val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                                    // 기기가 삭제되었으므로 홈 화면으로 이동
                                    val intent = Intent(this@DetailActivity, HomeActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(intent)
                                    finish()
                                }

                                createDialog(
                                    this@DetailActivity,
                                    "기기 삭제",
                                    "기기가 성공적으로 삭제되었습니다.",
                                    positiveButtonOnClickListener
                                )

                            } else {
                                simpleDialog(
                                    this@DetailActivity,
                                    "기기 삭제",
                                    "기기 삭제에 실패했습니다. 다시 시도해주세요."
                                )

                                Log.d("기기삭제 ($type)", "결과: 실패")
                                Log.d("기기삭제 ($type)", result.toString())
                            }
                        }
                    } else {
                        // status code가 200 ~ 299가 아닐 때
                        val errorResult = response.errorBody()
                        val result = response.body()

                        if (errorResult != null) {
                            Log.d("기기삭제 ($type)", "결과: 실패 (response.isSuccessful 통과하지 못함)")
                            Log.d("기기삭제 ($type)", "statusCode: ${response.code()}")
                            Log.d("기기삭제 ($type)", errorResult.string())
                            Log.d("기기삭제 ($type)", result.toString())
                        }
                    }
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    // progress bar 종료
                    progressDialog.dismiss()

                    simpleDialog(
                        this@DetailActivity,
                        "기기 삭제",
                        "서버와의 통신이 원활하지 않습니다. 잠시 후 다시 시도해주세요."
                    )

                    Log.d("기기삭제 ($type)", "결과: 실패 (onFailure)")
                    Log.d("기기삭제 ($type)", t.message.toString())
                }
            }
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
            && (relativeCO2EmissionPercentage >= 0)) {
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
            (relativeElectricPowerConsumePercentage >= 0)) {
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

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("deviceType", DeviceTypeList::class.java)
        } else {
            intent.getSerializableExtra("deviceType") as DeviceTypeList
        }

        // 해당 기기의 id 값
        val id = intent.getIntExtra("id", -1)
        val powerOfConsume = intent.getDoubleExtra("powerOfConsume", -1.0)
        val relativeElectricPowerConsumeGrade = intent.getIntExtra("relativeElectricPowerConsumeGrade", -1)
        val relativeElectricPowerConsumePercentage = intent.getIntExtra("relativeElectricPowerConsumePercentage", -1)
        val amountOfCO2Emission = intent.getDoubleExtra("amountOfCO2Emission", -1.0)
        val relativeCO2EmissionGrade = intent.getIntExtra("relativeCO2EmissionGrade", -1)
        val relativeCO2EmissionPercentage = intent.getIntExtra("relativeCO2EmissionPercentage", -1)
        val position = intent.getIntExtra("position", -1)
        val averageUsageTimePerDay = intent.getDoubleExtra("averageUsageTimePerDay", -1.0)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        if (powerOfConsume > 0.0) {
            val _powerOfConsume = BigDecimal(powerOfConsume).setScale(1, RoundingMode.HALF_UP)
            binding.textPowerOfConsume.text = _powerOfConsume.toString()
        } else {
            binding.textPowerOfConsume.text = ""
        }

        if (amountOfCO2Emission > 0.0) {
            val _amountOfCO2Emission = BigDecimal(amountOfCO2Emission).setScale(1, RoundingMode.HALF_UP)
            binding.textCO2Emission.text = _amountOfCO2Emission.toString()
        } else {
            binding.textCO2Emission.text = ""
        }

        // 시뮬레이션 버튼 클릭시
        binding.btnSimulation.setOnClickListener {
            val intent = Intent(this, SimulationActivity::class.java)

            // 기기 종류를 intent에 저장
            intent.putExtra("deviceType", deviceType)

            // 소비전력을 intent에 저장
            intent.putExtra("powerOfConsume", powerOfConsume)

            // 전력 소비 누적 비율(%)을 intent에 저장
            intent.putExtra("relativeElectricPowerConsumePercentage", relativeElectricPowerConsumePercentage)

            // position 정보를 intent에 저장
            intent.putExtra("position", position)

            startActivity(intent)
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

        binding.btnDeleteDevice.setOnClickListener {
            val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                callApplianceDelete(deviceType, id)
            }

            createDialog(
                this@DetailActivity,
                "기기 삭제",
                "해당 기기를 삭제하시겠습니까?",
                positiveButtonOnClickListener,
                defaultNegativeDialogInterfaceOnClickListener
            )
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 앱바에 back 버튼 활성화
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish() // back 버튼을 누르면 이전 화면으로 돌아감
        }
        return true
    }
}