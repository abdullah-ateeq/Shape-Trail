package com.example.shapetrail.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shapetrail.data.Difficulty
import com.example.shapetrail.data.ShapeTile
import com.example.shapetrail.ui.theme.BrandError
import com.example.shapetrail.ui.theme.BrandSuccess
import kotlin.math.roundToInt

@Composable
fun ShapeBoard(
    tiles: List<ShapeTile>,
    difficulty: Difficulty,
    enabled: Boolean,
    reducedMotion: Boolean,
    onTileClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = difficulty.rows
    val cols = difficulty.cols

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (c in 0 until cols) {
                    val index = r * cols + c
                    val tile = tiles.getOrNull(index)
                    if (tile != null) {
                        ShapeTileItem(
                            tile = tile,
                            enabled = enabled && !tile.matched,
                            reducedMotion = reducedMotion,
                            onClick = { onTileClick(tile.id) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ShapeTileItem(
    tile: ShapeTile,
    enabled: Boolean,
    reducedMotion: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shakeOffset = remember { Animatable(0f) }

    // Trigger shake on error
    LaunchedEffect(tile.isError) {
        if (tile.isError && !reducedMotion) {
            val shakeValues = listOf(0f, -14f, 14f, -10f, 10f, -5f, 5f, 0f)
            for (value in shakeValues) {
                shakeOffset.animateTo(
                    targetValue = value,
                    animationSpec = tween(durationMillis = 35)
                )
            }
        }
    }

    val tileScale by animateFloatAsState(
        targetValue = when {
            tile.isError -> 1.05f
            tile.matched -> 0.94f
            else -> 1.0f
        },
        animationSpec = if (reducedMotion) tween(0) else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tileScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (tile.matched) 0.38f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "contentAlpha"
    )

    val surfaceColor = MaterialTheme.colorScheme.surface
    val borderColor = when {
        tile.isError -> BrandError
        tile.matched -> BrandSuccess.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderWidth = if (tile.isError) 2.5.dp else if (tile.matched) 1.5.dp else 1.dp
    val shapeRadius = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .offset { IntOffset(x = shakeOffset.value.roundToInt(), y = 0) }
            .scale(tileScale)
            .clip(shapeRadius)
            .background(surfaceColor)
            .border(borderWidth, borderColor, shapeRadius)
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Shape graphic
        ShapeIcon(
            shape = tile.shape,
            size = 44.dp,
            alpha = contentAlpha,
            modifier = Modifier.padding(8.dp)
        )

        // Matched checkmark overlay
        if (tile.matched) {
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .background(BrandSuccess, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Error overlay
        if (tile.isError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BrandError.copy(alpha = 0.22f))
            )
        }
    }
}
