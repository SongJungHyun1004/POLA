// presentation/viewmodel/MyTypeViewModel.kt
package com.jinjinjara.pola.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.model.Report
import com.jinjinjara.pola.domain.repository.ReportRepository
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MyTypeUiState {
    data object Loading : MyTypeUiState
    data class Success(val reports: List<Report>) : MyTypeUiState
    data class Error(val message: String) : MyTypeUiState
}

@HiltViewModel
class MyTypeViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyTypeUiState>(MyTypeUiState.Loading)
    val uiState: StateFlow<MyTypeUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            Log.d("MyType:VM", "Loading reports")
            _uiState.value = MyTypeUiState.Loading

            when (val result = reportRepository.getMyReports()) {
                is Result.Success -> {
                    Log.d("MyType:VM", "Successfully loaded ${result.data.size} reports")
                    _uiState.value = MyTypeUiState.Success(result.data)
                }
                is Result.Error -> {
                    Log.e("MyType:VM", "Failed to load reports: ${result.message}")
                    _uiState.value = MyTypeUiState.Error(
                        result.message ?: "알 수 없는 오류가 발생했습니다."
                    )
                }
                is Result.Loading -> {

                }
            }
        }
    }

    fun retry() {
        Log.d("MyType:VM", "Retrying to load reports")
        loadReports()
    }
}