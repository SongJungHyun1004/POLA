package com.jinjinjara.pola.presentation.ui.screen.my

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.model.User
import com.jinjinjara.pola.domain.usecase.auth.GetUserUseCase
import com.jinjinjara.pola.domain.usecase.auth.LogoutUseCase
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// My 화면 ViewModel
@HiltViewModel
class MyViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyUiState>(MyUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _userInfoState = MutableStateFlow<UserInfoUiState>(UserInfoUiState.Loading)
    val userInfoState = _userInfoState.asStateFlow()

    init {
        loadUserInfo()
    }

    // 사용자 정보 로드
    private fun loadUserInfo() {
        Log.d("Auth:UI", "Loading user info")
        viewModelScope.launch {
            when (val result = getUserUseCase()) {
                is Result.Success -> {
                    Log.d("Auth:UI", "User info loaded: ${result.data.email}")
                    _userInfoState.value = UserInfoUiState.Success(result.data)
                }
                is Result.Error -> {
                    Log.e("Auth:UI", "Failed to load user info: ${result.message}")
                    _userInfoState.value = UserInfoUiState.Error(result.message ?: "사용자 정보를 불러올 수 없습니다")
                }
                else -> Unit
            }
        }
    }

    // 로그아웃
    fun logout() {
        Log.d("Auth:UI", "Logout button clicked")
        _uiState.value = MyUiState.LogoutLoading

        viewModelScope.launch {
            Log.d("Auth:UI", "Calling logout use case")
            when (val result = logoutUseCase()) {
                is Result.Success -> {
                    Log.d("Auth:UI", "Logout successful")
                    _uiState.value = MyUiState.LogoutSuccess
                }
                is Result.Error -> {
                    // 로그아웃은 항상 성공으로 처리
                    Log.d("Auth:UI", "Logout completed with error but treated as success")
                    _uiState.value = MyUiState.LogoutSuccess
                }
                else -> Unit
            }
        }
    }

    // 로그아웃 상태 초기화
    fun resetLogoutState() {
        _uiState.value = MyUiState.Idle
    }
}

// My 화면 UI 상태
sealed interface MyUiState {
    object Idle : MyUiState
    object LogoutLoading : MyUiState
    object LogoutSuccess : MyUiState
}

// 사용자 정보 UI 상태
sealed interface UserInfoUiState {
    object Loading : UserInfoUiState
    data class Success(val user: User) : UserInfoUiState
    data class Error(val message: String) : UserInfoUiState
}
