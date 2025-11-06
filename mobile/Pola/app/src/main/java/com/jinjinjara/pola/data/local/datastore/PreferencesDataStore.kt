package com.jinjinjara.pola.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore를 사용한 환경설정 관리
@Singleton
class PreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val IS_FIRST_LAUNCH_KEY = stringPreferencesKey("is_first_launch")
        private val IS_ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("is_onboarding_completed")
    }

    // 토큰 관련

    suspend fun saveAccessToken(token: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
        }
    }

    suspend fun getAccessToken(): String? {
        return dataStore.data.first()[ACCESS_TOKEN_KEY]
    }

    fun observeAccessToken(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            !preferences[ACCESS_TOKEN_KEY].isNullOrEmpty()
        }
    }

    suspend fun saveRefreshToken(token: String) {
        dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN_KEY] = token
        }
    }

    suspend fun getRefreshToken(): String? {
        return dataStore.data.first()[REFRESH_TOKEN_KEY]
    }

    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }

    // 사용자 정보 관련

    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun getUserId(): String? {
        return dataStore.data.first()[USER_ID_KEY]
    }

    suspend fun clearUserId() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }

    // 앱 설정 관련

    suspend fun setFirstLaunch(isFirst: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH_KEY] = isFirst.toString()
        }
    }

    suspend fun isFirstLaunch(): Boolean {
        val value = dataStore.data.first()[IS_FIRST_LAUNCH_KEY]
        return value?.toBoolean() ?: true
    }

    // 온보딩 관련

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_ONBOARDING_COMPLETED_KEY] = completed
        }
    }

    suspend fun isOnboardingCompleted(): Boolean {
        return dataStore.data.first()[IS_ONBOARDING_COMPLETED_KEY] ?: false
    }

    fun observeOnboardingCompleted(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[IS_ONBOARDING_COMPLETED_KEY] ?: false
        }
    }

    // 전체 데이터 삭제

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}