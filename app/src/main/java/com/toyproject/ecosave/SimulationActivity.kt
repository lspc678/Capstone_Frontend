package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

import com.toyproject.ecosave.databinding.ActivitySimulationBinding
import com.toyproject.ecosave.models.DeviceTypeList
import com.toyproject.ecosave.utilities.getPowerOfConsumeUnit
import com.toyproject.ecosave.utilities.getTranslatedDeviceType
import com.toyproject.ecosave.widget.defaultNegativeDialogInterfaceOnClickListener
import com.toyproject.ecosave.widget.simpleDialog

// 다이얼로그에서 값 받아오기 위한 인터페이스
interface SelectedAfterPowerOfConsumeInterface {
    fun onSelectedAfterPowerOfConsume(selected: Int)
}

class SimulationActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySimulationBinding

    // 기기 변경 전 등록된 모든 기기의 총 소비 전력량 (월간)
    // kWh 단위
    private var totalPowerOfConsumeForMonth = 0.0F

    // 기기 변경 후 등록된 모든 기기의 총 소비 전력량 (월간)
    // kWh 단위
    private var afterTotalPowerOfConsumeForMonth = 0.0F

    // 기기 변경 전 총 소비전력량을 계산하고 시뮬레이션 결과에 표시
    @SuppressLint("SetTextI18n")
    private fun calculateCurrentTotalPowerOfConsumeForMonth() {
        totalPowerOfConsumeForMonth = 0.0F

        for (relativeGradeData in HomeActivity.list) {
            when (relativeGradeData.deviceType) {
                DeviceTypeList.REFRIGERATOR,
                DeviceTypeList.WASHING_MACHINE -> {
                    totalPowerOfConsumeForMonth += relativeGradeData.powerOfConsume
                }
                DeviceTypeList.MICROWAVE_OVEN -> {
                    // 전자레인지의 경우 하루에 10분씩 사용한다고 가정
                    // 전자레인지에 대한 월간 소비 전력량 = 소비전력 * 1/6(시간) * 30(일)
                    // kWh로 환산하기 위해 맨 마지막에 1000으로 나눔
                    totalPowerOfConsumeForMonth += (relativeGradeData.powerOfConsume / 6 * 30) / 1000
                }
                DeviceTypeList.AIR_CONDITIONER -> {
                    // 에어컨의 경우 하루 평균 사용 시간을 통해 월간 소비 전력량을 계산
                    // 월간 소비 전력량 = 표준 월간 소비 전력량 * (하루 평균 사용 시간 / 7.8)
                    totalPowerOfConsumeForMonth +=
                        relativeGradeData.powerOfConsume * (relativeGradeData.averageUsageTimePerDay!! / 7.8F)
                }
                DeviceTypeList.TV -> {
                    // TV의 경우 하루 평균 사용 시간을 통해 월간 소비 전력량을 계산
                    // 월간 소비 전력량 = 소비전력 * 하루 평균 사용 시간 * 30(일)
                    // kWh로 환산하기 위해 맨 마지막에 1000으로 나눔
                    totalPowerOfConsumeForMonth +=
                        (relativeGradeData.powerOfConsume * relativeGradeData.averageUsageTimePerDay!! * 30) / 1000
                }
                DeviceTypeList.BOILER -> {
                    // 보일러의 경우 아직 시뮬레이션 기능을 제공하지 않음
                }
                else -> {

                }
            }
        }

        // 기기 변경 전 총 소비전력량 시뮬레이션 결과에 표시
        binding.textTotalPowerOfConsumeForMonth.text = "$totalPowerOfConsumeForMonth kWh/월"
    }

    // 기기 변경 후 총 소비전력량을 계산하고 시뮬레이션 결과에 표시
    @SuppressLint("SetTextI18n")
    private fun calculateAfterCurrentTotalPowerOfConsumeForMonth(
        position: Int, deviceType: DeviceTypeList?, afterPowerOfConsume: Int) {
        // 소비 전력 변화량
        val changeInPowerConsumption =
            afterPowerOfConsume - HomeActivity.list[position].powerOfConsume

        afterTotalPowerOfConsumeForMonth = 0.0F

        // 기기 변경 후 등록된 모든 기기의 총 소비 전력량 (월간) 계산
        when (deviceType) {
            DeviceTypeList.REFRIGERATOR,
            DeviceTypeList.WASHING_MACHINE -> {
                afterTotalPowerOfConsumeForMonth += changeInPowerConsumption
            }
            DeviceTypeList.MICROWAVE_OVEN -> {
                // 전자레인지의 경우 하루에 10분씩 사용한다고 가정
                // 전자레인지에 대한 월간 소비 전력량 = 소비전력 * 1/6(시간) * 30(일)
                // kWh로 환산하기 위해 맨 마지막에 1000으로 나눔
                afterTotalPowerOfConsumeForMonth += (changeInPowerConsumption / 6 * 30) / 1000
            }
            DeviceTypeList.AIR_CONDITIONER -> {
                // 에어컨의 경우 하루 평균 사용 시간을 통해 월간 소비 전력량을 계산
                // 월간 소비 전력량 = 표준 월간 소비 전력량 * (하루 평균 사용 시간 / 7.8)
                afterTotalPowerOfConsumeForMonth +=
                    changeInPowerConsumption * (HomeActivity.list[position].averageUsageTimePerDay!! / 7.8F)
            }
            DeviceTypeList.TV -> {
                // TV의 경우 하루 평균 사용 시간을 통해 월간 소비 전력량을 계산
                // 월간 소비 전력량 = 소비전력 * 하루 평균 사용 시간 * 30(일)
                // kWh로 환산하기 위해 맨 마지막에 1000으로 나눔
                afterTotalPowerOfConsumeForMonth +=
                    (changeInPowerConsumption * HomeActivity.list[position].averageUsageTimePerDay!! * 30) / 1000
            }
            DeviceTypeList.BOILER -> {
                // 보일러의 경우 아직 시뮬레이션 기능을 제공하지 않음
            }
            else -> {

            }
        }

        afterTotalPowerOfConsumeForMonth += totalPowerOfConsumeForMonth

        // 기기 변경 후 총 소비전력량 시뮬레이션 결과에 표시
        binding.textAfterTotalPowerOfConsumeForMonth.text = "$afterTotalPowerOfConsumeForMonth kWh/월"

        Log.d("시뮬레이션", totalPowerOfConsumeForMonth.toString())
        Log.d("시뮬레이션", afterTotalPowerOfConsumeForMonth.toString())

        calculateMonthlyElectricityBillChange()
    }

    // 한 달 전기 요금 변화량 계산
    @SuppressLint("ResourceAsColor", "SetTextI18n")
    private fun calculateMonthlyElectricityBillChange() {
        var textMonthlyElectricityBillChangeText = ""

        if (afterTotalPowerOfConsumeForMonth < totalPowerOfConsumeForMonth) {
            // 기기 변경 후 총 소비전력량이 더 적을 경우
            // 한 달 전기 요금 변화량에 대한 글자 색을 파란색으로 설정
            binding.textMonthlyElectricityBillChange.setTextColor(getColor(R.color.blue))
            textMonthlyElectricityBillChangeText += "-"

        } else if (afterTotalPowerOfConsumeForMonth > totalPowerOfConsumeForMonth) {
            // 기기 변경 후 총 소비전력량이 더 클 경우
            // 한 달 전기 요금 변화량에 대한 글자 색을 빨간색으로 설정
            binding.textMonthlyElectricityBillChange.setTextColor(getColor(R.color.red))
            textMonthlyElectricityBillChangeText += "+"
        } else {
            // 기기 변경 전과 후의 소비전력량이 같을 경우
            // 한 달 전기 요금 변화량에 대한 글자 색을 초록색으로 설정
            binding.textMonthlyElectricityBillChange.setTextColor(getColor(R.color.green))
        }

        // 기기 변경 전 예상 전기 요금 (월간)
        val monthlyElectricityBillForMonth = calculateMonthlyElectricityBill()

        // 기기 변경 후 예상 전기 요금 (월간)
        val afterMonthlyElectricityBillForMonth = calculateAfterMonthlyElectricityBill()

        textMonthlyElectricityBillChangeText += "${kotlin.math.abs(
            monthlyElectricityBillForMonth - afterMonthlyElectricityBillForMonth
        )}원"

        // 한 달 전기 요금 변화량을 화면에 출력
        binding.textMonthlyElectricityBillChange.text = textMonthlyElectricityBillChangeText
    }

    // 기기 변경 전 예상 전기 요금 (월간) 계산
    private fun calculateMonthlyElectricityBill() : Int {
        // 주택용(고압)으로 계산
        // 12월에 대한 것만 계산 (추후 현재 날짜에 따라 1월 ~ 12월 모두 제공 예정)

        // 기본 요금
        val baseFee = if (totalPowerOfConsumeForMonth <= 200.0F) {
            // 200kWh 이하 사용 시
            910
        } else if (totalPowerOfConsumeForMonth <= 400.0F) {
            // 201 ~ 400kWh 사용 시
            1600
        } else {
            // 401kWh 이상 사용 시
            7300
        }

        // 전력량에 의한 전기 요금
        var electricalEnergy = 0.0F

        if (totalPowerOfConsumeForMonth <= 200.0F) {
            // 200kWh 이하 사용 시
            electricalEnergy += (105 * totalPowerOfConsumeForMonth)
        } else {
            electricalEnergy += (105 * 200)

            if (totalPowerOfConsumeForMonth <= 400.0F) {
                // 201 ~ 400kWh 사용 시
                electricalEnergy += (174 * ((totalPowerOfConsumeForMonth - 200)))
            } else {
                electricalEnergy += (174 * 200)

                if (totalPowerOfConsumeForMonth <= 1000.0F) {
                    // 401 ~ 1000kWh 사용 시
                    electricalEnergy += (242.3F * ((totalPowerOfConsumeForMonth) - 400))
                } else {
                    // 1001kWh 이상 사용 시
                    electricalEnergy += (242.3F * 600)
                    electricalEnergy += (601.3F * (totalPowerOfConsumeForMonth) - 1000)
                }
            }
        }

        // 기후 환경 요금
        val climateEnvironmentFee = totalPowerOfConsumeForMonth * 9

        // 연료비조정액
        val fuelCostAdjustmentFee = totalPowerOfConsumeForMonth * 5

        return (baseFee + electricalEnergy + climateEnvironmentFee + fuelCostAdjustmentFee).toInt()
    }

    // 기기 변경 후 예상 전기 요금 (월간) 계산
    private fun calculateAfterMonthlyElectricityBill() : Int {
        // 주택용(고압)으로 계산
        // 12월에 대한 것만 계산 (추후 현재 날짜에 따라 1월 ~ 12월 모두 제공 예정)

        // 기본 요금
        val baseFee = if (afterTotalPowerOfConsumeForMonth <= 200.0F) {
            // 200kWh 이하 사용 시
            910
        } else if (afterTotalPowerOfConsumeForMonth <= 400.0F) {
            // 201 ~ 400kWh 사용 시
            1600
        } else {
            // 401kWh 이상 사용 시
            7300
        }

        // 전력량에 의한 전기 요금
        var electricalEnergy = 0.0F

        if (afterTotalPowerOfConsumeForMonth <= 200.0F) {
            // 200kWh 이하 사용 시
            electricalEnergy += (105 * afterTotalPowerOfConsumeForMonth)
        } else {
            electricalEnergy += (105 * 200)

            if (afterTotalPowerOfConsumeForMonth <= 400.0F) {
                // 201 ~ 400kWh 사용 시
                electricalEnergy += (174 * (afterTotalPowerOfConsumeForMonth - 200))
            } else {
                electricalEnergy += (174 * 200)

                if (afterTotalPowerOfConsumeForMonth <= 1000.0F) {
                    // 401 ~ 1000kWh 사용 시
                    electricalEnergy += (242.3F * (afterTotalPowerOfConsumeForMonth - 400))
                } else {
                    // 1001kWh 이상 사용 시
                    electricalEnergy += (242.3F * 600)
                    electricalEnergy += (601.3F * (afterTotalPowerOfConsumeForMonth - 1000))
                }
            }
        }

        // 기후 환경 요금
        val climateEnvironmentFee = afterTotalPowerOfConsumeForMonth * 9

        // 연료비조정액
        val fuelCostAdjustmentFee = afterTotalPowerOfConsumeForMonth * 5

        return (baseFee + electricalEnergy + climateEnvironmentFee + fuelCostAdjustmentFee).toInt()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimulationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("deviceType", DeviceTypeList::class.java)
        } else {
            intent.getSerializableExtra("deviceType") as DeviceTypeList
        }

        val powerOfConsume = intent.getFloatExtra("powerOfConsume", -1.0F)
        val relativeElectricPowerConsumePercentage = intent.getIntExtra("relativeElectricPowerConsumePercentage", -1)
        val position = intent.getIntExtra("position", -1)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        when (deviceType) {
            deviceType -> {
                supportActionBar?.title = "시뮬레이션 (${getTranslatedDeviceType(deviceType)})"
                binding.textPowerOfConsumeType.text = getPowerOfConsumeUnit(deviceType)["description"]
                binding.textPowerOfConsumeUnit.text = getPowerOfConsumeUnit(deviceType)["symbol"]
                binding.textPowerOfConsume.text = powerOfConsume.toString()
                binding.textAfterPowerOfConsumeType.text = "기기 변경 후"
                binding.textAfterPowerOfConsumeUnit.text = getPowerOfConsumeUnit(deviceType)["symbol"]
                binding.textAfterPowerOfConsume.text = ""
            }
            else -> {
                supportActionBar?.title = "시뮬레이션"
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 앱바에 back 버튼 활성화

        // 기기 변경 전 총 소비전력량을 계산하고 시뮬레이션 결과에 표시
        calculateCurrentTotalPowerOfConsumeForMonth()

        // 기기 변경 후 오른쪽에 있는 입력란을 클릭할 경우
        binding.relativeLayoutForAfterPowerOfConsume.setOnClickListener {
            val editText = EditText(this)
            editText.inputType = InputType.TYPE_CLASS_NUMBER

            val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                val text = editText.text.toString()

                if (text.toIntOrNull() != null) {
                    binding.textAfterPowerOfConsume.text = text
                    Log.d("시뮬레이션", text.toIntOrNull().toString())
                }
            }

            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("희망 소비전력량 입력")
            alertDialog.setView(editText)
            alertDialog.setPositiveButton("확인", positiveButtonOnClickListener)
            alertDialog.setNegativeButton("취소", defaultNegativeDialogInterfaceOnClickListener)
            alertDialog.show()
        }

        // 적용 버튼 클릭 시
        binding.btnApply.setOnClickListener {
            if (binding.textAfterPowerOfConsume.text == "") {
                simpleDialog(
                    this@SimulationActivity,
                    "시뮬레이션",
                    "기기 변경 후 소비전력량을 입력해 주세요."
                )
                return@setOnClickListener
            }

            val afterPowerOfConsume = binding.textAfterPowerOfConsume.text.toString().toIntOrNull()

            if (afterPowerOfConsume == null) {
                simpleDialog(
                    this@SimulationActivity,
                    "시뮬레이션",
                    "기기 변경 후 소비전력량에 정수만 입력해 주세요."
                )
                return@setOnClickListener
            }

            calculateAfterCurrentTotalPowerOfConsumeForMonth(position, deviceType, afterPowerOfConsume)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish() // back 버튼을 누르면 이전 화면으로 돌아감
        }
        return true
    }
}