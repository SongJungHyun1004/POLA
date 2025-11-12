package com.jinjinjara.pola.presentation.ui.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.data.local.datastore.PreferencesDataStore
import com.jinjinjara.pola.domain.usecase.category.GetUserCategoriesUseCase
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUserCategoriesUseCase: GetUserCategoriesUseCase,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    init {
        checkCategories()
    }

    /**
     * 사용자의 카테고리 존재 여부를 확인
     * 카테고리가 "미분류"만 있거나 아무것도 없으면 온보딩이 필요함
     * DataStore를 업데이트하면 PolaNavHost의 LaunchedEffect가 자동으로 네비게이션 처리
     */
    private fun checkCategories() {
        viewModelScope.launch {
            // 로그인 직후 이미 체크했는지 확인
            val alreadyChecked = preferencesDataStore.isCategoryCheckedOnLogin()
            if (alreadyChecked) {
                Log.d("Main:VM", "Category already checked on login, skipping duplicate check")
                preferencesDataStore.setCategoryCheckedOnLogin(false) // 플래그 리셋
                return@launch
            }

            Log.d("Main:VM", "=== Checking user categories ===")

            when (val result = getUserCategoriesUseCase()) {
                is Result.Success -> {
                    val categories = result.data
                    Log.d("Main:VM", "Categories found: ${categories.size}")

                    // 카테고리 목록 로그
                    categories.forEach { category ->
                        Log.d("Main:VM", "Category: ${category.categoryName}")
                    }

                    // 카테고리가 없거나 "미분류"만 있는 경우 온보딩 필요
                    val needsOnboarding = categories.isEmpty() ||
                        (categories.size == 1 && categories.first().categoryName == "미분류")

                    if (needsOnboarding) {
                        Log.d("Main:VM", "No valid categories (empty or only '미분류'), needs onboarding")
                        // DataStore 업데이트 -> PolaNavHost의 LaunchedEffect가 자동으로 CategorySelect로 이동
                        preferencesDataStore.setOnboardingCompleted(false)
                    } else {
                        Log.d("Main:VM", "Valid categories exist, onboarding completed")
                    }
                }
                is Result.Error -> {
                    Log.e("Main:VM", "Failed to check categories: ${result.message}")
                    // 에러가 발생해도 진행 (네트워크 문제일 수 있음)
                }
                else -> {
                    // Do nothing
                }
            }
        }
    }
}
