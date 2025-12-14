package com.example.mobile_final.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_final.dto.Chat
import com.example.mobile_final.dto.ChatMessage
import com.example.mobile_final.services.AuthManager
import com.example.mobile_final.services.ChatManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context

class ChatViewModel : ViewModel() {
    private lateinit var chatManager: ChatManager
    private lateinit var authManager: AuthManager
    private var isInitialized = false

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    private val _activeChat = MutableStateFlow<com.example.mobile_final.dto.Chat?>(null)
    val activeChat: StateFlow<com.example.mobile_final.dto.Chat?> = _activeChat.asStateFlow()

    val chats = MutableStateFlow<List<Chat>>(emptyList())
    val messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    val currentUserId = MutableStateFlow<String?>(null)

    fun initialize(context: Context) {
        if (!isInitialized) {
            authManager = AuthManager.getInstance(context)
            chatManager = ChatManager(context,
                com.example.mobile_final.services.ApiService.getInstance(context),
                com.example.mobile_final.storage.UserStore(context)
            )
            isInitialized = true

            // Load user ID
            currentUserId.value = authManager.currentUser?.value?.id

            // Observe flows from ChatManager
            viewModelScope.launch {
                chatManager.chats.collect { chatList ->
                    chats.value = chatList
                }
            }

            viewModelScope.launch {
                chatManager.messages.collect { messageList ->
                    messages.value = messageList
                }
            }

            viewModelScope.launch {
                chatManager.activeChat.collect {
                    // Keep active chat synced
                }
            }
        }
    }

    fun setActiveChat(chatId: String) {
        _activeChatId.value = chatId
        viewModelScope.launch {
            isLoading.value = true
            try {
                chatManager.loadMessages(chatId)
                
                // Get the active chat from the manager and update our local state
                val chat = chatManager.activeChat.value
                _activeChat.value = chat
            } catch (e: Exception) {
                error.value = "Ошибка загрузки сообщений: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loadChats() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                chatManager.loadChats()
            } catch (e: Exception) {
                error.value = "Ошибка загрузки чатов: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                chatManager.sendMessage(text)
            } catch (e: Exception) {
                error.value = "Ошибка отправки сообщения: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun toggleIgnoreChat(chatId: String) {
        viewModelScope.launch {
            val isIgnored = isChatIgnored(chatId)
            if (isIgnored) {
                chatManager.unignoreChat(chatId)
            } else {
                chatManager.ignoreChat(chatId)
            }
        }
    }

    fun isChatIgnored(chatId: String): Boolean {
        return chatManager.isChatIgnored(chatId)
    }

    fun getUnreadCount(chatId: String): Int {
        return chatManager.getUnreadCount(chatId)
    }

    fun connectWebSocket() {
        // Start polling for new messages
        chatManager.startPolling()
    }

    fun disconnectWebSocket() {
        // Stop polling
        chatManager.stopPolling()
    }

    fun clearActiveChat() {
        _activeChatId.value = null
        _activeChat.value = null
        chatManager.clearActiveChat()
    }
}