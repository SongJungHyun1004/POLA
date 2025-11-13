package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TagSuggestionsResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: TagSuggestionsData?
)

@JsonClass(generateAdapter = true)
data class TagSuggestionsData(
    @Json(name = "tags") val tags: List<String>,
    @Json(name = "count") val count: Int
)