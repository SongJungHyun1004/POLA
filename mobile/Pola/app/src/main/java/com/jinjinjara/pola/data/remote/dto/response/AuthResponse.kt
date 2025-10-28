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
    val id: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "profile_image_url")
    val profileImageUrl: String? = null,

    @Json(name = "created_at")
    val createdAt: Long
)