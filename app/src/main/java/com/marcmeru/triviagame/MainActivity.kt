package com.marcmeru.triviagame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marcmeru.triviagame.ui.*
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

class TriviaViewModel : androidx.lifecycle.ViewModel() {
    // Estado con Compose mutableStateOf que persiste mientras ViewModel viva
    var currentScreen by mutableStateOf("home")
        private set

    var gameConfig by mutableStateOf<GameConfig?>(null)
        private set

    fun showHome() {
        currentScreen = "home"
        gameConfig = null
    }

    fun startQuickPlay() {
        gameConfig = GameConfig()
        currentScreen = "quiz"
    }

    fun startCustomGame(config: GameConfig) {
        gameConfig = config
        currentScreen = "quiz"
    }

    fun goToCustomGame() {
        currentScreen = "custom"
    }
}

@Composable
fun TriviaApp(modifier: Modifier = Modifier, viewModel: TriviaViewModel = viewModel()) {
    val currentScreen by remember { derivedStateOf { viewModel.currentScreen } }
    val gameConfig by remember { derivedStateOf { viewModel.gameConfig } }

    when (currentScreen) {
        "home" -> {
            HomeScreen(
                onQuickPlay = { viewModel.startQuickPlay() },
                onCustomGame = { viewModel.goToCustomGame() },
                modifier = modifier
            )
        }
        "custom" -> {
            CustomGameScreen(
                onStartGame = { config -> viewModel.startCustomGame(config) },
                onBack = { viewModel.showHome() },
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
                    TextButton(onClick = { viewModel.showHome() }) {
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
