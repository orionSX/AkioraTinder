package com.example.akioratinder.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.akioratinder.data.ChatMessage
import com.example.akioratinder.services.ApiService
import com.example.akioratinder.services.AuthManager
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : ComponentActivity() {
    private lateinit var chatId: String
    private lateinit var targetUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        chatId = intent.getStringExtra("chat_id") ?: ""
        targetUserName = intent.getStringExtra("target_user_name") ?: "User"

        super.onCreate(savedInstanceState)

        setContent {
            AkioraTinderTheme {
                Scaffold(
                    topBar = {
                        ChatTopBar(
                            targetUser = targetUserName,
                            onBackClick = { onBackPressed() }
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        ChatScreen(chatId = chatId)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreen(chatId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { ApiService.getInstance(context) }
    val authManager = remember { AuthManager.getInstance(context) }

    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var newMessageText by remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()

    val currentUserId by authManager.currentUser.collectAsState()

    LaunchedEffect(chatId) {
        if (chatId.isNotEmpty()) {
            coroutineScope.launch {
                try {
                    messages = apiService.getMessages(chatId)
                    isLoading = false
                } catch (e: Exception) {
                    // Обработка ошибки
                    isLoading = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.weight(1f)) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                state = scrollState,
                reverseLayout = true
            ) {
                items(messages, key = { it.id }) { message ->
                    val isOwnMessage = message.creatorId == currentUserId?.id
                    ChatMessageItem(
                        message = message,
                        isOwnMessage = isOwnMessage
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newMessageText,
                onValueChange = { newMessageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (newMessageText.isNotBlank() && chatId.isNotEmpty()) {
                        coroutineScope.launch {
                            try {
                                apiService.sendMessage(chatId, newMessageText)
                                newMessageText = ""
                                // Обновляем сообщения
                                messages = apiService.getMessages(chatId)
                                coroutineScope.launch {
                                    scrollState.animateScrollToItem(0)
                                }
                            } catch (e: Exception) {
                                // Обработка ошибки
                            }
                        }
                    }
                },
                enabled = newMessageText.isNotBlank() && chatId.isNotEmpty()
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage, isOwnMessage: Boolean) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isOwnMessage)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    color = if (isOwnMessage)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = dateFormat.format(Date(message.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(targetUser: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(targetUser, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Online",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    )
}