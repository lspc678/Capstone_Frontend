package com.toyproject.ecosave.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.toyproject.ecosave.databinding.ActivityTestMainBinding
import com.toyproject.ecosave.test.testmodels.TestRequestPost
import com.toyproject.ecosave.test.testmodels.TestResponsePost
import com.toyproject.ecosave.test.testmodels.TestUserData

// retrofit2
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

class TestMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestMainBinding

    // API 테스트 (GET 통신), API 테스트 (POST 통신)에서 사용
    interface RequestUser {
        @GET("/api/users/{uid}")
        fun getUser(@Path("uid") uid: String) : Call<TestUserData>

        @POST("/api/users")
        fun postUser(@Body testRequestPost: TestRequestPost) : Call<TestResponsePost>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // API 테스트 (GET 통신)
        binding.btnTestGetRequest.setOnClickListener {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://reqres.in")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val requestUser = retrofit.create(RequestUser::class.java)
            requestUser.getUser("3").enqueue(object : Callback<TestUserData> {
                override fun onResponse(
                    call: Call<TestUserData>,
                    response: Response<TestUserData>
                ) {
                    binding.textTestGetRequestResult.text = response.body()?.data?.first_name ?: "Null"
                }

                override fun onFailure(call: Call<TestUserData>, t: Throwable) {
                    binding.textTestGetRequestResult.text = t.message
                }
            })
        }

        // API 테스트 (POST 통신)
        binding.btnTestPostRequest.setOnClickListener {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://reqres.in")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val requestUser = retrofit.create(RequestUser::class.java)
            requestUser.postUser(TestRequestPost("pawan", "programmer")).enqueue(object : Callback<TestResponsePost> {
                override fun onResponse(
                    call: Call<TestResponsePost>,
                    response: Response<TestResponsePost>
                ) {
                    binding.textTestPostRequestResult.text = response.body()?.name ?: "Null"
                }

                override fun onFailure(call: Call<TestResponsePost>, t: Throwable) {
                    binding.textTestPostRequestResult.text = t.message
                }
            })
        }
    }
}