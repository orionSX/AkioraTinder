package com.example.mobile_final.services

import android.content.Context
import android.util.Log
import com.example.mobile_final.dto.ChatMessage
import com.example.mobile_final.storage.UserStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class PollingChatService(
    private val context: Context,
    private val apiService: ApiService,
    private val userStore: UserStore
) {
    private val tag = "PollingChatService"
    private var pollingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _messages = MutableSharedFlow<ChatMessage>()
    val messages: SharedFlow<ChatMessage> = _messages.asSharedFlow()
    
    private val _pollingActive = MutableStateFlow(false)
    val pollingActive: StateFlow<Boolean> = _pollingActive.asStateFlow()
    
    // Store the last message timestamp for each chat to avoid duplicates
    private val lastMessageTimestamps = mutableMapOf<String, String>()
    
    fun startPolling() {
        if (pollingJob?.isActive == true) return
        
        pollingJob = scope.launch {
            _pollingActive.value = true
            val userId = userStore.getUserId() ?: return@launch
            
            while (isActive) {
                try {
                    // Get user's chats to know which chat IDs to poll
                    val chats = apiService.getChats()
                    
                    // Poll for new messages in each chat
                    for (chat in chats) {
                        pollChatMessages(chat.id)
                    }
                    
                    // Wait 5 seconds before next poll
                    delay(5000)
                } catch (e: Exception) {
                    Log.e(tag, "Error during polling: ${e.message}")
                    delay(5000) // Wait 5 seconds before retrying
                }
            }
        }
    }
    
    private suspend fun pollChatMessages(chatId: String) {
        try {
            // Get last message timestamp for this chat to only fetch newer messages
            val lastTimestamp = lastMessageTimestamps[chatId]
            
            // Fetch messages, potentially filtering by timestamp if the API supports it
            val messages = apiService.getMessages(chatId, limit = 50)
            
            for (message in messages) {
                // Only process messages newer than the last processed one
                if (lastTimestamp == null || message.timestamp > lastTimestamp) {
                    // Update the last timestamp for this chat
                    if (lastTimestamp == null || message.timestamp > lastTimestamp) {
                        lastMessageTimestamps[chatId] = message.timestamp
                    }
                    
                    // Emit the message if it's from another user (not the current user)
                    val currentUserId = userStore.getUserId()
                    if (currentUserId != null && message.creatorId != currentUserId) {
                        _messages.emit(message)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error polling messages for chat $chatId: ${e.message}")
        }
    }
    
    fun stopPolling() {
        pollingJob?.cancel()
        _pollingActive.value = false
    }
    
    fun isPollingActive(): Boolean = _pollingActive.value
    
    // Method to manually refresh messages for a specific chat
    suspend fun refreshChatMessages(chatId: String) {
        pollChatMessages(chatId)
    }
    
    // Method to get the last message timestamp for a chat
    fun getLastMessageTimestamp(chatId: String): String? {
        return lastMessageTimestamps[chatId]
    }
    
    // Method to clear last message timestamps (e.g., when switching chats)
    fun clearLastMessageTimestamps() {
        lastMessageTimestamps.clear()
    }
}