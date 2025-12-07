package GaitVision.com

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Diagnostic Exporter - Exports raw signals for comparison with PC pipeline
 * 
 * Exports:
 * 1. Raw angle signals (before cleaning)
 * 2. Cleaned signals (after spike rejection, interpolation, smoothing)
 * 3. Extracted features
 * 4. Model inputs/outputs
 * 
 * This allows direct comparison with PC extraction for debugging.
 */
object DiagnosticExporter {
    
    private const val TAG = "DiagnosticExporter"
    
    /**
     * Export complete diagnostic package for a single video analysis
     */
    fun exportDiagnostics(
        context: Context,
        subjectId: String,
        fps: Float,
        // Raw signals (before cleaning)
        rawLeftKnee: List<Float>,
        rawRightKnee: List<Float>,
        rawLeftHip: List<Float>,
        rawRightHip: List<Float>,
        rawInterAnkle: List<Float>,
        rawLegLengths: List<Float>,
        // Cleaned signals (after processing)
        cleanedLeftKnee: List<Float>? = null,
        cleanedRightKnee: List<Float>? = null,
        cleanedHip: List<Float>? = null,
        cleanedInterAnkle: List<Float>? = null,
        cleanedLegLengths: List<Float>? = null,
        // 9 extracted features
        features9: FloatArray? = null,
        // Normalized features (input to model)
        normalizedFeatures: FloatArray? = null,
        // Model output
        reconstructedFeatures: FloatArray? = null,
        reconstructionError: Float? = null,
        gaitScore: Int? = null,
        isImpaired: Boolean? = null,
        // Additional info
        cleaningStats: CleaningStats? = null
    ): File? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val filename = "${subjectId}_diagnostic_${timestamp}.json"
            
            val json = JSONObject()
            
            // Metadata
            json.put("subject_id", subjectId)
            json.put("timestamp", timestamp)
            json.put("fps", fps)
            json.put("platform", "android")
            json.put("model_version", "v3_AE_L4")
            
            // Raw signals - convert NaN to null for JSON compatibility
            fun List<Float>.toJsonArray(): JSONArray {
                val arr = JSONArray()
                this.forEach { v -> 
                    if (v.isNaN() || v.isInfinite()) arr.put(JSONObject.NULL) 
                    else arr.put(v.toDouble())
                }
                return arr
            }
            
            val rawSignals = JSONObject()
            rawSignals.put("left_knee_angles", rawLeftKnee.toJsonArray())
            rawSignals.put("right_knee_angles", rawRightKnee.toJsonArray())
            rawSignals.put("left_hip_angles", rawLeftHip.toJsonArray())
            rawSignals.put("right_hip_angles", rawRightHip.toJsonArray())
            rawSignals.put("inter_ankle_distances", rawInterAnkle.toJsonArray())
            rawSignals.put("leg_lengths", rawLegLengths.toJsonArray())
            rawSignals.put("total_frames", rawLeftKnee.size)
            json.put("raw_signals", rawSignals)
            
            // Cleaned signals
            if (cleanedLeftKnee != null) {
                val cleanedSignals = JSONObject()
                cleanedSignals.put("left_knee", cleanedLeftKnee.toJsonArray())
                cleanedSignals.put("right_knee", (cleanedRightKnee ?: listOf<Float>()).toJsonArray())
                cleanedSignals.put("hip", (cleanedHip ?: listOf<Float>()).toJsonArray())
                cleanedSignals.put("inter_ankle", (cleanedInterAnkle ?: listOf<Float>()).toJsonArray())
                cleanedSignals.put("leg_lengths", (cleanedLegLengths ?: listOf<Float>()).toJsonArray())
                json.put("cleaned_signals", cleanedSignals)
            }
            
            // 9 Features
            if (features9 != null) {
                val featuresJson = JSONObject()
                val featureNames = listOf(
                    "stride_amp_norm", "knee_left_rom", "knee_right_rom",
                    "knee_left_max", "knee_right_max", "hip_rom",
                    "ldj_knee_left", "ldj_knee_right", "ldj_hip"
                )
                for (i in features9.indices) {
                    val value = features9[i]
                    featuresJson.put(featureNames[i], if (value.isNaN()) "NaN" else value)
                }
                featuresJson.put("nan_count", features9.count { it.isNaN() })
                json.put("features_9", featuresJson)
            }
            
