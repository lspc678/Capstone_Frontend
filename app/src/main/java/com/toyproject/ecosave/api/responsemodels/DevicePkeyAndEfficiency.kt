package com.toyproject.ecosave.api.responsemodels

import com.google.gson.annotations.SerializedName

data class DevicePkeyAndEfficiency(
    @SerializedName("id")
    val id: Int,
    @SerializedName("efficiency")
    val efficiency: Double
)