package com.toyproject.ecosave.api

import com.toyproject.ecosave.api.responsemodels.EmailAuthenticationResponse

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface APIInterface {
    @GET("account/sign-up/certification")
    fun sendEmailAuthenticationRequest(
        @Query("mail") mail: String,
        @Query("code") code: String) : Call<EmailAuthenticationResponse>
}