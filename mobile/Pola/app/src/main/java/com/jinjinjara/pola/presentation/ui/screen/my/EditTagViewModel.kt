package com.jinjinjara.pola.presentation.ui.screen.my

import android.util.Log
import androidx.lifecycle.SavedStateHandle
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
class EditTagViewModel @Inject constructor(
    private val initCategoryTagsUseCase: InitCategoryTagsUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditTagUiState>(EditTagUiState.Idle)
    val uiState = _uiState.asStateFlow()

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

    /**
     * 초기 태그 선택 (태그 ID 리스트)
     * @param tagIds 선택할 태그 ID 리스트
     */
    fun initializeSelectedTagIds(tagIds: List<Long>) {
        savedStateHandle[KEY_SELECTED_TAG_IDS] = tagIds
    }

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

    fun resetState() {
        _uiState.value = EditTagUiState.Idle
    }
}

sealed interface EditTagUiState {
    object Idle : EditTagUiState
    object Loading : EditTagUiState
    object Success : EditTagUiState
    data class Error(val message: String, val errorType: ErrorType) : EditTagUiState
}
