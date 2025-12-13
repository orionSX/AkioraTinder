package com.example.akioratinder.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Chat(
    @SerialName("_id") val id: String = "",
    val user1: String,
    val user2: String,
    val ignoredBy: List<String> = emptyList(),
    val createdAt: String = ""
)

@Serializable
data class ChatMessage(
    @SerialName("_id") val id: String = "",
    val creatorId: String,
    val text: String,
    val timestamp: String = "",
    val status: MessageStatus = MessageStatus.SENT,
    val chatId: String
)

@Serializable
enum class MessageStatus {
    @SerialName("sent") SENT,
    @SerialName("delivered") DELIVERED,
    @SerialName("read") READ
}

@Serializable
data class WsMessage(
    val type: String, // "message", "typing", "read_receipt"
    val data: Map<String, String>
)

@Serializable
data class TypingNotification(
    val chatId: String,
    val userId: String,
    val isTyping: Boolean
)

@Serializable
data class ReadReceipt(
    val messageId: String,
    val userId: String
)