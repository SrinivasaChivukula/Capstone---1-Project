package com.gaitvision

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.gaitvision.ui.AppNavigation
import com.gaitvision.ui.GaitVisionTheme

import com.gaitvision.data.AppDatabase
import com.gaitvision.platform.PoseDetector
import com.gaitvision.platform.VideoProcessor

@Composable
fun App(
    poseDetector: PoseDetector,
    videoProcessor: VideoProcessor,
    database: AppDatabase
) {
    GaitVisionTheme {
        AppNavigation(
            poseDetector = poseDetector,
            videoProcessor = videoProcessor,
            database = database
        )
    }
}
