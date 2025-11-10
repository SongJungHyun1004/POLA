package com.jinjinjara.pola.data.repository

import android.util.Log
import com.jinjinjara.pola.data.remote.api.FavoriteApi
import com.jinjinjara.pola.domain.repository.FavoriteRepository
import com.jinjinjara.pola.util.ErrorType
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteApi: FavoriteApi
) : FavoriteRepository {

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
