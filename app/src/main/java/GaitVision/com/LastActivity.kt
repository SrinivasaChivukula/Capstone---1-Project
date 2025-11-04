package GaitVision.com

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.activity.ComponentActivity
import java.io.File
import java.io.FileOutputStream
import android.widget.TextView
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToLong

import java.io.InputStream
import kotlin.math.sqrt

class LastActivity : ComponentActivity()
{
    // Store scores for CSV export
    private var autoencoderScore: Float = 0f
    private var pcaScore: Float = 0f

    /*
    Name           : loadFloatBinFile
    Parameters     :
        context    : This parameter is the interface that contains global information about
                     the application environment.
        filename   : This is the filename/path for the file we want to open.
    Description    : This function will read in the data from file specified for use in the gait score
                     process. It reads the entire binary file and converts the values in little endian
                      to floats that were originally used.
    Return         :
        FloatArray : Returns an array of the float values to be used for calculating the gait score.
                     Array is a 1 by 9 array
     */
    fun loadFloatBinFile(context: Context, filename: String): FloatArray {
        //Open file and read all the data
        val inputStream = context.assets.open(filename)
        val bytes = inputStream.readBytes()
        inputStream.close()

        ///Change from bytearray to byte buffer in little endian
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val numFloats = bytes.size / 4

        //Loop through array including all values into return value
        val result = FloatArray(numFloats)
        for (i in 0 until numFloats) {
            result[i] = buffer.float
        }

        return result
    }

    /*
    Name            : loadNpyFloatArray
    Parameters      :
        assetStream : An opened file with the data we want to read out of.
    Description     : This function will read a file with information on the clean and impaired
                      centroid information used in the gait score training.
    Return:
        FloatArray : Returns an array of the locations of the centroid on the 2D plane during
                     gait analysis training. Array is a 1 by 2
     */
    fun loadNpyFloatArray(assetStream: InputStream): FloatArray {
        //Ignore header information of file
        val header = ByteArray(128)
        assetStream.read(header)

        // Skip to data
        val data = assetStream.readBytes()
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

        //Add value to array
        val floatList = mutableListOf<Float>()
        while (buffer.hasRemaining()) {
            floatList.add(buffer.float)
        }

        return floatList.toFloatArray()
    }

    /*
    Name        : euclideanDistance
    Parameters  :
        a       : First point we want position of.
        b       : Second point we want position of.
    Description : This function will calculate the distance between the 2 points in a 2D plane.
                 Uses pythagorean theorem.
    Return      :
        Float   : Returns the distance between both points.
     */
    fun euclideanDistance(a: FloatArray, b: FloatArray): Float {
        var sum = 0f  // Initialize a sum variable for the squared differences

        // Loop through each pair of elements from a and b
        for (i in a.indices) {
            val diff = a[i] - b[i]  // Calculate the difference between corresponding elements
            sum += diff * diff       // Square the difference and add to the sum
        }

        return sqrt(sum)  // Return the square root of the sum
    }

    /*
    Name        : calculateAutoencoderScore
    Parameters  :
        scaledInput : The z-score normalized 9-feature input vector
    Description : This function calculates the gait score using the neural network autoencoder.
                  It loads the TFLite model, runs inference to get 2D latent space coordinates,
                  then calculates distances to clean/impaired centroids to produce a score.
    Return      :
        Float   : Gait score from 0-100, where higher values indicate more impaired gait
     */
    private fun calculateAutoencoderScore(scaledInput: FloatArray): Float {
        // Load autoencoder model
        val tfliteModel = FileUtil.loadMappedFile(this, "encoder_model.tflite")
        val interpreter = Interpreter(tfliteModel)
        
        // Load centroids
        val cleanCentroidStream = assets.open("clean_centroid.npy")
        val impairedCentroidStream = assets.open("impaired_centroid.npy")
        val cleanCentroid = loadNpyFloatArray(cleanCentroidStream)
        val impairedCentroid = loadNpyFloatArray(impairedCentroidStream)
        
        // Run autoencoder inference: 9D → 2D
        val output = Array(1){FloatArray(2)}
        val input = arrayOf(scaledInput)
        interpreter.run(input, output)
        
        val latentSpace = output[0]
        Log.d("GaitAnalysis", "Autoencoder Latent: ${latentSpace.contentToString()}")
        
        // Calculate distances to centroids
        val distClean = euclideanDistance(latentSpace, cleanCentroid)
        val distImpaired = euclideanDistance(latentSpace, impairedCentroid)
        
        Log.d("GaitAnalysis", "Autoencoder - DistClean: $distClean, DistImpaired: $distImpaired")
        
        // Calculate gait score
        val gaitIndexUnscaled = 1 - (distClean / (distClean + distImpaired))
        val gaitIndexScaled = gaitIndexUnscaled * 100
        
        // Clean up
        interpreter.close()
        cleanCentroidStream.close()
        impairedCentroidStream.close()
        
        return gaitIndexScaled
    }

