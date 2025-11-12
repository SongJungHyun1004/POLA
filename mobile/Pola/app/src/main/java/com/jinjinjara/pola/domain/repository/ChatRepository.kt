package com.jinjinjara.pola.domain.repository

import com.jinjinjara.pola.presentation.ui.screen.search.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * Chat message persistence repository
 *
 * Note: Messages are cleared on app restart and logout
 */
interface ChatRepository {

    /**
     * Get all chat messages ordered by timestamp
     * @return Flow of chat message list
     */
    fun getAllMessages(): Flow<List<ChatMessage>>

    /**
     * Save a chat message
     * Note: BotLoading messages are not persisted
     * @param message The message to save
     */
    suspend fun saveMessage(message: ChatMessage)

    /**
     * Clear all chat messages
     * Called on app restart and logout
     */
    suspend fun clearAllMessages()
}
