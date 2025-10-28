package com.jinjinjara.pola.data.repository

import com.jinjinjara.pola.data.local.datastore.PreferencesDataStore
import com.jinjinjara.pola.data.remote.api.AuthApi
import com.jinjinjara.pola.data.remote.dto.request.LoginRequest
import com.jinjinjara.pola.data.remote.dto.request.RefreshTokenRequest
import com.jinjinjara.pola.data.remote.dto.request.SignUpRequest
import com.jinjinjara.pola.data.mapper.toUser
import com.jinjinjara.pola.di.IoDispatcher
import com.jinjinjara.pola.domain.model.User
import com.jinjinjara.pola.domain.repository.AuthRepository
import com.jinjinjara.pola.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * AuthRepository 구현체
 */
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val preferencesManager: PreferencesDataStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return withContext(ioDispatcher) {
            try {
                val response = authApi.login(
                    LoginRequest(email = email, password = password)
                )

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    // 토큰 저장
                    saveTokens(
                        accessToken = loginResponse.accessToken,
                        refreshToken = loginResponse.refreshToken
                    )

                    // User 정보 반환
                    Result.Success(loginResponse.user.toUser())
                } else {
                    Result.Error(
                        exception = Exception(response.message()),
                        message = "로그인에 실패했습니다."
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    exception = e,
                    message = e.message ?: "네트워크 오류가 발생했습니다."
                )
            }
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        name: String
    ): Result<User> {
        return withContext(ioDispatcher) {
            try {
                val response = authApi.signUp(
                    SignUpRequest(
                        email = email,
                        password = password,
                        name = name
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val signUpResponse = response.body()!!

                    // 토큰 저장
                    saveTokens(
                        accessToken = signUpResponse.accessToken,
                        refreshToken = signUpResponse.refreshToken
                    )

                    Result.Success(signUpResponse.user.toUser())
                } else {
                    Result.Error(
                        exception = Exception(response.message()),
                        message = "회원가입에 실패했습니다."
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    exception = e,
                    message = e.message ?: "네트워크 오류가 발생했습니다."
                )
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                // 서버에 로그아웃 요청 (선택사항)
                authApi.logout()

                // 로컬 토큰 삭제
                clearTokens()

                Result.Success(Unit)
            } catch (e: Exception) {
                // 로그아웃은 실패해도 로컬 토큰은 삭제
                clearTokens()
                Result.Success(Unit)
            }
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<String> {
        return withContext(ioDispatcher) {
            try {
                val response = authApi.refreshToken(
                    RefreshTokenRequest(refreshToken = refreshToken)
                )

                if (response.isSuccessful && response.body() != null) {
                    val newAccessToken = response.body()!!.accessToken

                    // 새 액세스 토큰 저장
                    preferencesManager.saveAccessToken(newAccessToken)

                    Result.Success(newAccessToken)
                } else {
                    Result.Error(
                        exception = Exception(response.message()),
                        message = "토큰 갱신에 실패했습니다."
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    exception = e,
                    message = e.message ?: "토큰 갱신 중 오류가 발생했습니다."
                )
            }
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return withContext(ioDispatcher) {
            val accessToken = preferencesManager.getAccessToken()
            !accessToken.isNullOrEmpty()
        }
    }

    override fun observeLoginState(): Flow<Boolean> {
        return preferencesManager.observeAccessToken()
    }

    override suspend fun getAccessToken(): String? {
        return withContext(ioDispatcher) {
            preferencesManager.getAccessToken()
        }
    }

    override suspend fun getRefreshToken(): String? {
        return withContext(ioDispatcher) {
            preferencesManager.getRefreshToken()
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        withContext(ioDispatcher) {
            preferencesManager.saveAccessToken(accessToken)
            preferencesManager.saveRefreshToken(refreshToken)
        }
    }

    override suspend fun clearTokens() {
        withContext(ioDispatcher) {
            preferencesManager.clearTokens()
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return withContext(ioDispatcher) {
            try {
                val response = authApi.getCurrentUser()

                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!.toUser())
                } else {
                    Result.Error(
                        exception = Exception(response.message()),
                        message = "사용자 정보를 가져올 수 없습니다."
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    exception = e,
                    message = e.message ?: "네트워크 오류가 발생했습니다."
                )
            }
        }
    }
}