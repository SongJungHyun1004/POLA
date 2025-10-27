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