    /*
    Name        : calculatePCAScore
    Parameters  :
        inputData : The raw 9-feature input vector (not normalized)
    Description : Calculates gait score using PCA transformation.
                  Loads PCA's scaler, normalizes input, transforms to 2D space,
                  then calculates distances to centroids to produce a score.
    Return      :
        Float   : Gait score from 0-100, where higher values indicate more impaired gait
     */
    private fun calculatePCAScore(inputData: FloatArray): Float {
        // Load PCA's scaler (fit on PCA training data)
        val pcaScalerMean = loadFloatBinFile(this, "scaler_mean_pca.bin")
        val pcaScalerScale = loadFloatBinFile(this, "scaler_scale_pca.bin")
        
        // Make sure no values are smaller than a threshold
        val minScaleValue = 1e-15f
        val safePcaScalerScale = pcaScalerScale.map {
            if (it < minScaleValue) minScaleValue else it
        }.toFloatArray()
        
        // Normalize input using PCA's scaler
        val scaledInput = FloatArray(inputData.size) { i ->
            (inputData[i] - pcaScalerMean[i]) / safePcaScalerScale[i]
        }
        
        // Load PCA components matrix (9x2)
        val pcaComponents = loadFloatBinFile(this, "pca_components.bin")
        
        // Reshape components to 9x2 matrix (stored as flat array: 18 floats)
        // Matrix multiplication: scaledInput (1x9) @ pcaComponents (9x2) = result (1x2)
        val pcaResult = FloatArray(2)
        for (i in 0 until 2) {
            var sum = 0f
            for (j in 0 until 9) {
                sum += scaledInput[j] * pcaComponents[j * 2 + i]
            }
            pcaResult[i] = sum
        }
        
        Log.d("GaitAnalysis", "PCA Latent: ${pcaResult.contentToString()}")
        
        // Load centroids
        val cleanCentroidPCA = loadFloatBinFile(this, "clean_centroid_pca.bin")
        val impairedCentroidPCA = loadFloatBinFile(this, "impaired_centroid_pca.bin")
        
        // Calculate distances to centroids
        val distClean = euclideanDistance(pcaResult, cleanCentroidPCA)
        val distImpaired = euclideanDistance(pcaResult, impairedCentroidPCA)
        
        Log.d("GaitAnalysis", "PCA - DistClean: $distClean, DistImpaired: $distImpaired")
        
        // Calculate gait score (same formula as autoencoder)
        val gaitIndexUnscaled = 1 - (distClean / (distClean + distImpaired))
        val gaitIndexScaled = gaitIndexUnscaled * 100
        
        return gaitIndexScaled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_last)


        //Initialize data for gait score prediction
        val inputData = floatArrayOf(
            leftKneeMinAngles.average().toFloat(),
            leftKneeMaxAngles.average().toFloat(),
            rightKneeMinAngles.average().toFloat(),
            rightKneeMaxAngles.average().toFloat(),
            torsoMinAngles.average().toFloat(),
            torsoMaxAngles.average().toFloat(),
            calcStrideLengthAvg(participantHeight.toFloat()*39.37F),
            leftKneeMaxAngles.average().toFloat() - leftKneeMinAngles.average().toFloat(),
            rightKneeMaxAngles.average().toFloat() - rightKneeMinAngles.average().toFloat()
        )

