package com.toyproject.ecosave.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APIClientForServer {
    companion object {
        // 서버 주소
        private const val BASE_URL = "http://13.125.24.196:8000/"

        fun getClient() : Retrofit {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val headerInterceptor = Interceptor {
                val request = it.request()
                    .newBuilder()
                    .build()
                return@Interceptor it.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}

// https 통신을 할 때 SSL 인증서(certificate) 검사를 우회함
class APIClientForServerByPassSSLCertificate {
    companion object {
        // 서버 주소
        private const val BASE_URL = "http://13.125.24.196:8000/"

        fun getClient() : Retrofit {
            // getUnsafeOkHttpClient()를 이용하여 SSL 인증서 검사를 우회함
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getUnsafeOkHttpClient().build())
                .build()

            Log.d("홈 화면 (APIClient)", "retrofit 생성 완료")

            return retrofit
        }
    }
}

class APIClientForNaverMap {
    companion object {
        // 서버 주소
        private const val BASE_URL = "https://naveropenapi.apigw.ntruss.com/"

        fun getClient() : Retrofit {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val headerInterceptor = Interceptor {
                val request = it.request()
                    .newBuilder()
                    .build()
                return@Interceptor it.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}