package com.example.shapetrail.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.shapetrail.data.TrailShape
import com.example.shapetrail.ui.theme.getShapeColor
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ShapeIcon(
    shape: TrailShape,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    tint: Color? = null,
    alpha: Float = 1.0f
) {
    val effectiveColor = (tint ?: getShapeColor(shape)).copy(alpha = alpha)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            when (shape) {
                TrailShape.CIRCLE -> {
                    val radius = minOf(w, h) * 0.40f
                    drawCircle(
                        color = effectiveColor,
                        radius = radius,
                        center = Offset(cx, cy)
                    )
                }

                TrailShape.TRIANGLE -> {
                    val path = Path().apply {
                        moveTo(cx, h * 0.14f)
                        lineTo(w * 0.88f, h * 0.86f)
                        lineTo(w * 0.12f, h * 0.86f)
                        close()
                    }
                    drawPath(path = path, color = effectiveColor)
                }

                TrailShape.SQUARE -> {
                    val s = minOf(w, h) * 0.72f
                    val left = (w - s) / 2f
                    val top = (h - s) / 2f
                    val corner = s * 0.18f
                    drawRoundRect(
                        color = effectiveColor,
                        topLeft = Offset(left, top),
                        size = Size(s, s),
                        cornerRadius = CornerRadius(corner, corner)
                    )
                }

                TrailShape.DIAMOND -> {
                    val path = Path().apply {
                        moveTo(cx, h * 0.12f)
                        lineTo(w * 0.88f, cy)
                        lineTo(cx, h * 0.88f)
                        lineTo(w * 0.12f, cy)
                        close()
                    }
                    drawPath(path = path, color = effectiveColor)
                }

                TrailShape.STAR -> {
                    val rOuter = minOf(w, h) * 0.43f
                    val rInner = rOuter * 0.46f
                    val path = Path()
                    val startAngle = -Math.PI / 2.0
                    val step = Math.PI / 5.0

                    for (i in 0 until 10) {
                        val angle = startAngle + i * step
                        val r = if (i % 2 == 0) rOuter else rInner
                        val x = cx + (r * cos(angle)).toFloat()
                        val y = cy + (r * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    drawPath(path = path, color = effectiveColor)
                }

                TrailShape.HEXAGON -> {
                    val r = minOf(w, h) * 0.42f
                    val path = Path()
                    for (i in 0 until 6) {
                        val angle = Math.toRadians((60.0 * i) + 30.0)
                        val x = cx + (r * cos(angle)).toFloat()
                        val y = cy + (r * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    drawPath(path = path, color = effectiveColor)
                }

                TrailShape.PLUS -> {
                    val t = minOf(w, h) * 0.24f
                    val s = minOf(w, h) * 0.76f
                    val left = (w - s) / 2f
                    val top = (h - s) / 2f
                    val r = t * 0.35f

                    // Horizontal arm
                    drawRoundRect(
                        color = effectiveColor,
                        topLeft = Offset(left, cy - t / 2f),
                        size = Size(s, t),
                        cornerRadius = CornerRadius(r, r)
                    )
                    // Vertical arm
                    drawRoundRect(
                        color = effectiveColor,
                        topLeft = Offset(cx - t / 2f, top),
                        size = Size(t, s),
                        cornerRadius = CornerRadius(r, r)
                    )
                }

                TrailShape.HEART -> {
                    val path = Path().apply {
                        val hw = w * 0.78f
                        val hh = h * 0.74f
                        val ox = (w - hw) / 2f
                        val oy = (h - hh) / 2f

                        moveTo(ox + hw / 2f, oy + hh * 0.85f)
                        // Left lobe curve
                        cubicTo(
                            ox + hw * 0.05f, oy + hh * 0.55f,
                            ox - hw * 0.05f, oy + hh * 0.15f,
                            ox + hw * 0.28f, oy + hh * 0.04f
                        )
                        cubicTo(
                            ox + hw * 0.45f, oy + hh * 0.04f,
                            ox + hw / 2f, oy + hh * 0.25f,
                            ox + hw / 2f, oy + hh * 0.28f
                        )
                        // Right lobe curve
                        cubicTo(
                            ox + hw / 2f, oy + hh * 0.25f,
                            ox + hw * 0.55f, oy + hh * 0.04f,
                            ox + hw * 0.72f, oy + hh * 0.04f
                        )
                        cubicTo(
                            ox + hw * 1.05f, oy + hh * 0.15f,
                            ox + hw * 0.95f, oy + hh * 0.55f,
                            ox + hw / 2f, oy + hh * 0.85f
                        )
                        close()
                    }
                    drawPath(path = path, color = effectiveColor)
                }
            }
        }
    }
}
