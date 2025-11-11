package com.jinjinjara.pola.presentation.ui.screen.contents

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.model.FileDetail
import com.jinjinjara.pola.domain.repository.ContentRepository
import com.jinjinjara.pola.domain.usecase.favorite.ToggleFavoriteUseCase
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 파일 상세 화면 ViewModel
 */
@HiltViewModel
class ContentsViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContentsUiState>(ContentsUiState.Loading)
    val uiState: StateFlow<ContentsUiState> = _uiState.asStateFlow()
    private var currentFileId: Long? = null

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState.asStateFlow()

    /**
     * 파일 상세 정보 로드
     */
    fun loadFileDetail(fileId: Long) {
        viewModelScope.launch {
            _uiState.value = ContentsUiState.Loading
            currentFileId = fileId

            Log.d("ContentsVM", "Loading file detail for fileId: $fileId")

            when (val result = contentRepository.getFileDetail(fileId)) {
                is Result.Success -> {
                    Log.d("ContentsVM", "Successfully loaded file detail")
                    _uiState.value = ContentsUiState.Success(result.data)
                    _isBookmarked.value = result.data.favorite
                }
                is Result.Error -> {
                    Log.e("ContentsVM", "Failed to load file detail: ${result.message}")
                    _uiState.value = ContentsUiState.Error(result.message ?: "알 수 없는 오류")
                }
                is Result.Loading -> {
                    _uiState.value = ContentsUiState.Loading
                }
            }
        }
    }

    /**
     * 즐겨찾기 토글
     */
    fun toggleBookmark() {
        val fileId = currentFileId ?: return
        val newFavoriteState = !_isBookmarked.value

        // UI 즉시 반영 (낙관적 업데이트)
        _isBookmarked.value = newFavoriteState

        viewModelScope.launch {
            Log.d("ContentsVM", "Toggling favorite for fileId=$fileId, newState=$newFavoriteState")

            when (val result = toggleFavoriteUseCase(fileId, newFavoriteState)) {
                is Result.Success -> {
                    Log.d("ContentsVM", "Favorite toggle success: ${result.data}")
                    _isBookmarked.value = result.data // 서버 응답에 따라 최종 반영
                }
                is Result.Error -> {
                    Log.e("ContentsVM", "Favorite toggle failed: ${result.message}")
                    // 실패 시 UI 롤백
                    _isBookmarked.value = !newFavoriteState
                }
                else -> Unit
            }
        }
    }

    /**
     * 파일 삭제
     */
    fun deleteFile() {
        val fileId = currentFileId ?: return

        viewModelScope.launch {
            _deleteState.value = DeleteState.Loading

            when (val result = contentRepository.deleteFile(fileId)) {
                is Result.Success -> {
                    Log.d("ContentsVM", "File deleted successfully")
                    _deleteState.value = DeleteState.Success
                }
                is Result.Error -> {
                    Log.e("ContentsVM", "Failed to delete file: ${result.message}")
                    _deleteState.value = DeleteState.Error(result.message ?: "삭제 실패")
                }
                else -> Unit
            }
        }
    }

    fun resetDeleteState() {
        _deleteState.value = DeleteState.Idle
    }
}


/**
 * 파일 상세 화면 UI 상태
 */
sealed class ContentsUiState {
    object Loading : ContentsUiState()
    data class Success(val fileDetail: FileDetail) : ContentsUiState()
    data class Error(val message: String) : ContentsUiState()
}

sealed class DeleteState {
    object Idle : DeleteState()
    object Loading : DeleteState()
    object Success : DeleteState()
    data class Error(val message: String) : DeleteState()
}