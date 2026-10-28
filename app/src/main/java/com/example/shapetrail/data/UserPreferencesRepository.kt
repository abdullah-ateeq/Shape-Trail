package com.example.shapetrail.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

data class GameStatistics(
    val bestScore: Int = 0,
    val bestRound: Int = 0,
    val longestTrail: Int = 0,
    val gamesPlayed: Int = 0,
    val totalCorrectShapes: Int = 0
)

data class AppPreferences(
    val theme: AppTheme = AppTheme.SYSTEM,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val difficulty: Difficulty = Difficulty.EASY,
    val stats: GameStatistics = GameStatistics()
)

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<AppPreferences> = _preferences.asStateFlow()

    private fun loadPreferences(): AppPreferences {
        val themeOrdinal = prefs.getInt(KEY_THEME, AppTheme.SYSTEM.ordinal)
        val theme = AppTheme.values().getOrElse(themeOrdinal) { AppTheme.SYSTEM }
        val sound = prefs.getBoolean(KEY_SOUND, true)
        val haptics = prefs.getBoolean(KEY_HAPTICS, true)
        val reducedMotion = prefs.getBoolean(KEY_REDUCED_MOTION, false)
        val difficultyName = prefs.getString(KEY_DIFFICULTY, Difficulty.EASY.name) ?: Difficulty.EASY.name
        val difficulty = try {
            Difficulty.valueOf(difficultyName)
        } catch (_: Exception) {
            Difficulty.EASY
        }

        val stats = GameStatistics(
            bestScore = prefs.getInt(KEY_BEST_SCORE, 0),
            bestRound = prefs.getInt(KEY_BEST_ROUND, 0),
            longestTrail = prefs.getInt(KEY_LONGEST_TRAIL, 0),
            gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0),
            totalCorrectShapes = prefs.getInt(KEY_TOTAL_CORRECT_SHAPES, 0)
        )

        return AppPreferences(
            theme = theme,
            soundEnabled = sound,
            hapticsEnabled = haptics,
            reducedMotion = reducedMotion,
            difficulty = difficulty,
            stats = stats
        )
    }

    fun setTheme(theme: AppTheme) {
        prefs.edit().putInt(KEY_THEME, theme.ordinal).apply()
        _preferences.value = _preferences.value.copy(theme = theme)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _preferences.value = _preferences.value.copy(soundEnabled = enabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS, enabled).apply()
        _preferences.value = _preferences.value.copy(hapticsEnabled = enabled)
    }

    fun setReducedMotion(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCED_MOTION, enabled).apply()
        _preferences.value = _preferences.value.copy(reducedMotion = enabled)
    }

    fun setDifficulty(difficulty: Difficulty) {
        prefs.edit().putString(KEY_DIFFICULTY, difficulty.name).apply()
        _preferences.value = _preferences.value.copy(difficulty = difficulty)
    }

    fun recordGameFinished(
        score: Int,
        round: Int,
        longestTrail: Int,
        completedShapes: Int
    ): RecordResult {
        val currentStats = _preferences.value.stats
        val isNewBestScore = score > currentStats.bestScore
        val isNewLongestTrail = longestTrail > currentStats.longestTrail

        val newBestScore = maxOf(currentStats.bestScore, score)
        val newBestRound = maxOf(currentStats.bestRound, round)
        val newLongestTrail = maxOf(currentStats.longestTrail, longestTrail)
        val newGamesPlayed = currentStats.gamesPlayed + 1
        val newTotalShapes = currentStats.totalCorrectShapes + completedShapes

        val updatedStats = GameStatistics(
            bestScore = newBestScore,
            bestRound = newBestRound,
            longestTrail = newLongestTrail,
            gamesPlayed = newGamesPlayed,
            totalCorrectShapes = newTotalShapes
        )

        prefs.edit()
            .putInt(KEY_BEST_SCORE, newBestScore)
            .putInt(KEY_BEST_ROUND, newBestRound)
            .putInt(KEY_LONGEST_TRAIL, newLongestTrail)
            .putInt(KEY_GAMES_PLAYED, newGamesPlayed)
            .putInt(KEY_TOTAL_CORRECT_SHAPES, newTotalShapes)
            .apply()

        _preferences.value = _preferences.value.copy(stats = updatedStats)

        return RecordResult(
            isNewBestScore = isNewBestScore && score > 0,
            isNewLongestTrail = isNewLongestTrail && longestTrail > 0
        )
    }

    fun resetStatistics() {
        prefs.edit()
            .putInt(KEY_BEST_SCORE, 0)
            .putInt(KEY_BEST_ROUND, 0)
            .putInt(KEY_LONGEST_TRAIL, 0)
            .putInt(KEY_GAMES_PLAYED, 0)
            .putInt(KEY_TOTAL_CORRECT_SHAPES, 0)
            .apply()

        _preferences.value = _preferences.value.copy(stats = GameStatistics())
    }

    data class RecordResult(
        val isNewBestScore: Boolean,
        val isNewLongestTrail: Boolean
    )

    companion object {
        private const val PREFS_NAME = "shape_trail_prefs"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_HAPTICS = "haptics_enabled"
        private const val KEY_REDUCED_MOTION = "reduced_motion"
        private const val KEY_DIFFICULTY = "difficulty"
        private const val KEY_BEST_SCORE = "best_score"
        private const val KEY_BEST_ROUND = "best_round"
        private const val KEY_LONGEST_TRAIL = "longest_trail"
        private const val KEY_GAMES_PLAYED = "games_played"
        private const val KEY_TOTAL_CORRECT_SHAPES = "total_correct_shapes"
    }
}
