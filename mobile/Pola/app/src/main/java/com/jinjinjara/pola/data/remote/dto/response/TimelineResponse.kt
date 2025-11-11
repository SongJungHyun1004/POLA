package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TimelineResponse(
    @Json(name = "data") val data: TimelineData,
    @Json(name = "message") val message: String,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class TimelineData(
    @Json(name = "content") val content: List<TimelineFileDto>,
    @Json(name = "page") val page: Int,
    @Json(name = "size") val size: Int,
    @Json(name = "totalElements") val totalElements: Int,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "last") val last: Boolean
)

@JsonClass(generateAdapter = true)
data class TimelineFileDto(
    @Json(name = "id") val id: Long,
    @Json(name = "src") val src: String,
    @Json(name = "type") val type: String,
    @Json(name = "context") val context: String?,
    @Json(name = "favorite") val favorite: Boolean,
    @Json(name = "tags") val tags: List<String>,
    @Json(name = "createdAt") val createdAt: String
)
