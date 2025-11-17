package com.jinjinjara.pola.presentation.ui.screen.my

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.model.Tag
import com.jinjinjara.pola.domain.usecase.category.AddTagsToCategoryUseCase
import com.jinjinjara.pola.domain.usecase.category.CreateCategoryUseCase
import com.jinjinjara.pola.domain.usecase.category.DeleteCategoryUseCase
import com.jinjinjara.pola.domain.usecase.category.InitCategoryTagsUseCase
import com.jinjinjara.pola.domain.usecase.category.RemoveTagFromCategoryUseCase
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
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val addTagsToCategoryUseCase: AddTagsToCategoryUseCase,
    private val removeTagFromCategoryUseCase: RemoveTagFromCategoryUseCase,
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

    // DB에서 로드된 초기 상태 (비교용)
    private var initialCategoriesWithTags = mapOf<String, List<Tag>>()
    private var categoryIdMap = mapOf<String, Long>()

    /**
     * EditCategoryScreen에서 전달받은 초기 상태 설정
     */
    fun initializeState(categoriesWithTags: Map<String, List<Tag>>, categoryIdMap: Map<String, Long>) {
        this.initialCategoriesWithTags = categoriesWithTags
        this.categoryIdMap = categoryIdMap
        Log.d("[Edit]", "Initialized state with ${categoriesWithTags.size} categories")
    }

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

    /**
     * 변경사항을 서버에 제출
     */
    fun submitChanges(currentCategoriesWithTags: Map<String, List<Tag>>) {
        viewModelScope.launch {
            _uiState.value = EditTagUiState.Loading

            try {
                Log.d("[Edit]", "=== Starting submitChanges ===")
                Log.d("[Edit]", "Initial categories: ${initialCategoriesWithTags.keys}")
                Log.d("[Edit]", "Current categories: ${currentCategoriesWithTags.keys}")

                val initialCategoryNames = initialCategoriesWithTags.keys
                val currentCategoryNames = currentCategoriesWithTags.keys

                // 1. 삭제된 카테고리 처리
                val categoriesToDelete = initialCategoryNames - currentCategoryNames
                Log.d("[Edit]", "Categories to delete: $categoriesToDelete")

                for (categoryName in categoriesToDelete) {
                    val categoryId = categoryIdMap[categoryName]
                    if (categoryId != null) {
                        when (val result = deleteCategoryUseCase(categoryId)) {
                            is Result.Success -> {
                                Log.d("[Edit]", "Successfully deleted category: $categoryName")
                            }
                            is Result.Error -> {
                                Log.e("[Edit]", "Failed to delete category: $categoryName - ${result.message}")
                                _uiState.value = EditTagUiState.Error(
                                    "카테고리 삭제 실패: $categoryName",
                                    result.errorType
                                )
                                return@launch
                            }
                            else -> Unit
                        }
                    }
                }

                // 2. 새로 추가된 카테고리 생성
                val categoriesToCreate = currentCategoryNames - initialCategoryNames
                Log.d("[Edit]", "Categories to create: $categoriesToCreate")

                val newCategoryIdMap = mutableMapOf<String, Long>()
                for (categoryName in categoriesToCreate) {
                    when (val result = createCategoryUseCase(categoryName)) {
                        is Result.Success -> {
                            val newCategoryId = result.data
                            newCategoryIdMap[categoryName] = newCategoryId
                            Log.d("[Edit]", "Successfully created category: $categoryName with ID: $newCategoryId")
                        }
                        is Result.Error -> {
                            Log.e("[Edit]", "Failed to create category: $categoryName - ${result.message}")
                            _uiState.value = EditTagUiState.Error(
                                "카테고리 생성 실패: $categoryName",
                                result.errorType
                            )
                            return@launch
                        }
                        else -> Unit
                    }
                }

                // 3. 유지된 카테고리의 태그 변경 처리
                val retainedCategories = initialCategoryNames.intersect(currentCategoryNames)
                Log.d("[Edit]", "Retained categories: $retainedCategories")

                for (categoryName in retainedCategories) {
                    val categoryId = categoryIdMap[categoryName] ?: continue
                    val initialTags = initialCategoriesWithTags[categoryName] ?: emptyList()
                    val currentTags = currentCategoriesWithTags[categoryName] ?: emptyList()

                    val initialTagIds = initialTags.map { it.id }.toSet()
                    val currentTagIds = currentTags.map { it.id }.filter { it > 0 }.toSet() // 음수는 커스텀 태그

                    // 제거된 태그
                    val tagsToRemove = initialTagIds - currentTagIds
                    for (tagId in tagsToRemove) {
                        when (val result = removeTagFromCategoryUseCase(categoryId, tagId)) {
                            is Result.Success -> {
                                Log.d("[Edit]", "Successfully removed tag $tagId from category $categoryName")
                            }
                            is Result.Error -> {
                                Log.e("[Edit]", "Failed to remove tag $tagId from category $categoryName - ${result.message}")
                            }
                            else -> Unit
                        }
                    }

                    // 추가된 태그 (기존 태그)
                    val tagsToAdd = currentTagIds - initialTagIds
                    val tagsToAddNames = currentTags.filter { it.id in tagsToAdd }.map { it.name }

                    // 커스텀 태그 (음수 ID)
                    val customTags = currentTags.filter { it.id < 0 }.map { it.name }
                    val allTagsToAdd = (tagsToAddNames + customTags).distinct()

                    if (allTagsToAdd.isNotEmpty()) {
                        when (val result = addTagsToCategoryUseCase(categoryId, allTagsToAdd)) {
                            is Result.Success -> {
                                Log.d("[Edit]", "Successfully added tags to category $categoryName: $allTagsToAdd")
                            }
                            is Result.Error -> {
                                Log.e("[Edit]", "Failed to add tags to category $categoryName - ${result.message}")
                            }
                            else -> Unit
                        }
                    }
                }

                // 4. 새로 생성된 카테고리에 태그 추가
                for (categoryName in categoriesToCreate) {
                    val categoryId = newCategoryIdMap[categoryName] ?: continue
                    val tags = currentCategoriesWithTags[categoryName] ?: emptyList()
                    val tagNames = tags.map { it.name }

                    if (tagNames.isNotEmpty()) {
                        when (val result = addTagsToCategoryUseCase(categoryId, tagNames)) {
                            is Result.Success -> {
                                Log.d("[Edit]", "Successfully added tags to new category $categoryName: $tagNames")
                            }
                            is Result.Error -> {
                                Log.e("[Edit]", "Failed to add tags to new category $categoryName - ${result.message}")
                            }
                            else -> Unit
                        }
                    }
                }

                Log.d("[Edit]", "=== Successfully completed all changes ===")
                _uiState.value = EditTagUiState.Success

            } catch (e: Exception) {
                Log.e("[Edit]", "Exception while submitting changes", e)
                _uiState.value = EditTagUiState.Error(
                    e.message ?: "알 수 없는 오류가 발생했습니다",
                    ErrorType.NETWORK
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = EditTagUiState.Idle
    }

    /**
     * SavedStateHandle과 내부 상태를 완전히 초기화
     */
    fun clearSavedState() {
        savedStateHandle[KEY_SELECTED_TAG_IDS] = emptyList<Long>()
        savedStateHandle[KEY_CUSTOM_TAGS] = emptyMap<String, List<String>>()
        initialCategoriesWithTags = emptyMap()
        categoryIdMap = emptyMap()
        _uiState.value = EditTagUiState.Idle
        Log.d("[Edit]", "Cleared all saved state in EditTagViewModel")
    }
}

sealed interface EditTagUiState {
    object Idle : EditTagUiState
    object Loading : EditTagUiState
    object Success : EditTagUiState
    data class Error(val message: String, val errorType: ErrorType) : EditTagUiState
}
