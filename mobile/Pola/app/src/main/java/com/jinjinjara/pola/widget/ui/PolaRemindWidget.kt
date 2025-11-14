package com.jinjinjara.pola.widget.ui

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.currentState
import com.jinjinjara.pola.widget.data.WidgetState
import com.jinjinjara.pola.widget.data.WidgetStateDefinition
import com.jinjinjara.pola.widget.data.WidgetStateManager
import com.jinjinjara.pola.widget.util.BitmapLoader
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * POLA 리마인드 위젯
 */
class PolaRemindWidget : GlanceAppWidget() {

    companion object {
        private const val TAG = "PolaRemindWidget"
    }

    override val stateDefinition = WidgetStateDefinition

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PolaWidgetEntryPoint {
        fun widgetStateManager(): WidgetStateManager
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d(TAG, "[Widget] ========== provideGlance called for glanceId: $id ==========")

        provideContent {
            // Glance가 관리하는 상태 가져오기
            val widgetState = currentState<WidgetState>()
            Log.d(TAG, "[Widget] Widget state loaded from Glance: ${widgetState.remindItems.size} items, currentIndex: ${widgetState.currentIndex}")

            val currentBitmap = try {
                val item = widgetState.remindItems.getOrNull(widgetState.currentIndex)
                if (item != null) {
                    Log.d(TAG, "[Widget] Current item: fileId=${item.fileId}, localImagePath=${item.localImagePath}")
                    val bitmap = BitmapLoader.loadBitmapFromPath(item.localImagePath)
                    if (bitmap == null) {
                        Log.w(TAG, "[Widget] Bitmap is null for fileId: ${item.fileId}, path: ${item.localImagePath}")
                    } else {
                        Log.d(TAG, "[Widget] Bitmap loaded for widget: ${bitmap.width}x${bitmap.height}")
                    }
                    bitmap
                } else {
                    Log.w(TAG, "[Widget] Current item is null at index ${widgetState.currentIndex}")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "[Widget] Bitmap load failed", e)
                null
            }

            Log.d(TAG, "[Widget] Rendering widget with bitmap: ${if (currentBitmap != null) "available" else "null"}")

            GlanceTheme {
                WidgetContentLayout(
                    widgetState = widgetState,
                    currentBitmap = currentBitmap,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.background)
                        .padding(16.dp)
                )
            }
        }

        Log.d(TAG, "[Widget] ========== provideGlance completed ==========")
    }

    /**
     * 모든 위젯 인스턴스를 강제로 업데이트
     */
    suspend fun updateAll(context: Context) {
        Log.d(TAG, "[Widget] updateAll() called")
        try {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(PolaRemindWidget::class.java)

            Log.d(TAG, "[Widget] Found ${glanceIds.size} widget instances")

            glanceIds.forEach { glanceId ->
                Log.d(TAG, "[Widget] Updating glanceId: $glanceId")
                update(context, glanceId)
            }

            Log.d(TAG, "[Widget] updateAll() completed")
        } catch (e: Exception) {
            Log.e(TAG, "[Widget] Error in updateAll()", e)
        }
    }
}
