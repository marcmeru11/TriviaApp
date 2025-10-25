package com.marcmeru.triviagame.api

import android.content.Context
import android.content.SharedPreferences
import com.marcmeru.triviagame.data.TokenResponse  // ← Asegúrate de importar esto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "trivia_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_TOKEN = "session_token"
        private const val KEY_TOKEN_TIMESTAMP = "token_timestamp"
        private const val TOKEN_VALIDITY_HOURS = 6
    }

    suspend fun getValidToken(): String? = withContext(Dispatchers.IO) {
        val token = prefs.getString(KEY_TOKEN, null)
        val timestamp = prefs.getLong(KEY_TOKEN_TIMESTAMP, 0)

        if (token != null && isTokenValid(timestamp)) {
            token
        } else {
            // Token expirado o no existe, solicitar uno nuevo
            requestNewToken()
        }
    }

    private fun isTokenValid(timestamp: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val sixHoursInMillis = TOKEN_VALIDITY_HOURS * 60 * 60 * 1000
        return (currentTime - timestamp) < sixHoursInMillis
    }

    private suspend fun requestNewToken(): String? = withContext(Dispatchers.IO) {
        try {
            val response : TokenResponse = RetrofitInstance.api.requestToken()

            if (response.response_code == 0) {
                prefs.edit().apply {
                    putString(KEY_TOKEN, response.token)
                    putLong(KEY_TOKEN_TIMESTAMP, System.currentTimeMillis())
                    apply()
                }
                response.token
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearToken() {
        prefs.edit().clear().apply()
    }
}
