package com.example.akioratinder.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.akioratinder.data.PlayerProfile
import com.example.akioratinder.services.ApiService
import com.example.akioratinder.services.AuthManager
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

class LikesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AkioraTinderTheme {
                Scaffold(
                    topBar = {
                        LikesTopBar(onBackClick = { onBackPressed() })
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        LikesScreen()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikesTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Likes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

@Composable
fun LikesScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { ApiService.getInstance(context) }
    val authManager = remember { AuthManager.getInstance(context) }

    var likedProfiles by remember { mutableStateOf<List<PlayerProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val currentUser by authManager.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val allProfiles = apiService.getForms()
                // Находим профили, которые лайкнул текущий пользователь
                likedProfiles = allProfiles.filter { profile ->
                    profile.likedBy.contains(currentUser?.id)
                }
                isLoading = false
            } catch (e: Exception) {
                // Обработка ошибки
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LikesHeader(likesCount = likedProfiles.size)

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else if (likedProfiles.isEmpty()) {
            EmptyLikesState()
        } else {
            LikesList(
                likedProfiles = likedProfiles,
                onRemoveLike = { profile ->
                    coroutineScope.launch {
                        try {
                            apiService.dislikeForm(profile.id)
                            // Обновляем список
                            val allProfiles = apiService.getForms()
                            likedProfiles = allProfiles.filter {
                                it.likedBy.contains(currentUser?.id)
                            }
                        } catch (e: Exception) {
                            // Обработка ошибки
                        }
                    }
                },
                onChatClick = { profile ->
                    // TODO: Создать или получить чат с пользователем
                    // Пока просто открываем ChatActivity с ID профиля
                    val intent = Intent(context, ChatActivity::class.java).apply {
                        putExtra("target_user_name", profile.userData.name)
                        // Нужно получить chatId из API
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun LikesHeader(likesCount: Int) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(48.dp)
                .scale(pulseScale)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Liked Profiles",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$likesCount ${if (likesCount == 1) "profile" else "profiles"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun EmptyLikesState() {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha)
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No likes yet",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Swipe right on profiles you like",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun LikesList(
    likedProfiles: List<PlayerProfile>,
    onRemoveLike: (PlayerProfile) -> Unit,
    onChatClick: (PlayerProfile) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(likedProfiles, key = { it.id }) { profile ->
            AnimatedLikedProfileCard(
                profile = profile,
                onRemoveClick = { onRemoveLike(profile) },
                onChatClick = { onChatClick(profile) }
            )
        }
    }
}

@Composable
fun AnimatedLikedProfileCard(
    profile: PlayerProfile,
    onRemoveClick: () -> Unit,
    onChatClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 200)
    )
    val translateY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 50f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .offset(y = translateY.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProfileCardHeader(
                profile = profile,
                onRemoveClick = onRemoveClick,
                onChatClick = onChatClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileCardContent(profile = profile)
        }
    }
}

@Composable
fun ProfileCardHeader(
    profile: PlayerProfile,
    onRemoveClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.userData.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = profile.account.server,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onChatClick) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Start chat",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove like",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ProfileCardContent(profile: PlayerProfile) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProfileInfoItem(
            label = "Server",
            value = profile.account.server,
            icon = Icons.Default.Public
        )

        ProfileInfoItem(
            label = "Roles",
            value = profile.gameData.roles.take(2).joinToString(", ") { it.name },
            icon = Icons.Default.SportsEsports
        )

        ProfileInfoItem(
            label = "Looking for",
            value = profile.gameData.rolesLookingFor.take(2).joinToString(", ") { it.name },
            icon = Icons.Default.Group
        )
    }

    if (profile.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = profile.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            maxLines = 2
        )
    }
}

@Composable
fun ProfileInfoItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}