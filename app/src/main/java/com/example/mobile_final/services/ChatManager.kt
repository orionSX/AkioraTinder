// ChatManager.kt
package com.example.mobile_final.services

import android.content.Context
import android.util.Log
import com.example.mobile_final.dto.*
import com.example.mobile_final.storage.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ChatManager(
    private val context: Context,
    private val apiService: ApiService,
    private val userStore: UserStore
) {
    private val tag = "ChatManager"

    private val pollingChatService = PollingChatService(context, apiService, userStore)

    private val _activeChat = MutableStateFlow<Chat?>(null)
    val activeChat: StateFlow<Chat?> = _activeChat.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Subscribe to polling messages
        pollingChatService.messages
            .onEach { message -> handlePolledMessage(message) }
            .launchIn(kotlinx.coroutines.MainScope())
    }

    fun startPolling() {
        pollingChatService.startPolling()
    }

    fun stopPolling() {
        pollingChatService.stopPolling()
    }

    suspend fun loadChats(): List<Chat> = withContext(Dispatchers.IO) {
        _isLoading.value = true
        _error.value = null

        try {
            val loadedChats = apiService.getChats()
            _chats.value = loadedChats
            
            loadedChats
        } catch (e: Exception) {
            _error.value = "Ошибка загрузки чатов: ${e.message}"
            Log.e(tag, "Error loading chats: ${e.message}")
            emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun loadMessages(chatId: String, limit: Int = 50) = withContext(Dispatchers.IO) {
        _isLoading.value = true
        _error.value = null

        try {
            val chat = apiService.getChatById(chatId)
            _activeChat.value = chat

            val loadedMessages = apiService.getMessages(chatId, limit = limit)
            _messages.value = loadedMessages.sortedBy { it.timestamp }

            // Отмечаем сообщения как прочитанные
            loadedMessages
                .filter { it.creatorId != userStore.getUserId() && it.status != MessageStatus.READ }
                .forEach { message ->
                    apiService.markMessageAsRead(message.id)
                }
        } catch (e: Exception) {
            _error.value = "Ошибка загрузки сообщений: ${e.message}"
            Log.e(tag, "Error loading messages: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun sendMessage(text: String): ChatMessage? = withContext(Dispatchers.IO) {
        val chat = _activeChat.value ?: return@withContext null
        val userId = userStore.getUserId() ?: return@withContext null

        _error.value = null

        return@withContext try {
            val message = apiService.sendMessage(chat.id, text)
            message?.let { msg ->
                // Обновляем локальный список сообщений
                _messages.update { current ->
                    (current + msg).sortedBy { it.timestamp }
                }
            }
            message
        } catch (e: Exception) {
            _error.value = "Ошибка отправки сообщения: ${e.message}"
            Log.e(tag, "Error sending message: ${e.message}")
            null
        }
    }

    suspend fun ignoreChat(chatId: String) = withContext(Dispatchers.IO) {
        _error.value = null

        try {
            val success = apiService.ignoreChat(chatId)
            if (success) {
                updateChatIgnoredStatus(chatId, true)
            }
        } catch (e: Exception) {
            _error.value = "Ошибка игнорирования чата: ${e.message}"
            Log.e(tag, "Error ignoring chat: ${e.message}")
        }
    }

    suspend fun unignoreChat(chatId: String) = withContext(Dispatchers.IO) {
        _error.value = null

        try {
            val success = apiService.unignoreChat(chatId)
            if (success) {
                updateChatIgnoredStatus(chatId, false)
            }
        } catch (e: Exception) {
            _error.value = "Ошибка отмены игнорирования: ${e.message}"
            Log.e(tag, "Error unignoring chat: ${e.message}")
        }
    }

    fun clearActiveChat() {
        _activeChat.value = null
        _messages.value = emptyList()
    }

    private fun handlePolledMessage(message: ChatMessage) {
        // Если это сообщение для активного чата, добавляем его
        if (_activeChat.value?.id == message.chatId) {
            _messages.update { current ->
                (current + message).sortedBy { it.timestamp }
            }
        }
    }

    private fun updateChatIgnoredStatus(chatId: String, ignoredBy: List<String>?) {
        val userId = userStore.getUserId() ?: return

        _chats.update { current ->
            current.map { chat ->
                if (chat.id == chatId) {
                    chat.copy(
                        ignoredBy = ignoredBy ?: chat.ignoredBy
                    )
                } else chat
            }
        }

        if (_activeChat.value?.id == chatId) {
            _activeChat.update { chat ->
                chat?.copy(
                    ignoredBy = ignoredBy ?: chat.ignoredBy
                )
            }
        }
    }

    private fun updateChatIgnoredStatus(chatId: String, isIgnored: Boolean) {
        val userId = userStore.getUserId() ?: return

        _chats.update { current ->
            current.map { chat ->
                if (chat.id == chatId) {
                    if (isIgnored) {
                        chat.copy(
                            ignoredBy = (chat.ignoredBy + userId).distinct()
                        )
                    } else {
                        chat.copy(
                            ignoredBy = chat.ignoredBy.filter { it != userId }
                        )
                    }
                } else chat
            }
        }

        if (_activeChat.value?.id == chatId) {
            _activeChat.update { chat ->
                if (isIgnored) {
                    chat?.copy(
                        ignoredBy = (chat?.ignoredBy ?: emptyList()) + userId
                    )
                } else {
                    chat?.copy(
                        ignoredBy = chat.ignoredBy.filter { it != userId }
                    )
                }
            }
        }
    }

    fun getUnreadCount(chatId: String): Int {
        val userId = userStore.getUserId() ?: return 0
        return _messages.value.count {
            it.chatId == chatId &&
                    it.creatorId != userId &&
                    it.status != MessageStatus.READ
        }
    }

    fun getTotalUnreadCount(): Int {
        return _chats.value.sumOf { chat ->
            getUnreadCount(chat.id)
        }
    }

    fun isChatIgnored(chatId: String): Boolean {
        val userId = userStore.getUserId() ?: return false
        return _chats.value.find { it.id == chatId }?.ignoredBy?.contains(userId) == true
    }

    // Method to get user names for chat participants
    suspend fun getUserNamesForChats(): Map<String, String> = withContext(Dispatchers.IO) {
        val userId = userStore.getUserId() ?: return@withContext emptyMap()
        val chats = _chats.value
        val userNames = mutableMapOf<String, String>()
        
        for (chat in chats) {
            // Get the other participant in the chat (not the current user)
            val otherUserId = if (chat.user1 == userId) chat.user2 else chat.user1
            if (otherUserId != userId && !userNames.containsKey(otherUserId)) {
                try {
                    val userProfile = apiService.getUserById(otherUserId)
                    userNames[otherUserId] = userProfile.name
                } catch (e: Exception) {
                    Log.e(tag, "Error getting user info for $otherUserId: ${e.message}")
                    userNames[otherUserId] = "Пользователь $otherUserId" // Fallback name
                }
            }
        }
        
        userNames
    }
}