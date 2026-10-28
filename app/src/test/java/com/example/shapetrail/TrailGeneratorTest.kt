package com.example.shapetrail

import com.example.shapetrail.data.Difficulty
import com.example.shapetrail.data.TrailShape
import com.example.shapetrail.game.TrailGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TrailGeneratorTest {

    @Test
    fun boardTest_100RoundsVerification() {
        val random = Random(42)
        val difficulties = listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD)

        for (difficulty in difficulties) {
            var previousSequence: List<TrailShape>? = null

            for (roundIndex in 1..100) {
                val seqLength = TrailGenerator.sequenceLengthForRound(roundIndex, difficulty)
                val round = TrailGenerator.generateTrail(
                    sequenceLength = seqLength,
                    boardSize = difficulty.boardSize,
                    difficulty = difficulty,
                    previousSequence = previousSequence,
                    random = random
                )

                // 1. Board size is correct
                assertEquals(
                    "Board size must match difficulty board size",
                    difficulty.boardSize,
                    round.tiles.size
                )

                // 2. All tile IDs are unique (0 until boardSize)
                val allTileIds = round.tiles.map { it.id }
                assertEquals(
                    "All tile IDs on the board must be unique",
                    difficulty.boardSize,
                    allTileIds.distinct().size
                )
                assertEquals(
                    "Tile IDs should span 0 until boardSize",
                    (0 until difficulty.boardSize).toSet(),
                    allTileIds.toSet()
                )

                // 3. Every sequence position has exactly one target
                assertEquals(
                    "Target tile IDs count must equal sequence length",
                    round.sequence.size,
                    round.targetTileIds.size
                )
                assertEquals(
                    "Sequence length must equal requested length",
                    seqLength,
                    round.sequence.size
                )

                // 4. Every target tile ID is unique
                assertEquals(
                    "All target tile IDs must be unique",
                    round.targetTileIds.size,
                    round.targetTileIds.distinct().size
                )

                // 5. Every target tile exists on the board
                for (i in round.sequence.indices) {
                    val targetId = round.targetTileIds[i]
                    val expectedShape = round.sequence[i]
                    val tile = round.tiles.find { it.id == targetId }

                    assertTrue("Target tile $targetId must exist on board", tile != null)
                    assertTrue("Target tile must be marked isTarget=true", tile!!.isTarget)
                    assertEquals(
                        "Target tile at position $i must have matching shape",
                        expectedShape,
                        tile.shape
                    )
                }

                // 6. Easy mode should have no duplicate target shapes on the board
                if (difficulty == Difficulty.EASY) {
                    val targetShapes = round.sequence.toSet()
                    val distractorShapes = round.tiles.filter { !it.isTarget }.map { it.shape }.toSet()
                    val duplicates = targetShapes.intersect(distractorShapes)
                    assertTrue("Easy difficulty must not duplicate target shapes as distractors", duplicates.isEmpty())
                }

                previousSequence = round.sequence
            }
        }
    }

    @Test
    fun sequenceGeneration_neverRepeatsImmediately() {
        val random = Random(123)
        var prev: List<TrailShape>? = null
        for (i in 1..50) {
            val seq = TrailGenerator.generateSequence(
                length = 5,
                previousSequence = prev,
                random = random
            )
            assertEquals(5, seq.size)
            if (prev != null) {
                assertNotEquals("Consecutive sequences should not be identical", prev, seq)
            }
            prev = seq
        }
    }
}
