package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemindResponse(
    @Json(name = "data") val data: List<RemindFileData>,
    @Json(name = "message") val message: String,
    @Json(name = "status") val status: String,
    @Json(name = "code") val code: String? = null
)

@JsonClass(generateAdapter = true)
data class RemindFileData(
    @Json(name = "id") val id: Long,
    @Json(name = "src") val src: String,
    @Json(name = "type") val type: String,
    @Json(name = "context") val context: String,
    @Json(name = "favorite") val favorite: Boolean,
    @Json(name = "tags") val tags: List<String> = emptyList()
)
