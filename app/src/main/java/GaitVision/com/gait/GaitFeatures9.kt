package GaitVision.com.gait

/**
 * 9-Feature Gait Extractor
 * 
 * Changing these calculations will lead to mismatch with training environment.
 * 
 * Features extracted (in order):
 * 1. stride_amp_norm   - P95(inter_ankle_dist) / avg_leg_length
 * 2. knee_left_rom     - P95 - P05 of left knee angle
 * 3. knee_right_rom    - P95 - P05 of right knee angle
 * 4. knee_left_max     - P95 of left knee angle
 * 5. knee_right_max    - P95 of right knee angle
 * 6. hip_rom           - P95 - P05 of hip angle
 * 7. ldj_knee_left     - Log dimensionless jerk of left knee
 * 8. ldj_knee_right    - Log dimensionless jerk of right knee
 * 9. ldj_hip           - Log dimensionless jerk of hip
 */
object GaitFeatures9 {
    
    /**
     * Compute normalized stride amplitude.
     * 
     * stride_amp_norm = P95(inter_ankle_dist) / avg_leg_length
     * 
     * Where:
     *   avg_leg_length = nanmean(0.5 * (leg_length_left + leg_length_right))
     * 
     * @param interAnkleDist Inter-ankle distance signal (normalized coords)
     * @param legLengthLeft Left leg length signal (normalized coords)
     * @param legLengthRight Right leg length signal (normalized coords)
     * @return Normalized stride amplitude (dimensionless), or NaN if insufficient data
     */
    fun computeStrideAmpNorm(
        interAnkleDist: FloatArray,
        legLengthLeft: FloatArray,
        legLengthRight: FloatArray
    ): Float {
        // P95 of inter-ankle distance
        val d95 = GaitLDJ9.nanPercentile(interAnkleDist, GaitConfig9.PERCENTILE_HIGH.toFloat())
        
        if (d95.isNaN()) {
            return Float.NaN
        }
        
        // Average leg length
        // Compute frame-by-frame average, then take mean
        val minLen = minOf(legLengthLeft.size, legLengthRight.size)
        if (minLen == 0) {
            return Float.NaN
        }
        
        val legAvgValues = mutableListOf<Float>()
        for (i in 0 until minLen) {
            val left = legLengthLeft[i]
            val right = legLengthRight[i]
            if (!left.isNaN() && !left.isInfinite() && 
                !right.isNaN() && !right.isInfinite()) {
                legAvgValues.add(0.5f * (left + right))
            }
        }
        
        if (legAvgValues.isEmpty()) {
            return Float.NaN
        }
        
        val avgLegLength = legAvgValues.average().toFloat()
        
        if (avgLegLength <= 0f || avgLegLength.isNaN()) {
            return Float.NaN
        }
        
        return d95 / avgLegLength
    }
    
    /**
     * Extract all 9 features from processed signals.
     * 
     * @param signals Processed signals
     * @return FeatureVector9 with all 9 features
     */
    fun extractFeatures(signals: ProcessedSignals9): FeatureVector9 {
        val fps = signals.fps
        
        // Feature 1: stride_amp_norm
        val strideAmpNorm = computeStrideAmpNorm(
            signals.interAnkleDist,
            signals.legLengthLeft,
            signals.legLengthRight
        )
        
        // Features 2-3: Knee ROMs
        val kneeLeftRom = GaitLDJ9.computeRom(signals.kneeAngleLeft)
        val kneeRightRom = GaitLDJ9.computeRom(signals.kneeAngleRight)
        
        // Features 4-5: Knee maxes (P95)
        val kneeLeftMax = GaitLDJ9.computeMax(signals.kneeAngleLeft)
        val kneeRightMax = GaitLDJ9.computeMax(signals.kneeAngleRight)
        
        // Feature 6: Hip ROM
        val hipRom = GaitLDJ9.computeRom(signals.hipAngle)
        
        // Features 7-9: LDJ values
        val ldjKneeLeft = GaitLDJ9.computeLdj(signals.kneeAngleLeft, fps)
        val ldjKneeRight = GaitLDJ9.computeLdj(signals.kneeAngleRight, fps)
        val ldjHip = GaitLDJ9.computeLdj(signals.hipAngle, fps)
        
        return FeatureVector9(
            strideAmpNorm = strideAmpNorm,
            kneeLeftRom = kneeLeftRom,
            kneeRightRom = kneeRightRom,
            kneeLeftMax = kneeLeftMax,
            kneeRightMax = kneeRightMax,
            hipRom = hipRom,
            ldjKneeLeft = ldjKneeLeft,
            ldjKneeRight = ldjKneeRight,
            ldjHip = ldjHip
        )
    }
    
