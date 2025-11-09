package GaitVision.com.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import GaitVision.com.R
import android.widget.Button
import android.widget.EditText

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val participantInput = findViewById<EditText>(R.id.etParticipantId)
        val heightInput = findViewById<EditText>(R.id.etHeight)

        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            startActivity(Intent(this, VideoPickerActivity::class.java))
        }

        findViewById<Button>(R.id.btnSelect).setOnClickListener {
            startActivity(Intent(this, VideoPickerActivity::class.java))
        }

        findViewById<Button>(R.id.btnAnalyze).setOnClickListener {
            val intent = Intent(this, AnalysisActivity::class.java)
            intent.putExtra("participantId", participantInput.text.toString())
            intent.putExtra("height", heightInput.text.toString())
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnViewAnalysis).setOnClickListener {
            startActivity(Intent(this, ResultsActivity::class.java))
        }

        findViewById<Button>(R.id.btnExportCsv).setOnClickListener {
            startActivity(Intent(this, ResultsActivity::class.java))
        }
    }
}
