package com.toyproject.ecosave.api.responsemodels

import com.google.gson.annotations.SerializedName

data class DevicePkeyAndEnergy(
    @SerializedName("id")
    val id: Int,
    @SerializedName("energy")
    val energy: Double
)