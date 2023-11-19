package com.toyproject.ecosave.api.responsemodels

import com.google.gson.annotations.SerializedName

data class SearchAddressResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("errorMessage")
    val errorMessage: String,
    @SerializedName("addresses")
    val addresses: List<AddressResponse>
)

data class AddressResponse(
    @SerializedName("roadAddress")
    val roadAddress: String,
    @SerializedName("jibunAddress")
    val jibunAddress: String,
    @SerializedName("x")
    val x: String,
    @SerializedName("y")
    val y: String
)