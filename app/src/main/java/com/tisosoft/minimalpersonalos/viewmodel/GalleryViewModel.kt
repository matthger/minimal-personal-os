package com.tisosoft.minimalpersonalos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tisosoft.minimalpersonalos.data.MediaRepository
import com.tisosoft.minimalpersonalos.model.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GalleryUiState(
    val images: List<MediaItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedImage: MediaItem? = null
)

class GalleryViewModel(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    fun loadImages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val media = repository.fetchMedia()
                _uiState.update { it.copy(images = media, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectImage(image: MediaItem?) {
        _uiState.update { it.copy(selectedImage = image) }
    }

    fun removeImageFromState(image: MediaItem) {
        _uiState.update { state ->
            state.copy(
                images = state.images.filter { it.id != image.id },
                selectedImage = if (state.selectedImage?.id == image.id) null else state.selectedImage
            )
        }
    }

    fun deleteImage(image: MediaItem) {
        viewModelScope.launch {
            val success = repository.deleteMedia(image)
            if (success) {
                removeImageFromState(image)
                loadImages()
            }
        }
    }
}
