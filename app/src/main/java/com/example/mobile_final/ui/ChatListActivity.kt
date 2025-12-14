package com.example.mobile_final.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.mobile_final.MainActivity
import com.example.mobile_final.R
import com.example.mobile_final.services.ApiService
import com.example.mobile_final.ui.theme.Mobile_finalTheme
import com.example.mobile_final.utils.LocaleHelper
import com.example.mobile_final.utils.PreferencesManager
import com.example.mobile_final.viewmodels.ChatViewModel
import kotlinx.coroutines.launch

class ChatsListActivity : ComponentActivity() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var preferencesManager: PreferencesManager

    override fun attachBaseContext(newBase: Context) {
        val preferencesManager = PreferencesManager(newBase)
        val language = preferencesManager.getLanguage()
        val context = LocaleHelper.setLocale(newBase, language)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)

        // Enable edge-to-edge
        enableEdgeToEdge()

        viewModel = ViewModelProvider.AndroidViewModelFactory
            .getInstance(application)
            .create(ChatViewModel::class.java)

        viewModel.initialize(applicationContext)

        setContent {
            Mobile_finalTheme(
                darkTheme = when (preferencesManager.getThemeMode()) {
                    "light" -> false
                    "dark" -> true
                    else -> isSystemInDarkTheme()
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ChatsListScreen(
                        viewModel = viewModel,
                        onBackClick = {val intent = Intent(this@ChatsListActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish() } // Добавляем обработчик нажатия "Назад"
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadChats()
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit // Параметр для обработки нажатия "Назад"
) {
    val chats by viewModel.chats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { ApiService.getInstance(context) }

    // Состояние для хранения имен пользователей
    val userNames = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(Unit) {
        viewModel.loadChats()
    }

    // Загружаем имена пользователей при изменении списка чатов
    LaunchedEffect(chats) {
        if (chats.isNotEmpty() && currentUserId != null) {
            scope.launch {
                chats.forEach { chat ->
                    val otherUserId = if (chat.user1 == currentUserId) chat.user2 else chat.user1
                    if (otherUserId != currentUserId && !userNames.containsKey(otherUserId)) {
                        try {
                            val userProfile = apiService.getUserById(otherUserId)
                            userNames[otherUserId] = userProfile.name.takeIf { it.isNotEmpty() }
                                ?: "Пользователь $otherUserId"
                        } catch (e: Exception) {
                            userNames[otherUserId] = "Пользователь $otherUserId"
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            // Добавляем TopAppBar с кнопкой "Назад"
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.chats),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && chats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()

                    }
                }
            } else if (chats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = stringResource(R.string.no_chats),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chats, key = { it.id }) { chat ->
                        val otherUserId = if (chat.user1 == currentUserId) chat.user2 else chat.user1
                        val userName = userNames[otherUserId] ?: "Загрузка..."

                        ChatListItem(
                            chat = chat,
                            currentUserId = currentUserId ?: "",
                            userName = userName,
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
    }
}