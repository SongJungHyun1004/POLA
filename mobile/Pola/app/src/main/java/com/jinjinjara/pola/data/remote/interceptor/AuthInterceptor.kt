package com.jinjinjara.pola.data.remote.interceptor

import android.util.Log
import com.jinjinjara.pola.data.local.datastore.PreferencesDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * API 요청에 인증 토큰을 추가하는 인터셉터
 */
class AuthInterceptor @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // 토큰이 필요없는 요청은 그대로 진행 (로그인, 회원가입 등)
        val skipTokenPaths = listOf(
            "/auth/login",
            "/auth/google",
            "/auth/signup",
            "/oauth/token",
            "/oauth/signup",
            "/oauth/signin"
        )

        if (skipTokenPaths.any { path.contains(it) }) {
            Log.d("AuthInterceptor", "Public endpoint, skip token: $path")
            return chain.proceed(originalRequest)
        }

        // DataStore에서 토큰 가져오기
        val token = runBlocking {
            preferencesDataStore.getAccessToken()
        }
        Log.d("AuthInterceptor", "Token retrieved: ${if (token.isNullOrEmpty()) "null/empty" else "exists"}")

        // 토큰이 있으면 헤더에 추가
        val newRequest = if (!token.isNullOrEmpty()) {
            Log.d("AuthInterceptor", "Adding Authorization header to: $path")
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            Log.w("AuthInterceptor", "No token for request: $path")
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}