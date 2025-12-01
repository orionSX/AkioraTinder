package com.example.akioratinder.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import com.example.akioratinder.R
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.akioratinder.services.ProfilesManager
import com.example.akioratinder.services.GlobalSettingsManager
import com.example.akioratinder.services.LikesManager
import com.example.akioratinder.services.SwipeManager
import com.example.akioratinder.storage.ProfilesStore
import com.example.akioratinder.storage.SessionSwipeStore
import com.example.akioratinder.storage.ThemeLanguageStore
import com.example.akioratinder.ui.components.SwipeableCardStack
import com.example.akioratinder.viewmodels.ThemeViewModel


class MainActivity : ComponentActivity() {
    private lateinit var themeStore: ThemeLanguageStore
    private lateinit var profilesStore: ProfilesStore

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(themeStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalSettingsManager.initialize(this)
        ProfilesManager.initialize(this)
        SwipeManager.initialize(this)
        themeStore = ThemeLanguageStore(this)
        profilesStore = ProfilesStore(this)

        setContent {
            val darkTheme by themeViewModel.darkTheme.collectAsState()
            GlobalSettingsManager.ObserveSettings()

            AkioraTinderTheme(darkTheme = darkTheme) {
                Scaffold(
                    topBar = { TopBar() },
                    bottomBar = { BottomNav(0) }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        ProfileListScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileListScreen() {
    val context = LocalContext.current
    val profiles by ProfilesManager.getAllProfiles(context).collectAsState()

    val sessionSwipeStore = remember { SessionSwipeStore(context) }
    val filteredProfiles = profiles.filter {
        !sessionSwipeStore.wasSwiped(it.summonerName)
    }

    SwipeableCardStack(
        profiles = filteredProfiles,
        onSwipeLeft = { profile ->
            sessionSwipeStore.markLeft(profile.summonerName)
        },
        onSwipeRight = { profile ->
            sessionSwipeStore.markRight(profile.summonerName)
            LikesManager.addLike(context, profile)
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(
                    painter = painterResource(R.drawable.logof),
                    contentDescription = null
                )
            }
        }
    )
}

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
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_likes)) },
            selected = current == 3,
            onClick = {
                if (context !is LikesActivity) {
                    context.startActivity(Intent(context, LikesActivity::class.java))
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
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_settings)) },
            selected = current == 2,
            onClick = {
                if (context !is SettingsActivity) {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                }
            }
        )
    }
}
