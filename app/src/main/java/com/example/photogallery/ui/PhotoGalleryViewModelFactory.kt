package com.example.photogallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.photogallery.database.FavoriteRepository

class PhotoGalleryViewModelFactory(private val repository: FavoriteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhotoGalleryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PhotoGalleryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}