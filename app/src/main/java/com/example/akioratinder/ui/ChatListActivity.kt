package com.example.akioratinder.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.akioratinder.data.Chat
import com.example.akioratinder.services.ApiService
import com.example.akioratinder.services.AuthManager
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AkioraTinderTheme {
                Scaffold(
                    topBar = {
                        ChatListTopBar(
                            onBackClick = { onBackPressed() }
                        )
                    },
                    bottomBar = { BottomNav(2) } // ChatListActivity is at index 2
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        ChatListScreen()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text("Chats", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

@Composable
fun ChatListScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { ApiService.getInstance(context) }
    val authManager = remember { AuthManager.getInstance(context) }

    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val currentUserId = authManager.currentUser.value?.id

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            coroutineScope.launch {
                try {
                    // Get all chats for the current user
                    val allChats = apiService.getChats()
                    chats = allChats.filter { 
                        it.user1 == currentUserId || it.user2 == currentUserId 
                    }
                    isLoading = false
                } catch (e: Exception) {
                    // Handle error
                    isLoading = false
                }
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        LazyColumn {
            items(chats) { chat ->
                ChatListItem(
                    chat = chat,
                    currentUserId = currentUserId ?: "",
                    onClick = {
                        // Navigate to chat
                        val intent = Intent(context, ChatActivity::class.java).apply {
                            putExtra("chat_id", chat.id)
                            putExtra("target_user_name", getTargetUserName(chat, currentUserId ?: ""))
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun ChatListItem(chat: Chat, currentUserId: String, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getInitials(getTargetUserName(chat, currentUserId)),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = getTargetUserName(chat, currentUserId),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                
                // We don't have last message in the current Chat model, so showing a placeholder
                Text(
                    text = "Tap to start chatting...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = dateFormat.format(java.util.Date()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper function to get the other user's name in the chat
fun getTargetUserName(chat: Chat, currentUserId: String): String {
    return if (chat.user1 == currentUserId) {
        chat.user2  // Return the other user
    } else {
        chat.user1  // Return the other user
    }
}

fun getInitials(name: String): String {
    return if (name.isNotEmpty()) {
        name.split(" ").take(2).map { it.firstOrNull()?.uppercaseChar() ?: 'U' }.joinToString("")
    } else {
        "U"
    }
}