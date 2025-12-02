package GaitVision.com.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import GaitVision.com.R
import android.widget.Button
import android.widget.TextView

class AnalysisActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        val selectedPath = intent.getStringExtra("videoPath")
        findViewById<TextView>(R.id.tvSelectedPath).text =
            "Selected Video: ${selectedPath ?: "none"}"

        findViewById<Button>(R.id.btnRunAnalysis).setOnClickListener {
            // TODO: Hook up actual ML processing
        }
    }
}
