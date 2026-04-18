package com.example.photogallery.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_photos")
data class FavoritePhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val prompt: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)