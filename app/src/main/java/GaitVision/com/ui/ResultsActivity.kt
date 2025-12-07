package GaitVision.com.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import GaitVision.com.R
import GaitVision.com.data.AppDatabase
import GaitVision.com.data.GaitScore
import GaitVision.com.data.repository.GaitScoreRepository
import GaitVision.com.plotLineGraph
import GaitVision.com.calcStrideLengthAvg
import GaitVision.com.editedUri
import GaitVision.com.leftKneeAngles
import GaitVision.com.rightKneeAngles
import GaitVision.com.leftHipAngles
import GaitVision.com.rightHipAngles
import GaitVision.com.interAnkleDistances
import GaitVision.com.leftKneeMinAngles
import GaitVision.com.leftKneeMaxAngles
import GaitVision.com.rightKneeMinAngles
import GaitVision.com.rightKneeMaxAngles
import GaitVision.com.torsoMinAngles
import GaitVision.com.torsoMaxAngles
import GaitVision.com.participantId
import GaitVision.com.participantHeight
import GaitVision.com.currentPatientId
import GaitVision.com.currentVideoId
import GaitVision.com.interAnkleDistances
import GaitVision.com.legLengths
import GaitVision.com.GaitScorer9
import GaitVision.com.GaitResult9
import GaitVision.com.gait.GaitFeatures9
import GaitVision.com.gait.GaitLDJ9
import GaitVision.com.DiagnosticExporter
import GaitVision.com.CleaningStats
import GaitVision.com.gait.GaitSignalProcessing9
import GaitVision.com.gait.CleanedSignalResult9
import GaitVision.com.detectedFps
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToLong
import kotlin.math.sqrt

class ResultsActivity : AppCompatActivity() {

    private lateinit var tvGaitScore: TextView
    private lateinit var tvScoreLabel: TextView
    private lateinit var hipChart: LineChart
    private lateinit var kneeChart: LineChart
    private lateinit var interAnkleChart: LineChart  // Repurposed from ankleChart
    private lateinit var btnSelectGraph: Button

    private var calculatedScore: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        initializeViews()
        setupButtons()
        
        // Calculate and display gait score
        calculateGaitScore()
        
