package com.jinjinjara.pola.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val timestamp: Long,

    val messageType: String, // "USER", "BOT", "BOT_IMAGE", "BOT_IMAGE_GRID"

    // Content fields (nullable based on type)
    val text: String? = null,
    val imageUrl: String? = null,
    val tags: String? = null, // JSON string of List<String>
    val images: String? = null // JSON string of List<ImageData>
)
