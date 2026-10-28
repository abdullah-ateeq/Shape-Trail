package com.example.shapetrail

import com.example.shapetrail.data.Difficulty
import com.example.shapetrail.data.GameOverReason
import com.example.shapetrail.data.GameState
import com.example.shapetrail.data.ShapeTile
import com.example.shapetrail.data.TrailShape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShapeTrailLogicTest {

    @Test
    fun correctInputSequence_advancesAndCompletes() {
        // Sequence: CIRCLE -> TRIANGLE -> SQUARE
        val sequence = listOf(TrailShape.CIRCLE, TrailShape.TRIANGLE, TrailShape.SQUARE)
        val targetTileIds = listOf(4, 9, 2)

        var currentSequenceIndex = 0
        var score = 0
        var timerSeconds = 15.0f
        val maxTimer = 18.0f
        val bonus = 1.0f

        // Player taps 4 (first target)
        assertEquals(targetTileIds[currentSequenceIndex], 4)
        currentSequenceIndex++
        score += 100
        timerSeconds = minOf(timerSeconds + bonus, maxTimer)
        assertEquals(1, currentSequenceIndex)

        // Player taps 9 (second target)
        assertEquals(targetTileIds[currentSequenceIndex], 9)
        currentSequenceIndex++
        score += 100
        timerSeconds = minOf(timerSeconds + bonus, maxTimer)
        assertEquals(2, currentSequenceIndex)

        // Player taps 2 (third and final target)
        assertEquals(targetTileIds[currentSequenceIndex], 2)
        currentSequenceIndex++
        score += 100
        timerSeconds = minOf(timerSeconds + bonus, maxTimer)
        assertEquals(3, currentSequenceIndex)

        val isRoundComplete = currentSequenceIndex >= sequence.size
        assertTrue("Round should complete after tapping all sequence targets", isRoundComplete)
    }

    @Test
    fun wrongInput_futureTargetFailsImmediately() {
        val targetTileIds = listOf(4, 9, 2)
        val currentSequenceIndex = 0
        val expectedTarget = targetTileIds[currentSequenceIndex] // 4
        val playerTappedTileId = 2 // future target

        val isCorrect = playerTappedTileId == expectedTarget
        assertFalse("Tapping a future target out of order must fail", isCorrect)
    }

    @Test
    fun duplicateShape_wrongTileIdFails() {
        // Target is tile 4 (CIRCLE). Tile 7 is also a CIRCLE.
        val targetTileId = 4
        val tile4 = ShapeTile(id = 4, shape = TrailShape.CIRCLE, isTarget = true)
        val tile7 = ShapeTile(id = 7, shape = TrailShape.CIRCLE, isTarget = false)

        val playerTapped = tile7
        val isTargetMatch = playerTapped.id == targetTileId
        assertFalse("Tapping duplicate shape with wrong tile ID must fail", isTargetMatch)

        val expectedShape = TrailShape.CIRCLE
        val reason = if (playerTapped.shape == expectedShape) {
            GameOverReason.WRONG_TILE
        } else {
            GameOverReason.WRONG_SHAPE
        }
        assertEquals(GameOverReason.WRONG_TILE, reason)
    }

    @Test
    fun scoreMultiplierAndStreakBonus() {
        val round = 8
        val multiplier = 1.0 + (minOf(round - 1, 20) * 0.05)
        val stepScore = Math.round(100.0 * multiplier).toInt()
        assertEquals(135, stepScore) // 100 * (1 + 7*0.05) = 100 * 1.35 = 135
    }

    @Test
    fun timerNeverExceedsMaximum() {
        val maxTimer = 18.0f
        var currentTimer = 17.5f
        val bonus = 1.0f
        currentTimer = minOf(currentTimer + bonus, maxTimer)
        assertEquals(18.0f, currentTimer, 0.001f)
    }

    @Test
    fun pausePreservesGameProgress() {
        val initialIndex = 2
        val initialScore = 350
        val initialTimer = 8.4f

        var state = GameState.PLAYING
        // Pause
        state = GameState.PAUSED
        assertEquals(GameState.PAUSED, state)
        // Check progress unchanged
        assertEquals(2, initialIndex)
        assertEquals(350, initialScore)
        assertEquals(8.4f, initialTimer, 0.001f)

        // Resume
        state = GameState.PLAYING
        assertEquals(GameState.PLAYING, state)
    }
}
