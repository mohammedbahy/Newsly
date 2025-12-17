package com.bahy.newsly.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahy.newsly.data.model.ChatMessage
import com.bahy.newsly.data.repository.AuthRepository
import com.bahy.newsly.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ChatUiState(
    val isLoading: Boolean = false,
    val isTyping: Boolean = false, // For typing indicator
    val error: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val input: String = ""
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var userId: String? = null

    init {
        observeUserAndMessages()
    }

    private fun observeUserAndMessages() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                userId = user?.id
                if (userId != null) {
                    chatRepository.streamMessages(userId!!).collectLatest { messages ->
                        _uiState.value = _uiState.value.copy(messages = messages)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(messages = emptyList())
                }
            }
        }
    }

    fun updateInput(value: String) {
        _uiState.value = _uiState.value.copy(input = value)
    }

    fun sendMessage() {
        val text = _uiState.value.input.trim()
        val uid = userId
        if (text.isEmpty() || uid == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                isTyping = true, // Show typing indicator
                error = null, 
                input = ""
            )
            chatRepository.sendMessage(uid, text)
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isTyping = false,
                        error = e.message
                    )
                }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isTyping = false
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearChat() {
        val uid = userId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            chatRepository.clearConversation(uid)
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }
}

