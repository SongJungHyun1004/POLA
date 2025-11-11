package com.jinjinjara.pola.presentation.ui.screen.timeline

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.model.Category
import com.jinjinjara.pola.domain.model.TimelineFile
import com.jinjinjara.pola.domain.usecase.category.GetCategoriesUseCase
import com.jinjinjara.pola.domain.usecase.timeline.GetTimelineUseCase
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getTimelineUseCase: GetTimelineUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TimelineUiState>(TimelineUiState.Loading)
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    private var currentPage = 0
    private var isLastPage = false
    private var isLoadingMore = false
    private var currentFilterType: String? = null
    private var currentFilterId: Long? = null

    init {
        loadCategories()
        loadTimeline()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            Log.d(TAG, "loadCategories() 시작")
            when (val result = getCategoriesUseCase()) {
                is Result.Success -> {
                    Log.d(TAG, "카테고리 로드 성공 - ${result.data.size}개")
                    _categories.value = result.data
                }
                is Result.Error -> {
                    Log.e(TAG, "카테고리 로드 실패 - ${result.message}")
                    // 카테고리 로드 실패해도 타임라인은 표시
                    _categories.value = emptyList()
                }
                is Result.Loading -> {
                    Log.d(TAG, "카테고리 로딩 중")
                }
            }
        }
    }

    fun selectCategory(categoryId: Long?) {
        Log.d(TAG, "selectCategory() - categoryId: $categoryId")
        _selectedCategoryId.value = categoryId

        // 카테고리 선택 시 타임라인 필터링
        val filterType = if (categoryId == null) null else "category"
        loadTimeline(filterType = filterType, filterId = categoryId)
    }

    fun loadTimeline(filterType: String? = null, filterId: Long? = null) {
        viewModelScope.launch {
            Log.d(TAG, "loadTimeline() 시작 - filterType: $filterType, filterId: $filterId")

            // 필터가 변경되면 리셋
            if (filterType != currentFilterType || filterId != currentFilterId) {
                currentPage = 0
                isLastPage = false
                currentFilterType = filterType
                currentFilterId = filterId
            }

            _uiState.value = TimelineUiState.Loading

            when (val result = getTimelineUseCase(
                page = currentPage,
                size = 20,
                filterType = filterType,
                filterId = filterId
            )) {
                is Result.Success -> {
                    Log.d(TAG, "UseCase Success - 파일 개수: ${result.data.files.size}")
                    val groupedFiles = groupFilesByDate(result.data.files)
                    isLastPage = result.data.isLast

                    _uiState.value = TimelineUiState.Success(
                        groupedFiles = groupedFiles,
                        canLoadMore = !isLastPage
                    )
                }
                is Result.Error -> {
                    Log.e(TAG, "UseCase Error - message: ${result.message}")
                    _uiState.value = TimelineUiState.Error(
                        result.message ?: "타임라인을 불러올 수 없습니다"
                    )
                }
                is Result.Loading -> {
                    Log.d(TAG, "UseCase Loading")
                    _uiState.value = TimelineUiState.Loading
                }
            }
        }
    }

    fun loadMore() {
        if (isLoadingMore || isLastPage) {
            Log.d(TAG, "loadMore() 중단 - isLoadingMore: $isLoadingMore, isLastPage: $isLastPage")
            return
        }

        viewModelScope.launch {
            isLoadingMore = true
            Log.d(TAG, "loadMore() 시작 - currentPage: $currentPage")

            val currentState = _uiState.value
            if (currentState !is TimelineUiState.Success) {
                isLoadingMore = false
                return@launch
            }

            // 로딩 중 상태 표시
            _uiState.value = currentState.copy(isLoadingMore = true)

            currentPage++
            when (val result = getTimelineUseCase(
                page = currentPage,
                size = 20,
                filterType = currentFilterType,
                filterId = currentFilterId
            )) {
                is Result.Success -> {
                    Log.d(TAG, "loadMore Success - 추가 파일 개수: ${result.data.files.size}")

                    // 기존 파일 + 새 파일 병합
                    val currentFiles = currentState.groupedFiles.values.flatten()
                    val allFiles = currentFiles + result.data.files
                    val newGroupedFiles = groupFilesByDate(allFiles)

                    isLastPage = result.data.isLast

                    _uiState.value = TimelineUiState.Success(
                        groupedFiles = newGroupedFiles,
                        isLoadingMore = false,
                        canLoadMore = !isLastPage
                    )
                }
                is Result.Error -> {
                    Log.e(TAG, "loadMore Error - message: ${result.message}")
                    currentPage-- // 실패 시 페이지 번호 복원
                    _uiState.value = currentState.copy(isLoadingMore = false)
                    _errorEvent.emit(result.message ?: "추가 데이터를 불러올 수 없습니다")
                }
                is Result.Loading -> {
                    Log.d(TAG, "loadMore Loading")
                }
            }
            isLoadingMore = false
        }
    }

    fun refresh() {
        currentPage = 0
        isLastPage = false
        loadTimeline(currentFilterType, currentFilterId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun groupFilesByDate(files: List<TimelineFile>): Map<String, List<TimelineFile>> {
        return files.groupBy { file ->
            val formatter = DateTimeFormatter.ofPattern("yy.MM.dd")
            val dayOfWeek = file.createdAt.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).uppercase()
            "${file.createdAt.format(formatter)} $dayOfWeek"
        }
    }

    companion object {
        private const val TAG = "TimelineViewModel"
    }
}

sealed class TimelineUiState {
    data object Loading : TimelineUiState()
    data class Success(
        val groupedFiles: Map<String, List<TimelineFile>>,
        val isLoadingMore: Boolean = false,
        val canLoadMore: Boolean = true
    ) : TimelineUiState()
    data class Error(val message: String) : TimelineUiState()
}
