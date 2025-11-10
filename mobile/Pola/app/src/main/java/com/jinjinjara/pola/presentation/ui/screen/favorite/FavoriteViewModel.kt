package com.jinjinjara.pola.presentation.ui.screen.favorite

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.model.FavoriteData
import com.jinjinjara.pola.domain.usecase.favorite.GetFavoriteListUseCase
import com.jinjinjara.pola.domain.usecase.favorite.ToggleFavoriteUseCase
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val getFavoriteListUseCase: GetFavoriteListUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoriteUiState>(FavoriteUiState.Loading)
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            Log.d(TAG, "loadFavorites() 시작")
            _uiState.value = FavoriteUiState.Loading

            when (val result = getFavoriteListUseCase()) {
                is Result.Success -> {
                    Log.d(TAG, "UseCase Success - 데이터 개수: ${result.data.size}")
                    result.data.forEachIndexed { index, favoriteData ->
                        Log.d(TAG, "  [$index] id: ${favoriteData.id}, imageUrl: ${favoriteData.imageUrl}")
                    }
                    _uiState.value = FavoriteUiState.Success(result.data)
                }
                is Result.Error -> {
                    Log.e(TAG, "UseCase Error - message: ${result.message}")
                    _uiState.value = FavoriteUiState.Error(
                        result.message ?: "즐겨찾기 목록을 불러올 수 없습니다"
                    )
                }
                is Result.Loading -> {
                    Log.d(TAG, "UseCase Loading")
                    _uiState.value = FavoriteUiState.Loading
                }
            }
        }
    }

    fun toggleFavorite(fileId: Long) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is FavoriteUiState.Success) return@launch

            val favoriteData = currentState.data.find { it.fileId == fileId } ?: return@launch
            val newFavoriteState = !favoriteData.isFavorite

            Log.d(TAG, "toggleFavorite - fileId: $fileId, newState: $newFavoriteState")

            // Optimistic update: UI 즉시 업데이트
            val updatedList = currentState.data.map { item ->
                if (item.fileId == fileId) {
                    item.copy(isFavorite = newFavoriteState)
                } else {
                    item
                }
            }
            _uiState.value = FavoriteUiState.Success(updatedList)

            // API 호출
            when (val result = toggleFavoriteUseCase(fileId, newFavoriteState)) {
                is Result.Success -> {
                    Log.d(TAG, "즐겨찾기 토글 성공 - favorite: ${result.data}")
                    // 성공 시 그대로 유지 (이미 UI 업데이트 완료)
                }
                is Result.Error -> {
                    Log.e(TAG, "즐겨찾기 토글 실패 - message: ${result.message}")
                    // 실패 시 원래 상태로 복원
                    _uiState.value = currentState
                    // 에러 토스트 이벤트 발생
                    _errorEvent.emit(result.message ?: "즐겨찾기 상태를 변경할 수 없습니다")
                }
                is Result.Loading -> {
                    Log.d(TAG, "즐겨찾기 토글 로딩")
                }
            }
        }
    }

    companion object {
        private const val TAG = "FavoriteViewModel"
    }
}

sealed class FavoriteUiState {
    data object Loading : FavoriteUiState()
    data class Success(val data: List<FavoriteData>) : FavoriteUiState()
    data class Error(val message: String) : FavoriteUiState()
}
