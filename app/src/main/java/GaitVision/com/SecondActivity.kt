package GaitVision.com

import GaitVision.com.databinding.ActivityMainBinding
import GaitVision.com.databinding.ActivitySecondBinding
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.PopupMenu
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
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
                            mBinding.videoViewer.visibility = GONE
                            mBinding.SplittingText.visibility = GONE
                            mBinding.CreationText.visibility = GONE
                            mBinding.splittingBar.visibility = GONE
                            mBinding.VideoCreation.visibility = GONE
                            mBinding.splittingProgressValue.visibility = GONE
                            mBinding.CreatingProgressValue.visibility = GONE
                            mBinding.selectAngleText.visibility = GONE
                            var newVideoUri = withContext(Dispatchers.IO){ProcVid(this@SecondActivity, it, outputFilePath,mBinding, "hip")}
                            mBinding.SplittingText.visibility = GONE
                            mBinding.CreationText.visibility = GONE
                            mBinding.splittingBar.visibility = GONE
                            mBinding.VideoCreation.visibility = GONE
                            mBinding.splittingProgressValue.visibility = GONE
                            mBinding.CreatingProgressValue.visibility = GONE
                            mBinding.videoViewer.visibility = VISIBLE
                            mBinding.calAngleBtn.visibility = VISIBLE

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
            else if (id == R.id.menu_knee){
                // toggle here
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
                            mBinding.videoViewer.visibility = GONE
                            mBinding.SplittingText.visibility = GONE
                            mBinding.CreationText.visibility = GONE
                            mBinding.splittingBar.visibility = GONE
                            mBinding.VideoCreation.visibility = GONE
                            mBinding.splittingProgressValue.visibility = GONE
                            mBinding.CreatingProgressValue.visibility = GONE
                            mBinding.selectAngleText.visibility = GONE
                            var newVideoUri = withContext(Dispatchers.IO){ProcVid(this@SecondActivity, it, outputFilePath,mBinding, "knee")}
                            mBinding.SplittingText.visibility = GONE
                            mBinding.CreationText.visibility = GONE
                            mBinding.splittingBar.visibility = GONE
                            mBinding.VideoCreation.visibility = GONE
                            mBinding.splittingProgressValue.visibility = GONE
                            mBinding.CreatingProgressValue.visibility = GONE
                            mBinding.videoViewer.visibility = VISIBLE
                            mBinding.calAngleBtn.visibility = VISIBLE

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
            else if (id == R.id.menu_ankle){
                // toggle here
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
                            mBinding.videoViewer.visibility = GONE
                            mBinding.SplittingText.visibility = GONE
                            mBinding.CreationText.visibility = GONE
                            mBinding.splittingBar.visibility = GONE
                            mBinding.VideoCreation.visibility = GONE
                            mBinding.splittingProgressValue.visibility = GONE
                            mBinding.CreatingProgressValue.visibility = GONE
                            mBinding.selectAngleText.visibility = GONE
                            var newVideoUri = withContext(Dispatchers.IO){ProcVid(this@SecondActivity, it, outputFilePath,mBinding, "ankle")}
                            mBinding.SplittingText.visibility = GONE
                            mBinding.CreationText.visibility = GONE
                            mBinding.splittingBar.visibility = GONE
                            mBinding.VideoCreation.visibility = GONE
                            mBinding.splittingProgressValue.visibility = GONE
                            mBinding.CreatingProgressValue.visibility = GONE
                            mBinding.videoViewer.visibility = VISIBLE
                            mBinding.calAngleBtn.visibility = VISIBLE

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
            else if (id == R.id.menu_torso){
                // toggle here
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
                            mBinding.videoViewer.visibility = GONE
                            mBinding.SplittingText.visibility = GONE
                            mBinding.CreationText.visibility = GONE
                            mBinding.splittingBar.visibility = GONE
                            mBinding.VideoCreation.visibility = GONE
                            mBinding.splittingProgressValue.visibility = GONE
                            mBinding.CreatingProgressValue.visibility = GONE
                            mBinding.selectAngleText.visibility = GONE
                            var newVideoUri = withContext(Dispatchers.IO){ProcVid(this@SecondActivity, it, outputFilePath,mBinding, "torso")}
                            mBinding.SplittingText.visibility = GONE
                            mBinding.CreationText.visibility = GONE
                            mBinding.splittingBar.visibility = GONE
                            mBinding.VideoCreation.visibility = GONE
                            mBinding.splittingProgressValue.visibility = GONE
                            mBinding.CreatingProgressValue.visibility = GONE
                            mBinding.videoViewer.visibility = VISIBLE
                            mBinding.calAngleBtn.visibility = VISIBLE

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
            else if (id == R.id.menu_all_agl){
                // toggle here

            }
            false
        }

        chooseAngleBtn.setOnClickListener {
            popupMenu.show()
        }

        val help02Btn = findViewById<Button>(R.id.help02_btn)
        help02Btn.setOnClickListener{
            val dialogBinding = layoutInflater.inflate(R.layout.help02_dialog, null)

            val myDialog = Dialog(this)
            myDialog.setContentView(dialogBinding)

            myDialog.setCancelable(false)
            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.show()

            val yes02Btn = dialogBinding.findViewById<Button>(R.id.help02_yes)
            yes02Btn.setOnClickListener{
                myDialog.dismiss()
            }

        }

    }

}
