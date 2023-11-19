package com.toyproject.ecosave.api

import com.toyproject.ecosave.api.requestmodels.LoginRequest
import com.toyproject.ecosave.api.responsemodels.EmailAuthenticationResponse
import com.toyproject.ecosave.api.responsemodels.LoginResponse
import com.toyproject.ecosave.api.responsemodels.ReverseGeocodingResponse
import com.toyproject.ecosave.api.responsemodels.SearchAddressResponse
import com.toyproject.ecosave.api.responsemodels.SignUpSendMailResponse

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface APIInterface {
    @GET("account/sign-up/certification")
    fun sendEmailAuthenticationRequest(
        @Query("mail") mail: String,
        @Query("code") code: String) : Call<EmailAuthenticationResponse>

    @GET("account/sign-up/send-mail")
    fun sendMail(@Query("mail") mail: String) : Call<SignUpSendMailResponse>

    @Headers("Content-Type: application/json")
    @POST("account/log-in")
    fun login(@Body userInfo: LoginRequest) : Call<LoginResponse>

    companion object {
        // 암호화 예정
        const val API_KEY_ID = "gqeoomhzl8"
        const val API_KEY = "MAmOVj7yJv7ETYUXVrIJrQ78hdJdjbPu6vBpY6NY"
    }

    // GeocodingAPI
    @Headers(
        "X-NCP-APIGW-API-KEY-ID: $API_KEY_ID",
        "X-NCP-APIGW-API-KEY: $API_KEY",
        "Accept: application/json"
    )
    @GET("map-geocode/v2/geocode")
    fun searchAddress(@Query("query") query: String) : Call<SearchAddressResponse>

    // ReverseGeocodingAPI
    @Headers(
        "X-NCP-APIGW-API-KEY-ID: $API_KEY_ID",
        "X-NCP-APIGW-API-KEY: $API_KEY"
    )
    @GET("map-reversegeocode/v2/gc")
    fun searchAddressByPoint(
        @Query("coords") coords: String,
        @Query("output") output: String,
        @Query("orders") orders: String
    ) : Call<ReverseGeocodingResponse>
}