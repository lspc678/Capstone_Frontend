package com.toyproject.ecosave.test

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged

import com.toyproject.ecosave.App
import com.toyproject.ecosave.api.APIClientForServerByPassSSLCertificate
import com.toyproject.ecosave.api.APIInterface
import com.toyproject.ecosave.api.responsemodels.ApplianceDetailResponse
import com.toyproject.ecosave.databinding.ActivityTestMainBinding
import com.toyproject.ecosave.test.api.TestAPIClient
import com.toyproject.ecosave.test.api.TestAPIInterface
import com.toyproject.ecosave.test.api.requestmodel.TestCreateRequest
import com.toyproject.ecosave.test.api.responsemodel.TestCreateResponse
import com.toyproject.ecosave.test.api.responsemodel.TestListUsersResponse
import com.toyproject.ecosave.test.api.responsemodel.TestSingleUserResponse

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// retrofit2
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TestMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestMainBinding

    // API 테스트 (GET 통신), API 테스트 (POST 통신)에서 사용

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // API 테스트 (LIST USERS)
        binding.btnTestListUsersRequest.setOnClickListener {
            val apiInterface = TestAPIClient.getClient().create(TestAPIInterface::class.java)
            val call = apiInterface.listUsers(2)
            call.enqueue(
                object : Callback<TestListUsersResponse> {
                    override fun onResponse(
                        call: Call<TestListUsersResponse>,
                        response: Response<TestListUsersResponse>
                    ) {
                        if (response.isSuccessful) {
                            val result = response.body()
                            Log.d("API 테스트", "결과: 성공 (statusCode: ${response.code()})")
                            Log.d("API 테스트", result.toString())
                        } else {
                            Log.d("API 테스트", "결과: 실패 (statusCode: ${response.code()})")
                            Log.d("API 테스트", response.message())
                        }
                    }

                    override fun onFailure(call: Call<TestListUsersResponse>, t: Throwable) {
                        Log.d("API 테스트", "결과: 실패")
                        if (t.message != null) {
                            Log.d("API 테스트", t.message!!)
                        }
                    }
                }
            )
        }

        // API 테스트 (SINGLE USER)
        binding.btnTestSingleUserRequest.setOnClickListener {
            val apiInterface = TestAPIClient.getClient().create(TestAPIInterface::class.java)
            val call = apiInterface.singleUser()
            call.enqueue(
                object : Callback<TestSingleUserResponse> {
                    override fun onResponse(
                        call: Call<TestSingleUserResponse>,
                        response: Response<TestSingleUserResponse>
                    ) {
                        if (response.isSuccessful) {
                            val result = response.body()
                            if (response.code() == 200) {
                                Log.d("API 테스트", "결과: 성공")
                                Log.d("API 테스트", result.toString())
                            } else {
                                Log.d("API 테스트", "결과: 성공 (statusCode: ${response.code()})")
                                Log.d("API 테스트", result.toString())
                            }
                        } else {
                            Log.d("API 테스트", "결과: 실패 (statusCode: ${response.code()})")
                            Log.d("API 테스트", response.message())
                        }
                    }

                    override fun onFailure(call: Call<TestSingleUserResponse>, t: Throwable) {
                        Log.d("API 테스트", "결과: 실패")
                        if (t.message != null) {
                            Log.d("API 테스트", t.message!!)
                        }
                    }
                }
            )
        }

        // API 테스트 (SINGLE USER NOT FOUND)
        binding.btnTestSingleUserNotFoundRequest.setOnClickListener {
            val apiInterface = TestAPIClient.getClient().create(TestAPIInterface::class.java)
            val call = apiInterface.singleUserNotFound()
            call.enqueue(
                object : Callback<TestSingleUserResponse> {
                    override fun onResponse(
                        call: Call<TestSingleUserResponse>,
                        response: Response<TestSingleUserResponse>
                    ) {
                        if (response.isSuccessful) {
                            val result = response.body()
                            if (response.code() == 200) {
                                Log.d("API 테스트", "결과: 성공")
                                Log.d("API 테스트", result.toString())
                            } else {
                                Log.d("API 테스트", "결과: 성공 (statusCode: ${response.code()})")
                                Log.d("API 테스트", result.toString())
                            }
                        } else {
                            Log.d("API 테스트", "결과: 실패 (statusCode: ${response.code()})")
                            Log.d("API 테스트", response.message())
                        }
                    }

                    override fun onFailure(call: Call<TestSingleUserResponse>, t: Throwable) {
                        Log.d("API 테스트", "결과: 실패")
                        if (t.message != null) {
                            Log.d("API 테스트", t.message!!)
                        }
                    }
                }
            )
        }

        // API 테스트 (CREATE)
        binding.btnTestCreateRequest.setOnClickListener {
            val apiInterface = TestAPIClient.getClient().create(TestAPIInterface::class.java)
            val call = apiInterface.create(TestCreateRequest("morpheus", "leader"))
            call.enqueue(
                object : Callback<TestCreateResponse> {
                    override fun onResponse(
                        call: Call<TestCreateResponse>,
                        response: Response<TestCreateResponse>
                    ) {
                        if (response.isSuccessful) {
                            val result = response.body()
                            Log.d("API 테스트", "결과: 성공 (statusCode: ${response.code()})")
                            Log.d("API 테스트", result.toString())
                        } else {
                            Log.d("API 테스트", "결과: 실패 (statusCode: ${response.code()})")
                            Log.d("API 테스트", response.message())
                        }
                    }

                    override fun onFailure(call: Call<TestCreateResponse>, t: Throwable) {
                        Log.d("API 테스트", "결과: 실패")
                        if (t.message != null) {
                            Log.d("API 테스트", t.message!!)
                        }
                    }
                }
            )
        }

        // 중복 확인 클릭 시 Readonly 모드로 전환
        binding.btnTestCheckDuplicateNickname.setOnClickListener {
            val editTextForNickname = binding.editTextForNickname
            editTextForNickname.isEnabled = false
            Log.d("닉네임 중복 확인(테스트)", binding.editTextForNickname.text.toString())
        }

        // 비밀번호 일치 확인
        binding.editTextForPasswordConfirm.doOnTextChanged { _, _, _, _ ->
            val password = binding.editTextForPassword.text.toString()
            val passwordConfirm = binding.editTextForPasswordConfirm.text.toString()

            if (password != passwordConfirm) {
                binding.textInputForPasswordConfirm.error = "비밀번호가 일치하지 않습니다."
            } else {
                binding.textInputForPasswordConfirm.error = null
            }
        }

        // SharedPreferences 저장
        binding.btnTestSharedPreferencesSave.setOnClickListener {
            try {
                App.prefs.setValue("boilerEnergyConsume", 92)
                App.prefs.setValue("refrigeratorEnergyConsume", 35)
                App.prefs.setValue("refrigeratorCO2Emission", 21)

                Log.d("SharedPreferences", "저장 완료")
            } catch (e: Exception) {
                Log.d("SharedPreferences", e.toString())
            }

        }

        // SharedPreferences 불러오기
        binding.btnTestSharedPreferencesLoad.setOnClickListener {
            try {
                Log.d("SharedPreferences", App.prefs.getValue("boilerEnergyConsume", 0))
                Log.d("SharedPreferences", App.prefs.getValue("refrigeratorEnergyConsume", 0))
                Log.d("SharedPreferences", App.prefs.getValue("refrigeratorCO2Emission", 0))
                Log.d("SharedPreferences", App.prefs.getValue("microwaveOvenCO2Emission", 0))
            } catch (e: Exception) {
                Log.d("SharedPreferences", e.toString())
            }
        }

        // Multiple API Request 테스트
        binding.btnTestMultipleAPIRequest.setOnClickListener {
            try {
                prepareData()
            } catch (e: Exception) {
                Log.d("Multiple API Request 테스트", e.toString())
            }
        }
    }

    data class Info(val deviceType: String, val id: Int)
    private val idList = mutableListOf<Info>()

    private fun prepareData() {
        idList.add(Info("tv", 26))
        idList.add(Info("tv", 26))
        idList.add(Info("tv", 26))
        idList.add(Info("tv", 26))
        idList.add(Info("tv", 26))

        idList.add(Info("dryer", 34))
        idList.add(Info("dryer", 36))
        idList.add(Info("dryer", 37))
        idList.add(Info("dryer", 40))
        idList.add(Info("dryer", 34))
        idList.add(Info("dryer", 36))
        idList.add(Info("dryer", 37))
        idList.add(Info("dryer", 40))
        idList.add(Info("dryer", 34))
        idList.add(Info("dryer", 36))
        idList.add(Info("dryer", 37))
        idList.add(Info("dryer", 40))

        callAPI()
    }

    private fun callAPI() {
        CoroutineScope(Dispatchers.IO).launch {
            callAPI(0)
        }
    }

    private fun callAPI(idx: Int) {
        if (idx < idList.size - 1) {
            val apiInterface = APIClientForServerByPassSSLCertificate
                .getClient()
                .create(APIInterface::class.java)
            val call: Call<ApplianceDetailResponse>

            when (idList[idx].deviceType) {
                "tv" -> {
                    call = apiInterface.applianceTelevisionGet(idList[idx].id)
                    makeRequest(idList[idx].id, idx, "tv", call)
                }
                "dryer" -> {
                    call = apiInterface.applianceDryerGet(idList[idx].id)
                    makeRequest(idList[idx].id, idx, "dryer", call)
                }
            }
        }
    }

    private fun makeRequest(id: Int, idx: Int, deviceType: String, call: Call<ApplianceDetailResponse>) {
        try {
            call.enqueue(
                object : Callback<ApplianceDetailResponse> {
                    override fun onResponse(
                        call: Call<ApplianceDetailResponse>,
                        response: Response<ApplianceDetailResponse>
                    ) {
                        Log.d("Multiple API Request 테스트(makeRequest)", "id: $id, statusCode: ${response.code()}")
                        Log.d("Multiple API Request 테스트(makeRequest)", "결과: ${response.body()?.data}")

                        callAPI(idx + 1)

                        if (response.isSuccessful) {
                            val result = response.body()

                            if (result != null) {
                                if (result.success) {
                                    val data = result.data
                                    Log.d("Multiple API Request 테스트(makeRequest)", data.toString())
                                }
                            }
                        } else {
                            if (response.code() == 500) {
                                Log.d(
                                    "Multiple API Request 테스트(makeRequest)",
                                    "errorCode: 500"
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<ApplianceDetailResponse>, t: Throwable) {
                        Log.d(
                            "Multiple API Request 테스트(makeRequest)",
                            "결과: 실패 (onFailure)"
                        )
                        Log.d(
                            "Multiple API Request 테스트(makeRequest)",
                            t.message.toString()
                        )
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("Multiple API Request 테스트(makeRequest)", e.toString())
        }
    }
}