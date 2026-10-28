package com.example.shapetrail.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.shapetrail.data.TrailShape

// Backgrounds & Surface
val DarkBackground = Color(0xFF0F172A)     // Deep slate navy
val DarkSurface = Color(0xFF1E293B)        // Elevated slate
val DarkSurfaceVariant = Color(0xFF334155) // Card border / muted
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)

val LightBackground = Color(0xFFF8FAFC)    // Crisp clean background
val LightSurface = Color(0xFFFFFFFF)       // Pure white card
val LightSurfaceVariant = Color(0xFFE2E8F0)// Border
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)

// Brand Accents
val BrandPrimary = Color(0xFF6366F1)       // Modern Indigo
val BrandSecondary = Color(0xFF06B6D4)     // Cyan
val BrandSuccess = Color(0xFF10B981)       // Emerald
val BrandWarning = Color(0xFFF59E0B)       // Amber
val BrandError = Color(0xFFEF4444)         // Crimson Red

// Premium Shape Palette
// High contrast, distinctive visual identity for each shape
val ShapeCircleColor = Color(0xFF06B6D4)   // Vibrant Cyan
val ShapeTriangleColor = Color(0xFFF59E0B) // Radiant Amber
val ShapeSquareColor = Color(0xFF8B5CF6)   // Electric Violet
val ShapeDiamondColor = Color(0xFF10B981)  // Emerald Green
val ShapeStarColor = Color(0xFFEAB308)     // Bright Gold
val ShapeHexagonColor = Color(0xFFEC4899)  // Rose Pink
val ShapePlusColor = Color(0xFF3B82F6)     // Royal Blue
val ShapeHeartColor = Color(0xFFEF4444)    // Ruby Red

fun getShapeColor(shape: TrailShape): Color = when (shape) {
    TrailShape.CIRCLE -> ShapeCircleColor
    TrailShape.TRIANGLE -> ShapeTriangleColor
    TrailShape.SQUARE -> ShapeSquareColor
    TrailShape.DIAMOND -> ShapeDiamondColor
    TrailShape.STAR -> ShapeStarColor
    TrailShape.HEXAGON -> ShapeHexagonColor
    TrailShape.PLUS -> ShapePlusColor
    TrailShape.HEART -> ShapeHeartColor
}
