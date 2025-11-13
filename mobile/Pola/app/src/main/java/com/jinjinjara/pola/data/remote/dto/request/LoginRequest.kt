package com.jinjinjara.pola.data.remote.dto.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// OAuth
@JsonClass(generateAdapter = true)
data class OAuthTokenRequest(
    @Json(name = "idToken")
    val idToken: String
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
