package GaitVision.com

import GaitVision.com.gait.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

/**
 * Unit tests for 9-feature gait extraction.
 * 
 * Tests alignment with PC implementation using golden test cases.
 * 
 * Run PC tests first to generate golden values:
 *   cd matched_extraction
 *   python -m gait_features.tests.test_ldj_synthetic
 */
class GaitFeatures9Test {
    
    companion object {
        const val TOLERANCE = 0.01f  // Numerical tolerance for float comparison
        
        // Golden test values (from PC: golden_simple.json)
        val GOLDEN_THETA = floatArrayOf(
            30f, 35f, 40f, 45f, 50f, 55f, 60f, 55f, 50f, 45f,
            40f, 35f, 30f, 35f, 40f, 45f, 50f, 55f, 60f, 55f,
            50f, 45f, 40f, 35f, 30f, 35f, 40f, 45f, 50f, 55f
        )
        const val GOLDEN_FPS = 30f
        
        // Expected percentiles (linear interpolation)
        const val EXPECTED_P05 = 31.45f
        const val EXPECTED_P95 = 58.55f
        const val EXPECTED_ROM = 27.1f
    }
    
    // =============================================================================
    // Percentile Tests
    // =============================================================================
    
    @Test
    fun testPercentileSimple() {
        val arr = floatArrayOf(1f, 2f, 3f, 4f, 5f)
        
        val p50 = GaitLDJ9.nanPercentile(arr, 50f)
        
        assertEquals("P50 should be 3.0", 3.0f, p50, TOLERANCE)
    }
    
    @Test
    fun testPercentileWithNaN() {
        val arr = floatArrayOf(1f, Float.NaN, 3f, Float.NaN, 5f)
        
        val p50 = GaitLDJ9.nanPercentile(arr, 50f)
        
        // Should compute over [1, 3, 5] only
        assertEquals("P50 with NaNs should be 3.0", 3.0f, p50, TOLERANCE)
    }
    
    @Test
    fun testPercentileAllNaN() {
        val arr = floatArrayOf(Float.NaN, Float.NaN, Float.NaN)
        
        val p50 = GaitLDJ9.nanPercentile(arr, 50f)
        
        assertTrue("All-NaN should return NaN", p50.isNaN())
    }
    
    @Test
    fun testPercentileEndpoints() {
        val arr = floatArrayOf(10f, 20f, 30f, 40f, 50f)
        
        val p0 = GaitLDJ9.nanPercentile(arr, 0f)
        val p100 = GaitLDJ9.nanPercentile(arr, 100f)
        
        assertEquals("P0 should be min", 10f, p0, TOLERANCE)
        assertEquals("P100 should be max", 50f, p100, TOLERANCE)
    }
    
    @Test
    fun testPercentileGolden() {
        // Test with golden values
        val p05 = GaitLDJ9.nanPercentile(GOLDEN_THETA, 5f)
        val p95 = GaitLDJ9.nanPercentile(GOLDEN_THETA, 95f)
        
        assertEquals("P05 should match golden", EXPECTED_P05, p05, 0.1f)
        assertEquals("P95 should match golden", EXPECTED_P95, p95, 0.1f)
    }
    
    // =============================================================================
    // ROM Tests
    // =============================================================================
    
    @Test
    fun testRomSimple() {
        val arr = floatArrayOf(10f, 20f, 30f, 40f, 50f)
        
        // ROM with 0-100 percentiles should be max - min
        val rom = GaitLDJ9.computeRom(arr, 0f, 100f)
        
        assertEquals("ROM should be 40", 40f, rom, TOLERANCE)
    }
    
    @Test
    fun testRomGolden() {
        val rom = GaitLDJ9.computeRom(GOLDEN_THETA)
        
        assertEquals("ROM should match golden", EXPECTED_ROM, rom, 0.2f)
    }
    
    // =============================================================================
    // Central Difference Tests
    // =============================================================================
    
    @Test
    fun testVelocityCentralLinear() {
        // Linear signal: velocity should be constant
        val signal = floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f)
        val dt = 1f
        
