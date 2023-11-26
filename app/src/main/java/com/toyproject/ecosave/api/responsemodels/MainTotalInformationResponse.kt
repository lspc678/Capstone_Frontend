package com.toyproject.ecosave.api.responsemodels

import com.google.gson.annotations.SerializedName

data class MainTotalInformationResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: DeviceData,
    @SerializedName("message")
    val message: String
)

data class DeviceData (
    @SerializedName("refrigerator")
    val refrigerator: ArrayList<DevicePkeyAndEnergy>?,
    @SerializedName("air_conditioner")
    val air_conditioner: ArrayList<DevicePkeyAndEnergy>?,
    @SerializedName("television")
    val television: ArrayList<DevicePkeyAndEnergy>?,
    @SerializedName("washing_machine")
    val washing_machine: ArrayList<DevicePkeyAndEnergy>?,
    @SerializedName("microwave")
    val microwave: ArrayList<DevicePkeyAndEnergy>?,
    @SerializedName("boiler")
    val boiler: ArrayList<DevicePkeyAndEfficiency>?
)