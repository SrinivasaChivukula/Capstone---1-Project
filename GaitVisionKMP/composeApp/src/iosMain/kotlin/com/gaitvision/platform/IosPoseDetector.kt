package com.gaitvision.platform

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGSize
import platform.CoreVideo.CVPixelBufferRef
import platform.Vision.VNDetectHumanBodyPoseRequest
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizedPoint
import platform.Vision.VNRecognizedPointKey
import platform.Vision.VNHumanBodyPoseObservation
import platform.Vision.VNRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import platform.Foundation.NSError

@OptIn(ExperimentalForeignApi::class)
class IOSPoseDetector : PoseDetector {

    override suspend fun detectPose(image: Any): Pose? {
        // We expect image to be CVPixelBufferRef
        val pixelBuffer = image as? CVPixelBufferRef ?: return null

        return suspendCancellableCoroutine { continuation ->
            val request = VNDetectHumanBodyPoseRequest { request, error ->
                if (error != null) {
                    continuation.resume(null) // Or resumeWithException if preferred
                    return@VNDetectHumanBodyPoseRequest
                }

                val observation = request.results?.firstOrNull() as? VNHumanBodyPoseObservation
                if (observation == null) {
                    continuation.resume(null)
                    return@VNDetectHumanBodyPoseRequest
                }

                val pose = processObservation(observation)
                continuation.resume(pose)
            }

            val handler = VNImageRequestHandler(cvPixelBuffer = pixelBuffer, options = mapOf<Any?, Any?>())
            try {
                handler.performRequests(listOf(request), error = null)
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }

    private fun processObservation(observation: VNHumanBodyPoseObservation): Pose {
        val landmarks = mutableMapOf<LandmarkType, Landmark>()
        
        // Helper to map VNPoint to our Landmark
        fun mapPoint(vnKey: String, type: LandmarkType) {
            val point = observation.recognizedPointForKey(vnKey, error = null)
            if (point != null && point.confidence > 0.3) {
                // Vision points are normalized (0,0 bottom-left to 1,1 top-right)
                // We need to convert to our coordinate system if necessary, but for now we keep normalized
                // Note: Android ML Kit is (0,0 top-left). We might need to flip Y.
                // For now, let's store as is and handle flipping in UI or Analysis if needed.
                // Actually, let's flip Y here to match Android (0 at top)
                val x = point.x.toFloat()
                val y = 1.0f - point.y.toFloat() 
                
                landmarks[type] = Landmark(
                    type = type,
                    position = Point3D(x, y, 0f),
                    visibility = point.confidence,
                    presence = point.confidence
                )
            }
        }

        // Mapping
        // Note: Vision keys are strings. In Kotlin Native we access them via platform constants if available,
        // or hardcoded strings if necessary. Using string keys for now.
        // Actually, platform constants like VNHumanBodyPoseObservationJointNameNose are available.
        
        // Head
        mapPoint("VNHumanBodyPoseObservationJointNameNose", LandmarkType.NOSE)
        mapPoint("VNHumanBodyPoseObservationJointNameLeftEye", LandmarkType.LEFT_EYE)
        mapPoint("VNHumanBodyPoseObservationJointNameRightEye", LandmarkType.RIGHT_EYE)
        mapPoint("VNHumanBodyPoseObservationJointNameLeftEar", LandmarkType.LEFT_EAR)
        mapPoint("VNHumanBodyPoseObservationJointNameRightEar", LandmarkType.RIGHT_EAR)
        
        // Body
        mapPoint("VNHumanBodyPoseObservationJointNameLeftShoulder", LandmarkType.LEFT_SHOULDER)
        mapPoint("VNHumanBodyPoseObservationJointNameRightShoulder", LandmarkType.RIGHT_SHOULDER)
        mapPoint("VNHumanBodyPoseObservationJointNameLeftElbow", LandmarkType.LEFT_ELBOW)
        mapPoint("VNHumanBodyPoseObservationJointNameRightElbow", LandmarkType.RIGHT_ELBOW)
        mapPoint("VNHumanBodyPoseObservationJointNameLeftWrist", LandmarkType.LEFT_WRIST)
        mapPoint("VNHumanBodyPoseObservationJointNameRightWrist", LandmarkType.RIGHT_WRIST)
        mapPoint("VNHumanBodyPoseObservationJointNameLeftHip", LandmarkType.LEFT_HIP)
        mapPoint("VNHumanBodyPoseObservationJointNameRightHip", LandmarkType.RIGHT_HIP)
        mapPoint("VNHumanBodyPoseObservationJointNameLeftKnee", LandmarkType.LEFT_KNEE)
        mapPoint("VNHumanBodyPoseObservationJointNameRightKnee", LandmarkType.RIGHT_KNEE)
        mapPoint("VNHumanBodyPoseObservationJointNameLeftAnkle", LandmarkType.LEFT_ANKLE)
        mapPoint("VNHumanBodyPoseObservationJointNameRightAnkle", LandmarkType.RIGHT_ANKLE)

        // Vision doesn't give source width/height in the observation directly relative to image size 
        // unless we pass it. We will assume normalized coordinates (0-1) and let UI scale it.
        // We set sourceWidth/Height to 1 to indicate normalized.
        return Pose(landmarks, 1, 1)
    }
}
