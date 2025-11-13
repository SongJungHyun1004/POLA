package com.jinjinjara.pola.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.jinjinjara.pola.data.local.database.PolaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 로컬 데이터베이스 관련 의존성을 제공하는 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "pola_database"
    private const val DATASTORE_NAME = "pola_preferences"

    /**
     * Room Database 제공
     */
    @Provides
    @Singleton
    fun providePolaDatabase(
        @ApplicationContext context: Context
    ): PolaDatabase {
        return Room.databaseBuilder(
            context,
            PolaDatabase::class.java,
            DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // 마이그레이션 실패 시 데이터 삭제 후 재생성
            .build()
    }

    /**
     * ChatMessageDao 제공
     */
    @Provides
    @Singleton
    fun provideChatMessageDao(database: PolaDatabase) = database.chatMessageDao()

    /**
     * DAO 제공 예시
     */
    // @Provides
    // @Singleton
    // fun provideUserDao(database: PolaDatabase): UserDao {
    //     return database.userDao()
    // }

    /**
     * DataStore 제공
     */
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = DATASTORE_NAME
    )

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }
}