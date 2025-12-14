package com.example.mobile_final

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobile_final.ui.*
import com.example.mobile_final.ui.theme.Mobile_finalThemeWithPref
import com.example.mobile_final.utils.PreferencesManager

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)
        enableEdgeToEdge()
        setContent {
            Mobile_finalThemeWithPref(
                themeMode = preferencesManager.getThemeMode()
            ) {
                MainApp(
                    onLogout = {

                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun MainApp(onLogout: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                onUserIconClick = {
                    navController.navigate("settings") {
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.Recommendations.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val apiService = com.example.mobile_final.services.ApiService.getInstance(context)
                MainRecommendationScreen(
                    apiService = apiService,
                    onNavigateToChat = { chatId ->
                        navController.navigate("chat/$chatId")
                    }
                )
            }
            composable(Screen.Chats.route) {

                val context = androidx.compose.ui.platform.LocalContext.current
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    val intent = Intent(context, ChatsListActivity::class.java)
                    context.startActivity(intent)
                }
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onLogout = onLogout)
            }
            composable("settings") {
                val context = androidx.compose.ui.platform.LocalContext.current
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    val intent = Intent(context, SettingsActivity::class.java)
                    context.startActivity(intent)
                }
            }
            composable("chat/{chatId}") { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId")
                if (chatId != null) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val apiService = com.example.mobile_final.services.ApiService.getInstance(context)
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        val intent = Intent(context, ChatActivity::class.java).apply {
                            putExtra("chatId", chatId)
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    Mobile_finalThemeWithPref {
        MainApp(onLogout = {})
    }
}