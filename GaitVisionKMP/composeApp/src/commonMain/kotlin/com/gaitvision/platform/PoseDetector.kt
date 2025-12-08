package com.gaitvision.platform

enum class LandmarkType {
    LEFT_SHOULDER, RIGHT_SHOULDER,
    LEFT_ELBOW, RIGHT_ELBOW,
    LEFT_WRIST, RIGHT_WRIST,
    LEFT_HIP, RIGHT_HIP,
    LEFT_KNEE, RIGHT_KNEE,
    LEFT_ANKLE, RIGHT_ANKLE,
    LEFT_HEEL, RIGHT_HEEL,
    LEFT_FOOT_INDEX, RIGHT_FOOT_INDEX,
    NOSE, LEFT_EYE_INNER, LEFT_EYE, LEFT_EYE_OUTER,
    RIGHT_EYE_INNER, RIGHT_EYE, RIGHT_EYE_OUTER,
    LEFT_EAR, RIGHT_EAR,
    LEFT_PINKY, RIGHT_PINKY,
    LEFT_INDEX, RIGHT_INDEX,
    LEFT_THUMB, RIGHT_THUMB,
    MOUTH_LEFT, MOUTH_RIGHT
}

data class Point3D(val x: Float, val y: Float, val z: Float)

data class Landmark(
    val type: LandmarkType,
    val position: Point3D,
    val visibility: Float, // 0.0 to 1.0
    val presence: Float // 0.0 to 1.0
)

data class Pose(
    val landmarks: Map<LandmarkType, Landmark>,
    val sourceWidth: Int,
    val sourceHeight: Int
) {
    fun getLandmark(type: LandmarkType): Landmark? = landmarks[type]
}

interface PoseDetector {
    // We use a generic 'Any' for the image input because it differs per platform
    // Android: Bitmap, iOS: UIImage/CVPixelBuffer
    // In a real implementation, we might use a shared ImageBitmap class from Compose
    suspend fun detectPose(image: Any): Pose?
}
