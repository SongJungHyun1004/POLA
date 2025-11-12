package com.jinjinjara.pola.data.remote.dto.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FileCompleteRequest(
    @Json(name = "key")
    val key: String,

    @Json(name = "type")
    val type: String,

    @Json(name = "fileSize")
    val fileSize: Long,

    @Json(name = "originUrl")
    val originUrl: String,

    @Json(name = "platform")
    val platform: String,
)