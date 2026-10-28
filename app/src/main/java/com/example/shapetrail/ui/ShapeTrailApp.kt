package com.example.shapetrail.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shapetrail.data.GameState
import com.example.shapetrail.ui.screens.AboutScreen
import com.example.shapetrail.ui.screens.GameScreen
import com.example.shapetrail.ui.screens.HomeScreen
import com.example.shapetrail.ui.screens.ResultScreen
import com.example.shapetrail.ui.screens.SettingsScreen
import com.example.shapetrail.ui.screens.SplashScreen
import com.example.shapetrail.ui.theme.ShapeTrailTheme
import com.example.shapetrail.viewmodel.ShapeTrailViewModel

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val GAME = "game"
    const val RESULT = "result"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

@Composable
fun ShapeTrailApp(
    viewModel: ShapeTrailViewModel = viewModel()
) {
    val navController = rememberNavController()
    val preferences by viewModel.preferencesRepository.preferences.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // When game state changes to GAME_OVER, navigate to Result screen
    LaunchedEffect(uiState.gameState) {
        if (uiState.gameState == GameState.GAME_OVER) {
            navController.navigate(Routes.RESULT) {
                popUpTo(Routes.GAME) { inclusive = true }
            }
        }
    }

    ShapeTrailTheme(appTheme = preferences.theme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Routes.SPLASH
            ) {
                composable(Routes.SPLASH) {
                    SplashScreen(
                        onSplashFinished = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.HOME) {
                    HomeScreen(
                        preferences = preferences,
                        onDifficultySelected = { diff ->
                            viewModel.preferencesRepository.setDifficulty(diff)
                        },
                        onPlayClicked = {
                            viewModel.startGame(preferences.difficulty)
                            navController.navigate(Routes.GAME)
                        },
                        onSettingsClicked = {
                            navController.navigate(Routes.SETTINGS)
                        },
                        onAboutClicked = {
                            navController.navigate(Routes.ABOUT)
                        }
                    )
                }

                composable(Routes.GAME) {
                    GameScreen(
                        uiState = uiState,
                        reducedMotion = preferences.reducedMotion,
                        onTileClick = { tileId ->
                            viewModel.onTileClicked(tileId)
                        },
                        onPauseClicked = {
                            viewModel.pauseGame()
                        },
                        onResumeClicked = {
                            viewModel.resumeGame()
                        },
                        onRestartConfirmed = {
                            viewModel.restartGame()
                        },
                        onExitConfirmed = {
                            viewModel.exitGame()
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.GAME) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.RESULT) {
                    ResultScreen(
                        result = uiState.lastResult,
                        bestScore = preferences.stats.bestScore,
                        onPlayAgainClicked = {
                            viewModel.startGame(preferences.difficulty)
                            navController.navigate(Routes.GAME) {
                                popUpTo(Routes.RESULT) { inclusive = true }
                            }
                        },
                        onHomeClicked = {
                            viewModel.exitGame()
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.RESULT) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        preferences = preferences,
                        onThemeChanged = { theme ->
                            viewModel.preferencesRepository.setTheme(theme)
                        },
                        onSoundChanged = { enabled ->
                            viewModel.preferencesRepository.setSoundEnabled(enabled)
                        },
                        onHapticsChanged = { enabled ->
                            viewModel.preferencesRepository.setHapticsEnabled(enabled)
                        },
                        onReducedMotionChanged = { enabled ->
                            viewModel.preferencesRepository.setReducedMotion(enabled)
                        },
                        onResetStatsConfirmed = {
                            viewModel.preferencesRepository.resetStatistics()
                        },
                        onBackClicked = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Routes.ABOUT) {
                    AboutScreen(
                        onBackClicked = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
