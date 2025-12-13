package com.example.akioratinder

import android.app.Application
import com.example.akioratinder.config.BackendConfig

class AkioraTinderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Установите здесь URL вашего бэкенда
        // Пример: BackendConfig.setBackendUrl("https://your-backend-url.com")
        // В продакшене вы можете получать URL из настроек, конфигурационных файлов или ресурсов
        val backendUrl = "https://your-backend-url.com" // Замените на ваш URL
        BackendConfig.setBackendUrl(backendUrl)
    }
}