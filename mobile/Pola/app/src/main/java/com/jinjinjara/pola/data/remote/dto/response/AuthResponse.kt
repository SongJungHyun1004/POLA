package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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

// OAuth verify 응답 데이터
@JsonClass(generateAdapter = true)
data class OAuthVerifyData(
    @Json(name = "valid")
    val valid: Boolean,

    @Json(name = "userId")
    val userId: Long,

    @Json(name = "email")
    val email: String
)

@JsonClass(generateAdapter = true)
data class UserCategoryDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "categoryName")
    val categoryName: String,

    @Json(name = "fileCount")
    val fileCount: Int,

    @Json(name = "createdAt")
    val createdAt: String,

    @Json(name = "userEmail")
    val userEmail: String
)

// 카테고리 목록 응답
typealias UserCategoriesResponse = OAuthApiResponse<List<UserCategoryDto>>

// OAuth reissue 응답은 OAuthTokenData 타입
typealias OAuthReissueResponse = OAuthApiResponse<OAuthTokenData>