package com.toyproject.ecosave.api.responsemodels

import com.google.gson.annotations.SerializedName

data class ApplianceDetailResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: ApplianceDetailData,
    @SerializedName("message")
    val message: String
)

data class ApplianceDetailData(
    @SerializedName("energy")
    val energy: Float,
    @SerializedName("co2")
    val co2: Float,
    @SerializedName("model_name")
    val model_name: String,
    @SerializedName("tier")
    val tier: Int,
    @SerializedName("relativePercent")
    val relativePercent: Int
)

data class BoilerDetailResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: BoilerDetailData,
    @SerializedName("message")
    val message: String
)

data class BoilerDetailData(
    @SerializedName("efficiency")
    val efficiency: Float,
    @SerializedName("gas_consumption")
    val gas_consumption: Float,
    @SerializedName("output")
    val output: Float,
    @SerializedName("model_name")
    val model_name: String,
    @SerializedName("tier")
    val tier: Int,
    @SerializedName("relativePercent")
    val relativePercent: Int
)