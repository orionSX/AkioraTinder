package com.example.akioratinder.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.akioratinder.localization.LocaleHelper
import com.example.akioratinder.services.AuthManager
import com.example.akioratinder.services.GlobalSettingsManager
import com.example.akioratinder.storage.ThemeLanguageStore
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import com.example.akioratinder.R
import com.example.akioratinder.viewmodels.ThemeViewModel
import com.example.akioratinder.viewmodels.ThemeViewModelFactory
import kotlinx.coroutines.runBlocking

class SettingsActivity : ComponentActivity() {
    private lateinit var store: ThemeLanguageStore
    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(store)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Инициализируем хранилище
        store = ThemeLanguageStore(this)
        GlobalSettingsManager.initialize(this)

        // Инициализируем сервисы
        AuthManager.getInstance(this)

        super.onCreate(savedInstanceState)

        setContent {
            val darkTheme by themeViewModel.darkTheme.collectAsState()
            val lang by themeViewModel.currentLanguage.collectAsState()

            GlobalSettingsManager.ObserveSettings()

            AkioraTinderTheme(darkTheme = darkTheme) {
                Scaffold(
                    topBar = { TopBar() },
                    bottomBar = { BottomNav(2) }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        SettingsScreen(
                            themeViewModel = themeViewModel,
                            onLanguageChangeRequest = { newLang ->
                                recreateWithNewLanguage(newLang)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun recreateWithNewLanguage(newLang: String) {
        GlobalSettingsManager.setLanguage(this, newLang)
        recreate()
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = GlobalSettingsManager.getLanguage(newBase)
        val ctx = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(ctx)
    }
}


@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel,
    onLanguageChangeRequest: (String) -> Unit
) {
    val dark by themeViewModel.darkTheme.collectAsState()
    val lang by themeViewModel.currentLanguage.collectAsState()

    Column(modifier = Modifier.padding(20.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        // Настройки темы
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { themeViewModel.toggleTheme() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (dark)
                            "Switch to Light Theme"
                        else
                            "Switch to Dark Theme"
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Настройки языка
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Language",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        val newLang = if (lang == "ru") "en" else "ru"
                        themeViewModel.toggleLanguage()
                        onLanguageChangeRequest(newLang)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Current: ${lang.uppercase()}")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Сброс настроек
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Reset Settings",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        themeViewModel.resetToDefault()
                        onLanguageChangeRequest("ru")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text("Reset to Default")
                }
            }
        }
    }
}