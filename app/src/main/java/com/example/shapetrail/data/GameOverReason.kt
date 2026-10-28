package com.example.shapetrail.data

enum class GameOverReason(val displayMessage: String) {
    WRONG_SHAPE("WRONG SHAPE"),
    WRONG_ORDER("WRONG ORDER"),
    WRONG_TILE("WRONG TILE"),
    TIMEOUT("TIME'S UP")
}
