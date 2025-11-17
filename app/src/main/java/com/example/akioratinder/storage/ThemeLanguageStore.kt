package com.example.akioratinder.storage

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow

val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeLanguageStore(private val context: Context) {

    companion object {
        val THEME_KEY = booleanPreferencesKey("dark_mode")
        val LANG_KEY = stringPreferencesKey("lang")
    }

    val darkThemeFlow: Flow<Boolean> = context.dataStore.data.map {
        it[THEME_KEY] ?: false
    }

    val langFlow: Flow<String> = context.dataStore.data.map {
        it[LANG_KEY] ?: "ru"
    }

    suspend fun toggleTheme(isDark: Boolean) {
        context.dataStore.edit { it[THEME_KEY] = isDark }
    }

    suspend fun setLang(code: String) {
        context.dataStore.edit { it[LANG_KEY] = code }
    }
}
