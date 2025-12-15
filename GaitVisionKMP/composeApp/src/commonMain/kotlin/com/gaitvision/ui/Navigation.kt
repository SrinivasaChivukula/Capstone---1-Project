package com.gaitvision.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gaitvision.data.AppDatabase
import com.gaitvision.platform.PoseDetector
import com.gaitvision.platform.VideoProcessor

enum class Screen(val route: String) {
    Dashboard("dashboard"),
    Camera("camera"),
    Analysis("analysis")
}

@Composable
fun AppNavigation(
    poseDetector: PoseDetector,
    videoProcessor: VideoProcessor,
    database: AppDatabase,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToAnalysis = { navController.navigate(Screen.Analysis.route) },
                database = database,
                videoProcessor = videoProcessor
            )
        }
        
        composable(Screen.Camera.route) {
            CameraScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAnalysis = { navController.navigate(Screen.Analysis.route) },
                poseDetector = poseDetector,
                videoProcessor = videoProcessor
            )
        }
        
        composable(Screen.Analysis.route) {
            AnalysisScreen(
                onNavigateBack = { navController.popBackStack() },
                database = database
            )
        }
    }
}
