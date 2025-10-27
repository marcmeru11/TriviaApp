package com.marcmeru.triviagame.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun QuizScreen(
    modifier: Modifier = Modifier,
    viewModel: QuizViewModel = viewModel(),
    gameConfig: GameConfig
) {
    LaunchedEffect(gameConfig) {
        viewModel.startGame(gameConfig)
    }

    val uiState by viewModel.uiState.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val isAnswerSubmitted by viewModel.isAnswerSubmitted.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val bestStreak by viewModel.bestStreak.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = uiState) {
            is QuizUiState.Loading -> LoadingView()
            is QuizUiState.Error -> ErrorView(state.message)
            is QuizUiState.Success -> {
                // Mostrar categoría y dificultad arriba
                QuestionInfo(
                    category = state.question.category,
                    difficulty = state.question.difficulty
                )

                Spacer(modifier = Modifier.height(8.dp))

                StreakIndicator(currentStreak, bestStreak)
                Spacer(modifier = Modifier.height(16.dp))

                QuestionContent(
                    question = state.question,
                    selectedAnswer = selectedAnswer,
                    isAnswerSubmitted = isAnswerSubmitted,
                    onAnswerSelected = viewModel::selectAnswer,
                    onSubmitAnswer = viewModel::submitAnswer,
                    onNextQuestion = viewModel::nextQuestion,
                    decodeHtml = viewModel::decodeHtml
                )
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Loading questions...", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun ErrorView(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = message, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun StreakIndicator(
    currentStreak: Int,
    bestStreak: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔥 $currentStreak",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Current Streak",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆 $bestStreak",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Best Record",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun QuestionContent(
    question: com.marcmeru.triviagame.data.Question,
    selectedAnswer: String?,
    isAnswerSubmitted: Boolean,
    onAnswerSelected: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    decodeHtml: (String) -> String
) {
    val isDarkTheme = isSystemInDarkTheme()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = decodeHtml(question.question),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        val answers = question.allAnswers
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            answers.forEach { answer ->
                val isSelected = selectedAnswer == answer
                val isCorrect = answer == question.correct_answer
                val showCorrect = isAnswerSubmitted && isCorrect
                val showIncorrect = isAnswerSubmitted && isSelected && !isCorrect

                val backgroundColor = when {
                    showCorrect -> Color(0xFFB7F5C2) // verde claro
                    showIncorrect -> MaterialTheme.colorScheme.errorContainer
                    isSelected -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }

                // Color de texto depende del modo oscuro o claro
                val contentColor = when {
                    showCorrect && isDarkTheme -> Color(0xFF1B5E20)  // verde oscuro en oscuro para legibilidad
                    showCorrect && !isDarkTheme -> Color.White       // blanco en claro
                    showIncorrect -> MaterialTheme.colorScheme.onErrorContainer
                    isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            onClick = { if (!isAnswerSubmitted) onAnswerSelected(answer) },
                            role = Role.RadioButton,
                            enabled = !isAnswerSubmitted
                        ),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    border = if (isSelected) CardDefaults.outlinedCardBorder() else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            enabled = !isAnswerSubmitted,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = contentColor,
                                unselectedColor = contentColor
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = decodeHtml(answer),
                            style = MaterialTheme.typography.bodyLarge,
                            color = contentColor
                        )
                    }
                }
            }
        }

        if (selectedAnswer != null && !isAnswerSubmitted) {
            Button(onClick = onSubmitAnswer, modifier = Modifier.fillMaxWidth()) {
                Text("Submit Answer")
            }
        }

        if (isAnswerSubmitted) {
            Button(onClick = onNextQuestion, modifier = Modifier.fillMaxWidth()) {
                Text("Next Question")
            }
        }
    }
}


@Composable
fun QuestionInfo(category: String, difficulty: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium
        )
        DifficultyChip(difficulty)
    }
}

@Composable
fun DifficultyChip(difficulty: String) {
    val color = when (difficulty.lowercase()) {
        "easy" -> MaterialTheme.colorScheme.tertiary
        "medium" -> MaterialTheme.colorScheme.primary
        "hard" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.2f),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = difficulty.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

