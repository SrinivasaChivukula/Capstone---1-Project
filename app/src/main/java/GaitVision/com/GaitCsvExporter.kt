package GaitVision.com

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import GaitVision.com.gait.GaitConfig9
import GaitVision.com.GaitResult9
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * CSV Exporter - Generates CSV for gait feature extraction results.
 * 
 * Allows direct comparison between Android and training environment extraction.
 */
object GaitCsvExporter {
    
    /**
     * CSV column headers (matches training environment format)
     */
    private val CSV_HEADERS = listOf(
        // Metadata
        "video_file",
        "subject_id", 
        "condition",
        "trial",
        "severity",
        "severity_code",
        "label",
        
        // Video stats
        "fps",
        "total_frames",
        "valid_frames",
        "detection_rate",
        "quality_tier",
        
        // 6 Features (MUST BE IN THIS ORDER)
        "stride_length_norm",
        "knee_left_rom",
        "knee_right_rom",
        "ldj_knee_left",
        "ldj_knee_right",
        "ldj_hip",
        
        // Diagnostics
        "spikes_rejected",
        "gaps_filled",
        "spike_pct",
        "interpolation_pct",
        "nan_feature_count",
        "nan_feature_pct",
        "left_knee_valid",
        "right_knee_valid",
        "left_hip_valid",
        
        // Android-specific (for comparison)
        "gait_score",
        "extraction_timestamp"
    )
    
    /**
     * Data class for a single extraction result
     */
    data class ExtractionRow(
        // Metadata
        val videoFile: String = "",
        val subjectId: String = "",
        val condition: String = "",
        val trial: String = "",
        val severity: String = "none",
        val severityCode: Int = 0,
        val label: String = "clean",
        
        // Video stats
        val fps: Float = 0f,
        val totalFrames: Int = 0,
        val validFrames: Int = 0,
        val detectionRate: Float = 0f,
        val qualityTier: String = "unknown",
        
        // 6 Features
        val strideLengthNorm: Float = Float.NaN,
        val kneeLeftRom: Float = Float.NaN,
        val kneeRightRom: Float = Float.NaN,
        val ldjKneeLeft: Float = Float.NaN,
        val ldjKneeRight: Float = Float.NaN,
        val ldjHip: Float = Float.NaN,
        
        // Diagnostics
        val spikesRejected: Int = 0,
        val gapsFilled: Int = 0,
        val spikePct: Float = 0f,
        val interpolationPct: Float = 0f,
        val nanFeatureCount: Int = 0,
        val nanFeaturePct: Float = 0f,
        val leftKneeValid: Int = 0,
        val rightKneeValid: Int = 0,
        val leftHipValid: Int = 0,
        
        // Android-specific
        val gaitScore: Int = 0,
        val extractionTimestamp: String = ""
    ) {
        fun toCSVRow(): String {
            return listOf(
                videoFile,
                subjectId,
                condition,
                trial,
                severity,
                severityCode.toString(),
                label,
                
                fps.toString(),
                totalFrames.toString(),
                validFrames.toString(),
                detectionRate.toString(),
                qualityTier,
                
                formatFloat(strideLengthNorm),
                formatFloat(kneeLeftRom),
                formatFloat(kneeRightRom),
                formatFloat(ldjKneeLeft),
                formatFloat(ldjKneeRight),
                formatFloat(ldjHip),
                
                spikesRejected.toString(),
                gapsFilled.toString(),
                formatFloat(spikePct),
                formatFloat(interpolationPct),
                nanFeatureCount.toString(),
                formatFloat(nanFeaturePct),
                leftKneeValid.toString(),
                rightKneeValid.toString(),
                leftHipValid.toString(),
                
                gaitScore.toString(),
                extractionTimestamp
            ).joinToString(",")
        }
        
        private fun formatFloat(value: Float): String {
            return if (value.isNaN() || value.isInfinite()) "" else "%.6f".format(value)
        }
    }
    
