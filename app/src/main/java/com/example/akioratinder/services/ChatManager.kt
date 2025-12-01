package com.example.akioratinder.services

import android.content.Context
import androidx.core.content.edit
import com.example.akioratinder.data.Chat
import com.example.akioratinder.data.ChatMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ChatManager {

    private val gson = Gson()
    private lateinit var prefs: android.content.SharedPreferences

    private val messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences("chat_storage", Context.MODE_PRIVATE)
        loadMessages()
    }

    fun getMessagesFlow(context: Context) = messagesFlow.asStateFlow()

    private fun loadMessages() {
        val json = prefs.getString("messages", "[]") ?: "[]"
        val type = object : TypeToken<List<ChatMessage>>() {}.type
        val loaded = gson.fromJson<List<ChatMessage>>(json, type)
        messagesFlow.value = loaded
    }

    private fun saveMessages() {
        prefs.edit {
            putString("messages", gson.toJson(messagesFlow.value))
        }
    }

    fun getOrCreateChat(context: Context, u1: String, u2: String): Chat {
        val chatId = listOf(u1, u2).sorted().joinToString("_")
        return Chat(chatId, u1, u2)
    }

    fun sendMessage(context: Context, chatId: String, from: String, to: String, text: String) {
        val msg = ChatMessage(
            chatId = chatId,
            senderId = from,
            receiverId = to,
            message = text
        )

        val updated = messagesFlow.value.toMutableList().apply { add(0, msg) }
        messagesFlow.value = updated
        saveMessages()
    }
}
