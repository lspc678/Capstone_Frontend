package com.toyproject.ecosave.api.requestmodels

import com.google.gson.annotations.SerializedName

data class AppliancePostRequest(
    @SerializedName("energy")
    val energy: Double,
    @SerializedName("co2")
    val co2: Double,
    @SerializedName("modelname")
    val modelname: String
)
