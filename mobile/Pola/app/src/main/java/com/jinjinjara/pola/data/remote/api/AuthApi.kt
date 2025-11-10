package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.request.GoogleLoginRequest
import com.jinjinjara.pola.data.remote.dto.request.LoginRequest
import com.jinjinjara.pola.data.remote.dto.request.OAuthSigninRequest
import com.jinjinjara.pola.data.remote.dto.request.OAuthSignupRequest
import com.jinjinjara.pola.data.remote.dto.request.OAuthTokenRequest
import com.jinjinjara.pola.data.remote.dto.request.RefreshTokenRequest
import com.jinjinjara.pola.data.remote.dto.request.SignUpRequest
import com.jinjinjara.pola.data.remote.dto.response.AuthResponse
import com.jinjinjara.pola.data.remote.dto.response.OAuthApiResponse
import com.jinjinjara.pola.data.remote.dto.response.OAuthReissueResponse
import com.jinjinjara.pola.data.remote.dto.response.OAuthTokenData
import com.jinjinjara.pola.data.remote.dto.response.OAuthVerifyData
import com.jinjinjara.pola.data.remote.dto.response.RefreshTokenResponse
import com.jinjinjara.pola.data.remote.dto.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
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

    @GET("users/me")
    suspend fun getUser(): Response<OAuthApiResponse<UserResponse>>

    @GET("users/me/categories")
    suspend fun getUserCategories(): Response<OAuthApiResponse<Any>>

    // OAuth 2.0 엔드포인트
    @POST("oauth/token")
    suspend fun getOAuthToken(
        @Body request: OAuthTokenRequest
    ): Response<OAuthApiResponse<OAuthTokenData>>

    @POST("oauth/signup")
    suspend fun oauthSignup(
        @Body request: OAuthSignupRequest
    ): Response<OAuthApiResponse<OAuthTokenData>>

    @POST("oauth/signin")
    suspend fun oauthSignin(
        @Body request: OAuthSigninRequest
    ): Response<OAuthApiResponse<OAuthTokenData>>

    @GET("oauth/verify")
    @Headers("X-Client-Type: APP")
    suspend fun oauthVerify(): Response<OAuthApiResponse<OAuthVerifyData>>

    @POST("oauth/reissue")
    @Headers("X-Client-Type: APP")
    suspend fun oauthReissue(
        @Header("Authorization") refreshToken: String
    ): Response<OAuthReissueResponse>
}