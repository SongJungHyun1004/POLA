package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.request.CategoryTagInitRequest
import com.jinjinjara.pola.data.remote.dto.response.CategoryRecommendationsResponse
import com.jinjinjara.pola.data.remote.dto.response.OAuthApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Category related API
 */
interface CategoryApi {

    @GET("categories/tags/recommendations")
    suspend fun getCategoryRecommendations(): Response<CategoryRecommendationsResponse>

    @POST("categories/tags/init")
    suspend fun initCategoryTags(
        @Body request: CategoryTagInitRequest
    ): Response<Unit>
}