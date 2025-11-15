package com.jinjinjara.pola.widget.worker

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.jinjinjara.pola.widget.data.WidgetRepository
import com.jinjinjara.pola.widget.data.WidgetState
import com.jinjinjara.pola.widget.data.WidgetStateDefinition
import com.jinjinjara.pola.widget.data.WidgetStateManager
import com.jinjinjara.pola.widget.ui.PolaRemindWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.jinjinjara.pola.util.Result as DomainResult

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val repo: WidgetRepository,
    private val stateManager: WidgetStateManager
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "WidgetUpdateWorker"
        const val WORK_NAME = "widget_update_work"

        private fun queueImageDownload(context: Context, url: String, fileId: Long) {
            val req = OneTimeWorkRequestBuilder<ImageDownloadWorker>()
                .setInputData(
                    workDataOf(
                        ImageDownloadWorker.KEY_IMAGE_URL to url,
                        ImageDownloadWorker.KEY_FILE_ID to fileId
                    )
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    java.util.concurrent.TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "download_$fileId",
                ExistingWorkPolicy.REPLACE,
                req
            )

            Log.d(TAG, "[Widget] Enqueued image download: $fileId")
        }
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "[Widget] Starting worker")

            val glanceIds =
                GlanceAppWidgetManager(context).getGlanceIds(PolaRemindWidget::class.java)

            if (glanceIds.isEmpty()) {
                Log.d(TAG, "[Widget] No widgets present")
                return Result.success()
            }

            when (val result = repo.fetchRemindData()) {

                is DomainResult.Success -> {
                    val items = result.data

                    // 모든 이미지 다운로드
                    Log.d(TAG, "[Widget] Queuing ${items.size} images for download")
                    items.forEach { item ->
                        queueImageDownload(context, item.imageUrl, item.fileId)
                    }

                    // 각 위젯 인스턴스의 Glance 상태 업데이트
                    glanceIds.forEach { glanceId ->
                        updateAppWidgetState(
                            context = context,
                            definition = WidgetStateDefinition,
                            glanceId = glanceId
                        ) { _ ->
                            WidgetState(
                                currentIndex = 0,
                                remindItems = items,
                                lastUpdated = System.currentTimeMillis(),
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                        PolaRemindWidget().update(context, glanceId)
                    }

                    Result.success()
                }

                is DomainResult.Error -> {
                    Log.e(TAG, "[Widget] Failed to fetch remind data: ${result.message}")

                    // 재시도 횟수 확인 (최대 3회)
                    val attemptCount = runAttemptCount
                    val shouldRetry = attemptCount < 3

                    // 네트워크 오류인지 확인
                    val isNetworkError = result.exception?.let { ex ->
                        ex is java.net.UnknownHostException ||
                        ex is java.net.SocketTimeoutException ||
                        ex.cause is java.net.UnknownHostException
                    } ?: false

                    val errorMessage = when {
                        isNetworkError -> "네트워크 연결을 확인해주세요"
                        else -> result.message ?: "데이터를 불러올 수 없습니다"
                    }

                    // 각 위젯 인스턴스의 에러 상태 업데이트
                    glanceIds.forEach { glanceId ->
                        updateAppWidgetState(
                            context = context,
                            definition = WidgetStateDefinition,
                            glanceId = glanceId
                        ) { currentState ->
                            // 캐시된 데이터가 있으면 유지하고 에러 메시지만 표시
                            if (currentState.remindItems.isNotEmpty()) {
                                Log.d(TAG, "[Widget] Keeping cached data (${currentState.remindItems.size} items)")
                                currentState.copy(
                                    isLoading = false,
                                    errorMessage = errorMessage
                                )
                            } else {
                                // 캐시가 없으면 에러 상태
                                currentState.copy(
                                    isLoading = false,
                                    errorMessage = errorMessage
                                )
                            }
                        }
                        PolaRemindWidget().update(context, glanceId)
                    }

                    // 재시도 또는 실패 반환
                    if (shouldRetry && isNetworkError) {
                        Log.d(TAG, "[Widget] Retrying (attempt $attemptCount/3)")
                        Result.retry()
                    } else {
                        Log.d(TAG, "[Widget] Giving up after $attemptCount attempts")
                        Result.failure()
                    }
                }

                is DomainResult.Loading -> Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "[Widget] Worker exception", e)

            // 재시도 횟수 확인
            val attemptCount = runAttemptCount
            val shouldRetry = attemptCount < 3

            // 네트워크 관련 예외인지 확인
            val isNetworkError = e is java.net.UnknownHostException ||
                    e is java.net.SocketTimeoutException ||
                    e.cause is java.net.UnknownHostException

            if (shouldRetry && isNetworkError) {
                Log.d(TAG, "[Widget] Retrying after exception (attempt $attemptCount/3)")
                Result.retry()
            } else {
                Log.d(TAG, "[Widget] Failing after exception (attempt $attemptCount)")
                Result.failure()
            }
        }
    }
}
