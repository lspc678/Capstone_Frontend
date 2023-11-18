package com.toyproject.ecosave.test.api.responsemodel

import com.google.gson.annotations.SerializedName

data class TestSupport(
    @SerializedName("url")
    val url: String?,
    @SerializedName("text")
    val text: String?
)
