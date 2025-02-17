package GaitVision.com


import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.os.Environment
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import androidx.activity.ComponentActivity
import java.io.File
import java.io.FileOutputStream
import android.widget.TextView

class LastActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_last)

        // temp random number generator for activity_last
        val scoreTextView = findViewById<TextView>(R.id.score_textview)
        val randomScore = (50..70).random()
        scoreTextView.text = randomScore.toString()

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
                graphHip.text = graphHipName.toString()

                hipGraph.visibility = View.VISIBLE
                kneeGraph.visibility = View.INVISIBLE
                ankleGraph.visibility = View.INVISIBLE
                torsoGraph.visibility = View.INVISIBLE
            }
            else if (id == R.id.menu_knee) {
                // knee graph
                val graphKnee = findViewById<TextView>(R.id.select_graph_btn)
                val graphKneeName = "KNEE GRAPH"
                graphKnee.text = graphKneeName.toString()

                hipGraph.visibility = View.INVISIBLE
                kneeGraph.visibility = View.VISIBLE
                ankleGraph.visibility = View.INVISIBLE
                torsoGraph.visibility = View.INVISIBLE
            }
            else if (id == R.id.menu_ankle) {
                // ankle graph
                val graphAnkle = findViewById<TextView>(R.id.select_graph_btn)
                val graphAnkleName = "ANKLE GRAPH"
                graphAnkle.text = graphAnkleName.toString()

                hipGraph.visibility = View.INVISIBLE
                kneeGraph.visibility = View.INVISIBLE
                ankleGraph.visibility = View.VISIBLE
                torsoGraph.visibility = View.INVISIBLE
            }
            else if (id == R.id.menu_torso){
                // torso graph
                val graphTorso = findViewById<TextView>(R.id.select_graph_btn)
                val graphTorsoName = "TORSO GRAPH"
                graphTorso.text = graphTorsoName.toString()

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
            append("Movies/")
            append(participantId)
            append("_video.mp4")
        }

        val oldFilePath = File(Environment.getExternalStorageDirectory(), "Movies/edited_video.mp4")    //Path of the existing edited video
        val newFilePath = File(Environment.getExternalStorageDirectory(), vidName)                           //New path for the renamed video

        oldFilePath.renameTo(newFilePath)
    }

}