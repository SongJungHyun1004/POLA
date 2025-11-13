package com.jinjinjara.pola.data.repository

import android.util.Log
import com.jinjinjara.pola.data.mapper.toDomain
import com.jinjinjara.pola.data.remote.api.RagSearchApi
import com.jinjinjara.pola.data.remote.api.SearchApi
import com.jinjinjara.pola.data.remote.dto.request.RagSearchRequest
import com.jinjinjara.pola.domain.model.RagSearchResult
import com.jinjinjara.pola.domain.model.TagSearchFile
import com.jinjinjara.pola.domain.repository.SearchRepository
import com.jinjinjara.pola.util.ErrorType
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val ragSearchApi: RagSearchApi,
    private val searchApi: SearchApi
) : SearchRepository {

    override suspend fun searchWithRag(query: String): Result<RagSearchResult> {
        return try {
            Log.d("SearchRepository", "Searching with RAG: $query")
            val response = ragSearchApi.search(RagSearchRequest(query))

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.data != null) {
                    Log.d("SearchRepository", "Search successful: ${body.data.sources.size} sources found")
                    Result.Success(body.data.toDomain())
                } else {
                    Log.e("SearchRepository", "Search response data is null")
                    Result.Error(
                        message = body.message,
                        errorType = ErrorType.SERVER
                    )
                }
            } else {
                Log.e("SearchRepository", "Search failed: ${response.code()} ${response.message()}")
                Result.Error(
                    message = "검색에 실패했습니다: ${response.message()}",
                    errorType = ErrorType.SERVER
                )
            }
        } catch (e: Exception) {
            Log.e("SearchRepository", "Search exception", e)
            Result.Error(
                message = "검색 중 오류가 발생했습니다: ${e.message}",
                errorType = ErrorType.NETWORK
            )
        }
    }

    override suspend fun getTagSuggestions(keyword: String): Result<List<String>> {
        return try {
            Log.d("SearchRepository", "Getting tag suggestions for: $keyword")
            val response = searchApi.getTagSuggestions(keyword)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.data != null) {
                    Log.d("SearchRepository", "Tag suggestions successful: ${body.data.count} tags found")
                    Result.Success(body.data.tags)
                } else {
                    Log.d("SearchRepository", "No tag suggestions found")
                    Result.Success(emptyList())
                }
            } else {
                Log.e("SearchRepository", "Tag suggestions failed: ${response.code()} ${response.message()}")
                Result.Error(
                    message = "태그 자동완성 조회에 실패했습니다: ${response.message()}",
                    errorType = ErrorType.SERVER
                )
            }
        } catch (e: Exception) {
            Log.e("SearchRepository", "Tag suggestions exception", e)
            Result.Error(
                message = "태그 자동완성 중 오류가 발생했습니다: ${e.message}",
                errorType = ErrorType.NETWORK
            )
        }
    }

    override suspend fun searchFilesByTag(tag: String): Result<List<TagSearchFile>> {
        return try {
            Log.d("SearchRepository", "Searching files by tag: $tag")
            val response = searchApi.searchFilesByTag(tag)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.data != null) {
                    Log.d("SearchRepository", "Tag search successful: ${body.data.totalCount} files found")
                    Result.Success(body.data.results.map { it.toDomain() })
                } else {
                    Log.d("SearchRepository", "No files found for tag: $tag")
                    Result.Success(emptyList())
                }
            } else {
                Log.e("SearchRepository", "Tag search failed: ${response.code()} ${response.message()}")
                Result.Error(
                    message = "태그 검색에 실패했습니다: ${response.message()}",
                    errorType = ErrorType.SERVER
                )
            }
        } catch (e: Exception) {
            Log.e("SearchRepository", "Tag search exception", e)
            Result.Error(
                message = "태그 검색 중 오류가 발생했습니다: ${e.message}",
                errorType = ErrorType.NETWORK
            )
        }
    }

    override suspend fun searchAll(keyword: String): Result<List<TagSearchFile>> {
        return try {
            Log.d("SearchRepository", "Searching all with keyword: $keyword")
            val response = searchApi.searchAll(keyword)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.data != null) {
                    Log.d("SearchRepository", "All search successful: ${body.data.totalCount} files found")
                    Result.Success(body.data.results.map { it.toDomain() })
                } else {
                    Log.d("SearchRepository", "No files found for keyword: $keyword")
                    Result.Success(emptyList())
                }
            } else {
                Log.e("SearchRepository", "All search failed: ${response.code()} ${response.message()}")
                Result.Error(
                    message = "통합 검색에 실패했습니다: ${response.message()}",
                    errorType = ErrorType.SERVER
                )
            }
        } catch (e: Exception) {
            Log.e("SearchRepository", "All search exception", e)
            Result.Error(
                message = "통합 검색 중 오류가 발생했습니다: ${e.message}",
                errorType = ErrorType.NETWORK
            )
        }
    }
}
