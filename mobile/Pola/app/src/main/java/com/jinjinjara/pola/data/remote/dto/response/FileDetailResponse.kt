package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 파일 상세 정보 응답
 */
@JsonClass(generateAdapter = true)
data class FileDetailResponse(
    @Json(name = "data")
    val data: FileDetailData,
    @Json(name = "message")
    val message: String,
    @Json(name = "status")
    val status: String
)

@JsonClass(generateAdapter = true)
data class FileDetailData(
    @Json(name = "id")
    val id: Long,
    @Json(name = "user_id")
    val userId: Long,
    @Json(name = "category_id")
    val categoryId: Long,
    @Json(name = "src")
    val src: String,
    @Json(name = "type")
    val type: String,
    @Json(name = "context")
    val context: String?,
    @Json(name = "ocr_text")
    val ocrText: String?,
    @Json(name = "vector_id")
    val vectorId: Long?,
    @Json(name = "file_size")
    val fileSize: Long,
    @Json(name = "share_status")
    val shareStatus: Boolean,
    @Json(name = "favorite")
    val favorite: Boolean,
    @Json(name = "favorite_sort")
    val favoriteSort: Int,
    @Json(name = "favorited_at")
    val favoritedAt: String?,
    @Json(name = "views")
    val views: Int,
    @Json(name = "platform")
    val platform: String,
    @Json(name = "origin_url")
    val originUrl: String,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "last_viewed_at")
    val lastViewedAt: String?,
    @Json(name = "tags")
    val tags: List<FileTag>
)

@JsonClass(generateAdapter = true)
data class FileTag(
    @Json(name = "id")
    val id: Long,
    @Json(name = "tagName")
    val tagName: String
)
