package com.jinjinjara.pola.presentation.ui.screen.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jinjinjara.pola.domain.model.RagSearchResult
import com.jinjinjara.pola.domain.repository.ChatRepository
import com.jinjinjara.pola.domain.usecase.search.SearchWithRagUseCase
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val searchWithRagUseCase: SearchWithRagUseCase,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatbotUiState>(ChatbotUiState.Idle)
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    // 채팅 메시지 목록 (Room Database에서 로드)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    init {
        loadMessages()
    }

    /**
     * Room Database에서 저장된 메시지 로드 (초기 로딩만)
     * 저장된 메시지가 없으면 초기 인사 메시지 추가 및 저장
     */
    private fun loadMessages() {
        viewModelScope.launch {
            // first()를 사용하여 초기 로딩만 수행 (계속 관찰하지 않음)
            val savedMessages = chatRepository.getAllMessages().first()

            if (savedMessages.isEmpty()) {
                // 저장된 메시지가 없으면 초기 인사 메시지 추가
                val greeting = ChatMessage.Bot("저는 상담포아예요. 무엇을 도와드릴까요?")
                _messages.value = listOf(greeting)
                // 초기 메시지도 저장
                chatRepository.saveMessage(greeting)
            } else {
                _messages.value = savedMessages
            }
        }
    }

    fun addUserMessage(text: String) {
        val message = ChatMessage.User(text)
        _messages.value = _messages.value + message
        // Room Database에 저장
        viewModelScope.launch {
            chatRepository.saveMessage(message)
        }
    }

    fun addLoadingMessage() {
        _messages.value = _messages.value + ChatMessage.BotLoading
    }

    fun removeLoadingMessage() {
        _messages.value = _messages.value.filterNot { it is ChatMessage.BotLoading }
    }

    fun search(query: String) {
        Log.d("ChatbotViewModel", "Starting search: $query")
        viewModelScope.launch {
            _uiState.value = ChatbotUiState.Loading

            when (val result = searchWithRagUseCase(query)) {
                is Result.Success -> {
                    Log.d("ChatbotViewModel", "Search success: ${result.data.sources.size} sources")

                    // 로딩 메시지 제거
                    removeLoadingMessage()

                    // 챗봇 답변 추가 및 저장
                    val botAnswer = ChatMessage.Bot(result.data.answer)
                    _messages.value = _messages.value + botAnswer
                    chatRepository.saveMessage(botAnswer)

                    // PolaCard 이미지 추가 (sources)
                    if (result.data.sources.size == 1) {
                        // 1개면 단일 이미지로 표시
                        val source = result.data.sources.first()
                        val imageMessage = ChatMessage.BotImage(
                            imageUrl = source.src,
                            tags = source.tags
                        )
                        _messages.value = _messages.value + imageMessage
                        chatRepository.saveMessage(imageMessage)
                    } else if (result.data.sources.size >= 2) {
                        // 2개 이상이면 그리드로 표시
                        val imageDataList = result.data.sources.map { source ->
                            ImageData(
                                imageUrl = source.src,
                                tags = source.tags
                            )
                        }
                        val gridMessage = ChatMessage.BotImageGrid(imageDataList)
                        _messages.value = _messages.value + gridMessage
                        chatRepository.saveMessage(gridMessage)
                    }

                    _uiState.value = ChatbotUiState.Success(result.data)
                }
                is Result.Error -> {
                    Log.e("ChatbotViewModel", "Search error: ${result.message}")

                    // 로딩 메시지 제거
                    removeLoadingMessage()

                    // 에러 메시지 추가 및 저장
                    val errorMessage = ChatMessage.Bot("죄송합니다. ${result.message}")
                    _messages.value = _messages.value + errorMessage
                    chatRepository.saveMessage(errorMessage)

                    _uiState.value = ChatbotUiState.Error(result.message ?: "알 수 없는 오류가 발생했습니다")
                }
                is Result.Loading -> {
                    // Loading은 이미 위에서 설정했으므로 무시
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = ChatbotUiState.Idle
    }
}

sealed class ChatbotUiState {
    object Idle : ChatbotUiState()
    object Loading : ChatbotUiState()
    data class Success(val result: RagSearchResult) : ChatbotUiState()
    data class Error(val message: String) : ChatbotUiState()
}

// 메시지 타입 정의
sealed class ChatMessage {
    data class User(val text: String) : ChatMessage()
    data class Bot(val text: String) : ChatMessage()
    object BotLoading : ChatMessage()
    data class BotImage(val imageUrl: String, val tags: List<String>) : ChatMessage()
    data class BotImageGrid(val images: List<ImageData>) : ChatMessage()
}

data class ImageData(
    val imageUrl: String,
    val tags: List<String>
)
