package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HomeDataResponse(
    @Json(name = "data")
    val data: HomeData,

    @Json(name = "message")
    val message: String,

    @Json(name = "status")
    val status: String
)

@JsonClass(generateAdapter = true)
data class HomeData(
    @Json(name = "categories")
    val categories: List<CategoryData>,

    @Json(name = "favorites")
    val favorites: List<HomeFileData>,

    @Json(name = "reminds")
    val reminds: List<HomeFileData>,

    @Json(name = "timeline")
    val timeline: List<HomeFileData>
)

@JsonClass(generateAdapter = true)
data class CategoryData(
    @Json(name = "categoryId")
    val categoryId: Long,

    @Json(name = "categoryName")
    val categoryName: String,

    @Json(name = "files")
    val files: List<HomeFileData>
)

@JsonClass(generateAdapter = true)
data class HomeFileData(
    @Json(name = "id")
    val id: Long,

    @Json(name = "src")
    val src: String,

    @Json(name = "type")
    val type: String,

    @Json(name = "context")
    val context: String,

    @Json(name = "favorite")
    val favorite: Boolean
)