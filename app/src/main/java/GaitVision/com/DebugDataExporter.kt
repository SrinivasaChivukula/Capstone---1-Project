package GaitVision.com

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import GaitVision.com.CleaningStats
import GaitVision.com.gait.CleanedSignalResult9

/**
 * Debug Data Exporter for Android
 * 
 * Exports extraction data in CSV format for cross-platform comparison.
 * Updated to support 9-feature format (AE_L4).
 */
object DebugDataExporter {
    
    private const val TAG = "DebugDataExporter"
    
    // 9-feature names in canonical order
    val FEATURE_NAMES_9 = listOf(
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
    
    /**
     * Export debug data to CSV file.
     * 
     * @param context Android context
     * @param leftKneeAngles Raw left knee angles (before cleaning)
     * @param rightKneeAngles Raw right knee angles (before cleaning)
     * @param leftHipAngles Raw left hip angles (before cleaning)
     * @param interAnkleDistances Raw inter-ankle distances (before cleaning)
     * @param legLengths Raw leg lengths (before cleaning)
     * @param leftKneeCleaned Cleaned left knee angles
     * @param rightKneeCleaned Cleaned right knee angles
     * @param leftHipCleaned Cleaned left hip angles
     * @param interAnkleCleaned Cleaned inter-ankle distances
     * @param legLengthsCleaned Cleaned leg lengths
     * @param rawFeatures Final 9 feature values
     * @param fps Video frame rate
     * @param cleaningStats Signal cleaning statistics
     * @return File path of exported CSV, or null on error
     */
    fun exportDebugData(
        context: Context,
        leftKneeAngles: List<Float>,
        rightKneeAngles: List<Float>,
        leftHipAngles: List<Float>,
        interAnkleDistances: List<Float>,
        legLengths: List<Float>,
        leftKneeCleaned: CleanedSignalResult9,
        rightKneeCleaned: CleanedSignalResult9,
        leftHipCleaned: CleanedSignalResult9,
        interAnkleCleaned: CleanedSignalResult9,
        legLengthsCleaned: CleanedSignalResult9,
        rawFeatures: FloatArray,
        fps: Float,
        cleaningStats: CleaningStats? = null
    ): String? {
        try {
            // Use public Downloads directory for easy access (same as regular CSV export)
            val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            
            // Generate filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val outputFile = File(outputDir, "debug_android_$timestamp.csv")
            
            FileWriter(outputFile).use { writer ->
                // Write header
                writer.append("frame_index,timestamp_sec,confidence,")
                writer.append("left_knee_angle_raw,right_knee_angle_raw,left_hip_angle_raw,")
                writer.append("inter_ankle_dist_raw,leg_length_raw,")
                writer.append("left_knee_angle_cleaned,right_knee_angle_cleaned,left_hip_angle_cleaned,")
                writer.append("inter_ankle_dist_cleaned,leg_length_cleaned\n")
                
                // Determine max length
                val maxFrames = maxOf(
                    leftKneeAngles.size,
                    rightKneeAngles.size,
                    leftHipAngles.size,
                    interAnkleDistances.size,
                    legLengths.size,
                    leftKneeCleaned.data.size,
                    rightKneeCleaned.data.size,
                    leftHipCleaned.data.size,
                    interAnkleCleaned.data.size,
                    legLengthsCleaned.data.size
                )
                
                // Write per-frame data
                for (i in 0 until maxFrames) {
                    val timestampSec = i / fps
                    
                    // Raw signals
                    val lkRaw = if (i < leftKneeAngles.size) leftKneeAngles[i] else Float.NaN
                    val rkRaw = if (i < rightKneeAngles.size) rightKneeAngles[i] else Float.NaN
                    val lhRaw = if (i < leftHipAngles.size) leftHipAngles[i] else Float.NaN
                    val iaRaw = if (i < interAnkleDistances.size) interAnkleDistances[i] else Float.NaN
                    val llRaw = if (i < legLengths.size) legLengths[i] else Float.NaN
                    
                    // Cleaned signals
                    val lkClean = if (i < leftKneeCleaned.data.size) leftKneeCleaned.data[i] else Float.NaN
                    val rkClean = if (i < rightKneeCleaned.data.size) rightKneeCleaned.data[i] else Float.NaN
                    val lhClean = if (i < leftHipCleaned.data.size) leftHipCleaned.data[i] else Float.NaN
                    val iaClean = if (i < interAnkleCleaned.data.size) interAnkleCleaned.data[i] else Float.NaN
                    val llClean = if (i < legLengthsCleaned.data.size) legLengthsCleaned.data[i] else Float.NaN
                    
                    writer.append("$i,$timestampSec,1.0,")
                    writer.append("${formatFloat(lkRaw)},${formatFloat(rkRaw)},${formatFloat(lhRaw)},")
                    writer.append("${formatFloat(iaRaw)},${formatFloat(llRaw)},")
                    writer.append("${formatFloat(lkClean)},${formatFloat(rkClean)},${formatFloat(lhClean)},")
                    writer.append("${formatFloat(iaClean)},${formatFloat(llClean)}\n")
                }
                
                // Write summary row
                writer.append("SUMMARY,${maxFrames / fps},,")
                writer.append(",,,,")  // Raw signals (empty in summary)
                writer.append(",,,,")  // Cleaned signals (empty in summary)
                writer.append("\n")
                
                // Write 9 feature values
                for (i in FEATURE_NAMES_9.indices) {
                    if (i < rawFeatures.size) {
                        writer.append("feature_${FEATURE_NAMES_9[i]},${rawFeatures[i]}\n")
                    }
                }
                
                // Write diagnostics
                writer.append("fps,$fps\n")
                writer.append("total_frames,$maxFrames\n")
                writer.append("valid_frames,${leftKneeCleaned.validFrames}\n")
                writer.append("detection_rate,${if (maxFrames > 0) (leftKneeCleaned.validFrames.toFloat() / maxFrames * 100) else 0f}\n")
                
                if (cleaningStats != null) {
                    writer.append("spikes_rejected,${cleaningStats.totalSpikesRejected}\n")
                    writer.append("gaps_filled,${cleaningStats.totalGapsFilled}\n")
                } else {
                    val totalSpikes = leftKneeCleaned.spikesRejected + rightKneeCleaned.spikesRejected + 
                                     leftHipCleaned.spikesRejected
                    val totalGaps = leftKneeCleaned.gapsFilled + rightKneeCleaned.gapsFilled + 
                                   leftHipCleaned.gapsFilled
                    writer.append("spikes_rejected,$totalSpikes\n")
                    writer.append("gaps_filled,$totalGaps\n")
                }
            }
            
            // Make file visible in file managers immediately
            android.media.MediaScannerConnection.scanFile(context, arrayOf(outputFile.absolutePath), null, null)
            
            Log.d(TAG, "Debug data exported to: ${outputFile.absolutePath}")
            return outputFile.absolutePath
            
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting debug data: ${e.message}", e)
            return null
        }
    }
    
    private fun formatFloat(value: Float): String {
        return when {
            value.isNaN() -> ""
            value.isInfinite() -> ""
            else -> "%.6f".format(value)
        }
    }
}
