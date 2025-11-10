package GaitVision.com.shared.data.models

/**
 * Video data model - shared across platforms.
 * Maps to the videos table in SQLDelight.
 */
data class Video(
    val id: Long = 0,
    val patientId: Long,
    val originalVideoPath: String, // Original video URI path (galleryUri)
    val editedVideoPath: String, // Processed video URI path (editedUri)
    val recordedAt: Long = System.currentTimeMillis(),
    val strideLengthAvg: Double? = null, // Average stride length in meters
    val videoLengthMicroseconds: Long? = null // Video length in microseconds
)

