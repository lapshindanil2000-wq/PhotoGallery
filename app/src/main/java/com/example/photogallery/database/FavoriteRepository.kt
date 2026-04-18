package com.example.photogallery.database

import kotlinx.coroutines.flow.Flow

class FavoriteRepository(private val dao: FavoritePhotoDao) {
    fun getAllFavorites(): Flow<List<FavoritePhoto>> = dao.getAllFavorites()

    suspend fun addToFavorites(photo: FavoritePhoto) = dao.insert(photo)

    suspend fun removeFromFavorites(photo: FavoritePhoto) = dao.delete(photo)

    suspend fun clearAll() = dao.deleteAll()
}