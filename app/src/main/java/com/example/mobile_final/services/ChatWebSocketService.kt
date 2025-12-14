// ChatWebSocketService.kt
package com.example.mobile_final.services

import android.content.Context
import android.util.Log
import com.example.mobile_final.dto.ChatWebSocketMessage
import com.example.mobile_final.storage.UserStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ChatWebSocketService(
    private val context: Context,
    private val userStore: UserStore
) {
    private val tag = "ChatWebSocketService"
    private var webSocket: WebSocket? = null
    private var isConnected = false

    private val _messages = MutableSharedFlow<ChatWebSocketMessage>()
    val messages: SharedFlow<ChatWebSocketMessage> = _messages.asSharedFlow()

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun connect(userId: String) {
        if (isConnected) return

        val request = Request.Builder()
            .url("wss://orion-vuz-mobile.vercel.app/ws/$userId")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(tag, "WebSocket connected")
                isConnected = true
                _connectionState.value = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(tag, "Message received: $text")
                try {
                    val message = ChatWebSocketMessage.fromJson(text)
                    message?.let {
                        kotlinx.coroutines.runBlocking {
                            _messages.emit(it)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error parsing message: ${e.message}")
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(tag, "WebSocket closed: $code $reason")
                isConnected = false
                _connectionState.value = false
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(tag, "WebSocket error: ${t.message}")
                isConnected = false
                _connectionState.value = false
                // Попробуем переподключиться через 5 секунд
                kotlinx.coroutines.MainScope().launch {
                    kotlinx.coroutines.delay(5000)
                    connect(userId)
                }
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Normal closure")
        webSocket = null
        isConnected = false
        _connectionState.value = false
    }

    fun sendMessage(message: String): Boolean {
        return if (isConnected) {
            webSocket?.send(message) == true
        } else {
            false
        }
    }

    fun isConnected(): Boolean = isConnected
}