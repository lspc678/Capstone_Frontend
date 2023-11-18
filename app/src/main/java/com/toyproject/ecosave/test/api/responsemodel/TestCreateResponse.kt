package com.toyproject.ecosave.test.api.responsemodel

import com.google.gson.annotations.SerializedName

data class TestCreateResponse(
    @SerializedName("name")
    val name: String?,
    @SerializedName("job")
    val job: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("createdAt")
    val createdAt: String?,
)
