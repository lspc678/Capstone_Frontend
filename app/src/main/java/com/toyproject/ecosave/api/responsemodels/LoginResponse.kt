package com.toyproject.ecosave.models

import com.google.gson.annotations.SerializedName

data class LoginResponseBody(
    @SerializedName("success")
    val success: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("token")
    val token: String?,
)