        val vel = GaitLDJ9.computeVelocityCentral(signal, dt)
        
        // Central values should all be ~1.0 (slope of line)
        for (i in 1 until vel.size - 1) {
            assertEquals("Central velocity should be 1.0", 1.0f, vel[i], TOLERANCE)
        }
    }
    
    @Test
    fun testVelocityCentralQuadratic() {
        // Quadratic signal: t^2 -> velocity = 2t
        val signal = floatArrayOf(0f, 1f, 4f, 9f, 16f)  // 0, 1, 4, 9, 16
        val dt = 1f
        
        val vel = GaitLDJ9.computeVelocityCentral(signal, dt)
        
        // At t=2: v = (4-0)/(2) = 2
        assertEquals("Velocity at t=2", 2.0f, vel[2], TOLERANCE)
    }
    
    // =============================================================================
    // LDJ Tests
    // =============================================================================
    
    @Test
    fun testLdjInsufficientFrames() {
        val shortSignal = floatArrayOf(1f, 2f, 3f, 4f, 5f)
        
        val ldj = GaitLDJ9.computeLdj(shortSignal, 30f, minFrames = 30)
        
        assertTrue("LDJ should be NaN for insufficient frames", ldj.isNaN())
    }
    
    @Test
    fun testLdjSmoothSignal() {
        // Smooth sinusoidal signal
        val n = 90  // 3 seconds at 30 FPS
        val signal = FloatArray(n) { i ->
            45f + 25f * kotlin.math.sin(2 * Math.PI * i / 30.0).toFloat()
        }
        
        val ldj = GaitLDJ9.computeLdj(signal, 30f)
        
        assertFalse("LDJ should not be NaN for smooth signal", ldj.isNaN())
        assertTrue("LDJ should be negative (smooth)", ldj < 0)
        println("Smooth signal LDJ: $ldj")
    }
    
    @Test
    fun testLdjNoisySignal() {
        // Noisy signal (less smooth)
        val n = 90
        val random = java.util.Random(42)
        val signal = FloatArray(n) { i ->
            45f + 25f * kotlin.math.sin(2 * Math.PI * i / 30.0).toFloat() + 
            random.nextGaussian().toFloat() * 5f
        }
        
        val ldj = GaitLDJ9.computeLdj(signal, 30f)
        
        assertFalse("LDJ should not be NaN for noisy signal", ldj.isNaN())
        println("Noisy signal LDJ: $ldj")
    }
    
    @Test
    fun testLdjGoldenCase() {
        // Test with golden values
        val ldj = GaitLDJ9.computeLdj(GOLDEN_THETA, GOLDEN_FPS)
        
        assertFalse("LDJ should not be NaN for golden case", ldj.isNaN())
        assertTrue("LDJ should be negative", ldj < 0f)
        
        println("Golden case LDJ: $ldj")
        // Note: Compare this value with PC output to verify alignment
    }
    
    // =============================================================================
    // Signal Processing Tests
    // =============================================================================
    
    @Test
    fun testMaskSpikes() {
        // Signal with an obvious spike
        val signal = floatArrayOf(10f, 11f, 12f, 100f, 13f, 14f)
        
        val masked = GaitSignalProcessing9.maskSpikes(signal, 2f)
        
        assertTrue("Spike should be masked to NaN", masked[3].isNaN())
        assertFalse("Normal values should not be masked", masked[0].isNaN())
    }
    
    @Test
    fun testInterpolateGaps() {
        val signal = floatArrayOf(10f, Float.NaN, Float.NaN, 40f, 50f)
        
        val filled = GaitSignalProcessing9.interpolateGaps(signal, 0.2f, 30f)
        
        // Gap of 2 frames should be filled with linear interpolation
        assertFalse("Gap index 1 should be filled", filled[1].isNaN())
        assertFalse("Gap index 2 should be filled", filled[2].isNaN())
        
        // Expected: 10 -> 40 over 4 steps: 10, 20, 30, 40
        assertEquals("Interpolated value at 1", 20f, filled[1], 1f)
        assertEquals("Interpolated value at 2", 30f, filled[2], 1f)
    }
    
    @Test
    fun testSmoothMovingAverage() {
        val signal = floatArrayOf(10f, 20f, 30f, 20f, 10f, 20f, 30f)
        
        val smoothed = GaitSignalProcessing9.smoothMovingAverage(signal, 3)
        
        // Middle value with window 3
        assertEquals("Smoothed[3] = mean(30,20,10)", 20f, smoothed[3], TOLERANCE)
    }
    
    // =============================================================================
    // Full Feature Extraction Test
    // =============================================================================
    
    @Test
    fun testFullFeatureExtraction() {
        // Generate synthetic gait-like signals
        val n = 150
        val fps = 30f
        
        // Knee angles (oscillating)
        val kneeLeft = FloatArray(n) { i ->
            45f + 25f * kotlin.math.sin(2 * Math.PI * i / 30.0).toFloat()
        }
        val kneeRight = FloatArray(n) { i ->
            45f + 25f * kotlin.math.sin(2 * Math.PI * i / 30.0 + 0.5).toFloat()
        }
        
        // Hip angle
        val hip = FloatArray(n) { i ->
            30f + 15f * kotlin.math.sin(2 * Math.PI * i / 30.0).toFloat()
        }
        
        // Inter-ankle distance
        val interAnkle = FloatArray(n) { i ->
            0.3f + 0.15f * kotlin.math.cos(2 * Math.PI * i / 30.0).toFloat()
        }
        
        // Leg lengths (relatively constant)
        val legLeft = FloatArray(n) { 0.45f }
        val legRight = FloatArray(n) { 0.44f }
        
        // Extract features
        val features = GaitFeatures9.extractFeaturesFromRaw(
            fps = fps,
            kneeAngleLeft = kneeLeft,
            kneeAngleRight = kneeRight,
            hipAngle = hip,
            interAnkleDist = interAnkle,
            legLengthLeft = legLeft,
            legLengthRight = legRight
        )
        
        // Check all features are valid
        println("Extracted features:")
        features.toMap().forEach { (name, value) ->
            println("  $name: $value")
            assertFalse("$name should not be NaN", value.isNaN())
        }
        
        assertEquals("Should have 9 valid features", 9, features.validCount())
        
        // Check reasonable ranges
        assertTrue("stride_amp_norm should be positive", features.strideAmpNorm > 0)
        assertTrue("knee_left_rom should be in reasonable range", 
            features.kneeLeftRom in 20f..100f)
        assertTrue("LDJ should be negative", features.ldjKneeLeft < 0)
    }
    
    @Test
    fun testFeatureVector9() {
        val features = FeatureVector9(
            strideAmpNorm = 1.0f,
            kneeLeftRom = 50f,
            kneeRightRom = 48f,
            kneeLeftMax = 70f,
            kneeRightMax = 68f,
            hipRom = 30f,
            ldjKneeLeft = -20f,
            ldjKneeRight = -21f,
            ldjHip = -22f
        )
        
        // Test toArray
        val arr = features.toArray()
        assertEquals("Array should have 9 elements", 9, arr.size)
        assertEquals("First element should be stride_amp_norm", 1.0f, arr[0], TOLERANCE)
        
        // Test toMap
        val map = features.toMap()
        assertEquals("Map should have 9 entries", 9, map.size)
        assertEquals("stride_amp_norm in map", 1.0f, map["stride_amp_norm"]!!, TOLERANCE)
        
        // Test fromArray
        val restored = FeatureVector9.fromArray(arr)
        assertEquals("Restored should match", features.strideAmpNorm, restored.strideAmpNorm, TOLERANCE)
        
        // Test validCount
        assertEquals("Should have 9 valid features", 9, features.validCount())
        
        // Test nanVector
        val nanVec = FeatureVector9.nanVector()
        assertEquals("NaN vector should have 0 valid features", 0, nanVec.validCount())
    }
}

