package com.example.akioratinder.data

import java.util.*


data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)


data class Chat(
    val chatId: String,
    val user1: String,
    val user2: String
)