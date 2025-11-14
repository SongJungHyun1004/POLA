package com.jinjinjara.pola.data.mapper

import com.jinjinjara.pola.data.remote.dto.response.ReportDto
import com.jinjinjara.pola.domain.model.Report

fun ReportDto.toDomain(): Report {
    return Report(
        id = id,
        reportType = reportType,
        title = title,
        description = description,
        imageUrl = imageUrl,
        reportWeek = reportWeek,
        createdAt = createdAt,
        analysisStartDate = analysisStartDate,
        analysisEndDate = analysisEndDate,
        score = score
    )
}