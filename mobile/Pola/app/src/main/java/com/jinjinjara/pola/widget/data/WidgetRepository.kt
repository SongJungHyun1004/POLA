package com.jinjinjara.pola.widget.data

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.util.TypedValue
import com.jinjinjara.pola.di.IoDispatcher
import com.jinjinjara.pola.domain.model.RemindData
import com.jinjinjara.pola.domain.usecase.remind.GetRemindersUseCase
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 위젯 데이터 관리 Repository
 */
@Singleton
class WidgetRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getRemindersUseCase: GetRemindersUseCase,
    private val widgetPreferences: WidgetPreferences,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    companion object {
        private const val TAG = "WidgetRepository"
    }

    /**
     * 텍스트 너비 측정을 위한 Paint 객체 (재사용)
     */
    private val textMeasurePaint by lazy {
        Paint().apply {
            textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                28f,  // 위젯 태그 텍스트 크기 (WidgetPolaCard.kt와 동일)
                context.resources.displayMetrics
            )
        }
    }

    /**
     * 리마인드 데이터를 가져와서 위젯 아이템으로 변환
     */
    suspend fun fetchRemindData(): Result<List<WidgetRemindItem>> {
        return withContext(ioDispatcher) {
            try {
                Log.d(TAG, "Fetching remind data for widget")

                when (val result = getRemindersUseCase()) {
                    is Result.Success -> {
                        val widgetItems = result.data.map { remindData ->
                            remindData.toWidgetItem()
                        }
                        Log.d(TAG, "Successfully fetched ${widgetItems.size} remind items")

                        // 캐시된 개수 저장
                        widgetPreferences.setCachedRemindCount(widgetItems.size)
                        widgetPreferences.setLastUpdateTime(System.currentTimeMillis())

                        Result.Success(widgetItems)
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Failed to fetch remind data: ${result.message}")
                        Result.Error(
                            message = result.message ?: "리마인드 데이터를 불러올 수 없습니다",
                            exception = result.exception
                        )
                    }
                    is Result.Loading -> {
                        Log.d(TAG, "Loading remind data")
                        Result.Loading
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while fetching remind data", e)
                Result.Error(
                    message = "위젯 데이터를 불러오는 중 오류가 발생했습니다",
                    exception = e
                )
            }
        }
    }

    /**
     * 다음 인덱스로 이동
     */
    suspend fun moveToNext(currentIndex: Int, totalCount: Int): Int {
        return withContext(ioDispatcher) {
            val newIndex = if (currentIndex >= totalCount - 1) 0 else currentIndex + 1
            widgetPreferences.setCurrentIndex(newIndex)
            Log.d(TAG, "Moved to next: $currentIndex -> $newIndex")
            newIndex
        }
    }

    /**
     * 이전 인덱스로 이동
     */
    suspend fun moveToPrevious(currentIndex: Int, totalCount: Int): Int {
        return withContext(ioDispatcher) {
            val newIndex = if (currentIndex <= 0) totalCount - 1 else currentIndex - 1
            widgetPreferences.setCurrentIndex(newIndex)
            Log.d(TAG, "Moved to previous: $currentIndex -> $newIndex")
            newIndex
        }
    }

    /**
     * 현재 인덱스 가져오기
     */
    suspend fun getCurrentIndex(): Int {
        return withContext(ioDispatcher) {
            var index = 0
            widgetPreferences.currentIndex.collect { index = it }
            index
        }
    }

    /**
     * 업데이트가 필요한지 확인
     */
    suspend fun needsUpdate(): Boolean {
        return withContext(ioDispatcher) {
            var lastUpdateTime = 0L
            var updateInterval = 1

            widgetPreferences.lastUpdateTime.collect { lastUpdateTime = it }
            widgetPreferences.updateIntervalHours.collect { updateInterval = it }

            val currentTime = System.currentTimeMillis()
            val elapsedHours = (currentTime - lastUpdateTime) / (1000 * 60 * 60)

            val needs = elapsedHours >= updateInterval
            Log.d(TAG, "Needs update: $needs (elapsed: $elapsedHours hours, interval: $updateInterval hours)")
            needs
        }
    }

    /**
     * RemindData를 WidgetRemindItem으로 변환
     */
    private fun RemindData.toWidgetItem(): WidgetRemindItem {
        return WidgetRemindItem(
            fileId = this.id,
            imageUrl = this.imageUrl,
            isFavorite = this.isFavorite,
            localImagePath = null,  // 이미지 다운로드는 ImageDownloadWorker에서 처리
            tags = calculateVisibleTags(this.tags),  // 한 줄에 들어갈 태그만 선택
            type = this.type,
            context = this.context
        )
    }

    /**
     * 한 줄에 들어갈 수 있는 태그만 계산 (앱의 ClippedTagRowForCard와 동일한 동작)
     *
     * Paint.measureText()를 사용하여 실제 텍스트 너비를 정확하게 측정합니다.
     * 한글, 영어, 숫자 등 모든 문자 종류에 대응하며 가변폭 폰트도 정확히 처리합니다.
     *
     * @param tags 원본 태그 리스트
     * @param maxWidthDp 최대 가로 너비 (dp) - 위젯 이미지 너비에서 패딩 제외한 값
     * @return 한 줄에 들어갈 수 있는 태그 리스트
     */
    private fun calculateVisibleTags(
        tags: List<String>,
        maxWidthDp: Int = 280  // 위젯 이미지 너비(350dp) - 패딩(24dp*2) - 여유(~20dp)
    ): List<String> {
        if (tags.isEmpty()) return emptyList()

        val result = mutableListOf<String>()
        var currentWidth = 0f

        val density = context.resources.displayMetrics.density

        for (tag in tags) {
            // Paint.measureText()로 실제 텍스트 너비 측정 (픽셀 단위)
            val tagTextWidthPx = textMeasurePaint.measureText("#$tag")

            // px를 dp로 변환 (약간 작게 계산하여 여유 확보: 0.95 배율)
            val tagTextWidth = (tagTextWidthPx / density) * 0.95f
            val spacingWidth = if (result.isNotEmpty()) 8f else 0f

            val tagTotalWidth = tagTextWidth + spacingWidth

            if (currentWidth + tagTotalWidth <= maxWidthDp) {
                result.add(tag)
                currentWidth += tagTotalWidth
            } else {
                // 공간이 부족하면 이 태그부터 모두 숨김 (앱과 동일한 동작)
                break
            }
        }

        return result
    }
}
