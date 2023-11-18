package com.toyproject.ecosave.api.responsemodels

data class SearchAddressResponse(
    val status: String,
    val errorMessage: String,
    val addresses: List<AddressResponse>
)

data class AddressResponse(
    val roadAddress: String,
    val jibunAddress: String,
    val x: String,
    val y: String
)