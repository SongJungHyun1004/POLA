package com.jinjinjara.pola.presentation.ui.screen.upload

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.repository.FileUploadRepository
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 업로드 상태
sealed class UploadScreenState {
    object Idle : UploadScreenState()
    object Uploading : UploadScreenState()
    data class Success(val message: String) : UploadScreenState()
    data class Error(val message: String) : UploadScreenState()
}

data class UploadUiState(
    val images: List<GalleryImage> = emptyList(),
    val selectedImage: GalleryImage? = null,
    val isLoading: Boolean = false,
    val uploadState: UploadScreenState = UploadScreenState.Idle
)

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val fileUploadRepository: FileUploadRepository
) : ViewModel() {

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

    fun selectImage(image: GalleryImage) {
        _uiState.update { state ->
            val updatedImages = state.images.map {
                it.copy(isSelected = it.id == image.id)
            }

            state.copy(
                images = updatedImages,
                selectedImage = image.copy(isSelected = true)
            )
        }
    }

    fun clearSelection() {
        _uiState.update { state ->
            val clearedImages = state.images.map { it.copy(isSelected = false) }
            state.copy(
                images = clearedImages,
                selectedImage = null
            )
        }
    }

    fun uploadSelectedImage(context: Context) {
        val selectedImage = _uiState.value.selectedImage ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(uploadState = UploadScreenState.Uploading) }

            val fileName = "upload_${System.currentTimeMillis()}.jpg"
            val contentType = context.contentResolver.getType(selectedImage.uri) ?: "image/jpeg"

            when (val result = fileUploadRepository.uploadFile(selectedImage.uri, fileName, contentType)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(uploadState = UploadScreenState.Success(result.data))
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(uploadState = UploadScreenState.Error(result.message ?: "업로드 실패"))
                    }
                }
                is Result.Loading -> {
                    // 이미 Uploading 상태
                }
            }
        }
    }

    fun resetUploadState() {
        _uiState.update { it.copy(uploadState = UploadScreenState.Idle) }
    }

    fun selectCameraImage(uri: Uri) {
        val newImage = GalleryImage(
            id = System.currentTimeMillis(),
            uri = uri,
            displayName = "Camera_${System.currentTimeMillis()}",
            dateAdded = System.currentTimeMillis(),
            isSelected = true
        )
        _uiState.update { state ->
            state.copy(
                selectedImage = newImage,
                images = listOf(newImage) + state.images // 맨 앞에 추가
            )
        }
    }

}