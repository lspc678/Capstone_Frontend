package com.toyproject.ecosave.test.api

import com.toyproject.ecosave.test.api.requestmodel.TestCreateRequest
import com.toyproject.ecosave.test.api.requestmodel.TestLoginRequest
import com.toyproject.ecosave.test.api.responsemodel.TestCreateResponse
import com.toyproject.ecosave.test.api.responsemodel.TestListUsersResponse
import com.toyproject.ecosave.test.api.responsemodel.TestLoginResponse
import com.toyproject.ecosave.test.api.responsemodel.TestSingleUserResponse

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface TestAPIInterface {
    @GET("api/users")
    fun listUsers(@Query("page") page: Int) : Call<TestListUsersResponse>

    @GET("api/users/1")
    fun singleUser() : Call<TestSingleUserResponse>

    @GET("api/users/23")
    fun singleUserNotFound() : Call<TestSingleUserResponse>

    @Headers("Content-Type: application/json")
    @POST("api/users")
    fun create(@Body body: TestCreateRequest) : Call<TestCreateResponse>

    @Headers("Content-Type: application/json")
    @POST("api/login")
    fun login(@Body body: TestLoginRequest) : Call<TestLoginResponse>
}