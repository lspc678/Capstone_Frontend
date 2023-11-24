package com.toyproject.ecosave.api

import com.toyproject.ecosave.App

import okhttp3.Interceptor
import okhttp3.Response

// HTTP 요청 전에 토큰이 있을 경우 헤더에 추가해 준다.
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requeset = chain.request().newBuilder()
            .addHeader("Authorization", App.prefs.token ?: "")
            .build()

        return chain.proceed(requeset)
    }
}