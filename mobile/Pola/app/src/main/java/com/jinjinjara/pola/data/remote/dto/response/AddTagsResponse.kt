package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddTagsResponse(
    @Json(name = "data")
    val data: List<FileTagRelation>,
    @Json(name = "message")
    val message: String,
    @Json(name = "status")
    val status: String
)

@JsonClass(generateAdapter = true)
data class FileTagRelation(
    @Json(name = "id")
    val id: Long,
    @Json(name = "fileId")
    val fileId: Long,
    @Json(name = "tagId")
    val tagId: Long
)