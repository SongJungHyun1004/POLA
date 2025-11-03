package com.jinjinjara.pola.presentation.ui.screen.start

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.data.remote.auth.GoogleSignInManager
import com.jinjinjara.pola.domain.usecase.auth.LoginUseCase
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
        Log.d("StartViewModel", "signIn() called")
        viewModelScope.launch {
            googleSignInManager.signIn(
                context = context,
                onSuccess = { idToken ->
                    Log.d("StartViewModel", "Google signIn success, idToken received")
                    viewModelScope.launch {
                        when (val result = loginUseCase(LoginUseCase.Params.Google(idToken))) {
                            is Result.Success -> {
                                Log.d("StartViewModel", "Login success: ${result.data}")
                                _uiState.value = StartUiState.Success
                            }
                            is Result.Error -> {
                                Log.e("StartViewModel", "Login error: ${result.message}")
                                _uiState.value = StartUiState.Error(result.message ?: "로그인 실패")
                            }
                            else -> Unit
                        }
                    }
                },
                onError = { error ->
                    Log.e("StartViewModel", "Google signIn error: $error")
                    _uiState.value = StartUiState.Error(error)
                }
            )
        }
    }
}

sealed interface StartUiState {
    object Idle : StartUiState
    object Loading : StartUiState
    object Success : StartUiState
    data class Error(val message: String) : StartUiState
}
