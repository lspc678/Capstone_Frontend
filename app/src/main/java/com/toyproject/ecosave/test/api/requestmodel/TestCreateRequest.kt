package com.toyproject.ecosave.test.api.requestmodel

import com.google.gson.annotations.SerializedName

data class TestCreateRequest(
    @SerializedName("name")
    val name: String?,
    @SerializedName("job")
    val job: String?
)
