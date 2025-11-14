package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.response.ReportListResponse
import retrofit2.Response
import retrofit2.http.GET

/**
 * Report related API
 */
interface ReportApi {

    @GET("users/me/reports")
    suspend fun getMyReports(): Response<ReportListResponse>
}