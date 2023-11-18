package com.toyproject.ecosave.apis.responsemodel

import com.google.gson.annotations.SerializedName

data class EmailAuthenticationResponseBody(
    @SerializedName("success")
    val success: Boolean?,
    @SerializedName("message")
    val message: String?
)