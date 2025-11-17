package com.example.akioratinder.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.akioratinder.data.UserProfile
import com.example.akioratinder.ui.theme.AkioraTinderTheme

class UserProfileActivity : ComponentActivity() {
    private val currentUser = UserProfile("Summoner1337", "EUW", "Mid", "Diamond", "Люблю играть")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AkioraTinderTheme {
                Scaffold(
                    topBar = { TopBar() },
                    bottomBar = { BottomNav(current = 1) }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        UserProfileScreenEditable(currentUser)
                    }
                }
            }
        }
    }
}


@Composable
fun UserProfileScreen() {
    Column(modifier = Modifier.padding(20.dp)) {

        Text(text = "Ваш профиль", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        Text("Имя: Summoner1337")
        Text("Сервер: EUW")
        Text("Роль: Mid")
        Text("Ранг: Diamond IV")
    }
}

@Composable
fun UserProfileScreenEditable(userProfile: UserProfile) {
    var name by remember { mutableStateOf(userProfile.summonerName) }
    var server by remember { mutableStateOf(userProfile.server) }
    var role by remember { mutableStateOf(userProfile.role) }
    var rank by remember { mutableStateOf(userProfile.rank) }
    var bio by remember { mutableStateOf(userProfile.bio) }

    Column(modifier = Modifier.padding(20.dp)) {
        Text("Ваш профиль", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(text = "Имя") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = server,
            onValueChange = { server = it },
            label = { Text("Сервер") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = role,
            onValueChange = { role = it },
            label = { Text("Роль") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = rank,
            onValueChange = { rank = it },
            label = { Text("Ранг") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Биография") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            // сохраняем изменения в объекте userProfile
            userProfile.summonerName = name
            userProfile.server = server
            userProfile.role = role
            userProfile.rank = rank
            userProfile.bio = bio
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Сохранить")
        }
    }
}
