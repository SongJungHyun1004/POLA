package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FilesListResponse(
    @Json(name = "data") val data: FilesPageData,
    @Json(name = "message") val message: String,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class FilesPageData(
    @Json(name = "content") val content: List<FileItemDto>,
    @Json(name = "page") val page: Int,
    @Json(name = "size") val size: Int,
    @Json(name = "totalElements") val totalElements: Int,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "last") val last: Boolean
)

@JsonClass(generateAdapter = true)
data class FileItemDto(
    @Json(name = "id") val id: Long,
    @Json(name = "src") val src: String,
    @Json(name = "type") val type: String,
    @Json(name = "context") val context: String,
    @Json(name = "favorite") val favorite: Boolean,
    @Json(name = "tags") val tags: List<String>,
    @Json(name = "createdAt") val createdAt: String
)