package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import com.toyproject.ecosave.api.APIClientForServerByPassSSLCertificate
import com.toyproject.ecosave.api.APIInterface
import com.toyproject.ecosave.api.requestmodels.AppliancePostRequest
import com.toyproject.ecosave.api.requestmodels.BoilerPostRequest
import com.toyproject.ecosave.api.responsemodels.DefaultResponse
import com.toyproject.ecosave.databinding.ActivityAddDeviceBinding
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

class AddDeviceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddDeviceBinding

    private var energyConsumption = 0.0F
    private var amountOfCO2 = 0.0F

    private val MARGIN_SIDE = 20.0F
    private val MARGIN_BETWEEN_CARDVIEWS = 30.0F

    companion object {
        private const val GET_ENERGY_CONSUMPTION_AND_CO2 = 50
    }

    private fun reset() {
        binding.textPowerOfConsume.text = "0"
        binding.textCO2Emission.text = "0"
        energyConsumption = 0.0F
        amountOfCO2 = 0.0F
    }

    private fun setCardView() {
        // cardViewForEnergyConsume 크기 조절
        val layoutParamsForEnergyConsume = binding.cardViewForEnergyConsume.layoutParams

        val screenWidth = App.getWidth(this)
        val marginSidePx = fromDpToPx(resources, MARGIN_SIDE)
        val marginBetweenCardViewPx = fromDpToPx(resources, MARGIN_BETWEEN_CARDVIEWS)
        val cardViewWidth = (screenWidth - marginSidePx * 2 - marginBetweenCardViewPx) / 2

        layoutParamsForEnergyConsume.width = cardViewWidth
        layoutParamsForEnergyConsume.height = cardViewWidth

        // cardViewForCO2 크기 조절
        val layoutParamsForCO2 = binding.cardViewForCO2.layoutParams

        layoutParamsForCO2.width = cardViewWidth
        layoutParamsForCO2.height = cardViewWidth
    }

    // 기기등록
    private fun callAppliancePost(deviceType: DeviceTypeList) {
        // progress bar 불러오기
        val progressDialog = ProgressDialog.getProgressDialog(this, "처리 중 입니다")
        progressDialog.show()

        val type = getTranslatedDeviceType(deviceType)
        val apiInterface = APIClientForServerByPassSSLCertificate
            .getClient()
            .create(APIInterface::class.java)

        val call: Call<DefaultResponse>?

        when (deviceType) {
            DeviceTypeList.REFRIGERATOR -> {
                call = apiInterface.applianceRefrigeratorPost(
                    AppliancePostRequest(
                        energyConsumption.toDouble(),
                        amountOfCO2.toDouble(),
                        "none"
                    )
                )
            }
            DeviceTypeList.AIR_CONDITIONER -> {
                call = apiInterface.applianceAirConditionerPost(
                    AppliancePostRequest(
                        energyConsumption.toDouble(),
                        amountOfCO2.toDouble(),
                        "none"
                    )
                )
            }
            DeviceTypeList.TV -> {
                call = apiInterface.applianceTelevisionPost(
                    AppliancePostRequest(
                        energyConsumption.toDouble(),
                        amountOfCO2.toDouble(),
                        "none"
                    )
                )
            }
            DeviceTypeList.WASHING_MACHINE -> {
                call = apiInterface.applianceWashingMachinePost(
                    AppliancePostRequest(
                        energyConsumption.toDouble(),
                        amountOfCO2.toDouble(),
                        "none"
                    )
                )
            }
            DeviceTypeList.MICROWAVE_OVEN -> {
                call = apiInterface.applianceMicrowavePost(
                    AppliancePostRequest(
                        energyConsumption.toDouble(),
                        amountOfCO2.toDouble(),
                        "none"
                    )
                )
            }
            DeviceTypeList.BOILER -> {
                call = apiInterface.applianceBoilerPost(
                    BoilerPostRequest(
                        energyConsumption,
                        24.0F,
                        24.0F,
                        "none"
                    )
                )
            }
            DeviceTypeList.DRYER -> {
                call = apiInterface.applianceDryerPost(
                    AppliancePostRequest(
                        energyConsumption.toDouble(),
                        amountOfCO2.toDouble(),
                        "none"
                    )
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
                                Log.d("기기등록 ($type)", "결과: 성공")
                                Log.d("기기등록 ($type)", result.toString())

                                val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                                    reset()
                                    val intent = Intent(this@AddDeviceActivity, HomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }

                                val onKeyListener = DialogInterface.OnKeyListener { _, keyCode, event ->
                                    if ((keyCode == KeyEvent.KEYCODE_BACK)
                                        && (event.action == KeyEvent.ACTION_UP)) {
                                        reset()
                                        val intent = Intent(this@AddDeviceActivity, HomeActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    return@OnKeyListener false
                                }

                                AlertDialog.Builder(this@AddDeviceActivity)
                                    .setTitle("기기 추가")
                                    .setMessage("기기가 등록되었습니다.")
                                    .setPositiveButton("확인", positiveButtonOnClickListener)
                                    .setOnKeyListener(onKeyListener)
                                    .setCancelable(false)
                                    .create()
                                    .show()
                            } else {
                                simpleDialog(
                                    this@AddDeviceActivity,
                                    "기기 추가",
                                    "기기 등록에 실패했습니다. 다시 시도해주세요."
                                )

                                Log.d("기기등록 ($type)", "결과: 실패")
                                Log.d("기기등록 ($type)", result.toString())
                            }
                        }
                    } else {
                        // status code가 200 ~ 299가 아닐 때
                        val errorResult = response.errorBody()
                        val result = response.body()

                        if (errorResult != null) {
                            simpleDialog(
                                this@AddDeviceActivity,
                                "기기 추가",
                                "기기 등록에 실패했습니다. 다시 시도해주세요."
                            )

                            Log.d("기기등록 ($type)", "결과: 실패 (response.isSuccessful 통과하지 못함)")
                            Log.d("기기등록 ($type)", "statusCode: ${response.code()}")
                            Log.d("기기등록 ($type)", errorResult.string())
                            Log.d("기기등록 ($type)", result.toString())
                        }
                    }
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    // progress bar 종료
                    progressDialog.dismiss()

                    simpleDialog(
                        this@AddDeviceActivity,
                        "기기 추가",
                        "서버와의 통신이 원활하지 않습니다. 잠시 후 다시 시도해주세요."
                    )

                    Log.d("기기등록 ($type)", "결과: 실패 (onFailure)")
                    Log.d("기기등록 ($type)", t.message.toString())
                }
            }
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GET_ENERGY_CONSUMPTION_AND_CO2) {
            if (data != null) {
                Log.d("기기추가", data.getFloatExtra("energyConsumption", 0.0F).toString())
                Log.d("기기추가", data.getFloatExtra("amountOfCO2", 0.0F).toString())

                energyConsumption = data.getFloatExtra("energyConsumption", 0.0F)
                if (energyConsumption > 0.0F) {
                    binding.textPowerOfConsume.text = energyConsumption.toString()
                }

                amountOfCO2 = data.getFloatExtra("amountOfCO2", 0.0F)
                if (amountOfCO2 > 0.0F) {
                    binding.textCO2Emission.text = amountOfCO2.toString()
                }
            } else {
                Log.d("기기추가", "null")
            }
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "기기 추가"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 앱바에 back 버튼 활성화

        // 사진 촬영 버튼
        binding.btnTakePicture.setOnClickListener {
            val intent = Intent(this, LivePreviewActivity::class.java)
            intent.putExtra("selectedItemPosition", binding.spinner.selectedItemPosition)
            startActivityForResult(intent, GET_ENERGY_CONSUMPTION_AND_CO2)
        }

        val items = resources.getStringArray(R.array.category_list)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        // 2개의 CardView에 대한 크기 조절
        setCardView()

        binding.spinner.adapter = spinnerAdapter
        binding.spinner.prompt = "카테고리 선택"
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("SetTextI18n")
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long) {
                lateinit var powerOfConsumeUnit: Map<String, String>
                lateinit var co2EmissionUnit: String
                when (position) {
                    0 -> { // 냉장고
                        powerOfConsumeUnit = getPowerOfConsumeUnit(DeviceTypeList.REFRIGERATOR)
                        co2EmissionUnit = getCO2EmissionUnit(DeviceTypeList.REFRIGERATOR)
                    }
                    1 -> { // 에어컨
                        powerOfConsumeUnit = getPowerOfConsumeUnit(DeviceTypeList.AIR_CONDITIONER)
                        co2EmissionUnit = getCO2EmissionUnit(DeviceTypeList.AIR_CONDITIONER)
                    }
                    2 -> { // TV
                        powerOfConsumeUnit = getPowerOfConsumeUnit(DeviceTypeList.TV)
                        co2EmissionUnit = getCO2EmissionUnit(DeviceTypeList.TV)
                    }
                    3 -> { // 세탁기
                        powerOfConsumeUnit = getPowerOfConsumeUnit(DeviceTypeList.WASHING_MACHINE)
                        co2EmissionUnit = getCO2EmissionUnit(DeviceTypeList.WASHING_MACHINE)
                    }
                    4 -> { // 전자레인지
                        powerOfConsumeUnit = getPowerOfConsumeUnit(DeviceTypeList.MICROWAVE_OVEN)
                        co2EmissionUnit = getCO2EmissionUnit(DeviceTypeList.MICROWAVE_OVEN)
                    }
                    5 -> { // 보일러
                        powerOfConsumeUnit = getPowerOfConsumeUnit(DeviceTypeList.BOILER)
                        co2EmissionUnit = getCO2EmissionUnit(DeviceTypeList.BOILER)
                    }
                    6 -> { // 건조기
                        powerOfConsumeUnit = getPowerOfConsumeUnit(DeviceTypeList.DRYER)
                        co2EmissionUnit = getCO2EmissionUnit(DeviceTypeList.DRYER)
                    }
                    else -> {
                        powerOfConsumeUnit = getPowerOfConsumeUnit(DeviceTypeList.OTHERS)
                        co2EmissionUnit = getCO2EmissionUnit(DeviceTypeList.OTHERS)
                    }
                }
                binding.textPowerOfConsumeUnit.text = powerOfConsumeUnit["symbol"]
                binding.textPowerOfConsumeType.text = powerOfConsumeUnit["description"]
                binding.textCO2EmissionUnit.text = co2EmissionUnit

                reset()

                if (co2EmissionUnit == "") {
                    binding.cardViewForCO2.visibility = View.GONE
                } else {
                    binding.cardViewForCO2.visibility = View.VISIBLE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 기기 추가 버튼 클릭 시
        binding.btnAddDevice.setOnClickListener {
            when (binding.spinner.selectedItemPosition) {
                0, 1, 2, 3, 6 -> { // 냉장고, 에어컨, TV, 세탁기, 건조기
                    if ((energyConsumption == 0.0F)
                        || (amountOfCO2 == 0.0F)) {
                        simpleDialog(
                            this@AddDeviceActivity,
                            "기기 추가",
                            "사진 촬영이 진행되지 않았습니다."
                        )
                        return@setOnClickListener
                    }
                }
                4 -> { // 전자레인지
                    if (energyConsumption == 0.0F) {
                        simpleDialog(
                            this@AddDeviceActivity,
                            "기기 추가",
                            "사진 촬영이 진행되지 않았습니다."
                        )
                        return@setOnClickListener
                    }
                }
                5 -> { // 보일러
                    if (energyConsumption == 0.0F) {
                        simpleDialog(
                            this@AddDeviceActivity,
                            "기기 추가",
                            "사진 촬영이 진행되지 않았습니다."
                        )
                        return@setOnClickListener
                    }
                }
            }

            val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                when (binding.spinner.selectedItemPosition) {
                    0 -> { // 냉장고
                        callAppliancePost(DeviceTypeList.REFRIGERATOR)
                    }
                    1 -> { // 에어컨
                        callAppliancePost(DeviceTypeList.AIR_CONDITIONER)
                    }
                    2 -> { // TV
                        callAppliancePost(DeviceTypeList.TV)
                    }
                    3 -> { // 세탁기
                        callAppliancePost(DeviceTypeList.WASHING_MACHINE)
                    }
                    4 -> { // 전자레인지
                        callAppliancePost(DeviceTypeList.MICROWAVE_OVEN)
                    }
                    5 -> { // 보일러
                        callAppliancePost(DeviceTypeList.BOILER)
                    }
                    6 -> { // 건조기
                        callAppliancePost(DeviceTypeList.DRYER)
                    }
                    else -> {}
                }
            }

            createDialog(
                this@AddDeviceActivity,
                "기기 등록",
                "기기 등록을 하시겠습니까?",
                positiveButtonOnClickListener,
                defaultNegativeDialogInterfaceOnClickListener
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish() // back 버튼을 누르면 이전 화면으로 돌아감
        }
        return true
    }
}