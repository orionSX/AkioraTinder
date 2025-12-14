package com.example.mobile_final.ui

import android.content.Intent
import android.os.Bundle
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
import com.example.mobile_final.MainActivity
import com.example.mobile_final.R
import com.example.mobile_final.dto.Gender
import com.example.mobile_final.dto.UpdateProfileRequest
import com.example.mobile_final.dto.UserProfile
import com.example.mobile_final.services.ApiService
import com.example.mobile_final.services.AuthManager
import com.example.mobile_final.storage.UserStore
import com.example.mobile_final.ui.theme.Mobile_finalTheme
import com.example.mobile_final.utils.LocaleHelper
import com.example.mobile_final.utils.PreferencesManager
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    private fun restartApplication() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferencesManager = PreferencesManager(this)
        val language = preferencesManager.getLanguage()
        val context = LocaleHelper.setLocale(this, language)

        val apiService = ApiService.getInstance(this)
        val userStore = UserStore(this)
        val authManager = AuthManager.getInstance(this)

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
                        preferencesManager = preferencesManager,
                        onBackClick = {
                            finish()
                            restartApplication()
                        },
                        apiService = apiService,
                        userStore = userStore,
                        authManager = authManager
                    )
                }
            }
        }
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        val preferencesManager = PreferencesManager(newBase)
        val language = preferencesManager.getLanguage()
        val context = LocaleHelper.setLocale(newBase, language)
        super.attachBaseContext(context)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    onBackClick: () -> Unit,
    apiService: ApiService,
    userStore: UserStore,
    authManager: AuthManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf<UserProfile?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Получаем текущие настройки
    val currentTheme by produceState(initialValue = preferencesManager.getThemeMode()) {
        value = preferencesManager.getThemeMode()
    }

    val currentLanguage by produceState(initialValue = preferencesManager.getLanguage()) {
        value = preferencesManager.getLanguage()
    }

    // Load current user data
    LaunchedEffect(Unit) {
        currentUser = userStore.getUserData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                // Profile section
                ProfileSection(
                    user = currentUser,
                    onEditProfile = { showEditDialog = true }
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                            onClick = {
                                preferencesManager.setThemeMode("system")
                                (context as? android.app.Activity)?.recreate()
                            }
                        )
                        RadioOption(
                            text = stringResource(R.string.light_theme),
                            selected = currentTheme == "light",
                            onClick = {
                                preferencesManager.setThemeMode("light")
                                (context as? android.app.Activity)?.recreate()
                            }
                        )
                        RadioOption(
                            text = stringResource(R.string.dark_theme),
                            selected = currentTheme == "dark",
                            onClick = {
                                preferencesManager.setThemeMode("dark")
                                (context as? android.app.Activity)?.recreate()
                            }
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
                            onClick = {
                                preferencesManager.setLanguage("en")
                                // Перезапускаем Activity для применения языка
                                (context as? android.app.Activity)?.let {
                                    it.recreate()
                                }
                            }
                        )
                        RadioOption(
                            text = "Русский",
                            selected = currentLanguage == "ru",
                            onClick = {
                                preferencesManager.setLanguage("ru")
                                // Перезапускаем Activity для применения языка
                                (context as? android.app.Activity)?.let {
                                    it.recreate()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog && currentUser != null) {
        EditProfileDialog(
            user = currentUser!!,
            onSave = { updatedUser ->
                scope.launch {
                    val result = apiService.updateUser(currentUser?.id!!, updatedUser)
                    if (result.id.isNotEmpty()) {
                        userStore.saveUserData(result)
                        snackbarHostState.showSnackbar(context.getString(R.string.update_success))
                        currentUser = result
                    } else {
                        snackbarHostState.showSnackbar(context.getString(R.string.update_error))
                    }
                }
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun ProfileSection(
    user: UserProfile?,
    onEditProfile: () -> Unit  // Changed: removed @Composable
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
                    onClick = onEditProfile
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingItem(
                    icon = Icons.Default.Cake,
                    title = stringResource(R.string.age),
                    description = userData.age?.toString() ?: stringResource(R.string.not_specified),
                    onClick = onEditProfile
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
                    onClick = onEditProfile
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingItem(
                    icon = Icons.Default.Chat,
                    title = stringResource(R.string.discord),
                    description = userData.discord ?: stringResource(R.string.not_specified),
                    onClick = onEditProfile
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
    onClick: () -> Unit  // Changed: removed @Composable
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)  // Fixed: use proper syntax
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
            .clickable(onClick = onClick)
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
    onSave: (UpdateProfileRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var ageText by remember { mutableStateOf(user.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(user.gender) }
    var discord by remember { mutableStateOf(user.discord ?: "") }
    var genderExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_profile)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    placeholder = { Text(stringResource(R.string.enter_name)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

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

                // Gender dropdown
                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = !genderExpanded }
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
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.male)) },
                            onClick = {
                                gender = Gender.MALE
                                genderExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.female)) },
                            onClick = {
                                gender = Gender.FEMALE
                                genderExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.other)) },
                            onClick = {
                                gender = Gender.ANY
                                genderExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.not_specified)) },
                            onClick = {
                                gender = null
                                genderExpanded = false
                            }
                        )
                    }
                }

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
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.save_changes))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

fun UserProfile.toUpdateRequest(): UpdateProfileRequest {
    return UpdateProfileRequest(
        name = this.name,
        age = this.age,
        gender = this.gender,
        discord = this.discord
    )
}