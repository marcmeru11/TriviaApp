package com.marcmeru.triviagame.ui

import com.marcmeru.triviagame.data.Question


sealed class QuizUiState {
    data object Loading : QuizUiState()
    data class Success(val question: Question) : QuizUiState()
    data class Error(val message: String) : QuizUiState()
}