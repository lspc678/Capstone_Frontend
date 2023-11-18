package com.toyproject.ecosave.test.api.responsemodel

import com.google.gson.annotations.SerializedName

data class TestListUsersResponse(
    @SerializedName("page")
    val page: Int?,
    @SerializedName("per_page")
    val per_page: Int?,
    @SerializedName("total")
    val total: Int?,
    @SerializedName("total_pages")
    val total_pages: Int?,
    @SerializedName("data")
    val data: ArrayList<TestUserData>?,
    @SerializedName("support")
    val support: TestSupport?
)
