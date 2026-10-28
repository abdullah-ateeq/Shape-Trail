package com.example.shapetrail.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shapetrail.audio.SoundManager
import com.example.shapetrail.data.Difficulty
import com.example.shapetrail.data.GameOverReason
import com.example.shapetrail.data.GameState
import com.example.shapetrail.data.ShapeTile
import com.example.shapetrail.data.ShapeTrailResult
import com.example.shapetrail.data.ShapeTrailRound
import com.example.shapetrail.data.TrailShape
import com.example.shapetrail.data.UserPreferencesRepository
import com.example.shapetrail.game.TrailGenerator
import com.example.shapetrail.util.HapticsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class GameUiState(
    val gameState: GameState = GameState.READY,
    val currentRound: Int = 1,
    val difficulty: Difficulty = Difficulty.EASY,
    val sequence: List<TrailShape> = emptyList(),
    val targetTileIds: List<Int> = emptyList(),
    val tiles: List<ShapeTile> = emptyList(),
    val currentSequenceIndex: Int = 0,
    val score: Int = 0,
    val timerSeconds: Float = 15f,
    val maxTimerSeconds: Float = 18f,
    val streak: Int = 0,
    val longestTrail: Int = 0,
    val completedShapesInGame: Int = 0,
    val countdownNumber: Int = 3,
    val isShowingSequencePreview: Boolean = false,
    val streakMilestoneText: String? = null,
    val roundBonusEarned: Int = 0,
    val lastResult: ShapeTrailResult? = null,
    val errorTileId: Int? = null
) {
    val currentTargetShape: TrailShape?
        get() = sequence.getOrNull(currentSequenceIndex)

    val currentTargetTileId: Int?
        get() = targetTileIds.getOrNull(currentSequenceIndex)

    val isWarningTime: Boolean
        get() = gameState == GameState.PLAYING && timerSeconds in 0.01f..3.0f
}

class ShapeTrailViewModel(application: Application) : AndroidViewModel(application) {

