package GaitVision.com

import android.os.Bundle
import android.widget.Button
import android.content.Intent
import androidx.activity.ComponentActivity

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val uploadCSVBtn = findViewById<Button>(R.id.upload_csv_btn)
        uploadCSVBtn.setOnClickListener {
            val intent = Intent(this, ThirdActivity::class.java)
            startActivity(intent)
        }
    }
}