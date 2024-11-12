package GaitVision.com

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
        val fileData = "placeholder text"
        submitButton.setOnClickListener {
            val fileName = participantId.text.toString()    //filename is retrieved from the participant_id text field

            writeToFile(fileName, fileData)                 //write to file is called with file name and placeholder as parameters
        }

        val mainMenuBtn = findViewById<Button>(R.id.main_mnu_btn)
        mainMenuBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    //Function for writing to file
    private fun writeToFile(fileName:String, fileData:String) {
        val fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) //Directory of the Documents folder is located
        val outputFile = File(fileDirectory, fileName)

        FileOutputStream(outputFile).use {
                output -> output.write(fileData.toByteArray())
        }
    }
}