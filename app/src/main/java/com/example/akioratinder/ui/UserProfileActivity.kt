package com.example.akioratinder.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.akioratinder.data.*
import com.example.akioratinder.services.ApiService
import com.example.akioratinder.services.AuthManager
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

class UserProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AkioraTinderTheme {
                Scaffold(
                    topBar = { TopBar() },
                    bottomBar = { BottomNav(current = 1) }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        UserProfileScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfileScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authManager = remember { AuthManager.getInstance(context) }
    val apiService = remember { ApiService.getInstance(context) }

    val user by authManager.currentUser.collectAsState()
    val playerProfile by authManager.currentPlayerProfile.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var showPlayerForm by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        authManager.loadPlayerProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Информация пользователя
        UserInfoCard(user = user)

        Spacer(modifier = Modifier.height(16.dp))

        // Игровой профиль
        PlayerProfileCard(
            playerProfile = playerProfile,
            onEditClick = { showPlayerForm = true }
        )

        if (showPlayerForm) {
            Spacer(modifier = Modifier.height(16.dp))
            PlayerProfileForm(
                existingProfile = playerProfile,
                onSave = { formData ->
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            if (playerProfile != null) {
                                val update = UpdateFormRequest(
                                    description = formData.description,
                                    account = formData.account,
                                    roles = formData.roles,
                                    rolesLookingFor = formData.rolesLookingFor,
                                    personData = formData.personData,
                                    gameTypes = formData.gameTypes
                                )
                                authManager.updatePlayerProfile(playerProfile!!.id, update)
                            } else {
                                val createRequest = CreateFormRequest(
                                    description = formData.description,
                                    account = formData.account,
                                    roles = formData.roles,
                                    rolesLookingFor = formData.rolesLookingFor,
                                    personData = formData.personData,
                                    creatorId = user?.id ?: "",
                                    gameTypes = formData.gameTypes
                                )
                                authManager.createPlayerProfile(createRequest)
                            }
                            showPlayerForm = false
                        } catch (e: Exception) {
                            error = e.message
                        } finally {
                            isLoading = false
                        }
                    }
                },
                onCancel = { showPlayerForm = false },
                isLoading = isLoading
            )
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun UserInfoCard(user: UserProfile?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "User Information",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (user != null) {
                InfoRow(label = "Name", value = user.name)
                InfoRow(label = "Email", value = user.email)
                user.age?.let { InfoRow(label = "Age", value = it.toString()) }
                user.gender?.let { InfoRow(label = "Gender", value = it.name) }
                user.discord?.let { InfoRow(label = "Discord", value = it) }
            } else {
                Text("No user data available")
            }
        }
    }
}

@Composable
fun PlayerProfileCard(
    playerProfile: PlayerProfile?,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Player Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Button(onClick = onEditClick) {
                    Text(
                        text = if (playerProfile != null) "Edit" else "Create"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (playerProfile != null) {
                if (playerProfile.description.isNotBlank()) {
                    Text(
                        text = playerProfile.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                InfoRow(
                    label = "Account",
                    value = "${playerProfile.account.name}#${playerProfile.account.tag} (${playerProfile.account.server})"
                )
                InfoRow(
                    label = "Roles",
                    value = playerProfile.gameData.roles.joinToString(", ") { it.name }
                )
                InfoRow(
                    label = "Looking for",
                    value = playerProfile.gameData.rolesLookingFor.joinToString(", ") { it.name }
                )
                InfoRow(
                    label = "Game types",
                    value = playerProfile.gameData.gameTypes.joinToString(", ") { it.name }
                )

                playerProfile.personData.minAge?.let { minAge ->
                    playerProfile.personData.maxAge?.let { maxAge ->
                        InfoRow(label = "Age range", value = "$minAge - $maxAge")
                    }
                }

                playerProfile.personData.gender?.let {
                    InfoRow(label = "Preferred gender", value = it.name)
                }

                InfoRow(
                    label = "Voice chat",
                    value = if (playerProfile.personData.voice) "Yes" else "No"
                )
            } else {
                Text(
                    text = "No player profile created yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// Простая форма для игрового профиля (упрощенная версия)
@Composable
fun PlayerProfileForm(
    existingProfile: PlayerProfile?,
    onSave: (CreateFormRequest) -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean
) {
    var description by remember { mutableStateOf(existingProfile?.description ?: "") }
    var accountName by remember { mutableStateOf(existingProfile?.account?.name ?: "") }
    var accountTag by remember { mutableStateOf(existingProfile?.account?.tag ?: "") }
    var accountServer by remember { mutableStateOf(existingProfile?.account?.server ?: "") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (existingProfile != null) "Edit Player Profile" else "Create Player Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Summoner Name") },
                    modifier = Modifier.weight(2f)
                )

                OutlinedTextField(
                    value = accountTag,
                    onValueChange = { accountTag = it },
                    label = { Text("Tag") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = accountServer,
                onValueChange = { accountServer = it },
                label = { Text("Server (EUW, NA, etc.)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        val account = Account(accountName, accountServer, accountTag)
                        // TODO: Добавить остальные поля формы
                        val formData = CreateFormRequest(
                            description = description,
                            account = account,
                            roles = listOf(Role.TOP, Role.JG), // Пример
                            rolesLookingFor = listOf(Role.MID, Role.ADC, Role.SUP), // Пример
                            personData = PersonData(
                                minAge = 18,
                                maxAge = 30,
                                gender = Gender.ANY,
                                voice = true
                            ),
                            creatorId = "", // Будет установлено в Activity
                            gameTypes = listOf(GameType.SOLOQ, GameType.FLEX) // Пример
                        )
                        onSave(formData)
                    },
                    enabled = !isLoading && accountName.isNotBlank() && accountTag.isNotBlank() && accountServer.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Text("Save")
                    }
                }
            }
        }
    }
}