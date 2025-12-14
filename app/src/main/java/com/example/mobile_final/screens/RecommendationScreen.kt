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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.github.dellisd.cardstack.CardStack
import io.github.dellisd.cardstack.CardStackState
import io.github.dellisd.cardstack.SwipeDirection
import kotlinx.coroutines.delay

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
    var refreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = refreshing)
    
    val cardStackState = remember {
        CardStackState(
            onSwipe = { index, direction ->
                val profileId = profiles[index].id
                when (direction) {
                    SwipeDirection.Left -> onDislike(profileId)
                    SwipeDirection.Right -> {
                        val profile = profiles[index]
                        if (profile.formTest != null && profile.formTest.questions.isNotEmpty()) {
                            onNavigateToTest(profileId)
                        } else {
                            onLike(profileId)
                        }
                    }
                    else -> {}
                }
                true
            }
        )
    }

    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(1500) // Имитация загрузки
            refreshing = false
        }
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { refreshing = true },
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (profiles.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Нет доступных анкет", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { refreshing = true }) {
                        Text("Обновить")
                    }
                }
            } else {
                // Card stack for swipeable profiles
                CardStack(
                    state = cardStackState,
                    content = {
                        profiles.forEachIndexed { index, profile ->
                            ProfileCard(
                                profile = profile,
                                onNavigateToChat = { onNavigateToChat(profile.id) }
                            )
                        }
                    }
                )
                
                // Action buttons overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (profiles.isNotEmpty()) {
                                    onDislike(profiles[cardStackState.currentIndex].id)
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dislike",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Не подходит")
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Button(
                            onClick = {
                                if (profiles.isNotEmpty()) {
                                    val currentProfile = profiles[cardStackState.currentIndex]
                                    if (currentProfile.formTest != null && currentProfile.formTest.questions.isNotEmpty()) {
                                        onNavigateToTest(currentProfile.id)
                                    } else {
                                        onLike(currentProfile.id)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Like",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Подходит")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileCard(profile: PlayerProfile, onNavigateToChat: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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

            // Chat button at the bottom
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onNavigateToChat,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Написать")
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

        content = content as @Composable (FlowRowScope.() -> Unit)
    )
}