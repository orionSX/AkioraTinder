package com.example.akioratinder.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.akioratinder.R
import com.example.akioratinder.ui.*

@Composable
fun BottomNav(current: Int) {
    val context = LocalContext.current
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_profiles)) },
            selected = current == 0,
            onClick = {
                if (context !is MainActivity) {
                    context.startActivity(Intent(context, MainActivity::class.java))
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_profile)) },
            selected = current == 1,
            onClick = {
                if (context !is UserProfileActivity) {
                    context.startActivity(Intent(context, UserProfileActivity::class.java))
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Chat, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_chats)) },
            selected = current == 2,
            onClick = {
                if (context !is ChatListActivity) {
                    context.startActivity(Intent(context, ChatListActivity::class.java))
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_settings)) },
            selected = current == 3,
            onClick = {
                if (context !is SettingsActivity) {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                }
            }
        )
    }
}