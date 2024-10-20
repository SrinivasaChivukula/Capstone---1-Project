package GaitVision.com

import GaitVision.com.databinding.ActivityMainBinding
import GaitVision.com.databinding.ActivitySecondBinding
import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil

class SecondActivity : ComponentActivity() {

    private lateinit var mBinding: ActivitySecondBinding
    private var videoUri: Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_second)
//
//        val uploadCSVBtn = findViewById<Button>(R.id.upload_csv_btn)
//        uploadCSVBtn.setOnClickListener {
//            val intent = Intent(this, ThirdActivity::class.java)
//            startActivity(intent)
//        }

        mBinding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
//        mBinding= DataBindingUtil.setContentView(this, R.layout.activity_second)
        mBinding.uploadCsvBtn.setOnClickListener{startActivity(Intent(this,SecondActivity::class.java))}

        val uriString = intent.getStringExtra("VIDEO_URI")

        videoUri = Uri.parse(uriString)

        val videoView = findViewById<VideoView>(R.id.video_viewer)
        videoView.setVideoURI(videoUri)

        val mediaController = MediaController(this)
        videoView.setMediaController(mediaController)

//        mBinding.preview.setImageURI(videoUri)
        processFrames(this, videoUri)
    }

}