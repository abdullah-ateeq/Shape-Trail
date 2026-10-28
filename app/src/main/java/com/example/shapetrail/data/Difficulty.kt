package com.example.shapetrail.data

enum class Difficulty(
    val title: String,
    val rows: Int,
    val cols: Int,
    val minSequenceLength: Int,
    val maxSequenceLength: Int,
    val startingTimerSeconds: Float,
    val correctTapBonusSeconds: Float,
    val maxTimerSeconds: Float
) {
    EASY(
        title = "Easy",
        rows = 3,
        cols = 3,
        minSequenceLength = 3,
        maxSequenceLength = 6,
        startingTimerSeconds = 15f,
        correctTapBonusSeconds = 1.0f,
        maxTimerSeconds = 18f
    ),
    MEDIUM(
        title = "Medium",
        rows = 4,
        cols = 4,
        minSequenceLength = 4,
        maxSequenceLength = 9,
        startingTimerSeconds = 12f,
        correctTapBonusSeconds = 0.8f,
        maxTimerSeconds = 15f
    ),
    HARD(
        title = "Hard",
        rows = 5,
        cols = 4,
        minSequenceLength = 5,
        maxSequenceLength = 12,
        startingTimerSeconds = 9f,
        correctTapBonusSeconds = 0.6f,
        maxTimerSeconds = 12f
    );

    val boardSize: Int get() = rows * cols
}
