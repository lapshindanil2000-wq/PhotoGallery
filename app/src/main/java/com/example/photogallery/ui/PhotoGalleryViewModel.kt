package com.example.photogallery.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photogallery.network.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.math.min

sealed class PhotoGalleryUiState {
    object Loading : PhotoGalleryUiState()
    data class Success(val bitmaps: List<Bitmap>) : PhotoGalleryUiState()
    data class Error(val message: String) : PhotoGalleryUiState()
}

class PhotoGalleryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<PhotoGalleryUiState>(PhotoGalleryUiState.Loading)
    val uiState: StateFlow<PhotoGalleryUiState> = _uiState.asStateFlow()

    private val prompts = listOf(
        "a cute cat in space",
        "a beautiful sunset over mountains",
        "a futuristic city with flying cars",
        "a serene lake with autumn trees",
        "a robot painting in an art studio"
    )

    init {
        generateImages()
    }

    fun generateImages() {
        viewModelScope.launch {
            _uiState.value = PhotoGalleryUiState.Loading
            try {
                val bitmaps = mutableListOf<Bitmap>()
                for (prompt in prompts) {
                    val bitmap = generateImageWithRetry(prompt, maxRetries = 3)
                    bitmap?.let { bitmaps.add(it) }
                }
                if (bitmaps.isEmpty()) {
                    _uiState.value = PhotoGalleryUiState.Error("No images generated")
                } else {
                    _uiState.value = PhotoGalleryUiState.Success(bitmaps)
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
                // Если не удалось, пробуем ещё раз
                retries++
                if (retries <= maxRetries) {
                    delay(2000L) // пауза 2 секунды перед повтором
                }
            } catch (e: Exception) {
                e.printStackTrace()
                retries++
                if (retries <= maxRetries) {
                    delay(2000L)
                }
            }
        }
        return null
    }
}