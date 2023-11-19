package com.toyproject.ecosave.api.responsemodels

import com.google.gson.annotations.SerializedName

data class ReverseGeocodingResponse(
    @SerializedName("results")
    val results: List<AreaResponse>
)

data class AreaResponse(
    @SerializedName("name")
    val name: String,
    @SerializedName("region")
    val region: RegionObject,
    @SerializedName("land")
    val land: LandObject
)

data class RegionObject(
    @SerializedName("area1")
    val area1: AreaObject,
    @SerializedName("area2")
    val area2: AreaObject,
    @SerializedName("area3")
    val area3: AreaObject
)

data class AreaObject(
    @SerializedName("name")
    val name: String
)

data class LandObject(
    @SerializedName("type")
    val type: String,
    @SerializedName("number1")
    val number1: String,
    @SerializedName("number2")
    val number2: String,
    @SerializedName("name")
    val name: String
)