package com.jinjinjara.pola.widget.callback

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jinjinjara.pola.domain.usecase.favorite.ToggleFavoriteUseCase
import com.jinjinjara.pola.util.Result
import com.jinjinjara.pola.widget.data.WidgetStateDefinition
import com.jinjinjara.pola.widget.ui.PolaRemindWidget
import com.jinjinjara.pola.widget.worker.WidgetUpdateWorker
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * 위젯 액션 콜백을 위한 Hilt EntryPoint
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetCallbackEntryPoint {
    fun toggleFavoriteUseCase(): ToggleFavoriteUseCase
}

/**
 * 이전 버튼 클릭 콜백
 */
class NavigatePreviousCallback : ActionCallback {
    companion object {
        private const val TAG = "NavigatePreviousCallback"
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d(TAG, "[Widget] NavigatePreviousCallback.onAction called")
        try {
            Log.d(TAG, "[Widget] Navigate to previous")

            // Glance 상태 업데이트
            updateAppWidgetState(
                context = context,
                definition = WidgetStateDefinition,
                glanceId = glanceId
            ) { currentState ->
                val newIndex = if (currentState.currentIndex <= 0) {
                    currentState.remindItems.size - 1
                } else {
                    currentState.currentIndex - 1
                }

                Log.d(TAG, "[Widget] Index changed: ${currentState.currentIndex} → $newIndex")

                currentState.copy(currentIndex = newIndex)
            }

            Log.d(TAG, "[Widget] Calling PolaRemindWidget().update()")

            // 위젯 업데이트
            PolaRemindWidget().update(context, glanceId)

            Log.d(TAG, "[Widget] update() call completed")
            Log.d(TAG, "[Widget] Successfully moved to previous")
        } catch (e: Exception) {
            Log.e(TAG, "[Widget] Error navigating to previous", e)
        }
    }
}

/**
 * 다음 버튼 클릭 콜백
 */
class NavigateNextCallback : ActionCallback {
    companion object {
        private const val TAG = "NavigateNextCallback"
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d(TAG, "[Widget] NavigateNextCallback.onAction called")
        try {
            Log.d(TAG, "[Widget] Navigate to next")

            // Glance 상태 업데이트
            updateAppWidgetState(
                context = context,
                definition = WidgetStateDefinition,
                glanceId = glanceId
            ) { currentState ->
                val newIndex = if (currentState.currentIndex >= currentState.remindItems.size - 1) {
                    0
                } else {
                    currentState.currentIndex + 1
                }

                Log.d(TAG, "[Widget] Index changed: ${currentState.currentIndex} → $newIndex")

                currentState.copy(currentIndex = newIndex)
            }

            Log.d(TAG, "[Widget] Calling PolaRemindWidget().update()")

            // 위젯 업데이트
            PolaRemindWidget().update(context, glanceId)

            Log.d(TAG, "[Widget] update() call completed")
            Log.d(TAG, "[Widget] Successfully moved to next")
        } catch (e: Exception) {
            Log.e(TAG, "[Widget] Error navigating to next", e)
        }
    }
}

/**
 * 새로고침 콜백
 */
class RefreshCallback : ActionCallback {
    companion object {
        private const val TAG = "RefreshCallback"
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d(TAG, "[Widget] RefreshCallback.onAction called")
        try {
            Log.d(TAG, "[Widget] Refreshing widget data")

            // 로딩 상태로 설정
            updateAppWidgetState(
                context = context,
                definition = WidgetStateDefinition,
                glanceId = glanceId
            ) { currentState ->
                currentState.copy(isLoading = true, errorMessage = null)
            }

            // 위젯 업데이트 (로딩 표시)
            PolaRemindWidget().update(context, glanceId)

            // WidgetUpdateWorker 실행
            val updateRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
            WorkManager.getInstance(context).enqueue(updateRequest)

            Log.d(TAG, "[Widget] Refresh work enqueued")
        } catch (e: Exception) {
            Log.e(TAG, "[Widget] Error during refresh", e)
        }
    }
}

/**
 * 즐겨찾기 토글 콜백
 */
class ToggleFavoriteCallback : ActionCallback {
    companion object {
        private const val TAG = "ToggleFavoriteCallback"
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d(TAG, "[Widget] ToggleFavoriteCallback.onAction called")
        try {
            Log.d(TAG, "[Widget] Toggle favorite")

            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetCallbackEntryPoint::class.java
            )
            val toggleFavoriteUseCase = entryPoint.toggleFavoriteUseCase()

            // updateAppWidgetState 한 번만 호출하고, 블록 내부에서 모든 작업 처리
            updateAppWidgetState(
                context = context,
                definition = WidgetStateDefinition,
                glanceId = glanceId
            ) { currentState ->
                val currentItem = currentState.remindItems.getOrNull(currentState.currentIndex)

                if (currentItem == null) {
                    Log.e(TAG, "[Widget] No current item found")
                    return@updateAppWidgetState currentState
                }

                Log.d(TAG, "[Widget] Current item: fileId=${currentItem.fileId}, isFavorite=${currentItem.isFavorite}")

                // 즐겨찾기 토글 API 호출 (suspend 블록 내부에서 가능)
                val newFavoriteState = !currentItem.isFavorite
                when (val result = toggleFavoriteUseCase(currentItem.fileId, newFavoriteState)) {
                    is Result.Success -> {
                        Log.d(TAG, "[Widget] Favorite toggled successfully: ${result.data}")

                        // 상태 업데이트하여 반환
                        val updatedItems = currentState.remindItems.toMutableList()
                        updatedItems[currentState.currentIndex] =
                            updatedItems[currentState.currentIndex].copy(isFavorite = result.data)

                        currentState.copy(remindItems = updatedItems)
                    }
                    is Result.Error -> {
                        Log.e(TAG, "[Widget] Failed to toggle favorite: ${result.message}")
                        currentState  // 실패 시 기존 상태 유지
                    }
                    is Result.Loading -> {
                        Log.d(TAG, "[Widget] Toggle favorite loading")
                        currentState  // 로딩 중일 경우 기존 상태 유지
                    }
                }
            }

            // 위젯 UI 업데이트
            PolaRemindWidget().update(context, glanceId)
            Log.d(TAG, "[Widget] Successfully toggled favorite and updated widget")
        } catch (e: Exception) {
            Log.e(TAG, "[Widget] Error toggling favorite", e)
        }
    }
}
