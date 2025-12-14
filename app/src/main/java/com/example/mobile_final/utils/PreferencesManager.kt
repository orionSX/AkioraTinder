package com.example.mobile_final.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val THEME_KEY = "theme_mode"
        private const val LANGUAGE_KEY = "language"
        private const val DEFAULT_THEME = "system" // Options: system, light, dark
        private const val DEFAULT_LANGUAGE = "en" // Options: en, ru
    }

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
    }
}