package GaitVision.com.gait

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Signal Processing for 9-Feature Gait Analysis
 * 
 * Changing these algorithms will lead to mismatch with training environment.
 * 
 * Provides:
 * - Global 3σ spike rejection (maskSpikes)
 * - Linear gap interpolation (interpolateGaps)
 * - 5-frame centered moving average smoothing (smoothMovingAverage)
 * - Full processing pipeline (processSignal)
 */
object GaitSignalProcessing9 {
    
    /**
     * Mask outliers using global 3σ rule.
     * 
     * Computes mean and std over all non-NaN values, then masks any value
     * where |x - mean| > sigma_thresh * std.
     * 
     * @param signal Input signal array (may contain NaN)
     * @param sigmaThresh Number of standard deviations for threshold (default 3.0)
     * @return New array with spikes replaced by NaN
     */
    fun maskSpikes(
        signal: FloatArray,
        sigmaThresh: Float = GaitConfig9.SIGMA_THRESH
    ): FloatArray {
        val result = signal.copyOf()
        
        // Get valid (non-NaN) values
        val validValues = result.filter { !it.isNaN() && !it.isInfinite() }
        
        if (validValues.size < 2) {
            return result
        }
        
        // Compute global statistics
        val mean = validValues.average().toFloat()
        val variance = validValues.map { (it - mean) * (it - mean) }.average()
        val std = sqrt(variance).toFloat()
        
        if (std == 0f) {
            return result
        }
        
        val threshold = sigmaThresh * std
        
        // Mask outliers
        for (i in result.indices) {
            if (!result[i].isNaN() && !result[i].isInfinite()) {
                if (abs(result[i] - mean) > threshold) {
                    result[i] = Float.NaN
                }
            }
        }
        
        return result
    }
    
    /**
     * Fill small NaN gaps with linear interpolation.
     * 
     * For each contiguous run of NaN values:
     * - If length <= max_gap_frames AND has valid values before AND after:
     *   Fill with linear interpolation
     * - Otherwise: Leave as NaN
     * 
     * @param signal Input signal array (may contain NaN)
     * @param maxGapTime Maximum gap duration to fill (seconds)
     * @param fps Frame rate
     * @return New array with small gaps filled
     */
    fun interpolateGaps(
        signal: FloatArray,
        maxGapTime: Float = GaitConfig9.MAX_GAP_TIME,
        fps: Float
    ): FloatArray {
        val result = signal.copyOf()
        val maxGapFrames = (maxGapTime * fps).toInt().coerceAtLeast(1)
        
        var i = 0
        while (i < result.size) {
            if (result[i].isNaN() || result[i].isInfinite()) {
                // Find end of this NaN run
                val gapStart = i
                while (i < result.size && (result[i].isNaN() || result[i].isInfinite())) {
                    i++
                }
                val gapEnd = i
                val gapLength = gapEnd - gapStart
                
                // Check if we can interpolate
                if (gapLength <= maxGapFrames) {
                    // Need valid values before and after
                    if (gapStart > 0 && gapEnd < result.size) {
                        val beforeVal = result[gapStart - 1]
                        val afterVal = result[gapEnd]
                        
                        if (!beforeVal.isNaN() && !beforeVal.isInfinite() &&
                            !afterVal.isNaN() && !afterVal.isInfinite()) {
                            // Linear interpolation
                            for (j in gapStart until gapEnd) {
                                val t = (j - gapStart + 1).toFloat() / (gapLength + 1)
                                result[j] = beforeVal + t * (afterVal - beforeVal)
                            }
                        }
                    }
                }
            } else {
                i++
            }
        }
        
        return result
    }
    
    /**
     * Apply centered moving average smoothing.
     * 
     * For frame t:
     *   smoothed[t] = mean(signal[t-half : t+half+1]) ignoring NaNs
     * 
     * Edge handling: Shrinks window at edges.
     * 
     * @param signal Input signal array
     * @param window Window size (should be odd, default 5)
     * @return Smoothed signal array
     */
    fun smoothMovingAverage(
        signal: FloatArray,
        window: Int = GaitConfig9.SMOOTHING_WINDOW
    ): FloatArray {
        val n = signal.size
        if (n < window) {
            return signal.copyOf()
        }
        
        val result = FloatArray(n) { Float.NaN }
        val halfWindow = window / 2
        
        for (i in signal.indices) {
            // Determine window bounds
            val start = maxOf(0, i - halfWindow)
            val end = minOf(n, i + halfWindow + 1)
            
            // Get values in window, excluding NaN
            val windowValues = mutableListOf<Float>()
            for (j in start until end) {
                if (!signal[j].isNaN() && !signal[j].isInfinite()) {
                    windowValues.add(signal[j])
                }
            }
            
            // Compute mean if we have valid values
            if (windowValues.isNotEmpty()) {
                result[i] = windowValues.average().toFloat()
            }
            // else: result[i] stays NaN
        }
        
        return result
    }
    
