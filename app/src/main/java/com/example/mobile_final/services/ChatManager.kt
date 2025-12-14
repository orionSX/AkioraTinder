// ChatManager.kt
package com.example.mobile_final.services

import android.content.Context
import android.util.Log
import com.example.mobile_final.dto.*
import com.example.mobile_final.storage.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class ChatManager(
    private val context: Context,
    private val apiService: ApiService,
    private val userStore: UserStore
) {
    private val tag = "ChatManager"

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

    suspend fun loadChats(): List<Chat> = withContext(Dispatchers.IO) {
        _isLoading.value = true
        _error.value = null

        try {
            Log.d(tag, "Loading chats...")
            val loadedChats = apiService.getChats()
            Log.d(tag, "Loaded ${loadedChats.size} chats")
            _chats.value = loadedChats

            loadedChats
        } catch (e: Exception) {
            val errorMsg = "Ошибка загрузки чатов: ${e.message}"
            _error.value = errorMsg
            Log.e(tag, errorMsg)
            emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun loadMessages(chatId: String, limit: Int = 50) = withContext(Dispatchers.IO) {
        _isLoading.value = true
        _error.value = null

        try {
            Log.d(tag, "Loading messages for chat: $chatId")

            // Загружаем информацию о чате
            val chat = apiService.getChatById(chatId)
            _activeChat.value = chat
            Log.d(tag, "Chat loaded: ${chat?.id}")

            // Загружаем сообщения
            val loadedMessages = apiService.getMessages(chatId, limit = limit)
            Log.d(tag, "Loaded ${loadedMessages.size} messages")

            // Сортируем по времени
            val sortedMessages = loadedMessages.sortedBy { it.timestamp }
            _messages.value = sortedMessages
            Log.d(tag, "Messages set to state flow")

            // Отмечаем сообщения как прочитанные
            val userId = userStore.getUserId()
            loadedMessages
                .filter { it.creatorId != userId && it.status != MessageStatus.READ }
                .forEach { message ->
                    apiService.markMessageAsRead(message.id)
                }
        } catch (e: Exception) {
            val errorMsg = "Ошибка загрузки сообщений: ${e.message}"
            _error.value = errorMsg
            Log.e(tag, errorMsg, e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun sendMessage(text: String): ChatMessage? = withContext(Dispatchers.IO) {
        val chat = _activeChat.value ?: return@withContext null
        val userId = userStore.getUserId() ?: return@withContext null

        _error.value = null

        return@withContext try {
            Log.d(tag, "Sending message: $text to chat: ${chat.id}")
            val message = apiService.sendMessage(chat.id, text)
            message?.let { msg ->
                Log.d(tag, "Message sent successfully: ${msg.id}")
                // Обновляем локальный список сообщений
                _messages.update { current ->
                    val newList = (current + msg).sortedBy { it.timestamp }
                    newList
                }
            }
            message
        } catch (e: Exception) {
            val errorMsg = "Ошибка отправки сообщения: ${e.message}"
            _error.value = errorMsg
            Log.e(tag, errorMsg, e)
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
}