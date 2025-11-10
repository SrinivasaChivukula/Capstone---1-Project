package GaitVision.com.shared.platform

/**
 * Platform-agnostic camera interface for video recording and gallery access.
 * Implementations are provided in androidMain and iosMain.
 */
expect class CameraManager {
    /**
     * Request necessary camera and storage permissions.
     * @return true if permissions were granted, false otherwise
     */
    suspend fun requestPermissions(): Boolean
    
    /**
     * Check if all required permissions are granted.
     * @return true if permissions are granted, false otherwise
     */
    fun hasPermissions(): Boolean
    
    /**
     * Capture a video using the device camera.
     * @return VideoResult containing the video URI and path, or null if cancelled/failed
     */
    suspend fun captureVideo(): VideoResult?
    
    /**
     * Pick a video from the device gallery.
     * @return VideoResult containing the video URI and path, or null if cancelled/failed
     */
    suspend fun pickVideoFromGallery(): VideoResult?
}

/**
 * Result of a video operation (capture or pick from gallery).
 */
data class VideoResult(
    val uri: String,
    val path: String,
    val displayName: String? = null
)

