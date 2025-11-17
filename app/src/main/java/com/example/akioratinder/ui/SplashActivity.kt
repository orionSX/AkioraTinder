package com.example.akioratinder.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.akioratinder.ui.theme.AkioraTinderTheme
import com.example.akioratinder.R
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // keeps system splash briefly
        super.onCreate(savedInstanceState)

        setContent {
            AkioraTinderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SplashContent(
                        onFinished = {
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SplashContent(onFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }


    LaunchedEffect(true) {
        alpha.animateTo(1f, animationSpec = tween(1500))
        delay(1000)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {


            Image(
                painter = painterResource(id = R.drawable.logof),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(140.dp)
                    .alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Akiora",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}
