package GaitVision.com.shared.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of PoseDetector using ML Kit.
 */
actual class PoseDetector {
    
    private val poseDetector = PoseDetection.getClient(
        AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
            .build()
    )
    
    actual suspend fun detectPose(imageData: ByteArray, width: Int, height: Int): PoseResult? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                val image = InputImage.fromBitmap(bitmap, 0)
                
                poseDetector.process(image)
                    .addOnSuccessListener { pose ->
                        val landmarks = convertPoseToLandmarks(pose)
                        continuation.resume(PoseResult(landmarks))
                    }
                    .addOnFailureListener { e ->
                        continuation.resume(null)
                    }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }
    
    actual suspend fun detectPoseInFrame(
        frameData: ByteArray,
        width: Int,
        height: Int,
        timestamp: Long
    ): PoseResult? {
        return detectPose(frameData, width, height)
    }
    
    private fun convertPoseToLandmarks(pose: Pose): List<Landmark> {
        val landmarks = mutableListOf<Landmark>()
        
        // Convert ML Kit pose landmarks to our common Landmark format
        // This is a simplified version - you'll need to map all the landmarks
        pose.allPoseLandmarks.forEach { mlKitLandmark ->
            val type = mapMlKitLandmarkType(mlKitLandmark.landmarkType)
            landmarks.add(
                Landmark(
                    type = type,
                    x = mlKitLandmark.position.x,
                    y = mlKitLandmark.position.y,
                    z = mlKitLandmark.position.z ?: 0f,
                    visibility = mlKitLandmark.inFrameLikelihood
                )
            )
        }
        
        return landmarks
    }
    
    private fun mapMlKitLandmarkType(mlKitType: Int): LandmarkType {
        // Map ML Kit landmark types to our common types
        // This is a placeholder - you'll need to map all types properly
        return when (mlKitType) {
            com.google.mlkit.vision.pose.PoseLandmark.LEFT_ANKLE -> LandmarkType.LEFT_ANKLE
            com.google.mlkit.vision.pose.PoseLandmark.RIGHT_ANKLE -> LandmarkType.RIGHT_ANKLE
            com.google.mlkit.vision.pose.PoseLandmark.LEFT_KNEE -> LandmarkType.LEFT_KNEE
            com.google.mlkit.vision.pose.PoseLandmark.RIGHT_KNEE -> LandmarkType.RIGHT_KNEE
            com.google.mlkit.vision.pose.PoseLandmark.LEFT_HIP -> LandmarkType.LEFT_HIP
            com.google.mlkit.vision.pose.PoseLandmark.RIGHT_HIP -> LandmarkType.RIGHT_HIP
            // Add more mappings as needed
            else -> LandmarkType.NOSE // Default fallback
        }
    }
}

