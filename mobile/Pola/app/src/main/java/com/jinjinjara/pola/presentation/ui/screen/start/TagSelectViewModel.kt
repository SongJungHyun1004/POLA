package com.jinjinjara.pola.presentation.ui.screen.start

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.jinjinjara.pola.domain.model.Tag
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
    private val initCategoryTagsUseCase: InitCategoryTagsUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<TagSelectUiState>(TagSelectUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // SavedStateHandle을 사용하여 선택된 태그와 커스텀 태그 영속화
    companion object {
        private const val KEY_SELECTED_TAG_IDS = "selected_tag_ids"
        private const val KEY_CUSTOM_TAGS = "custom_tags"
    }

    // 선택된 태그 ID 리스트
    val selectedTagIds = savedStateHandle.getStateFlow<List<Long>>(
        key = KEY_SELECTED_TAG_IDS,
        initialValue = emptyList()
    )

    // 커스텀 태그 맵
    val customTagsMap = savedStateHandle.getStateFlow<Map<String, List<String>>>(
        key = KEY_CUSTOM_TAGS,
        initialValue = emptyMap()
    )

    fun toggleTag(tagId: Long) {
        val currentTagIds = selectedTagIds.value.toSet()
        val newTagIds = if (currentTagIds.contains(tagId)) {
            currentTagIds - tagId
        } else {
            currentTagIds + tagId
        }
        savedStateHandle[KEY_SELECTED_TAG_IDS] = newTagIds.toList()
    }

    fun selectAllTagsInCategory(tagIds: List<Long>) {
        val currentTagIds = selectedTagIds.value.toSet()
        val newTagIds = currentTagIds + tagIds.toSet()
        savedStateHandle[KEY_SELECTED_TAG_IDS] = newTagIds.toList()
    }

    fun deselectAllTagsInCategory(tagIds: List<Long>) {
        val currentTagIds = selectedTagIds.value.toSet()
        val newTagIds = currentTagIds - tagIds.toSet()
        savedStateHandle[KEY_SELECTED_TAG_IDS] = newTagIds.toList()
    }

    // 커스텀 태그 추가 시 임시 ID 할당 (음수 사용)
    private var nextCustomTagId = -1L

    fun addCustomTags(categoryName: String, newTags: List<String>) {
        val currentCustomTags = customTagsMap.value
        val updatedTags = (currentCustomTags[categoryName] ?: emptyList()) + newTags
        savedStateHandle[KEY_CUSTOM_TAGS] = currentCustomTags + (categoryName to updatedTags)

        // 추가된 커스텀 태그에 임시 ID를 부여하고 자동으로 선택
        val newTagIds = newTags.map {
            val tempId = nextCustomTagId
            nextCustomTagId--
            tempId
        }
        val currentSelected = selectedTagIds.value.toSet()
        savedStateHandle[KEY_SELECTED_TAG_IDS] = (currentSelected + newTagIds.toSet()).toList()
    }

    /**
     * 선택된 태그를 서버로 전송
     * @param categoriesWithTags 카테고리별 전체 Tag 객체 정보
     * @param selectedTagIds 사용자가 선택한 태그 ID Set
     */
    fun submitSelectedTags(
        categoriesWithTags: Map<String, List<Tag>>,
        selectedTagIds: Set<Long>
    ) {
        viewModelScope.launch {
            _uiState.value = TagSelectUiState.Loading
            Log.d("TagSelect:VM", "Submitting selected tags")
            Log.d("TagSelect:VM", "Total selected tag IDs: ${selectedTagIds.size}")

            // 카테고리별로 선택된 태그만 필터링하여 태그 이름으로 변환
            val filteredCategoriesWithTags = categoriesWithTags.mapNotNull { (categoryName, tags) ->
                val selectedTagsForCategory = tags
                    .filter { selectedTagIds.contains(it.id) }
                    .map { it.name }
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

    fun retry(categoriesWithTags: Map<String, List<Tag>>, selectedTagIds: Set<Long>) {
        submitSelectedTags(categoriesWithTags, selectedTagIds)
    }
}

sealed interface TagSelectUiState {
    object Idle : TagSelectUiState
    object Loading : TagSelectUiState
    object Success : TagSelectUiState
    data class Error(val message: String, val errorType: ErrorType) : TagSelectUiState
}