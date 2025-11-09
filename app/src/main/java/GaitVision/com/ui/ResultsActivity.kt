package GaitVision.com.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import GaitVision.com.R
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart

class ResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val gaitScore = findViewById<TextView>(R.id.tvGaitScore)
        val lineChart = findViewById<LineChart>(R.id.lineChart)

        gaitScore.text = "Gait Score: 85 (example)"

        // TODO: populate chart with actual angle data
    }
}