        // Setup charts
        setupCharts()
    }

    private fun initializeViews() {
        tvGaitScore = findViewById(R.id.tvGaitScore)
        tvScoreLabel = findViewById(R.id.tvScoreLabel)
        hipChart = findViewById(R.id.lineChartHip)
        kneeChart = findViewById(R.id.lineChartKnee)
        interAnkleChart = findViewById(R.id.lineChartAnkle)  // Repurposed for inter-ankle distance
        // torsoChart removed - not used in 9-feature pipeline
        btnSelectGraph = findViewById(R.id.btnSelectGraph)
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnMainMenu).setOnClickListener {
            // Go back to dashboard and clear the back stack
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btnExportCsv).setOnClickListener {
            exportCsvFiles()
        }

        btnSelectGraph.setOnClickListener {
            showGraphPopup()
        }
    }

    // Legacy gaitResult removed - only using gaitResult9 (9-feature model)
    
    // Store the v3 gait result for export (new 9-feature AE_L4)
    private var gaitResult9: GaitResult9? = null
    
    // Store cleaning stats for CSV export
    private var lastCleaningStats: CleaningStats? = null
    
    private fun calculateGaitScore() {
        try {
            // Initialize the 9-feature scorer (AE_L4)
            if (!GaitScorer9.isReady()) {
                if (!GaitScorer9.initialize(this)) {
                    Log.e("ResultsActivity", "Failed to initialize GaitScorer9")
                    showScoringError("Failed to load gait scoring model")
                    return
                }
            }
            
            // Check if we have enough data
            if (leftKneeAngles.size < 10 || rightKneeAngles.size < 10 || 
                leftHipAngles.size < 10 || interAnkleDistances.size < 10 || legLengths.size < 10) {
                Log.e("ResultsActivity", "Insufficient data for scoring")
                showScoringError("Not enough pose data detected. Please ensure the subject is visible throughout the video.")
                return
            }
            
            // Use detected FPS (from video metadata) or default to 30
            val fps = if (detectedFps > 0) detectedFps else 30f
            Log.d("ResultsActivity", "Using FPS: $fps for 9-feature extraction")
            
            // STEP 1: Apply signal cleaning (spike rejection, interpolation, smoothing)
            val leftKneeCleaned = GaitSignalProcessing9.cleanSignal(leftKneeAngles, fps)
            val rightKneeCleaned = GaitSignalProcessing9.cleanSignal(rightKneeAngles, fps)
            val leftHipCleaned = GaitSignalProcessing9.cleanSignal(leftHipAngles, fps)
            val rightHipCleaned = GaitSignalProcessing9.cleanSignal(rightHipAngles, fps)
            val interAnkleCleaned = GaitSignalProcessing9.cleanSignal(interAnkleDistances, fps)
            val legLengthsCleaned = GaitSignalProcessing9.cleanSignal(legLengths, fps)
            
            // Track cleaning stats for export
            val totalSpikes = leftKneeCleaned.spikesRejected + rightKneeCleaned.spikesRejected + 
                              leftHipCleaned.spikesRejected + rightHipCleaned.spikesRejected
            val totalGaps = leftKneeCleaned.gapsFilled + rightKneeCleaned.gapsFilled +
                            leftHipCleaned.gapsFilled + rightHipCleaned.gapsFilled
            val totalFrames = leftKneeAngles.size
            
            lastCleaningStats = CleaningStats(
                totalSpikesRejected = totalSpikes,
                totalGapsFilled = totalGaps,
                spikePct = if (totalFrames > 0) totalSpikes.toFloat() / totalFrames * 100 else 0f,
                interpolationPct = if (totalFrames > 0) totalGaps.toFloat() / totalFrames * 100 else 0f,
                leftKneeValid = leftKneeCleaned.validFrames,
                rightKneeValid = rightKneeCleaned.validFrames,
                leftHipValid = leftHipCleaned.validFrames
            )
            
            Log.d("ResultsActivity", "Signal cleaning: ${totalSpikes} spikes rejected, ${totalGaps} gaps filled")
            
            // STEP 2: Average left and right hip for hip ROM and LDJ
            val avgHip = if (leftHipCleaned.data.size == rightHipCleaned.data.size) {
                leftHipCleaned.data.zip(rightHipCleaned.data).map { (l, r) -> (l + r) / 2f }
            } else {
                leftHipCleaned.data.ifEmpty { rightHipCleaned.data }
            }
            
            // STEP 3: Extract all 9 features using GaitFeatures9 pipeline
            // Feature 1: stride_amp_norm (P95 inter-ankle / avg leg length)
            val interAnkleArray = interAnkleCleaned.data.toFloatArray()
            val legLeftArray = legLengthsCleaned.data.toFloatArray()  // Using same for both since we only have avg
            val legRightArray = legLengthsCleaned.data.toFloatArray()
            val strideAmpNorm = GaitFeatures9.computeStrideAmpNorm(interAnkleArray, legLeftArray, legRightArray)
            
            // Features 2-5: Knee ROMs and maxes
            val leftKneeArray = leftKneeCleaned.data.toFloatArray()
            val rightKneeArray = rightKneeCleaned.data.toFloatArray()
            val kneeLeftRom = GaitLDJ9.computeRom(leftKneeArray)
            val kneeRightRom = GaitLDJ9.computeRom(rightKneeArray)
            val kneeLeftMax = GaitLDJ9.computeMax(leftKneeArray)
            val kneeRightMax = GaitLDJ9.computeMax(rightKneeArray)
            
            // Feature 6: Hip ROM   CURRENTLY THE MOST INNACURATE FEATURE EXTRACTION ON MOBILE VERSION
            val hipArray = avgHip.toFloatArray()
            val hipRom = GaitLDJ9.computeRom(hipArray)
            
            // Features 7-9: LDJ values These are log flattened jerk values they describe the smootheness of motion
            val ldjKneeLeft = GaitLDJ9.computeLdj(leftKneeArray, fps)
            val ldjKneeRight = GaitLDJ9.computeLdj(rightKneeArray, fps)
            val ldjHip = GaitLDJ9.computeLdj(hipArray, fps)
            
            // Build 9-feature array in canonical order
            val rawFeatures9 = floatArrayOf(
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
            
            // Store for export
            lastFeatures9 = rawFeatures9
            
            // Log detailed feature extraction results
            Log.d("ResultsActivity", "=== 9-FEATURE EXTRACTION ===")
            Log.d("ResultsActivity", "  stride_amp_norm: $strideAmpNorm")
            Log.d("ResultsActivity", "  knee_left_rom: $kneeLeftRom")
            Log.d("ResultsActivity", "  knee_right_rom: $kneeRightRom")
            Log.d("ResultsActivity", "  knee_left_max: $kneeLeftMax")
            Log.d("ResultsActivity", "  knee_right_max: $kneeRightMax")
            Log.d("ResultsActivity", "  hip_rom: $hipRom")
            Log.d("ResultsActivity", "  ldj_knee_left: $ldjKneeLeft")
            Log.d("ResultsActivity", "  ldj_knee_right: $ldjKneeRight")
            Log.d("ResultsActivity", "  ldj_hip: $ldjHip")
            Log.d("ResultsActivity", "  NaN count: ${rawFeatures9.count { it.isNaN() }}")
            
            // Check for NaN features - this causes score of 0!
            val nanCount = rawFeatures9.count { it.isNaN() }
            if (nanCount > 0) {
                Log.e("ResultsActivity", "WARNING: $nanCount features are NaN! This will cause low score.")
                Log.e("ResultsActivity", "  Signal lengths: leftKnee=${leftKneeArray.size}, rightKnee=${rightKneeArray.size}, hip=${hipArray.size}")
                Log.e("ResultsActivity", "  Inter-ankle: ${interAnkleArray.size}, LegLength: ${legLeftArray.size}")
            }
            
            // STEP 4: Run v3 scoring (9-feature AE_L4)
            gaitResult9 = GaitScorer9.score(rawFeatures9)
            calculatedScore = gaitResult9!!.gaitScore.toDouble()
            
            // Store normalized features for export
            lastNormalizedFeatures = gaitResult9!!.normalizedFeatures
            
            // Update UI
            tvGaitScore.text = gaitResult9!!.gaitScore.toString()
            tvScoreLabel.text = gaitResult9!!.label
            
            // Color based on score
            val scoreColor = when {
                calculatedScore >= 80 -> "#4CAF50" // Green
                calculatedScore >= 60 -> "#FF9800" // Orange
                else -> "#F44336" // Red
            }
            tvGaitScore.setTextColor(android.graphics.Color.parseColor(scoreColor))
            
            // Log detailed results
            Log.d("ResultsActivity", "=== SCORING RESULTS ===")
            Log.d("ResultsActivity", "  Gait Score: ${gaitResult9!!.gaitScore}")
            Log.d("ResultsActivity", "  Reconstruction error: ${gaitResult9!!.reconstructionError}")
            Log.d("ResultsActivity", "  Classification: ${if (gaitResult9!!.isImpaired) "IMPAIRED" else "NORMAL"}")
            Log.d("ResultsActivity", "  Threshold: ${gaitResult9!!.threshold}")
            Log.d("ResultsActivity", "  Normalized features: ${gaitResult9!!.normalizedFeatures.joinToString { "%.3f".format(it) }}")
            Log.d("ResultsActivity", "  Reconstructed: ${gaitResult9!!.reconstructedFeatures.joinToString { "%.3f".format(it) }}")
            
            // Save to database
            saveGaitScoreToDatabase()
            
        } catch (e: Exception) {
            Log.e("ResultsActivity", "Error in scoring: ${e.message}", e)
            showScoringError("Scoring failed: ${e.message}")
        }
    }
    
    /**
     * Show error when scoring fails
     */
    private fun showScoringError(message: String) {
        tvGaitScore.text = "--"
        tvScoreLabel.text = "Error"
        tvGaitScore.setTextColor(android.graphics.Color.parseColor("#F44336"))
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        calculatedScore = 0.0
    }
    
    private fun getScoreLabel(score: Double): String {
        // Use same thresholds as GaitScorer9
        return when {
            score >= 90 -> "Excellent"
            score >= 80 -> "Good"
            score >= 70 -> "Fair"
            score >= 50 -> "Mild Impairment"
            score >= 30 -> "Moderate Impairment"
            else -> "Severe Impairment"
        }
    }

    private fun saveGaitScoreToDatabase() {
        if (currentPatientId == null || currentVideoId == null) return

        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@ResultsActivity)
                val gaitScoreRepository = GaitScoreRepository(database.gaitScoreDao())

                withContext(Dispatchers.IO) {
                    val leftKneeScore = if (leftKneeMinAngles.isNotEmpty() && leftKneeMaxAngles.isNotEmpty()) {
                        (leftKneeMaxAngles.average() - leftKneeMinAngles.average()) / 180.0 * 100.0
                    } else null

                    val rightKneeScore = if (rightKneeMinAngles.isNotEmpty() && rightKneeMaxAngles.isNotEmpty()) {
                        (rightKneeMaxAngles.average() - rightKneeMinAngles.average()) / 180.0 * 100.0
                    } else null

                    val leftHipScore = if (leftHipAngles.isNotEmpty()) {
                        leftHipAngles.average().toDouble() / 180.0 * 100.0
                    } else null

                    val rightHipScore = if (rightHipAngles.isNotEmpty()) {
                        rightHipAngles.average().toDouble() / 180.0 * 100.0
                    } else null

                    val torsoScore = if (torsoMinAngles.isNotEmpty() && torsoMaxAngles.isNotEmpty()) {
                        (torsoMaxAngles.average() - torsoMinAngles.average()) / 180.0 * 100.0
                    } else null

                    val gaitScore = GaitScore(
                        patientId = currentPatientId!!,
                        videoId = currentVideoId!!,
                        overallScore = calculatedScore,
                        leftKneeScore = leftKneeScore,
                        rightKneeScore = rightKneeScore,
                        leftHipScore = leftHipScore,
                        rightHipScore = rightHipScore,
                        torsoScore = torsoScore,
                        recordedAt = System.currentTimeMillis()
                    )

                    gaitScoreRepository.insertGaitScore(gaitScore)
                    Log.d("ResultsActivity", "Saved gait score: ${gaitScore.overallScore}")
                }
            } catch (e: Exception) {
                Log.e("ResultsActivity", "Error saving gait score: ${e.message}", e)
            }
        }
    }

    private fun setupCharts() {
        // Plot charts for 9-feature pipeline signals
        if (leftKneeAngles.isNotEmpty() || rightKneeAngles.isNotEmpty()) {
            plotLineGraph(kneeChart, leftKneeAngles, rightKneeAngles, "Left Knee", "Right Knee")
        }
        if (leftHipAngles.isNotEmpty() || rightHipAngles.isNotEmpty()) {
            plotLineGraph(hipChart, leftHipAngles, rightHipAngles, "Left Hip", "Right Hip")
        }
        if (interAnkleDistances.isNotEmpty()) {
            plotLineGraph(interAnkleChart, interAnkleDistances, interAnkleDistances, "Inter-Ankle", "Inter-Ankle")
        }

        // Initially show knee chart
        showChart("KNEE")
    }

    private fun showGraphPopup() {
        val popup = PopupMenu(this, btnSelectGraph)
        popup.menu.add(0, 1, 0, "Knee Graph")
        popup.menu.add(0, 2, 1, "Hip Graph")
        popup.menu.add(0, 3, 2, "Stride Graph")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> showChart("KNEE")
                2 -> showChart("HIP")
                3 -> showChart("STRIDE")
            }
            btnSelectGraph.text = item.title
            true
        }
        popup.show()
    }

    private fun showChart(chartType: String) {
        hipChart.visibility = View.INVISIBLE
        kneeChart.visibility = View.INVISIBLE
        interAnkleChart.visibility = View.INVISIBLE

        when (chartType) {
            "KNEE" -> {
                kneeChart.visibility = View.VISIBLE
                btnSelectGraph.text = "Knee Graph"
            }
            "HIP" -> {
                hipChart.visibility = View.VISIBLE
                btnSelectGraph.text = "Hip Graph"
            }
            "STRIDE" -> {
                interAnkleChart.visibility = View.VISIBLE
                btnSelectGraph.text = "Stride Graph"
            }
        }
    }

    // Store the last extracted features for export
    private var lastFeatures9: FloatArray? = null
    private var lastNormalizedFeatures: FloatArray? = null
    
    private fun exportCsvFiles() {
        try {
            val fps = if (detectedFps > 0) detectedFps else 30f
            val exportedFiles = mutableListOf<String>()
            
            // EXPORT 1: Diagnostic JSON, Contains raw signals, 9 features, model I/O, scoring results
            val diagnosticFile = DiagnosticExporter.exportDiagnostics(
                context = this,
                subjectId = participantId,
                fps = fps,
                rawLeftKnee = leftKneeAngles,
                rawRightKnee = rightKneeAngles,
                rawLeftHip = leftHipAngles,
                rawRightHip = rightHipAngles,
                rawInterAnkle = interAnkleDistances,
                rawLegLengths = legLengths,
                features9 = lastFeatures9,
                normalizedFeatures = lastNormalizedFeatures,
                reconstructedFeatures = gaitResult9?.reconstructedFeatures,
                reconstructionError = gaitResult9?.reconstructionError,
                gaitScore = gaitResult9?.gaitScore,
                isImpaired = gaitResult9?.isImpaired,
                cleaningStats = lastCleaningStats
            )
            if (diagnosticFile != null) {
                exportedFiles.add("Diagnostic JSON: ${diagnosticFile.name}")
                Log.d("ResultsActivity", "Exported diagnostic: ${diagnosticFile.absolutePath}")
            }
            
            // EXPORT 2: Raw signals CSV (for spreadsheet/plotting analysis)
            val signalsFile = DiagnosticExporter.exportSignalsCsv(
                context = this,
                subjectId = participantId,
                leftKnee = leftKneeAngles,
                rightKnee = rightKneeAngles,
                leftHip = leftHipAngles,
                rightHip = rightHipAngles,
                interAnkle = interAnkleDistances,
                legLengths = legLengths
            )
            if (signalsFile != null) {
                exportedFiles.add("Signals CSV: ${signalsFile.name}")
                Log.d("ResultsActivity", "Exported signals: ${signalsFile.absolutePath}")
            }

            // EXPORT 3: Rename edited video
            renameEditedVideo()
            exportedFiles.add("Video: ${participantId}_video.mp4")

            // Show summary - simplified for 9-feature pipeline
            val message = buildString {
                append("Exported to Downloads:\n\n")
                exportedFiles.forEach { append("• $it\n") }
                append("\n")
                append("For PC comparison:\n")
                append("• Diagnostic JSON: features, scoring, model I/O\n")
                append("• Signals CSV: raw signals for plotting")
            }
            
            AlertDialog.Builder(this)
                .setTitle("Export Successful")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()

        } catch (e: Exception) {
            Log.e("ResultsActivity", "Error exporting: ${e.message}", e)
            Toast.makeText(this, "Error exporting files: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun writeToFile(fileName: String, fileData: List<Float>) {
        // Use public Downloads directory for easy access
        val fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!fileDirectory.exists()) {
            fileDirectory.mkdirs()
        }
        val outputFile = File(fileDirectory, fileName)

        FileOutputStream(outputFile).use { output ->
            output.write("Frame #,Angle\n".toByteArray())
            for (i in fileData.indices) {
                output.write("$i,${fileData[i]}\n".toByteArray())
            }
        }

        MediaScannerConnection.scanFile(this, arrayOf(outputFile.absolutePath), null, null)
    }

    private fun renameEditedVideo() {
        val vidName = "${participantId}_video.mp4"
        val moviesDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        val oldFilePath = File(moviesDir, "edited_video.mp4")
        val newFilePath = File(moviesDir, vidName)

        if (oldFilePath.exists()) {
            oldFilePath.renameTo(newFilePath)
            editedUri = Uri.fromFile(newFilePath)
            MediaScannerConnection.scanFile(this, arrayOf(newFilePath.absolutePath), null, null)
        }
    }

    // Helper functions for gait score calculation
    private fun loadFloatBinFile(context: Context, filename: String): FloatArray {
        val inputStream = context.assets.open(filename)
        val bytes = inputStream.readBytes()
        inputStream.close()

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val numFloats = bytes.size / 4
        val result = FloatArray(numFloats)
        for (i in 0 until numFloats) {
            result[i] = buffer.float
        }
        return result
    }

    private fun loadNpyFloatArray(assetStream: InputStream): FloatArray {
        val header = ByteArray(128)
        assetStream.read(header)
        val data = assetStream.readBytes()
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        val floatList = mutableListOf<Float>()
        while (buffer.hasRemaining()) {
            floatList.add(buffer.float)
        }
        return floatList.toFloatArray()
    }

    private fun euclideanDistance(a: FloatArray, b: FloatArray): Float {
        var sum = 0f
        for (i in a.indices) {
            val diff = a[i] - b[i]
            sum += diff * diff
        }
        return sqrt(sum)
    }
}