    val preferencesRepository = UserPreferencesRepository(application)
    val soundManager = SoundManager()
    val hapticsManager = HapticsManager(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var sequenceAnimationJob: Job? = null
    private var lastWarningSecondPlayed: Int = -1

    init {
        // Sync preferences with sound and haptics managers
        viewModelScope.launch {
            preferencesRepository.preferences.collect { prefs ->
                soundManager.isEnabled = prefs.soundEnabled
                hapticsManager.isEnabled = prefs.hapticsEnabled
            }
        }
    }

    fun startGame(difficulty: Difficulty = preferencesRepository.preferences.value.difficulty) {
        timerJob?.cancel()
        sequenceAnimationJob?.cancel()

        _uiState.update {
            GameUiState(
                gameState = GameState.COUNTDOWN,
                currentRound = 1,
                difficulty = difficulty,
                score = 0,
                streak = 0,
                longestTrail = 0,
                completedShapesInGame = 0,
                countdownNumber = 3,
                timerSeconds = difficulty.startingTimerSeconds,
                maxTimerSeconds = difficulty.maxTimerSeconds
            )
        }

        startCountdownAndRound(isFirstRoundOfGame = true)
    }

    private fun startCountdownAndRound(isFirstRoundOfGame: Boolean) {
        viewModelScope.launch {
            val difficulty = _uiState.value.difficulty
            val roundNum = _uiState.value.currentRound

            // 1. Generate the round
            val seqLength = TrailGenerator.sequenceLengthForRound(roundNum, difficulty)
            val roundData = TrailGenerator.generateTrail(
                sequenceLength = seqLength,
                boardSize = difficulty.boardSize,
                difficulty = difficulty,
                previousSequence = _uiState.value.sequence.ifEmpty { null }
            )

            // 2. Countdown phase (if first round, do full 3-2-1-GO; if subsequent round, do brief preview)
            if (isFirstRoundOfGame) {
                _uiState.update {
                    it.copy(
                        gameState = GameState.COUNTDOWN,
                        sequence = roundData.sequence,
                        targetTileIds = roundData.targetTileIds,
                        tiles = roundData.tiles,
                        currentSequenceIndex = 0,
                        countdownNumber = 3,
                        isShowingSequencePreview = true,
                        streakMilestoneText = null,
                        errorTileId = null
                    )
                }

                for (num in 3 downTo 1) {
                    _uiState.update { it.copy(countdownNumber = num) }
                    soundManager.play(SoundManager.SoundEffect.COUNTDOWN_TICK)
                    hapticsManager.vibrateTick()
                    delay(800)
                }

                _uiState.update { it.copy(countdownNumber = 0) } // 0 indicates "GO!"
                soundManager.play(SoundManager.SoundEffect.COUNTDOWN_GO)
                hapticsManager.vibrateCorrect()
                delay(500)
            } else {
                // Brief preview of YOUR TRAIL before board starts
                _uiState.update {
                    it.copy(
                        gameState = GameState.READY,
                        sequence = roundData.sequence,
                        targetTileIds = roundData.targetTileIds,
                        tiles = roundData.tiles,
                        currentSequenceIndex = 0,
                        isShowingSequencePreview = true,
                        streakMilestoneText = null,
                        errorTileId = null,
                        timerSeconds = difficulty.startingTimerSeconds
                    )
                }
                delay(900)
            }

            // 3. Start Playing
            lastWarningSecondPlayed = -1
            _uiState.update {
                it.copy(
                    gameState = GameState.PLAYING,
                    isShowingSequencePreview = false,
                    timerSeconds = difficulty.startingTimerSeconds,
                    maxTimerSeconds = difficulty.maxTimerSeconds
                )
            }

            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val intervalMs = 50L
            while (isActive) {
                delay(intervalMs)
                if (_uiState.value.gameState != GameState.PLAYING) break

                val newTime = _uiState.value.timerSeconds - (intervalMs / 1000f)

                if (newTime <= 0f) {
                    _uiState.update { it.copy(timerSeconds = 0f) }
                    onTimeout()
                    break
                }

                val currentSecInt = newTime.toInt()
                if (newTime in 0.01f..3.0f && currentSecInt != lastWarningSecondPlayed) {
                    lastWarningSecondPlayed = currentSecInt
                    soundManager.play(SoundManager.SoundEffect.TIMER_TICK)
                    hapticsManager.vibrateTick()
                }

                _uiState.update { it.copy(timerSeconds = newTime) }
            }
        }
    }

    fun onTileClicked(tileId: Int) {
        val state = _uiState.value
        if (state.gameState != GameState.PLAYING) return

        val tappedTile = state.tiles.find { it.id == tileId } ?: return
        if (tappedTile.matched) return // Already matched tile

        val expectedTargetTileId = state.currentTargetTileId
        val expectedShape = state.currentTargetShape

        if (tileId == expectedTargetTileId) {
            // Correct tile selected!
            val updatedTiles = state.tiles.map {
                if (it.id == tileId) it.copy(matched = true, selected = true) else it
            }

            val nextIndex = state.currentSequenceIndex + 1
            soundManager.play(SoundManager.SoundEffect.CORRECT_TAP, stepIndex = state.currentSequenceIndex)
            hapticsManager.vibrateCorrect()

            // Calculate score for this step
            val multiplier = 1.0 + (minOf(state.currentRound - 1, 20) * 0.05)
            val stepScore = Math.round(100.0 * multiplier).toInt()
            val newScore = state.score + stepScore

            // Add time bonus
            val bonusTime = state.difficulty.correctTapBonusSeconds
            val newTimer = minOf(state.timerSeconds + bonusTime, state.maxTimerSeconds)

            val newCompletedShapes = state.completedShapesInGame + 1
            val newLongestTrail = maxOf(state.longestTrail, nextIndex)

            _uiState.update {
                it.copy(
                    tiles = updatedTiles,
                    currentSequenceIndex = nextIndex,
                    score = newScore,
                    timerSeconds = newTimer,
                    completedShapesInGame = newCompletedShapes,
                    longestTrail = newLongestTrail
                )
            }

            // Check if round complete
            if (nextIndex >= state.sequence.size) {
                onRoundComplete()
            }
        } else {
            // Wrong tile tapped!
            handleWrongTap(tappedTile, expectedShape)
        }
    }

    private fun handleWrongTap(tappedTile: ShapeTile, expectedShape: TrailShape?) {
        timerJob?.cancel()
        val state = _uiState.value

        val reason = when {
            expectedShape != null && tappedTile.shape == expectedShape -> GameOverReason.WRONG_TILE
            expectedShape != null && state.sequence.contains(tappedTile.shape) -> GameOverReason.WRONG_ORDER
            else -> GameOverReason.WRONG_SHAPE
        }

        val updatedTiles = state.tiles.map {
            if (it.id == tappedTile.id) it.copy(isError = true) else it
        }

        _uiState.update {
            it.copy(
                tiles = updatedTiles,
                errorTileId = tappedTile.id
            )
        }

        soundManager.play(SoundManager.SoundEffect.WRONG_TAP)
        hapticsManager.vibrateWrong()

        viewModelScope.launch {
            delay(500)
            triggerGameOver(
                reason = reason,
                expectedShape = expectedShape,
                tappedShape = tappedTile.shape
            )
        }
    }

    private fun onTimeout() {
        val state = _uiState.value
        soundManager.play(SoundManager.SoundEffect.WRONG_TAP)
        hapticsManager.vibrateWrong()

        viewModelScope.launch {
            delay(350)
            triggerGameOver(
                reason = GameOverReason.TIMEOUT,
                expectedShape = state.currentTargetShape,
                tappedShape = null
            )
        }
    }

    private fun onRoundComplete() {
        timerJob?.cancel()
        val state = _uiState.value

        val roundClearBonus = 250
        val speedBonus = (state.timerSeconds * 5f).toInt()
        val newStreak = state.streak + 1
        var streakBonus = 0

        var milestoneText: String? = null
        if (newStreak % 5 == 0) {
            streakBonus = 100
        }
        when (newStreak) {
            5 -> milestoneText = "GOOD TRAIL!"
            10 -> milestoneText = "GREAT TRAIL!"
            20 -> milestoneText = "SHAPE MASTER!"
            30 -> milestoneText = "UNSTOPPABLE!"
        }

        val totalRoundBonus = roundClearBonus + speedBonus + streakBonus
        val finalScore = state.score + totalRoundBonus

        if (milestoneText != null) {
            soundManager.play(SoundManager.SoundEffect.STREAK_MILESTONE)
            hapticsManager.vibrateSuccess()
        } else {
            soundManager.play(SoundManager.SoundEffect.ROUND_COMPLETE)
            hapticsManager.vibrateSuccess()
        }

        _uiState.update {
            it.copy(
                gameState = GameState.ROUND_COMPLETE,
                score = finalScore,
                streak = newStreak,
                streakMilestoneText = milestoneText,
                roundBonusEarned = totalRoundBonus
            )
        }

        viewModelScope.launch {
            delay(1300)
            if (_uiState.value.gameState == GameState.ROUND_COMPLETE) {
                _uiState.update {
                    it.copy(
                        currentRound = it.currentRound + 1,
                        roundBonusEarned = 0,
                        streakMilestoneText = null
                    )
                }
                startCountdownAndRound(isFirstRoundOfGame = false)
            }
        }
    }

    private fun triggerGameOver(
        reason: GameOverReason,
        expectedShape: TrailShape?,
        tappedShape: TrailShape?
    ) {
        val state = _uiState.value

        soundManager.play(SoundManager.SoundEffect.GAME_OVER)

        val recordResult = preferencesRepository.recordGameFinished(
            score = state.score,
            round = state.currentRound,
            longestTrail = state.longestTrail,
            completedShapes = state.completedShapesInGame
        )

        val result = ShapeTrailResult(
            difficulty = state.difficulty,
            score = state.score,
            round = state.currentRound,
            longestTrail = state.longestTrail,
            completedShapes = state.currentSequenceIndex,
            totalShapes = state.sequence.size,
            streak = state.streak,
            reason = reason,
            expectedShape = expectedShape,
            tappedShape = tappedShape,
            isNewBestScore = recordResult.isNewBestScore,
            isNewLongestTrail = recordResult.isNewLongestTrail
        )

        _uiState.update {
            it.copy(
                gameState = GameState.GAME_OVER,
                lastResult = result
            )
        }
    }

    fun pauseGame() {
        if (_uiState.value.gameState == GameState.PLAYING) {
            timerJob?.cancel()
            _uiState.update { it.copy(gameState = GameState.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.gameState == GameState.PAUSED) {
            _uiState.update { it.copy(gameState = GameState.PLAYING) }
            startTimer()
        }
    }

    fun restartGame() {
        startGame(_uiState.value.difficulty)
    }

    fun exitGame() {
        timerJob?.cancel()
        sequenceAnimationJob?.cancel()
        _uiState.update { GameUiState(gameState = GameState.READY) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        sequenceAnimationJob?.cancel()
    }
}
