package com.example.shapetrail.game

import com.example.shapetrail.data.Difficulty
import com.example.shapetrail.data.ShapeTile
import com.example.shapetrail.data.ShapeTrailRound
import com.example.shapetrail.data.TrailShape
import kotlin.random.Random

object TrailGenerator {

    fun generateSequence(
        length: Int,
        availableShapes: List<TrailShape> = TrailShape.values().toList(),
        previousSequence: List<TrailShape>? = null,
        random: Random = Random.Default
    ): List<TrailShape> {
        require(length > 0) { "Sequence length must be positive" }
        require(availableShapes.isNotEmpty()) { "Available shapes cannot be empty" }

        val allShapes = availableShapes.shuffled(random)
        var sequence: List<TrailShape>

        var attempts = 0
        do {
            attempts++
            if (length <= allShapes.size) {
                sequence = allShapes.take(length)
            } else {
                // If sequence length exceeds available unique shapes (e.g. 9..12)
                val result = ArrayList<TrailShape>(length)
                // First take all shapes once
                result.addAll(allShapes)
                // Fill the rest ensuring no adjacent duplicates
                while (result.size < length) {
                    val remainingPool = allShapes.filter { it != result.last() }
                    result.add(remainingPool.random(random))
                }
                sequence = result
            }
        } while (sequence == previousSequence && attempts < 10)

        return sequence
    }

    fun generateTrail(
        sequenceLength: Int,
        boardSize: Int,
        difficulty: Difficulty,
        previousSequence: List<TrailShape>? = null,
        random: Random = Random.Default
    ): ShapeTrailRound {
        require(sequenceLength <= boardSize) {
            "Sequence length ($sequenceLength) cannot exceed board size ($boardSize)"
        }

        // 1. Shapes pool based on difficulty if needed (all 8 shapes available)
        val availableShapes = TrailShape.values().toList()

        // 2. Generate sequence
        val sequence = generateSequence(
            length = sequenceLength,
            availableShapes = availableShapes,
            previousSequence = previousSequence,
            random = random
        )

        // 3. Choose distinct target positions on the board
        val allTileIndices = (0 until boardSize).toList()
        val targetTileIds = allTileIndices.shuffled(random).take(sequenceLength)

        // 4. Determine distractor shapes according to difficulty rules
        val nonSequenceShapes = availableShapes.filter { !sequence.contains(it) }
        val tiles = ArrayList<ShapeTile>(boardSize)

        // Pre-allocate a map of index -> tile
        val tileMap = HashMap<Int, ShapeTile>()

        // Assign target tiles
        for (i in sequence.indices) {
            val tileId = targetTileIds[i]
            val shape = sequence[i]
            tileMap[tileId] = ShapeTile(
                id = tileId,
                shape = shape,
                isTarget = true,
                matched = false,
                selected = false
            )
        }

        // Remaining tiles are distractors
        val distractorIndices = allTileIndices.filter { !targetTileIds.contains(it) }
        val duplicateCandidateShapes = sequence.distinct()

        // Determine how many duplicates of target shapes to insert as distractors
        val maxDuplicates = when (difficulty) {
            Difficulty.EASY -> 0
            Difficulty.MEDIUM -> (1..2).random(random).coerceAtMost(distractorIndices.size)
            Difficulty.HARD -> (2..4).random(random).coerceAtMost(distractorIndices.size)
        }

        var duplicatesAdded = 0
        for (distractorId in distractorIndices) {
            val shape: TrailShape = if (duplicatesAdded < maxDuplicates && duplicateCandidateShapes.isNotEmpty()) {
                duplicatesAdded++
                duplicateCandidateShapes.random(random)
            } else if (nonSequenceShapes.isNotEmpty()) {
                nonSequenceShapes.random(random)
            } else {
                availableShapes.random(random)
            }

            tileMap[distractorId] = ShapeTile(
                id = distractorId,
                shape = shape,
                isTarget = false,
                matched = false,
                selected = false
            )
        }

        for (id in 0 until boardSize) {
            tiles.add(tileMap[id] ?: ShapeTile(id = id, shape = availableShapes.first()))
        }

        return ShapeTrailRound(
            sequence = sequence,
            targetTileIds = targetTileIds,
            tiles = tiles
        )
    }

    fun sequenceLengthForRound(round: Int, difficulty: Difficulty): Int {
        val base = when (round) {
            1 -> 3
            2 -> 3
            3 -> 4
            4 -> 4
            5 -> 5
            6 -> 5
            7 -> 6
            8 -> 6
            9 -> 7
            10 -> 7
            11 -> 8
            12 -> 8
            13 -> 9
            14 -> 9
            15 -> 10
            16 -> 10
            17 -> 11
            18 -> 11
            else -> 12
        }
        return base.coerceIn(difficulty.minSequenceLength, difficulty.maxSequenceLength)
    }
}
