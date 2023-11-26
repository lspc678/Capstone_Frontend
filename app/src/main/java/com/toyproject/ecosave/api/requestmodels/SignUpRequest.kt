package com.toyproject.ecosave.api.requestmodels

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    @SerializedName("mail")
    val mail: String,
    @SerializedName("pw1")
    val pw1: String,
    @SerializedName("pw2")
    val pw2: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("latitude")
    val latitude: Double
)
