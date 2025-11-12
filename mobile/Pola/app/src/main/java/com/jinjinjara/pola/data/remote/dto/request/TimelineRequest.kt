package com.jinjinjara.pola.data.remote.dto.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TimelineRequest(
    @Json(name = "page") val page: Int = 0,
    @Json(name = "size") val size: Int = 20,
    @Json(name = "sortBy") val sortBy: String = "createdAt",
    @Json(name = "direction") val direction: String = "DESC",
    @Json(name = "filterType") val filterType: String? = null,
    @Json(name = "filterId") val filterId: Long? = null
)
