package com.example.akioratinder.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.akioratinder.data.Chat
import com.example.akioratinder.data.ChatMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatStore(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("chat_store", Context.MODE_PRIVATE)

    private val gson = Gson()

    companion object {
        private const val KEY_CHATS = "chats_list"
        private const val KEY_MESSAGES = "messages_list"
    }


    private val _chats = MutableStateFlow(loadChats())
    val chats: StateFlow<List<Chat>> = _chats

    private val _messages = MutableStateFlow(loadMessages())
    val messages: StateFlow<List<ChatMessage>> = _messages


    private fun loadChats(): List<Chat> {
        val json = prefs.getString(KEY_CHATS, null) ?: return emptyList()
        val type = object : TypeToken<List<Chat>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun loadMessages(): List<ChatMessage> {
        val json = prefs.getString(KEY_MESSAGES, null) ?: return emptyList()
        val type = object : TypeToken<List<ChatMessage>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }


    private fun saveChats(chats: List<Chat>) {
        prefs.edit().putString(KEY_CHATS, gson.toJson(chats)).apply()
        _chats.value = chats
    }

    private fun saveMessages(messages: List<ChatMessage>) {
        prefs.edit().putString(KEY_MESSAGES, gson.toJson(messages)).apply()
        _messages.value = messages
    }



    fun getOrCreateChat(user1: String, user2: String): Chat {
        val chatId = generateChatId(user1, user2)

        val existing = _chats.value.find { it.chatId == chatId }
        if (existing != null) return existing

        val newChat = Chat(chatId, user1, user2)
        val updated = _chats.value.toMutableList().apply { add(newChat) }

        saveChats(updated)
        return newChat
    }


    fun sendMessage(chatId: String, senderId: String, receiverId: String, text: String) {
        val newMessage = ChatMessage(
            chatId = chatId,
            senderId = senderId,
            receiverId = receiverId,
            message = text
        )

        val updatedMessages = _messages.value.toMutableList().apply {
            add(0, newMessage)
        }

        saveMessages(updatedMessages)
    }


    fun getChatMessages(chatId: String): List<ChatMessage> =
        _messages.value.filter { it.chatId == chatId }




    private fun generateChatId(user1: String, user2: String): String =
        listOf(user1, user2).sorted().joinToString("_")

    fun getChat(chatId: String): Chat? =
        _chats.value.find { it.chatId == chatId }
}
