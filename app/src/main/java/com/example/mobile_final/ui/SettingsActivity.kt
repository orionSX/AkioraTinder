package com.example.mobile_final.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobile_final.R
import com.example.mobile_final.dto.Gender
import com.example.mobile_final.dto.UpdateProfileRequest
import com.example.mobile_final.dto.UserProfile
import com.example.mobile_final.services.ApiService
import com.example.mobile_final.services.AuthManager
import com.example.mobile_final.storage.UserStore
import com.example.mobile_final.ui.theme.Mobile_finalTheme
import com.example.mobile_final.utils.PreferencesManager
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var apiService: ApiService
    private lateinit var userStore: UserStore
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)
        apiService = ApiService.getInstance(this)
        userStore = UserStore(this)
        authManager = AuthManager.getInstance(this)

        setContent {
            Mobile_finalTheme(
                darkTheme = when (preferencesManager.getThemeMode()) {
                    "light" -> false
                    "dark" -> true
                    else -> isSystemInDarkTheme()
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(
                        currentTheme = preferencesManager.getThemeMode(),
                        currentLanguage = preferencesManager.getLanguage(),
                        onThemeChanged = { theme ->
                            preferencesManager.setThemeMode(theme)
                            // Instead of recreating, update the theme immediately
                            recreate()
                        },
                        onLanguageChanged = { language ->
                            preferencesManager.setLanguage(language)
                            // Instead of recreating, update the language immediately
                            recreate()
                        },
                        onBackClick = {
                            finish()
                        },
                        apiService = apiService,
                        userStore = userStore,
                        authManager = authManager
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsScreen(
        currentTheme: String,
        currentLanguage: String,
        onThemeChanged: (String) -> Unit,
        onLanguageChanged: (String) -> Unit,
        onBackClick: () -> Unit,
        apiService: ApiService,
        userStore: UserStore,
        authManager: AuthManager
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var currentUser by remember { mutableStateOf<UserProfile?>(null) }
        
        // Load current user data
        LaunchedEffect(Unit) {
            currentUser = userStore.getUserData()
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                // Top app bar with back button
                TopAppBar(
                    title = { Text(stringResource(R.string.settings)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Profile section
                ProfileSection(
                    user = currentUser,
                    onEditProfile = { user ->
                        // Show edit dialog for profile
                        EditProfileDialog(
                            user = user,
                            onSave = { updatedUser ->
                                scope.launch {
                                    val result = apiService.updateUser(updatedUser.id, updatedUser.toUpdateRequest())
                                    if (result.id.isNotEmpty()) {
                                        userStore.saveUserData(result)
                                        Toast.makeText(context, stringResource(R.string.update_success), Toast.LENGTH_SHORT).show()
                                        currentUser = result
                                    } else {
                                        Toast.makeText(context, stringResource(R.string.update_error), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Appearance settings card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.appearance),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Theme selection
                        SettingItem(
                            icon = Icons.Default.NightsStay,
                            title = stringResource(R.string.switch_theme),
                            description = when (currentTheme) {
                                "light" -> stringResource(R.string.light_theme)
                                "dark" -> stringResource(R.string.dark_theme)
                                else -> stringResource(R.string.system_default)
                            },
                            onClick = {
                                // Theme selection dialog would go here
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Language selection
                        SettingItem(
                            icon = Icons.Default.Language,
                            title = stringResource(R.string.language),
                            description = when (currentLanguage) {
                                "en" -> "English"
                                "ru" -> "Русский"
                                else -> "English"
                            },
                            onClick = {
                                // Language selection dialog would go here
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Theme selection expanded
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.switch_theme),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        RadioOption(
                            text = stringResource(R.string.system_default),
                            selected = currentTheme == "system",
                            onClick = { onThemeChanged("system") }
                        )
                        RadioOption(
                            text = stringResource(R.string.light_theme),
                            selected = currentTheme == "light",
                            onClick = { onThemeChanged("light") }
                        )
                        RadioOption(
                            text = stringResource(R.string.dark_theme),
                            selected = currentTheme == "dark",
                            onClick = { onThemeChanged("dark") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Language selection expanded
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.switch_lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        RadioOption(
                            text = "English",
                            selected = currentLanguage == "en",
                            onClick = { onLanguageChanged("en") }
                        )
                        RadioOption(
                            text = "Русский",
                            selected = currentLanguage == "ru",
                            onClick = { onLanguageChanged("ru") }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ProfileSection(
        user: UserProfile?,
        onEditProfile: (UserProfile) -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_settings),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                user?.let { userData ->
                    SettingItem(
                        icon = Icons.Default.Person,
                        title = userData.name.takeIf { it.isNotBlank() } ?: stringResource(R.string.enter_name),
                        description = userData.email,
                        onClick = { onEditProfile(userData) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingItem(
                        icon = Icons.Default.Cake,
                        title = stringResource(R.string.age),
                        description = userData.age?.toString() ?: stringResource(R.string.not_specified),
                        onClick = { onEditProfile(userData) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingItem(
                        icon = Icons.Default.Face,
                        title = stringResource(R.string.gender),
                        description = when (userData.gender) {
                            Gender.MALE -> stringResource(R.string.male)
                            Gender.FEMALE -> stringResource(R.string.female)
                            Gender.ANY -> stringResource(R.string.other)
                            else -> stringResource(R.string.not_specified)
                        },
                        onClick = { onEditProfile(userData) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingItem(
                        icon = Icons.Default.Chat,
                        title = stringResource(R.string.discord),
                        description = userData.discord ?: stringResource(R.string.not_specified),
                        onClick = { onEditProfile(userData) }
                    )
                } ?: run {
                    Text(
                        text = stringResource(R.string.loading),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    @Composable
    fun SettingItem(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        title: String,
        description: String,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClick() }
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    @Composable
    fun RadioOption(
        text: String,
        selected: Boolean,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { onClick() }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EditProfileDialog(
        user: UserProfile,
        onSave: (UpdateProfileRequest) -> Unit
    ) {
        var showDialog by remember { mutableStateOf(true) }
        var name by remember { mutableStateOf(user.name) }
        var ageText by remember { mutableStateOf(user.age?.toString() ?: "") }
        var gender by remember { mutableStateOf(user.gender) }
        var discord by remember { mutableStateOf(user.discord ?: "") }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(R.string.edit_profile)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.name)) },
                            placeholder = { Text(stringResource(R.string.enter_name)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { 
                                // Only allow numeric input
                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                    ageText = it 
                                }
                            },
                            label = { Text(stringResource(R.string.age)) },
                            placeholder = { Text(stringResource(R.string.enter_age)) },
                            leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = {}
                        ) {
                            OutlinedTextField(
                                value = when (gender) {
                                    Gender.MALE -> stringResource(R.string.male)
                                    Gender.FEMALE -> stringResource(R.string.female)
                                    Gender.ANY -> stringResource(R.string.other)
                                    else -> stringResource(R.string.not_specified)
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.gender)) },
                                leadingIcon = { Icon(Icons.Default.Face, contentDescription = null) },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            
                            ExposedDropdownMenu(
                                expanded = false,
                                onDismissRequest = { }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.male)) },
                                    onClick = { gender = Gender.MALE }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.female)) },
                                    onClick = { gender = Gender.FEMALE }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.other)) },
                                    onClick = { gender = Gender.ANY }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.not_specified)) },
                                    onClick = { gender = null }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = discord,
                            onValueChange = { discord = it },
                            label = { Text(stringResource(R.string.discord)) },
                            placeholder = { Text(stringResource(R.string.enter_discord)) },
                            leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val age = if (ageText.isNotEmpty()) ageText.toIntOrNull() else null
                            val updateRequest = UpdateProfileRequest(
                                name = name,
                                age = age,
                                gender = gender,
                                discord = if (discord.isNotEmpty()) discord else null
                            )
                            onSave(updateRequest)
                            showDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.save_changes))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialog = false }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

fun UserProfile.toUpdateRequest(): UpdateProfileRequest {
    return UpdateProfileRequest(
        name = this.name,
        age = this.age,
        gender = this.gender,
        discord = this.discord
    )
}