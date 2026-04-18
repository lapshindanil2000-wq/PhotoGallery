package com.example.photogallery.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PollinationApiService {
    @GET("image/{prompt}")
    suspend fun generateImage(
        @Path("prompt") prompt: String,
        @Query("width") width: Int = 512,
        @Query("height") height: Int = 512
    ): Response<ResponseBody>
}