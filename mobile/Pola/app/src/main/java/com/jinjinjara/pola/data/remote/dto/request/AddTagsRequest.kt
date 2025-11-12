package com.jinjinjara.pola.data.remote.dto.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddTagsRequest(
    @Json(name = "tagNames")
    val tagNames: List<String>
)