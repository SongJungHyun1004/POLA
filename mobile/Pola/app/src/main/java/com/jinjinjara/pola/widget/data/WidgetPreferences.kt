package com.jinjinjara.pola.widget.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 위젯 설정 관리
 * DataStore Preferences를 사용하여 위젯 설정 저장
 */
private val Context.widgetPreferencesDataStore by preferencesDataStore(
    name = "widget_preferences"
)

@Singleton
class WidgetPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.widgetPreferencesDataStore

    companion object {
        private val CURRENT_INDEX = intPreferencesKey("current_index")
        private val LAST_UPDATE_TIME = longPreferencesKey("last_update_time")
        private val CACHED_REMIND_COUNT = intPreferencesKey("cached_remind_count")
        private val UPDATE_INTERVAL_HOURS = intPreferencesKey("update_interval_hours")
    }

    /**
     * 현재 표시 중인 인덱스 가져오기
     */
    val currentIndex: Flow<Int> = dataStore.data.map { preferences ->
        preferences[CURRENT_INDEX] ?: 0
    }

    /**
     * 마지막 업데이트 시간 가져오기
     */
    val lastUpdateTime: Flow<Long> = dataStore.data.map { preferences ->
        preferences[LAST_UPDATE_TIME] ?: 0L
    }

    /**
     * 캐시된 리마인드 개수 가져오기
     */
    val cachedRemindCount: Flow<Int> = dataStore.data.map { preferences ->
        preferences[CACHED_REMIND_COUNT] ?: 0
    }

    /**
     * 업데이트 주기 가져오기 (기본 1시간)
     */
    val updateIntervalHours: Flow<Int> = dataStore.data.map { preferences ->
        preferences[UPDATE_INTERVAL_HOURS] ?: 1
    }

    /**
     * 현재 인덱스 저장
     */
    suspend fun setCurrentIndex(index: Int) {
        dataStore.edit { preferences ->
            preferences[CURRENT_INDEX] = index
        }
    }

    /**
     * 마지막 업데이트 시간 저장
     */
    suspend fun setLastUpdateTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_UPDATE_TIME] = time
        }
    }

    /**
     * 캐시된 리마인드 개수 저장
     */
    suspend fun setCachedRemindCount(count: Int) {
        dataStore.edit { preferences ->
            preferences[CACHED_REMIND_COUNT] = count
        }
    }

    /**
     * 업데이트 주기 저장
     */
    suspend fun setUpdateIntervalHours(hours: Int) {
        dataStore.edit { preferences ->
            preferences[UPDATE_INTERVAL_HOURS] = hours
        }
    }

    /**
     * 모든 설정 초기화
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
