// ChatListActivity.kt
package com.example.mobile_final.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.mobile_final.ui.theme.Mobile_finalThemeWithPref
import com.example.mobile_final.utils.PreferencesManager

class ChatsListActivity : ComponentActivity() {
    private lateinit var viewModel: ChatViewModel
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)

        viewModel = ViewModelProvider.AndroidViewModelFactory
            .getInstance(application)
            .create(ChatViewModel::class.java)

        setContent {
            Mobile_finalThemeWithPref(
                themeMode = preferencesManager.getThemeMode()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    ChatsListScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.connectWebSocket()
        viewModel.loadChats()
    }
}

@Composable
fun ChatsListScreen(viewModel: ChatViewModel) {
    val chats by viewModel.chats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val context = LocalContext.current

    if (isLoading && chats.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chats, key = { it.id }) { chat ->
                ChatListItem(
                    chat = chat,
                    currentUserId = currentUserId ?: "",
                    unreadCount = viewModel.getUnreadCount(chat.id),
                    onClick = {
                        val intent = Intent(context, ChatActivity::class.java)
                        intent.putExtra("chatId", chat.id)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}