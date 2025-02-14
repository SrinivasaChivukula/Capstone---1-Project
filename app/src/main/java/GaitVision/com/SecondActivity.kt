package GaitVision.com

import GaitVision.com.databinding.ActivitySecondBinding
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.MediaController
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.AccessController.getContext


class SecondActivity : ComponentActivity() {

    private lateinit var mBinding: ActivitySecondBinding
    private var videoUri: Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.calAngleBtn.setOnClickListener{startActivity(Intent(this, LastActivity::class.java))}

        val chooseAngleBtn = findViewById<Button>(R.id.choose_agl_btn)
        val popupMenu = PopupMenu(this, chooseAngleBtn)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        if(galleryUri == null)
        {
            startActivity(Intent(this, MainActivity::class.java))
        }

        popupMenu.setOnMenuItemClickListener { menuItem -> val id = menuItem.itemId

            if (id == R.id.menu_hip){
                // toggle here

                val angleHip = findViewById<TextView>(R.id.choose_agl_btn)
                val angleHipName = "HIP ANGLE"
                angleHip.text = angleHipName.toString()

//                val uriString = intent.getStringExtra("VIDEO_URI")
                val videoView = findViewById<VideoView>(R.id.video_viewer)
//                videoUri = Uri.parse(uriString)
                val mediaController = MediaController(this)
                videoView.setMediaController(mediaController)

                galleryUri?.let{
                    lifecycleScope.launch{
                        try{
                            Log.d("ErrorChecking", "Gallery URI: ${galleryUri}")
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
                            if(frameList.isEmpty()) {
                                editedUri = withContext(Dispatchers.IO) {
                                    ProcVidEmpty(
                                        this@SecondActivity,
                                        outputFilePath,
                                        mBinding,
                                        "hip"
                                    )
                                }
                            }
                            else
                            {
                                editedUri = withContext(Dispatchers.IO) {
                                    ProcVidCon(
                                        this@SecondActivity,
                                        outputFilePath,
                                        mBinding,
                                        "hip"
                                    )
                                }
                            }
                            mBinding.SplittingText.visibility = GONE
                            mBinding.CreationText.visibility = GONE
                            mBinding.splittingBar.visibility = GONE
                            mBinding.VideoCreation.visibility = GONE
                            mBinding.splittingProgressValue.visibility = GONE
                            mBinding.CreatingProgressValue.visibility = GONE
                            mBinding.videoViewer.visibility = VISIBLE
                            mBinding.calAngleBtn.visibility = VISIBLE

                            MediaScannerConnection.scanFile(
                                this@SecondActivity,
                                arrayOf(editedUri?.path),
                                null
                            ) { path, uri ->
                                Log.d("GalleryUpdate", "File $path was scanned successfully with URI: $uri")
                            }
                            Log.d("ErrorChecking", "Function URI: ${editedUri}")
                            videoView.setVideoURI(editedUri)
                        } catch(e:Exception){
                            Log.e("ErrorChecking","Error processing video: ${e.message}")
                        }
                    }
                } ?:run{
                    Log.e("ErrorChecking", "Gallery URI is NULL")
                }
            }
            else if (id == R.id.menu_knee){
                // toggle here

                val angleKnee = findViewById<TextView>(R.id.choose_agl_btn)
                val angleKneeName = "KNEE ANGLE"
                angleKnee.text = angleKneeName.toString()

//                val uriString = intent.getStringExtra("VIDEO_URI")
                val videoView = findViewById<VideoView>(R.id.video_viewer)
//                videoUri = Uri.parse(uriString)
                val mediaController = MediaController(this)
                videoView.setMediaController(mediaController)

                galleryUri?.let{
                    lifecycleScope.launch{
                        try{
                            Log.d("ErrorChecking", "Gallery URI: ${galleryUri}")
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
                            if(frameList.isEmpty()) {
                                editedUri = withContext(Dispatchers.IO) {
                                    ProcVidEmpty(
                                        this@SecondActivity,
                                        outputFilePath,
                                        mBinding,
                                        "knee"
                                    )
                                }
                            }
                            else
                            {
                                editedUri = withContext(Dispatchers.IO) {
                                    ProcVidCon(
                                        this@SecondActivity,
                                        outputFilePath,
                                        mBinding,
                                        "knee"
                                    )
                                }
                            }
                            mBinding.SplittingText.visibility = GONE
                            mBinding.CreationText.visibility = GONE
                            mBinding.splittingBar.visibility = GONE
                            mBinding.VideoCreation.visibility = GONE
                            mBinding.splittingProgressValue.visibility = GONE
                            mBinding.CreatingProgressValue.visibility = GONE
                            mBinding.videoViewer.visibility = VISIBLE
                            mBinding.calAngleBtn.visibility = VISIBLE

                            MediaScannerConnection.scanFile(
                                this@SecondActivity,
                                arrayOf(editedUri?.path),
                                null
                            ) { path, uri ->
                                Log.d("GalleryUpdate", "File $path was scanned successfully with URI: $uri")
                            }
                            Log.d("ErrorChecking", "Function URI: ${editedUri}")
                            videoView.setVideoURI(editedUri)
                        } catch(e:Exception){
                            Log.e("ErrorChecking","Error processing video: ${e.message}")
                        }
                    }
                } ?:run{
                    Log.e("ErrorChecking", "Gallery URI is NULL")
                }
            }
            else if (id == R.id.menu_ankle){
                // toggle here

                val angleAnkle = findViewById<TextView>(R.id.choose_agl_btn)
                val angleAnkleName = "ANKLE ANGLE"
                angleAnkle.text = angleAnkleName.toString()

//                val uriString = intent.getStringExtra("VIDEO_URI")
                val videoView = findViewById<VideoView>(R.id.video_viewer)
//                videoUri = Uri.parse(uriString)
                val mediaController = MediaController(this)
                videoView.setMediaController(mediaController)

                galleryUri?.let{
                    lifecycleScope.launch{
                        try{
                            Log.d("ErrorChecking", "Gallery URI: ${galleryUri}")
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
                            if(frameList.isEmpty()) {
                                editedUri = withContext(Dispatchers.IO) {
                                    ProcVidEmpty(
                                        this@SecondActivity,
                                        outputFilePath,
                                        mBinding,
                                        "ankle"
                                    )
                                }
                            }
                            else
                            {
                                editedUri = withContext(Dispatchers.IO) {
                                    ProcVidCon(
                                        this@SecondActivity,
                                        outputFilePath,
                                        mBinding,
                                        "ankle"
                                    )
                                }
                            }
                            mBinding.SplittingText.visibility = GONE
                            mBinding.CreationText.visibility = GONE
                            mBinding.splittingBar.visibility = GONE
                            mBinding.VideoCreation.visibility = GONE
                            mBinding.splittingProgressValue.visibility = GONE
                            mBinding.CreatingProgressValue.visibility = GONE
                            mBinding.videoViewer.visibility = VISIBLE
                            mBinding.calAngleBtn.visibility = VISIBLE

                            MediaScannerConnection.scanFile(
                                this@SecondActivity,
                                arrayOf(editedUri?.path),
                                null
                            ) { path, uri ->
                                Log.d("GalleryUpdate", "File $path was scanned successfully with URI: $uri")
                            }
                            Log.d("ErrorChecking", "Function URI: ${editedUri}")
                            videoView.setVideoURI(editedUri)
                        } catch(e:Exception){
                            Log.e("ErrorChecking","Error processing video: ${e.message}")
                        }
                    }
                } ?:run{
                    Log.e("ErrorChecking", "Gallery URI is NULL")
                }
            }
            else if (id == R.id.menu_torso){
                // toggle here

                val angleTorso = findViewById<TextView>(R.id.choose_agl_btn)
                val angleTorsoName = "TORSO ANGLE"
                angleTorso.text = angleTorsoName.toString()

//                val uriString = intent.getStringExtra("VIDEO_URI")
                val videoView = findViewById<VideoView>(R.id.video_viewer)
//                videoUri = Uri.parse(uriString)
                val mediaController = MediaController(this)
                videoView.setMediaController(mediaController)

                galleryUri?.let{
                    lifecycleScope.launch{
                        try{
                            Log.d("ErrorChecking", "Gallery URI: ${galleryUri}")
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
                            if(frameList.isEmpty()) {
                                editedUri = withContext(Dispatchers.IO) {
                                    ProcVidEmpty(
                                        this@SecondActivity,
                                        outputFilePath,
                                        mBinding,
                                        "torso"
                                    )
                                }
                            }
                            else
                            {
                                editedUri = withContext(Dispatchers.IO) {
                                    ProcVidCon(
                                        this@SecondActivity,
                                        outputFilePath,
                                        mBinding,
                                        "torso"
                                    )
                                }
                            }
                            mBinding.SplittingText.visibility = GONE
                            mBinding.CreationText.visibility = GONE
                            mBinding.splittingBar.visibility = GONE
                            mBinding.VideoCreation.visibility = GONE
                            mBinding.splittingProgressValue.visibility = GONE
                            mBinding.CreatingProgressValue.visibility = GONE
                            mBinding.videoViewer.visibility = VISIBLE
                            mBinding.calAngleBtn.visibility = VISIBLE

                            MediaScannerConnection.scanFile(
                                this@SecondActivity,
                                arrayOf(editedUri?.path),
                                null
                            ) { path, uri ->
                                Log.d("GalleryUpdate", "File $path was scanned successfully with URI: $uri")
                            }
                            Log.d("ErrorChecking", "Function URI: ${editedUri}")
                            videoView.setVideoURI(editedUri)
                        } catch(e:Exception){
                            Log.e("ErrorChecking","Error processing video: ${e.message}")
                        }
                    }
                } ?:run{
                    Log.e("ErrorChecking", "Gallery URI is NULL")
                }
            }
            else if (id == R.id.menu_all_agl){
                // toggle here

                val angleAll = findViewById<TextView>(R.id.choose_agl_btn)
                val angleAllName = "ALL ANGLES"
                angleAll.text = angleAllName.toString()

//                val uriString = intent.getStringExtra("VIDEO_URI")
                val videoView = findViewById<VideoView>(R.id.video_viewer)
//                videoUri = Uri.parse(uriString)
                val mediaController = MediaController(this)
                videoView.setMediaController(mediaController)

                galleryUri?.let{
                    lifecycleScope.launch{
                        try{
                            Log.d("ErrorChecking", "Gallery URI: ${galleryUri}")
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
                            if(frameList.isEmpty()) {
                                editedUri = withContext(Dispatchers.IO) {
                                    ProcVidEmpty(
                                        this@SecondActivity,
                                        outputFilePath,
                                        mBinding,
                                        "all"
                                    )
                                }
                            }
                            else
                            {
                                editedUri = withContext(Dispatchers.IO) {
                                    ProcVidCon(
                                        this@SecondActivity,
                                        outputFilePath,
                                        mBinding,
                                        "all"
                                    )
                                }
                            }
                            mBinding.SplittingText.visibility = GONE
                            mBinding.CreationText.visibility = GONE
                            mBinding.splittingBar.visibility = GONE
                            mBinding.VideoCreation.visibility = GONE
                            mBinding.splittingProgressValue.visibility = GONE
                            mBinding.CreatingProgressValue.visibility = GONE
                            mBinding.videoViewer.visibility = VISIBLE
                            mBinding.calAngleBtn.visibility = VISIBLE

                            MediaScannerConnection.scanFile(
                                this@SecondActivity,
                                arrayOf(editedUri?.path),
                                null
                            ) { path, uri ->
                                Log.d("GalleryUpdate", "File $path was scanned successfully with URI: $uri")
                            }
                            Log.d("ErrorChecking", "Function URI: ${editedUri}")
                            videoView.setVideoURI(editedUri)
                        } catch(e:Exception){
                            Log.e("ErrorChecking","Error processing video: ${e.message}")
                        }
                    }
                } ?:run{
                    Log.e("ErrorChecking", "Gallery URI is NULL")
                }

            }
            false
        }

        chooseAngleBtn.setOnClickListener {
            popupMenu.show()
        }

        val sharedPref = getSharedPreferences("HelpPrefs", Context.MODE_PRIVATE)
        val isHelpShown = sharedPref.getBoolean("Help02Shown", false)

        if (!isHelpShown) {
            showHelpDialog()

            val editor = sharedPref.edit()
            editor.putBoolean("Help02Shown", true)
            editor.apply()
        }

        val help02Btn = findViewById<Button>(R.id.help02_btn)
        help02Btn.setOnClickListener {
            showHelpDialog()
        }

    }

    private fun showHelpDialog() {
        val dialogBinding = layoutInflater.inflate(R.layout.help02_dialog, null)

        val myDialog = Dialog(this)
        myDialog.setContentView(dialogBinding)

        myDialog.setCancelable(false)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()

        val yes02Btn = dialogBinding.findViewById<Button>(R.id.help02_yes)
        yes02Btn.setOnClickListener {
            myDialog.dismiss()
        }
    }

}