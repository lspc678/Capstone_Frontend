package com.toyproject.ecosave.services

import android.util.Log
import com.toyproject.ecosave.apis.LoginAPI
import com.toyproject.ecosave.models.LoginRequestBody
import com.toyproject.ecosave.models.LoginResponseBody

import retrofit2.Call
import retrofit2.Response

class LoginService(private val userInfo: LoginRequestBody) {
    fun work() {
        val service = LoginAPI.emgMedService

        service.processLoginByEnqueue(userInfo).enqueue(object : retrofit2.Callback<LoginResponseBody> {
            override fun onResponse(call: Call<LoginResponseBody>, response: Response<LoginResponseBody>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    Log.d("로그인 성공", "$result")

                }
            }

            override fun onFailure(call: Call<LoginResponseBody>, t: Throwable) {
                Log.d("로그인 실패", t.message.toString())
            }
        })
    }
}