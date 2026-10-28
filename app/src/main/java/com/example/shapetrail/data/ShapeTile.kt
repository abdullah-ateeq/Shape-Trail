package com.example.shapetrail.data

data class ShapeTile(
    val id: Int,
    val shape: TrailShape,
    val isTarget: Boolean = false,
    val matched: Boolean = false,
    val selected: Boolean = false,
    val isError: Boolean = false
)
