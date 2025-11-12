package com.jinjinjara.pola.data.remote.dto.request

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShareRequest(
    val expireHours: Int = 24
)