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
import com.jinjinjara.pola.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
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

    /** Google 로그인 */
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
                authApi.logout()
                clearTokens()
                Result.Success(Unit)
            } catch (e: Exception) {
                clearTokens()
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

    /** OAuth 2.0 Google 로그인 (새로운 플로우) */
    override suspend fun googleLoginWithOAuth(idToken: String, displayName: String): Result<User> {
        return withContext(ioDispatcher) {
            try {
                Log.d("AuthRepository", "OAuth flow started")

                // Step 1: Google ID Token으로 임시 JWT 획득
                val tokenResponse = authApi.getOAuthToken(OAuthTokenRequest(idToken))
                if (!tokenResponse.isSuccessful || tokenResponse.body()?.data == null) {
                    val errorBody = tokenResponse.errorBody()?.string()
                    Log.e("AuthRepository", "=== OAuth Token Request Failed ===")
                    Log.e("AuthRepository", "Status Code: ${tokenResponse.code()}")
                    Log.e("AuthRepository", "Status Message: ${tokenResponse.message()}")
                    Log.e("AuthRepository", "Error Body: $errorBody")
                    Log.e("AuthRepository", "Request URL: ${tokenResponse.raw().request.url}")
                    Log.e("AuthRepository", "ID Token (full):")
                    Log.e("AuthRepository", idToken)
                    return@withContext Result.Error(
                        exception = Exception(tokenResponse.message()),
                        message = "Google 인증에 실패했습니다. (${tokenResponse.code()})"
                    )
                }

                // 임시 토큰 저장 (signin/signup 요청에 사용될 수 있음)
                val tempTokenData = tokenResponse.body()!!.data!!
                Log.d("AuthRepository", "Temp tokens received")

                // Step 2: Google ID Token에서 email 추출
                val email = extractEmailFromIdToken(idToken)
                if (email == null) {
                    Log.e("AuthRepository", "Failed to extract email from ID token")
                    return@withContext Result.Error(
                        exception = Exception("Invalid ID Token"),
                        message = "Google 계정 정보를 가져올 수 없습니다."
                    )
                }
                Log.d("AuthRepository", "Email extracted: $email")

                // Step 3: 먼저 signin 시도 (기존 사용자)
                val signinRequest = OAuthSigninRequest(email, displayName)
                val signinResponse = authApi.oauthSignin(signinRequest)

                val finalTokenData = if (signinResponse.isSuccessful && signinResponse.body()?.data != null) {
                    Log.d("AuthRepository", "Signin successful (existing user)")
                    signinResponse.body()!!.data!!
                } else {
                    // Step 4: signin 실패 -> signup 시도 (신규 사용자)
                    Log.d("AuthRepository", "Signin failed, trying signup (new user)")
                    val signupRequest = OAuthSignupRequest(email, displayName)
                    val signupResponse = authApi.oauthSignup(signupRequest)

                    if (signupResponse.isSuccessful && signupResponse.body()?.data != null) {
                        Log.d("AuthRepository", "Signup successful")
                        signupResponse.body()!!.data!!
                    } else {
                        Log.e("AuthRepository", "Both signin and signup failed")
                        return@withContext Result.Error(
                            exception = Exception(signupResponse.message()),
                            message = "로그인에 실패했습니다."
                        )
                    }
                }

                // Step 5: 최종 토큰 저장
                saveTokens(
                    accessToken = finalTokenData.accessToken,
                    refreshToken = finalTokenData.refreshToken
                )
                Log.d("AuthRepository", "Final tokens saved")

                // Step 6: 사용자 정보 가져오기
                getCurrentUser()

            } catch (e: Exception) {
                Log.e("AuthRepository", "OAuth flow exception: ${e.message}", e)
                Result.Error(
                    exception = e,
                    message = e.message ?: "OAuth 로그인 중 오류가 발생했습니다."
                )
            }
        }
    }

    /**
     * Google ID Token(JWT)에서 email 추출
     */
    private fun extractEmailFromIdToken(idToken: String): String? {
        return try {
            val parts = idToken.split(".")
            if (parts.size < 2) return null

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val json = JSONObject(payload)
            json.optString("email", null)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to extract email: ${e.message}")
            null
        }
    }
}
