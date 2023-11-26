package com.toyproject.ecosave.api.responsemodels

import com.google.gson.annotations.SerializedName

data class DefaultResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)
