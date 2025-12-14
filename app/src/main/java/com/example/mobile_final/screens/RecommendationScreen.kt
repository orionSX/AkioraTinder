package com.example.mobile_final.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Message
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(profiles) { profile ->
                        ProfileCardWithActions(
                            profile = profile,
                            onLike = {
                                if (profile.formTest != null && profile.formTest.questions.isNotEmpty()) {
                                    onNavigateToTest(profile.id)
                                } else {
                                    onLike(profile.id)
                                }
                            },
                            onDislike = { onDislike(profile.id) },
                            onNavigateToChat = { onNavigateToChat(profile.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileCardWithActions(
    profile: PlayerProfile,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Профиль пользователя
            ProfileCardContent(profile = profile)

            // Кнопки действий
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Кнопка "Не подходит"
                OutlinedButton(
                    onClick = onDislike,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dislike",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Не подходит")
                }

                // Кнопка "Написать"
                OutlinedButton(
                    onClick = onNavigateToChat,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "Chat",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Написать")
                }

                // Кнопка "Подходит"
                Button(
                    onClick = onLike,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
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

@Composable
fun ProfileCardContent(profile: PlayerProfile) {
    Column(
        modifier = Modifier.fillMaxWidth()
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
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Roles
        if (profile.gameData.roles.isNotEmpty()) {
            Text(
                text = "Роли:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            ProfileFlowRow(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                profile.gameData.roles.forEach { role ->
                    AssistChip(
                        onClick = { },
                        label = { Text(role.name) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Looking for roles
        if (profile.gameData.rolesLookingFor.isNotEmpty()) {
            Text(
                text = "Ищет:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            ProfileFlowRow(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                profile.gameData.rolesLookingFor.forEach { role ->
                    AssistChip(
                        onClick = { },
                        label = { Text(role.name) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Game types
        if (profile.gameData.gameTypes.isNotEmpty()) {
            Text(
                text = "Типы игр:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            ProfileFlowRow(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                profile.gameData.gameTypes.forEach { gameType ->
                    AssistChip(
                        onClick = { },
                        label = { Text(gameType.name) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Personal data
        val genderText = profile.personData.gender?.name ?: "Любой"
        val ageRange = if (profile.personData.minAge != null || profile.personData.maxAge != null) {
            val minAge = profile.personData.minAge?.toString() ?: "N/A"
            val maxAge = profile.personData.maxAge?.toString() ?: "N/A"
            "$minAge-$maxAge лет"
        } else {
            "Не указан"
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
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

// Исправленная версия FlowRow
@Composable
fun ProfileFlowRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.Top,
        maxItemsInEachRow = 3
    ) {
        content()
    }
}