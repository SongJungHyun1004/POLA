package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.request.OAuthTokenRequest
import com.jinjinjara.pola.data.remote.dto.response.OAuthApiResponse
import com.jinjinjara.pola.data.remote.dto.response.OAuthReissueResponse
import com.jinjinjara.pola.data.remote.dto.response.OAuthTokenData
import com.jinjinjara.pola.data.remote.dto.response.OAuthVerifyData
import com.jinjinjara.pola.data.remote.dto.response.UserCategoriesResponse
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

    @POST("oauth/logout")
    @Headers("X-Client-Type: APP")
    suspend fun logout(
        @Header("Authorization") refreshToken: String
    ): Response<Unit>

    @GET("users/me")
    suspend fun getUser(): Response<OAuthApiResponse<UserResponse>>

    @GET("users/me/categories")
    suspend fun getUserCategories(): Response<UserCategoriesResponse>

    // OAuth 2.0 엔드포인트
    @POST("oauth/token")
    suspend fun getOAuthToken(
        @Body request: OAuthTokenRequest
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