package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.response.RemindResponse
import retrofit2.Response
import retrofit2.http.GET

interface RemindApi {

    @GET("files/reminders")
    suspend fun getReminders(): Response<RemindResponse>
}
