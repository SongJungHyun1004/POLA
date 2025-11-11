package com.jinjinjara.pola.data.remote.interceptor

import android.util.Log
import com.jinjinjara.pola.data.local.datastore.PreferencesDataStore
import com.jinjinjara.pola.data.remote.api.AuthApi
import com.jinjinjara.pola.data.remote.dto.request.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Access Token 만료 시 자동으로 토큰을 갱신하는 Authenticator
 * 401 Unauthorized 응답 시 Refresh Token으로 새 Access Token을 발급받음
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val authApi: dagger.Lazy<AuthApi>
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("Auth:Token", "=== Token Refresh Triggered === URL: ${response.request.url}")

        // 이미 재시도한 요청이면 null 반환하여 무한 루프 방지
        if (response.request.header("Token-Refreshed") != null) {
            Log.w("Auth:Token", "Already retried with refreshed token, stopping")
            return null
        }

        // refresh 엔드포인트 자체가 실패한 경우 null 반환
        if (response.request.url.encodedPath.contains("/auth/refresh")) {
            Log.e("Auth:Token", "Refresh token endpoint failed, logging out")
            clearTokensAndLogout()
            return null
        }

        // 현재 요청에 사용된 토큰 추출
        val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()

        return runBlocking {
            mutex.withLock {
                Log.d("Auth:Token", "Lock acquired, checking if token was already refreshed")

                // 현재 저장된 토큰 가져오기
                val currentToken = preferencesDataStore.getAccessToken()

                // 토큰이 이미 갱신되었는지 확인 (다른 스레드가 이미 갱신함)
                if (!currentToken.isNullOrEmpty() && currentToken != requestToken) {
                    Log.d("Auth:Token", "Token already refreshed by another request, using new token")
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .header("Token-Refreshed", "true")
                        .build()
                }

                Log.d("Auth:Token", "Attempting to refresh access token")

                // 1. Refresh Token 가져오기
                val refreshToken = preferencesDataStore.getRefreshToken()

                if (refreshToken.isNullOrEmpty()) {
                    Log.e("Auth:Token", "No refresh token available, forcing logout")
                    clearTokensAndLogout()
                    return@withLock null
                }
                Log.d("Auth:Token", "Refresh token found: ${refreshToken.take(20)}...")

                try {
                    // 2. 새 Access Token 발급 요청
                    Log.d("Auth:Token", "Requesting new access token from server")
                    val tokenResponse = authApi.get().refreshToken(
                        RefreshTokenRequest(refreshToken)
                    )

                    if (tokenResponse.isSuccessful && tokenResponse.body() != null) {
                        val newAccessToken = tokenResponse.body()!!.accessToken

                        Log.d("Auth:Token", "=== Token Refresh SUCCESS ===")
                        Log.d("Auth:Token", "New access token: ${newAccessToken.take(20)}...")

                        // 3. 새 토큰 저장
                        preferencesDataStore.saveAccessToken(newAccessToken)

                        // 4. 실패한 요청을 새 토큰으로 재시도
                        Log.d("Auth:Token", "Retrying original request with new token")
                        return@withLock response.request.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .header("Token-Refreshed", "true")
                            .build()
                    } else {
                        // Refresh Token도 만료된 경우 로그아웃 처리
                        Log.e("Auth:Token", "=== Token Refresh FAILED === Refresh token expired, forcing logout")
                        clearTokensAndLogout()
                        return@withLock null
                    }
                } catch (e: Exception) {
                    Log.e("Auth:Token", "=== Token Refresh FAILED === Exception occurred", e)
                    clearTokensAndLogout()
                    return@withLock null
                }
            }
        }
    }

    // 토큰을 삭제하고 로그아웃 처리
    private fun clearTokensAndLogout() {
        runBlocking {
            Log.d("Auth:Token", "Clearing all tokens for forced logout")
            preferencesDataStore.clearTokens()
            Log.d("Auth:Token", "All tokens cleared, user must login again")
        }
    }
}
