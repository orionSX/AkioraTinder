package com.example.akioratinder.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.akioratinder.services.ApiService
import com.example.akioratinder.services.AuthManager
import com.example.akioratinder.services.GlobalSettingsManager
import com.example.akioratinder.storage.ThemeLanguageStore
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import com.example.akioratinder.R
import com.example.akioratinder.viewmodels.ThemeViewModel
import com.example.akioratinder.viewmodels.ThemeViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

class LoginActivity : ComponentActivity() {
    private lateinit var themeStore: ThemeLanguageStore

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(themeStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализируем сервисы
        GlobalSettingsManager.initialize(this)
        ApiService.getInstance(this)
        AuthManager.getInstance(this)
        themeStore = ThemeLanguageStore(this)

        setContent {
            val darkTheme by themeViewModel.darkTheme.collectAsState()
            GlobalSettingsManager.ObserveSettings()

            AkioraTinderTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onLoginSuccess = {
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        },
                        onRegister = {
                            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegister: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authManager = remember { AuthManager.getInstance(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessToast by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineMedium
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (showSuccessToast) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.login_success),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
                showSuccessToast = false
            },
            label = { Text(stringResource(R.string.email)) },
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
                showSuccessToast = false
            },
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Валидация полей
                if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    errorMessage = context.getString(R.string.err_email_invalid)
                    return@Button
                }

                if (password.isBlank() || password.length < 6) {
                    errorMessage = context.getString(R.string.err_password_short)
                    return@Button
                }

                isLoading = true
                errorMessage = null

                coroutineScope.launch {
                    try {
                        // Вызываем реальную аутентификацию через AuthManager
                        val response = authManager.login(email.trim(), password)

                        if (response.success) {
                            // Успешный вход
                            showSuccessToast = true
                            // Задержка перед переходом на главный экран
                            kotlinx.coroutines.delay(500) // Можно убрать если не нужна задержка
                            onLoginSuccess()
                        } else {
                            // Сервер вернул ошибку аутентификации
                            errorMessage = context.getString(R.string.login_error)
                        }
                    } catch (e: Exception) {
                        // Ошибка сети или другая ошибка
                        errorMessage = when {
                            e.message?.contains("Unable to resolve host") == true ->
                                context.getString(R.string.login_error)
                            e.message?.contains("timeout") == true ->
                                context.getString(R.string.login_error)
                            else ->
                                context.getString(R.string.login_error) + ": ${e.message}"
                        }
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.login))
            } else {
                Text(stringResource(R.string.login))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ссылка на регистрацию
        TextButton(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.register))
        }

        Spacer(modifier = Modifier.height(24.dp))


    }
}



