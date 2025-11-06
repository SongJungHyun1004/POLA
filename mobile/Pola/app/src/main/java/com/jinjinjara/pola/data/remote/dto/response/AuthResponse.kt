package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "access_token")
    val accessToken: String,

    @Json(name = "refresh_token")
    val refreshToken: String,

    @Json(name = "user")
    val user: UserResponse
)

@JsonClass(generateAdapter = true)
data class RefreshTokenResponse(
    @Json(name = "access_token")
    val accessToken: String
)

@JsonClass(generateAdapter = true)
data class UserResponse(
    @Json(name = "id")
    val id: Long,

    @Json(name = "email")
    val email: String,

    @Json(name = "display_name")
    val displayName: String,

    @Json(name = "profile_image_url")
    val profileImageUrl: String? = null,

    @Json(name = "created_at")
    val createdAt: String
)

// OAuth API 공통 응답 래퍼
@JsonClass(generateAdapter = true)
data class OAuthApiResponse<T>(
    @Json(name = "data")
    val data: T?,

    @Json(name = "message")
    val message: String,

    @Json(name = "status")
    val status: String,

    @Json(name = "code")
    val code: String? = null
)

// OAuth 토큰 데이터
@JsonClass(generateAdapter = true)
data class OAuthTokenData(
    @Json(name = "accessToken")
    val accessToken: String,

    @Json(name = "refreshToken")
    val refreshToken: String
)

// OAuth reissue 응답은 data가 String 타입
typealias OAuthReissueResponse = OAuthApiResponse<String>