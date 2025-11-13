package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RagSearchResponse(
    @Json(name = "data")
    val data: RagSearchData?,

    @Json(name = "message")
    val message: String,

    @Json(name = "status")
    val status: String,

    @Json(name = "code")
    val code: String? = null
)

@JsonClass(generateAdapter = true)
data class RagSearchData(
    @Json(name = "answer")
    val answer: String,

    @Json(name = "sources")
    val sources: List<SourceDto>
)

@JsonClass(generateAdapter = true)
data class SourceDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "tags")
    val tags: List<String> = emptyList(),

    @Json(name = "src")
    val src: String,

    @Json(name = "context")
    val context: String,

    @Json(name = "relevanceScore")
    val relevanceScore: Double
)
