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
import android.widget.EditText
import androidx.activity.ComponentActivity
import java.io.File
import java.io.FileOutputStream

class ThirdActivity : ComponentActivity() {
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
            rightAnkleAngles
        )

        val angleNames = listOf(        //list of names used for files
            "LeftHip",
            "RightHip",
            "LeftKnee",
            "RightKnee",
            "LeftAnkle",
            "RightAnkle",
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
            }

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setMessage("CSV File saved to Documents")
                .setTitle("Successfully Exported")

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        val mainMenuBtn = findViewById<Button>(R.id.main_mnu_btn)
        mainMenuBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        /*val help03Btn = findViewById<Button>(R.id.help03_btn)
        help03Btn.setOnClickListener{
            val dialogBinding = layoutInflater.inflate(R.layout.help03_dialog, null)

            val myDialog = Dialog(this)
            myDialog.setContentView(dialogBinding)

            myDialog.setCancelable(false)
            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.show()

            val yes03Btn = dialogBinding.findViewById<Button>(R.id.help03_yes)
            yes03Btn.setOnClickListener{
                myDialog.dismiss()
            }

        }*/

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
}