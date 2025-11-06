package com.jinjinjara.pola.data.repository

import android.util.Base64
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
                Log.d("Auth:Logout", "Sending logout request to server")
                authApi.logout()
                Log.d("Auth:Logout", "Server logout successful")
                clearTokens()
                Log.d("Auth:Logout", "Local tokens cleared")
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.w("Auth:Logout", "Server logout failed, clearing local tokens anyway", e)
                clearTokens()
                Log.d("Auth:Logout", "Local tokens cleared")
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
                    Log.e("Auth:User", "Failed to fetch user info: ${response.message()}")
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

                // Step 1: Google ID Token으로 임시 JWT 획득
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

                // 임시 토큰 저장
                val tempTokenData = tokenResponse.body()!!.data!!
                Log.d("Auth:Login", "Step 1 SUCCESS: OAuth token received")

                // Step 2: Google ID Token에서 email 추출
                Log.d("Auth:Login", "Step 2: Extracting email from ID token")
                val email = extractEmailFromIdToken(idToken)
                if (email == null) {
                    Log.e("Auth:Login", "Step 2 FAILED: Cannot extract email from ID token")
                    return@withContext Result.Error(
                        exception = Exception("Invalid ID Token"),
                        message = "Google 계정 정보를 가져올 수 없습니다."
                    )
                }
                Log.d("Auth:Login", "Step 2 SUCCESS: Email extracted -> $email")

                // Step 3: 먼저 signin 시도
                Log.d("Auth:Login", "Step 3: Trying signin for existing user -> $email")
                val signinRequest = OAuthSigninRequest(email, displayName)
                val signinResponse = authApi.oauthSignin(signinRequest)

                val finalTokenData = if (signinResponse.isSuccessful && signinResponse.body()?.data != null) {
                    Log.d("Auth:Login", "Step 3 SUCCESS: Existing user signin -> $email")
                    signinResponse.body()!!.data!!
                } else {
                    // Step 4: signin 실패 시 signup 시도
                    Log.d("Auth:Login", "Step 3 SKIP: Not existing user, trying signup -> $email")
                    Log.d("Auth:Login", "Step 4: Trying signup for new user -> $email")
                    val signupRequest = OAuthSignupRequest(email, displayName)
                    val signupResponse = authApi.oauthSignup(signupRequest)

                    if (signupResponse.isSuccessful && signupResponse.body()?.data != null) {
                        Log.d("Auth:Login", "Step 4 SUCCESS: New user signup -> $email")
                        signupResponse.body()!!.data!!
                    } else {
                        Log.e("Auth:Login", "Step 4 FAILED: Both signin and signup failed -> $email")
                        return@withContext Result.Error(
                            exception = Exception(signupResponse.message()),
                            message = "로그인에 실패했습니다."
                        )
                    }
                }

                // Step 5: 최종 토큰 저장
                Log.d("Auth:Login", "Step 5: Saving tokens")
                saveTokens(
                    accessToken = finalTokenData.accessToken,
                    refreshToken = finalTokenData.refreshToken
                )
                Log.d("Auth:Token", "Access token saved: ${finalTokenData.accessToken.take(20)}...")
                Log.d("Auth:Token", "Refresh token saved: ${finalTokenData.refreshToken.take(20)}...")

                // Step 6: 사용자 정보 가져오기
                Log.d("Auth:Login", "Step 6: Getting current user info")
                val userResult = getUser()
                if (userResult is Result.Success) {
                    Log.d("Auth:Login", "=== Google OAuth Login SUCCESS === User: ${userResult.data.email}")
                }
                return@withContext userResult

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

    // Google ID Token에서 email 추출
    private fun extractEmailFromIdToken(idToken: String): String? {
        return try {
            val parts = idToken.split(".")
            if (parts.size < 2) {
                Log.e("Auth:Login", "Invalid ID token format: parts=${parts.size}")
                return null
            }

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val json = JSONObject(payload)
            json.optString("email", null)
        } catch (e: Exception) {
            Log.e("Auth:Login", "Failed to parse ID token", e)
            null
        }
    }
}
