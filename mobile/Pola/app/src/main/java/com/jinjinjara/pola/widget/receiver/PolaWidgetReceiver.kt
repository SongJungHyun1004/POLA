package com.jinjinjara.pola.widget.receiver

import android.content.Context
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
    }

    /**
     * 위젯이 비활성화될 때 호출
     */
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "[Widget] Widget disabled")

        // Worker 취소
        WorkManager.getInstance(context).cancelUniqueWork(WidgetUpdateWorker.WORK_NAME)
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
}
