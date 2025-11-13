package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TagSearchResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: TagSearchData?
)

@JsonClass(generateAdapter = true)
data class TagSearchData(
    @Json(name = "totalCount") val totalCount: Int,
    @Json(name = "results") val results: List<TagSearchResult>
)

@JsonClass(generateAdapter = true)
data class TagSearchResult(
    @Json(name = "fileId") val fileId: Long,
    @Json(name = "userId") val userId: Long,
    @Json(name = "categoryName") val categoryName: String,
    @Json(name = "tags") val tags: String,
    @Json(name = "context") val context: String,
    @Json(name = "ocrText") val ocrText: String,
    @Json(name = "imageUrl") val imageUrl: String,
    @Json(name = "createdAt") val createdAt: String
)
