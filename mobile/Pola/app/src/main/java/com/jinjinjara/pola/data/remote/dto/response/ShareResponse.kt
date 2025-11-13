package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShareResponse(
    @Json(name = "data")
    val data: ShareData,
    @Json(name = "message")
    val message: String,
    @Json(name = "status")
    val status: String
)

data class ShareData(
    @Json(name = "shareUrl")
    val shareUrl: String,
    @Json(name = "expiredAt")
    val expiredAt: String
)