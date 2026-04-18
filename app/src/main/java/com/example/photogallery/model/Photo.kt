package com.example.photogallery.model

data class Photo(
    val id: String = System.currentTimeMillis().toString() + (0..1000).random(),
    val prompt: String,
    val url: String
)