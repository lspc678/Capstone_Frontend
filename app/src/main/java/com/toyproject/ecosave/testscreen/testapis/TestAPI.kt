package com.toyproject.ecosave.testscreen.testapis

import com.toyproject.ecosave.testscreen.testmodels.TestRequestPost
import com.toyproject.ecosave.testscreen.testmodels.TestResponsePost
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface TestAPI {
    @POST("/api/users")
    fun postUser(@Body testRequestPost: TestRequestPost) : Call<TestResponsePost>

    companion object {
        private const val BASE_URL = "https://reqres.in"

        fun create() : TestAPI {
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
                .create(TestAPI::class.java)
        }
    }
}