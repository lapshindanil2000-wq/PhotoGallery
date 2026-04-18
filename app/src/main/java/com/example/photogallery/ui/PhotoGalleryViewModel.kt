package com.example.photogallery.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photogallery.database.FavoritePhoto
import com.example.photogallery.database.FavoriteRepository
import com.example.photogallery.model.Photo
import com.example.photogallery.network.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PhotoGalleryUiState {
    object Loading : PhotoGalleryUiState()
    data class Success(val photos: List<Photo>) : PhotoGalleryUiState()
    data class Error(val message: String) : PhotoGalleryUiState()
}

class PhotoGalleryViewModel(
    private val repository: FavoriteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<PhotoGalleryUiState>(PhotoGalleryUiState.Loading)
    val uiState: StateFlow<PhotoGalleryUiState> = _uiState.asStateFlow()

    private val _favorites = MutableStateFlow<List<FavoritePhoto>>(emptyList())
    val favorites: StateFlow<List<FavoritePhoto>> = _favorites.asStateFlow()

    private val prompts = listOf(
        "a cute cat in space",
        "a beautiful sunset over mountains",
        "a futuristic city with flying cars",
        "a serene lake with autumn trees",
        "a robot painting in an art studio"
    )

    init {
        generateImages()
        loadFavorites()
    }

    fun generateImages() {
        viewModelScope.launch {
            _uiState.value = PhotoGalleryUiState.Loading
            try {
                val photos = mutableListOf<Photo>()
                for (prompt in prompts) {
                    val bitmap = generateImageWithRetry(prompt, maxRetries = 3)
                    if (bitmap != null) {
                        val url = "https://image.pollinations.ai/image/${prompt.replace(" ", "%20")}?width=512&height=512"
                        photos.add(Photo(prompt = prompt, url = url))
                    }
                }
                if (photos.isEmpty()) {
                    _uiState.value = PhotoGalleryUiState.Error("No images generated")
                } else {
                    _uiState.value = PhotoGalleryUiState.Success(photos)
                }
            } catch (e: Exception) {
                _uiState.value = PhotoGalleryUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun generateImageWithRetry(prompt: String, maxRetries: Int): Bitmap? {
        var retries = 0
        while (retries <= maxRetries) {
            try {
                val response = RetrofitInstance.api.generateImage(prompt, 512, 512)
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        val bitmap = BitmapFactory.decodeStream(responseBody.byteStream())
                        if (bitmap != null) return bitmap
                    }
                }
                retries++
                if (retries <= maxRetries) delay(2000L)
            } catch (e: Exception) {
                retries++
                if (retries <= maxRetries) delay(2000L)
            }
        }
        return null
    }

    fun saveToFavorites(photo: Photo) {
        viewModelScope.launch {
            val favorite = FavoritePhoto(
                prompt = photo.prompt,
                url = photo.url
            )
            repository.addToFavorites(favorite)
            loadFavorites()
        }
    }

    fun removeFromFavorites(favorite: FavoritePhoto) {
        viewModelScope.launch {
            repository.removeFromFavorites(favorite)
            loadFavorites()
        }
    }

    fun clearAllFavorites() {
        viewModelScope.launch {
            repository.clearAll()
            loadFavorites()
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            repository.getAllFavorites().collect { list ->
                _favorites.value = list
            }
        }
    }
}