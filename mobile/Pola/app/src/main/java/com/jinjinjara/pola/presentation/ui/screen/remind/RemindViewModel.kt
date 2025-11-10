package com.jinjinjara.pola.presentation.ui.screen.remind

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.model.RemindData
import com.jinjinjara.pola.domain.usecase.favorite.ToggleFavoriteUseCase
import com.jinjinjara.pola.domain.usecase.remind.GetRemindersUseCase
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
class RemindViewModel @Inject constructor(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RemindUiState>(RemindUiState.Loading)
    val uiState: StateFlow<RemindUiState> = _uiState.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    init {
        loadReminders()
    }

    fun loadReminders() {
        viewModelScope.launch {
            Log.d(TAG, "loadReminders() 시작")
            _uiState.value = RemindUiState.Loading

            when (val result = getRemindersUseCase()) {
                is Result.Success -> {
                    Log.d(TAG, "UseCase Success - 데이터 개수: ${result.data.size}")
                    result.data.forEachIndexed { index, remindData ->
                        Log.d(TAG, "  [$index] id: ${remindData.id}, imageUrl: ${remindData.imageUrl}")
                    }
                    _uiState.value = RemindUiState.Success(result.data)
                }
                is Result.Error -> {
                    Log.e(TAG, "UseCase Error - message: ${result.message}")
                    _uiState.value = RemindUiState.Error(
                        result.message ?: "리마인드 데이터를 불러올 수 없습니다"
                    )
                }
                is Result.Loading -> {
                    Log.d(TAG, "UseCase Loading")
                    _uiState.value = RemindUiState.Loading
                }
            }
        }
    }

    fun toggleFavorite(index: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is RemindUiState.Success) return@launch

            val remindData = currentState.data.getOrNull(index) ?: return@launch
            val newFavoriteState = !remindData.isFavorite

            Log.d(TAG, "toggleFavorite - index: $index, fileId: ${remindData.id}, newState: $newFavoriteState")

            // Optimistic update: UI 즉시 업데이트
            val updatedList = currentState.data.toMutableList()
            updatedList[index] = remindData.copy(isFavorite = newFavoriteState)
            _uiState.value = RemindUiState.Success(updatedList)

            // API 호출
            when (val result = toggleFavoriteUseCase(remindData.id, newFavoriteState)) {
                is Result.Success -> {
                    Log.d(TAG, "즐겨찾기 토글 성공 - favorite: ${result.data}")
                    // 성공 시 그대로 유지 (이미 UI 업데이트 완료)
                }
                is Result.Error -> {
                    Log.e(TAG, "즐겨찾기 토글 실패 - message: ${result.message}")
                    // 실패 시 원래 상태로 복원
                    val revertedList = currentState.data.toMutableList()
                    revertedList[index] = remindData
                    _uiState.value = RemindUiState.Success(revertedList)
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
        private const val TAG = "RemindViewModel"
    }
}

sealed class RemindUiState {
    data object Loading : RemindUiState()
    data class Success(val data: List<RemindData>) : RemindUiState()
    data class Error(val message: String) : RemindUiState()
}
