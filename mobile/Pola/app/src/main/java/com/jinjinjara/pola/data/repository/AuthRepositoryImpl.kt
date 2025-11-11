package com.jinjinjara.pola.data.repository

import android.util.Log
import com.jinjinjara.pola.data.local.datastore.PreferencesDataStore
import com.jinjinjara.pola.data.remote.api.AuthApi
import com.jinjinjara.pola.data.remote.dto.request.*
import com.jinjinjara.pola.data.mapper.toUser
import com.jinjinjara.pola.di.IoDispatcher
import com.jinjinjara.pola.domain.model.User
import com.jinjinjara.pola.domain.repository.AuthRepository
import com.jinjinjara.pola.util.ErrorType
import com.jinjinjara.pola.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
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
                val response = authApi.login(LoginRequest(email, password))

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    saveTokens(
                        accessToken = loginResponse.accessToken,
                        refreshToken = loginResponse.refreshToken
                    )

                    Result.Success(loginResponse.user.toUser())
                } else {
                    Result.Error(
                        exception = Exception(response.message()),
                        message = "로그인에 실패했습니다."
                    )
                }
            } catch (e: Exception) {
                Log.e("Auth:Login", "Login failed with exception: ${e.message}", e)
                Result.Error(
                    exception = e,
                    message = e.message ?: "네트워크 오류가 발생했습니다."
                )
            }
        }
    }

    // Google 로그인
    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return withContext(ioDispatcher) {
            try {
                Log.d("AuthRepository", "loginWithGoogle called")
                val response = authApi.loginWithGoogle(GoogleLoginRequest(idToken))

                if (response.isSuccessful && response.body() != null) {
                    val googleLoginResponse = response.body()!!
                    Log.d("AuthRepository", "API response success")

                    // 토큰 저장
                    saveTokens(
                        accessToken = googleLoginResponse.accessToken,
                        refreshToken = googleLoginResponse.refreshToken
                    )
                    Log.d("AuthRepository", "Tokens saved")

                    Result.Success(googleLoginResponse.user.toUser())
                } else {
                    Log.e("AuthRepository", "API response failed: ${response.code()} ${response.message()}")
                    Result.Error(
                        exception = Exception(response.message()),
                        message = "Google 로그인에 실패했습니다."
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Exception: ${e.message}", e)
                Result.Error(
                    exception = e,
                    message = e.message ?: "Google 로그인 중 오류가 발생했습니다."
                )
            }
        }
    }

    override suspend fun signUp(email: String, password: String, name: String): Result<User> {
        return withContext(ioDispatcher) {
            try {
                val response = authApi.signUp(SignUpRequest(email, password, name))

                if (response.isSuccessful && response.body() != null) {
                    val signUpResponse = response.body()!!

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
                Log.e("Auth:SignUp", "Sign up failed with exception: ${e.message}", e)
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
                // Refresh Token 가져오기
                val refreshToken = preferencesManager.getRefreshToken()

                if (!refreshToken.isNullOrEmpty()) {
                    Log.d("Auth:Logout", "Sending logout request to server with refresh token")
                    authApi.logout("Bearer $refreshToken")
                    Log.d("Auth:Logout", "Server logout successful - current device's refresh token invalidated")
                } else {
                    Log.d("Auth:Logout", "No refresh token available, skipping server logout")
                }

                clearTokensAndResetOnboarding()
                Log.d("Auth:Logout", "Local tokens and onboarding status cleared")
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.w("Auth:Logout", "Server logout failed, clearing local tokens anyway", e)
                clearTokensAndResetOnboarding()
                Log.d("Auth:Logout", "Local tokens and onboarding status cleared")
                Result.Success(Unit)
            }
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<String> {
        return withContext(ioDispatcher) {
            try {
                val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))

                if (response.isSuccessful && response.body() != null) {
                    val newAccessToken = response.body()!!.accessToken
                    preferencesManager.saveAccessToken(newAccessToken)
                    Result.Success(newAccessToken)
                } else {
                    Result.Error(
                        exception = Exception(response.message()),
                        message = "토큰 갱신에 실패했습니다."
                    )
                }
            } catch (e: Exception) {
                Log.e("Auth:Refresh", "Token refresh failed with exception: ${e.message}", e)
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

    override fun observeLoginState(): Flow<Boolean> = preferencesManager.observeAccessToken()

    override suspend fun getAccessToken(): String? =
        withContext(ioDispatcher) { preferencesManager.getAccessToken() }

    override suspend fun getRefreshToken(): String? =
        withContext(ioDispatcher) { preferencesManager.getRefreshToken() }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        withContext(ioDispatcher) {
            Log.d("Auth:Token", "Saving access token: ${accessToken.take(20)}...")
            preferencesManager.saveAccessToken(accessToken)
            Log.d("Auth:Token", "Saving refresh token: ${refreshToken.take(20)}...")
            preferencesManager.saveRefreshToken(refreshToken)
        }
    }

    override suspend fun clearTokens() {
        withContext(ioDispatcher) {
            Log.d("Auth:Token", "Clearing all tokens")
            preferencesManager.clearTokens()
            Log.d("Auth:Token", "All tokens cleared")
        }
    }

    /**
     * 토큰 삭제 + 온보딩 상태 리셋
     * 로그아웃 시에만 사용 (토큰 재발급 실패 시에는 사용하지 않음)
     */
    private suspend fun clearTokensAndResetOnboarding() {
        withContext(ioDispatcher) {
            Log.d("Auth:Token", "Clearing all tokens and resetting onboarding status")
            preferencesManager.clearTokens()
            preferencesManager.setOnboardingCompleted(false)
            Log.d("Auth:Token", "All tokens cleared and onboarding status reset")
        }
    }

    override suspend fun getUser(): Result<User> {
        return withContext(ioDispatcher) {
            try {
                Log.d("Auth:User", "Fetching current user info from server")
                val response = authApi.getUser()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.data != null) {
                        Log.d("Auth:User", "User info retrieved: ${apiResponse.data.email}")
                        Result.Success(apiResponse.data.toUser())
                    } else {
                        Log.e("Auth:User", "User info data is null")
                        Result.Error(
                            exception = Exception("Data is null"),
                            message = "사용자 정보를 가져올 수 없습니다."
                        )
                    }
                } else {
                    val statusCode = response.code()
                    val errorBody = response.errorBody()?.string()
                    Log.e("Auth:User", "Failed to fetch user info")
                    Log.e("Auth:User", "Status Code: $statusCode")
                    Log.e("Auth:User", "Error message: ${response.message()}")
                    Log.e("Auth:User", "Error body: $errorBody")
                    Result.Error(
                        exception = Exception(response.message()),
                        message = "사용자 정보를 가져올 수 없습니다."
                    )
                }
            } catch (e: Exception) {
                Log.e("Auth:User", "Exception while fetching user info", e)
                Result.Error(
                    exception = e,
                    message = e.message ?: "네트워크 오류가 발생했습니다."
                )
            }
        }
    }

    // OAuth 2.0 Google 로그인
    override suspend fun googleLoginWithOAuth(idToken: String, displayName: String): Result<User> {
        return withContext(ioDispatcher) {
            try {
                Log.d("Auth:Login", "=== Google OAuth Login Started ===")
                Log.d("Auth:Login", "ID Token: $idToken")

                // Step 1: Google ID Token으로 최종 JWT 획득
                Log.d("Auth:Login", "Step 1: Requesting OAuth token")
                val tokenResponse = authApi.getOAuthToken(OAuthTokenRequest(idToken))

                if (!tokenResponse.isSuccessful || tokenResponse.body()?.data == null) {
                    val errorBody = tokenResponse.errorBody()?.string()
                    val statusCode = tokenResponse.code()
                    Log.e("Auth:Login", "Step 1 FAILED: OAuth token request failed")
                    Log.e("Auth:Login", "Status Code: $statusCode")
                    Log.e("Auth:Login", "Error: ${tokenResponse.message()}")
                    Log.e("Auth:Login", "Body: $errorBody")

                    return@withContext when (statusCode) {
                        401 -> Result.Error(
                            message = "인증에 실패했습니다. 다시 시도해주세요.",
                            errorType = ErrorType.UNAUTHORIZED
                        )
                        in 400..499 -> Result.Error(
                            message = "잘못된 요청입니다. 다시 시도해주세요.",
                            errorType = ErrorType.BAD_REQUEST
                        )
                        in 500..599 -> Result.Error(
                            message = "서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.",
                            errorType = ErrorType.SERVER
                        )
                        else -> Result.Error(
                            message = "Google 인증에 실패했습니다.",
                            errorType = ErrorType.UNKNOWN
                        )
                    }
                }

                val tokenData = tokenResponse.body()!!.data!!
                Log.d("Auth:Login", "Step 1 SUCCESS: OAuth token received")

                // Step 2: 토큰 임시 저장 (Step 3~4 API 호출을 위해 필요)
                Log.d("Auth:Login", "Step 2: Saving tokens temporarily for subsequent API calls")
                saveTokens(
                    accessToken = tokenData.accessToken,
                    refreshToken = tokenData.refreshToken
                )

                // Step 3: 사용자 정보 가져오기
                Log.d("Auth:Login", "Step 3: Getting current user info")
                val userInfoResponse = authApi.getUser()

                if (!userInfoResponse.isSuccessful || userInfoResponse.body()?.data == null) {
                    val statusCode = userInfoResponse.code()
                    val errorBody = userInfoResponse.errorBody()?.string()
                    Log.e("Auth:Login", "Step 3 FAILED: User info request failed")
                    Log.e("Auth:Login", "Status Code: $statusCode")
                    Log.e("Auth:Login", "Error message: ${userInfoResponse.message()}")
                    Log.e("Auth:Login", "Error body: $errorBody")

                    // 실패 시 토큰 삭제
                    clearTokens()

                    return@withContext Result.Error(
                        exception = Exception(userInfoResponse.message()),
                        message = "사용자 정보를 가져올 수 없습니다."
                    )
                }

                val userResponse = userInfoResponse.body()!!.data!!
                Log.d("Auth:Login", "Step 3 SUCCESS: User info retrieved -> ${userResponse.email}")

                // Step 4: 카테고리 확인으로 온보딩 상태 판단
                Log.d("Auth:Login", "Step 4: Checking user categories for onboarding status")
                val categoriesResponse = authApi.getUserCategories()

                val onboardingCompleted = when {
                    !categoriesResponse.isSuccessful -> {
                        val statusCode = categoriesResponse.code()
                        val errorBody = categoriesResponse.errorBody()?.string()
                        Log.e("Auth:Login", "Step 4: Categories API FAILED")
                        Log.e("Auth:Login", "Status Code: $statusCode")
                        Log.e("Auth:Login", "Error body: $errorBody")

                        // 5xx 서버 오류인 경우 로그인 전체 실패로 처리
                        if (statusCode in 500..599) {
                            Log.e("Auth:Login", "Server error detected, rolling back tokens and returning error")
                            clearTokens() // 저장된 토큰 롤백
                            return@withContext Result.Error(
                                message = "서버에 일시적인 문제가 발생했습니다.\n잠시 후 다시 시도해주세요.",
                                errorType = ErrorType.SERVER
                            )
                        }

                        // 404 또는 기타 4xx 에러는 카테고리 없음으로 간주 (온보딩 필요)
                        Log.d("Auth:Login", "Categories API returned ${statusCode}, treating as no categories (onboarding needed)")
                        false
                    }
                    categoriesResponse.body()?.data == null || categoriesResponse.body()?.data!!.isEmpty() -> {
                        Log.d("Auth:Login", "Step 4: User has no categories (onboarding needed)")
                        false
                    }
                    else -> {
                        val categoryCount = categoriesResponse.body()?.data?.size ?: 0
                        Log.d("Auth:Login", "Step 4 SUCCESS: User has $categoryCount categories (onboarding completed)")
                        true
                    }
                }

                // Step 5: 온보딩 상태 DataStore에 저장
                Log.d("Auth:Login", "Step 5: Saving onboarding status to DataStore")
                preferencesManager.setOnboardingCompleted(onboardingCompleted)

                // Step 6: 완료 (토큰과 온보딩 상태 모두 저장 완료)
                Log.d("Auth:Login", "Step 6: All verifications complete")
                Log.d("Auth:Token", "Final state - Access token: ${tokenData.accessToken.take(20)}...")
                Log.d("Auth:Token", "Final state - Refresh token: ${tokenData.refreshToken.take(20)}...")

                val user = userResponse.toUser(onboardingCompleted = onboardingCompleted)
                Log.d("Auth:Login", "=== Google OAuth Login SUCCESS === User: ${user.email}, Onboarding: $onboardingCompleted")
                Result.Success(user)

            } catch (e: SocketTimeoutException) {
                Log.e("Auth:Login", "=== Google OAuth Login FAILED === Timeout", e)
                Result.Error(
                    exception = e,
                    message = "요청 시간이 초과되었습니다. 다시 시도해주세요.",
                    errorType = ErrorType.TIMEOUT
                )
            } catch (e: IOException) {
                Log.e("Auth:Login", "=== Google OAuth Login FAILED === Network error", e)
                Result.Error(
                    exception = e,
                    message = "인터넷 연결을 확인해주세요.",
                    errorType = ErrorType.NETWORK
                )
            } catch (e: Exception) {
                Log.e("Auth:Login", "=== Google OAuth Login FAILED === Unknown error", e)
                Result.Error(
                    exception = e,
                    message = e.message ?: "로그인 중 오류가 발생했습니다.",
                    errorType = ErrorType.UNKNOWN
                )
            }
        }
    }

    // Access Token 유효성 검증
    override suspend fun verifyAccessToken(): Result<Boolean> {
        return withContext(ioDispatcher) {
            try {
                Log.d("Auth:Verify", "=== Access Token Verification Started ===")
                val response = authApi.oauthVerify()

                if (response.isSuccessful && response.body()?.data != null) {
                    val verifyData = response.body()!!.data!!
                    Log.d("Auth:Verify", "Token is valid for user: ${verifyData.email}")
                    Result.Success(verifyData.valid)
                } else {
                    val statusCode = response.code()
                    Log.e("Auth:Verify", "Token verification failed with status: $statusCode")
                    Result.Error(
                        exception = Exception("Token verification failed"),
                        message = "토큰 검증에 실패했습니다.",
                        errorType = if (statusCode == 401) ErrorType.UNAUTHORIZED else ErrorType.UNKNOWN
                    )
                }
            } catch (e: Exception) {
                Log.e("Auth:Verify", "Token verification error", e)
                Result.Error(
                    exception = e,
                    message = "토큰 검증 중 오류가 발생했습니다.",
                    errorType = ErrorType.UNKNOWN
                )
            }
        }
    }

    // Access Token 재발급
    override suspend fun reissueAccessToken(): Result<User> {
        return withContext(ioDispatcher) {
            try {
                Log.d("Auth:Reissue", "=== Access Token Reissue Started ===")

                // Step 1: Refresh Token 가져오기
                Log.d("Auth:Reissue", "Step 1: Getting refresh token from DataStore")
                val refreshToken = preferencesManager.getRefreshToken()
                if (refreshToken.isNullOrEmpty()) {
                    Log.e("Auth:Reissue", "Step 1 FAILED: No refresh token available")
                    return@withContext Result.Error(
                        exception = Exception("No refresh token"),
                        message = "저장된 Refresh Token이 없습니다.",
                        errorType = ErrorType.UNAUTHORIZED
                    )
                }

                Log.d("Auth:Reissue", "Step 1 SUCCESS: Refresh token found: ${refreshToken.take(20)}...")

                // Step 2: 토큰 재발급 요청
                Log.d("Auth:Reissue", "Step 2: Requesting new tokens from server")
                val response = authApi.oauthReissue("Bearer $refreshToken")

                if (!response.isSuccessful || response.body()?.data == null) {
                    val statusCode = response.code()
                    val errorBody = response.errorBody()?.string()
                    Log.e("Auth:Reissue", "Step 2 FAILED: Token reissue failed")
                    Log.e("Auth:Reissue", "Status Code: $statusCode")
                    Log.e("Auth:Reissue", "Error: ${response.message()}")
                    Log.e("Auth:Reissue", "Body: $errorBody")

                    // Refresh Token도 만료된 경우 토큰 삭제 (온보딩 상태는 유지)
                    if (statusCode == 401) {
                        Log.d("Auth:Reissue", "Refresh token expired, clearing tokens only (keeping onboarding status)")
                        clearTokens()
                    }

                    return@withContext when (statusCode) {
                        401 -> Result.Error(
                            message = "다시 로그인해주세요.",
                            errorType = ErrorType.UNAUTHORIZED
                        )
                        in 400..499 -> Result.Error(
                            message = "잘못된 요청입니다.",
                            errorType = ErrorType.BAD_REQUEST
                        )
                        in 500..599 -> Result.Error(
                            message = "서버에 문제가 발생했습니다.",
                            errorType = ErrorType.SERVER
                        )
                        else -> Result.Error(
                            message = "토큰 재발급에 실패했습니다.",
                            errorType = ErrorType.UNKNOWN
                        )
                    }
                }

                val tokenData = response.body()!!.data!!
                Log.d("Auth:Reissue", "Step 2 SUCCESS: New tokens received")

                // Step 3: 새 토큰 저장
                Log.d("Auth:Reissue", "Step 3: Saving new tokens to DataStore")
                saveTokens(
                    accessToken = tokenData.accessToken,
                    refreshToken = tokenData.refreshToken
                )
                Log.d("Auth:Reissue", "Step 3 SUCCESS: New tokens saved")

                // Step 4: 사용자 정보 가져오기
                Log.d("Auth:Reissue", "Step 4: Getting current user info")
                val userInfoResponse = authApi.getUser()

                if (!userInfoResponse.isSuccessful || userInfoResponse.body()?.data == null) {
                    Log.e("Auth:Reissue", "Step 4 FAILED: Failed to get user info after reissue")
                    return@withContext Result.Error(
                        exception = Exception(userInfoResponse.message()),
                        message = "사용자 정보를 가져올 수 없습니다."
                    )
                }

                val userResponse = userInfoResponse.body()!!.data!!
                Log.d("Auth:Reissue", "Step 4 SUCCESS: User info retrieved: ${userResponse.email}")

                // Step 5: 카테고리 확인으로 온보딩 상태 판단
                Log.d("Auth:Reissue", "Step 5: Checking user categories for onboarding status")
                val categoriesResponse = authApi.getUserCategories()

                val onboardingCompleted = when {
                    !categoriesResponse.isSuccessful -> {
                        val statusCode = categoriesResponse.code()
                        val errorBody = categoriesResponse.errorBody()?.string()
                        Log.e("Auth:Reissue", "Step 5: Categories API FAILED")
                        Log.e("Auth:Reissue", "Status Code: $statusCode")
                        Log.e("Auth:Reissue", "Error body: $errorBody")

                        // 5xx 서버 오류인 경우 토큰 재발급 전체 실패로 처리
                        if (statusCode in 500..599) {
                            Log.e("Auth:Reissue", "Server error detected, rolling back tokens and returning error")
                            clearTokens() // 저장된 토큰 롤백
                            return@withContext Result.Error(
                                message = "서버에 일시적인 문제가 발생했습니다.\n잠시 후 다시 시도해주세요.",
                                errorType = ErrorType.SERVER
                            )
                        }

                        // 404 또는 기타 4xx 에러는 카테고리 없음으로 간주 (온보딩 필요)
                        Log.d("Auth:Reissue", "Categories API returned ${statusCode}, treating as no categories (onboarding needed)")
                        false
                    }
                    categoriesResponse.body()?.data == null || categoriesResponse.body()?.data!!.isEmpty() -> {
                        Log.d("Auth:Reissue", "Step 5: User has no categories (onboarding needed)")
                        false
                    }
                    else -> {
                        val categoryCount = categoriesResponse.body()?.data?.size ?: 0
                        Log.d("Auth:Reissue", "Step 5 SUCCESS: User has $categoryCount categories (onboarding completed)")
                        true
                    }
                }

                // Step 6: 온보딩 상태 DataStore에 저장
                Log.d("Auth:Reissue", "Step 6: Saving onboarding status to DataStore: $onboardingCompleted")
                preferencesManager.setOnboardingCompleted(onboardingCompleted)

                val user = userResponse.toUser(onboardingCompleted = onboardingCompleted)
                Log.d("Auth:Reissue", "=== Token Reissue SUCCESS === User: ${user.email}")
                Result.Success(user)

            } catch (e: SocketTimeoutException) {
                Log.e("Auth:Reissue", "Timeout error", e)
                Result.Error(
                    exception = e,
                    message = "요청 시간이 초과되었습니다.",
                    errorType = ErrorType.TIMEOUT
                )
            } catch (e: IOException) {
                Log.e("Auth:Reissue", "Network error", e)
                Result.Error(
                    exception = e,
                    message = "인터넷 연결을 확인해주세요.",
                    errorType = ErrorType.NETWORK
                )
            } catch (e: Exception) {
                Log.e("Auth:Reissue", "Unknown error", e)
                Result.Error(
                    exception = e,
                    message = e.message ?: "토큰 재발급 중 오류가 발생했습니다.",
                    errorType = ErrorType.UNKNOWN
                )
            }
        }
    }
}
