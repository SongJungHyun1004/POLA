package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FavoriteResponse(
    @Json(name = "data") val data: FavoriteFileData,
    @Json(name = "message") val message: String,
    @Json(name = "status") val status: String,
    @Json(name = "code") val code: String? = null
)

@JsonClass(generateAdapter = true)
data class FavoriteFileData(
    @Json(name = "id") val id: Long,
    @Json(name = "userId") val userId: Long,
    @Json(name = "categoryId") val categoryId: Long,
    @Json(name = "src") val src: String,
    @Json(name = "type") val type: String,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "context") val context: String,
    @Json(name = "ocrText") val ocrText: String? = null,
    @Json(name = "vectorId") val vectorId: String? = null,
    @Json(name = "fileSize") val fileSize: Long,
    @Json(name = "shareStatus") val shareStatus: Boolean,
    @Json(name = "favorite") val favorite: Boolean,
    @Json(name = "favoriteSort") val favoriteSort: Int,
    @Json(name = "favoritedAt") val favoritedAt: String? = null,
    @Json(name = "views") val views: Int,
    @Json(name = "platform") val platform: String? = null,
    @Json(name = "originUrl") val originUrl: String? = null,
    @Json(name = "lastViewedAt") val lastViewedAt: String? = null
)
