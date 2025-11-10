package GaitVision.com.shared.platform

import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic pose detection interface.
 * Implementations use ML Kit (Android) or Vision Framework (iOS).
 */
expect class PoseDetector {
    /**
     * Process an image and detect pose landmarks.
     * @param imageData The image data as ByteArray
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @return PoseResult containing detected landmarks, or null if detection failed
     */
    suspend fun detectPose(imageData: ByteArray, width: Int, height: Int): PoseResult?
    
    /**
     * Process a video frame and detect pose landmarks.
     * @param frameData The frame data as ByteArray
     * @param width Frame width in pixels
     * @param height Frame height in pixels
     * @param timestamp Frame timestamp in microseconds
     * @return PoseResult containing detected landmarks, or null if detection failed
     */
    suspend fun detectPoseInFrame(
        frameData: ByteArray,
        width: Int,
        height: Int,
        timestamp: Long
    ): PoseResult?
}

/**
 * Result of pose detection containing landmark coordinates.
 */
data class PoseResult(
    val landmarks: List<Landmark>,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Represents a single pose landmark (e.g., left knee, right ankle).
 */
data class Landmark(
    val type: LandmarkType,
    val x: Float,
    val y: Float,
    val z: Float = 0f,
    val visibility: Float = 1f
)

/**
 * Types of pose landmarks used in gait analysis.
 */
enum class LandmarkType {
    LEFT_ANKLE,
    RIGHT_ANKLE,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_WRIST,
    RIGHT_WRIST,
    NOSE,
    LEFT_EYE,
    RIGHT_EYE,
    LEFT_EAR,
    RIGHT_EAR,
    LEFT_FOOT_INDEX,
    RIGHT_FOOT_INDEX
}

