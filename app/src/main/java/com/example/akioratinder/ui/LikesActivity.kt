
package com.example.akioratinder.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.akioratinder.R
import com.example.akioratinder.data.LikedProfile
import com.example.akioratinder.services.ChatManager
import com.example.akioratinder.services.GlobalSettingsManager
import com.example.akioratinder.services.LikesManager
import com.example.akioratinder.storage.ThemeLanguageStore
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import com.example.akioratinder.viewmodels.LikesViewModel
import com.example.akioratinder.viewmodels.ThemeViewModel
import java.text.SimpleDateFormat
import java.util.*

class LikesActivity : ComponentActivity() {
    private lateinit var themeStore: ThemeLanguageStore

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(themeStore)
    }

    private val likesViewModel: LikesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        GlobalSettingsManager.initialize(this)
        LikesManager.initialize(this)
        ChatManager.initialize(this)
        themeStore = ThemeLanguageStore(this)

        super.onCreate(savedInstanceState)

        setContent {
            val darkTheme by themeViewModel.darkTheme.collectAsState()
            GlobalSettingsManager.ObserveSettings()

            AkioraTinderTheme(darkTheme = darkTheme) {
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
                        LikesScreen(likesViewModel = likesViewModel)
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
fun LikesScreen(likesViewModel: LikesViewModel) {
    val context = LocalContext.current
    val likedProfiles by likesViewModel.getLikedProfilesFlow(context).collectAsState()
    val likesCount = likesViewModel.getLikesCount(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LikesHeader(likesCount = likesCount)

        Spacer(modifier = Modifier.height(16.dp))

        if (likedProfiles.isEmpty()) {
            EmptyLikesState()
        } else {
            LikesList(
                likedProfiles = likedProfiles,
                onRemoveLike = { profile ->
                    likesViewModel.removeLike(context, profile.userProfile)
                },
                onChatClick = { profile ->
                    val intent = Intent(context, ChatActivity::class.java).apply {
                        putExtra("target_user", profile.userProfile.summonerName)
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
            text = stringResource(R.string.liked_profiles),
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
                text = stringResource(R.string.no_likes_yet),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.no_likes_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun LikesList(
    likedProfiles: List<LikedProfile>,
    onRemoveLike: (LikedProfile) -> Unit,
    onChatClick: (LikedProfile) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(likedProfiles, key = { it.userProfile.summonerName }) { likedProfile ->
            AnimatedLikedProfileCard(
                likedProfile = likedProfile,
                onRemoveClick = { onRemoveLike(likedProfile) },
                onChatClick = { onChatClick(likedProfile) }
            )
        }
    }
}

@Composable
fun AnimatedLikedProfileCard(
    likedProfile: LikedProfile,
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
                likedProfile = likedProfile,
                onRemoveClick = onRemoveClick,
                onChatClick = onChatClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileCardContent(likedProfile = likedProfile)
        }
    }
}

@Composable
fun ProfileCardHeader(
    likedProfile: LikedProfile,
    onRemoveClick: () -> Unit,
    onChatClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = likedProfile.userProfile.summonerName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Liked on ${dateFormat.format(likedProfile.likedAt)}",
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
fun ProfileCardContent(likedProfile: LikedProfile) {
    val fullRank = if (likedProfile.userProfile.rankTier in listOf("Master", "Grandmaster", "Challenger")) {
        likedProfile.userProfile.rankTier
    } else {
        "${likedProfile.userProfile.rankTier} ${likedProfile.userProfile.rankDivision}"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProfileInfoItem(
            label = "Server",
            value = likedProfile.userProfile.server,
            icon = Icons.Default.Public
        )

        ProfileInfoItem(
            label = "Role",
            value = likedProfile.userProfile.role,
            icon = Icons.Default.SportsEsports
        )

        ProfileInfoItem(
            label = "Rank",
            value = fullRank,
            icon = Icons.Default.Leaderboard
        )
    }

    if (likedProfile.userProfile.bio.isNotBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = likedProfile.userProfile.bio,
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
        Text(text = value, style = MaterialTheme.typography.bodySmall)
    }
}
