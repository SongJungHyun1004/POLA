package com.jinjinjara.pola.presentation.ui.screen.contents

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.model.FileDetail
import com.jinjinjara.pola.domain.repository.CategoryRepository
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 파일 상세 화면 ViewModel
 */
@HiltViewModel
class ContentsViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContentsUiState>(ContentsUiState.Loading)
    val uiState: StateFlow<ContentsUiState> = _uiState.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    /**
     * 파일 상세 정보 로드
     */
    fun loadFileDetail(fileId: Long) {
        viewModelScope.launch {
            _uiState.value = ContentsUiState.Loading

            Log.d("ContentsVM", "Loading file detail for fileId: $fileId")

            when (val result = categoryRepository.getFileDetail(fileId)) {
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
        _isBookmarked.value = !_isBookmarked.value
        // TODO: API 연동하여 서버에 즐겨찾기 상태 업데이트
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