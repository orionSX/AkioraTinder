package com.example.mobile_final.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.mobile_final.R
import com.example.mobile_final.ui.theme.Mobile_finalTheme
import com.example.mobile_final.utils.PreferencesManager

class SettingsActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)

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
                            recreate()
                        },
                        onLanguageChanged = { language ->
                            preferencesManager.setLanguage(language)
                            recreate()
                        },
                        onBackClick = {
                            finish()
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun SettingsScreen(
        currentTheme: String,
        currentLanguage: String,
        onThemeChanged: (String) -> Unit,
        onLanguageChanged: (String) -> Unit,
        onBackClick: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
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

            // Theme selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.switch_theme),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(modifier = Modifier.selectableGroup()) {
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Language selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.switch_lang),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(modifier = Modifier.selectableGroup()) {
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
    fun RadioOption(
        text: String,
        selected: Boolean,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = onClick
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null // null because we handle click on the entire row
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Add these string resources to strings.xml
val systemDefaultString = "System Default"
val lightThemeString = "Light Theme"
val darkThemeString = "Dark Theme"