package com.jinjinjara.pola.presentation.ui.screen.contents

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.data.remote.dto.response.FileTag
import com.jinjinjara.pola.domain.repository.ContentRepository
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContentsEditUiState(
    val isLoading: Boolean = false,
    val tags: List<FileTag> = emptyList(),
    val contentText: String = "",
    val initialTags: List<FileTag> = emptyList(),
    val initialContentText: String = "",
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class ContentsEditViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContentsEditUiState())
    val uiState: StateFlow<ContentsEditUiState> = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadFileData(fileId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 파일 상세 정보 로드
            when (val fileResult = contentRepository.getFileDetail(fileId)) {
                is Result.Success -> {
                    val contentText = fileResult.data.context ?: ""

                    // 태그 정보 로드
                    when (val tagsResult = contentRepository.getFileTags(fileId)) {
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    tags = tagsResult.data,
                                    contentText = contentText,
                                    initialTags = tagsResult.data,
                                    initialContentText = contentText
                                )
                            }
                            Log.d("ContentsEdit", "Data loaded successfully")
                        }
                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = tagsResult.message
                                )
                            }
                            Log.e("ContentsEdit", "Failed to load tags: ${tagsResult.message}")
                        }
                        is Result.Loading -> {
                            // 이미 isLoading = true 상태
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = fileResult.message
                        )
                    }
                    Log.e("ContentsEdit", "Failed to load file: ${fileResult.message}")
                }
                is Result.Loading -> {
                    // 이미 isLoading = true 상태
                }
            }
        }
    }

    fun updateContentText(text: String) {
        _uiState.update { it.copy(contentText = text) }
    }

    fun addTags(newTagNames: List<String>) {
        val currentTags = _uiState.value.tags
        val newTags = newTagNames.map { tagName ->
            FileTag(
                id = -1L, // 임시 ID (서버에서 실제 ID 받아옴)
                tagName = tagName
            )
        }
        _uiState.update { it.copy(tags = currentTags + newTags) }
    }

    fun removeTag(tag: FileTag) {
        val updatedTags = _uiState.value.tags.filter { it.tagName != tag.tagName }
        _uiState.update { it.copy(tags = updatedTags) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveChanges(fileId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val currentState = _uiState.value
            val hasContentChanged = currentState.contentText != currentState.initialContentText
            val initialTagNames = currentState.initialTags.map { it.tagName }.toSet()
            val currentTagNames = currentState.tags.map { it.tagName }.toSet()

            // 추가된 태그
            val addedTagNames = currentTagNames - initialTagNames
            // 삭제된 태그
            val removedTags = currentState.initialTags.filter { it.tagName !in currentTagNames }

            var hasError = false

            // 1. context 업데이트 (변경된 경우에만)
            if (hasContentChanged) {
                when (val result = contentRepository.updateFileContext(fileId, currentState.contentText)) {
                    is Result.Success -> {
                        Log.d("ContentsEdit", "Context updated successfully")
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = result.message, isLoading = false) }
                        hasError = true
                        return@launch
                    }
                    is Result.Loading -> {
                        // 이미 isLoading = true 상태
                    }
                }
            }

            // 2. 태그 삭제
            for (tag in removedTags) {
                when (val result = contentRepository.removeFileTag(fileId, tag.id)) {
                    is Result.Success -> {
                        Log.d("ContentsEdit", "Tag ${tag.tagName} removed successfully")
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = result.message, isLoading = false) }
                        hasError = true
                        return@launch
                    }
                    is Result.Loading -> {
                        // 이미 isLoading = true 상태
                    }
                }
            }

            // 3. 태그 추가
            if (addedTagNames.isNotEmpty()) {
                when (val result = contentRepository.addFileTags(fileId, addedTagNames.toList())) {
                    is Result.Success -> {
                        Log.d("ContentsEdit", "Tags added successfully: $addedTagNames")
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = result.message, isLoading = false) }
                        hasError = true
                        return@launch
                    }
                    is Result.Loading -> {
                        // 이미 isLoading = true 상태
                    }
                }
            }

            if (!hasError) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        saveSuccess = true
                    )
                }
                Log.d("ContentsEdit", "All changes saved successfully")
                onSuccess()
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}