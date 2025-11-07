package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.response.HomeDataResponse
import retrofit2.Response
import retrofit2.http.GET

interface HomeApi {
    @GET("users/me/home")
    suspend fun getHomeData(): Response<HomeDataResponse>
}