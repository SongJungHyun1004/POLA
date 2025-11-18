package com.jinjinjara.pola.widget.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.jinjinjara.pola.widget.data.WidgetStateDefinition
import com.jinjinjara.pola.widget.ui.PolaRemindWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 위젯 자동 넘김 Receiver
 * AlarmManager로부터 3초마다 트리거되어 다음 이미지로 자동 전환
 */
class WidgetAutoFlipReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WidgetAutoFlipReceiver"
        const val ACTION_AUTO_FLIP = "com.jinjinjara.pola.widget.ACTION_AUTO_FLIP"
        private const val AUTO_FLIP_INTERVAL_MS = 3000L // 3초
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_AUTO_FLIP) {
            return
        }

        Log.d(TAG, "[Widget] Auto-flip triggered")

        // PendingResult for async work
        val pendingResult = goAsync()

        scope.launch {
            try {
                // Get all active widget IDs
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, PolaWidgetReceiver::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

                if (appWidgetIds.isEmpty()) {
                    Log.d(TAG, "[Widget] No active widgets found, not rescheduling")
                    pendingResult.finish()
                    return@launch
                }

                Log.d(TAG, "[Widget] Found ${appWidgetIds.size} active widgets")

                // Get Glance widget manager
                val glanceManager = GlanceAppWidgetManager(context)

                // Update each widget
                appWidgetIds.forEach { appWidgetId ->
                    try {
                        // Try to get glanceId with error handling
                        val glanceId = try {
                            glanceManager.getGlanceIdBy(appWidgetId)
                        } catch (e: Exception) {
                            Log.e(TAG, "[Widget] Failed to get glanceId for widget $appWidgetId", e)
                            return@forEach // Skip this widget
                        }

                        // Update widget state - increment index
                        updateAppWidgetState(
                            context = context,
                            definition = WidgetStateDefinition,
                            glanceId = glanceId
                        ) { currentState ->
                            // Skip if no items
                            if (currentState.remindItems.isEmpty()) {
                                Log.d(TAG, "[Widget] No items to flip")
                                return@updateAppWidgetState currentState
                            }

                            // Calculate next index (wrap around)
                            val newIndex = if (currentState.currentIndex >= currentState.remindItems.size - 1) {
                                0
                            } else {
                                currentState.currentIndex + 1
                            }

                            Log.d(TAG, "[Widget] Auto-flip: ${currentState.currentIndex} → $newIndex")

                            currentState.copy(currentIndex = newIndex)
                        }

                        // Update widget UI
                        PolaRemindWidget().update(context, glanceId)
                        Log.d(TAG, "[Widget] Successfully flipped widget $appWidgetId")
                    } catch (e: Exception) {
                        Log.e(TAG, "[Widget] Error flipping widget $appWidgetId", e)
                    }
                }

                Log.d(TAG, "[Widget] Auto-flip completed for all widgets")

                // Reschedule next alarm (Android 12+ uses one-shot alarms)
                rescheduleNextFlip(context)

            } catch (e: Exception) {
                Log.e(TAG, "[Widget] Error during auto-flip", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * 다음 자동 넘김 알람 예약
     * Android 12+ 에서는 setExactAndAllowWhileIdle이 일회성이므로 매번 재예약 필요
     */
    private fun rescheduleNextFlip(context: Context) {
        // Android 12+ 에서만 재예약 필요 (setRepeating이 아니므로)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d(TAG, "[Widget] Rescheduling next auto-flip alarm")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, WidgetAutoFlipReceiver::class.java).apply {
                action = ACTION_AUTO_FLIP
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerTime = SystemClock.elapsedRealtime() + AUTO_FLIP_INTERVAL_MS

            try {
                // canScheduleExactAlarms()로 권한 체크
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.d(TAG, "[Widget] Next alarm scheduled with setExactAndAllowWhileIdle")
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.d(TAG, "[Widget] Next alarm scheduled with setAndAllowWhileIdle (no permission)")
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "[Widget] SecurityException when rescheduling, falling back to inexact", e)
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            // Android 11 이하는 setRepeating으로 이미 반복 예약되어 있으므로 재예약 불필요
            Log.d(TAG, "[Widget] Skipping reschedule (pre-Android 12, using setRepeating)")
        }
    }
}
