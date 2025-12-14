// ProfileScreen.kt
package com.example.mobile_final.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mobile_final.R
import com.example.mobile_final.services.AuthManager
import androidx.compose.runtime.collectAsState

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val authManager = AuthManager.getInstance(context)
    val currentUser = authManager.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.profile),
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        currentUser?.let { user ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Имя: ${user.collectAsState().value?.name}")
                Text(text = "Email: ${user.collectAsState().value?.email}")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            val intent = Intent(context, CreateFormActivity::class.java)
            context.startActivity(intent)
        }) {
            Text("Create Form")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            authManager.logout()
            onLogout()
        }) {
            Text(stringResource(R.string.logout))
        }
    }
}