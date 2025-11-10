package com.jinjinjara.pola.data.remote.dto.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email")
    val email: String,

    @Json(name = "password")
    val password: String
)

@JsonClass(generateAdapter = true)
data class SignUpRequest(
    @Json(name = "email")
    val email: String,

    @Json(name = "password")
    val password: String,

    @Json(name = "name")
    val name: String
)

@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    @Json(name = "refresh_token")
    val refreshToken: String
)


// Google
@JsonClass(generateAdapter = true)
data class GoogleLoginRequest(
    @Json(name = "id_token")
    val idToken: String
)

// OAuth
@JsonClass(generateAdapter = true)
data class OAuthTokenRequest(
    @Json(name = "idToken")
    val idToken: String
)

@JsonClass(generateAdapter = true)
data class OAuthSignupRequest(
    @Json(name = "email")
    val email: String,

    @Json(name = "username")
    val username: String
)

@JsonClass(generateAdapter = true)
data class OAuthSigninRequest(
    @Json(name = "email")
    val email: String,

    @Json(name = "username")
    val username: String
)

// Category
@JsonClass(generateAdapter = true)
data class CategoryTagInitRequest(
    @Json(name = "categories")
    val categories: List<CategoryWithTags>
)

@JsonClass(generateAdapter = true)
data class CategoryWithTags(
    @Json(name = "categoryName")
    val categoryName: String,

    @Json(name = "tags")
    val tags: List<String>
)