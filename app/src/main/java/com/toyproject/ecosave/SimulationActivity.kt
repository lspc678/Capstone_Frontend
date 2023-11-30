package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.toyproject.ecosave.databinding.ActivitySimulationBinding
import com.toyproject.ecosave.models.ComparableRecommendProductData
import com.toyproject.ecosave.models.DeviceTypeList
import com.toyproject.ecosave.models.RecommendProductData
import com.toyproject.ecosave.utilities.getPowerOfConsumeUnit
import com.toyproject.ecosave.utilities.getTranslatedDeviceType
import com.toyproject.ecosave.widget.SelectAverageUsageTimePerDayDialog

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.jsoup.Jsoup

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.PriorityQueue

interface SelectedAverageUsageTimePerDayInterface {
    fun onSelectedHour(hours: Int, minute: Int)
}

class SimulationActivity : AppCompatActivity(), SelectedAverageUsageTimePerDayInterface {
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

    private var productRecommendationList = mutableListOf<RecommendProductData>()

    private var position = -1
    private var deviceType: DeviceTypeList? = null
    private var afterPowerOfConsume: Double? = null

    // number picker에서 선택한 하루 평균 사용 시간
    private var selectedHours = 0
    private var selectedMinutes = 0

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
    private suspend fun crawl(productCode: String) : List<RecommendProductData> = withContext(Dispatchers.IO) {
        val searchSite = "https://prod.danawa.com/list/?cate="

        // tv = 1022811 (단위 W)
        // 전자레인지 = 10338815 (단위 W)
        // 냉장고 = 10251508 (단위 kWh(월))
        // 에어컨 = 1022644(단위 : kW)
        // 건조기 = 10244108 (단위 W/kg)
        val document = Jsoup.connect(searchSite + productCode).get()

        val numOfItems = 10

        val onlyProduct = document.select("li.prod_item.prod_layer")
        val onlyProductString = onlyProduct.joinToString(" ") { it.text() }

        val productsName = onlyProduct.select("a[name='productName']").map { it.text() }.take(numOfItems)
        val productPowers = extractPowers(onlyProductString).take(numOfItems)

        val thumbimage = onlyProduct.select("div.thumb_image")
        val forimage = thumbimage.select("img")
        val imageUrls = forimage.mapNotNull { imgElement ->
            imgElement.attr("data-original").takeIf { it.isNotEmpty() }
                ?: imgElement.attr("src").takeIf { it.isNotEmpty() }
        }.map { it.substringBefore("?") }
            .distinct() // 중복된 URL 제거

        // 이미지 URL을 '//'로 시작하는 부분까지만 잘라내어 저장
        val productImgUrl = imageUrls.take(numOfItems)

        val productInfoList = List(minOf( productImgUrl.size, productsName.size, productPowers.size)) { index ->
            RecommendProductData(
                imageUrl = productImgUrl.getOrNull(index) ?: "Unknown",
                productName = productsName.getOrNull(index) ?: "Unknown",
                powerOfConsume = productPowers.getOrNull(index) ?: 0.0
            )
        }

        return@withContext productInfoList
    }

