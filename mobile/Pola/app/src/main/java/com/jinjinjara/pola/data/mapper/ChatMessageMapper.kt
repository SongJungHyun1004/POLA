package com.jinjinjara.pola.data.mapper

import com.jinjinjara.pola.data.local.database.converter.ChatMessageConverters
import com.jinjinjara.pola.data.local.database.entity.ChatMessageEntity
import com.jinjinjara.pola.presentation.ui.screen.search.ChatMessage
import com.jinjinjara.pola.presentation.ui.screen.search.ImageData

private val converters = ChatMessageConverters()

/**
 * ChatMessage (Domain) -> ChatMessageEntity (Data)
 */
fun ChatMessage.toEntity(): ChatMessageEntity? {
    val timestamp = System.currentTimeMillis()

    return when (this) {
        is ChatMessage.User -> ChatMessageEntity(
            timestamp = timestamp,
            messageType = "USER",
            text = this.text
        )

        is ChatMessage.Bot -> ChatMessageEntity(
            timestamp = timestamp,
            messageType = "BOT",
            text = this.text
        )

        is ChatMessage.BotImage -> ChatMessageEntity(
            timestamp = timestamp,
            messageType = "BOT_IMAGE",
            fileId = this.fileId,
            imageUrl = this.imageUrl,
            tags = converters.fromStringList(this.tags)
        )

        is ChatMessage.BotImageGrid -> ChatMessageEntity(
            timestamp = timestamp,
            messageType = "BOT_IMAGE_GRID",
            images = converters.fromImageDataList(this.images)
        )

        is ChatMessage.BotLoading -> {
            // BotLoading은 저장하지 않음 (임시 UI 상태)
            null
        }
    }
}

/**
 * ChatMessageEntity (Data) -> ChatMessage (Domain)
 */
fun ChatMessageEntity.toChatMessage(): ChatMessage? {
    return when (messageType) {
        "USER" -> text?.let { ChatMessage.User(it) }

        "BOT" -> text?.let { ChatMessage.Bot(it) }

        "BOT_IMAGE" -> {
            val id = fileId ?: 0L
            val url = imageUrl ?: return null
            val tagsList = converters.toStringList(tags) ?: emptyList()
            ChatMessage.BotImage(id, url, tagsList)
        }

        "BOT_IMAGE_GRID" -> {
            val imagesList = converters.toImageDataList(images) ?: emptyList()
            ChatMessage.BotImageGrid(imagesList)
        }

        else -> null
    }
}
