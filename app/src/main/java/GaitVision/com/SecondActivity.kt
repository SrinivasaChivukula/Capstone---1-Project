package GaitVision.com

import GaitVision.com.databinding.ActivityMainBinding
import GaitVision.com.databinding.ActivitySecondBinding
import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File


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
        val videoView = findViewById<VideoView>(R.id.video_viewer)
        videoUri = Uri.parse(uriString)
        val mediaController = MediaController(this)
        videoView.setMediaController(mediaController)

        videoUri?.let{
            lifecycleScope.launch{
                try{
                    Log.d("ErrorChecking", "Gallery URI: ${videoUri}")
                    val outputPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath + "/processed_video.mp4"
                    val outputFilePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)}/processed_video.mp4"
                    val outputFile = File(outputFilePath)
                    if(outputFile.exists())
                    {
                        Log.d("ErrorChecking", "Video Exists")
                        outputFile.delete()
                    }
                    Log.d("ErrorChecking", "Before function")
                    var newVideoUri = processVideo(this@SecondActivity, it)
                    Log.d("ErrorChecking", "Function URI: ${newVideoUri}")
                    videoView.setVideoURI(newVideoUri)
                } catch(e:Exception){
                    Log.e("ErrorChecking","Error processing video: ${e.message}")
                }
            }
        } ?:run{
            Log.e("ErrorChecking", "Video URI is NULL")
        }


    }

}