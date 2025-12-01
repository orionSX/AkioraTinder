package com.example.akioratinder.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.akioratinder.storage.ThemeLanguageStore
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

    fun setLanguage(newLang: String) {
        viewModelScope.launch {
            store.setLang(newLang)
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val newLang = if (currentLanguage.value == "ru") "en" else "ru"
            setLanguage(newLang)
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            store.setDarkTheme(false)
            store.setLang("ru")
        }
    }
}