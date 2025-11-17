package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.request.CategoryTagInitRequest
import com.jinjinjara.pola.data.remote.dto.request.FilesListRequest
import com.jinjinjara.pola.data.remote.dto.response.AddCategoryTagsResponse
import com.jinjinjara.pola.data.remote.dto.response.CategoryListResponse
import com.jinjinjara.pola.data.remote.dto.response.CategoryRecommendationsResponse
import com.jinjinjara.pola.data.remote.dto.response.CreateCategoryResponse
import com.jinjinjara.pola.data.remote.dto.response.FileDetailResponse
import com.jinjinjara.pola.data.remote.dto.response.FilesListResponse
import com.jinjinjara.pola.data.remote.dto.response.OAuthApiResponse
import com.jinjinjara.pola.data.remote.dto.response.UserCategoriesWithTagsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

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

    @POST("files/list")
    suspend fun getFilesList(
        @Body request: FilesListRequest
    ): Response<FilesListResponse>


    @GET("users/me/categories")
    suspend fun getCategories(): Response<CategoryListResponse>

    @GET("users/me/categories/tags")
    suspend fun getUserCategoriesWithTags(): Response<UserCategoriesWithTagsResponse>

    @PUT("users/me/categories/{id}")
    suspend fun updateCategory(
        @Path("id") categoryId: Long,
        @Query("name") name: String
    ): Response<Unit>

    @POST("users/me/categories")
    suspend fun createCategory(
        @Query("name") name: String
    ): Response<CreateCategoryResponse>

    @DELETE("users/me/categories/{id}")
    suspend fun deleteCategory(
        @Path("id") categoryId: Long
    ): Response<Unit>

    @DELETE("categories/{categoryId}/tags/{tagId}")
    suspend fun removeTagFromCategory(
        @Path("categoryId") categoryId: Long,
        @Path("tagId") tagId: Long
    ): Response<Unit>

    @POST("categories/{categoryId}/tags")
    suspend fun addTagsToCategory(
        @Path("categoryId") categoryId: Long,
        @Body tagNames: List<String>
    ): Response<AddCategoryTagsResponse>

}