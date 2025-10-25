package com.marcmeru.triviagame.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class StreakManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "trivia_stats",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_BEST_STREAK = "best_streak"
    }

    fun getBestStreak(): Int {
        return prefs.getInt(KEY_BEST_STREAK, 0)
    }

    fun saveBestStreak(streak: Int) {
        val currentBest = getBestStreak()
        if (streak > currentBest) {
            prefs.edit { putInt(KEY_BEST_STREAK, streak) }
        }
    }
}
