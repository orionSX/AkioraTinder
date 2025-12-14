package com.example.mobile_final.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val THEME_KEY = "theme_mode"
        private const val LANGUAGE_KEY = "language"
        private const val DEFAULT_THEME = "system"
        private const val DEFAULT_LANGUAGE = "en"
    }

    // Flow для языка (если нужно отслеживать изменения)
    private val _languageFlow = MutableStateFlow(getLanguage())
    val languageFlow: StateFlow<String> = _languageFlow

    fun getThemeMode(): String {
        return sharedPreferences.getString(THEME_KEY, DEFAULT_THEME) ?: DEFAULT_THEME
    }

    fun setThemeMode(themeMode: String) {
        sharedPreferences.edit().putString(THEME_KEY, themeMode).apply()
    }

    fun getLanguage(): String {
        return sharedPreferences.getString(LANGUAGE_KEY, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun setLanguage(language: String) {
        sharedPreferences.edit().putString(LANGUAGE_KEY, language).apply()
        _languageFlow.value = language
    }
}