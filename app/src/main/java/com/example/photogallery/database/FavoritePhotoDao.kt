package com.example.photogallery.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePhotoDao {
    @Insert
    suspend fun insert(photo: FavoritePhoto)

    @Delete
    suspend fun delete(photo: FavoritePhoto)

    @Query("SELECT * FROM favorite_photos ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoritePhoto>>

    @Query("DELETE FROM favorite_photos")
    suspend fun deleteAll()
}