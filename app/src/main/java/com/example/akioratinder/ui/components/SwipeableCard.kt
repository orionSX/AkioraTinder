package com.example.akioratinder.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.akioratinder.R
import com.example.akioratinder.data.UserProfile
import com.example.akioratinder.services.LikesManager
import com.example.akioratinder.services.ProfilesManager
import com.example.akioratinder.services.SwipeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun SwipeableCardStack(
    profiles: List<UserProfile>,
    onSwipeLeft: (UserProfile) -> Unit = {},
    onSwipeRight: (UserProfile) -> Unit = {}
) {
    var currentIndex by remember { mutableStateOf(0) }

    if (currentIndex >= profiles.size) {
        EmptyProfilesState()
        return
    }


    val visibleCards = profiles.drop(currentIndex).take(4)

    Box(modifier = Modifier.fillMaxSize()) {
        visibleCards.forEachIndexed { idx, profile ->
            val isTopCard = idx == 0

            SwipeableCard(
                profile = profile,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 70.dp)
                    .zIndex((visibleCards.size - idx).toFloat()),
                isTopCard = isTopCard,
                onSwipeLeft = {
                    onSwipeLeft(profile)
                    currentIndex++
                },
                onSwipeRight = {
                    onSwipeRight(profile)
                    currentIndex++
                }
            )
        }
    }
}

@Composable
fun EmptyProfilesState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_more_profiles),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Check back later for new profiles!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SwipeableCard(
    profile: UserProfile,
    modifier: Modifier = Modifier,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    isTopCard: Boolean = true
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val cardWidth = LocalContext.current.resources.displayMetrics.widthPixels.toFloat()
    val swipeThreshold = cardWidth * 0.25f


    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "offsetX"
    )

    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "offsetY"
    )

    val rotation = (animatedOffsetX / 15f).coerceIn(-12f, 12f)
    val alpha = 1f - (abs(animatedOffsetX) / cardWidth * 0.3f).coerceIn(0f, 1f)


    val swipeIconScale by animateFloatAsState(
        targetValue = if (abs(animatedOffsetX) > 100f) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipeIconScale"
    )

    val swipeIconAlpha by animateFloatAsState(
        targetValue = if (abs(animatedOffsetX) > 50f) (abs(animatedOffsetX) / swipeThreshold * 0.8f).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "swipeIconAlpha"
    )

    Card(
        modifier = modifier
            .offset(x = animatedOffsetX.dp, y = animatedOffsetY.dp)
            .rotate(rotation)
            .alpha(alpha)
            .then(
                if (isTopCard) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y * 0.3f
                            },
                            onDragEnd = {
                                isDragging = false
                                if (abs(offsetX) > swipeThreshold) {
                                    val direction = if (offsetX > 0) 1 else -1
                                    scope.launch {
                                        // Плавная анимация ухода
                                        val targetOffset = direction * cardWidth * 1.5f
                                        offsetX = targetOffset
                                        delay(300)

                                        if (direction > 0) {
                                            onSwipeRight()
                                        } else {
                                            onSwipeLeft()
                                        }


                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                } else {

                                    scope.launch {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            }
                        )
                    }
                } else Modifier
            ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isTopCard) 12.dp else 4.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
            )


            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .align(Alignment.TopStart)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Column {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = profile.summonerName,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(4.dp))


                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = getFullRank(profile),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }


                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = profile.server,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))


                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = profile.role,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }


                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Био
                    if (profile.bio.isNotBlank()) {
                        Column {
                            Text(
                                text = stringResource(R.string.bio).uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = profile.bio,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                        }
                    }


                    if (profile.playStyle.isNotBlank() || profile.microphone.isNotBlank() || profile.goals.isNotBlank()) {
                        Column {
                            Text(
                                text = "Preferences".uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (profile.playStyle.isNotBlank()) {
                                    InfoChip(
                                        text = profile.playStyle,
                                        icon = Icons.Default.Person
                                    )
                                }
                                if (profile.microphone.isNotBlank()) {
                                    InfoChip(
                                        text = profile.microphone,
                                        icon = Icons.Default.Mic
                                    )
                                }
                                if (profile.goals.isNotBlank()) {
                                    InfoChip(
                                        text = profile.goals,
                                        icon = Icons.Default.Flag
                                    )
                                }
                                if (profile.playSchedule.isNotBlank()) {
                                    InfoChip(
                                        text = profile.playSchedule,
                                        icon = Icons.Default.Schedule
                                    )
                                }
                            }
                        }
                    }
                }


                if (isTopCard) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SwipeIndicator(
                            text = "Nope",
                            icon = Icons.Default.Close,
                            color = Color(0xFFF44336),
                            isVisible = animatedOffsetX < -50f,
                            scale = swipeIconScale,
                            alpha = if (animatedOffsetX < 0) swipeIconAlpha else 0f
                        )

                        SwipeIndicator(
                            text = "Like",
                            icon = Icons.Default.Favorite,
                            color = Color(0xFF4CAF50),
                            isVisible = animatedOffsetX > 50f,
                            scale = swipeIconScale,
                            alpha = if (animatedOffsetX > 0) swipeIconAlpha else 0f
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getFullRank(profile: UserProfile): String {
    return if (profile.rankTier in listOf("Master", "Grandmaster", "Challenger")) {
        profile.rankTier
    } else if (profile.rankDivision.isNotBlank()) {
        "${profile.rankTier} ${profile.rankDivision}"
    } else {
        profile.rankTier
    }
}

@Composable
private fun InfoChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SwipeIndicator(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isVisible: Boolean,
    scale: Float,
    alpha: Float
) {
    if (isVisible) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                color = color,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .size(60.dp)
                    .scale(scale)
                    .alpha(alpha)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(alpha)
            )
        }
    }
}
