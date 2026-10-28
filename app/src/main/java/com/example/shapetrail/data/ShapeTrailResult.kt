package com.example.shapetrail.data

data class ShapeTrailResult(
    val difficulty: Difficulty,
    val score: Int,
    val round: Int,
    val longestTrail: Int,
    val completedShapes: Int,
    val totalShapes: Int,
    val streak: Int,
    val reason: GameOverReason,
    val expectedShape: TrailShape? = null,
    val tappedShape: TrailShape? = null,
    val isNewBestScore: Boolean = false,
    val isNewLongestTrail: Boolean = false
)
