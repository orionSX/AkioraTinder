package com.example.akioratinder.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.akioratinder.storage.ThemeLanguageStore

class ThemeViewModelFactory(
    private val store: ThemeLanguageStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            return ThemeViewModel(store) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}