        //Log check values to see if they look correct
        Log.d("GaitAnalysis", "InputData: ${inputData.contentToString()}")

        // ==================== AUTOENCODER ANALYSIS ====================
        // Load autoencoder's scaler
        val aeScalerMean = loadFloatBinFile(this, "scaler_mean.bin")
        val aeScalerScale = loadFloatBinFile(this, "scaler_scale.bin")
        
        // Make sure no values are smaller than a threshold
        val minScaleValue = 1e-15f
        val safeAeScalerScale = aeScalerScale.map {
            if (it < minScaleValue) minScaleValue else it
        }.toFloatArray()
        
        // Normalize input for autoencoder
        val scaledInputAE = FloatArray(inputData.size) { i ->
            (inputData[i] - aeScalerMean[i]) / safeAeScalerScale[i]
        }
        
        autoencoderScore = calculateAutoencoderScore(scaledInputAE)
        Log.d("GaitAnalysis", "Autoencoder Score: $autoencoderScore")

        // ==================== PCA ANALYSIS ====================
        // PCA uses its own scaler (fit on PCA training data)
        pcaScore = calculatePCAScore(inputData)
        Log.d("GaitAnalysis", "PCA Score: $pcaScore")

        // ==================== UPDATE UI WITH BOTH SCORES ====================
        val autoencoderScoreView = findViewById<TextView>(R.id.autoencoder_score)
        val pcaScoreView = findViewById<TextView>(R.id.pca_score)
        val comparisonTextView = findViewById<TextView>(R.id.comparison_text)

        autoencoderScoreView.text = autoencoderScore.roundToLong().toString()
        pcaScoreView.text = pcaScore.roundToLong().toString()

        // Calculate and display difference
        val difference = pcaScore - autoencoderScore
        val diffText = when {
            difference > 0 -> "PCA +${difference.roundToLong()} higher"
            difference < 0 -> "Autoencoder +${(-difference).roundToLong()} higher"
            else -> "Scores match"
        }
        comparisonTextView.text = diffText

        val chooseGraphBtn = findViewById<Button>(R.id.select_graph_btn)
        val popupMenu = PopupMenu(this, chooseGraphBtn)
        popupMenu.menuInflater.inflate(R.menu.popup_menu_2, popupMenu.menu)

        var hipGraph = findViewById<com.github.mikephil.charting.charts.LineChart>(R.id.lineChartHip)
        var kneeGraph = findViewById<com.github.mikephil.charting.charts.LineChart>(R.id.lineChartKnee)
        var ankleGraph = findViewById<com.github.mikephil.charting.charts.LineChart>(R.id.lineChartAnkle)
        var torsoGraph = findViewById<com.github.mikephil.charting.charts.LineChart>(R.id.lineChartTorso)

        plotLineGraph(kneeGraph, leftKneeAngles, rightKneeAngles, "Left Knee Angles", "Right Knee Angles")
        plotLineGraph(ankleGraph, leftAnkleAngles, rightAnkleAngles, "Left Ankle Angles", "Right Ankle Angles")
        plotLineGraph(hipGraph, leftHipAngles, rightHipAngles, "Left Hip Angles", "Right Hip Angles")
        plotLineGraph(torsoGraph, torsoAngles, torsoAngles, "Torso Angles", "Torso Angles") // Assuming torso is the same

