package com.jinjinjara.pola.presentation.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.usecase.search.GetTagSuggestionsUseCase
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getTagSuggestionsUseCase: GetTagSuggestionsUseCase
) : ViewModel() {

    private val _tagSuggestions = MutableStateFlow<List<String>>(emptyList())
    val tagSuggestions: StateFlow<List<String>> = _tagSuggestions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun getTagSuggestions(keyword: String) {
        if (keyword.isEmpty()) {
            _tagSuggestions.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            when (val result = getTagSuggestionsUseCase(keyword)) {
                is Result.Success -> {
                    _tagSuggestions.value = result.data
                }
                is Result.Error -> {
                    _tagSuggestions.value = emptyList()
                }
                is Result.Loading -> {
                    // Handle loading state if needed
                }
            }
            _isLoading.value = false
        }
    }

    fun clearSuggestions() {
        _tagSuggestions.value = emptyList()
    }
}