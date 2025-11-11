package com.jinjinjara.pola.data.repository

import android.util.Log
import com.jinjinjara.pola.data.mapper.toDomain
import com.jinjinjara.pola.data.remote.api.TimelineApi
import com.jinjinjara.pola.data.remote.dto.request.TimelineRequest
import com.jinjinjara.pola.domain.model.TimelinePage
import com.jinjinjara.pola.domain.repository.TimelineRepository
import com.jinjinjara.pola.util.ErrorType
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class TimelineRepositoryImpl @Inject constructor(
    private val timelineApi: TimelineApi
) : TimelineRepository {

    override suspend fun getTimeline(
        page: Int,
        size: Int,
        sortBy: String,
        direction: String,
        filterType: String?,
        filterId: Long?
    ): Result<TimelinePage> {
        return try {
            Log.d(TAG, "getTimeline() 시작 - page: $page, size: $size, filterType: $filterType, filterId: $filterId")

            val request = TimelineRequest(
                page = page,
                size = size,
                sortBy = sortBy,
                direction = direction,
                filterType = filterType,
                filterId = filterId
            )

            val response = timelineApi.getTimelineList(request)
            Log.d(TAG, "API 응답 - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "API 성공 - 파일 개수: ${body.data.content.size}, 페이지: ${body.data.page}/${body.data.totalPages}")

                val timelinePage = body.data.toDomain()
                Log.d(TAG, "변환 완료 - 총 ${timelinePage.files.size}개의 타임라인 데이터")

                Result.Success(timelinePage)
            } else {
                Log.e(TAG, "API 실패 - code: ${response.code()}, message: ${response.message()}")
                Result.Error(
                    message = "타임라인을 불러올 수 없습니다",
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
        private const val TAG = "TimelineRepository"
    }
}
