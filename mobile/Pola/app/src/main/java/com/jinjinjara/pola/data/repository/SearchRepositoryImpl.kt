package com.jinjinjara.pola.data.repository

import android.util.Log
import com.jinjinjara.pola.data.mapper.toDomain
import com.jinjinjara.pola.data.remote.api.RagSearchApi
import com.jinjinjara.pola.data.remote.dto.request.RagSearchRequest
import com.jinjinjara.pola.domain.model.RagSearchResult
import com.jinjinjara.pola.domain.repository.SearchRepository
import com.jinjinjara.pola.util.ErrorType
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val ragSearchApi: RagSearchApi
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
}
