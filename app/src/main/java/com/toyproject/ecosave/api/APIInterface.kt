package com.toyproject.ecosave.api

import com.toyproject.ecosave.api.requestmodels.AppliancePostRequest
import com.toyproject.ecosave.api.requestmodels.ChangeMyResidenceRequest
import com.toyproject.ecosave.api.requestmodels.LoginRequest
import com.toyproject.ecosave.api.requestmodels.SignUpRequest
import com.toyproject.ecosave.api.responsemodels.ApplianceDetailResponse
import com.toyproject.ecosave.api.responsemodels.BoilerDetailResponse
import com.toyproject.ecosave.api.responsemodels.CheckDuplicateNicknameResponse
import com.toyproject.ecosave.api.responsemodels.DefaultResponse
import com.toyproject.ecosave.api.responsemodels.EmailAuthenticationResponse
import com.toyproject.ecosave.api.responsemodels.LoginResponse
import com.toyproject.ecosave.api.responsemodels.MainTotalInformationResponse
import com.toyproject.ecosave.api.responsemodels.ReverseGeocodingResponse
import com.toyproject.ecosave.api.responsemodels.SearchAddressResponse
import com.toyproject.ecosave.api.responsemodels.SignUpResponse
import com.toyproject.ecosave.api.responsemodels.SignUpSendMailResponse

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface APIInterface {
    @GET("account/sign-up/certification")
    fun sendEmailAuthenticationRequest(
        @Query("mail") mail: String,
        @Query("code") code: String) : Call<EmailAuthenticationResponse>

    @GET("account/sign-up/send-mail")
    fun sendMail(@Query("mail") mail: String) : Call<SignUpSendMailResponse>

    @GET("account/duplicate-nickname")
    fun checkDuplicateNickname(@Query("nickname") nickname: String) : Call<CheckDuplicateNicknameResponse>

    @Headers("Content-Type: application/json")
    @POST("account")
    fun signUp(@Body signupInfo: SignUpRequest) : Call<SignUpResponse>

    @POST("account/log-in")
    fun login(@Body userInfo: LoginRequest) : Call<LoginResponse>

    @GET("main/totalInformation")
    fun mainTotalInformation() : Call<MainTotalInformationResponse>

    // 내 거주지 변경
    @Headers("Content-Type: application/json")
    @PUT("main/address")
    fun changeMyResidence(@Body body: ChangeMyResidenceRequest) : Call<DefaultResponse>

    // 나의 냉장고 세부정보 호출
    @GET("appliance/refrigerator")
    fun applianceRefrigeratorGet(@Query("id") id: Int) : Call<ApplianceDetailResponse>

    // 나의 냉장고 등록
    @POST("appliance/refrigerator")
    fun applianceRefrigeratorPost(@Body body: AppliancePostRequest) : Call<DefaultResponse>

    // 나의 냉장고 삭제
    @DELETE("appliance/refrigerator")
    fun applianceRefrigeratorDelete() : Call<DefaultResponse>

    // 나의 에어컨 세부정보 호출
    @GET("appliance/air-conditioner")
    fun applianceAirConditionerGet(@Query("id") id: Int) : Call<ApplianceDetailResponse>

    // 나의 에어컨 등록
    @POST("appliance/air-conditioner")
    fun applianceAirConditionerPost(@Body body: AppliancePostRequest) : Call<DefaultResponse>

    // 나의 에어컨 삭제
    @DELETE("appliance/air_conditioner")
    fun applianceAirConditionerDelete() : Call<DefaultResponse>

    // 나의 TV 세부정보 호출
    @GET("appliance/television")
    fun applianceTelevisionGet(@Query("id") id: Int) : Call<ApplianceDetailResponse>

    // 나의 TV 등록
    @POST("appliance/television")
    fun applianceTelevisionPost(@Body body: AppliancePostRequest) : Call<DefaultResponse>

    // 나의 TV 삭제
    @DELETE("appliance/television")
    fun applianceTelevisionDelete() : Call<DefaultResponse>

    // 나의 세탁기 세부정보 호출
    @GET("appliance/washing-machine")
    fun applianceWashingMachineGet(@Query("id") id: Int) : Call<ApplianceDetailResponse>

    // 나의 세탁기 등록
    @POST("appliance/washing-machine")
    fun applianceWashingMachinePost(@Body body: AppliancePostRequest) : Call<DefaultResponse>

    // 나의 세탁기 삭제
    @DELETE("appliance/washing-machine")
    fun applianceWashingMachineDelete() : Call<DefaultResponse>

    // 나의 전자레인지 세부정보 호출
    @GET("appliance/microwave")
    fun applianceMicrowaveGet(@Query("id") id: Int) : Call<ApplianceDetailResponse>

    // 나의 전자레인지 등록
    @POST("appliance/microwave")
    fun applianceMicrowavePost(@Body body: AppliancePostRequest) : Call<DefaultResponse>

    // 나의 전자레인지 삭제
    @DELETE("appliance/microwave")
    fun applianceMicrowaveDelete() : Call<DefaultResponse>

    // 나의 보일러 세부정보 호출
    @GET("appliance/boiler")
    fun applianceBoilerGet(@Query("id") id: Int) : Call<BoilerDetailResponse>

    // 나의 보일러 등록
    @POST("appliance/boiler")
    fun applianceBoilerPost(@Body body: AppliancePostRequest) : Call<DefaultResponse>

    // 나의 보일러 삭제
    @DELETE("appliance/boiler")
    fun applianceBoilerDelete() : Call<DefaultResponse>

    // 나의 건조기 세부정보 호출
    @GET("appliance/dryer")
    fun applianceDryerGet(@Query("id") id: Int) : Call<ApplianceDetailResponse>

    // 나의 건조기 등록
    @POST("appliance/dryer")
    fun applianceDryerPost(@Body body: AppliancePostRequest) : Call<DefaultResponse>

    // 나의 건조기 삭제
    @DELETE("appliance/dryer")
    fun applianceDryerDelete() : Call<DefaultResponse>

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