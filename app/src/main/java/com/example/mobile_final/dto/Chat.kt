package com.example.mobile_final.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import org.json.JSONObject

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
sealed class ChatWebSocketMessage {
    data class Message(
        @SerialName("type") val type: String = "message",
        @SerialName("chat_id") val chatId: String,
        @SerialName("message_id") val messageId: String,
        @SerialName("creator_id") val creatorId: String,
        @SerialName("text") val text: String,
        @SerialName("timestamp") val timestamp: String,
        @SerialName("status") val status: String
    ) : ChatWebSocketMessage()

    data class MessageStatus(
        @SerialName("type") val type: String = "message_status",
        @SerialName("message_id") val messageId: String,
        @SerialName("status") val status: String
    ) : ChatWebSocketMessage()

    data class ChatUpdate(
        @SerialName("type") val type: String = "chat_update",
        @SerialName("chat_id") val chatId: String,
        @SerialName("ignored_by") val ignoredBy: List<String>? = null
    ) : ChatWebSocketMessage()

    data class Error(
        @SerialName("type") val type: String = "error",
        @SerialName("error") val error: String
    ) : ChatWebSocketMessage()

    companion object {
        fun fromJson(json: String): ChatWebSocketMessage? {
            return try {
                val jsonObject = JSONObject(json)
                when (jsonObject.getString("type")) {
                    "message" -> Message(
                        chatId = jsonObject.getString("chat_id"),
                        messageId = jsonObject.getString("message_id"),
                        creatorId = jsonObject.getString("creator_id"),
                        text = jsonObject.getString("text"),
                        timestamp = jsonObject.getString("timestamp"),
                        status = jsonObject.getString("status")
                    )
                    "message_status" -> MessageStatus(
                        messageId = jsonObject.getString("message_id"),
                        status = jsonObject.getString("status")
                    )
                    "chat_update" -> ChatUpdate(
                        chatId = jsonObject.getString("chat_id"),
                        ignoredBy = if (jsonObject.has("ignored_by")) {
                            val array = jsonObject.getJSONArray("ignored_by")
                            (0 until array.length()).map { array.getString(it) }
                        } else null
                    )
                    "error" -> Error(
                        error = jsonObject.getString("error")
                    )
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}