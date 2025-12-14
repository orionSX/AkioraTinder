package com.example.mobile_final.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mobile_final.MainActivity
import com.example.mobile_final.services.AuthManager
import com.example.mobile_final.ui.theme.Mobile_finalTheme
import kotlinx.coroutines.launch
import com.example.mobile_final.R

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Mobile_finalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RegisterScreen(
                        onRegistrationComplete = {
                            // Переход на MainActivity после успешной верификации
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(onRegistrationComplete: () -> Unit) {
    var currentStep by remember { mutableStateOf(1) } // 1 - регистрация, 2 - верификация
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var verificationCodeError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun validateRegistration(): Boolean {
        var isValid = true
        nameError = null
        emailError = null
        passwordError = null
        confirmPasswordError = null

        if (name.isBlank()) {
            nameError = context.getString(R.string.err_required)
            isValid = false
        }

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = context.getString(R.string.err_email_invalid)
            isValid = false
        }

        if (password.isBlank() || password.length < 3) {
            passwordError = context.getString(R.string.err_password_short)
            isValid = false
        }

        if (confirmPassword.isBlank() || password != confirmPassword) {
            confirmPasswordError = context.getString(R.string.err_password_mismatch)
            isValid = false
        }

        return isValid
    }

    fun validateVerification(): Boolean {
        var isValid = true
        verificationCodeError = null

        if (verificationCode.isBlank()) {
            verificationCodeError = context.getString(R.string.err_required)
            isValid = false
        }

        return isValid
    }

    fun handleRegister() {
        if (!validateRegistration()) return

        isLoading = true
        coroutineScope.launch {
            val authManager = AuthManager.getInstance(context)
            val response = authManager.register(
                name = name.trim(),
                email = email.trim(),
                password = password.trim()
            )

            if (response) {
                // Успешная регистрация, переходим к шагу верификации
                currentStep = 2
            }
            isLoading = false
        }
    }

    fun handleVerify() {
        if (!validateVerification()) return

        isLoading = true
        coroutineScope.launch {
            val authManager = AuthManager.getInstance(context)
            val response = authManager.verify(
                email = email.trim(),
                code = verificationCode.trim()
            )

            if (response.success) {
                // Успешная верификация, завершаем регистрацию
                onRegistrationComplete()
            }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (currentStep) {
            1 -> RegistrationStep(
                name = name,
                onNameChange = { name = it; nameError = null },
                nameError = nameError,
                email = email,
                onEmailChange = { email = it; emailError = null },
                emailError = emailError,
                password = password,
                onPasswordChange = { password = it; passwordError = null },
                passwordError = passwordError,
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = { confirmPassword = it; confirmPasswordError = null },
                confirmPasswordError = confirmPasswordError,
                isLoading = isLoading,
                onRegisterClick = { handleRegister() }
            )

            2 -> VerificationStep(
                email = email,
                verificationCode = verificationCode,
                onVerificationCodeChange = { verificationCode = it; verificationCodeError = null },
                verificationCodeError = verificationCodeError,
                isLoading = isLoading,
                onVerifyClick = { handleVerify() },
                onBackClick = {
                    currentStep = 1
                    verificationCode = ""
                    verificationCodeError = null
                }
            )
        }
    }
}

@Composable
fun RegistrationStep(
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordError: String?,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    confirmPasswordError: String?,
    isLoading: Boolean,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.login)) },
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(stringResource(R.string.email)) },
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text(stringResource(R.string.confirm_password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = confirmPasswordError != null,
            supportingText = confirmPasswordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(
                text = if (isLoading) stringResource(R.string.loading)
                else stringResource(R.string.register)
            )
        }
    }
}

@Composable
fun VerificationStep(
    email: String,
    verificationCode: String,
    onVerificationCodeChange: (String) -> Unit,
    verificationCodeError: String?,
    isLoading: Boolean,
    onVerifyClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.verification_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = stringResource(R.string.verification_message, email),
            style = MaterialTheme.typography.bodyMedium
        )

        // Поле email только для чтения
        OutlinedTextField(
            value = email,
            onValueChange = {},
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = false // Заблокировано для редактирования
        )

        OutlinedTextField(
            value = verificationCode,
            onValueChange = onVerificationCodeChange,
            label = { Text(stringResource(R.string.verification_code)) },
            isError = verificationCodeError != null,
            supportingText = verificationCodeError?.let {
                { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onVerifyClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(
                text = if (isLoading) stringResource(R.string.verifying)
                else stringResource(R.string.verify)
            )
        }

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(text = stringResource(R.string.back))
        }
    }
}