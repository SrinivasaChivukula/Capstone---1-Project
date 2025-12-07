package GaitVision.com.gait

import kotlin.math.ln

/**
 * Log Dimensionless Jerk (LDJ) Computation
 * 
 * Implements smoothness metric using central differences for derivatives.
 * Changing these values will lead to mismatch with training environment.
 * 
 * Formula:
 *   1. Compute velocity, acceleration, jerk using central differences
 *   2. J = Σ(jerk²) * dt  (squared jerk integral)
 *   3. A = P95(theta) - P05(theta)  (amplitude using percentiles)
 *   4. T = N * dt  (duration)
 *   5. dimensionless_jerk = (T⁵ / A²) * J
 *   6. LDJ = -ln(dimensionless_jerk)
 * 
 * More negative LDJ = smoother movement.
 */
object GaitLDJ9 {
    
    /**
     * Compute velocity using central differences.
     * 
     * For interior indices i = 1..N-2:
     *   vel[i] = (signal[i+1] - signal[i-1]) / (2*dt)
     * 
     * Forward/backward differences at edges.
     * 
     * @param signal Input signal array (no NaN values)
     * @param dt Time step between samples
     * @return Velocity array (same length as input)
     */
    fun computeVelocityCentral(signal: FloatArray, dt: Float): FloatArray {
        val n = signal.size
        if (n < 2) {
            return floatArrayOf()
        }
        
        val vel = FloatArray(n)
        
        // Forward difference at start
        vel[0] = (signal[1] - signal[0]) / dt
        
        // Central differences for interior
        for (i in 1 until n - 1) {
            vel[i] = (signal[i + 1] - signal[i - 1]) / (2 * dt)
        }
        
        // Backward difference at end
        vel[n - 1] = (signal[n - 1] - signal[n - 2]) / dt
        
        return vel
    }
    
    /**
     * Compute jerk (3rd derivative) using successive central differences.
     * 
     * @param signal Input signal array (no NaN values)
     * @param dt Time step between samples
     * @return Jerk array
     */
    fun computeJerkCentral(signal: FloatArray, dt: Float): FloatArray {
        val vel = computeVelocityCentral(signal, dt)
        val acc = computeVelocityCentral(vel, dt)
        val jerk = computeVelocityCentral(acc, dt)
        return jerk
    }
    
    /**
     * Compute percentile ignoring NaN values.
     * Uses linear interpolation (NumPy default method).
     * 
     * @param arr Input array (may contain NaN)
     * @param percentile Percentile value [0, 100]
     * @return Percentile value, or NaN if no valid data
     */
    fun nanPercentile(arr: FloatArray, percentile: Float): Float {
        // Filter out NaN and Inf values, then sort
        val valid = arr.filter { !it.isNaN() && !it.isInfinite() }.sorted()
        
        if (valid.isEmpty()) {
            return Float.NaN
        }
        
        if (valid.size == 1) {
            return valid[0]
        }
        
        // Linear interpolation (matches NumPy default)
        val p = percentile / 100f
        val index = p * (valid.size - 1)
        val lowerIdx = index.toInt()
        val upperIdx = (lowerIdx + 1).coerceAtMost(valid.size - 1)
        val fraction = index - lowerIdx
        
        return valid[lowerIdx] + fraction * (valid[upperIdx] - valid[lowerIdx])
    }
    
    /**
     * Compute Log Dimensionless Jerk for an angle time series.
     * 
     * Steps:
     * 1. Remove NaN values
     * 2. Ensure at least minFrames valid samples
     * 3. Compute derivatives using central differences
     * 4. Compute squared jerk integral
     * 5. Compute amplitude using P95 - P05
     * 6. Compute duration
     * 7. Compute dimensionless jerk and take negative log
     * 
     * @param theta Angle time series (degrees), may contain NaN
     * @param fps Frame rate
     * @param minFrames Minimum required valid frames (default 30)
     * @return LDJ value (more negative = smoother), or NaN if insufficient data
     */
    fun computeLdj(
        theta: FloatArray,
        fps: Float,
        minFrames: Int = GaitConfig9.LDJ_MIN_FRAMES
    ): Float {
        // Step 1: Remove NaN values
        val validTheta = theta.filter { !it.isNaN() && !it.isInfinite() }.toFloatArray()
        
        // Step 2: Check minimum frames
        val n = validTheta.size
        if (n < minFrames) {
            return Float.NaN
        }
        
        // Step 3: Time step
        val dt = 1.0f / fps
        
        // Step 4: Compute derivatives using central differences
        val jerk = computeJerkCentral(validTheta, dt)
        
        // Filter out edge effects - use central portion of jerk
        val jerkCentral = if (jerk.size > 6) {
            jerk.slice(2 until jerk.size - 2).toFloatArray()
        } else {
            jerk
        }
        
        if (jerkCentral.isEmpty()) {
            return Float.NaN
        }
        
        // Step 5: Squared jerk integral
        // J = Σ(jerk²) * dt
        var J = 0.0
        for (j in jerkCentral) {
            J += j.toDouble() * j.toDouble()
        }
        J *= dt
        
        // Step 6: Amplitude using percentiles
        val p95 = nanPercentile(validTheta, GaitConfig9.PERCENTILE_HIGH.toFloat())
        val p05 = nanPercentile(validTheta, GaitConfig9.PERCENTILE_LOW.toFloat())
        val A = p95 - p05
        
        if (A <= 0f || A.isNaN()) {
            return Float.NaN
        }
        
        // Step 7: Duration
        val T = n * dt.toDouble()
        
        // Step 8: Dimensionless jerk
        // dimensionless_jerk = (T^5 / A^2) * J
        val T5 = T * T * T * T * T
        val A2 = A.toDouble() * A.toDouble()
        val dimensionlessJerk = (T5 / A2) * J
        
        if (dimensionlessJerk <= 0) {
            return Float.NaN
        }
        
        // Step 9: Log dimensionless jerk
        val ldj = -ln(dimensionlessJerk)
        
        return ldj.toFloat()
    }
    
    /**
     * Compute Range of Motion (ROM) as P95 - P05.
     * 
     * @param signal Angle time series (may contain NaN)
     * @return ROM in same units as input, or NaN if insufficient data
     */
    fun computeRom(
        signal: FloatArray,
        percentileLow: Float = GaitConfig9.PERCENTILE_LOW.toFloat(),
        percentileHigh: Float = GaitConfig9.PERCENTILE_HIGH.toFloat()
    ): Float {
        val pLow = nanPercentile(signal, percentileLow)
        val pHigh = nanPercentile(signal, percentileHigh)
        
        if (pLow.isNaN() || pHigh.isNaN()) {
            return Float.NaN
        }
        
        return pHigh - pLow
    }
    
    /**
     * Compute robust maximum as P95 (to avoid spikes).
     * 
     * @param signal Angle time series (may contain NaN)
     * @return P95 value, or NaN if insufficient data
     */
    fun computeMax(
        signal: FloatArray,
        percentile: Float = GaitConfig9.PERCENTILE_HIGH.toFloat()
    ): Float {
        return nanPercentile(signal, percentile)
    }
}

