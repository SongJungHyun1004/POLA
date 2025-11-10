package com.jinjinjara.pola.data.repository

import android.util.Log
import com.jinjinjara.pola.data.remote.api.RemindApi
import com.jinjinjara.pola.domain.model.RemindData
import com.jinjinjara.pola.domain.repository.RemindRepository
import com.jinjinjara.pola.util.ErrorType
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class RemindRepositoryImpl @Inject constructor(
    private val remindApi: RemindApi
) : RemindRepository {

    override suspend fun getReminders(): Result<List<RemindData>> {
        return try {
            Log.d(TAG, "getReminders() 호출 시작")
            val response = remindApi.getReminders()

            Log.d(TAG, "API 응답 수신 - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "응답 body - status: ${body.status}, message: ${body.message}, data count: ${body.data.size}")

                // DTO를 Domain Model로 변환
                val reminders = body.data.mapIndexed { index, fileData ->
                    Log.d(TAG, "[$index] id: ${fileData.id}, src: ${fileData.src}, type: ${fileData.type}, favorite: ${fileData.favorite}, tags: ${fileData.tags}")
                    RemindData(
                        id = fileData.id,
                        imageUrl = fileData.src,
                        type = fileData.type,
                        context = fileData.context,
                        isFavorite = fileData.favorite,
                        tags = fileData.tags
                    )
                }

                Log.d(TAG, "변환 완료 - 총 ${reminders.size}개의 리마인드 데이터")
                Result.Success(reminders)
            } else {
                Log.e(TAG, "API 실패 - code: ${response.code()}, message: ${response.message()}")
                Result.Error(
                    message = "리마인드 데이터를 불러올 수 없습니다",
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
        private const val TAG = "RemindRepository"
    }
}
