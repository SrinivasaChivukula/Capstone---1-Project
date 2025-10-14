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

        //Load files needed for prediction
        val tfliteModel = FileUtil.loadMappedFile(this, "encoder_model.tflite")
        val interpreter = Interpreter(tfliteModel)
        val scalerMean = loadFloatBinFile(this, "scaler_mean.bin")
        val scalerScale = loadFloatBinFile(this, "scaler_scale.bin")
        val cleanCentroidStream = assets.open("clean_centroid.npy")
        val impairedCentroidStream = assets.open("impaired_centroid.npy")
        val cleanCentroid = loadNpyFloatArray(cleanCentroidStream)
        val impairedCentroid = loadNpyFloatArray(impairedCentroidStream)

        //Make sure no values are smaller than a threshold
        val minScaleValue = 1e-15f // Define a small threshold for scaler values
        val safeScalerScale = scalerScale.map {
            if (it < minScaleValue) minScaleValue else it
        }.toFloatArray()

        //Scale input
        val scaledInput = FloatArray(inputData.size) { i ->
            (inputData[i] - scalerMean[i]) / safeScalerScale[i]
        }

        //make input and output arrays for prediction
        val output = Array(1){FloatArray(2)}
        val input = arrayOf(scaledInput)

        //Predict output
        interpreter.run(input, output)

        //Log check values to see if they look correct
        Log.d("ErrorCheck", "ScalerMean: ${scalerMean.contentToString()}")
        Log.d("ErrorCheck", "ScalerScale: ${scalerScale.contentToString()}")
        Log.d("ErrorCheck", "InputData: ${inputData.contentToString()}")
        Log.d("ErrorCheck", "ScaledInputData: ${input[0].joinToString(", ")}")
        Log.d("ErrorCheck", "Output: ${output[0].contentToString()} Length: ${output[0].size}")
        Log.d("ErrorCheck", "Clean Centroid: ${cleanCentroid.contentToString()} Length: ${cleanCentroid.size}")
        Log.d("ErrorCheck", "Impaired Centroid: ${impairedCentroid.contentToString()} Length: ${impairedCentroid.size}")

        // Calculate the Euclidean distance between the encoded output and the centroids
        val distClean = euclideanDistance(output[0], cleanCentroid)
        val distImpaired = euclideanDistance(output[0], impairedCentroid)
        Log.d("ErrorCheck", "DistClean: $distClean")
        Log.d("ErrorCheck", "DistImpaired: $distImpaired")


        // Calculate the gait index
        val gaitIndexUnscaled = 1 - (distClean / (distClean + distImpaired))
        val gaitIndexScaled = gaitIndexUnscaled * 100  // Scale it from 0 to 100

        // Print or use the gait index
        Log.d("ErrorCheck", "Gait Index (Unscaled): $gaitIndexUnscaled")
        Log.d("ErrorCheck", "Gait Index (Scaled): $gaitIndexScaled")

        println("Gait Index (Unscaled): $gaitIndexUnscaled")
        println("Gait Index (Scaled): $gaitIndexScaled")

        //Update score
        var scoreTextView = findViewById<TextView>(R.id.score_textview)
        scoreTextView.text = gaitIndexScaled.roundToLong().toString()

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
            torsoAngles
        )

        val angleNames = listOf(        //list of names used for files
            "LeftHip",
            "RightHip",
            "LeftKnee",
            "RightKnee",
            "LeftAnkle",
            "RightAnkle",
            "Torso"
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

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setMessage("CSV Files saved to Documents as ParticipantID_GraphName.csv.\n\nUpdated video saved to Videos as ParticipantID_video.mp4")
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

    //Function for writing to file
    private fun writeToFile(fileName:String, fileData:MutableList<Float>) {
        val fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) //Directory of the Documents folder is located
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

    //Function for renaming the edited video
    private fun renameTo(participantId:String) {
        val vidName = buildString {     //String is built to include participant ID in the name
            append(participantId)
            append("_video.mp4")
        }

        val oldFilePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "edited_video.mp4")    //Path of the existing edited video
        val newFilePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), vidName)                           //New path for the renamed video

        editedUri = Uri.fromFile(newFilePath)

        oldFilePath.renameTo(newFilePath)
    }

}
