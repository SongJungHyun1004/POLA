package com.jinjinjara.pola.data.remote.dto.request

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FilesListRequest(
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val direction: String = "DESC",
    val filterType: String? = "category", // category | favorite | tag | null
    val filterId: Long? = null
)