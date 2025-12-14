// ChatViewModel.kt
package com.example.mobile_final.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_final.services.ApiService
import com.example.mobile_final.services.ChatManager
import com.example.mobile_final.storage.UserStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val userStore = UserStore(context)
    private val apiService = ApiService.getInstance(context)

    private val chatManager by lazy {
        ChatManager(context, apiService, userStore)
    }

    // Публичные потоки данных
    val activeChat: StateFlow<com.example.mobile_final.dto.Chat?> = chatManager.activeChat
    val messages: StateFlow<List<com.example.mobile_final.dto.ChatMessage>> = chatManager.messages
    val chats: StateFlow<List<com.example.mobile_final.dto.Chat>> = chatManager.chats
    val isLoading: StateFlow<Boolean> = chatManager.isLoading
    val error: StateFlow<String?> = chatManager.error

    // Дополнительные состояния
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    init {
        // Инициализируем текущего пользователя
        _currentUserId.value = userStore.getUserId()

        // Подписываемся на обновления
        viewModelScope.launch {
            userStore.userDataFlow.collect { user ->
                _currentUserId.value = user?.id
            }
        }
    }

    fun setActiveChat(chatId: String) {
        viewModelScope.launch {
            chatManager.loadMessages(chatId)
        }
    }

    fun loadChats() {
        viewModelScope.launch {
            chatManager.loadChats()
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            chatManager.sendMessage(text)
        }
    }

    fun toggleIgnoreChat(chatId: String) {
        viewModelScope.launch {
            if (isChatIgnored(chatId)) {
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

    fun getTotalUnreadCount(): Int {
        return chatManager.getTotalUnreadCount()
    }

    fun connectWebSocket() {
        chatManager.connectWebSocket()
    }

    fun disconnectWebSocket() {
        chatManager.disconnectWebSocket()
    }

    fun clearError() {
        // Нужно добавить метод в ChatManager для очистки ошибок
        // пока просто устанавливаем null
        // chatManager.clearError()
    }

    override fun onCleared() {
        super.onCleared()
        chatManager.disconnectWebSocket()
    }
}