            // Model I/O
            if (normalizedFeatures != null) {
                fun FloatArray.toJsonArray(): JSONArray {
                    val arr = JSONArray()
                    this.forEach { v -> 
                        if (v.isNaN() || v.isInfinite()) arr.put(JSONObject.NULL) 
                        else arr.put(v.toDouble())
                    }
                    return arr
                }
                
                val modelJson = JSONObject()
                modelJson.put("normalized_input", normalizedFeatures.toJsonArray())
                if (reconstructedFeatures != null) {
                    modelJson.put("reconstructed_output", reconstructedFeatures.toJsonArray())
                }
                if (reconstructionError != null) {
                    if (reconstructionError.isNaN() || reconstructionError.isInfinite()) {
                        modelJson.put("reconstruction_error", JSONObject.NULL)
                    } else {
                        modelJson.put("reconstruction_error", reconstructionError.toDouble())
                    }
                }
                if (gaitScore != null) {
                    modelJson.put("gait_score", gaitScore)
                }
                if (isImpaired != null) {
                    modelJson.put("is_impaired", isImpaired)
                }
                json.put("model_output", modelJson)
            }
            
            // Cleaning stats
            if (cleaningStats != null) {
                val statsJson = JSONObject()
                statsJson.put("spikes_rejected", cleaningStats.totalSpikesRejected)
                statsJson.put("gaps_filled", cleaningStats.totalGapsFilled)
                statsJson.put("spike_pct", cleaningStats.spikePct)
                statsJson.put("interpolation_pct", cleaningStats.interpolationPct)
                statsJson.put("left_knee_valid", cleaningStats.leftKneeValid)
                statsJson.put("right_knee_valid", cleaningStats.rightKneeValid)
                statsJson.put("left_hip_valid", cleaningStats.leftHipValid)
                json.put("cleaning_stats", statsJson)
            }
            
            // Signal statistics for quick debugging
            fun List<Float>.validMin(): Any = this.filter { !it.isNaN() }.minOrNull() ?: JSONObject.NULL
            fun List<Float>.validMax(): Any = this.filter { !it.isNaN() }.maxOrNull() ?: JSONObject.NULL
            
            val signalStats = JSONObject()
            signalStats.put("left_knee_min", rawLeftKnee.validMin())
            signalStats.put("left_knee_max", rawLeftKnee.validMax())
            signalStats.put("left_knee_count", rawLeftKnee.size)
            signalStats.put("left_knee_nan_count", rawLeftKnee.count { it.isNaN() })
            signalStats.put("right_knee_min", rawRightKnee.validMin())
            signalStats.put("right_knee_max", rawRightKnee.validMax())
            signalStats.put("inter_ankle_min", rawInterAnkle.validMin())
            signalStats.put("inter_ankle_max", rawInterAnkle.validMax())
            json.put("signal_stats", signalStats)
            
            // Write to Downloads
            val fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs()
            }
            val outputFile = File(fileDirectory, filename)
            
            FileOutputStream(outputFile).use { output ->
                output.write(json.toString(2).toByteArray())
            }
            
            MediaScannerConnection.scanFile(context, arrayOf(outputFile.absolutePath), null, null)
            
            Log.d(TAG, "Diagnostic export saved: ${outputFile.absolutePath}")
            outputFile
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export diagnostics: ${e.message}", e)
            null
        }
    }
    
    /**
     * Export raw signals to CSV for easy comparison in spreadsheet
     */
    fun exportSignalsCsv(
        context: Context,
        subjectId: String,
        leftKnee: List<Float>,
        rightKnee: List<Float>,
        leftHip: List<Float>,
        rightHip: List<Float>,
        interAnkle: List<Float>,
        legLengths: List<Float>
    ): File? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val filename = "${subjectId}_signals_${timestamp}.csv"
            
            val maxLen = maxOf(
                leftKnee.size, rightKnee.size, leftHip.size,
                rightHip.size, interAnkle.size, legLengths.size
            )
            
            val fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs()
            }
            val outputFile = File(fileDirectory, filename)
            
            FileOutputStream(outputFile).use { output ->
                // Header
                output.write("frame,left_knee,right_knee,left_hip,right_hip,inter_ankle,leg_length\n".toByteArray())
                
                for (i in 0 until maxLen) {
                    val row = listOf(
                        i.toString(),
                        leftKnee.getOrNull(i)?.toString() ?: "",
                        rightKnee.getOrNull(i)?.toString() ?: "",
                        leftHip.getOrNull(i)?.toString() ?: "",
                        rightHip.getOrNull(i)?.toString() ?: "",
                        interAnkle.getOrNull(i)?.toString() ?: "",
                        legLengths.getOrNull(i)?.toString() ?: ""
                    ).joinToString(",")
                    output.write("$row\n".toByteArray())
                }
            }
            
            MediaScannerConnection.scanFile(context, arrayOf(outputFile.absolutePath), null, null)
            
            Log.d(TAG, "Signals CSV saved: ${outputFile.absolutePath}")
            outputFile
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export signals CSV: ${e.message}", e)
            null
        }
    }
}

