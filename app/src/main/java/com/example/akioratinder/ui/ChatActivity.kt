package com.example.akioratinder.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.akioratinder.data.ChatMessage
import com.example.akioratinder.services.ChatManager
import com.example.akioratinder.services.GlobalSettingsManager
import com.example.akioratinder.services.ProfilesManager
import com.example.akioratinder.storage.ThemeLanguageStore
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import com.example.akioratinder.viewmodels.ChatViewModel
import com.example.akioratinder.viewmodels.ChatViewModelFactory
import com.example.akioratinder.viewmodels.ThemeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : ComponentActivity() {
    private lateinit var themeStore: ThemeLanguageStore
    private lateinit var targetUser: String

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(themeStore)
    }

    private val chatViewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(targetUser)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        targetUser = intent.getStringExtra("target_user") ?: "Unknown"

        GlobalSettingsManager.initialize(this)
        ChatManager.initialize(this)
        themeStore = ThemeLanguageStore(this)

        super.onCreate(savedInstanceState)

        setContent {
            val darkTheme by themeViewModel.darkTheme.collectAsState()

            AkioraTinderTheme(darkTheme = darkTheme) {
                Scaffold(
                    topBar = {
                        ChatTopBar(
                            targetUser = targetUser,
                            onBackClick = { onBackPressed() }
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        ChatScreen(
                            chatViewModel = chatViewModel,
                            targetUser = targetUser
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreen(chatViewModel: ChatViewModel, targetUser: String) {
    val context = LocalContext.current
    val currentUser = ProfilesManager.getCurrentUserProfile(context).summonerName

    val messages by chatViewModel.getMessagesFlow(context).collectAsState()

    var newMessageText by remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            state = scrollState,
            reverseLayout = true
        ) {
            items(messages, key = { it.id }) { message ->
                ChatMessageItem(
                    message = message,
                    isOwnMessage = message.senderId == currentUser
                )
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
                    if (newMessageText.isNotBlank()) {
                        chatViewModel.sendMessage(context, targetUser, newMessageText)
                        newMessageText = ""
                        coroutineScope.launch {
                            scrollState.animateScrollToItem(0)
                        }
                    }
                },
                enabled = newMessageText.isNotBlank()
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
                    text = message.message,
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
