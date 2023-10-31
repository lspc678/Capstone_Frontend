package com.toyproject.ecosave.apis.naverapi

import com.toyproject.ecosave.models.ReverseGeocodingResponse

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ReverseGeocodingAPI {
    @Headers(
        "X-NCP-APIGW-API-KEY-ID: gqeoomhzl8",
        "X-NCP-APIGW-API-KEY: MAmOVj7yJv7ETYUXVrIJrQ78hdJdjbPu6vBpY6NY",
    )
    @GET("map-reversegeocode/v2/gc")
    fun searchAddressByPoint(
        @Query("coords") coords: String,
        @Query("output") output: String,
        @Query("orders") orders: String
    ) : Call<ReverseGeocodingResponse>

    companion object {
        private const val BASE_URL = "https://naveropenapi.apigw.ntruss.com/"

        fun create() : ReverseGeocodingAPI {
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
                .create(ReverseGeocodingAPI::class.java)
        }
    }
}