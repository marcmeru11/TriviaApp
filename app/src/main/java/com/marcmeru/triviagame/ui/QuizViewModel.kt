package com.marcmeru.triviagame.ui

import android.app.Application
import android.text.Html
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.marcmeru.triviagame.api.RetrofitInstance
import com.marcmeru.triviagame.api.TokenManager
import com.marcmeru.triviagame.data.StreakManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val streakManager = StreakManager(application)

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _selectedAnswer = MutableStateFlow<String?>(null)
    val selectedAnswer: StateFlow<String?> = _selectedAnswer.asStateFlow()

    private val _isAnswerSubmitted = MutableStateFlow(false)
    val isAnswerSubmitted: StateFlow<Boolean> = _isAnswerSubmitted.asStateFlow()

    // Sistema de racha
    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _bestStreak = MutableStateFlow(streakManager.getBestStreak())
    val bestStreak: StateFlow<Int> = _bestStreak.asStateFlow()

    private val _lastAnswerWasCorrect = MutableStateFlow<Boolean?>(null)
    val lastAnswerWasCorrect: StateFlow<Boolean?> = _lastAnswerWasCorrect.asStateFlow()

    // Lista de preguntas y posición actual
    private val questionQueue = mutableListOf<com.marcmeru.triviagame.data.Question>()
    private var currentQuestionIndex = 0

    private val _currentQuestionNumber = MutableStateFlow(1)
    val currentQuestionNumber: StateFlow<Int> = _currentQuestionNumber.asStateFlow()

    private val _totalQuestions = MutableStateFlow(10)
    val totalQuestions: StateFlow<Int> = _totalQuestions.asStateFlow()

    // Configuración del juego
    private var gameConfig = GameConfig()

    init {
        // No cargar preguntas automáticamente
        // Esperar a que se llame startGame()
    }

    // Método público para iniciar juego con configuración
    fun startGame(config: GameConfig = GameConfig()) {
        gameConfig = config
        _currentStreak.value = 0
        _lastAnswerWasCorrect.value = null
        fetchQuestions()
    }

    private fun fetchQuestions() {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            _selectedAnswer.value = null
            _isAnswerSubmitted.value = false

            try {
                val token = tokenManager.getValidToken()

                val response = RetrofitInstance.api.getQuestions(
                    amount = gameConfig.amount,  // ← Usar config
                    type = "multiple",
                    token = token,
                    category = gameConfig.category,  // ← Usar config
                    difficulty = gameConfig.difficulty  // ← Usar config
                )

                when (response.response_code) {
                    0 -> {
                        if (response.results.isNotEmpty()) {
                            questionQueue.clear()
                            questionQueue.addAll(response.results)
                            currentQuestionIndex = 0
                            _currentQuestionNumber.value = 1
                            _totalQuestions.value = questionQueue.size
                            showCurrentQuestion()
                        } else {
                            _uiState.value = QuizUiState.Error("No questions received")
                        }
                    }
                    3 -> {
                        tokenManager.clearToken()
                        fetchQuestions()
                    }
                    4 -> {
                        _uiState.value = QuizUiState.Error(
                            "🎉 You've answered all available questions!\n\n" +
                                    "The session token has run out of questions. " +
                                    "This will reset automatically."
                        )
                        tokenManager.clearToken()
                    }
                    else -> {
                        _uiState.value = QuizUiState.Error(
                            "API Error (Code ${response.response_code})"
                        )
                    }
                }

            } catch (e: retrofit2.HttpException) {
                if (e.code() == 429) {
                    _uiState.value = QuizUiState.Error(
                        "⏱️ Too many requests!\n\n" +
                                "You're going too fast! The API has a rate limit.\n" +
                                "Please wait a few seconds before trying again."
                    )
                } else {
                    _uiState.value = QuizUiState.Error("HTTP Error ${e.code()}: ${e.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error("Error: ${e.message}")
            }
        }
    }

    private fun showCurrentQuestion() {
        if (currentQuestionIndex < questionQueue.size) {
            _uiState.value = QuizUiState.Success(questionQueue[currentQuestionIndex])
            _currentQuestionNumber.value = currentQuestionIndex + 1
        } else {
            fetchQuestions()
        }
    }

    fun nextQuestion() {
        _selectedAnswer.value = null
        _isAnswerSubmitted.value = false
        _lastAnswerWasCorrect.value = null
        currentQuestionIndex++
        showCurrentQuestion()
    }

    fun selectAnswer(answer: String) {
        if (!_isAnswerSubmitted.value) {
            _selectedAnswer.value = answer
        }
    }

    fun submitAnswer() {
        _isAnswerSubmitted.value = true

        // Verificar si la respuesta es correcta
        val currentQuestion = (_uiState.value as? QuizUiState.Success)?.question
        val selectedAns = _selectedAnswer.value

        if (currentQuestion != null && selectedAns != null) {
            val isCorrect = currentQuestion.isCorrectAnswer(selectedAns)
            _lastAnswerWasCorrect.value = isCorrect

            if (isCorrect) {
                // Incrementar racha
                _currentStreak.value += 1

                // Actualizar mejor racha si es necesario
                if (_currentStreak.value > _bestStreak.value) {
                    _bestStreak.value = _currentStreak.value
                    streakManager.saveBestStreak(_currentStreak.value)
                }
            } else {
                // Reiniciar racha
                _currentStreak.value = 0
            }
        }
    }

    fun resetStreak() {
        _currentStreak.value = 0
    }

    fun decodeHtml(text: String): String {
        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
    }
}
