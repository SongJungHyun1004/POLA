package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.request.GoogleLoginRequest
import com.jinjinjara.pola.data.remote.dto.request.LoginRequest
import com.jinjinjara.pola.data.remote.dto.request.RefreshTokenRequest
import com.jinjinjara.pola.data.remote.dto.request.SignUpRequest
import com.jinjinjara.pola.data.remote.dto.response.AuthResponse
import com.jinjinjara.pola.data.remote.dto.response.RefreshTokenResponse
import com.jinjinjara.pola.data.remote.dto.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * 인증 관련 API
 */
interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    // Google
    @POST("auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest
    ): Response<AuthResponse>

    @POST("auth/signup")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<UserResponse>
}