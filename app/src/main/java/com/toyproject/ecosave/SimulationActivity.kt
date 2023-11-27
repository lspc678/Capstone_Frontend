package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.toyproject.ecosave.databinding.ActivitySimulationBinding
import com.toyproject.ecosave.models.DeviceTypeList
import com.toyproject.ecosave.models.RecommendProductData
import com.toyproject.ecosave.utilities.getPowerOfConsumeUnit
import com.toyproject.ecosave.utilities.getTranslatedDeviceType
import com.toyproject.ecosave.widget.defaultNegativeDialogInterfaceOnClickListener
import com.toyproject.ecosave.widget.simpleDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.math.BigDecimal
import java.math.RoundingMode

class SimulationActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySimulationBinding

    // 기기 변경 전 등록된 모든 기기의 총 소비 전력량 (월간)
    // kWh 단위
    private var totalPowerOfConsumeForMonth = 0.0

    // 기기 변경 후 등록된 모든 기기의 총 소비 전력량 (월간)
    // kWh 단위
    private var afterTotalPowerOfConsumeForMonth = 0.0

    private var recyclerView: RecyclerView? = null
    private var recyclerViewProductRecommendationListAdapter:
            RecyclerViewProductRecommendationListAdapter? = null

    var productRecommendationList = mutableListOf<RecommendProductData>()

    private val dummyData = mutableListOf(
        RecommendProductData(
            "",
            "NCB752-27L‚ LNG‚ FF",
            93.2
        ),
        RecommendProductData(
            "",
            "DRC-45S FC (LNG)",
            93.0
        ),
        RecommendProductData(
            "",
            "AST 콘덴싱-37H (LNG‚FE)",
            92.8
        ),
        RecommendProductData(
            "",
            "NCB354-33L (FF‚ LNG)",
            92.5
        ),
        RecommendProductData(
            "",
            "NCB384-15L (FF‚ LNG)",
            92.5
        ),
        RecommendProductData(
            "",
            "거꾸로 NEW 콘덴싱 P10-30H",
            92.2
        ),
        RecommendProductData(
            "",
            "NCB384-22K (FF‚ LNG)",
            92.2
        ),
        RecommendProductData(
            "",
            "거꾸로 NEW 콘덴싱 P10-30HW",
            92.0
        ),
        RecommendProductData(
            "",
            "거꾸로 ECO 콘덴싱 S11-18HEN",
            92.0
        ),
        RecommendProductData(
            "",
            "NCB551-14K‚LNG‚FE",
            91.8
        ),
    )

    // 시뮬레이션을 위한 기기 정보를 크롤링하여 List로 반환(TV, 전자레인지, 냉장고, 에어컨)
    private suspend fun crawl(): List<RecommendProductData> = withContext(Dispatchers.IO) {
        val searchSite = "https://prod.danawa.com/list/?cate="

        //tv = 1022811 (단위 W), 전자레인지 = 10338815 (단위 W), 냉장고 = 10251508 (단위 kWh(월)), 에어컨 = 1022644(단위 : kW)
        val productCode = "1022811"
        val document = Jsoup.connect(searchSite + productCode).get()

        val onlyProduct = document.select("li.prod_item.prod_layer")
        val onlyProductString = onlyProduct.joinToString(" ") { it.text() }

        val productsName = onlyProduct.select("a[name='productName']").map { it.text() }.take(10)
        val productPowers = extractPowers(onlyProductString).take(10)

        val thumbimage = onlyProduct.select("div.thumb_image")
        val forimage = thumbimage.select("img")
        val imageUrls = forimage.mapNotNull { imgElement ->
            imgElement.attr("data-original").takeIf { it.isNotEmpty() }
                ?: imgElement.attr("src").takeIf { it.isNotEmpty() }
        }.map { it.substringBefore("?") }
            .distinct() // 중복된 URL 제거

        // 이미지 URL을 '//'로 시작하는 부분까지만 잘라내어 저장
        val productImgUrl = imageUrls.take(10)

        val productInfoList = List(minOf( productImgUrl.size, productsName.size, productPowers.size)) { index ->
            RecommendProductData(
                imageUrl = productImgUrl.getOrNull(index) ?: "Unknown",
                productName = productsName.getOrNull(index) ?: "Unknown",
                powerOfConsume = productPowers.getOrNull(index) ?: 0.0
            )
        }

        return@withContext productInfoList
    }
    fun extractPowers(text: String): List<Double> {
        val powers = mutableListOf<Double>()
        val regex = "\\d+(\\.\\d+)?(?=(W|kW|kWh\\(월\\)))".toRegex()

        val splitText = text.split("소비전력").drop(1)

        for (part in splitText) {
            val matchResult = regex.find(part)
            matchResult?.let {
                val power = it.value.toDouble()
                powers.add(power)
            }
        }

        return powers
    }

    fun extractPrices(text: String): List<Int> {
        val prices = mutableListOf<Int>()
        val regex = "\\d{1,3}(,\\d{3})*원".toRegex()

        val splitText = text.split("소비전력")

        for (part in splitText) {
            val matchResult = regex.find(part)
            matchResult?.let {
                val price = it.value.replace("[^\\d]".toRegex(), "").toInt()
                prices.add(price)
            }
        }

        return prices
    }

    // 기기 변경 전 총 소비전력량을 계산하고 시뮬레이션 결과에 표시
    @SuppressLint("SetTextI18n")
    private fun calculateCurrentTotalPowerOfConsumeForMonth() {
        totalPowerOfConsumeForMonth = 0.0

        for (relativeGradeData in HomeActivity.list) {
            if (relativeGradeData.powerOfConsume == null) {
                continue
            }

            when (relativeGradeData.deviceType) {
                DeviceTypeList.REFRIGERATOR,
                DeviceTypeList.WASHING_MACHINE -> {
                    totalPowerOfConsumeForMonth += relativeGradeData.powerOfConsume!!
                }
                DeviceTypeList.MICROWAVE_OVEN -> {
                    // 전자레인지의 경우 하루에 10분씩 사용한다고 가정
                    // 전자레인지에 대한 월간 소비 전력량 = 소비전력 * 1/6(시간) * 30(일)
                    // kWh로 환산하기 위해 맨 마지막에 1000으로 나눔
                    totalPowerOfConsumeForMonth += (relativeGradeData.powerOfConsume!! / 6 * 30) / 1000
                }
                DeviceTypeList.AIR_CONDITIONER -> {
                    // 에어컨의 경우 하루 평균 사용 시간을 통해 월간 소비 전력량을 계산
                    // 월간 소비 전력량 = 표준 월간 소비 전력량 * (하루 평균 사용 시간 / 7.8)
                    totalPowerOfConsumeForMonth +=
                        relativeGradeData.powerOfConsume!! * (relativeGradeData.averageUsageTimePerDay!! / 7.8F)
                }
                DeviceTypeList.TV -> {
                    // TV의 경우 하루 평균 사용 시간을 통해 월간 소비 전력량을 계산
                    // 월간 소비 전력량 = 소비전력 * 하루 평균 사용 시간 * 30(일)
                    // kWh로 환산하기 위해 맨 마지막에 1000으로 나눔
                    totalPowerOfConsumeForMonth +=
                        (relativeGradeData.powerOfConsume!! * relativeGradeData.averageUsageTimePerDay!! * 30) / 1000
                }
                DeviceTypeList.BOILER -> {
                    // 보일러의 경우 아직 시뮬레이션 기능을 제공하지 않음
                }
                else -> {

                }
            }
        }

        // 기기 변경 전 총 소비전력량 시뮬레이션 결과에 표시
        val _totalPowerOfConsumeForMonth =
            BigDecimal(totalPowerOfConsumeForMonth).setScale(2, RoundingMode.HALF_UP)
        binding.textTotalPowerOfConsumeForMonth.text = "$_totalPowerOfConsumeForMonth kWh/월"
    }

    // 기기 변경 후 총 소비전력량을 계산하고 시뮬레이션 결과에 표시
    @SuppressLint("SetTextI18n")
    private fun calculateAfterCurrentTotalPowerOfConsumeForMonth(
        position: Int, deviceType: DeviceTypeList?, afterPowerOfConsume: Int) {
        // 소비 전력 변화량
        if (HomeActivity.list[position].powerOfConsume == null) {
            return
        }

        val changeInPowerConsumption =
            afterPowerOfConsume - HomeActivity.list[position].powerOfConsume!!

        afterTotalPowerOfConsumeForMonth = 0.0

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
        val _afterTotalPowerOfConsumeForMonth =
            BigDecimal(afterTotalPowerOfConsumeForMonth).setScale(2, RoundingMode.HALF_UP)
        binding.textAfterTotalPowerOfConsumeForMonth.text = "$_afterTotalPowerOfConsumeForMonth kWh/월"

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
        val baseFee = if (totalPowerOfConsumeForMonth <= 200.0) {
            // 200kWh 이하 사용 시
            910
        } else if (totalPowerOfConsumeForMonth <= 400.0) {
            // 201 ~ 400kWh 사용 시
            1600
        } else {
            // 401kWh 이상 사용 시
            7300
        }

        // 전력량에 의한 전기 요금
        var electricalEnergy = 0.0

        if (totalPowerOfConsumeForMonth <= 200.0) {
            // 200kWh 이하 사용 시
            electricalEnergy += (105 * totalPowerOfConsumeForMonth)
        } else {
            electricalEnergy += (105 * 200)

            if (totalPowerOfConsumeForMonth <= 400.0) {
                // 201 ~ 400kWh 사용 시
                electricalEnergy += (174 * ((totalPowerOfConsumeForMonth - 200)))
            } else {
                electricalEnergy += (174 * 200)

                if (totalPowerOfConsumeForMonth <= 1000.0) {
                    // 401 ~ 1000kWh 사용 시
                    electricalEnergy += (242.3 * ((totalPowerOfConsumeForMonth) - 400))
                } else {
                    // 1001kWh 이상 사용 시
                    electricalEnergy += (242.3 * 600)
                    electricalEnergy += (601.3 * (totalPowerOfConsumeForMonth) - 1000)
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
        val baseFee = if (afterTotalPowerOfConsumeForMonth <= 200.0) {
            // 200kWh 이하 사용 시
            910
        } else if (afterTotalPowerOfConsumeForMonth <= 400.0) {
            // 201 ~ 400kWh 사용 시
            1600
        } else {
            // 401kWh 이상 사용 시
            7300
        }

        // 전력량에 의한 전기 요금
        var electricalEnergy = 0.0

        if (afterTotalPowerOfConsumeForMonth <= 200.0) {
            // 200kWh 이하 사용 시
            electricalEnergy += (105 * afterTotalPowerOfConsumeForMonth)
        } else {
            electricalEnergy += (105 * 200)

            if (afterTotalPowerOfConsumeForMonth <= 400.0) {
                // 201 ~ 400kWh 사용 시
                electricalEnergy += (174 * (afterTotalPowerOfConsumeForMonth - 200))
            } else {
                electricalEnergy += (174 * 200)

                if (afterTotalPowerOfConsumeForMonth <= 1000.0) {
                    // 401 ~ 1000kWh 사용 시
                    electricalEnergy += (242.3 * (afterTotalPowerOfConsumeForMonth - 400))
                } else {
                    // 1001kWh 이상 사용 시
                    electricalEnergy += (242.3 * 600)
                    electricalEnergy += (601.3 * (afterTotalPowerOfConsumeForMonth - 1000))
                }
            }
        }

        // 기후 환경 요금
        val climateEnvironmentFee = afterTotalPowerOfConsumeForMonth * 9

        // 연료비조정액
        val fuelCostAdjustmentFee = afterTotalPowerOfConsumeForMonth * 5

        return (baseFee + electricalEnergy + climateEnvironmentFee + fuelCostAdjustmentFee).toInt()
    }

    // 추천 제품 목록 가져오기
    @SuppressLint("NotifyDataSetChanged")
    private fun getProductRecommendationList(deviceType: DeviceTypeList?) {
        productRecommendationList.clear()

        val input = binding.textAfterPowerOfConsume.text.toString().toInt()

        when (deviceType) {
            DeviceTypeList.BOILER -> {
                for (recommendProductData in dummyData) {
                    if (recommendProductData.powerOfConsume != null) {
                        if ((recommendProductData.powerOfConsume - 0.5 <= input)
                            && (input <= recommendProductData.powerOfConsume + 0.5)) {
                            productRecommendationList.add(recommendProductData)
                        }
                    }
                }
            }
            else -> {}
        }

        try {
            Log.d("시뮬레이션", productRecommendationList.toString())
            recyclerViewProductRecommendationListAdapter?.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("시뮬레이션", e.toString())
        }
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

        val powerOfConsume = intent.getDoubleExtra("powerOfConsume", -1.0)
        val _powerOfConsume = BigDecimal(powerOfConsume).setScale(1, RoundingMode.HALF_UP)
        val relativeElectricPowerConsumePercentage = intent.getIntExtra("relativeElectricPowerConsumePercentage", -1)
        val position = intent.getIntExtra("position", -1)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        when (deviceType) {
            deviceType -> {
                supportActionBar?.title = "시뮬레이션 (${getTranslatedDeviceType(deviceType)})"
                binding.textPowerOfConsumeType.text = getPowerOfConsumeUnit(deviceType)["description"]
                binding.textPowerOfConsumeUnit.text = getPowerOfConsumeUnit(deviceType)["symbol"]
                binding.textPowerOfConsume.text = _powerOfConsume.toString()
                binding.textAfterPowerOfConsumeType.text = "기기 변경 후"
                binding.textAfterPowerOfConsumeUnit.text = getPowerOfConsumeUnit(deviceType)["symbol"]
                binding.textAfterPowerOfConsume.text = ""
            }
            else -> {
                supportActionBar?.title = "시뮬레이션"
            }
        }

        when (deviceType) {
            DeviceTypeList.WASHING_MACHINE,
            DeviceTypeList.BOILER -> {
                // 세탁기, 보일러에 대해서는 아직 시뮬레이션 기능을 제공하지 않음
                binding.textSimulationResults.visibility = View.GONE
                binding.textSimulationResultsDescription.visibility = View.GONE
                binding.relativeLayoutForTotalPowerConsumptionBeforeDeviceChange.visibility = View.GONE
                binding.relativeLayoutForTotalPowerConsumptionAfterDeviceChange.visibility = View.GONE
                binding.relativeLayoutForMonthlyElectricityBillChange.visibility = View.GONE
            }
            else -> {}
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

            if ((deviceType != DeviceTypeList.WASHING_MACHINE)
                && (deviceType != DeviceTypeList.BOILER)) {
                calculateAfterCurrentTotalPowerOfConsumeForMonth(position, deviceType, afterPowerOfConsume)
            }

            // 추천 제품 목록 가져오기
            getProductRecommendationList(deviceType)
        }

        recyclerView = binding.recyclerView

        if (deviceType != null) {
            recyclerViewProductRecommendationListAdapter =
                RecyclerViewProductRecommendationListAdapter(this, productRecommendationList, deviceType)
            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
            recyclerView!!.layoutManager = layoutManager
            recyclerView!!.adapter = recyclerViewProductRecommendationListAdapter
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish() // back 버튼을 누르면 이전 화면으로 돌아감
        }
        return true
    }
}