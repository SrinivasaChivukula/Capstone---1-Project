package GaitVision.com.gait

/**
 * Configuration constants for the 9-feature gait analysis pipeline.
 * These values are synchronized with the PC extraction pipeline.
 */
object GaitConfig9 {
    
    // Pose Detection
    const val CONF_THRESH = 0.5f          // Minimum landmark confidence
    
    // Signal Preprocessing
    const val SIGMA_THRESH = 3.0f         // Spike rejection threshold (3σ rule)
    const val MAX_GAP_TIME = 0.15f        // Max interpolation gap (seconds)
    const val SMOOTHING_WINDOW = 5        // Moving average window (odd number)
    
    // Feature Extraction
    const val PERCENTILE_LOW = 5          // P05 for ROM calculation
    const val PERCENTILE_HIGH = 95        // P95 for ROM/max calculation
    const val LDJ_MIN_FRAMES = 30         // Min frames for LDJ computation
    
    // Quality Thresholds
    const val MIN_VALID_FRAMES = 30       // Min frames for feature extraction
    const val QUALITY_TIER_LOW = 0.30f    // Detection rate for "low" quality
    const val QUALITY_TIER_FAIR = 0.50f   // Detection rate for "fair" quality
    
    // Feature names in canonical order
    val FEATURE_NAMES = listOf(
        "stride_amp_norm",
        "knee_left_rom",
        "knee_right_rom",
        "knee_left_max",
        "knee_right_max",
        "hip_rom",
        "ldj_knee_left",
        "ldj_knee_right",
        "ldj_hip"
    )
}

