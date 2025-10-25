package com.marcmeru.triviagame.data

import com.google.gson.annotations.SerializedName

data class Question(
    val category: String,
    val type: String,
    val difficulty: String,
    val question: String,
    @SerializedName("correct_answer")
    val correct_answer: String,
    @SerializedName("incorrect_answers")
    val incorrect_answers: List<String>
) {
    private var _shuffledAnswers: List<String>? = null

    val allAnswers: List<String>
        get() {
            if (_shuffledAnswers == null) {
                _shuffledAnswers = (incorrect_answers + correct_answer).shuffled()
            }
            return _shuffledAnswers!!
        }

    fun isCorrectAnswer(answer: String): Boolean {
        return answer == correct_answer
    }
}