    /**
     * Export a single extraction result to CSV
     * Appends to existing file or creates new one with headers
     */
    fun exportToCsv(
        context: Context,
        row: ExtractionRow,
        filename: String = "gait_features_android.csv"
    ): File? {
        return try {
            // Use public Downloads directory for easy access
            val fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs()
            }
            val outputFile = File(fileDirectory, filename)
            
            val writeHeaders = !outputFile.exists()
            
            FileOutputStream(outputFile, true).use { output ->
                if (writeHeaders) {
                    output.write((CSV_HEADERS.joinToString(",") + "\n").toByteArray())
                }
                output.write((row.toCSVRow() + "\n").toByteArray())
            }
            
            // Make file visible in file managers immediately
            MediaScannerConnection.scanFile(context, arrayOf(outputFile.absolutePath), null, null)
            
            outputFile
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Export multiple results at once
     */
    fun exportAllToCsv(
        context: Context,
        rows: List<ExtractionRow>,
        filename: String = "gait_features_android.csv"
    ): File? {
        return try {
            // Use public Downloads directory for easy access
            val fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs()
            }
            val outputFile = File(fileDirectory, filename)
            
            FileOutputStream(outputFile, false).use { output ->
                // Write headers
                output.write((CSV_HEADERS.joinToString(",") + "\n").toByteArray())
                
                // Write all rows
                for (row in rows) {
                    output.write((row.toCSVRow() + "\n").toByteArray())
                }
            }
            
            // Make file visible in file managers immediately
            MediaScannerConnection.scanFile(context, arrayOf(outputFile.absolutePath), null, null)
            
            outputFile
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Create a row from extraction results
     */
    fun createRow(
        videoFile: String,
        subjectId: String,
        condition: String,
        rawFeatures: FloatArray,
        gaitResult: GaitResult9,
        fps: Float,
        totalFrames: Int,
        validFrames: Int,
        cleaningStats: CleaningStats
    ): ExtractionRow {
        val detectionRate = if (totalFrames > 0) validFrames.toFloat() / totalFrames else 0f
        val qualityTier = when {
            detectionRate >= GaitConfig9.QUALITY_TIER_FAIR -> "ok"
            detectionRate >= GaitConfig9.QUALITY_TIER_LOW -> "fair"
            else -> "low"
        }
        
        val nanCount = rawFeatures.count { it.isNaN() }
        
        // Parse severity from condition string (e.g., "KOA_MD" -> "moderate")
        val severity = parseSeverity(condition)
        val severityCode = when (severity) {
            "none" -> 0
            "early" -> 1
            "mild" -> 2
            "moderate" -> 3
            "severe" -> 4
            else -> 0
        }
        
        val label = if (condition.contains("NM", ignoreCase = true)) "clean" else "impaired"
        
        return ExtractionRow(
            videoFile = videoFile,
            subjectId = subjectId,
            condition = condition.split("_").firstOrNull() ?: condition,
            trial = "",
            severity = severity,
            severityCode = severityCode,
            label = label,
            
            fps = fps,
            totalFrames = totalFrames,
            validFrames = validFrames,
            detectionRate = detectionRate,
            qualityTier = qualityTier,
            
            strideLengthNorm = rawFeatures.getOrElse(0) { Float.NaN },
            kneeLeftRom = rawFeatures.getOrElse(1) { Float.NaN },
            kneeRightRom = rawFeatures.getOrElse(2) { Float.NaN },
            ldjKneeLeft = rawFeatures.getOrElse(3) { Float.NaN },
            ldjKneeRight = rawFeatures.getOrElse(4) { Float.NaN },
            ldjHip = rawFeatures.getOrElse(5) { Float.NaN },
            
            spikesRejected = cleaningStats.totalSpikesRejected,
            gapsFilled = cleaningStats.totalGapsFilled,
            spikePct = cleaningStats.spikePct,
            interpolationPct = cleaningStats.interpolationPct,
            nanFeatureCount = nanCount,
            nanFeaturePct = nanCount.toFloat() / 9 * 100,
            leftKneeValid = cleaningStats.leftKneeValid,
            rightKneeValid = cleaningStats.rightKneeValid,
            leftHipValid = cleaningStats.leftHipValid,
            
            gaitScore = gaitResult.gaitScore,
            extractionTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        )
    }
    
    private fun parseSeverity(condition: String): String {
        val upper = condition.uppercase()
        return when {
            upper.contains("SV") || upper.contains("SEVERE") -> "severe"
            upper.contains("MD") || upper.contains("MODERATE") -> "moderate"
            upper.contains("ML") || upper.contains("MILD") -> "mild"
            upper.contains("ER") || upper.contains("EARLY") -> "early"
            upper.contains("NM") || upper.contains("NORMAL") -> "none"
            else -> "none"
        }
    }
}

/**
 * Stats from signal cleaning
 */
data class CleaningStats(
    val totalSpikesRejected: Int = 0,
    val totalGapsFilled: Int = 0,
    val spikePct: Float = 0f,
    val interpolationPct: Float = 0f,
    val leftKneeValid: Int = 0,
    val rightKneeValid: Int = 0,
    val leftHipValid: Int = 0
)

