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

    private val chatWebSocketService = ChatWebSocketService(context, userStore)

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
        // Подписываемся на WebSocket сообщения
        chatWebSocketService.messages
            .onEach { handleWebSocketMessage(it) }
            .launchIn(kotlinx.coroutines.MainScope())
    }

    fun connectWebSocket() {
        val userId = userStore.getUserId()
        userId?.let {
            chatWebSocketService.connect(it)
        }
    }

    fun disconnectWebSocket() {
        chatWebSocketService.disconnect()
    }

    suspend fun loadChats() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        _error.value = null

        try {
            val loadedChats = apiService.getChats()
            _chats.value = loadedChats
        } catch (e: Exception) {
            _error.value = "Ошибка загрузки чатов: ${e.message}"
            Log.e(tag, "Error loading chats: ${e.message}")
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

                // Отправляем через WebSocket если есть соединение
                if (chatWebSocketService.isConnected()) {
                    val json = JSONObject().apply {
                        put("type", "message")
                        put("chat_id", chat.id)
                        put("text", text)
                    }
                    chatWebSocketService.sendMessage(json.toString())
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

    private fun handleWebSocketMessage(message: ChatWebSocketMessage) {
        when (message) {
            is ChatWebSocketMessage.Message -> {
                // Новое сообщение
                val newMessage = ChatMessage(
                    id = message.messageId,
                    creatorId = message.creatorId,
                    text = message.text,
                    timestamp = message.timestamp,
                    status = when (message.status) {
                        "read" -> MessageStatus.READ
                        "delivered" -> MessageStatus.DELIVERED
                        else -> MessageStatus.SENT
                    },
                    chatId = message.chatId
                )

                // Если это сообщение для активного чата, добавляем его
                if (_activeChat.value?.id == message.chatId) {
                    _messages.update { current ->
                        (current + newMessage).sortedBy { it.timestamp }
                    }
                }
            }

            is ChatWebSocketMessage.MessageStatus -> {
                // Обновление статуса сообщения
                _messages.update { current ->
                    current.map { msg ->
                        if (msg.id == message.messageId) {
                            msg.copy(
                                status = when (message.status) {
                                    "read" -> MessageStatus.READ
                                    "delivered" -> MessageStatus.DELIVERED
                                    else -> msg.status
                                }
                            )
                        } else msg
                    }
                }
            }

            is ChatWebSocketMessage.ChatUpdate -> {
                // Обновление информации о чате
                updateChatIgnoredStatus(message.chatId, message.ignoredBy)
            }

            is ChatWebSocketMessage.Error -> {
                _error.value = message.error
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
}