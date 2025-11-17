package com.jinjinjara.pola.presentation.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.model.HomeScreenData
import com.jinjinjara.pola.domain.usecase.home.GetHomeDataUseCase
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            getHomeDataUseCase().collect { result ->
                _uiState.value = when (result) {
                    is Result.Success -> HomeUiState.Success(result.data)
                    is Result.Error -> HomeUiState.Error(
                        result.message ?: "데이터를 불러올 수 없습니다"
                    )
                    is Result.Loading -> HomeUiState.Loading
                }
            }
        }
    }

    fun refresh() {
        loadHomeData()
    }

}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val data: HomeScreenData) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}