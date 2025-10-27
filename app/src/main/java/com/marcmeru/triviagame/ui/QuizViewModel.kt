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

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _bestStreak = MutableStateFlow(streakManager.getBestStreak())
    val bestStreak: StateFlow<Int> = _bestStreak.asStateFlow()

    private val _lastAnswerWasCorrect = MutableStateFlow<Boolean?>(null)
    val lastAnswerWasCorrect: StateFlow<Boolean?> = _lastAnswerWasCorrect.asStateFlow()

    private var questionQueue = mutableListOf<com.marcmeru.triviagame.data.Question>()
    private var currentQuestionIndex = 0

    // Guardar config y comparar en startGame
    private var currentGameConfig: GameConfig? = null

    fun startGame(config: GameConfig) {
        if (currentGameConfig == config && questionQueue.isNotEmpty()) {
            // Misma config, no reiniciar
            return
        }
        currentGameConfig = config
        resetGame()
        fetchQuestions()
    }

    private fun resetGame() {
        _currentStreak.value = 0
        _lastAnswerWasCorrect.value = null
        _selectedAnswer.value = null
        _isAnswerSubmitted.value = false
        questionQueue.clear()
        currentQuestionIndex = 0
        _uiState.value = QuizUiState.Loading
    }

    private fun fetchQuestions() {
        viewModelScope.launch {
            try {
                val token = tokenManager.getValidToken()
                val response = RetrofitInstance.api.getQuestions(
                    amount = currentGameConfig?.amount ?: 10,
                    type = "multiple",
                    token = token,
                    category = currentGameConfig?.category,
                    difficulty = currentGameConfig?.difficulty
                )
                when (response.response_code) {
                    0 -> {
                        if (response.results.isNotEmpty()) {
                            questionQueue.addAll(response.results)
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
                            "🎉 You've answered all available questions! The session token has run out."
                        )
                        tokenManager.clearToken()
                    }
                    else -> {
                        _uiState.value = QuizUiState.Error("API Error (Code ${response.response_code})")
                    }
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 429) {
                    _uiState.value = QuizUiState.Error("⏱️ Too many requests! Please wait and try again.")
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
        }
    }

    fun nextQuestion() {
        currentQuestionIndex++
        _selectedAnswer.value = null
        _isAnswerSubmitted.value = false
        _lastAnswerWasCorrect.value = null
        if (currentQuestionIndex >= questionQueue.size) {
            fetchQuestions()
        } else {
            showCurrentQuestion()
        }
    }

    fun selectAnswer(answer: String) {
        if (!_isAnswerSubmitted.value) {
            _selectedAnswer.value = answer
        }
    }

    fun submitAnswer() {
        _isAnswerSubmitted.value = true
        val currentQuestion = (_uiState.value as? QuizUiState.Success)?.question
        val selectedAns = _selectedAnswer.value
        if (currentQuestion != null && selectedAns != null) {
            val isCorrect = currentQuestion.isCorrectAnswer(selectedAns)
            _lastAnswerWasCorrect.value = isCorrect
            if (isCorrect) {
                _currentStreak.value += 1
                if (_currentStreak.value > _bestStreak.value) {
                    _bestStreak.value = _currentStreak.value
                    streakManager.saveBestStreak(_currentStreak.value)
                }
            } else {
                _currentStreak.value = 0
            }
        }
    }

    fun decodeHtml(text: String): String =
        Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
}
