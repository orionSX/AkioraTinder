// ProfileScreen.kt
package com.example.mobile_final.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mobile_final.R
import com.example.mobile_final.services.AuthManager
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextDecoration

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
                Text(text = stringResource(R.string.name) + ": ${user.collectAsState().value?.name}")
                CompositionLocalProvider(LocalContext provides context) {
                    val context = LocalContext.current
                    val userState = user.collectAsState().value
                    Text(
                        text = "Email: ${userState?.email}",
                        modifier = Modifier.clickable {
                            userState?.email?.let { email ->

                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$email")

                                    putExtra(Intent.EXTRA_SUBJECT, "Интент неявный")
                                }


                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {

                                    Toast.makeText(
                                        context,
                                        "No email app found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            val intent = Intent(context, CreateFormActivity::class.java)
            context.startActivity(intent)
        }) {
            Text(stringResource(R.string.create_form))
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