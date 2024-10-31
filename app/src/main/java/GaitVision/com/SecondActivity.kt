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
import android.widget.PopupMenu
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

        mBinding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
//        mBinding= DataBindingUtil.setContentView(this, R.layout.activity_second)
        mBinding.calAngleBtn.setOnClickListener{startActivity(Intent(this, GraphActivity::class.java))}

        val chooseAngleBtn = findViewById<Button>(R.id.choose_agl_btn)
        val popupMenu = PopupMenu(this, chooseAngleBtn)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem -> val id = menuItem.itemId

            if (id == R.id.menu_hip){
                // toggle here
            }
            else if (id == R.id.menu_knee){
                // toggle here
            }
            else if (id == R.id.menu_ankle){
                // toggle here
            }
            else if (id == R.id.menu_torso){
                // toggle here
            }

            false
        }

        chooseAngleBtn.setOnClickListener {
            popupMenu.show()
        }
        
//        setContentView(R.layout.activity_second)
//
//        val uploadCSVBtn = findViewById<Button>(R.id.upload_csv_btn)
//        uploadCSVBtn.setOnClickListener {
//            val intent = Intent(this, ThirdActivity::class.java)
//            startActivity(intent)
//        }



        val uriString = intent.getStringExtra("VIDEO_URI")
        val videoView = findViewById<VideoView>(R.id.video_viewer)
        videoUri = Uri.parse(uriString)
        val mediaController = MediaController(this)
        videoView.setMediaController(mediaController)

        videoUri?.let{
            lifecycleScope.launch{
                try{
                    Log.d("ErrorChecking", "Gallery URI: ${videoUri}")
                    val outputPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath + "/edited_video.mp4"
                    val outputFilePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)}/edited_video.mp4"
                    val outputFile = File(outputFilePath)
                    if(outputFile.exists())
                    {
                        Log.d("ErrorChecking", "Video Exists")
                        outputFile.delete()
                    }
                    Log.d("ErrorChecking", "Before function")
                    var newVideoUri = ProcVid(this@SecondActivity, it, outputFilePath)
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
