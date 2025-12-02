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
import GaitVision.com.leftAnkleAngles
import GaitVision.com.rightAnkleAngles
import GaitVision.com.leftKneeAngles
import GaitVision.com.rightKneeAngles
import GaitVision.com.leftHipAngles
import GaitVision.com.rightHipAngles
import GaitVision.com.torsoAngles
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
    private lateinit var ankleChart: LineChart
    private lateinit var torsoChart: LineChart
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
        ankleChart = findViewById(R.id.lineChartAnkle)
        torsoChart = findViewById(R.id.lineChartTorso)
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

    private fun calculateGaitScore() {
        try {
            // Check if we have enough data
            if (leftKneeMinAngles.isEmpty() || leftKneeMaxAngles.isEmpty() ||
                rightKneeMinAngles.isEmpty() || rightKneeMaxAngles.isEmpty() ||
                torsoMinAngles.isEmpty() || torsoMaxAngles.isEmpty()) {
                
                tvGaitScore.text = "--"
                tvScoreLabel.text = "Insufficient data for score calculation"
                return
            }

            // Prepare input data for the model
            val inputData = floatArrayOf(
                leftKneeMinAngles.average().toFloat(),
                leftKneeMaxAngles.average().toFloat(),
                rightKneeMinAngles.average().toFloat(),
                rightKneeMaxAngles.average().toFloat(),
                torsoMinAngles.average().toFloat(),
                torsoMaxAngles.average().toFloat(),
                calcStrideLengthAvg(participantHeight.toFloat() * 39.37F),
                leftKneeMaxAngles.average().toFloat() - leftKneeMinAngles.average().toFloat(),
                rightKneeMaxAngles.average().toFloat() - rightKneeMinAngles.average().toFloat()
            )

            // Load TFLite model and scalers
            val tfliteModel = FileUtil.loadMappedFile(this, "encoder_model.tflite")
            val interpreter = Interpreter(tfliteModel)
            val scalerMean = loadFloatBinFile(this, "scaler_mean.bin")
            val scalerScale = loadFloatBinFile(this, "scaler_scale.bin")
            val cleanCentroid = loadNpyFloatArray(assets.open("clean_centroid.npy"))
            val impairedCentroid = loadNpyFloatArray(assets.open("impaired_centroid.npy"))

            // Apply scaling with minimum threshold
            val minScaleValue = 1e-15f
            val safeScalerScale = scalerScale.map { 
                if (it < minScaleValue) minScaleValue else it 
            }.toFloatArray()

            val scaledInput = FloatArray(inputData.size) { i ->
                (inputData[i] - scalerMean[i]) / safeScalerScale[i]
            }

            // Run inference
            val output = Array(1) { FloatArray(2) }
            val input = arrayOf(scaledInput)
            interpreter.run(input, output)

            // Calculate gait index
            val distClean = euclideanDistance(output[0], cleanCentroid)
            val distImpaired = euclideanDistance(output[0], impairedCentroid)
            val gaitIndexUnscaled = 1 - (distClean / (distClean + distImpaired))
            val gaitIndexScaled = gaitIndexUnscaled * 100

            calculatedScore = gaitIndexScaled.roundToLong().toDouble()

            // Update UI
            tvGaitScore.text = calculatedScore.toLong().toString()
            tvScoreLabel.text = getScoreLabel(calculatedScore)

            // Color based on score
            val scoreColor = when {
                calculatedScore >= 80 -> "#4CAF50" // Green
                calculatedScore >= 60 -> "#FF9800" // Orange
                else -> "#F44336" // Red
            }
            tvGaitScore.setTextColor(android.graphics.Color.parseColor(scoreColor))

            Log.d("ResultsActivity", "Gait Score: $calculatedScore")

            // Save to database
            saveGaitScoreToDatabase()

        } catch (e: Exception) {
            Log.e("ResultsActivity", "Error calculating gait score: ${e.message}", e)
            tvGaitScore.text = "--"
            tvScoreLabel.text = "Error calculating score"
        }
    }

    private fun getScoreLabel(score: Double): String {
        return when {
            score >= 90 -> "Excellent Gait"
            score >= 80 -> "Good Gait"
            score >= 70 -> "Fair Gait"
            score >= 60 -> "Moderate Impairment"
            score >= 50 -> "Notable Impairment"
            else -> "Significant Impairment"
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
        // Plot all charts
        if (leftKneeAngles.isNotEmpty() || rightKneeAngles.isNotEmpty()) {
            plotLineGraph(kneeChart, leftKneeAngles, rightKneeAngles, "Left Knee", "Right Knee")
        }
        if (leftAnkleAngles.isNotEmpty() || rightAnkleAngles.isNotEmpty()) {
            plotLineGraph(ankleChart, leftAnkleAngles, rightAnkleAngles, "Left Ankle", "Right Ankle")
        }
        if (leftHipAngles.isNotEmpty() || rightHipAngles.isNotEmpty()) {
            plotLineGraph(hipChart, leftHipAngles, rightHipAngles, "Left Hip", "Right Hip")
        }
        if (torsoAngles.isNotEmpty()) {
            plotLineGraph(torsoChart, torsoAngles, torsoAngles, "Torso", "Torso")
        }

        // Initially show knee chart
        showChart("KNEE")
    }

    private fun showGraphPopup() {
        val popup = PopupMenu(this, btnSelectGraph)
        popup.menu.add(0, 1, 0, "Knee Graph")
        popup.menu.add(0, 2, 1, "Hip Graph")
        popup.menu.add(0, 3, 2, "Ankle Graph")
        popup.menu.add(0, 4, 3, "Torso Graph")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> showChart("KNEE")
                2 -> showChart("HIP")
                3 -> showChart("ANKLE")
                4 -> showChart("TORSO")
            }
            btnSelectGraph.text = item.title
            true
        }
        popup.show()
    }

    private fun showChart(chartType: String) {
        hipChart.visibility = View.INVISIBLE
        kneeChart.visibility = View.INVISIBLE
        ankleChart.visibility = View.INVISIBLE
        torsoChart.visibility = View.INVISIBLE

        when (chartType) {
            "HIP" -> {
                hipChart.visibility = View.VISIBLE
                btnSelectGraph.text = "Hip Graph"
            }
            "KNEE" -> {
                kneeChart.visibility = View.VISIBLE
                btnSelectGraph.text = "Knee Graph"
            }
            "ANKLE" -> {
                ankleChart.visibility = View.VISIBLE
                btnSelectGraph.text = "Ankle Graph"
            }
            "TORSO" -> {
                torsoChart.visibility = View.VISIBLE
                btnSelectGraph.text = "Torso Graph"
            }
        }
    }

    private fun exportCsvFiles() {
        val fileData = listOf(
            leftHipAngles,
            rightHipAngles,
            leftKneeAngles,
            rightKneeAngles,
            leftAnkleAngles,
            rightAnkleAngles,
            torsoAngles
        )

        val angleNames = listOf(
            "LeftHip",
            "RightHip",
            "LeftKnee",
            "RightKnee",
            "LeftAnkle",
            "RightAnkle",
            "Torso"
        )

        try {
            for (i in fileData.indices) {
                val fileName = "${participantId}_${angleNames[i]}.csv"
                writeToFile(fileName, fileData[i])
            }

            // Rename edited video
            renameEditedVideo()

            AlertDialog.Builder(this)
                .setTitle("Export Successful")
                .setMessage("CSV files saved to Documents as ${participantId}_GraphName.csv\n\nVideo saved to Movies as ${participantId}_video.mp4")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()

        } catch (e: Exception) {
            Log.e("ResultsActivity", "Error exporting: ${e.message}", e)
            Toast.makeText(this, "Error exporting files: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun writeToFile(fileName: String, fileData: List<Float>) {
        val fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
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
        val oldFilePath = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "edited_video.mp4"
        )
        val newFilePath = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            vidName
        )

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
