package GaitVision.com

/**
 * Coordinate Normalization Utilities
 * 
 * MediaPipe (training environment) uses normalized coordinates (0.0 to 1.0).
 * Android MLKit provides pixel coordinates.
 * 
 * This module normalizes MLKit pixel coordinates to match the training coordinate space,
 * ensuring that distance and angle calculations are equivalent between platforms.
 */
object CoordinateNormalization {
    
    /**
     * Normalized 2D point (matches PC's coordinate space)
     */
    data class NormalizedPoint(
        val x: Float,  // Normalized x coordinate (0.0 to 1.0)
        val y: Float  // Normalized y coordinate (0.0 to 1.0)
    )
    
    /**
     * Normalize MLKit pixel coordinates to PC's normalized coordinate space.
     * 
     * @param pixelX X coordinate in pixels
     * @param pixelY Y coordinate in pixels
     * @param frameWidth Video frame width in pixels
     * @param frameHeight Video frame height in pixels
     * @return Normalized point (x, y) in range [0.0, 1.0]
     */
    fun normalizePoint(
        pixelX: Float,
        pixelY: Float,
        frameWidth: Int,
        frameHeight: Int
    ): NormalizedPoint {
        return NormalizedPoint(
            x = pixelX / frameWidth.toFloat(),
            y = pixelY / frameHeight.toFloat()
        )
    }
    
    /**
     * Normalize multiple points at once (for efficiency).
     * 
     * @param points List of (pixelX, pixelY) pairs
     * @param frameWidth Video frame width in pixels
     * @param frameHeight Video frame height in pixels
     * @return List of normalized points
     */
    fun normalizePoints(
        points: List<Pair<Float, Float>>,
        frameWidth: Int,
        frameHeight: Int
    ): List<NormalizedPoint> {
        return points.map { (x, y) ->
            normalizePoint(x, y, frameWidth, frameHeight)
        }
    }
    
    /**
     * Check if normalized coordinates are valid (within [0, 1] range).
     * 
     * @param point Normalized point to validate
     * @return true if coordinates are in valid range
     */
    fun isValidNormalizedPoint(point: NormalizedPoint): Boolean {
        return point.x >= 0f && point.x <= 1f && 
               point.y >= 0f && point.y <= 1f
    }
}
