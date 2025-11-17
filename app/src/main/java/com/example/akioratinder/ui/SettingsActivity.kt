package com.example.akioratinder.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import com.example.akioratinder.storage.ThemeLanguageStore
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.akioratinder.localization.LocaleHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import androidx.lifecycle.lifecycleScope
import com.example.akioratinder.R

class SettingsActivity : ComponentActivity() {
    private lateinit var store: ThemeLanguageStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = ThemeLanguageStore(this)

        setContent {
            val darkTheme by store.darkThemeFlow.collectAsState(initial = false)
            AkioraTinderTheme(darkTheme = darkTheme) {
                Scaffold(
                    topBar = { TopBar() },
                    bottomBar = { BottomNav(2) }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        SettingsScreen(
                            store = store,
                            onLangChanged = { newLang ->
                                // Сохраняем язык в DataStore в корутине
                                lifecycleScope.launch {
                                    store.setLang(newLang)
                                    // Применяем локаль к текущему контексту и пересоздаём Activity
                                    val ctx = LocaleHelper.setLocale(this@SettingsActivity, newLang)
                                    // важно: в некоторых случаях нужно перезапустить приложение; чаще достаточно recreate()
                                    recreate()
                                }
                            }
                        )
                    }
                }

            }
        }
    }

    // attachBaseContext — чтобы при старте Activity применялась нужная локаль
    override fun attachBaseContext(newBase: Context) {
        val s = ThemeLanguageStore(newBase)
        val lang = runBlocking { s.langFlow.first() } // runBlocking в attachBaseContext — нормально здесь
        val ctx = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(ctx)
    }
}

@Composable
fun SettingsScreen(store: ThemeLanguageStore, onLangChanged: (String)->Unit) {
    val scope = rememberCoroutineScope()
    val dark by store.darkThemeFlow.collectAsState(initial = false)
    val lang by store.langFlow.collectAsState(initial = "ru")

    Column(modifier = Modifier.padding(20.dp)) {
        Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        Button(onClick = { scope.launch { store.toggleTheme(!dark) } }) {
            Text(stringResource(R.string.switch_theme))
        }

        Spacer(Modifier.height(20.dp))

        Button(onClick = {
            val newLang = if (lang == "ru") "en" else "ru"

            onLangChanged(newLang)
        }) {
            Text(stringResource(R.string.switch_lang))
        }
    }
}