    /**
     * Apply full preprocessing pipeline to a single signal.
     * 
     * Pipeline order:
     * 1. Spike rejection (3σ rule)
     * 2. Gap interpolation (max 0.15s)
     * 3. Smoothing (5-frame moving average)
     * 
     * @param raw Raw signal array
     * @param fps Frame rate
     * @return Processed signal array
     */
    fun processSignal(raw: FloatArray, fps: Float): FloatArray {
        // Step 1: Spike rejection
        var result = maskSpikes(raw, GaitConfig9.SIGMA_THRESH)
        
        // Step 2: Gap interpolation
        result = interpolateGaps(result, GaitConfig9.MAX_GAP_TIME, fps)
        
        // Step 3: Smoothing
        result = smoothMovingAverage(result, GaitConfig9.SMOOTHING_WINDOW)
        
        return result
    }
    
    /**
     * Apply full preprocessing pipeline with stats tracking.
     * 
     * Same as processSignal() but returns diagnostic stats for export/logging.
     * 
     * @param data Input signal as List<Float>
     * @param fps Frame rate
     * @return CleanedSignalResult9 with cleaned data and stats
     */
    fun cleanSignal(data: List<Float>, fps: Float): CleanedSignalResult9 {
        val raw = data.toFloatArray()
        
        // Step 1: Spike rejection - count spikes
        val afterSpikes = maskSpikes(raw, GaitConfig9.SIGMA_THRESH)
        val spikesRejected = raw.indices.count { i ->
            !raw[i].isNaN() && !raw[i].isInfinite() && afterSpikes[i].isNaN()
        }
        
        // Step 2: Gap interpolation - count filled gaps
        val afterInterp = interpolateGaps(afterSpikes, GaitConfig9.MAX_GAP_TIME, fps)
        val gapsFilled = afterSpikes.indices.count { i ->
            afterSpikes[i].isNaN() && !afterInterp[i].isNaN()
        }
        
        // Step 3: Smoothing
        val smoothed = smoothMovingAverage(afterInterp, GaitConfig9.SMOOTHING_WINDOW)
        
        return CleanedSignalResult9(
            data = smoothed.toList(),
            spikesRejected = spikesRejected,
            gapsFilled = gapsFilled,
            validFrames = smoothed.count { !it.isNaN() && !it.isInfinite() }
        )
    }
}

/**
 * Result of signal cleaning with diagnostic stats.
 */
data class CleanedSignalResult9(
    val data: List<Float>,
    val spikesRejected: Int,
    val gapsFilled: Int,
    val validFrames: Int
)

/**
 * Result container for processed signals.
 */
data class ProcessedSignals9(
    val fps: Float,
    val kneeAngleLeft: FloatArray,
    val kneeAngleRight: FloatArray,
    val hipAngle: FloatArray,
    val interAnkleDist: FloatArray,
    val legLengthLeft: FloatArray,
    val legLengthRight: FloatArray
) {
    /**
     * Process all signals using the canonical pipeline.
     */
    companion object {
        fun fromRaw(
            fps: Float,
            kneeAngleLeft: FloatArray,
            kneeAngleRight: FloatArray,
            hipAngle: FloatArray,
            interAnkleDist: FloatArray,
            legLengthLeft: FloatArray,
            legLengthRight: FloatArray
        ): ProcessedSignals9 {
            return ProcessedSignals9(
                fps = fps,
                kneeAngleLeft = GaitSignalProcessing9.processSignal(kneeAngleLeft, fps),
                kneeAngleRight = GaitSignalProcessing9.processSignal(kneeAngleRight, fps),
                hipAngle = GaitSignalProcessing9.processSignal(hipAngle, fps),
                interAnkleDist = GaitSignalProcessing9.processSignal(interAnkleDist, fps),
                legLengthLeft = GaitSignalProcessing9.processSignal(legLengthLeft, fps),
                legLengthRight = GaitSignalProcessing9.processSignal(legLengthRight, fps)
            )
        }
    }
}

