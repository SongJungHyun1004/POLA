package com.jinjinjara.pola.data.repository

import com.jinjinjara.pola.data.local.database.dao.ChatMessageDao
import com.jinjinjara.pola.data.mapper.toChatMessage
import com.jinjinjara.pola.data.mapper.toEntity
import com.jinjinjara.pola.domain.repository.ChatRepository
import com.jinjinjara.pola.presentation.ui.screen.search.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of ChatRepository
 *
 * Uses Room Database to persist chat messages during app session.
 * Messages are automatically cleared on app restart and logout.
 */
class ChatRepositoryImpl @Inject constructor(
    private val chatMessageDao: ChatMessageDao
) : ChatRepository {

    override fun getAllMessages(): Flow<List<ChatMessage>> {
        return chatMessageDao.getAllMessages()
            .map { entities ->
                entities.mapNotNull { it.toChatMessage() }
            }
    }

    override suspend fun saveMessage(message: ChatMessage) {
        // toEntity() returns null for BotLoading (don't persist transient UI state)
        val entity = message.toEntity()
        if (entity != null) {
            chatMessageDao.insertMessage(entity)
        }
    }

    override suspend fun clearAllMessages() {
        chatMessageDao.clearAllMessages()
    }
}
