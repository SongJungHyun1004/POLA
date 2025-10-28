package com.jinjinjara.pola.data.local.database

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase

/**
 * 임시 더미 Entity (빌드 에러 방지용)
 */
@Entity(tableName = "dummy")
internal data class DummyEntity(
    @PrimaryKey val id: Int = 0
)

/**
 * Pola 앱의 Room Database
 * TODO: 실제 Entity 구현 후 entities 배열에 추가
 */
@Database(
    entities = [
        DummyEntity::class, // 임시 더미
        // UserEntity::class, // TODO: 주석 해제
    ],
    version = 1,
    exportSchema = false
)
abstract class PolaDatabase : RoomDatabase() {
    // abstract fun userDao(): UserDao // TODO: 주석 해제
}