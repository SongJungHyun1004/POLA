package com.jinjinjara.pola.data.remote.interceptor

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * API 요청에 인증 토큰을 추가하는 인터셉터
 */
class AuthInterceptor @Inject constructor(
    // private val preferencesManager: PreferencesManager // DataStore 주입
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 토큰이 필요없는 요청은 그대로 진행
        if (originalRequest.url.encodedPath.contains("/auth/")) {
            return chain.proceed(originalRequest)
        }

        // DataStore에서 토큰 가져오기 (실제 구현 시)
        // val token = runBlocking {
        //     preferencesManager.getAccessToken()
        // }

        val token = "your_access_token_here" // 임시

        // 토큰이 있으면 헤더에 추가
        val newRequest = if (token.isNotEmpty()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}