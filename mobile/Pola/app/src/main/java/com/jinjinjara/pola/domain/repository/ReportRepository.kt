package com.jinjinjara.pola.domain.repository

import com.jinjinjara.pola.domain.model.Report
import com.jinjinjara.pola.util.Result

interface ReportRepository {
    suspend fun getMyReports(): Result<List<Report>>
}