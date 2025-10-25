package com.marcmeru.triviagame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marcmeru.triviagame.ui.CustomGameScreen
import com.marcmeru.triviagame.ui.GameConfig
import com.marcmeru.triviagame.ui.HomeScreen
import com.marcmeru.triviagame.ui.QuizScreen
import com.marcmeru.triviagame.ui.theme.TriviaGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TriviaGameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TriviaApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TriviaApp(modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf("home") }
    var gameConfig by remember { mutableStateOf<GameConfig?>(null) }

    when (currentScreen) {
        "home" -> {
            HomeScreen(
                onQuickPlay = {
                    gameConfig = GameConfig() // Config por defecto
                    currentScreen = "quiz"
                },
                onCustomGame = {
                    currentScreen = "custom"
                },
                modifier = modifier
            )
        }
        "custom" -> {
            CustomGameScreen(
                onStartGame = { config ->
                    gameConfig = config
                    currentScreen = "quiz"
                },
                onBack = { currentScreen = "home" },
                modifier = modifier
            )
        }
        "quiz" -> {
            Column(modifier = modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    TextButton(onClick = { currentScreen = "home" }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back to Home")
                    }
                }

                QuizScreen(
                    modifier = Modifier.fillMaxSize(),
                    gameConfig = gameConfig ?: GameConfig()
                )
            }
        }
    }
}

