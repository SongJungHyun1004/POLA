package com.jinjinjara.pola.presentation.ui.screen.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.model.FileItem
import com.jinjinjara.pola.domain.usecase.category.GetFilesByCategoryUseCase
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getFilesByCategoryUseCase: GetFilesByCategoryUseCase
) : ViewModel() {

    private val categoryId: Long = savedStateHandle.get<Long>("categoryId") ?: -1L

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    data class UiState(
        val isLoading: Boolean = false,
        val categoryName: String = "",
        val files: List<FileItem> = emptyList(),
        val errorMessage: String? = null,
        val currentPage: Int = 0,
        val hasMorePages: Boolean = true
    )

    init {
        loadCategoryFiles()
    }

    private fun loadCategoryFiles(page: Int = 0) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = getFilesByCategoryUseCase(categoryId, page)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            files = if (page == 0) result.data.content else it.files + result.data.content,
                            currentPage = page,
                            hasMorePages = !result.data.last,
                            errorMessage = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun loadMoreFiles() {
        if (!_uiState.value.isLoading && _uiState.value.hasMorePages) {
            loadCategoryFiles(_uiState.value.currentPage + 1)
        }
    }

    fun refresh() {
        loadCategoryFiles(0)
    }
}