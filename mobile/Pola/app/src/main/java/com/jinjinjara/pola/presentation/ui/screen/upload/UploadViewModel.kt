package com.jinjinjara.pola.presentation.ui.screen.upload

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UploadUiState(
    val images: List<GalleryImage> = emptyList(),
    val selectedImages: List<GalleryImage> = emptyList(),
    val isLoading: Boolean = false
)

class UploadViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun loadGalleryImages(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            val images = mutableListOf<GalleryImage>()
            val contentResolver: ContentResolver = context.contentResolver

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
            )

            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val dateAdded = cursor.getLong(dateColumn)

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    images.add(
                        GalleryImage(
                            id = id,
                            uri = contentUri,
                            displayName = name,
                            dateAdded = dateAdded
                        )
                    )
                }
            }

            _uiState.update {
                it.copy(
                    images = images,
                    isLoading = false
                )
            }
        }
    }

    fun toggleImageSelection(image: GalleryImage) {
        _uiState.update { state ->
            val updatedImages = state.images.map {
                if (it.id == image.id) {
                    it.copy(isSelected = !it.isSelected)
                } else {
                    it
                }
            }

            val selectedImages = updatedImages.filter { it.isSelected }

            state.copy(
                images = updatedImages,
                selectedImages = selectedImages
            )
        }
    }

    fun clearSelection() {
        _uiState.update { state ->
            val clearedImages = state.images.map { it.copy(isSelected = false) }
            state.copy(
                images = clearedImages,
                selectedImages = emptyList()
            )
        }
    }
}