package GaitVision.com.shared.data.models

/**
 * GaitScore data model - shared across platforms.
 * Maps to the gait_scores table in SQLDelight.
 */
data class GaitScore(
    val id: Long = 0,
    val patientId: Long,
    val videoId: Long,
    val overallScore: Double,
    val recordedAt: Long = System.currentTimeMillis(),
    // Additional gait metrics that could be useful
    val leftKneeScore: Double? = null,
    val rightKneeScore: Double? = null,
    val leftHipScore: Double? = null,
    val rightHipScore: Double? = null,
    val torsoScore: Double? = null
)

