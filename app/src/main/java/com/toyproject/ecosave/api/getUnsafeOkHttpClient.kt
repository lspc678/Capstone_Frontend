package com.toyproject.ecosave.api

import android.annotation.SuppressLint

import okhttp3.OkHttpClient

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

// SSL 인증서(certificate) 검사를 우회하는 함수
fun getUnsafeOkHttpClient() : OkHttpClient.Builder {
    val trustAllCerts = arrayOf<TrustManager>(
        @SuppressLint("CustomX509TrustManager")
        object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
    )

    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())

    val sslSocketFactory = sslContext.socketFactory

    val builder = OkHttpClient.Builder()
    builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
    builder.hostnameVerifier { hostname, session -> true }

    return builder
}