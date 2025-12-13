# Настройка URL бэкенда

В этом проекте реализована гибкая система настройки URL бэкенда, которая позволяет легко переключаться между различными серверами (разработка, тестирование, продакшен).

## Как изменить URL бэкенда

1. Откройте файл `/app/src/main/java/com/example/akioratinder/AkioraTinderApplication.kt`
2. Найдите строку с URL бэкенда:
   ```kotlin
   val backendUrl = "https://your-backend-url.com" // Замените на ваш URL
   ```
3. Измените URL на нужный вам:
   ```kotlin
   val backendUrl = "https://your-actual-backend-url.com"
   ```

## Использование в коде

Все сетевые вызовы автоматически используют установленный URL бэкенда через класс `BackendConfig`. Для выполнения сетевых запросов используется класс `ApiService`, который строит URL следующим образом:

```
baseUrl + путь_к_методу_API
```

Например:
- `users/login` -> `https://your-backend-url.com/users/login`
- `forms` -> `https://your-backend-url.com/forms`

## Динамическая смена URL (дополнительно)

Вы также можете изменить URL бэкенда в runtime в любом месте приложения:

```kotlin
import com.example.akioratinder.config.BackendConfig

// В любом месте кода
BackendConfig.setBackendUrl("https://new-backend-url.com")
```

Все последующие API-запросы будут использовать новый URL.

## Примечания

- Убедитесь, что ваш сервер поддерживает CORS для Android-приложения
- Если вы используете HTTP (не HTTPS), убедитесь, что в AndroidManifest.xml разрешен cleartext traffic
- Все API-методы в `APIService.kt` были обновлены в соответствии с OpenAPI спецификацией из файла `back`