        popupMenu.setOnMenuItemClickListener { menuItem -> val id = menuItem.itemId

            if (id == R.id.menu_hip) {
                // hip graph
                val graphHip = findViewById<TextView>(R.id.select_graph_btn)
                val graphHipName = "HIP GRAPH"
                graphHip.text = graphHipName

                hipGraph.visibility = View.VISIBLE
                kneeGraph.visibility = View.INVISIBLE
                ankleGraph.visibility = View.INVISIBLE
                torsoGraph.visibility = View.INVISIBLE
            }
            else if (id == R.id.menu_knee) {
                // knee graph
                val graphKnee = findViewById<TextView>(R.id.select_graph_btn)
                val graphKneeName = "KNEE GRAPH"
                graphKnee.text = graphKneeName

                hipGraph.visibility = View.INVISIBLE
                kneeGraph.visibility = View.VISIBLE
                ankleGraph.visibility = View.INVISIBLE
                torsoGraph.visibility = View.INVISIBLE
            }
            else if (id == R.id.menu_ankle) {
                // ankle graph
                val graphAnkle = findViewById<TextView>(R.id.select_graph_btn)
                val graphAnkleName = "ANKLE GRAPH"
                graphAnkle.text = graphAnkleName

                hipGraph.visibility = View.INVISIBLE
                kneeGraph.visibility = View.INVISIBLE
                ankleGraph.visibility = View.VISIBLE
                torsoGraph.visibility = View.INVISIBLE
            }
            else if (id == R.id.menu_torso){
                // torso graph
                val graphTorso = findViewById<TextView>(R.id.select_graph_btn)
                val graphTorsoName = "TORSO GRAPH"
                graphTorso.text = graphTorsoName

                hipGraph.visibility = View.INVISIBLE
                kneeGraph.visibility = View.INVISIBLE
                ankleGraph.visibility = View.INVISIBLE
                torsoGraph.visibility = View.VISIBLE
            }
            false
        }

        chooseGraphBtn.setOnClickListener {
            popupMenu.show()
        }

        //Functionality for exporting CSV files.

        val fileData: List<MutableList<Float>>  = mutableListOf(        //list of all data lists
            leftHipAngles,
            rightHipAngles,
            leftKneeAngles,
            rightKneeAngles,
            leftAnkleAngles,
            rightAnkleAngles,
            torsoAngles,
            strideAngles
        )

        val angleNames = listOf(        //list of names used for files
            "LeftHip",
            "RightHip",
            "LeftKnee",
            "RightKnee",
            "LeftAnkle",
            "RightAnkle",
            "Torso",
            "Stride"
        )

        val exportButton = findViewById<Button>(R.id.submit_id_btn)
        exportButton.setOnClickListener {
            for (i in fileData.indices) {       //for-loop iterates through the fileData list and creates a csv file for each of the angle graphs.
                val fileName = buildString {
                    append(participantId.toString())
                    append("_")
                    append(angleNames[i])
                    append(".csv")
                }    //filename participantId_angle

                writeToFile(fileName, fileData[i])                 //write to file is called with file name and placeholder as parameters
                renameTo(participantId.toString())
            }

            // Export metadata (participant height for stride calculation)
            val metadataFileName = buildString {
                append(participantId.toString())
                append("_metadata.csv")
            }
            writeMetadataFile(metadataFileName)

            // Export comparison results
            val comparisonFileName = buildString {
                append(participantId.toString())
                append("_analysis_comparison.csv")
            }
            writeComparisonFile(comparisonFileName)

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setMessage("Files exported successfully!\n\n" +
                        "CSV Files saved to Downloads folder:\n" +
                        "• 8 angle data files: ParticipantID_GraphName.csv\n" +
                        "  (LeftHip, RightHip, LeftKnee, RightKnee, LeftAnkle, RightAnkle, Torso, Stride)\n" +
                        "• Metadata: ParticipantID_metadata.csv (height, stride length)\n" +
                        "• Analysis comparison: ParticipantID_analysis_comparison.csv\n\n" +
                        "Access via Files app → Downloads folder\n" +
                        "Easy to transfer to computer via USB!")
                .setTitle("Successfully Exported")

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        val mainMenuBtn = findViewById<Button>(R.id.main_mnu_btn)
        mainMenuBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val sharedPref = getSharedPreferences("HelpPrefs", Context.MODE_PRIVATE)
        val isHelpShown = sharedPref.getBoolean("Help03Shown", false)

        if (!isHelpShown) {
            showHelpDialog()

            val editor = sharedPref.edit()
            editor.putBoolean("Help03Shown", true)
            editor.apply()
        }

        val help03Btn = findViewById<Button>(R.id.help03_btn)
        help03Btn.setOnClickListener {
            showHelpDialog()
        }

    }

