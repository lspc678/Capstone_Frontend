package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.google.android.material.navigation.NavigationView
import com.toyproject.ecosave.api.APIClientForNaverMap
import com.toyproject.ecosave.api.APIClientForServerByPassSSLCertificate
import com.toyproject.ecosave.api.APIInterface
import com.toyproject.ecosave.api.requestmodels.ChangeMyResidenceRequest
import com.toyproject.ecosave.api.responsemodels.ApplianceDetailResponse
import com.toyproject.ecosave.api.responsemodels.BoilerDetailResponse
import com.toyproject.ecosave.api.responsemodels.DefaultResponse
import com.toyproject.ecosave.api.responsemodels.MainTotalInformationResponse
import com.toyproject.ecosave.api.responsemodels.ReverseGeocodingResponse
import com.toyproject.ecosave.databinding.ActivityHomeBinding
import com.toyproject.ecosave.models.DeviceTypeList
import com.toyproject.ecosave.models.RegisteredDeviceData
import com.toyproject.ecosave.utilities.GPSLocation
import com.toyproject.ecosave.utilities.fromDpToPx
import com.toyproject.ecosave.utilities.getTranslatedDeviceType
import com.toyproject.ecosave.widget.ProgressDialog
import com.toyproject.ecosave.widget.createDialog
import com.toyproject.ecosave.widget.defaultNegativeDialogInterfaceOnClickListener
import com.toyproject.ecosave.widget.simpleDialog

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// 테스트용
import com.toyproject.ecosave.test.TestMainActivity

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var recyclerView: RecyclerView? = null
    private var recyclerViewRegisteredDeviceListAdapter: RecyclerViewRegisteredDeviceListAdapter? = null

    // 상대적 에너지 소비 효율 등급을 나타낼 수 없을 때 화면에 출력할 메세지
    private var textNoRelativeGradeData = ""

    // 내 거주지
    private var myLocation = ""

    // 내 거주지 설정 방법
    private val optionsForChangeMyResidence = arrayOf(
        "현재 위치를 거주지로 설정",
        "지도에서 검색"
    )

    private val MARGIN_SIDE = 20.0F
    private val MARGIN_BETWEEN_PYRAMIDS = 30.0F

    private val MARGIN_TEXT = arrayOf(
        arrayOf(87.0F, 6.0F),
        arrayOf(94.0F, 31.0F),
        arrayOf(94.0F, 31.0F),
        arrayOf(102.0F, 52.0F),
        arrayOf(102.0F, 52.0F),
        arrayOf(110.0F, 72.0F),
        arrayOf(110.0F, 72.0F),
        arrayOf(114.0F, 92.0F),
        arrayOf(114.0F, 92.0F)
    )

    companion object {
        // 등록된 기기 목록
        var list: MutableList<RegisteredDeviceData> = mutableListOf()

        // 등록된 기기의 개수
        private var numOfRegisteredDevices = 0

        // 상세 정보를 알아낸 기기의 개수
        private var numOfGetDetailedInformationDevices = 0

        // CO2 배출량에 대한 상대적 에너지 소비 효율 백분위(%)의 합
        private var sumOfRelativeCO2EmissionPercentage = 0.0

        // CO2 배출량에 대한 종합 상대적 에너지 소비 효율 등급
        private var totalRelativeCO2EmissionGrade = -1

        // 소비전력에 대한 상대적 에너지 소비 효율 백분위(%)의 합
        private var sumOfRelativeEnergyConsumePercentage = 0.0

        // 소비전력에 대한 상대적 에너지 소비 효율 등급
        var totalRelativeEnergyConsumeGrade = -1
    }

    // 서버로 부터 내 거주지를 불러옴
    private fun getMyLocationFromServer() {
        if (myLocation == "") {
            binding.textMyResidentInfo.text = "등록된 정보가 없습니다."
            textNoRelativeGradeData = "내 거주지 설정 이후 상대적 에너지 소비 효율 등급을 확인할 수 있습니다."
            binding.textNoRelativeGradeData.visibility = View.VISIBLE
            binding.constraintLayoutForPyramid.visibility = View.GONE
        } else {
            binding.textMyResidentInfo.text = myLocation
        }
    }

    private fun setMyCurrentLocationAsMyResidence() {
        // 현재 위치를 거주지로 설정
        val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
            val gpsLocation = GPSLocation(this@HomeActivity, this)

            if (!gpsLocation.getLocation()) {
                return@OnClickListener
            }

            // progress bar 불러오기
            val progressDialog = ProgressDialog.getProgressDialog(this, "현재 위치를 불러오고 있습니다")
            progressDialog.show()

            val job = CoroutineScope(Dispatchers.IO).launch {
                while ((GPSLocation.currentLatitude == 0.0)
                    || (GPSLocation.currentLongitude == 0.0)) {
                    delay(10L)
                }

                progressDialog.dismiss()

                if ((GPSLocation.currentLatitude != 0.0) &&
                    (GPSLocation.currentLongitude != 0.0)) {
                    searchAddress(GPSLocation.currentLatitude, GPSLocation.currentLongitude)
                }
            }

            // progress bar 표시 도중 취소 버튼을 눌렀을 때
            progressDialog.setOnKeyListener { _, keyCode, event ->
                if ((keyCode == KeyEvent.KEYCODE_BACK)
                    && (event.action == KeyEvent.ACTION_UP)) {
                    job.cancel()
                    simpleDialog(
                        this,
                        "내 거주지 변경",
                        "현재 위치 정보를 가져오지 못했습니다. 잠시 후 다시 시도해주세요."
                    )
                }
                return@setOnKeyListener false
            }
        }

        createDialog(
            this,
            "내 거주지 변경",
            "현재 위치를 기준으로 거주지를 변경합니다.\n\n"
                    + "주의: 이전에 저장된 거주지 정보는 사라집니다.",
            positiveButtonOnClickListener,
            defaultNegativeDialogInterfaceOnClickListener
        )
    }

    private fun prepareListData() {
        // list 목록 초기화
        list.clear()

        // progress bar 불러오기
        val progressDialog = ProgressDialog.getProgressDialog(this, "등록된 기기를 불러오고 있습니다")
        progressDialog.show()

        // 서버에서 상대적 에너지 소비 효율 등급에 대한 정보를 가져옴
        val apiInterface = APIClientForServerByPassSSLCertificate
            .getClient()
            .create(APIInterface::class.java)
        val callMainTotalInformation = apiInterface.mainTotalInformation()

        Log.d("홈 화면", App.prefs.token!!)

        callMainTotalInformation.enqueue(
            object : Callback<MainTotalInformationResponse> {
                override fun onResponse(
                    call: Call<MainTotalInformationResponse>,
                    response: Response<MainTotalInformationResponse>
                ) {
                    // progress bar 종료
                    progressDialog.dismiss()

                    if (response.isSuccessful) {
                        val result = response.body()

                        if (result != null) {
                            if (result.success) {
                                // 서버로부터 등록된 기기 목록에 관한 정보를 가져옴
                                val data = result.data

                                Log.d("홈 화면", data.toString())

                                // 등록된 냉장고의 pkey와 소비전력량 가져오기
                                if (data.refrigerator != null) {
                                    for (refrigeratorData in data.refrigerator) {
                                        val pkey = refrigeratorData.id
                                        val energy = refrigeratorData.energy
                                        list.add(
                                            RegisteredDeviceData(
                                                pkey,
                                                DeviceTypeList.REFRIGERATOR,
                                                "",
                                                null, null,
                                                energy,
                                                null, null, null, 24.0
                                            )
                                        )
                                        recyclerViewRegisteredDeviceListAdapter?.notifyItemInserted(list.size - 1)
                                    }
                                }

                                // 등록된 에어컨의 pkey와 소비전력량 가져오기
                                if (data.air_conditioner != null) {
                                    for (airConditionerData in data.air_conditioner) {
                                        val pkey = airConditionerData.id
                                        val energy = airConditionerData.energy
                                        list.add(
                                            RegisteredDeviceData(
                                                pkey,
                                                DeviceTypeList.AIR_CONDITIONER,
                                                "",
                                                null, null,
                                                energy,
                                                null, null, null,
                                                1.0
                                            )
                                        )
                                        recyclerViewRegisteredDeviceListAdapter?.notifyItemInserted(list.size - 1)
                                    }
                                }

                                // 등록된 TV의 pkey와 소비전력량 가져오기
                                if (data.television != null) {
                                    for (tvData in data.television) {
                                        val pkey = tvData.id
                                        val energy = tvData.energy
                                        list.add(
                                            RegisteredDeviceData(
                                                pkey,
                                                DeviceTypeList.TV,
                                                "",
                                                null, null,
                                                energy,
                                                null, null, null,
                                                1.0
                                            )
                                        )
                                        recyclerViewRegisteredDeviceListAdapter?.notifyItemInserted(list.size - 1)
                                    }
                                }

                                // 등록된 세탁기의 pkey와 소비전력량 가져오기
                                if (data.washing_machine != null) {
                                    for (washingMachineData in data.washing_machine) {
                                        val pkey = washingMachineData.id
                                        val energy = washingMachineData.energy
                                        list.add(
                                            RegisteredDeviceData(
                                                pkey,
                                                DeviceTypeList.WASHING_MACHINE,
                                                "",
                                                null, null,
                                                energy,
                                                null, null, null, 1.0
                                            )
                                        )
                                        recyclerViewRegisteredDeviceListAdapter?.notifyItemInserted(list.size - 1)
                                    }
                                }

                                // 등록된 전자레인지의 pkey와 소비전력량 가져오기
                                if (data.microwave != null) {
                                    for (microwaveOvenData in data.microwave) {
                                        val pkey = microwaveOvenData.id
                                        val energy = microwaveOvenData.energy
                                        list.add(
                                            RegisteredDeviceData(
                                                pkey,
                                                DeviceTypeList.MICROWAVE_OVEN,
                                                "",
                                                null, null,
                                                energy,
                                                null, null, null, 0.5
                                            )
                                        )
                                        recyclerViewRegisteredDeviceListAdapter?.notifyItemInserted(list.size - 1)
                                    }
                                }

                                // 등록된 보일러의 pkey와 소비전력량 가져오기
                                if (data.boiler != null) {
                                    for (boilerData in data.boiler) {
                                        val pkey = boilerData.id
                                        val efficiency = boilerData.efficiency
                                        list.add(
                                            RegisteredDeviceData(
                                                pkey,
                                                DeviceTypeList.BOILER,
                                                "",
                                                null, null,
                                                efficiency,
                                                null, null, null, null
                                            )
                                        )
                                        recyclerViewRegisteredDeviceListAdapter?.notifyItemInserted(list.size - 1)
                                    }
                                }

                                // 등록된 건조기의 pkey와 소비전력량 가져오기
                                if (data.dryer != null) {
                                    for (dryerData in data.dryer) {
                                        val pkey = dryerData.id
                                        val energy = dryerData.energy
                                        list.add(
                                            RegisteredDeviceData(
                                                pkey,
                                                DeviceTypeList.DRYER,
                                                "",
                                                null, null,
                                                energy,
                                                null, null, null, 0.5
                                            )
                                        )
                                        recyclerViewRegisteredDeviceListAdapter?.notifyItemInserted(list.size - 1)
                                    }
                                }

                                Log.d("홈 화면", list.toString())

                                if (myLocation != "") {
                                    if (list.isEmpty()) {
                                        binding.constraintLayoutForPyramid.visibility = View.GONE
                                        if (textNoRelativeGradeData == "") {
                                            textNoRelativeGradeData = "등록된 기기가 없습니다."
                                        }
                                        binding.textNoRelativeGradeData.text = textNoRelativeGradeData
                                    } else {
                                        binding.textNoRelativeGradeData.visibility = View.GONE
                                        numOfRegisteredDevices = list.size

                                        // list에 있는 RegisteredDeviceData를 이용해 GET request를 날림
                                        callApplianceGet(0)
                                    }
                                }
                            } else {
                                simpleDialog(
                                    this@HomeActivity,
                                    "홈 화면",
                                    "기기 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요."
                                )
                                Log.d("홈 화면", "결과: 실패")
                                Log.d("홈 화면", result.toString())
                            }
                        }
                    } else {
                        val errorResult = response.errorBody()
                        val result = response.body()

                        if (errorResult != null) {
                            Log.d("홈 화면", "결과: 실패 (response.isSuccessful 통과하지 못함)")
                            Log.d("홈 화면", errorResult.string())
                            Log.d("홈 화면", result.toString())
                        }
                    }
                }

                override fun onFailure(call: Call<MainTotalInformationResponse>, t: Throwable) {
                    // progress bar 종료
                    progressDialog.dismiss()

                    simpleDialog(
                        this@HomeActivity,
                        "통신 오류",
                        "서버와의 통신이 원활하지 않습니다. 잠시 후 다시 시도해주세요."
                    )
                    Log.d("홈 화면", "결과: 실패 (onFailure)")
                    Log.d("홈 화면", t.message.toString())
                }
            }
        )
    }

    private fun callApplianceGet(idx: Int) {
        if (idx < list.size) {
            val apiInterface = APIClientForServerByPassSSLCertificate
                .getClient()
                .create(APIInterface::class.java)
            val call: Call<ApplianceDetailResponse>

            when (list[idx].deviceType) {
                DeviceTypeList.REFRIGERATOR -> {
                    call = apiInterface.applianceRefrigeratorGet(list[idx].id)
                    callAppliance(idx, call)
                }
                DeviceTypeList.AIR_CONDITIONER -> {
                    call = apiInterface.applianceAirConditionerGet(list[idx].id)
                    callAppliance(idx, call)
                }
                DeviceTypeList.TV -> {
                    call = apiInterface.applianceTelevisionGet(list[idx].id)
                    callAppliance(idx, call)
                }
                DeviceTypeList.WASHING_MACHINE -> {
                    call = apiInterface.applianceWashingMachineGet(list[idx].id)
                    callAppliance(idx, call)
                }
                DeviceTypeList.MICROWAVE_OVEN -> {
                    call = apiInterface.applianceMicrowaveGet(list[idx].id)
                    callAppliance(idx, call)
                }
                DeviceTypeList.BOILER -> {
                    val callBoiler: Call<BoilerDetailResponse> =
                        apiInterface.applianceBoilerGet(list[idx].id)
                    callApplianceBoiler(idx, callBoiler)
                }
                DeviceTypeList.DRYER -> {
                    call = apiInterface.applianceDryerGet(list[idx].id)
                    callAppliance(idx, call)
                }
                else -> return
            }
        }
    }

    private fun callAppliance(idx: Int, call: Call<ApplianceDetailResponse>) {
        var retryCnt = 0 // 재시도 횟수
        call.enqueue(
            object : Callback<ApplianceDetailResponse> {
                override fun onResponse(
                    call: Call<ApplianceDetailResponse>,
                    response: Response<ApplianceDetailResponse>
                ) {
                    Log.d(
                        "홈 화면 (${getTranslatedDeviceType(list[idx].deviceType)} 세부정보 호출)",
                        "statusCode: ${response.code()}"
                    )
                    Log.d(
                        "홈 화면 (${getTranslatedDeviceType(list[idx].deviceType)} 세부정보 호출)",
                        "결과: $idx, ${response.body()?.data}"
                    )

                    callApplianceGet(idx + 1)

                    if (response.isSuccessful) {
                        val result = response.body()

                        if (result != null) {
                            if (result.success) {
                                val data = result.data

                                list[idx].powerOfConsume = data.energy.toDouble()
                                list[idx].amountOfCO2Emission = data.co2.toDouble()
                                list[idx].model = ""
                                list[idx].relativeElectricPowerConsumeGrade = data.tier
                                list[idx].relativeElectricPowerConsumePercentage = data.relativePercent

                                when (list[idx].deviceType) {
                                    DeviceTypeList.REFRIGERATOR,
                                    DeviceTypeList.WASHING_MACHINE,
                                    DeviceTypeList.TV,
                                    DeviceTypeList.AIR_CONDITIONER,
                                    DeviceTypeList.DRYER -> {
                                        list[idx].relativeCO2EmissionGrade = data.tier
                                        list[idx].relativeCO2EmissionPercentage = data.relativePercent
                                    }
                                    else -> {}
                                }

                                // 상세 정보를 알아낸 기기의 개수 1 증가
                                numOfGetDetailedInformationDevices++

                                // 전력 소비 누적 비율(%)
                                sumOfRelativeEnergyConsumePercentage += data.relativePercent

                                // CO2 배출량 누적 비율(%)은 전력 소비 누적 비율(%)과 동일하다고 가정(임시)
                                sumOfRelativeCO2EmissionPercentage += data.relativePercent

                                recyclerViewRegisteredDeviceListAdapter?.notifyItemChanged(idx)

                                if (numOfGetDetailedInformationDevices == numOfRegisteredDevices) {
                                    setPyramid()
                                    showPyramid()
                                }
                            }
                        } else {
                            val errorBody = response.errorBody()

                            if (errorBody != null) {
                                Log.d(
                                    "홈 화면 (${getTranslatedDeviceType(list[idx].deviceType)} 세부정보 호출)",
                                    "errorBody: ${errorBody.string()}"
                                )
                            }
                        }
                    } else {
                        if (response.code() == 500) {
                            // 500 Internal Server Error
                            // request 재시도
                            // 재시도는 최대 3번만 가능
                            retryCnt++

                            if (retryCnt <= 3) {
                                call.clone().enqueue(this)
                            } else {
                                Log.d(
                                    "홈 화면 (${getTranslatedDeviceType(list[idx].deviceType)} 세부정보 호출)",
                                    "재시도 횟수 3회 초과"
                                )
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ApplianceDetailResponse>, t: Throwable) {
                    Log.d(
                        "홈 화면 (${getTranslatedDeviceType(list[idx].deviceType)} 세부정보 호출)",
                        "결과: 실패 (onFailure)"
                    )
                    Log.d(
                        "홈 화면 (${getTranslatedDeviceType(list[idx].deviceType)} 세부정보 호출)",
                        t.message.toString()
                    )
                }
            }
        )
    }

    // 나의 보일러 세부정보 호출
    private fun callApplianceBoiler(idx: Int, call: Call<BoilerDetailResponse>) {
        var retryCnt = 0 // 재시도 횟수
        call.enqueue(
            object : Callback<BoilerDetailResponse> {
                override fun onResponse(
                    call: Call<BoilerDetailResponse>,
                    response: Response<BoilerDetailResponse>
                ) {
                    Log.d("홈 화면 (보일러 세부정보 호출)", "statusCode: ${response.code()}")
                    Log.d("홈 화면 (보일러 세부정보 호출)", "결과: $idx, ${response.body()?.data}")

                    callApplianceGet(idx + 1)

                    if (response.isSuccessful) {
                        val result = response.body()

                        if (result != null) {
                            if (result.success) {
                                val data = result.data

                                list[idx].powerOfConsume = data.efficiency.toDouble()
                                list[idx].model = ""
                                list[idx].relativeElectricPowerConsumeGrade = data.tier
                                list[idx].relativeElectricPowerConsumePercentage = data.relativePercent

                                // 상세 정보를 알아낸 기기의 개수 1 증가
                                numOfGetDetailedInformationDevices++

                                // 전력 소비 누적 비율(%)
                                sumOfRelativeEnergyConsumePercentage += data.relativePercent

                                // CO2 배출량 누적 비율(%)은 전력 소비 누적 비율(%)과 동일하다고 가정(임시)
                                sumOfRelativeCO2EmissionPercentage += data.relativePercent

                                recyclerViewRegisteredDeviceListAdapter?.notifyItemChanged(idx)

                                if (numOfGetDetailedInformationDevices == numOfRegisteredDevices) {
                                    setPyramid()
                                    showPyramid()
                                }
                            }
                        } else {
                            val errorBody = response.errorBody()

                            if (errorBody != null) {
                                Log.d("홈 화면 (보일러 세부정보 호출)", "errorBody: ${errorBody.string()}")
                            }
                        }
                    } else {
                        if (response.code() == 500) {
                            // 500 Internal Server Error
                            // request 재시도
                            // 재시도는 최대 3번만 가능
                            retryCnt++

                            if (retryCnt <= 3) {
                                call.clone().enqueue(this)
                            } else {
                                Log.d(
                                    "홈 화면 (보일러 세부정보 호출)",
                                    "재시도 횟수 3회 초과"
                                )
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<BoilerDetailResponse>, t: Throwable) {
                    Log.d("홈 화면 (보일러 세부정보 호출)", "결과: 실패 (onFailure)")
                    Log.d("홈 화면 (보일러 세부정보 호출)", t.message.toString())
                }
            }
        )
    }

    private fun callChangeMyResidence(latitude: Double, longitude: Double) {
        val apiInterface = APIClientForServerByPassSSLCertificate
            .getClient()
            .create(APIInterface::class.java)

        val call = apiInterface.changeMyResidence(
            ChangeMyResidenceRequest(latitude, longitude)
        )

        // progress bar 호출
        val progressDialog = ProgressDialog.getProgressDialog(this, "처리 중 입니다")
        progressDialog.show()

        call.enqueue(
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
                            if ((result.success) && (result.message == "전송성공")) {
                                Log.d("홈 화면 (내 거주지 변경)", "결과: 성공")
                                Log.d("홈 화면 (내 거주지 변경)", result.toString())

                                simpleDialog(
                                    this@HomeActivity,
                                    "내 거주지 변경",
                                    "거주지 변경이 완료되었습니다."
                                )
                            } else {
                                simpleDialog(
                                    this@HomeActivity,
                                    "내 거주지 변경",
                                    "거주지 변경에 실패했습니다. 다시 시도해주세요."
                                )

                                Log.d("홈 화면 (내 거주지 변경)", "결과: 실패")
                                Log.d("홈 화면 (내 거주지 변경)", result.toString())
                            }
                        }
                    } else {
                        // status code가 200 ~ 299가 아닐 때
                        val errorResult = response.errorBody()
                        val result = response.body()

                        if (errorResult != null) {
                            Log.d("홈 화면 (내 거주지 변경)", "결과: 실패 (response.isSuccessful 통과하지 못함)")
                            Log.d("홈 화면 (내 거주지 변경)", "statusCode: ${response.code()}")
                            Log.d("홈 화면 (내 거주지 변경)", errorResult.string())
                            Log.d("홈 화면 (내 거주지 변경)", result.toString())
                        }
                    }
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    // progress bar 종료
                    progressDialog.dismiss()

                    simpleDialog(
                        this@HomeActivity,
                        "통신 오류",
                        "서버와의 통신이 원활하지 않습니다. 잠시 후 다시 시도해주세요."
                    )
                    Log.d("홈 화면 (내 거주지 변경)", "결과: 실패 (onFailure)")
                    Log.d("홈 화면 (내 거주지 변경)", t.message.toString())
                }
            }
        )
    }

    // 등록된 기기의 사용 여부가 변경될 경우 피라미드 재설정
    fun resetPyramid(mode: String, registeredDeviceData: RegisteredDeviceData) {
        if (mode == "OFF") {
            // 사용 여부를 OFF로 설정할 때

            if (registeredDeviceData.relativeElectricPowerConsumePercentage != null) {
                numOfRegisteredDevices -= 1

                if ((registeredDeviceData.relativeCO2EmissionGrade == null)
                    || (registeredDeviceData.relativeCO2EmissionPercentage == null)
                    || (registeredDeviceData.amountOfCO2Emission == null)) {
                    // CO2 배출량이 적혀있지 않은 제품의 경우 CO2 배출량 상대 등급과 누적 비율(%)은 동일하다고 가정
                    sumOfRelativeCO2EmissionPercentage -= registeredDeviceData.relativeElectricPowerConsumePercentage!!
                } else {
                    sumOfRelativeCO2EmissionPercentage -= registeredDeviceData.relativeCO2EmissionPercentage!!
                }

                sumOfRelativeEnergyConsumePercentage -= registeredDeviceData.relativeElectricPowerConsumePercentage!!
            }
        } else if (mode == "ON") {
            // 사용 여부를 ON으로 설정할 때
            if (registeredDeviceData.relativeElectricPowerConsumePercentage != null) {
                numOfRegisteredDevices += 1

                if ((registeredDeviceData.relativeCO2EmissionGrade == null)
                    || (registeredDeviceData.relativeCO2EmissionPercentage == null)
                    || (registeredDeviceData.amountOfCO2Emission == null)) {
                    // CO2 배출량이 적혀있지 않은 제품의 경우 CO2 배출량 상대 등급과 누적 비율(%)은 동일하다고 가정
                    sumOfRelativeCO2EmissionPercentage += registeredDeviceData.relativeElectricPowerConsumePercentage!!
                } else {
                    sumOfRelativeCO2EmissionPercentage += registeredDeviceData.relativeCO2EmissionPercentage!!
                }

                sumOfRelativeEnergyConsumePercentage += registeredDeviceData.relativeElectricPowerConsumePercentage!!
            }
        }

        // 피라미드 재설정
        showPyramid()
    }

    // 피라미드에 종합 상대적 에너지 소비 효율 등급을 나타냄
    private fun setPyramid() {
        // 피라미드 크기 조정
        val screenWidth = App.getWidth(this)
        val width_margin_px = fromDpToPx(resources, MARGIN_SIDE)
        val margin_between_pyramids_px = fromDpToPx(resources, MARGIN_BETWEEN_PYRAMIDS)
        val pyramidWidth = (screenWidth - 2 * width_margin_px - margin_between_pyramids_px) / 2
        val pyramidHeight = pyramidWidth * 1.055

        val layoutParamsForEnergyConsumePyramid =
            binding.constraintLayoutForEnergyConsumePyramid.layoutParams
        layoutParamsForEnergyConsumePyramid.width = pyramidWidth
        layoutParamsForEnergyConsumePyramid.height = pyramidHeight.toInt()
        binding.constraintLayoutForEnergyConsumePyramid.layoutParams = layoutParamsForEnergyConsumePyramid

        val layoutParamsForCO2Pyramid = binding.constraintLayoutForCO2Pyramid.layoutParams
        layoutParamsForCO2Pyramid.width = pyramidWidth
        layoutParamsForCO2Pyramid.height = pyramidHeight.toInt()
        binding.constraintLayoutForCO2Pyramid.layoutParams = layoutParamsForCO2Pyramid
    }

    private fun calculateRelativeGradeData() {
        // list에 있는 각각의 상대적 에너지 소비 효율 데이터를 통해 평균치를 구함

        // 데이터 초기화
        numOfRegisteredDevices = 0
        sumOfRelativeCO2EmissionPercentage = 0.0
        totalRelativeCO2EmissionGrade = -1
        sumOfRelativeEnergyConsumePercentage = 0.0
        totalRelativeEnergyConsumeGrade = -1

        for (relativeGradeData in list) {
            if (relativeGradeData.relativeElectricPowerConsumePercentage != null) {
                numOfRegisteredDevices += 1
                sumOfRelativeEnergyConsumePercentage += relativeGradeData.relativeElectricPowerConsumePercentage!!

                if ((relativeGradeData.relativeCO2EmissionGrade == null)
                    || (relativeGradeData.relativeCO2EmissionPercentage == null)
                    || (relativeGradeData.amountOfCO2Emission == null)) {
                    // CO2 배출량이 적혀있지 않은 제품의 경우 CO2 배출량 상대 등급과 누적 비율(%)은 동일하다고 가정
                    sumOfRelativeCO2EmissionPercentage += relativeGradeData.relativeElectricPowerConsumePercentage!!
                } else {
                    sumOfRelativeCO2EmissionPercentage += relativeGradeData.relativeCO2EmissionPercentage!!
                }
            }
        }

        showPyramid()
    }

    // setPyramid()를 통해 얻은 정보를 이용하여 화면에 피라미드를 나타냄
    private fun showPyramid() {
        if (numOfRegisteredDevices >= 1) {
            // 등록된 기기의 개수가 1개 이상일 경우
            binding.constraintLayoutForPyramid.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
            showPyramidForCO2()
            showPyramidForEnergyConsume()
        } else {
            // 피라미드를 숨김
            binding.constraintLayoutForPyramid.visibility = View.GONE
        }
    }

    // CO2 배출량 상대 등급과 누적 비율을 피라미드 형식으로 표시
    @SuppressLint("SetTextI18n")
    private fun showPyramidForCO2() {
        if (numOfRegisteredDevices <= 0) {
            return
        }

        val averageRelativeCO2EmissionPercentage =
            sumOfRelativeCO2EmissionPercentage / numOfRegisteredDevices

        if (averageRelativeCO2EmissionPercentage <= 4.0) { // 1등급 (CO2)
            binding.CO2Pyramid.setImageResource(R.drawable.pyramid1_co2_green)
            binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_1))
            totalRelativeCO2EmissionGrade = 1
        } else if (averageRelativeCO2EmissionPercentage <= 11.0) { // 2등급 (CO2)
            binding.CO2Pyramid.setImageResource(R.drawable.pyramid2_co2_green)
            binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_2_and_3))
            totalRelativeCO2EmissionGrade = 2
        } else if (averageRelativeCO2EmissionPercentage <= 23.0) { // 3등급 (CO2)
            binding.CO2Pyramid.setImageResource(R.drawable.pyramid2_co2_green)
            binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_2_and_3))
            totalRelativeCO2EmissionGrade = 3
        } else if (averageRelativeCO2EmissionPercentage <= 40.0) { // 4등급 (CO2)
            binding.CO2Pyramid.setImageResource(R.drawable.pyramid3_co2_green)
            binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_4_and_5))
            totalRelativeCO2EmissionGrade = 4
        } else if (averageRelativeCO2EmissionPercentage <= 60.0) { // 5등급 (CO2)
            binding.CO2Pyramid.setImageResource(R.drawable.pyramid3_co2_green)
            binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_4_and_5))
            totalRelativeCO2EmissionGrade = 5
        } else if (averageRelativeCO2EmissionPercentage <= 77.0) { // 6등급 (CO2)
            binding.CO2Pyramid.setImageResource(R.drawable.pyramid4_co2_green)
            binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_6_and_7))
            totalRelativeCO2EmissionGrade = 6
        } else if (averageRelativeCO2EmissionPercentage <= 89.0) { // 7등급 (CO2)
            binding.CO2Pyramid.setImageResource(R.drawable.pyramid4_co2_green)
            binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_6_and_7))
            totalRelativeCO2EmissionGrade = 7
        } else if (averageRelativeCO2EmissionPercentage <= 96.0) { // 8등급 (CO2)
            binding.CO2Pyramid.setImageResource(R.drawable.pyramid5_co2_green)
            binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_8_and_9))
            totalRelativeCO2EmissionGrade = 8
        } else { // 9등급 (CO2)
            binding.CO2Pyramid.setImageResource(R.drawable.pyramid5_co2_green)
            binding.textCO2EmissionGrade.setTextColor(getColor(R.color.grade_8_and_9))
            totalRelativeCO2EmissionGrade = 9
        }

        val paramsForCO2Grade = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
        )

        binding.textCO2EmissionGrade.text = "상위 ${averageRelativeCO2EmissionPercentage.toInt()}%"

        paramsForCO2Grade.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        paramsForCO2Grade.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        paramsForCO2Grade.marginStart = fromDpToPx(
            resources, MARGIN_TEXT[totalRelativeCO2EmissionGrade - 1][0]
        )
        paramsForCO2Grade.topMargin = fromDpToPx(
            resources, MARGIN_TEXT[totalRelativeCO2EmissionGrade - 1][1])

        binding.textCO2EmissionGrade.layoutParams = paramsForCO2Grade
    }

    // 전력 소비 상대 등급과 누적 비율을 피라미드 형식으로 표시
    @SuppressLint("SetTextI18n")
    private fun showPyramidForEnergyConsume() {
        if (numOfRegisteredDevices <= 0) {
            return
        }

        val averageRelativeEnergyConsumePercentage =
            sumOfRelativeEnergyConsumePercentage / numOfRegisteredDevices

        if (averageRelativeEnergyConsumePercentage <= 4.0) { // 1등급 (소비전력)
            binding.energyConsumePyramid.setImageResource(R.drawable.pyramid1_energy_green)
            binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_1))
            totalRelativeEnergyConsumeGrade = 1
        } else if (averageRelativeEnergyConsumePercentage <= 11.0) { // 2등급 (소비전력)
            binding.energyConsumePyramid.setImageResource(R.drawable.pyramid2_energy_green)
            binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_2_and_3))
            totalRelativeEnergyConsumeGrade = 2
        } else if (averageRelativeEnergyConsumePercentage <= 23.0) { // 3등급 (소비전력)
            binding.energyConsumePyramid.setImageResource(R.drawable.pyramid2_energy_green)
            binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_2_and_3))
            totalRelativeEnergyConsumeGrade = 3
        } else if (averageRelativeEnergyConsumePercentage <= 40.0) { // 4등급 (소비전력)
            binding.energyConsumePyramid.setImageResource(R.drawable.pyramid3_energy_green)
            binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_4_and_5))
            totalRelativeEnergyConsumeGrade = 4
        } else if (averageRelativeEnergyConsumePercentage <= 60.0) { // 5등급 (소비전력)
            binding.energyConsumePyramid.setImageResource(R.drawable.pyramid3_energy_green)
            binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_4_and_5))
            totalRelativeEnergyConsumeGrade = 5
        } else if (averageRelativeEnergyConsumePercentage <= 77.0) { // 6등급 (소비전력)
            binding.energyConsumePyramid.setImageResource(R.drawable.pyramid4_energy_green)
            binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_6_and_7))
            totalRelativeEnergyConsumeGrade = 6
        } else if (averageRelativeEnergyConsumePercentage <= 89.0) { // 7등급 (소비전력)
            binding.energyConsumePyramid.setImageResource(R.drawable.pyramid4_energy_green)
            binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_6_and_7))
            totalRelativeEnergyConsumeGrade = 7
        } else if (averageRelativeEnergyConsumePercentage <= 96.0) { // 8등급 (소비전력)
            binding.energyConsumePyramid.setImageResource(R.drawable.pyramid5_energy_green)
            binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_8_and_9))
            totalRelativeEnergyConsumeGrade = 8
        } else { // 9등급 (소비전력)
            binding.energyConsumePyramid.setImageResource(R.drawable.pyramid5_energy_green)
            binding.textEnergyConsumeGrade.setTextColor(getColor(R.color.grade_8_and_9))
            totalRelativeEnergyConsumeGrade = 9
        }

        val paramsForEnergyConsumeGrade = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
        )

        binding.textEnergyConsumeGrade.text = "상위 ${averageRelativeEnergyConsumePercentage.toInt()}%"

        paramsForEnergyConsumeGrade.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        paramsForEnergyConsumeGrade.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        paramsForEnergyConsumeGrade.marginStart =
            fromDpToPx(resources, MARGIN_TEXT[totalRelativeEnergyConsumeGrade - 1][0])
        paramsForEnergyConsumeGrade.topMargin =
            fromDpToPx(resources, MARGIN_TEXT[totalRelativeEnergyConsumeGrade - 1][1])
        binding.textEnergyConsumeGrade.layoutParams = paramsForEnergyConsumeGrade
    }

    // 현재 위치를 기반으로 지번 주소와 도로명 주소를 찾음
    private fun searchAddress(latitude: Double, longitude: Double) {
        val coords = "$longitude,$latitude" // 경도, 위도
        Log.d("위치", coords)

        val apiInterface = APIClientForNaverMap
            .getClient()
            .create(APIInterface::class.java)
        val call = apiInterface.searchAddressByPoint(coords, "json", "addr,roadaddr")

        call.enqueue(
            object : Callback<ReverseGeocodingResponse> {
                override fun onResponse(
                    call: Call<ReverseGeocodingResponse>,
                    response: Response<ReverseGeocodingResponse>
                ) {
                    if (response.isSuccessful) {
                        try {
                            val result = response.body()
                            Log.d("위치", result.toString())

                            if (result != null) {
                                val addressList = mutableListOf<String>()

                                if (result.results.isEmpty()) {
                                    simpleDialog(
                                        this@HomeActivity,
                                        "내 거주지 변경",
                                        "해당 위치에 대한 데이터가 없습니다. 다른 지역에서 다시 시도해 주세요."
                                    )
                                    return
                                }

                                for (area in result.results) {
                                    val addrRegion = area.region
                                    val addrLand = area.land

                                    val address = if (addrLand.number2 == "") {
                                        "${addrRegion.area1.name} " +
                                                "${addrRegion.area2.name} " +
                                                "${addrRegion.area3.name} " +
                                                addrLand.number1
                                    } else {
                                        "${addrRegion.area1.name} " +
                                                "${addrRegion.area2.name} " +
                                                "${addrRegion.area3.name} " +
                                                "${addrLand.number1}-${addrLand.number2}"
                                    }

                                    addressList.add(address)
                                }

                                val finalAddressList = addressList.toTypedArray()
                                chooseAddress(finalAddressList, latitude, longitude)
                            }
                        } catch (e: Exception) {
                            simpleDialog(
                                this@HomeActivity,
                                "내 거주지 변경",
                                "현재 위치를 가져오지 못했습니다. 잠시 후 다시 시도해 주세요."
                            )
                            Log.d("홈 화면 (내 거주지 변경)", e.toString())
                            e.printStackTrace()
                        }
                    } else {
                        // status code가 200 ~ 299가 아닐 때
                        val errorResult = response.errorBody()
                        val result = response.body()

                        if (errorResult != null) {
                            Log.d("홈 화면 (내 거주지 변경)", "결과: 실패 (response.isSuccessful 통과하지 못함)")
                            Log.d("홈 화면 (내 거주지 변경)", "statusCode: ${response.code()}")
                            Log.d("홈 화면 (내 거주지 변경)", errorResult.string())
                            Log.d("홈 화면 (내 거주지 변경)", result.toString())
                        }
                    }
                }

                override fun onFailure(call: Call<ReverseGeocodingResponse>, t: Throwable) {
                    simpleDialog(
                        this@HomeActivity,
                        "내 거주지 변경",
                        "주소 정보를 가져오지 못했습니다. 다시 시도해주세요."
                    )
                    Log.d("홈 화면 (내 거주지 변경)", t.message.toString())
                }
            }
        )
    }

    // Dialog를 통해 거주지 설정
    private fun chooseAddress(
        addressList: Array<String>,
        latitude: Double,
        longitude: Double
    ) {
        var selected = 0

        val alertDialogBuilderBtn = AlertDialog.Builder(this)
        alertDialogBuilderBtn.setTitle("거주지로 설정할 주소를 선택해 주세요.")
        alertDialogBuilderBtn.setSingleChoiceItems(addressList, selected) { _, which ->
            when (which) {
                which -> selected = which
            }
        }
        alertDialogBuilderBtn.setPositiveButton("확인") { _, _ ->
            binding.textMyResidentInfo.text = addressList[selected]

            myLocation = addressList[selected]
            textNoRelativeGradeData = ""

            // changeMyResidence API 호출
            callChangeMyResidence(latitude, longitude)
            getMyLocationFromServer()
        }
        alertDialogBuilderBtn.setNegativeButton("취소") { _, _ -> }

        val alertDialogBox = alertDialogBuilderBtn.create()
        alertDialogBox.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 앱바에 back 버튼 활성화

        list = ArrayList()
        recyclerView = binding.recyclerView

        recyclerViewRegisteredDeviceListAdapter = RecyclerViewRegisteredDeviceListAdapter(this, list)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = recyclerViewRegisteredDeviceListAdapter

        binding.textMyResidentInfo.text = "인천광역시 미추홀구 용현동 12"
        myLocation = "인천광역시 미추홀구 용현동 12"

        binding.progressBar.visibility = View.VISIBLE
        binding.constraintLayoutForPyramid.visibility = View.GONE

        // 변수 초기화
        numOfGetDetailedInformationDevices = 0
        numOfRegisteredDevices = 0
        sumOfRelativeEnergyConsumePercentage = 0.0
        sumOfRelativeCO2EmissionPercentage = 0.0

        // 내 거주지 불러오기
        getMyLocationFromServer()

        // 서버로 부터 상대적 에너지 소비 효율 등급 관련 데이터를 받아옴
        prepareListData()

        drawerLayout = binding.drawerLayout
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // swipe refresh 기능 구현
        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            finish()
            overridePendingTransition(0, 0)
            val intent = intent
            startActivity(intent)
            overridePendingTransition(0, 0)
            swipeRefreshLayout.isRefreshing = false
        }

        navigationView = binding.navView
        navigationView.setNavigationItemSelectedListener(this)

        binding.btnChangeMyResident.setOnClickListener {
            val alertDialogBuilderBtn = AlertDialog.Builder(this)
            alertDialogBuilderBtn.setTitle("내 거주지 변경")
            alertDialogBuilderBtn.setItems(optionsForChangeMyResidence) { _, which ->
                when (which) {
                    0 -> {
                        // 현재 위치를 거주지로 설정
                        setMyCurrentLocationAsMyResidence()
                    }
                }
            }
            alertDialogBuilderBtn.setNegativeButton("취소") { _, _ -> }

            val alertDialogBox = alertDialogBuilderBtn.create()
            alertDialogBox.show()
        }

        binding.btnAddDevice.setOnClickListener {
            val intent = Intent(this, AddDeviceActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.drawerLogout -> {
                val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                    // 토큰 정보 초기화
                    App.prefs.clearValue()

                    // 로그인 화면으로 되돌아가고 intent 스택은 모두 삭제
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                }

                createDialog(
                    this@HomeActivity,
                    "로그아웃",
                    "로그아웃 하시겠습니까?",
                    positiveButtonOnClickListener,
                    defaultNegativeDialogInterfaceOnClickListener
                )
            }
            R.id.developersTool -> {
                val intent = Intent(this, TestMainActivity::class.java)
                startActivity(intent)
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}