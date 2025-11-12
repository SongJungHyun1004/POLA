package com.jinjinjara.pola.presentation.ui.screen.start

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.data.local.datastore.PreferencesDataStore
import com.jinjinjara.pola.data.remote.auth.GoogleSignInManager
import com.jinjinjara.pola.domain.usecase.auth.LoginUseCase
import com.jinjinjara.pola.domain.usecase.category.GetUserCategoriesUseCase
import com.jinjinjara.pola.util.ErrorType
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    private val googleSignInManager: GoogleSignInManager,
    private val loginUseCase: LoginUseCase,
    private val getUserCategoriesUseCase: GetUserCategoriesUseCase,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<StartUiState>(StartUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun signIn(context: Context) {
        Log.d("Auth:UI", "Login button clicked")
        _uiState.value = StartUiState.Loading
        viewModelScope.launch {
            googleSignInManager.signIn(
                context = context,
                onSuccess = { signInResult ->
                    Log.d("Auth:UI", "Google credentials received, calling login use case")
                    Log.d("Auth:UI", "ID Token: ${signInResult.idToken}")
                    when (val result = loginUseCase(
                        LoginUseCase.Params(
                            idToken = signInResult.idToken,
                            displayName = signInResult.displayName
                        )
                    )) {
                        is Result.Success -> {
                            Log.d("Auth:UI", "Login successful, user: ${result.data.email}")
                            Log.d("Auth:UI", "Onboarding status from API: ${result.data.onboardingCompleted}")

                            // 로그인 성공 후 실제 카테고리 존재 여부 확인
                            _uiState.value = StartUiState.CheckingCategories
                            checkCategoriesAndNavigate()
                        }
                        is Result.Error -> {
                            Log.e("Auth:UI", "Login failed: ${result.message}")
                            _uiState.value = StartUiState.Error(
                                message = result.message ?: "로그인 실패",
                                errorType = result.errorType
                            )
                        }
                        else -> Unit
                    }
                },
                onError = { error, errorType ->
                    Log.e("Auth:UI", "Google sign-in error: $error (type: $errorType)")
                    _uiState.value = StartUiState.Error(error, errorType)
                }
            )
        }
    }

    /**
     * 카테고리 존재 여부를 확인하여 온보딩 필요 여부 결정
     */
    private suspend fun checkCategoriesAndNavigate() {
        Log.d("Auth:UI", "=== Checking user categories after login ===")

        when (val result = getUserCategoriesUseCase()) {
            is Result.Success -> {
                val categories = result.data
                Log.d("Auth:UI", "Categories found: ${categories.size}")

                // 카테고리 목록 로그
                categories.forEach { category ->
                    Log.d("Auth:UI", "Category: ${category.categoryName}")
                }

                // 카테고리가 없거나 "미분류"만 있는 경우 온보딩 필요
                val needsOnboarding = categories.isEmpty() ||
                    (categories.size == 1 && categories.first().categoryName == "미분류")

                // 로그인 시 카테고리 체크 완료 플래그 설정
                preferencesDataStore.setCategoryCheckedOnLogin(true)
                Log.d("Auth:UI", "Set category checked on login flag")

                if (needsOnboarding) {
                    Log.d("Auth:UI", "No valid categories (empty or only '미분류'), needs onboarding")
                    // DataStore에 온보딩 미완료 저장
                    preferencesDataStore.setOnboardingCompleted(false)
                    _uiState.value = StartUiState.Success(onboardingCompleted = false)
                } else {
                    Log.d("Auth:UI", "Valid categories exist, onboarding completed")
                    // DataStore에 온보딩 완료 저장
                    preferencesDataStore.setOnboardingCompleted(true)
                    _uiState.value = StartUiState.Success(onboardingCompleted = true)
                }
            }
            is Result.Error -> {
                Log.e("Auth:UI", "Failed to check categories: ${result.message}")
                // 에러가 발생하면 온보딩 필요한 것으로 처리 (안전한 방향)
                _uiState.value = StartUiState.Success(onboardingCompleted = false)
            }
            else -> {
                _uiState.value = StartUiState.Success(onboardingCompleted = false)
            }
        }
    }

    fun resetError() {
        if (_uiState.value is StartUiState.Error) {
            Log.d("Auth:UI", "Resetting error state to Idle")
            _uiState.value = StartUiState.Idle
        }
    }
}

sealed interface StartUiState {
    object Idle : StartUiState
    object Loading : StartUiState
    object CheckingCategories : StartUiState
    data class Success(val onboardingCompleted: Boolean) : StartUiState
    data class Error(val message: String, val errorType: ErrorType) : StartUiState
}
