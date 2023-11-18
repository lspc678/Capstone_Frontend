package com.toyproject.ecosave.models

data class ReverseGeocodingResponse(
    val results: List<AreaResponse>
)

data class AreaResponse(
    val name: String,
    val region: RegionObject,
    val land: LandObject
)

data class RegionObject(
    val area1: AreaObject,
    val area2: AreaObject,
    val area3: AreaObject
)

data class AreaObject(
    val name: String
)

data class LandObject(
    val type: String,
    val number1: String,
    val number2: String,
    val name: String
)