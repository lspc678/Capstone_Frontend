package com.toyproject.ecosave.api.requestmodels

import com.google.gson.annotations.SerializedName

data class ChangeMyResidenceRequest(
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("latitude")
    val latitude: Double
)
