package com.jinjinjara.pola.widget.data

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 위젯 상태 관리 - 파일 기반 저장소
 */
@Singleton
class WidgetStateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "WidgetStateManager"
        private const val STATE_FILE_NAME = "widget_state.json"
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(WidgetState::class.java)

    private val stateFile: File
        get() = File(context.filesDir, STATE_FILE_NAME)

    /**
     * 상태 저장
     */
    suspend fun saveState(state: WidgetState) {
        withContext(Dispatchers.IO) {
            try {
                val json = adapter.toJson(state)
                stateFile.writeText(json)
                Log.d(TAG, "[Widget] State saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "[Widget] Failed to save state", e)
            }
        }
    }

    /**
     * 상태 로드
     */
    suspend fun loadState(): WidgetState {
        return withContext(Dispatchers.IO) {
            try {
                if (stateFile.exists()) {
                    val json = stateFile.readText()
                    adapter.fromJson(json) ?: WidgetState()
                } else {
                    Log.d(TAG, "[Widget] State file not found, returning default state")
                    WidgetState()
                }
            } catch (e: Exception) {
                Log.e(TAG, "[Widget] Failed to load state", e)
                WidgetState()
            }
        }
    }

    /**
     * 상태 삭제
     */
    suspend fun clearState() {
        withContext(Dispatchers.IO) {
            try {
                if (stateFile.exists()) {
                    stateFile.delete()
                    Log.d(TAG, "[Widget] State cleared successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "[Widget] Failed to clear state", e)
            }
        }
    }
}