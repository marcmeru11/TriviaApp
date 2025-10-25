package com.marcmeru.triviagame.data

data class TriviaResponse(
    val response_code: Int,
    val results: List<Question>
)
