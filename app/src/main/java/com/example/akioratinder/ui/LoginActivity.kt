package com.example.akioratinder.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.akioratinder.data.Preferences
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import com.example.akioratinder.R

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Preferences(this)

        setContent {
            AkioraTinderTheme {
                val context = LocalContext.current

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onLogin = { email, password ->

                            if (email.isNotBlank() && password == "password") {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.login_success),
                                    Toast.LENGTH_SHORT
                                ).show()

                                prefs.saveUser(email, "123", "NA", "JG","Platinum 1")
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.login_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onRegister = {
                            startActivity(Intent(this, RegisterActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit, onRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            isError = email.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        if (email.isBlank()) {
            Text(
                text = stringResource(R.string.err_required),
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onLogin(email.trim(), password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.login))
        }

        TextButton(
            onClick = onRegister,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = stringResource(R.string.register))
        }
    }
}