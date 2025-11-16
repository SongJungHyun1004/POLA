package com.jinjinjara.pola.domain.model

data class Report(
    val id: Long,
    val reportType: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val reportWeek: String,
    val createdAt: String,
    val analysisStartDate: String,
    val analysisEndDate: String,
    val score: Double
)