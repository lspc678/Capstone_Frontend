package com.toyproject.ecosave.test

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

import com.toyproject.ecosave.databinding.ActivityTestMainBinding
import com.toyproject.ecosave.test.api.TestAPIClient
import com.toyproject.ecosave.test.api.TestAPIInterface
import com.toyproject.ecosave.test.api.requestmodel.TestCreateRequest
import com.toyproject.ecosave.test.api.responsemodel.TestCreateResponse
import com.toyproject.ecosave.test.api.responsemodel.TestListUsersResponse
import com.toyproject.ecosave.test.api.responsemodel.TestSingleUserResponse

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
    }
}