package com.gaitvision.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gaitvision.platform.PoseDetector
import com.gaitvision.platform.VideoProcessor

@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAnalysis: () -> Unit,
    poseDetector: PoseDetector,
    videoProcessor: VideoProcessor
) {
    var pose by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.gaitvision.platform.Pose?>(null) }
    var isRecording by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val analyzer = androidx.compose.runtime.remember { com.gaitvision.logic.GaitAnalyzer() }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            poseDetector = poseDetector,
            videoProcessor = videoProcessor,
            onPoseDetected = { newPose ->
                pose = newPose
                if (isRecording) {
                    analyzer.addPose(newPose)
                }
            }
        )
        
        PoseOverlay(
            pose = pose,
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay controls
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar or Overlay info could go here
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (isRecording) {
                        isRecording = false
                        val score = analyzer.analyze()
                        analyzer.clear()
                        onNavigateToAnalysis()
                    } else {
                        isRecording = true
                        analyzer.clear()
                    }
                },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(if (isRecording) "Stop Recording" else "Start Recording")
            }
            
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text("Back")
            }
        }
    }
}
