package com.example.mobile_final.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobile_final.dto.PlayerProfile
import com.mecofarid.tinderswipe.SwipeCard
import com.mecofarid.tinderswipe.SwipeCardsBox
import com.mecofarid.tinderswipe.rememberSwipeCardsBoxState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    profiles: List<PlayerProfile>,
    onLike: (String) -> Unit,
    onDislike: (String) -> Unit,
    onNavigateToTest: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeCardsBoxState = rememberSwipeCardsBoxState()
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        if (profiles.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Нет доступных анкет", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* Refresh logic */ }) {
                    Text("Обновить")
                }
            }
        } else {
            SwipeCardsBox(
                state = swipeCardsBoxState,
                modifier = Modifier.fillMaxSize(),
                enableRotation = true,
                rotationAngle = 20f,
                onSwipeEnd = { index ->
                    // Card was swiped away, do nothing here since we handle actions in buttons
                },
                onSwipeCancel = { index ->
                    // Card returned to original position
                }
            ) {
                profiles.forEachIndexed { index, profile ->
                    SwipeCard(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(0.8f),
                        content = {
                            ProfileCard(profile = profile)
                        },
                        onSwipedRight = {
                            // Right swipe - like
                            if (profile.formTest != null && profile.formTest.questions.isNotEmpty()) {
                                // Has test, navigate to test screen
                                onNavigateToTest(profile.id)
                            } else {
                                // No test, just like
                                onLike(profile.id)
                            }
                        },
                        onSwipedLeft = {
                            // Left swipe - dislike
                            onDislike(profile.id)
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            swipeCardsBoxState.swipeLeft()
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dislike",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            swipeCardsBoxState.swipeRight()
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileCard(profile: PlayerProfile) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // User info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${profile.userData.name}, ${profile.userData.age ?: "N/A"}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${profile.account.name}#${profile.account.tag} (${profile.account.server})",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (profile.userData.discord != null) {
                    Text(
                        text = "@${profile.userData.discord}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            if (profile.description.isNotBlank()) {
                Text(
                    text = profile.description,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Roles
            Text(
                text = "Роли:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            FlowRow(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profile.gameData.roles.forEach { role ->
                    AssistChip(
                        onClick = { },
                        label = { Text(role.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Looking for roles
            Text(
                text = "Ищет:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            FlowRow(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profile.gameData.rolesLookingFor.forEach { role ->
                    AssistChip(
                        onClick = { },
                        label = { Text(role.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Game types
            if (profile.gameData.gameTypes.isNotEmpty()) {
                Text(
                    text = "Типы игр:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                FlowRow(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profile.gameData.gameTypes.forEach { gameType ->
                        AssistChip(
                            onClick = { },
                            label = { Text(gameType.name) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Personal data
            val genderText = profile.personData.gender?.name ?: "Любой"
            val ageRange = if (profile.personData.minAge != null || profile.personData.maxAge != null) {
                val minAge = profile.personData.minAge?.toString() ?: "N/A"
                val maxAge = profile.personData.maxAge?.toString() ?: "N/A"
                "$minAge-$maxAge лет"
            } else {
                "Не указан"
            }

            Text(
                text = "Предпочтения: $genderText, $ageRange",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (profile.personData.voice) {
                Text(
                    text = "Голосовой чат: Да",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        maxItemsInEachColumn = 2,
        content = content
    )
}