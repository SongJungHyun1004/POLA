package com.jinjinjara.pola.data.repository

import android.util.Log
import com.jinjinjara.pola.data.remote.api.FavoriteApi
import com.jinjinjara.pola.data.remote.dto.request.FavoriteListRequest
import com.jinjinjara.pola.domain.model.FavoriteData
import com.jinjinjara.pola.domain.repository.FavoriteRepository
import com.jinjinjara.pola.util.ErrorType
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteApi: FavoriteApi
) : FavoriteRepository {

    override suspend fun getFavoriteList(): Result<List<FavoriteData>> {
        return try {
            Log.d(TAG, "getFavoriteList() 시작")

            val request = FavoriteListRequest(
                page = 0,
                size = 50,
                sortBy = "favoriteSort",
                direction = "DESC",
                filterType = "favorite",
                filterId = null
            )

            val response = favoriteApi.getFavoriteList(request)
            Log.d(TAG, "API 응답 - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "API 성공 - 파일 개수: ${body.data.content.size}")

                body.data.content.forEachIndexed { index, fileData ->
                    Log.d(TAG, "[$index] id: ${fileData.id}, src: ${fileData.src}, favorite: ${fileData.favorite}")
                }

                val favorites = body.data.content.map { fileData ->
                    FavoriteData(
                        fileId = fileData.id,
                        imageUrl = fileData.src,
                        type = fileData.type,
                        context = fileData.context,
                        isFavorite = fileData.favorite,
                        tags = fileData.tags
                    )
                }

                Log.d(TAG, "변환 완료 - 총 ${favorites.size}개의 즐겨찾기 데이터")
                Result.Success(favorites)
            } else {
                Log.e(TAG, "API 실패 - code: ${response.code()}, message: ${response.message()}")
                Result.Error(
                    message = "즐겨찾기 목록을 불러올 수 없습니다",
                    errorType = ErrorType.SERVER
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "예외 발생", e)
            Result.Error(
                exception = e,
                message = e.message ?: "알 수 없는 오류가 발생했습니다",
                errorType = ErrorType.NETWORK
            )
        }
    }

    override suspend fun toggleFavorite(fileId: Long, isFavorite: Boolean): Result<Boolean> {
        return try {
            Log.d(TAG, "toggleFavorite() - fileId: $fileId, isFavorite: $isFavorite")

            val response = if (isFavorite) {
                favoriteApi.addFavorite(fileId = fileId, sortValue = 1)
            } else {
                favoriteApi.removeFavorite(fileId = fileId)
            }

            Log.d(TAG, "API 응답 - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "즐겨찾기 ${if (isFavorite) "추가" else "제거"} 성공 - favorite: ${body.data.favorite}")
                Result.Success(body.data.favorite)
            } else {
                Log.e(TAG, "API 실패 - code: ${response.code()}, message: ${response.message()}")
                Result.Error(
                    message = "즐겨찾기 상태를 변경할 수 없습니다",
                    errorType = ErrorType.SERVER
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "예외 발생", e)
            Result.Error(
                exception = e,
                message = e.message ?: "알 수 없는 오류가 발생했습니다",
                errorType = ErrorType.NETWORK
            )
        }
    }

    companion object {
        private const val TAG = "FavoriteRepository"
    }
}
