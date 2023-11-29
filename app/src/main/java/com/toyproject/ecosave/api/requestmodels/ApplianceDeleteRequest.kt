package com.toyproject.ecosave.api.requestmodels

import com.google.gson.annotations.SerializedName

data class ApplianceDeleteRequest(
    @SerializedName("id")
    val id: Int
)
