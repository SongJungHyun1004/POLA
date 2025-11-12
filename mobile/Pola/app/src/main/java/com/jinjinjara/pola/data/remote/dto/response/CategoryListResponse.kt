package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategoryListResponse(
    @Json(name = "data") val data: List<CategoryDto>,
    @Json(name = "message") val message: String,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class CategoryDto(
    @Json(name = "id") val id: Long,
    @Json(name = "categoryName") val categoryName: String,
    @Json(name = "categorySort") val categorySort: Int,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "userEmail") val userEmail: String? = null
)
