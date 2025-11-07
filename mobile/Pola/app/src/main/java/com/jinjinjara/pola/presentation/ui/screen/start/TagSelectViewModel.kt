package com.jinjinjara.pola.presentation.ui.screen.start

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.usecase.category.InitCategoryTagsUseCase
import com.jinjinjara.pola.util.ErrorType
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagSelectViewModel @Inject constructor(
    private val initCategoryTagsUseCase: InitCategoryTagsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TagSelectUiState>(TagSelectUiState.Idle)
    val uiState = _uiState.asStateFlow()

    /**
     * 선택된 태그를 서버로 전송
     * @param categoriesWithTags 카테고리별 전체 태그 정보
     * @param selectedTags 사용자가 선택한 태그 Set
     */
    fun submitSelectedTags(
        categoriesWithTags: Map<String, List<String>>,
        selectedTags: Set<String>
    ) {
        viewModelScope.launch {
            _uiState.value = TagSelectUiState.Loading
            Log.d("TagSelect:VM", "Submitting selected tags")
            Log.d("TagSelect:VM", "Total selected tags: ${selectedTags.size}")

            // 카테고리별로 선택된 태그만 필터링
            val filteredCategoriesWithTags = categoriesWithTags.mapNotNull { (categoryName, tags) ->
                val selectedTagsForCategory = tags.filter { selectedTags.contains(it) }
                if (selectedTagsForCategory.isNotEmpty()) {
                    categoryName to selectedTagsForCategory
                } else {
                    null
                }
            }.toMap()

            Log.d("TagSelect:VM", "Filtered categories: ${filteredCategoriesWithTags.keys}")
            filteredCategoriesWithTags.forEach { (categoryName, tags) ->
                Log.d("TagSelect:VM", "Category: $categoryName, Tags: $tags")
            }

            when (val result = initCategoryTagsUseCase(filteredCategoriesWithTags)) {
                is Result.Success -> {
                    Log.d("TagSelect:VM", "Successfully submitted tags")
                    _uiState.value = TagSelectUiState.Success
                }
                is Result.Error -> {
                    Log.e("TagSelect:VM", "Failed to submit tags: ${result.message}")
                    _uiState.value = TagSelectUiState.Error(
                        message = result.message ?: "태그 저장에 실패했습니다.",
                        errorType = result.errorType
                    )
                }
                else -> Unit
            }
        }
    }

    fun resetState() {
        _uiState.value = TagSelectUiState.Idle
    }

    fun retry(categoriesWithTags: Map<String, List<String>>, selectedTags: Set<String>) {
        submitSelectedTags(categoriesWithTags, selectedTags)
    }
}

sealed interface TagSelectUiState {
    object Idle : TagSelectUiState
    object Loading : TagSelectUiState
    object Success : TagSelectUiState
    data class Error(val message: String, val errorType: ErrorType) : TagSelectUiState
}