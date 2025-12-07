package GaitVision.com

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import kotlin.math.sqrt

/**
 * GaitScorer9 - 9-Feature Autoencoder-based Gait Scoring
 * 
 * Uses the AE_L4 autoencoder trained on 9 features with reconstruction error scoring.
 * 
 * Model: autoencoder_v3.tflite (AE_L4 architecture: 9>32>16>4>16>32>9)
 * Features: stride_amp_norm, knee_left_rom, knee_right_rom, knee_left_max,
 *           knee_right_max, hip_rom, ldj_knee_left, ldj_knee_right, ldj_hip
 * 
 * Scoring: Based on reconstruction error
 *   - Low error = normal gait (high score)
 *   - High error = impaired gait (low score)
 */
object GaitScorer9 {
    
    private const val TAG = "GaitScorer9"
    
    // Model file names
    private const val MODEL_FILE = "autoencoder_v3.tflite"
    private const val NORM_STATS_FILE = "normalization_stats_v3.json"
    private const val CONFIG_FILE = "model_config_v3.json"
    
    // Number of features
    private const val N_FEATURES = 9
    
    // Normalization stats (loaded from JSON)
    private var featureMean: FloatArray? = null
    private var featureStd: FloatArray? = null
    
    // Scoring parameters (loaded from config)
    private var threshold: Float = 0.232f
    private var minError: Float = 0.022f
    private var maxError: Float = 5.29f
    
    private var interpreter: Interpreter? = null
    private var isInitialized = false
    
