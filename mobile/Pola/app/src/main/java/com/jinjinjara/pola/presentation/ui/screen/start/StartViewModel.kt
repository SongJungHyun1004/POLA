package com.jinjinjara.pola.presentation.ui.screen.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.data.remote.auth.GoogleSignInManager
import com.jinjinjara.pola.domain.repository.AuthRepository
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    private val googleSignInManager: GoogleSignInManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StartUiState>(StartUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun signIn() {
        viewModelScope.launch {
            googleSignInManager.signIn(
                onSuccess = { idToken ->
                    viewModelScope.launch {
                        when (val result = authRepository.loginWithGoogle(idToken)) {
                            is Result.Success -> _uiState.value = StartUiState.Success
                            is Result.Error -> _uiState.value = StartUiState.Error(result.message ?: "로그인 실패")
                            else -> Unit // Loading은 현재 안 씀
                        }
                    }
                },
                onError = { error ->
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
