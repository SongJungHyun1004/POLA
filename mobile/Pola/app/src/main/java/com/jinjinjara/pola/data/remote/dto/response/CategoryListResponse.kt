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
    @Json(name = "fileCount") val fileCount: Int,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "userEmail") val userEmail: String? = null
)

@JsonClass(generateAdapter = true)
data class TagDto(
    @Json(name = "id") val id: Long,
    @Json(name = "tagName") val tagName: String
)

@JsonClass(generateAdapter = true)
data class UserCategoryWithTagsDto(
    @Json(name = "categoryId") val categoryId: Long,
    @Json(name = "categoryName") val categoryName: String,
    @Json(name = "tags") val tags: List<TagDto>
)

// OAuthApiResponse를 사용하여 타입 정의
typealias UserCategoriesWithTagsResponse = OAuthApiResponse<List<UserCategoryWithTagsDto>>
