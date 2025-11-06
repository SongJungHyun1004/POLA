package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PresignedUrlResponse(
    @Json(name = "data")
    val data: PresignedUrlData,

    @Json(name = "message")
    val message: String,

    @Json(name = "status")
    val status: String
)

@JsonClass(generateAdapter = true)
data class PresignedUrlData(
    @Json(name = "url")
    val url: String,

    @Json(name = "key")
    val key: String
)

@JsonClass(generateAdapter = true)
data class FileCompleteResponse(
    @Json(name = "data")
    val data: FileData,

    @Json(name = "message")
    val message: String,

    @Json(name = "status")
    val status: String
)

@JsonClass(generateAdapter = true)
data class FileData(
    @Json(name = "id")
    val id: Long,

    @Json(name = "userId")
    val userId: Long,

    @Json(name = "src")
    val src: String,

    @Json(name = "type")
    val type: String,

    @Json(name = "originUrl")
    val originUrl: String
)