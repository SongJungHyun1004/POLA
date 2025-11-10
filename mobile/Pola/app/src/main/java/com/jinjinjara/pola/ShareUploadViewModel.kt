package com.jinjinjara.pola

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.repository.FileUploadRepository
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ShareUploadState {
    object Idle : ShareUploadState()
    object Uploading : ShareUploadState()
    data class Success(val message: String) : ShareUploadState()
    data class Error(val message: String) : ShareUploadState()
}

@HiltViewModel
class ShareUploadViewModel @Inject constructor(
    private val fileUploadRepository: FileUploadRepository
) : ViewModel() {

    private val _uploadState = MutableStateFlow<ShareUploadState>(ShareUploadState.Idle)
    val uploadState: StateFlow<ShareUploadState> = _uploadState.asStateFlow()

    fun uploadImage(uri: Uri, contentType: String) {
        viewModelScope.launch {
            _uploadState.value = ShareUploadState.Uploading

            val fileName = "shared_${System.currentTimeMillis()}.png"

            when (val result = fileUploadRepository.uploadFile(uri, fileName, contentType)) {
                is Result.Success -> {
                    _uploadState.value = ShareUploadState.Success(result.data)
                }
                is Result.Error -> {
                    _uploadState.value = ShareUploadState.Error(
                        result.message ?: "업로드 실패"
                    )
                }
                is Result.Loading -> {
                    // Loading은 이미 Uploading 상태로 처리됨
                }
            }
        }
    }

    fun uploadText(text: String) {
        viewModelScope.launch {
            _uploadState.value = ShareUploadState.Uploading

            val fileName = "shared_${System.currentTimeMillis()}.txt"

            when (val result = fileUploadRepository.uploadText(text, fileName)) {
                is Result.Success -> {
                    _uploadState.value = ShareUploadState.Success(result.data)
                }
                is Result.Error -> {
                    _uploadState.value = ShareUploadState.Error(
                        result.message ?: "업로드 실패"
                    )
                }
                is Result.Loading -> {
                    // Loading은 이미 Uploading 상태로 처리됨
                }
            }
        }
    }

    fun resetState() {
        _uploadState.value = ShareUploadState.Idle
    }
}