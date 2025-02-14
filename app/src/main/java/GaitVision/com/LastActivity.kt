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

        popupMenu.setOnMenuItemClickListener { menuItem -> val id = menuItem.itemId

            if (id == R.id.menu_hip) {
                // hip graph
                val graphHip = findViewById<TextView>(R.id.select_graph_btn)
                val graphHipName = "HIP GRAPH"
                graphHip.text = graphHipName.toString()
            }
            else if (id == R.id.menu_knee) {
                // knee graph
                val graphKnee = findViewById<TextView>(R.id.select_graph_btn)
                val graphKneeName = "KNEE GRAPH"
                graphKnee.text = graphKneeName.toString()
            }
            else if (id == R.id.menu_ankle) {
                // ankle graph
                val graphAnkle = findViewById<TextView>(R.id.select_graph_btn)
                val graphAnkleName = "ANKLE GRAPH"
                graphAnkle.text = graphAnkleName.toString()
            }
            else if (id == R.id.menu_torso){
                // torso graph
                val graphTorso = findViewById<TextView>(R.id.select_graph_btn)
                val graphTorsoName = "TORSO GRAPH"
                graphTorso.text = graphTorsoName.toString()
            }
            false
        }

        chooseGraphBtn.setOnClickListener {
            popupMenu.show()
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

}