package com.toyproject.ecosave.apis

import com.toyproject.ecosave.models.EmailAuthenticationResponseBody

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface EmailAuthenticationAPI {
    @Headers("Content-Type: application/json")
    @GET("account/sign-up/certification")
    fun call(
        @Query("mail") mail: String,
        @Query("code") code: String) : Call<EmailAuthenticationResponseBody>

    companion object {
        private const val BASE_URL = "http://13.125.246.213:8000/"

        fun create() : EmailAuthenticationAPI {
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
                .create(EmailAuthenticationAPI::class.java)
        }
    }
}