package com.example.mobile_final.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleHelper {
    fun setLocale(context: Context, languageCode: String): Context {
        return updateResources(context, languageCode)
    }

    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)

            return context.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
            configuration.setLayoutDirection(locale)
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        }

        return context
    }

    fun getCurrentLanguage(context: Context): String {
        val preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return preferences.getString("language", "en") ?: "en"
    }
}