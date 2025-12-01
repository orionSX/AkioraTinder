package com.example.akioratinder.storage

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeLanguageStore(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_LANGUAGE = "language"
        private const val DEFAULT_LANGUAGE = "ru"
    }


    private val _darkTheme = MutableStateFlow(getDarkTheme())
    private val _language = MutableStateFlow(getLanguage())

    val darkThemeFlow: StateFlow<Boolean> = _darkTheme
    val langFlow: StateFlow<String> = _language


    fun getDarkTheme(): Boolean = prefs.getBoolean(KEY_DARK_THEME, false)
    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE


    fun setDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, isDark).apply()
        _darkTheme.value = isDark
    }

    fun setLang(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _language.value = lang
    }

    fun toggleTheme() {
        setDarkTheme(!getDarkTheme())
    }
}