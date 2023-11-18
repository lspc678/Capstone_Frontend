package com.toyproject.ecosave.test.api.requestmodel

import com.google.gson.annotations.SerializedName

data class TestLoginRequest(
    @SerializedName("email")
    val email: String?,
    @SerializedName("password")
    val password: String?
)