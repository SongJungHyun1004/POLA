package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.request.FavoriteListRequest
import com.jinjinjara.pola.data.remote.dto.response.FavoriteListResponse
import com.jinjinjara.pola.data.remote.dto.response.FavoriteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FavoriteApi {

    @POST("files/list")
    suspend fun getFavoriteList(
        @Body request: FavoriteListRequest
    ): Response<FavoriteListResponse>

    @PUT("files/{fileId}/favorite")
    suspend fun addFavorite(
        @Path("fileId") fileId: Long,
        @Query("sortValue") sortValue: Int = 1
    ): Response<FavoriteResponse>

    @DELETE("files/{fileId}/favorite")
    suspend fun removeFavorite(
        @Path("fileId") fileId: Long
    ): Response<FavoriteResponse>
}
