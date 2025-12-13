package com.example.akioratinder.services

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.akioratinder.storage.ThemeLanguageStore

object GlobalSettingsManager {
    private var themeLanguageStore: ThemeLanguageStore? = null

    fun initialize(context: Context) {
        if (themeLanguageStore == null) {
            themeLanguageStore = ThemeLanguageStore(context)
        }
    }

    private fun getStore(context: Context): ThemeLanguageStore {
        if (themeLanguageStore == null) {
            initialize(context)
        }
        return themeLanguageStore!!
    }

    @Composable
    fun ObserveSettings() {
        val context = LocalContext.current
        val store = remember { getStore(context) }

        val darkTheme by store.darkThemeFlow.collectAsState()
        val language by store.langFlow.collectAsState()

        DisposableEffect(darkTheme, language) {
            // Можно здесь обновлять тему приложения если нужно
            onDispose {}
        }
    }

    fun getDarkTheme(context: Context): Boolean {
        return getStore(context).getDarkTheme()
    }

    fun getLanguage(context: Context): String {
        return getStore(context).getLanguage()
    }

    fun setDarkTheme(context: Context, isDark: Boolean) {
        getStore(context).setDarkTheme(isDark)
    }

    fun setLanguage(context: Context, lang: String) {
        getStore(context).setLang(lang)
    }

    fun toggleTheme(context: Context) {
        getStore(context).toggleTheme()
    }
}