    private fun showHelpDialog() {
        val dialogBinding = layoutInflater.inflate(R.layout.help03_dialog, null)

        val myDialog = Dialog(this)
        myDialog.setContentView(dialogBinding)

        myDialog.setCancelable(false)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()

        val yes03Btn = dialogBinding.findViewById<Button>(R.id.help03_yes)
        yes03Btn.setOnClickListener {
            myDialog.dismiss()
        }
    }

    //Function for writing angle data to file
    private fun writeToFile(fileName:String, fileData:MutableList<Float>) {
        // Use Downloads folder - publicly accessible, no permissions needed on Android 10+
        val fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outputFile = File(fileDirectory, fileName)

        FileOutputStream(outputFile).use { output ->
            val identifiersText = "Frame #,Angle\n"
            output.write(identifiersText.toByteArray())
            for(i in 0 until fileData.size)
            {
                val floatData = fileData[i].toString()
                val index = i.toString()
                output.write(index.toByteArray())
                output.write(",".toByteArray())
                output.write(floatData.toByteArray())
                output.write("\n".toByteArray())
            }
        }
    }

    //Function for writing comparison analysis results to file
    private fun writeComparisonFile(fileName: String) {
        // Use Downloads folder - publicly accessible, no permissions needed on Android 10+
        val fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outputFile = File(fileDirectory, fileName)

        FileOutputStream(outputFile).use { output ->
            // Header
            val header = "Participant ID,Analysis Method,Gait Score,Difference,Notes\n"
            output.write(header.toByteArray())

            // Autoencoder row
            val autoRow = "$participantId,Autoencoder,${autoencoderScore.roundToLong()},,Neural network-based analysis\n"
            output.write(autoRow.toByteArray())

            // PCA row
            val difference = pcaScore - autoencoderScore
            val diffStr = if (difference > 0) "+${difference.roundToLong()}" else "${difference.roundToLong()}"
            val pcaRow = "$participantId,PCA,${pcaScore.roundToLong()},$diffStr,Linear dimensionality reduction\n"
            output.write(pcaRow.toByteArray())

            // Summary row
            val summaryRow = ",,,,Comparison for educational purposes\n"
            output.write(summaryRow.toByteArray())
        }

        Log.d("GaitAnalysis", "Comparison file exported: $fileName")
    }

    //Function for writing participant metadata to file
    private fun writeMetadataFile(fileName: String) {
        // Use Downloads folder - publicly accessible, no permissions needed on Android 10+
        val fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outputFile = File(fileDirectory, fileName)

        FileOutputStream(outputFile).use { output ->
            // Header
            val header = "Field,Value\n"
            output.write(header.toByteArray())

            // Participant ID
            val idRow = "ParticipantID,$participantId\n"
            output.write(idRow.toByteArray())

            // Height in inches (needed for stride calculation)
            val heightRow = "HeightInches,$participantHeight\n"
            output.write(heightRow.toByteArray())

            // Calculated stride length
            val strideLength = calcStrideLengthAvg(participantHeight.toFloat() * 39.37F)
            val strideRow = "StrideLengthAvg,$strideLength\n"
            output.write(strideRow.toByteArray())

            // Video length
            val videoRow = "VideoLengthMicroseconds,$videoLength\n"
            output.write(videoRow.toByteArray())
        }

        Log.d("GaitAnalysis", "Metadata file exported: $fileName")
    }

    //Function for renaming the edited video
    private fun renameTo(participantId:String) {
        val vidName = buildString {     //String is built to include participant ID in the name
            append(participantId)
            append("_video.mp4")
        }

        // Use app-specific external storage - no permissions needed on Android 10+
        val oldFilePath = File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "edited_video.mp4")    //Path of the existing edited video
        val newFilePath = File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), vidName)                           //New path for the renamed video

        editedUri = Uri.fromFile(newFilePath)

        oldFilePath.renameTo(newFilePath)
    }

}
