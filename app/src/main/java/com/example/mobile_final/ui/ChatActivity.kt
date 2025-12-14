// ChatActivity.kt
package com.example.mobile_final.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.mobile_final.ui.theme.Mobile_finalThemeWithPref
import com.example.mobile_final.utils.PreferencesManager
import com.example.mobile_final.viewmodels.ChatViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : ComponentActivity() {
    private lateinit var viewModel: ChatViewModel
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)

        viewModel = ViewModelProvider.AndroidViewModelFactory
            .getInstance(application)
            .create(ChatViewModel::class.java)

        // Инициализируем ViewModel
        viewModel.initialize(applicationContext)

        val chatId = intent.getStringExtra("chatId")
        chatId?.let {
            viewModel.setActiveChat(it)
        }

        setContent {
            Mobile_finalThemeWithPref(themeMode = preferencesManager.getThemeMode()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.connectWebSocket()
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnectWebSocket()
    }
}

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val activeChatId by viewModel.activeChatId.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    val listState = rememberLazyListState()
    val context = LocalContext.current


    // Авто-скролл к последнему сообщению
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                chatId = activeChatId!!,
                isLoading = isLoading,
                onBackClick = { (context as ChatActivity).finish() },
                onIgnoreClick = { activeChatId?.let { viewModel.toggleIgnoreChat(it) } },
                isChatIgnored = activeChatId?.let { viewModel.isChatIgnored(it) } ?: false
            )
        },
        bottomBar = {
            MessageInputField(
                onSendMessage = { text ->
                    if (text.isNotBlank()) {
                        viewModel.sendMessage(text)
                    }
                },
                enabled = activeChatId != null && !isLoading

            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (activeChatId == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Выберите чат",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = false,
                    verticalArrangement = Arrangement.Top,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            isOwnMessage = message.creatorId == currentUserId,
                            modifier = Modifier
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            error?.let {
                AnimatedVisibility(
                    visible = it.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Text(text = it)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    chatId: String,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onIgnoreClick: () -> Unit,
    isChatIgnored: Boolean
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Чат",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isChatIgnored) "Чат игнорируется" else "Активен",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
        },
        actions = {
            IconButton(
                onClick = onIgnoreClick,
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = if (isChatIgnored)
                        Icons.Default.Notifications
                    else
                        Icons.Default.NotificationsOff,
                    contentDescription = if (isChatIgnored)
                        "Включить уведомления"
                    else "Игнорировать чат"
                )
            }
        }
    )
}

@Composable
fun MessageBubble(
    message: com.example.mobile_final.dto.ChatMessage,
    isOwnMessage: Boolean,
    modifier: Modifier = Modifier
) {
    val alignment = if (isOwnMessage) Alignment.End else Alignment.Start
    val bubbleColor = if (isOwnMessage)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isOwnMessage)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = alignment as Alignment
    ) {
        Column(
            horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(1.dp, RoundedCornerShape(12.dp))
                .background(bubbleColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatMessageTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = textColor.copy(alpha = 0.7f)
                    ),
                    fontSize = 10.sp
                )

                if (isOwnMessage) {
                    Icon(
                        imageVector = when (message.status) {
                            com.example.mobile_final.dto.MessageStatus.SENT ->
                                Icons.Default.Done
                            com.example.mobile_final.dto.MessageStatus.DELIVERED ->
                                Icons.Default.DoneAll
                            com.example.mobile_final.dto.MessageStatus.READ ->
                                Icons.Default.DoneAll
                        },
                        contentDescription = null,
                        tint = when (message.status) {
                            com.example.mobile_final.dto.MessageStatus.SENT ->
                                textColor.copy(alpha = 0.5f)
                            com.example.mobile_final.dto.MessageStatus.DELIVERED ->
                                textColor.copy(alpha = 0.7f)
                            com.example.mobile_final.dto.MessageStatus.READ ->
                                MaterialTheme.colorScheme.tertiary
                        },
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputField(
    onSendMessage: (String) -> Unit,
    enabled: Boolean = true,

) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = "Введите сообщение...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (text.isNotBlank()) {
                            onSendMessage(text)
                            text = ""

                        }
                    }
                ),
                enabled = enabled,
                singleLine = false,
                maxLines = 3
            )

            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSendMessage(text)
                        text = ""

                    }
                },
                enabled = enabled && text.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (text.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Отправить",
                    tint = if (text.isNotBlank())
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ChatListItem(
    chat: com.example.mobile_final.dto.Chat,
    currentUserId: String,
    unreadCount: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val otherUserId = if (chat.user1 == currentUserId) chat.user2 else chat.user1

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (unreadCount > 0) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Аватар пользователя
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = otherUserId.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Информация о чате
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Пользователь $otherUserId",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatChatTime(chat.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Индикатор непрочитанных сообщений
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 10.sp
                    )
                }
            }

            // Индикатор игнорирования
            if (chat.ignoredBy.contains(currentUserId)) {
                Icon(
                    imageVector = Icons.Default.NotificationsOff,
                    contentDescription = "Чат игнорируется",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun formatMessageTime(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp)

        val now = Calendar.getInstance()
        val messageDate = Calendar.getInstance().apply { time = date }

        val today = now.get(Calendar.DATE) == messageDate.get(Calendar.DATE)
        val yesterday = now.get(Calendar.DATE) - 1 == messageDate.get(Calendar.DATE)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        timeFormat.timeZone = TimeZone.getDefault()

        when {
            today -> timeFormat.format(date)
            yesterday -> "Вчера, ${timeFormat.format(date)}"
            else -> {
                val dateFormat = SimpleDateFormat("dd.MM, HH:mm", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getDefault()
                dateFormat.format(date)
            }
        }
    } catch (e: Exception) {
        timestamp
    }
}

fun formatChatTime(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp)

        val now = Calendar.getInstance()
        val chatDate = Calendar.getInstance().apply { time = date }

        val daysDiff = (now.timeInMillis - chatDate.timeInMillis) / (1000 * 60 * 60 * 24)

        when {
            daysDiff == 0L -> {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                timeFormat.timeZone = TimeZone.getDefault()
                timeFormat.format(date)
            }
            daysDiff == 1L -> "Вчера"
            daysDiff < 7 -> {
                val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                dayFormat.timeZone = TimeZone.getDefault()
                dayFormat.format(date)
            }
            else -> {
                val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getDefault()
                dateFormat.format(date)
            }
        }
    } catch (e: Exception) {
        timestamp
    }
}