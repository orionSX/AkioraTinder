// NavBar.kt (новый файл в ui папке)
package com.example.mobile_final.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mobile_final.R

sealed class Screen(val route: String, val titleResId: Int, val icon: @Composable () -> Unit) {
    object Home : Screen("home", R.string.home, { Icon(Icons.Default.Cottage, contentDescription = null) })
    object Chats : Screen("chats", R.string.chats, { Icon(Icons.Default.Chat, contentDescription = null) })
    object Profile : Screen("profile", R.string.profile, { Icon(Icons.Default.AccountCircle, contentDescription = null) })
    object Recommendations : Screen("recommendations", R.string.recommendations, { Icon(Icons.Default.Favorite, contentDescription = null) })
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    onUserIconClick: () -> Unit = {}
) {
    val items = listOf(
        Screen.Home,
        Screen.Recommendations,
        Screen.Chats,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomAppBar(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = screen.icon,
                label = { Text(stringResource(id = screen.titleResId)) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                )
            )
        }

        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings)) },
            label = { Text(stringResource(R.string.settings)) },
            selected = false,
            onClick = onUserIconClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}