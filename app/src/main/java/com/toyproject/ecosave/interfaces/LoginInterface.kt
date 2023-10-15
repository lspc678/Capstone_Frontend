package com.toyproject.ecosave.interfaces

import com.toyproject.ecosave.models.LoginRequestBody
import com.toyproject.ecosave.models.LoginResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginInterface {
    @Headers("Content-Type: application/json")
    @POST("account/log-in")
    fun processLoginByEnqueue(@Body userInfo: LoginRequestBody) : Call<LoginResponseBody>
}