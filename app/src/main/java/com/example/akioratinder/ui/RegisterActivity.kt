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
                    RegisterScreen(onRegister = { email, password,server, role, rank ->
// basic validation
                        if (email.isBlank()) {
                            Toast.makeText(this, getString(R.string.err_required), Toast.LENGTH_SHORT).show()
                            return@RegisterScreen
                        }
                        prefs.saveUser(email, password, server, role, rank)
                        Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
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


    Column(modifier = Modifier
        .fillMaxSize()
        .padding(48.dp), verticalArrangement = Arrangement.Top) {


        Text(text = stringResource(R.string.register_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))


        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(stringResource(R.string.email)) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { password = it }, label = { Text(stringResource(R.string.password)) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = server, onValueChange = { server = it }, label = { Text(stringResource(R.string.server)) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text(stringResource(R.string.role)) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = rank, onValueChange = { rank = it }, label = { Text(stringResource(R.string.rank)) }, modifier = Modifier.fillMaxWidth())


        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onRegister(email.trim(),password.trim(), server.trim(), role.trim(), rank.trim()) }, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.register))
        }
    }
}