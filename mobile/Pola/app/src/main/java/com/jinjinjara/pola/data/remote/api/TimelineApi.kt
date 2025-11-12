package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.request.TimelineRequest
import com.jinjinjara.pola.data.remote.dto.response.TimelineResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TimelineApi {
    @POST("files/list")
    suspend fun getTimelineList(@Body request: TimelineRequest): Response<TimelineResponse>
}
