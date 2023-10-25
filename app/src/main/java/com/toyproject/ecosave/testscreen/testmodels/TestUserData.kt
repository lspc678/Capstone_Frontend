package com.toyproject.ecosave.testscreen.testmodels

class TestUserData (val data : TestDataClass) {
    data class TestDataClass(val first_name: String, val last_name: String, val id: String, val email: String, val avatar: String)
    data class TestSupportClass(val url: String, val text: String)
}