    private fun extractPowers(text: String) : List<Double> {
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

//    fun extractPrices(text: String): List<Int> {
//        val prices = mutableListOf<Int>()
//        val regex = "\\d{1,3}(,\\d{3})*원".toRegex()
//
//        val splitText = text.split("소비전력")
//
//        for (part in splitText) {
//            val matchResult = regex.find(part)
//            matchResult?.let {
//                val price = it.value.replace("[^\\d]".toRegex(), "").toInt()
//                prices.add(price)
//            }
//        }
//
//        return prices
//    }

    private fun calculateTotalPowerOfConsumeForMonth() {
        calculateCurrentTotalPowerOfConsumeForMonth()
        calculateAfterCurrentTotalPowerOfConsumeForMonth()
    }

    // 기기 변경 전 총 소비전력량을 계산하고 시뮬레이션 결과에 표시
    @SuppressLint("SetTextI18n")
    private fun calculateCurrentTotalPowerOfConsumeForMonth() {
        totalPowerOfConsumeForMonth = 0.0

        for (relativeGradeData in HomeActivity.list) {
            if (relativeGradeData.powerOfConsume == null) {
                continue
            }

            val usageTimeFor1Day = selectedHours + selectedMinutes / 60.0
            Log.d("시뮬레이션", usageTimeFor1Day.toString())

            when (relativeGradeData.deviceType) {
                DeviceTypeList.REFRIGERATOR,
                DeviceTypeList.WASHING_MACHINE -> {
                    totalPowerOfConsumeForMonth += relativeGradeData.powerOfConsume!!
                }
                DeviceTypeList.MICROWAVE_OVEN -> {
                    // 전자레인지에 대한 월간 소비 전력량 = 소비전력 * (하루 평균 사용 시간) * 30(일)
                    // kWh로 환산하기 위해 맨 마지막에 1000으로 나눔
                    totalPowerOfConsumeForMonth +=
                        (relativeGradeData.powerOfConsume!! * usageTimeFor1Day * 30) / 1000
                }
                DeviceTypeList.AIR_CONDITIONER -> {
                    // 에어컨의 경우 하루 평균 사용 시간을 통해 월간 소비 전력량을 계산
                    // 월간 소비 전력량 = 표준 월간 소비 전력량 * (하루 평균 사용 시간 / 7.8)
                    totalPowerOfConsumeForMonth +=
                        relativeGradeData.powerOfConsume!! * (usageTimeFor1Day / 7.8F)
                }
                DeviceTypeList.TV -> {
                    // TV의 경우 하루 평균 사용 시간을 통해 월간 소비 전력량을 계산
                    // 월간 소비 전력량 = 소비전력 * 하루 평균 사용 시간 * 30(일)
                    // kWh로 환산하기 위해 맨 마지막에 1000으로 나눔
                    totalPowerOfConsumeForMonth +=
                        (relativeGradeData.powerOfConsume!! * usageTimeFor1Day * 30) / 1000
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
    // 기기 변경 후 소비전력과 하루 평균 사용 시간이 모두 채워졌을 경우 시뮬레이션 결과를 보여줌
    @SuppressLint("SetTextI18n")
    fun calculateAfterCurrentTotalPowerOfConsumeForMonth() {
        // 소비 전력 변화량
        if (HomeActivity.list[position].powerOfConsume == null) {
            return
        }

        if (afterPowerOfConsume == null) {
            return
        }

        val changeInPowerConsumption =
            afterPowerOfConsume!! - HomeActivity.list[position].powerOfConsume!!

        afterTotalPowerOfConsumeForMonth = 0.0

        val usageTimeFor1Day = selectedHours + selectedMinutes / 60.0

        // 기기 변경 후 등록된 모든 기기의 총 소비 전력량 (월간) 계산
        when (deviceType) {
            DeviceTypeList.REFRIGERATOR -> {
                afterTotalPowerOfConsumeForMonth += changeInPowerConsumption
            }
            DeviceTypeList.MICROWAVE_OVEN -> {
                // 전자레인지에 대한 월간 소비 전력량 = 소비전력 * 1/6(시간) * 30(일)
                // kWh로 환산하기 위해 맨 마지막에 1000으로 나눔
                afterTotalPowerOfConsumeForMonth +=
                    (changeInPowerConsumption * usageTimeFor1Day * 30) / 1000
            }
            DeviceTypeList.AIR_CONDITIONER -> {
                // 에어컨의 경우 하루 평균 사용 시간을 통해 월간 소비 전력량을 계산
                // 월간 소비 전력량 = 표준 월간 소비 전력량 * (하루 평균 사용 시간 / 7.8)
                afterTotalPowerOfConsumeForMonth +=
                    changeInPowerConsumption * (usageTimeFor1Day / 7.8F)
            }
            DeviceTypeList.TV -> {
                // TV의 경우 하루 평균 사용 시간을 통해 월간 소비 전력량을 계산
                // 월간 소비 전력량 = 소비전력 * 하루 평균 사용 시간 * 30(일)
                // kWh로 환산하기 위해 맨 마지막에 1000으로 나눔
                afterTotalPowerOfConsumeForMonth +=
                    (changeInPowerConsumption * usageTimeFor1Day * 30) / 1000
            }
            DeviceTypeList.WASHING_MACHINE,
            DeviceTypeList.BOILER -> {
                // 세탁기, 보일러의 경우 아직 시뮬레이션 기능을 제공하지 않음
            }
            else -> {

            }
        }

        afterTotalPowerOfConsumeForMonth += totalPowerOfConsumeForMonth

        calculateMonthlyElectricityBillChange()
    }

    // 한 달 전기 요금 변화량 계산
    @SuppressLint("ResourceAsColor", "SetTextI18n")
    private fun calculateMonthlyElectricityBillChange() {
        // 기기 변경 후 총 소비전력량 시뮬레이션 결과에 표시
        val _afterTotalPowerOfConsumeForMonth =
            BigDecimal(afterTotalPowerOfConsumeForMonth).setScale(2, RoundingMode.HALF_UP)
        binding.textAfterTotalPowerOfConsumeForMonth.text = "$_afterTotalPowerOfConsumeForMonth kWh/월"

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
    private suspend fun getProductRecommendationList(deviceType: DeviceTypeList?) {
        productRecommendationList.clear()

        // 소비 전력이 낮은 기기부터 순차적으로 정렬 (단, 보일러 제외)
        // 보일러의 경우 반대로 난방열효율이 높은 기기부터 순차적으로 정렬
        // tv = 1022811 (단위 W), 전자레인지 = 10338815 (단위 W),
        // 냉장고 = 10251508 (단위 kWh(월)), 에어컨 = 1022644(단위 : kW)
        when (deviceType) {
            DeviceTypeList.REFRIGERATOR -> {
                val recommendProductDataList = crawl("10251508")
                val pq = PriorityQueue<ComparableRecommendProductData>()

                for (data in recommendProductDataList) {
                    pq.add(ComparableRecommendProductData(data))
                }

                while (pq.isNotEmpty()) {
                    pq.poll()?.let { productRecommendationList.add(it.recommendProductData) }
                }
            }
            DeviceTypeList.AIR_CONDITIONER -> {
                val recommendProductDataList = crawl("1022644")
                val pq = PriorityQueue<ComparableRecommendProductData>()

                for (data in recommendProductDataList) {
                    pq.add(ComparableRecommendProductData(data))
                }

                while (pq.isNotEmpty()) {
                    pq.poll()?.let { productRecommendationList.add(it.recommendProductData) }
                }
            }
            DeviceTypeList.TV -> {
                val recommendProductDataList = crawl("1022811")
                val pq = PriorityQueue<ComparableRecommendProductData>()

                for (data in recommendProductDataList) {
                    pq.add(ComparableRecommendProductData(data))
                }

                while (pq.isNotEmpty()) {
                    pq.poll()?.let { productRecommendationList.add(it.recommendProductData) }
                }
            }
            DeviceTypeList.MICROWAVE_OVEN -> {
                val recommendProductDataList = crawl("10338815")
                val pq = PriorityQueue<ComparableRecommendProductData>()

                for (data in recommendProductDataList) {
                    pq.add(ComparableRecommendProductData(data))
                }

                while (pq.isNotEmpty()) {
                    pq.poll()?.let { productRecommendationList.add(it.recommendProductData) }
                }
            }
            DeviceTypeList.BOILER -> {
                for (recommendProductData in dummyData) {
                    if (recommendProductData.powerOfConsume != null) {
                        productRecommendationList.add(recommendProductData)
                    }
                }
            }
            DeviceTypeList.DRYER -> {
                val recommendProductDataList = crawl("10244108")
                val pq = PriorityQueue<ComparableRecommendProductData>()

                for (data in recommendProductDataList) {
                    pq.add(ComparableRecommendProductData(data))
                }

                while (pq.isNotEmpty()) {
                    pq.poll()?.let { productRecommendationList.add(it.recommendProductData) }
                }
            }
            else -> {}
        }

        try {
            runOnUiThread {
                // progress bar 제거
                binding.constraintLayoutForProgressBar.visibility = View.GONE
                recyclerViewProductRecommendationListAdapter?.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("시뮬레이션", e.toString())
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSelectedHour(hours: Int, minute: Int) {
        if ((hours == 0) && (minute == 0)) {
            Toast.makeText(this, "하루 평균 사용 시간의 최솟값은 30분 입니다", Toast.LENGTH_SHORT).show()
            return
        }

        selectedHours = hours
        when (minute) {
            0 -> {
                binding.textUsageTimeFor1Day.text = hours.toString()
                selectedMinutes = 0
            }
            30 -> {
                binding.textUsageTimeFor1Day.text = "$hours.5"
                selectedMinutes = 30
            }
        }

        // 기기 변경 전 총 소비전력량을 계산하고 시뮬레이션 결과에 표시
        calculateTotalPowerOfConsumeForMonth()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimulationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("deviceType", DeviceTypeList::class.java)
        } else {
            intent.getSerializableExtra("deviceType") as DeviceTypeList
        }

        val powerOfConsume = intent.getDoubleExtra("powerOfConsume", -1.0)
        val _powerOfConsume = BigDecimal(powerOfConsume).setScale(1, RoundingMode.HALF_UP)
        position = intent.getIntExtra("position", -1)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        when (deviceType) {
            deviceType -> {
                supportActionBar?.title = "시뮬레이션 (${getTranslatedDeviceType(deviceType)})"
                binding.textPowerOfConsumeType.text = getPowerOfConsumeUnit(deviceType)["description"]
                binding.textPowerOfConsumeUnit.text = getPowerOfConsumeUnit(deviceType)["symbol"]
                binding.textPowerOfConsume.text = _powerOfConsume.toString()
                binding.textAfterPowerOfConsumeUnit.text = getPowerOfConsumeUnit(deviceType)["symbol"]
                binding.textAfterPowerOfConsume.text = ""
            }
            else -> {
                supportActionBar?.title = "시뮬레이션"
            }
        }

        if (deviceType == DeviceTypeList.REFRIGERATOR) {
            binding.textUsageTimeFor1Day.text = "24"
        } else {
            binding.textUsageTimeFor1Day.text = ""
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

        // 하루 평균 사용 시간 클릭 시
        binding.relativeLayoutForEditableField.setOnClickListener {
            try {
                if (deviceType == DeviceTypeList.REFRIGERATOR) {
                    Toast.makeText(
                        this,
                        "냉장고는 하루 평균 사용 시간 수정이 불가능합니다",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    SelectAverageUsageTimePerDayDialog(selectedHours, selectedMinutes).show(
                        supportFragmentManager, "SelectAverageUsageTimePerDayDialog")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("시뮬레이션", e.toString())
            }
        }

        // progress bar 표시
        binding.constraintLayoutForProgressBar.visibility = View.VISIBLE

        recyclerView = binding.recyclerView

        if (deviceType != null) {
            recyclerViewProductRecommendationListAdapter =
                RecyclerViewProductRecommendationListAdapter(
                    this,
                    productRecommendationList,
                    deviceType!!,
                    resources
                )
            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
            recyclerView!!.layoutManager = layoutManager
            recyclerView!!.isFocusable = false
            recyclerView!!.isNestedScrollingEnabled = false
            recyclerView!!.adapter = recyclerViewProductRecommendationListAdapter

            recyclerViewProductRecommendationListAdapter!!.setOnItemClickListener(
                object : RecyclerViewProductRecommendationListAdapter.OnItemClickListener {
                    override fun onItemClick(v: View, data: RecommendProductData, pos: Int) {
                        afterPowerOfConsume = data.powerOfConsume
                        if (afterPowerOfConsume != null) {
                            binding.textAfterPowerOfConsume.text = afterPowerOfConsume.toString()
                        }
                        // 기기 변경 전 총 소비전력량을 계산하고 시뮬레이션 결과에 표시
                        calculateTotalPowerOfConsumeForMonth()
                    }
                }
            )
        }

        // 추천 제품 목록 가져오기
        CoroutineScope(Dispatchers.IO).launch {
            getProductRecommendationList(deviceType)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish() // back 버튼을 누르면 이전 화면으로 돌아감
        }
        return true
    }
}