    /**
     * Extract features from raw signal arrays.
     * 
     * Convenience method that processes signals first.
     * 
     * @param fps Frame rate
     * @param kneeAngleLeft Raw left knee angle signal
     * @param kneeAngleRight Raw right knee angle signal
     * @param hipAngle Raw hip angle signal
     * @param interAnkleDist Raw inter-ankle distance signal
     * @param legLengthLeft Raw left leg length signal
     * @param legLengthRight Raw right leg length signal
     * @return FeatureVector9 with all 9 features
     */
    fun extractFeaturesFromRaw(
        fps: Float,
        kneeAngleLeft: FloatArray,
        kneeAngleRight: FloatArray,
        hipAngle: FloatArray,
        interAnkleDist: FloatArray,
        legLengthLeft: FloatArray,
        legLengthRight: FloatArray
    ): FeatureVector9 {
        val processed = ProcessedSignals9.fromRaw(
            fps = fps,
            kneeAngleLeft = kneeAngleLeft,
            kneeAngleRight = kneeAngleRight,
            hipAngle = hipAngle,
            interAnkleDist = interAnkleDist,
            legLengthLeft = legLengthLeft,
            legLengthRight = legLengthRight
        )
        return extractFeatures(processed)
    }
}

/**
 * Container for the 9 gait features in canonical order.
 */
data class FeatureVector9(
    val strideAmpNorm: Float,
    val kneeLeftRom: Float,
    val kneeRightRom: Float,
    val kneeLeftMax: Float,
    val kneeRightMax: Float,
    val hipRom: Float,
    val ldjKneeLeft: Float,
    val ldjKneeRight: Float,
    val ldjHip: Float
) {
    /**
     * Convert to FloatArray in canonical order.
     */
    fun toArray(): FloatArray {
        return floatArrayOf(
            strideAmpNorm,
            kneeLeftRom,
            kneeRightRom,
            kneeLeftMax,
            kneeRightMax,
            hipRom,
            ldjKneeLeft,
            ldjKneeRight,
            ldjHip
        )
    }
    
    /**
     * Convert to Map with feature names.
     */
    fun toMap(): Map<String, Float> {
        return mapOf(
            "stride_amp_norm" to strideAmpNorm,
            "knee_left_rom" to kneeLeftRom,
            "knee_right_rom" to kneeRightRom,
            "knee_left_max" to kneeLeftMax,
            "knee_right_max" to kneeRightMax,
            "hip_rom" to hipRom,
            "ldj_knee_left" to ldjKneeLeft,
            "ldj_knee_right" to ldjKneeRight,
            "ldj_hip" to ldjHip
        )
    }
    
    /**
     * Count valid (non-NaN) features.
     */
    fun validCount(): Int {
        return toArray().count { !it.isNaN() }
    }
    
    /**
     * Check if all features are valid.
     */
    fun isComplete(): Boolean {
        return validCount() == 9
    }
    
    companion object {
        /**
         * Create from FloatArray in canonical order.
         */
        fun fromArray(arr: FloatArray): FeatureVector9 {
            require(arr.size == 9) { "Expected 9 features, got ${arr.size}" }
            return FeatureVector9(
                strideAmpNorm = arr[0],
                kneeLeftRom = arr[1],
                kneeRightRom = arr[2],
                kneeLeftMax = arr[3],
                kneeRightMax = arr[4],
                hipRom = arr[5],
                ldjKneeLeft = arr[6],
                ldjKneeRight = arr[7],
                ldjHip = arr[8]
            )
        }
        
        /**
         * Create a vector with all NaN values.
         */
        fun nanVector(): FeatureVector9 {
            return FeatureVector9(
                strideAmpNorm = Float.NaN,
                kneeLeftRom = Float.NaN,
                kneeRightRom = Float.NaN,
                kneeLeftMax = Float.NaN,
                kneeRightMax = Float.NaN,
                hipRom = Float.NaN,
                ldjKneeLeft = Float.NaN,
                ldjKneeRight = Float.NaN,
                ldjHip = Float.NaN
            )
        }
    }
}

