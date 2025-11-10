package GaitVision.com.shared.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import GaitVision.com.shared.ui.screens.MainScreen
import GaitVision.com.shared.ui.screens.ProcessingScreen
import GaitVision.com.shared.ui.screens.ResultsScreen

sealed class Screen {
    object Main : Screen()
    data class Processing(val participantId: String, val height: Int, val videoUri: String) : Screen()
    data class Results(val videoId: Long) : Screen()
}

@Composable
fun GaitVisionNavGraph(
    startScreen: Screen = Screen.Main
) {
    var currentScreen by remember { mutableStateOf<Screen>(startScreen) }
    
    when (val screen = currentScreen) {
        is Screen.Main -> {
            MainScreen(
                onNavigateToProcessing = { participantId, height, videoUri ->
                    currentScreen = Screen.Processing(participantId, height, videoUri)
                }
            )
        }
        is Screen.Processing -> {
            ProcessingScreen(
                participantId = screen.participantId,
                height = screen.height,
                videoUri = screen.videoUri,
                onNavigateToResults = { videoId ->
                    currentScreen = Screen.Results(videoId)
                },
                onNavigateBack = {
                    currentScreen = Screen.Main
                }
            )
        }
        is Screen.Results -> {
            ResultsScreen(
                videoId = screen.videoId,
                onNavigateBack = {
                    currentScreen = Screen.Main
                }
            )
        }
    }
}