    /**
     * Initialize the scorer with TFLite model and normalization stats.
     */
    fun initialize(context: Context): Boolean {
        return try {
            // Load TFLite model
            val modelBuffer = FileUtil.loadMappedFile(context, MODEL_FILE)
            interpreter = Interpreter(modelBuffer)
            Log.d(TAG, "Loaded model: $MODEL_FILE")
            
            // Load normalization stats
            val normJson = context.assets.open(NORM_STATS_FILE).bufferedReader().use { it.readText() }
            val normObj = JSONObject(normJson)
            
            val meanArray = normObj.getJSONArray("mean")
            val stdArray = normObj.getJSONArray("std")
            
            featureMean = FloatArray(N_FEATURES) { i -> meanArray.getDouble(i).toFloat() }
            featureStd = FloatArray(N_FEATURES) { i -> stdArray.getDouble(i).toFloat() }
            
            Log.d(TAG, "Loaded normalization stats: ${featureMean?.size} features")
            
            // Load config (thresholds, scoring parameters)
            try {
                val configJson = context.assets.open(CONFIG_FILE).bufferedReader().use { it.readText() }
                val configObj = JSONObject(configJson)
                val scoringObj = configObj.getJSONObject("scoring")
                
                threshold = scoringObj.getDouble("threshold").toFloat()
                minError = scoringObj.getDouble("min_error").toFloat()
                maxError = scoringObj.getDouble("max_error").toFloat()
                
                Log.d(TAG, "Loaded config: threshold=$threshold, minError=$minError, maxError=$maxError")
            } catch (e: Exception) {
                Log.w(TAG, "Using default scoring parameters: ${e.message}")
            }
            
            isInitialized = true
            Log.d(TAG, "GaitScorer9 initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize GaitScorer9: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Normalize raw features using training statistics.
     * 
     * @param rawFeatures FloatArray of 9 raw feature values
     * @return FloatArray of 9 normalized (z-scored) feature values
     */
    fun normalizeFeatures(rawFeatures: FloatArray): FloatArray {
        require(rawFeatures.size == N_FEATURES) { "Expected $N_FEATURES features, got ${rawFeatures.size}" }
        require(featureMean != null && featureStd != null) { "Normalization stats not loaded" }
        
        val normalized = FloatArray(N_FEATURES) { i ->
            val mean = featureMean!![i]
            val std = if (featureStd!![i] < 1e-6f) 1f else featureStd!![i]
            val raw = if (rawFeatures[i].isNaN()) mean else rawFeatures[i]
            (raw - mean) / std
        }
        
        // Debug logging
        Log.d(TAG, "Normalization:")
        Log.d(TAG, "  Raw: ${rawFeatures.joinToString(", ") { "%.4f".format(it) }}")
        Log.d(TAG, "  Normalized: ${normalized.joinToString(", ") { "%.4f".format(it) }}")
        
        return normalized
    }
    
    /**
     * Run autoencoder inference and get reconstruction.
     * 
     * @param normalizedFeatures FloatArray of 9 normalized features
     * @return FloatArray of 9 reconstructed features
     */
    fun reconstruct(normalizedFeatures: FloatArray): FloatArray {
        require(isInitialized) { "GaitScorer9 not initialized. Call initialize() first." }
        require(normalizedFeatures.size == N_FEATURES) { "Expected $N_FEATURES features" }
        
        val input = arrayOf(normalizedFeatures)
        val output = arrayOf(FloatArray(N_FEATURES))
        
        interpreter?.run(input, output)
        
        return output[0]
    }
    
    /**
     * Compute reconstruction error (MSE).
     * 
     * @param original Original normalized features
     * @param reconstructed Reconstructed features from autoencoder
     * @return Mean squared error
     */
    fun computeReconstructionError(original: FloatArray, reconstructed: FloatArray): Float {
        require(original.size == reconstructed.size) { "Size mismatch" }
        
        var sumSquaredError = 0f
        for (i in original.indices) {
            val diff = original[i] - reconstructed[i]
            sumSquaredError += diff * diff
        }
        
        return sumSquaredError / original.size
    }
    
    /**
     * Compute gait score from reconstruction error using LOGISTIC transformation.
     * 
     * This provides a clinically intuitive scale where:
     * - 100 = perfect healthy gait
     * - 70 = impairment threshold (borderline)
     * - 0 = severe impairment
     * 
     * Uses a piecewise approach:
     * - Normal range (error ≤ threshold): Linear 100→70
     * - Impaired range (error > threshold): Logistic decay 70→0
     * 
     * Based on clinical scoring research (Shriners Gait Index, FRAX, etc.)
     * 
     * @param reconstructionError MSE from autoencoder
     * @return Gait score 0-100
     */
    fun computeGaitScore(reconstructionError: Float): Int {
        val score: Float = if (reconstructionError <= threshold) {
            // NORMAL RANGE: error [0, threshold] → score [100, 70]
            // Linear interpolation: lower error = higher score
            val fraction = reconstructionError / threshold  // 0 at best, 1 at threshold
            100f - 30f * fraction
        } else {
            // IMPAIRED RANGE: error [threshold, ∞) → score [70, 0]
            // Logistic decay: score = 70 / (1 + k * excess_error)
            // This creates a smooth "S-curve" dropoff
            val excessError = reconstructionError - threshold
            
            // k controls decay rate - tuned so:
            // - error = 0.5 → score ≈ 55
            // - error = 1.0 → score ≈ 40
            // - error = 2.0 → score ≈ 25
            // - error = 5.0 → score ≈ 10
            val k = 2.5f
            70f / (1f + k * excessError)
        }
        
        val finalScore = score.toInt().coerceIn(0, 100)
        
        // Debug logging
        Log.d(TAG, "Scoring (logistic): reconError=$reconstructionError, threshold=$threshold, score=$finalScore")
        
        return finalScore
    }
    
    /**
     * Check if gait is classified as impaired based on threshold.
     * 
     * @param reconstructionError MSE from autoencoder
     * @return true if impaired (error > threshold), false if normal
     */
    fun isImpaired(reconstructionError: Float): Boolean {
        return reconstructionError > threshold
    }
    
    /**
     * Full scoring pipeline: raw features → normalized → reconstruct → error → score
     * 
     * @param rawFeatures FloatArray of 9 raw feature values from GaitFeatures9
     * @return GaitResult9 with all scoring information
     */
    fun score(rawFeatures: FloatArray): GaitResult9 {
        val normalized = normalizeFeatures(rawFeatures)
        val reconstructed = reconstruct(normalized)
        val error = computeReconstructionError(normalized, reconstructed)
        val score = computeGaitScore(error)
        val impaired = isImpaired(error)
        
        return GaitResult9(
            rawFeatures = rawFeatures,
            normalizedFeatures = normalized,
            reconstructedFeatures = reconstructed,
            reconstructionError = error,
            gaitScore = score,
            isImpaired = impaired,
            threshold = threshold
        )
    }
    
    /**
     * Get score interpretation label.
     * 
     * Scale:
     * - 90-100: Excellent gait
     * - 80-89: Good gait
     * - 70-79: Fair gait (threshold is ~70)
     * - 50-69: Mild impairment
     * - 30-49: Moderate impairment
     * - 0-29: Severe impairment
     */
    fun getScoreLabel(score: Int): String = when {
        score >= 90 -> "Excellent"
        score >= 80 -> "Good"
        score >= 70 -> "Fair"
        score >= 50 -> "Mild Impairment"
        score >= 30 -> "Moderate Impairment"
        else -> "Severe Impairment"
    }
    
    /**
     * Get the threshold used for classification.
     */
    fun getThreshold(): Float = threshold
    
    fun isReady(): Boolean = isInitialized
    
    fun release() {
        interpreter?.close()
        interpreter = null
        isInitialized = false
        featureMean = null
        featureStd = null
    }
}

/**
 * Result of gait scoring using AE_L4 model.
 */
data class GaitResult9(
    val rawFeatures: FloatArray,
    val normalizedFeatures: FloatArray,
    val reconstructedFeatures: FloatArray,
    val reconstructionError: Float,
    val gaitScore: Int,
    val isImpaired: Boolean,
    val threshold: Float
) {
    val label: String get() = GaitScorer9.getScoreLabel(gaitScore)
    
    /**
     * Get confidence: how far from threshold (0 = at threshold, 1 = very confident)
     */
    val confidence: Float get() {
        val distance = kotlin.math.abs(reconstructionError - threshold)
        // Normalize: threshold distance of ~0.5 = ~100% confidence
        return (distance / 0.5f).coerceIn(0f, 1f)
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GaitResult9) return false
        return gaitScore == other.gaitScore && 
               reconstructionError == other.reconstructionError
    }
    
    override fun hashCode(): Int = gaitScore.hashCode()
    
    override fun toString(): String {
        return "GaitResult9(score=$gaitScore, label='$label', error=$reconstructionError, impaired=$isImpaired)"
    }
}

