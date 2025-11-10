package GaitVision.com.shared.platform

/**
 * iOS implementation of PoseDetector using Vision Framework.
 * TODO: Implement using VNDetectHumanBodyPoseRequest
 */
actual class PoseDetector {
    
    actual suspend fun detectPose(imageData: ByteArray, width: Int, height: Int): PoseResult? {
        // TODO: Implement iOS pose detection using Vision Framework
        // This will use VNDetectHumanBodyPoseRequest
        return null
    }
    
    actual suspend fun detectPoseInFrame(
        frameData: ByteArray,
        width: Int,
        height: Int,
        timestamp: Long
    ): PoseResult? {
        return detectPose(frameData, width, height)
    }
}

