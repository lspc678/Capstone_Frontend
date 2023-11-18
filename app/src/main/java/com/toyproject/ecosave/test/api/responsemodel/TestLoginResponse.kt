package com.toyproject.ecosave.test.api.responsemodel

import com.google.gson.annotations.SerializedName

data class TestLoginResponse(
    @SerializedName("token")
    val token: String?
)
