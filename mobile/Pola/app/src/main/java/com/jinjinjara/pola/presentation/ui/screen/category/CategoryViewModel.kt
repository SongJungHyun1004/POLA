package com.jinjinjara.pola.presentation.ui.screen.category

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.model.FileItem
import com.jinjinjara.pola.domain.model.UserCategory
import com.jinjinjara.pola.domain.usecase.category.GetFilesByCategoryUseCase
import com.jinjinjara.pola.domain.usecase.category.GetUserCategoriesUseCase
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
    private val getFilesByCategoryUseCase: GetFilesByCategoryUseCase,
    private val getUserCategoriesUseCase: GetUserCategoriesUseCase
) : ViewModel() {

    private val categoryId: Long = savedStateHandle.get<Long>("categoryId") ?: -1L

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    data class UiState(
        val isLoading: Boolean = false,
        val categoryId: Long = -1L,
        val categoryName: String = "",
        val files: List<FileItem> = emptyList(),
        val userCategories: List<UserCategory> = emptyList(),
        val errorMessage: String? = null,
        val currentPage: Int = 0,
        val hasMorePages: Boolean = true,
        val sortBy: String = "createdAt",
        val direction: String = "DESC"
    )

    init {
        _uiState.update { it.copy(categoryId = savedStateHandle.get<Long>("categoryId") ?: -1L) }
        loadUserCategories()
        loadCategoryFiles()
    }

    private fun loadUserCategories() {
        viewModelScope.launch {
            when (val result = getUserCategoriesUseCase()) {
                is Result.Success -> {
                    val currentCategory = result.data.find { it.id == categoryId }
                    _uiState.update {
                        it.copy(
                            userCategories = result.data,
                            categoryName = currentCategory?.categoryName ?: ""
                        )
                    }
                    Log.d("Category:VM", "Loaded ${result.data.size} categories")
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    Log.e("Category:VM", "Failed to load categories: ${result.message}")
                }

                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun loadCategoryFiles(
        page: Int = 0,
        targetCategoryId: Long = _uiState.value.categoryId,
        sortBy: String = _uiState.value.sortBy,
        direction: String = _uiState.value.direction
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = getFilesByCategoryUseCase(targetCategoryId, page, 20, sortBy, direction)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            categoryName = result.data.fileName,
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

    fun updateSort(sortBy: String, direction: String) {
        _uiState.update { it.copy(sortBy = sortBy, direction = direction) }
        loadCategoryFiles(0, sortBy = sortBy, direction = direction)
    }

    fun selectCategory(newCategoryId: Long) {
        _uiState.update { it.copy(categoryId = newCategoryId) }
        loadCategoryFiles(0, targetCategoryId = newCategoryId)
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