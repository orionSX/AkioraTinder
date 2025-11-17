package com.example.akioratinder.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import com.example.akioratinder.R

import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.example.akioratinder.data.UserProfile

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {
            AkioraTinderTheme {
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
    val profiles = listOf(
        UserProfile("AhriQueen", "EUW", "Mid", "Diamond", "Люблю играть в команде"),
        UserProfile("LeeSinMaster", "NA", "Jungle", "Platinum", "Опытный джанглер, ищу тиммейтов"),
        UserProfile("JinxFanatic", "KR", "ADC", "Gold", "Весёлый ADC, люблю фановый дэмедж"),
        UserProfile("BardSupport", "EUW", "Support", "Platinum", "Играю на саппорте, всегда помогаю команде"),
        UserProfile("ZedShadow", "RU", "Mid", "Diamond", "Люблю соло-мид и агрессивный стиль"),
        UserProfile("ThreshHook", "EUW", "Support", "Gold", "Обожаю ловить флеши крюком"),
        UserProfile("EzrealSniper", "NA", "ADC", "Platinum", "Механики выше среднего")
    )

    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        items(profiles) { profile ->
            ProfileCard(profile)
        }
    }


}
@Composable
fun ProfileCard(profile: UserProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Image(
                    painter = painterResource(id = R.drawable.logof),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(60.dp)
                )

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(profile.summonerName, style = MaterialTheme.typography.titleMedium)
                    Text("${profile.server} • ${profile.role} • ${profile.rank}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(profile.bio, style = MaterialTheme.typography.bodySmall)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        title = { Text("Akiora") },
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

