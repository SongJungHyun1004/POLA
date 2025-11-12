package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.request.RagSearchRequest
import com.jinjinjara.pola.data.remote.dto.response.RagSearchResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RagSearchApi {

    @POST("rag/search")
    suspend fun search(
        @Body request: RagSearchRequest
    ): Response<RagSearchResponse>
}
