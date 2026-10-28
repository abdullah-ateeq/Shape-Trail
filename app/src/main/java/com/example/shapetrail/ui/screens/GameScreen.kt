package com.example.shapetrail.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shapetrail.data.GameState
import com.example.shapetrail.ui.components.CountdownOverlay
import com.example.shapetrail.ui.components.ShapeBoard
import com.example.shapetrail.ui.components.ShapeIcon
import com.example.shapetrail.ui.components.TrailSequenceBar
import com.example.shapetrail.ui.theme.BrandError
import com.example.shapetrail.ui.theme.BrandPrimary
import com.example.shapetrail.ui.theme.BrandSuccess
import com.example.shapetrail.ui.theme.BrandWarning
import com.example.shapetrail.viewmodel.GameUiState

@Composable
fun GameScreen(
    uiState: GameUiState,
    reducedMotion: Boolean,
    onTileClick: (Int) -> Unit,
    onPauseClicked: () -> Unit,
    onResumeClicked: () -> Unit,
    onRestartConfirmed: () -> Unit,
    onExitConfirmed: () -> Unit
) {
    var showRestartDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Intercept back button during gameplay to pause
    BackHandler(enabled = uiState.gameState == GameState.PLAYING || uiState.gameState == GameState.PAUSED) {
        if (uiState.gameState == GameState.PLAYING) {
            onPauseClicked()
        } else if (uiState.gameState == GameState.PAUSED) {
            showExitDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: ROUND X, SCORE Y, PAUSE (Ⅱ)
            GameTopBar(
                round = uiState.currentRound,
                score = uiState.score,
                onPauseClick = onPauseClicked
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Trail sequence bar
            TrailSequenceBar(
                sequence = uiState.sequence,
                currentIndex = uiState.currentSequenceIndex,
                reducedMotion = reducedMotion
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Target Banner + Countdown Timer
            NextTargetAndTimerBar(
                targetShape = uiState.currentTargetShape,
                timerSeconds = uiState.timerSeconds,
                maxTimerSeconds = uiState.maxTimerSeconds,
                isWarning = uiState.isWarningTime,
                reducedMotion = reducedMotion
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Game Board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ShapeBoard(
                    tiles = uiState.tiles,
                    difficulty = uiState.difficulty,
                    enabled = uiState.gameState == GameState.PLAYING,
                    reducedMotion = reducedMotion,
                    onTileClick = onTileClick
                )
            }
        }

        // Countdown & Sequence Preview Overlay (3, 2, 1, GO!)
        AnimatedVisibility(
            visible = uiState.gameState == GameState.COUNTDOWN || uiState.isShowingSequencePreview,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(250))
        ) {
            CountdownOverlay(
                countdownNumber = uiState.countdownNumber,
                sequence = uiState.sequence,
                reducedMotion = reducedMotion
            )
        }

        // Round Complete Overlay
        AnimatedVisibility(
            visible = uiState.gameState == GameState.ROUND_COMPLETE,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            RoundCompleteOverlay(
                round = uiState.currentRound,
                bonusEarned = uiState.roundBonusEarned,
                milestoneText = uiState.streakMilestoneText,
                streak = uiState.streak
            )
        }

        // Pause Menu Dialog
        if (uiState.gameState == GameState.PAUSED) {
            PauseDialog(
                onResume = onResumeClicked,
                onRestartRequest = { showRestartDialog = true },
                onExitRequest = { showExitDialog = true }
            )
        }

        // Restart confirmation dialog
        if (showRestartDialog) {
            AlertDialog(
                onDismissRequest = { showRestartDialog = false },
                title = { Text(text = "Restart Game?") },
                text = { Text(text = "Current progress will be lost.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showRestartDialog = false
                            onRestartConfirmed()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandError)
                    ) {
                        Text("RESTART")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestartDialog = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }

        // Exit confirmation dialog
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text(text = "Exit Game?") },
                text = { Text(text = "Current progress will be lost.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            onExitConfirmed()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandError)
                    ) {
                        Text("EXIT")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }
    }
}

@Composable
private fun GameTopBar(
    round: Int,
    score: Int,
    onPauseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ROUND $round",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = "SCORE $score",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onPauseClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ⅱ",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun NextTargetAndTimerBar(
    targetShape: com.example.shapetrail.data.TrailShape?,
    timerSeconds: Float,
    maxTimerSeconds: Float,
    isWarning: Boolean,
    reducedMotion: Boolean
) {
    val timerPulseScale = if (isWarning && !reducedMotion) {
        val transition = rememberInfiniteTransition(label = "timerWarningPulse")
        val scale by transition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 350),
                repeatMode = RepeatMode.Reverse
            ),
            label = "timerScale"
        )
        scale
    } else 1.0f

    val progress = (timerSeconds / maxTimerSeconds).coerceIn(0f, 1f)
    val timerColor = when {
        timerSeconds <= 3.0f -> BrandError
        timerSeconds <= 6.0f -> BrandWarning
        else -> BrandPrimary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Next target badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "NEXT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                if (targetShape != null) {
                    ShapeIcon(shape = targetShape, size = 22.dp)
                }
            }

            // Numeric Timer
            Text(
                text = String.format("TIME %.1fs", timerSeconds),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = timerColor,
                modifier = Modifier.scale(timerPulseScale)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Timer progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = timerColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun RoundCompleteOverlay(
    round: Int,
    bonusEarned: Int,
    milestoneText: String?,
    streak: Int
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.90f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = milestoneText ?: "TRAIL COMPLETE!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = BrandSuccess,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "+$bonusEarned",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            if (streak > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "TRAIL STREAK: $streak",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PauseDialog(
    onResume: () -> Unit,
    onRestartRequest: () -> Unit,
    onExitRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GAME PAUSED",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onResume,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                Text(text = "RESUME", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onRestartRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "RESTART", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onExitRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "EXIT",
                    color = BrandError,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
