package com.marcmeru.triviagame.api

import com.marcmeru.triviagame.data.TokenResponse
import com.marcmeru.triviagame.data.TriviaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaApiService {

    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int = 1,
        @Query("category") category: Int? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("type") type: String? = null,
        @Query("token") token: String? = null // ← Token para evitar repeticiones
    ): TriviaResponse

    // Endpoint para obtener un nuevo token
    @GET("api_token.php")
    suspend fun requestToken(
        @Query("command") command: String = "request"
    ): TokenResponse

    companion object {
        const val BASE_URL = "https://opentdb.com/"
    }
}
