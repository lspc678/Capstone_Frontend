package com.toyproject.ecosave.test.api.responsemodel

import com.google.gson.annotations.SerializedName

data class TestSingleUserResponse(
    @SerializedName("data")
    val data: TestUserData?,
    @SerializedName("support")
    val support: TestSupport?
)
