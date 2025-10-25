package com.marcmeru.triviagame.ui

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
    gameConfig: GameConfig  // ← Añadir este parámetro
) {
    // LaunchedEffect para iniciar el juego cuando cambie la config
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = uiState) {
            is QuizUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading questions...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            is QuizUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.message.contains("429") ||
                                state.message.contains("Too many"))
                                MaterialTheme.colorScheme.tertiaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                }
            }

            is QuizUiState.Success -> {
                // Indicador de racha
                StreakIndicator(
                    currentStreak = currentStreak,
                    bestStreak = bestStreak
                )

                Spacer(modifier = Modifier.height(16.dp))

                QuestionContent(
                    question = state.question,
                    selectedAnswer = selectedAnswer,
                    isAnswerSubmitted = isAnswerSubmitted,
                    onAnswerSelected = { viewModel.selectAnswer(it) },
                    onSubmitAnswer = { viewModel.submitAnswer() },
                    onNextQuestion = { viewModel.nextQuestion() },
                    decodeHtml = { viewModel.decodeHtml(it) }
                )
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
        // Racha actual
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
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

        // Mejor racha
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pregunta
        Text(
            text = decodeHtml(question.question),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Respuestas
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
                    showCorrect -> Color(0xFFB7F5C2)
                    showIncorrect -> MaterialTheme.colorScheme.errorContainer
                    isSelected -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surface
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
                            enabled = !isAnswerSubmitted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = decodeHtml(answer),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        // Botón enviar respuesta
        if (selectedAnswer != null && !isAnswerSubmitted) {
            Button(onClick = onSubmitAnswer, modifier = Modifier.fillMaxWidth()) {
                Text("Submit Answer")
            }
        }

        // Botón siguiente pregunta
        if (isAnswerSubmitted) {
            Button(onClick = onNextQuestion, modifier = Modifier.fillMaxWidth()) {
                Text("Next Question")
            }
        }
    }
}


@Composable
fun AnswersList(
    answers: List<String>,
    selectedAnswer: String?,
    correctAnswer: String,
    isAnswerSubmitted: Boolean,
    onAnswerSelected: (String) -> Unit,
    decodeHtml: (String) -> String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        answers.forEach { answer ->
            val isSelected = selectedAnswer == answer
            val isCorrect = answer == correctAnswer
            val showCorrect = isAnswerSubmitted && isCorrect
            val showIncorrect = isAnswerSubmitted && isSelected && !isCorrect

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        onClick = { onAnswerSelected(answer) },
                        role = Role.RadioButton,
                        enabled = !isAnswerSubmitted
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        showCorrect -> MaterialTheme.colorScheme.primaryContainer
                        showIncorrect -> MaterialTheme.colorScheme.errorContainer
                        isSelected -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                border = if (isSelected) {
                    CardDefaults.outlinedCardBorder()
                } else null
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
                        enabled = !isAnswerSubmitted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = decodeHtml(answer),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
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
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = difficulty.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
