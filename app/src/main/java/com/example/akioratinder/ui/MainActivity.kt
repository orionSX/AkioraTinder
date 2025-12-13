package com.example.akioratinder.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.akioratinder.data.*
import com.example.akioratinder.services.*
import com.example.akioratinder.storage.*
import com.example.akioratinder.ui.components.SwipeableCardStack
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import com.example.akioratinder.R
import com.example.akioratinder.viewmodels.ThemeViewModel
import com.example.akioratinder.viewmodels.ThemeViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var themeStore: ThemeLanguageStore

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(themeStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализируем хранилище темы
        themeStore = ThemeLanguageStore(this)
        GlobalSettingsManager.initialize(this)

        // Инициализируем API сервисы
        ApiService.getInstance(this)
        AuthManager.getInstance(this)

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
    val coroutineScope = rememberCoroutineScope()
    val authManager = remember { AuthManager.getInstance(context) }
    val apiService = remember { ApiService.getInstance(context) }

    var profiles by remember { mutableStateOf<List<PlayerProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            profiles = apiService.getForms()
            // Фильтруем только активные формы и не свои
            val currentUserId = authManager.currentUser.value?.id
            profiles = profiles.filter {
                it.active && !it.deleted && it.creatorId != currentUserId
            }
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        error != null -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Error: $error",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        profiles.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "No profiles found",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        else -> {
            SwipeableCardStack(
                profiles = profiles,
                onSwipeLeft = { profile ->
                    coroutineScope.launch {
                        try {
                            val currentUserId = authManager.currentUser.value?.id
                            if (currentUserId != null) {
                                apiService.dislikeForm(profile.id, currentUserId)
                            }
                        } catch (e: Exception) {
                            // Обработка ошибки
                        }
                    }
                },
                onSwipeRight = { profile ->
                    coroutineScope.launch {
                        try {
                            val currentUserId = authManager.currentUser.value?.id
                            if (currentUserId != null) {
                                apiService.likeForm(profile.id, currentUserId)
                            }
                        } catch (e: Exception) {
                            // Обработка ошибки
                        }
                    }
                }
            )
        }
    }
}

// Остальной код (TopBar, BottomNav) остается таким же

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
