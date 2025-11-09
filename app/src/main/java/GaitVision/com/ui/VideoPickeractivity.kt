package GaitVision.com.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import GaitVision.com.R
import android.widget.Button
import android.widget.VideoView
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts

class VideoPickerActivity : AppCompatActivity() {

    private var selectedVideo: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_picker)

        val videoView = findViewById<VideoView>(R.id.videoView)

        val pickVideoLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                selectedVideo = uri
                videoView.setVideoURI(uri)
                videoView.start()
            }

        findViewById<Button>(R.id.btnPick).setOnClickListener {
            pickVideoLauncher.launch("video/*")
        }

        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            val recordIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            startActivity(recordIntent)
        }
    }
}
