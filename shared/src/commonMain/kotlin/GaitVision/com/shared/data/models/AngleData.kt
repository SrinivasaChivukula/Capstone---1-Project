package GaitVision.com.shared.data.models

/**
 * AngleData data model - shared across platforms.
 * Maps to the angle_data table in SQLDelight.
 */
data class AngleData(
    val id: Long = 0,
    val videoId: Long,
    val frameNumber: Int,
    // Angle measurements for each frame
    val leftAnkleAngle: Float? = null,
    val rightAnkleAngle: Float? = null,
    val leftKneeAngle: Float? = null,
    val rightKneeAngle: Float? = null,
    val leftHipAngle: Float? = null,
    val rightHipAngle: Float? = null,
    val torsoAngle: Float? = null,
    val strideAngle: Float? = null
)

