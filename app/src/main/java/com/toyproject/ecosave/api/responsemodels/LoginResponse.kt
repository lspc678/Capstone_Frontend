package com.toyproject.ecosave.api.responsemodels

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("token")
    val token: String?,
)
