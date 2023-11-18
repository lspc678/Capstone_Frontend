package com.toyproject.ecosave.models

import com.google.gson.annotations.SerializedName

data class EmailAuthenticationResponseBody(
    @SerializedName("success")
    val success: Boolean?,
    @SerializedName("message")
    val message: String?
)