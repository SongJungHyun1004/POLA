package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.request.CategoryTagInitRequest
import com.jinjinjara.pola.data.remote.dto.request.FilesListRequest
import com.jinjinjara.pola.data.remote.dto.response.CategoryListResponse
import com.jinjinjara.pola.data.remote.dto.response.CategoryRecommendationsResponse
import com.jinjinjara.pola.data.remote.dto.response.FileDetailResponse
import com.jinjinjara.pola.data.remote.dto.response.FilesListResponse
import com.jinjinjara.pola.data.remote.dto.response.OAuthApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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

    /**
     * 파일 단건의 상세 정보를 반환
     * 호출 시 해당 파일의 조회수가 1 증가하고, 마지막 열람 시각(lastViewedAt)이 갱신됨
     */
    @GET("files/{fileId}")
    suspend fun getFileDetail(
        @Path("fileId") fileId: Long
    ): Response<FileDetailResponse>
}