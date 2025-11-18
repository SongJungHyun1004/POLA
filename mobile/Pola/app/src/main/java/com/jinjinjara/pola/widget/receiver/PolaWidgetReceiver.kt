package com.jinjinjara.pola.widget.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.jinjinjara.pola.widget.ui.PolaRemindWidget
import com.jinjinjara.pola.widget.worker.WidgetUpdateWorker
import java.util.concurrent.TimeUnit

/**
 * POLA 위젯 Receiver
 * 위젯 생명주기 이벤트 처리 및 주기적 업데이트 예약
 */
class PolaWidgetReceiver : GlanceAppWidgetReceiver() {

    companion object {
        private const val TAG = "PolaWidgetReceiver"
        private const val UPDATE_INTERVAL_HOURS = 1L
        private const val AUTO_FLIP_INTERVAL_MS = 5000L // 5초
    }

    override val glanceAppWidget: GlanceAppWidget
        get() = PolaRemindWidget()

    /**
     * 위젯이 활성화될 때 호출
     */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "[Widget] Widget enabled")

        // 즉시 데이터 로드
        triggerImmediateUpdate(context)

        // 주기적 업데이트 Worker 예약
        schedulePeriodicUpdate(context)

        // 자동 넘김 알람 시작
        scheduleAutoFlip(context)
    }

    /**
     * 위젯이 비활성화될 때 호출
     */
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "[Widget] Widget disabled")

        // Worker 취소
        WorkManager.getInstance(context).cancelUniqueWork(WidgetUpdateWorker.WORK_NAME)

        // 자동 넘김 알람 취소
        cancelAutoFlip(context)
    }

    /**
     * 위젯 업데이트가 요청될 때 호출
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "[Widget] Widget update requested for ${appWidgetIds.size} widgets")

        // 즉시 데이터 로드
        triggerImmediateUpdate(context)
    }

    /**
     * 즉시 데이터 업데이트 트리거
     */
    private fun triggerImmediateUpdate(context: Context) {
        Log.d(TAG, "[Widget] Triggering immediate widget update")

        val immediateWorkRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        WorkManager.getInstance(context).enqueue(immediateWorkRequest)
    }

    /**
     * 주기적 업데이트 Worker 예약
     */
    private fun schedulePeriodicUpdate(context: Context) {
        Log.d(TAG, "[Widget] Scheduling periodic widget update (interval: $UPDATE_INTERVAL_HOURS hours)")

        val updateWorkRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            UPDATE_INTERVAL_HOURS,
            TimeUnit.HOURS
        )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WidgetUpdateWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            updateWorkRequest
        )
    }

    /**
     * 자동 넘김 알람 시작
     */
    private fun scheduleAutoFlip(context: Context) {
        Log.d(TAG, "[Widget] Scheduling auto-flip alarm (interval: ${AUTO_FLIP_INTERVAL_MS}ms)")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WidgetAutoFlipReceiver::class.java).apply {
            action = WidgetAutoFlipReceiver.ACTION_AUTO_FLIP
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = SystemClock.elapsedRealtime() + AUTO_FLIP_INTERVAL_MS

        // Android 12+ (API 31+)는 정확한 알람 사용
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                // canScheduleExactAlarms()로 권한 체크
                if (alarmManager.canScheduleExactAlarms()) {
                    Log.d(TAG, "[Widget] Using setExactAndAllowWhileIdle (exact alarm with permission)")
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    Log.d(TAG, "[Widget] Using setAndAllowWhileIdle (inexact alarm, no permission)")
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "[Widget] SecurityException when scheduling alarm, falling back to inexact", e)
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            // Android 11 이하는 setRepeating 사용 가능
            Log.d(TAG, "[Widget] Using setRepeating (pre-Android 12)")
            alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                AUTO_FLIP_INTERVAL_MS,
                pendingIntent
            )
        }

        Log.d(TAG, "[Widget] Auto-flip alarm scheduled successfully")
    }

    /**
     * 자동 넘김 알람 취소
     */
    private fun cancelAutoFlip(context: Context) {
        Log.d(TAG, "[Widget] Canceling auto-flip alarm")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WidgetAutoFlipReceiver::class.java).apply {
            action = WidgetAutoFlipReceiver.ACTION_AUTO_FLIP
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        Log.d(TAG, "[Widget] Auto-flip alarm canceled successfully")
    }
}
