package com.toyproject.ecosave.apis

import com.toyproject.ecosave.models.LoginRequestBody
import com.toyproject.ecosave.models.LoginResponseBody

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Call
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginAPI {
    @Headers("Content-Type: application/json")
    @POST("account/log-in")
    fun postLogin(@Body userInfo: LoginRequestBody) : Call<LoginResponseBody>

    companion object {
        private const val BASE_URL = "http://13.125.246.213:8000/"

        fun create() : LoginAPI {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val headerInterceptor = Interceptor {
                val request = it.request()
                    .newBuilder()
                    .build()
                return@Interceptor it.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LoginAPI::class.java)
        }
    }
}