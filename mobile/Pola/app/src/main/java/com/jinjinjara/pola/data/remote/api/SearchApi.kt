package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.response.TagSearchResponse
import com.jinjinjara.pola.data.remote.dto.response.TagSuggestionsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi {

    @GET("search/tag-suggestions")
    suspend fun getTagSuggestions(
        @Query("keyword") keyword: String
    ): Response<TagSuggestionsResponse>

    @GET("search/tags")
    suspend fun searchFilesByTag(
        @Query("tag") tag: String
    ): Response<TagSearchResponse>

    @GET("search/all")
    suspend fun searchAll(
        @Query("keyword") keyword: String
    ): Response<TagSearchResponse>
}