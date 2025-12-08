package com.gaitvision.platform

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.coroutines.tasks.await

class AndroidPoseDetector : PoseDetector {
    
    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
        .build()
        
    private val poseDetector = PoseDetection.getClient(options)

    override suspend fun detectPose(image: Any): Pose? {
        if (image !is Bitmap) {
            println("AndroidPoseDetector: Image is not a Bitmap")
            return null
        }

        val inputImage = InputImage.fromBitmap(image, 0)

        return processImage(inputImage)
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    suspend fun detect(imageProxy: androidx.camera.core.ImageProxy): Pose? {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            return processImage(inputImage)
        }
        return null
    }

    private suspend fun processImage(inputImage: InputImage): Pose? {
        return try {
            val mlKitPose = poseDetector.process(inputImage).await()
            mapToSharedPose(mlKitPose, inputImage.width, inputImage.height)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun mapToSharedPose(mlKitPose: com.google.mlkit.vision.pose.Pose, width: Int, height: Int): Pose {
        val landmarks = mutableMapOf<LandmarkType, Landmark>()

        // Helper to map a single landmark
        fun mapLandmark(mlKitType: Int, sharedType: LandmarkType) {
            val mlLandmark = mlKitPose.getPoseLandmark(mlKitType)
            if (mlLandmark != null) {
                landmarks[sharedType] = Landmark(
                    type = sharedType,
                    position = Point3D(
                        mlLandmark.position3D.x,
                        mlLandmark.position3D.y,
                        mlLandmark.position3D.z
                    ),
                    visibility = mlLandmark.inFrameLikelihood,
                    presence = mlLandmark.inFrameLikelihood // ML Kit uses likelihood for both roughly
                )
            }
        }

        mapLandmark(PoseLandmark.LEFT_SHOULDER, LandmarkType.LEFT_SHOULDER)
        mapLandmark(PoseLandmark.RIGHT_SHOULDER, LandmarkType.RIGHT_SHOULDER)
        mapLandmark(PoseLandmark.LEFT_ELBOW, LandmarkType.LEFT_ELBOW)
        mapLandmark(PoseLandmark.RIGHT_ELBOW, LandmarkType.RIGHT_ELBOW)
        mapLandmark(PoseLandmark.LEFT_WRIST, LandmarkType.LEFT_WRIST)
        mapLandmark(PoseLandmark.RIGHT_WRIST, LandmarkType.RIGHT_WRIST)
        mapLandmark(PoseLandmark.LEFT_HIP, LandmarkType.LEFT_HIP)
        mapLandmark(PoseLandmark.RIGHT_HIP, LandmarkType.RIGHT_HIP)
        mapLandmark(PoseLandmark.LEFT_KNEE, LandmarkType.LEFT_KNEE)
        mapLandmark(PoseLandmark.RIGHT_KNEE, LandmarkType.RIGHT_KNEE)
        mapLandmark(PoseLandmark.LEFT_ANKLE, LandmarkType.LEFT_ANKLE)
        mapLandmark(PoseLandmark.RIGHT_ANKLE, LandmarkType.RIGHT_ANKLE)
        mapLandmark(PoseLandmark.LEFT_HEEL, LandmarkType.LEFT_HEEL)
        mapLandmark(PoseLandmark.RIGHT_HEEL, LandmarkType.RIGHT_HEEL)
        mapLandmark(PoseLandmark.LEFT_FOOT_INDEX, LandmarkType.LEFT_FOOT_INDEX)
        mapLandmark(PoseLandmark.RIGHT_FOOT_INDEX, LandmarkType.RIGHT_FOOT_INDEX)
        mapLandmark(PoseLandmark.NOSE, LandmarkType.NOSE)
        mapLandmark(PoseLandmark.LEFT_EYE_INNER, LandmarkType.LEFT_EYE_INNER)
        mapLandmark(PoseLandmark.LEFT_EYE, LandmarkType.LEFT_EYE)
        mapLandmark(PoseLandmark.LEFT_EYE_OUTER, LandmarkType.LEFT_EYE_OUTER)
        mapLandmark(PoseLandmark.RIGHT_EYE_INNER, LandmarkType.RIGHT_EYE_INNER)
        mapLandmark(PoseLandmark.RIGHT_EYE, LandmarkType.RIGHT_EYE)
        mapLandmark(PoseLandmark.RIGHT_EYE_OUTER, LandmarkType.RIGHT_EYE_OUTER)
        mapLandmark(PoseLandmark.LEFT_EAR, LandmarkType.LEFT_EAR)
        mapLandmark(PoseLandmark.RIGHT_EAR, LandmarkType.RIGHT_EAR)
        mapLandmark(PoseLandmark.LEFT_PINKY, LandmarkType.LEFT_PINKY)
        mapLandmark(PoseLandmark.RIGHT_PINKY, LandmarkType.RIGHT_PINKY)
        mapLandmark(PoseLandmark.LEFT_INDEX, LandmarkType.LEFT_INDEX)
        mapLandmark(PoseLandmark.RIGHT_INDEX, LandmarkType.RIGHT_INDEX)
        mapLandmark(PoseLandmark.LEFT_THUMB, LandmarkType.LEFT_THUMB)
        mapLandmark(PoseLandmark.RIGHT_THUMB, LandmarkType.RIGHT_THUMB)
        mapLandmark(PoseLandmark.LEFT_MOUTH, LandmarkType.MOUTH_LEFT)
        mapLandmark(PoseLandmark.RIGHT_MOUTH, LandmarkType.MOUTH_RIGHT)

        return Pose(landmarks, width, height)
    }
}
