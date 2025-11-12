package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FileTagsResponse(
    @Json(name = "data")
    val data: List<FileTag>,
    @Json(name = "message")
    val message: String,
    @Json(name = "status")
    val status: String
)