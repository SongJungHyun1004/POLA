// data/remote/dto/response/ReportResponse.kt
package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReportDto(
    @Json(name = "id")
    val id: Long,

    @Json(name = "reportType")
    val reportType: String,

    @Json(name = "title")
    val title: String,

    @Json(name = "description")
    val description: String,

    @Json(name = "imageUrl")
    val imageUrl: String?,

    @Json(name = "reportWeek")
    val reportWeek: String,

    @Json(name = "createdAt")
    val createdAt: String,

    @Json(name = "analysisStartDate")
    val analysisStartDate: String,

    @Json(name = "analysisEndDate")
    val analysisEndDate: String,

    @Json(name = "score")
    val score: Double
)

@JsonClass(generateAdapter = true)
data class ReportListResponse(
    @Json(name = "data")
    val data: List<ReportDto>,

    @Json(name = "message")
    val message: String,

    @Json(name = "status")
    val status: String
)

@JsonClass(generateAdapter = true)
data class LatestReportResponse(
    @Json(name = "data")
    val data: ReportDto?,

    @Json(name = "message")
    val message: String,

    @Json(name = "status")
    val status: String
)