package com.jinjinjara.pola.presentation.ui.screen.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.model.TagSearchFile
import com.jinjinjara.pola.domain.usecase.search.GetAllSearchResultsUseCase
import com.jinjinjara.pola.domain.usecase.search.GetFilesByTagUseCase
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagViewModel @Inject constructor(
    private val getFilesByTagUseCase: GetFilesByTagUseCase,
    private val getAllSearchResultsUseCase: GetAllSearchResultsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TagUiState>(TagUiState.Loading)
    val uiState: StateFlow<TagUiState> = _uiState.asStateFlow()

    private val _files = MutableStateFlow<List<TagSearchFile>>(emptyList())
    val files: StateFlow<List<TagSearchFile>> = _files.asStateFlow()

    private val _sortOrder = MutableStateFlow("최신순")
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    fun loadFilesByTag(tagName: String) {
        viewModelScope.launch {
            _uiState.value = TagUiState.Loading

            when (val result = getFilesByTagUseCase(tagName)) {
                is Result.Success -> {
                    _files.value = result.data
                    _uiState.value = if (result.data.isEmpty()) {
                        TagUiState.Empty
                    } else {
                        TagUiState.Success
                    }
                }
                is Result.Error -> {
                    _uiState.value = TagUiState.Error(
                        result.message ?: "파일을 불러올 수 없습니다"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = TagUiState.Loading
                }
            }
        }
    }

    fun loadAllSearchResults(keyword: String) {
        viewModelScope.launch {
            _uiState.value = TagUiState.Loading

            when (val result = getAllSearchResultsUseCase(keyword)) {
                is Result.Success -> {
                    _files.value = result.data
                    _uiState.value = if (result.data.isEmpty()) {
                        TagUiState.Empty
                    } else {
                        TagUiState.Success
                    }
                }
                is Result.Error -> {
                    _uiState.value = TagUiState.Error(
                        result.message ?: "검색 결과를 불러올 수 없습니다"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = TagUiState.Loading
                }
            }
        }
    }

    fun setSortOrder(order: String) {
        _sortOrder.value = order
        sortFiles(order)
    }

    private fun sortFiles(order: String) {
        _files.value = when (order) {
            "최신순" -> _files.value.sortedByDescending { it.createdAt }
            "오래된순" -> _files.value.sortedBy { it.createdAt }
            else -> _files.value
        }
    }
}

sealed class TagUiState {
    data object Loading : TagUiState()
    data object Success : TagUiState()
    data object Empty : TagUiState()
    data class Error(val message: String) : TagUiState()
}
