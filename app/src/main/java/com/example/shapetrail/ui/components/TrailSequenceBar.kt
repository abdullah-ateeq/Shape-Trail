package com.example.shapetrail.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shapetrail.data.TrailShape
import com.example.shapetrail.ui.theme.BrandPrimary
import com.example.shapetrail.ui.theme.BrandSuccess
import com.example.shapetrail.ui.theme.getShapeColor

@Composable
fun TrailSequenceBar(
    sequence: List<TrailShape>,
    currentIndex: Int,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Auto-scroll to keep current index visible
    LaunchedEffect(currentIndex) {
        if (sequence.isNotEmpty()) {
            val progress = currentIndex.toFloat() / sequence.size.toFloat()
            scrollState.animateScrollTo((scrollState.maxValue * progress).toInt())
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRAIL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "${minOf(currentIndex, sequence.size)} / ${sequence.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.size(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in sequence.indices) {
                val shape = sequence[i]
                val isCompleted = i < currentIndex
                val isCurrent = i == currentIndex

                SequenceNode(
                    shape = shape,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent,
                    reducedMotion = reducedMotion
                )

                if (i < sequence.lastIndex) {
                    Text(
                        text = "→",
                        color = if (i < currentIndex) BrandSuccess.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SequenceNode(
    shape: TrailShape,
    isCompleted: Boolean,
    isCurrent: Boolean,
    reducedMotion: Boolean
) {
    val pulseScale = if (isCurrent && !reducedMotion) {
        val transition = rememberInfiniteTransition(label = "pulse")
        val scale by transition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 650),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
        scale
    } else 1.0f

    val shapeAlpha = when {
        isCompleted -> 0.4f
        isCurrent -> 1.0f
        else -> 0.85f
    }

    Box(
        modifier = Modifier
            .scale(pulseScale)
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isCurrent -> BrandPrimary.copy(alpha = 0.22f)
                    isCompleted -> BrandSuccess.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                }
            )
            .border(
                width = if (isCurrent) 1.8.dp else 1.dp,
                color = when {
                    isCurrent -> BrandPrimary
                    isCompleted -> BrandSuccess.copy(alpha = 0.5f)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Text(
                text = "✓",
                color = BrandSuccess,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            ShapeIcon(
                shape = shape,
                size = 20.dp,
                alpha = shapeAlpha
            )
        }
    }
}
