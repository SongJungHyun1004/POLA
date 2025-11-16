package com.jinjinjara.pola.presentation.ui.screen.my

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.model.CategoryRecommendation
import com.jinjinjara.pola.domain.model.Tag
import com.jinjinjara.pola.domain.usecase.category.GetUserCategoriesWithTagsUseCase
import com.jinjinjara.pola.domain.usecase.category.UpdateCategoryUseCase
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCategoryViewModel @Inject constructor(
    private val getUserCategoriesWithTagsUseCase: GetUserCategoriesWithTagsUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditCategoryUiState>(EditCategoryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    companion object {
        private const val KEY_SELECTED_CATEGORIES = "selected_categories"
    }

    // SavedStateHandle을 사용하여 상태 영속성 확보
    val selectedCategories = savedStateHandle.getStateFlow<Set<String>>(
        key = KEY_SELECTED_CATEGORIES,
        initialValue = emptySet()
    )

    // API에서 가져온 카테고리 정보 저장 (카테고리 이름 -> Tag 객체 리스트)
    private var categoriesMap = mapOf<String, List<Tag>>()

    // 카테고리 이름 -> ID 매핑 (업데이트 시 필요)
    private var categoryIdMap = mapOf<String, Long>()

    /**
     * 사용자의 기존 카테고리와 태그를 로드
     */
    fun loadUserCategoriesWithTags() {
        viewModelScope.launch {
            _uiState.value = EditCategoryUiState.Loading

            when (val result = getUserCategoriesWithTagsUseCase()) {
                is Result.Success -> {
                    Log.d("[Edit]", "User categories loaded: ${result.data.size}")

                    // 사용자 카테고리를 CategoryRecommendation 형식으로 변환
                    val categoryRecommendations = result.data.map { userCategory ->
                        CategoryRecommendation(
                            categoryName = userCategory.categoryName,
                            tags = userCategory.tags.map { it.name }
                        )
                    }

                    // 카테고리와 태그 정보 저장 (Tag 객체 전체)
                    categoriesMap = result.data.associate { userCategory ->
                        userCategory.categoryName to userCategory.tags
                    }

                    // 카테고리 ID 매핑 저장
                    categoryIdMap = result.data.associate {
                        it.categoryName to it.categoryId
                    }

                    // 모든 카테고리를 자동으로 선택
                    val allCategoryNames = categoryRecommendations.map { it.categoryName }.toSet()
                    savedStateHandle[KEY_SELECTED_CATEGORIES] = allCategoryNames

                    _uiState.value = EditCategoryUiState.Success(categoryRecommendations)
                }
                is Result.Error -> {
                    Log.e("[Edit]", "Failed to load user categories: ${result.message}")
                    _uiState.value = EditCategoryUiState.Error(result.message ?: "Unknown error")
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

    // DB에서 로드된 전체 카테고리 정보 반환 (초기 상태 비교용)
    fun getAllCategoriesWithTags(): Map<String, List<Tag>> {
        return categoriesMap
    }

    // 선택된 카테고리들의 정보를 반환 (카테고리 이름 -> Tag 객체 리스트, 커스텀 카테고리는 빈 리스트)
    fun getSelectedCategoriesWithTags(): Map<String, List<Tag>> {
        val selected = savedStateHandle.get<Set<String>>(KEY_SELECTED_CATEGORIES) ?: emptySet()
        return selected.associateWith { categoryName ->
            categoriesMap[categoryName] ?: emptyList() // 맵에 없으면 빈 리스트 (커스텀 카테고리)
        }
    }

    // 카테고리 ID 맵 반환
    fun getCategoryIdMap(): Map<String, Long> {
        return categoryIdMap
    }

    fun retry() {
        loadUserCategoriesWithTags()
    }

    /**
     * 카테고리 이름 수정
     */
    fun updateCategory(categoryName: String, newName: String) {
        viewModelScope.launch {
            val categoryId = categoryIdMap[categoryName]
            if (categoryId == null) {
                Log.e("[Edit]", "Category ID not found for: $categoryName")
                return@launch
            }

            when (val result = updateCategoryUseCase(categoryId, newName)) {
                is Result.Success -> {
                    Log.d("[Edit]", "Successfully updated category: $categoryName -> $newName")
                    // 페이지 새로고침
                    loadUserCategoriesWithTags()
                }
                is Result.Error -> {
                    Log.e("[Edit]", "Failed to update category: ${result.message}")
                    // TODO: 에러 처리 (토스트 메시지 등)
                }
                else -> Unit
            }
        }
    }

    /**
     * SavedStateHandle과 내부 상태를 완전히 초기화
     */
    fun clearSavedState() {
        savedStateHandle[KEY_SELECTED_CATEGORIES] = emptySet<String>()
        categoriesMap = emptyMap()
        categoryIdMap = emptyMap()
        Log.d("[Edit]", "Cleared all saved state in EditCategoryViewModel")
    }
}

sealed interface EditCategoryUiState {
    object Loading : EditCategoryUiState
    data class Success(val categories: List<CategoryRecommendation>) : EditCategoryUiState
    data class Error(val message: String) : EditCategoryUiState
}
