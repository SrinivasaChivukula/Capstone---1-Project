package GaitVision.com

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.os.Environment
import android.widget.EditText
import androidx.activity.ComponentActivity
import java.io.File
import java.io.FileOutputStream

/*class ThirdActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        //Functionality for writing to CSV
        val submitButton = findViewById<Button>(R.id.submit_id_btn)
        val participantId = findViewById<EditText>(R.id.participant_id)

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

        submitButton.setOnClickListener {

            for (i in fileData.indices) {       //for-loop iterates through the fileData list and creates a csv file for each of the angle graphs.
                val fileName = buildString {
                    append(participantId.text.toString())
                    append("_")
                    append(angleNames[i])
                    append(".csv")
                }    //filename participantId_angle

                writeToFile(fileName, fileData[i])                 //write to file is called with file name and placeholder as parameters
                renameTo(participantId.text.toString())
            }

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setMessage("CSV Files saved to Documents as ParticipantID_GraphName.csv.\n\nUpdated video saved to Videos as ParticipantID_video.mp4")
                .setTitle("Successfully Exported")

            val dialog: AlertDialog = builder.create()
            dialog.show()
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
}*/