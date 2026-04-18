package com.example.photogallery.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "https://image.pollinations.ai/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(120, TimeUnit.SECONDS)   // было 30, увеличили до 120
        .readTimeout(120, TimeUnit.SECONDS)      // было 30
        .writeTimeout(120, TimeUnit.SECONDS)     // добавили
        .retryOnConnectionFailure(true)          // автоматические повторные попытки
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .build()
    }

    val api: PollinationApiService by lazy {
        retrofit.create(PollinationApiService::class.java)
    }
}