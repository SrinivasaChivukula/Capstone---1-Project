package com.gaitvision.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import com.gaitvision.platform.LandmarkType
import com.gaitvision.platform.Pose

@Composable
fun PoseOverlay(
    pose: Pose?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (pose == null) return@Canvas

        val scaleX = size.width / pose.sourceWidth
        val scaleY = size.height / pose.sourceHeight

        pose.landmarks.values.forEach { landmark ->
            if (landmark.visibility > 0.5f) {
                drawCircle(
                    color = Color.Green,
                    radius = 8f,
                    center = Offset(landmark.position.x * scaleX, landmark.position.y * scaleY)
                )
            }
        }

        // Define connections (skeleton)
        val connections = listOf(
            Pair(LandmarkType.LEFT_SHOULDER, LandmarkType.RIGHT_SHOULDER),
            Pair(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_ELBOW),
            Pair(LandmarkType.LEFT_ELBOW, LandmarkType.LEFT_WRIST),
            Pair(LandmarkType.RIGHT_SHOULDER, LandmarkType.RIGHT_ELBOW),
            Pair(LandmarkType.RIGHT_ELBOW, LandmarkType.RIGHT_WRIST),
            Pair(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_HIP),
            Pair(LandmarkType.RIGHT_SHOULDER, LandmarkType.RIGHT_HIP),
            Pair(LandmarkType.LEFT_HIP, LandmarkType.RIGHT_HIP),
            Pair(LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE),
            Pair(LandmarkType.LEFT_KNEE, LandmarkType.LEFT_ANKLE),
            Pair(LandmarkType.RIGHT_HIP, LandmarkType.RIGHT_KNEE),
            Pair(LandmarkType.RIGHT_KNEE, LandmarkType.RIGHT_ANKLE)
        )

        connections.forEach { (startType, endType) ->
            val start = pose.getLandmark(startType)
            val end = pose.getLandmark(endType)

            if (start != null && end != null && start.visibility > 0.5f && end.visibility > 0.5f) {
                drawLine(
                    color = Color.White,
                    start = Offset(start.position.x * scaleX, start.position.y * scaleY),
                    end = Offset(end.position.x * scaleX, end.position.y * scaleY),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
