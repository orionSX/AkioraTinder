package com.example.akioratinder.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import com.example.akioratinder.data.Preferences
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import com.example.akioratinder.R

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Preferences(this)
        setContent {
            AkioraTinderTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val context = LocalContext.current
                    RegisterScreen(onRegister = { email, password, server, role, rank ->
                        prefs.saveUser(email, password, server, role, rank)
                        Toast.makeText(context, context.getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                        finish()
                    })
                }
            }
        }
    }
}


@Composable
fun RegisterScreen(onRegister: (String, String, String, String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var server by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var rank by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var serverError by remember { mutableStateOf<String?>(null) }
    var roleError by remember { mutableStateOf<String?>(null) }
    var rankError by remember { mutableStateOf<String?>(null) }


    val context = LocalContext.current

    fun validate(): Boolean {
        var isValid = true
        emailError = null
        passwordError = null
        serverError = null
        roleError = null
        rankError = null

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = context.getString(R.string.err_email_invalid)
            isValid = false
        }
        if (password.isBlank() || password.length < 6) {
            passwordError = context.getString(R.string.err_password_short)
            isValid = false
        }
        if (server.isBlank()) {
            serverError = context.getString(R.string.err_required)
            isValid = false
        }
        if (role.isBlank()) {
            roleError = context.getString(R.string.err_required)
            isValid = false
        }
        if (rank.isBlank()) {
            rankError = context.getString(R.string.err_required)
            isValid = false
        }
        return isValid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = { Text(stringResource(R.string.email)) },
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = server,
            onValueChange = {
                server = it
                serverError = null
            },
            label = { Text(stringResource(R.string.server)) },
            isError = serverError != null,
            supportingText = serverError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = role,
            onValueChange = {
                role = it
                roleError = null
            },
            label = { Text(stringResource(R.string.role)) },
            isError = roleError != null,
            supportingText = roleError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = rank,
            onValueChange = {
                rank = it
                rankError = null
            },
            label = { Text(stringResource(R.string.rank)) },
            isError = rankError != null,
            supportingText = rankError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (validate()) {
                    onRegister(email.trim(), password.trim(), server.trim(), role.trim(), rank.trim())
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.register))
        }
    }
}