package com.jinjinjara.pola.presentation.ui.screen.start

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.data.remote.auth.GoogleSignInManager
import com.jinjinjara.pola.domain.usecase.auth.LoginUseCase
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
    private val loginUseCase: LoginUseCase
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
                        LoginUseCase.Params.Google(
                            idToken = signInResult.idToken,
                            displayName = signInResult.displayName
                        )
                    )) {
                        is Result.Success -> {
                            Log.d("Auth:UI", "Login successful, user: ${result.data.email}")
                            Log.d("Auth:UI", "Onboarding status: ${result.data.onboardingCompleted}")
                            _uiState.value = StartUiState.Success(result.data.onboardingCompleted)
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
    data class Success(val onboardingCompleted: Boolean) : StartUiState
    data class Error(val message: String, val errorType: ErrorType) : StartUiState
}
