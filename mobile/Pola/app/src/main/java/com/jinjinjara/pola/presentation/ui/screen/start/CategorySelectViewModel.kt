package com.jinjinjara.pola.presentation.ui.screen.start

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.model.CategoryRecommendation
import com.jinjinjara.pola.domain.usecase.category.GetCategoryRecommendationsUseCase
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategorySelectViewModel @Inject constructor(
    private val getCategoryRecommendationsUseCase: GetCategoryRecommendationsUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoryUiState>(CategoryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    // SavedStateHandle을 사용하여 상태 영속성 확보
    val selectedCategories = savedStateHandle.getStateFlow<Set<String>>(
        key = KEY_SELECTED_CATEGORIES,
        initialValue = emptySet()
    )

    // API에서 가져온 카테고리 정보 저장 (카테고리 이름 -> 태그 리스트)
    private var apiCategoriesMap = mapOf<String, List<String>>()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = CategoryUiState.Loading

            when (val result = getCategoryRecommendationsUseCase()) {
                is Result.Success -> {
                    Log.d("Category:VM", "Categories loaded: ${result.data.size}")
                    // API 카테고리와 태그 정보 저장
                    apiCategoriesMap = result.data.associate { it.categoryName to it.tags }
                    _uiState.value = CategoryUiState.Success(result.data)
                }
                is Result.Error -> {
                    Log.e("Category:VM", "Failed to load categories: ${result.message}")
                    _uiState.value = CategoryUiState.Error(result.message ?: "Unknown error")
                }
                else -> Unit
            }
        }
    }

    fun toggleCategory(categoryName: String) {
        val current = savedStateHandle.get<Set<String>>(KEY_SELECTED_CATEGORIES) ?: emptySet()
        savedStateHandle[KEY_SELECTED_CATEGORIES] = if (current.contains(categoryName)) {
            current - categoryName
        } else {
            current + categoryName
        }
    }

    // 선택된 카테고리들의 정보를 반환 (카테고리 이름 -> 태그 리스트, 커스텀 카테고리는 빈 리스트)
    fun getSelectedCategoriesWithTags(): Map<String, List<String>> {
        val selected = savedStateHandle.get<Set<String>>(KEY_SELECTED_CATEGORIES) ?: emptySet()
        return selected.associateWith { categoryName ->
            apiCategoriesMap[categoryName] ?: emptyList() // API에 없으면 빈 리스트 (커스텀 카테고리)
        }
    }

    companion object {
        private const val KEY_SELECTED_CATEGORIES = "selected_categories"
    }

    fun retry() {
        loadCategories()
    }
}

sealed interface CategoryUiState {
    object Loading : CategoryUiState
    data class Success(val categories: List<CategoryRecommendation>) : CategoryUiState
    data class Error(val message: String) : CategoryUiState
}