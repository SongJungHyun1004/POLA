package com.jinjinjara.pola.widget.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.jinjinjara.pola.widget.data.WidgetStateDefinition
import com.jinjinjara.pola.widget.data.WidgetStateManager
import com.jinjinjara.pola.widget.ui.PolaRemindWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@HiltWorker
class ImageDownloadWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val stateManager: WidgetStateManager
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ImageDownloadWorker"
        const val KEY_IMAGE_URL = "image_url"
        const val KEY_FILE_ID = "file_id"
    }

    override suspend fun doWork(): Result {
        return try {
            val url = inputData.getString(KEY_IMAGE_URL) ?: return Result.failure()
            val fileId = inputData.getLong(KEY_FILE_ID, -1)
            if (fileId == -1L) return Result.failure()

            val path = downloadImage(url, fileId)

            if (path != null) {
                // 다운로드 성공 - 모든 위젯 인스턴스의 상태 업데이트
                val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(PolaRemindWidget::class.java)
                glanceIds.forEach { glanceId ->
                    updateAppWidgetState(
                        context = context,
                        definition = WidgetStateDefinition,
                        glanceId = glanceId
                    ) { state ->
                        val updated = state.remindItems.map {
                            if (it.fileId == fileId) it.copy(localImagePath = path)
                            else it
                        }
                        state.copy(remindItems = updated)
                    }
                    PolaRemindWidget().update(context, glanceId)
                }

                Result.success()
            } else {
                // 다운로드 실패 - 재시도 로직
                val attemptCount = runAttemptCount
                val shouldRetry = attemptCount < 3

                if (shouldRetry) {
                    Log.d(TAG, "[Widget] Retrying image download (attempt $attemptCount/3)")
                    Result.retry()
                } else {
                    Log.d(TAG, "[Widget] Giving up image download after $attemptCount attempts")
                    // 실패해도 기존 캐시된 이미지가 있으면 계속 사용 가능
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "[Widget] Download error", e)

            // 재시도 횟수 확인
            val attemptCount = runAttemptCount
            val shouldRetry = attemptCount < 3

            // 네트워크 관련 예외인지 확인
            val isNetworkError = e is java.net.UnknownHostException ||
                    e is java.net.SocketTimeoutException ||
                    e.cause is java.net.UnknownHostException

            if (shouldRetry && isNetworkError) {
                Log.d(TAG, "[Widget] Retrying after download exception (attempt $attemptCount/3)")
                Result.retry()
            } else {
                Log.d(TAG, "[Widget] Failing after download exception (attempt $attemptCount)")
                Result.failure()
            }
        }
    }

    private suspend fun downloadImage(url: String, fileId: Long): String? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "[Widget] Downloading image for fileId: $fileId from $url")

                val conn = URL(url).openConnection()
                val input = conn.getInputStream()
                val bitmap = BitmapFactory.decodeStream(input)
                input.close()

                if (bitmap == null) {
                    Log.e(TAG, "[Widget] Failed to decode bitmap for fileId: $fileId, URL: $url")
                    return@withContext null
                }

                Log.d(TAG, "[Widget] Bitmap decoded successfully: ${bitmap.width}x${bitmap.height} for fileId: $fileId")

                val dir = File(context.cacheDir, "widget_images")
                if (!dir.exists()) {
                    dir.mkdirs()
                    Log.d(TAG, "[Widget] Created widget_images directory: ${dir.absolutePath}")
                }

                val file = File(dir, "remind_$fileId.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }

                if (file.exists() && file.length() > 0) {
                    Log.d(TAG, "[Widget] Image saved successfully: ${file.absolutePath}, size: ${file.length()} bytes")
                    file.absolutePath
                } else {
                    Log.e(TAG, "[Widget] File is empty or not created: ${file.absolutePath}")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "[Widget] Image download failed for fileId: $fileId", e)
                null
            }
        }
    }
}
