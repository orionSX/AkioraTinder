package com.example.akioratinder.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.akioratinder.storage.ThemeLanguageStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(private val store: ThemeLanguageStore) : ViewModel() {
    val darkTheme: StateFlow<Boolean> = store.darkThemeFlow
    val currentLanguage: StateFlow<String> = store.langFlow

    fun toggleTheme() {
        viewModelScope.launch {
            store.toggleTheme()
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            store.setLang(lang)
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val current = store.langFlow.value
            val newLang = if (current == "ru") "en" else "ru"
            store.setLang(newLang)
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            store.setDarkTheme(false)
            store.setLang("ru")
        }
    }
}