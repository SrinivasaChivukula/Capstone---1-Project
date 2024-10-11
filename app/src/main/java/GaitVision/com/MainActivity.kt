package GaitVision.com

import android.os.Bundle
import android.widget.Button
import android.content.Intent
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val confirmVidBtn = findViewById<Button>(R.id.confirm_vid_btn)
        confirmVidBtn.setOnClickListener{
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }
}