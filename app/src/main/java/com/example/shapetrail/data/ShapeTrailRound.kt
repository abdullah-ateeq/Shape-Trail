package com.example.shapetrail.data

data class ShapeTrailRound(
    val sequence: List<TrailShape>,
    val targetTileIds: List<Int>,
    val tiles: List<ShapeTile>
)
