package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FilePostProcessResponse(
    @Json(name = "data")
    val data: FilePostProcessData,

    @Json(name = "message")
    val message: String,

    @Json(name = "status")
    val status: String,

    @Json(name = "code")
    val code: String
)

@JsonClass(generateAdapter = true)
data class FilePostProcessData(
    @Json(name = "id")
    val id: Long,

    @Json(name = "userId")
    val userId: Long,

    @Json(name = "categoryId")
    val categoryId: Long,

    @Json(name = "src")
    val src: String,

    @Json(name = "type")
    val type: String,

    @Json(name = "createdAt")
    val createdAt: String,

    @Json(name = "context")
    val context: String?,

    @Json(name = "ocrText")
    val ocrText: String?,

    @Json(name = "vectorId")
    val vectorId: Long?,

    @Json(name = "fileSize")
    val fileSize: Long,

    @Json(name = "shareStatus")
    val shareStatus: Boolean,

    @Json(name = "favorite")
    val favorite: Boolean,

    @Json(name = "favoriteSort")
    val favoriteSort: Int,

    @Json(name = "favoritedAt")
    val favoritedAt: String?,

    @Json(name = "views")
    val views: Int,

    @Json(name = "platform")
    val platform: String,

    @Json(name = "originUrl")
    val originUrl: String,

    @Json(name = "lastViewedAt")
    val lastViewedAt: String?
)