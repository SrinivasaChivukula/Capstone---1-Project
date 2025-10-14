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
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE

class SecondActivity : ComponentActivity()
{

    private lateinit var mBinding: ActivitySecondBinding
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable : Runnable? = null


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        mBinding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(mBinding.root)


        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                updateRunnable?.let {
                    handler.removeCallbacks(it)
                }
                finish()
            }
        })

        //Stop ruunnable if user wants to go to next page
        mBinding.calAngleBtn.setOnClickListener{
            updateRunnable?.let{
                handler.removeCallbacks(it)
            }
            startActivity(Intent(this, LastActivity::class.java))
        }

        val chooseAngleBtn = findViewById<Button>(R.id.choose_agl_btn)
        val popupMenu = PopupMenu(this, chooseAngleBtn)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        //Should never happen, but send user back to first page if they didn't select a video
        if(galleryUri == null)
        {
            startActivity(Intent(this, MainActivity::class.java))
        }

        //Function calls for angle selection dropdown
        popupMenu.setOnMenuItemClickListener { menuItem -> val id = menuItem.itemId

            if (id == R.id.menu_hip){
                updateRunnable?.let {
                    handler.removeCallbacks(it)
                }

                val frameLayout = findViewById<FrameLayout>(R.id.frame_layout)
                val constraintLayout = frameLayout.parent as ConstraintLayout
                val constraintSet = ConstraintSet()
                constraintSet.clone(constraintLayout)

                // Now change the top constraint dynamically
                constraintSet.connect(
                    frameLayout.id,
                    ConstraintSet.TOP,
                    R.id.HipAngle, // <-- this is the new view you want to constrain to
                    ConstraintSet.BOTTOM
                )

                // Apply the updated constraints
                constraintSet.applyTo(constraintLayout)
                processAngle("HIP ANGLES")
            }
            else if (id == R.id.menu_knee){
                updateRunnable?.let {
                    handler.removeCallbacks(it)
                }
                val frameLayout = findViewById<FrameLayout>(R.id.frame_layout)
                val constraintLayout = frameLayout.parent as ConstraintLayout
                val constraintSet = ConstraintSet()
                constraintSet.clone(constraintLayout)

                // Now change the top constraint dynamically
                constraintSet.connect(
                    frameLayout.id,
                    ConstraintSet.TOP,
                    R.id.KneeAngle, // <-- this is the new view you want to constrain to
                    ConstraintSet.BOTTOM
                )

                // Apply the updated constraints
                constraintSet.applyTo(constraintLayout)
                processAngle("KNEE ANGLES")
            }
            else if (id == R.id.menu_ankle){
                updateRunnable?.let {
                    handler.removeCallbacks(it)
                }
                val frameLayout = findViewById<FrameLayout>(R.id.frame_layout)
                val constraintLayout = frameLayout.parent as ConstraintLayout
                val constraintSet = ConstraintSet()
                constraintSet.clone(constraintLayout)

                // Now change the top constraint dynamically
                constraintSet.connect(
                    frameLayout.id,
                    ConstraintSet.TOP,
                    R.id.AnkleAngle, // <-- this is the new view you want to constrain to
                    ConstraintSet.BOTTOM
                )

                // Apply the updated constraints
                constraintSet.applyTo(constraintLayout)
                processAngle("ANKLE ANGLES")
            }
            else if (id == R.id.menu_torso){
                updateRunnable?.let {
                    handler.removeCallbacks(it)
                }
                val frameLayout = findViewById<FrameLayout>(R.id.frame_layout)
                val constraintLayout = frameLayout.parent as ConstraintLayout
                val constraintSet = ConstraintSet()
                constraintSet.clone(constraintLayout)

                // Now change the top constraint dynamically
                constraintSet.connect(
                    frameLayout.id,
                    ConstraintSet.TOP,
                    R.id.TorsoAngle, // <-- this is the new view you want to constrain to
                    ConstraintSet.BOTTOM
                )

                // Apply the updated constraints
                constraintSet.applyTo(constraintLayout)
                processAngle("TORSO ANGLE")
            }
            else if (id == R.id.menu_all_agl) {
                updateRunnable?.let {
                    handler.removeCallbacks(it)
                }
                processAngle("ALL ANGLES")
            }
            false
        }

        processAngle("ALL ANGLES")

        //Display popup menu when user clicks select angle
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

    fun processAngle(angle: String) {


        val angleChoice = findViewById<TextView>(R.id.choose_agl_btn)
        val name = angle
        angleChoice.text = name

        val videoView = findViewById<VideoView>(R.id.video_viewer)

        val mediaController = MediaController(this)
        videoView.setMediaController(mediaController)
        //Runnable for updating angles above video with angle for current frame
        updateRunnable = object : Runnable {
            override fun run() {
                if (videoView.isPlaying) {
                    var currentPos = videoView.currentPosition
                    var interval = 33
                    var index = currentPos / interval
                    if (name == "HIP ANGLES") {
                        mBinding.AnkleAngle.visibility = GONE
                        mBinding.KneeAngle.visibility = GONE
                        mBinding.HipAngle.visibility = VISIBLE
                        mBinding.TorsoAngle.visibility = GONE
                        var string = ""
                        if (index < rightHipAngles.size) {
                            string += "Right Hip:\n" + String.format("%.1f", rightHipAngles[index])

                        } else {
                            string += "Right Hip:\nERROR"

                        }
                        if (index < leftHipAngles.size) {
                            string += "\nLeft Hip:\n" + String.format("%.1f", leftHipAngles[index])
                        } else {
                            string += "\nLeft Hip:\nERROR"
                        }
                        mBinding.HipAngle.text = string
                    } else if (name == "KNEE ANGLES") {
                        mBinding.AnkleAngle.visibility = GONE
                        mBinding.KneeAngle.visibility = VISIBLE
                        mBinding.HipAngle.visibility = GONE
                        mBinding.TorsoAngle.visibility = GONE
                        var string = ""
                        if (index < rightKneeAngles.size) {
                            string += "Right Knee:\n" + String.format(
                                "%.1f",
                                rightKneeAngles[index]
                            )

                        } else {
                            string += "Right Knee:\nERROR"

                        }
                        if (index < leftKneeAngles.size) {
                            string += "\nLeft Knee:\n" + String.format(
                                "%.1f",
                                leftKneeAngles[index]
                            )
                        } else {
                            string += "\nLeft Knee:\nERROR"
                        }
                        mBinding.KneeAngle.text = string
                    } else if (name == "ANKLE ANGLES") {
                        mBinding.AnkleAngle.visibility = VISIBLE
                        mBinding.KneeAngle.visibility = GONE
                        mBinding.HipAngle.visibility = GONE
                        mBinding.TorsoAngle.visibility = GONE
                        var string = ""
                        if (index < rightAnkleAngles.size) {
                            string += "Right Ankle:\n" + String.format(
                                "%.1f",
                                rightAnkleAngles[index]
                            )

                        } else {
                            string += "Right Ankle:\nERROR"

                        }
                        if (index < leftAnkleAngles.size) {
                            string += "\nLeft Ankle:\n" + String.format(
                                "%.1f",
                                leftAnkleAngles[index]
                            )
                        } else {
                            string += "\nLeft Ankle:\nERROR"
                        }
                        mBinding.AnkleAngle.text = string
                    } else if (name == "TORSO ANGLE") {
                        mBinding.AnkleAngle.visibility = GONE
                        mBinding.KneeAngle.visibility = GONE
                        mBinding.HipAngle.visibility = GONE
                        mBinding.TorsoAngle.visibility = VISIBLE
                        var string = ""
                        if (index < torsoAngles.size) {
                            string += "\nTorso:\n" + String.format("%.1f", torsoAngles[index])

                        } else {
                            string += "\nTorso:\nERROR"
                        }
                        mBinding.TorsoAngle.text = string
                    } else if (name == "ALL ANGLES") {
                        mBinding.AnkleAngle.visibility = VISIBLE
                        mBinding.KneeAngle.visibility = VISIBLE
                        mBinding.HipAngle.visibility = VISIBLE
                        mBinding.TorsoAngle.visibility = VISIBLE
                        var stringA = ""
                        if (index < rightAnkleAngles.size) {
                            stringA += "Right Ankle:\n" + String.format(
                                "%.1f",
                                rightAnkleAngles[index]
                            )

                        } else {
                            stringA += "Right Ankle:\nERROR"

                        }
                        if (index < leftAnkleAngles.size) {
                            stringA += "\nLeft Ankle:\n" + String.format(
                                "%.1f",
                                leftAnkleAngles[index]
                            )
                        } else {
                            stringA += "\nLeft Ankle:\nERROR"
                        }
                        mBinding.AnkleAngle.text = stringA

                        var stringK = ""
                        if (index < rightKneeAngles.size) {
                            stringK += "Right Knee:\n" + String.format(
                                "%.1f",
                                rightKneeAngles[index]
                            )

                        } else {
                            stringK += "Right Knee:\nERROR"

                        }
                        if (index < leftKneeAngles.size) {
                            stringK += "\nLeft Knee:\n" + String.format(
                                "%.1f",
                                leftKneeAngles[index]
                            )
                        } else {
                            stringK += "\nLeft Knee:\nERROR"
                        }
                        mBinding.KneeAngle.text = stringK

                        var stringH = ""
                        if (index < rightHipAngles.size) {
                            stringH += "Right Hip:\n" + String.format("%.1f", rightHipAngles[index])
                        } else {
                            stringH += "Right Hip:\nERROR"

                        }
                        if (index < leftHipAngles.size) {
                            stringH += "\nLeft Hip:\n" + String.format("%.1f", leftHipAngles[index])
                        } else {
                            stringH += "\nLeft Hip:\nERROR"
                        }
                        mBinding.HipAngle.text = stringH

                        var stringT = ""
                        if (index < torsoAngles.size) {
                            stringT += "\nTorso:\n" + String.format("%.1f", torsoAngles[index])

                        } else {
                            stringT += "\nTorso:\nERROR"
                        }
                        mBinding.TorsoAngle.text = stringT
                    }
                }

                handler.postDelayed(this, 33)
            }
        }

        count = 0



        if (editedUri != null)
        {
            //Log check video size and list sizes
            Log.d("ErrorCheck", "RecordVideo edited URI: $editedUri")
            Log.d("ErrorCheck", "VideoSize: ${frameList.size}")
            Log.d("ErrorCheck", "LeftKneeListSize: ${leftKneeAngles.size}")
            Log.d("ErrorCheck", "RightKneeListSize: ${rightKneeAngles.size}")
            Log.d("ErrorCheck", "LeftAnkleListSize: ${leftAnkleAngles.size}")
            Log.d("ErrorCheck", "RightAnkleListSize: ${rightAnkleAngles.size}")
            Log.d("ErrorCheck", "LeftHipListSize: ${leftHipAngles.size}")
            Log.d("ErrorCheck", "RightHipListSize: ${rightHipAngles.size}")
            Log.d("ErrorCheck", "TorsoListSize: ${torsoAngles.size}")
            Log.d("ErrorCheck", "Count: $count")

            // Log check to see Local Min/Max
            val LeftHipMin = FindLocalMin(leftHipAngles)
            val LeftHipMax = FindLocalMax(leftHipAngles)
            val RightHipMin = FindLocalMin(rightHipAngles)
            val RightHipMax = FindLocalMax(rightHipAngles)
            val LeftAnkleMin = FindLocalMin(leftAnkleAngles)
            val LeftAnkleMax = FindLocalMax(leftAnkleAngles)
            val RightAnkleMin = FindLocalMin(rightAnkleAngles)
            val RightAnkleMax = FindLocalMax(rightAnkleAngles)
            val LeftKneeMin = FindLocalMin(leftKneeAngles)
            leftKneeMinAngles = LeftKneeMin.toMutableList()
            val LeftKneeMax = FindLocalMax(leftKneeAngles)
            leftKneeMaxAngles = LeftKneeMax.toMutableList()
            val RightKneeMin = FindLocalMin(rightKneeAngles)
            rightKneeMinAngles = RightKneeMin.toMutableList()
            val RightKneeMax = FindLocalMax(rightKneeAngles)
            rightKneeMaxAngles = RightKneeMax.toMutableList()
            val TorsoMin = FindLocalMin(torsoAngles)
            torsoMinAngles = TorsoMin.toMutableList()
            val TorsoMax = FindLocalMax(torsoAngles)
            torsoMaxAngles = TorsoMax.toMutableList()
            Log.d("ErrorCheck", "Left Hip Min: $LeftHipMin, Max: $LeftHipMax")
            Log.d("ErrorCheck", "Right Hip Min: $RightHipMin, Max: $RightHipMax")
            Log.d("ErrorCheck", "Left Ankle Min: $LeftAnkleMin, Max: $LeftAnkleMax")
            Log.d("ErrorCheck", "Right Ankle Min: $RightAnkleMin, Max: $RightAnkleMax")
            Log.d("ErrorCheck", "Left Knee Min: $LeftKneeMin, Max: $LeftKneeMax")
            Log.d("ErrorCheck", "Right Knee Min: $RightKneeMin, Max: $RightKneeMax")
            Log.d("ErrorCheck", "Torso Min: $TorsoMin, Max: $TorsoMax")
            var sum = calcStrideLength(participantHeight.toFloat())
            var strideSpeedAvg = sum / (videoLength * 0.000001)
            Log.d("ErrorCheck", "Stride Speed AVG(m/s): ${strideSpeedAvg}")
            Log.d(
                "ErrorCheck",
                "Stride Length AVG(m): ${calcStrideLengthAvg(participantHeight.toFloat())}"
            )
            Log.d("ErrorCheck", "Stride Angles: ${FindLocalMax(strideAngles)}")

            mBinding.SplittingText.visibility = GONE
            mBinding.CreationText.visibility = GONE
            mBinding.splittingBar.visibility = GONE
            mBinding.VideoCreation.visibility = GONE
            mBinding.splittingProgressValue.visibility = GONE
            mBinding.CreatingProgressValue.visibility = GONE
            mBinding.videoViewer.visibility = VISIBLE
            mBinding.calAngleBtn.visibility = VISIBLE
            mBinding.chooseAglBtn.isClickable = TRUE

            Log.d("ErrorCheck", "URI PATH: ${editedUri?.path}, URI: $editedUri")
            MediaScannerConnection.scanFile(
                this@SecondActivity,
                arrayOf(editedUri?.path),
                null
            ) { path, uri ->
                Log.d(
                    "ErrorCheck",
                    "File $path was scanned successfully with URI: $editedUri"
                )
            }
            Log.d("ErrorCheck", "Function URI: ${editedUri}")
            videoView.setVideoURI(editedUri)



            videoView.setOnPreparedListener {
                videoView.start()
                handler.post(updateRunnable!!)
            }
        } else
        {
            galleryUri?.let {
                lifecycleScope.launch {
                    try {
                        Log.d("ErrorChecking", "VideoUri is now galleryUri")
                        Log.d(
                            "ErrorChecking",
                            "galleryUri(RFAR): $galleryUri, galleryPath(RFAR): ${galleryUri?.path}"
                        )
                        val outputFilePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)}/edited_video.mp4"
                        val outputFile = File(outputFilePath)
                        if (outputFile.exists()) {
                            Log.d("ErrorChecking", "Video Exists")
                            outputFile.delete()
                        }
                        Log.d("ErrorChecking", "Before function")

                        //Update UI to remove all progress bars and text from screen and make angle
                        //selection button unclickable
                        mBinding.videoViewer.visibility = GONE
                        mBinding.SplittingText.visibility = GONE
                        mBinding.CreationText.visibility = GONE
                        mBinding.splittingBar.visibility = GONE
                        mBinding.VideoCreation.visibility = GONE
                        mBinding.splittingProgressValue.visibility = GONE
                        mBinding.CreatingProgressValue.visibility = GONE
                        mBinding.selectAngleText.visibility = GONE
                        mBinding.AnkleAngle.visibility = GONE
                        mBinding.KneeAngle.visibility = GONE
                        mBinding.HipAngle.visibility = GONE
                        mBinding.TorsoAngle.visibility = GONE
                        mBinding.chooseAglBtn.isClickable = FALSE
                        if (editedUri == null) {
                            editedUri = withContext(Dispatchers.IO)
                            {
                                ProcVidEmpty(
                                    this@SecondActivity,
                                    outputFilePath,
                                    mBinding
                                )
                            }
                            //Log check video size and list sizes
                            Log.d("ErrorCheck", "RecordVideo edited URI: $editedUri")
                            Log.d("ErrorCheck", "VideoSize: ${frameList.size}")
                            Log.d("ErrorCheck", "LeftKneeListSize: ${leftKneeAngles.size}")
                            Log.d("ErrorCheck", "RightKneeListSize: ${rightKneeAngles.size}")
                            Log.d("ErrorCheck", "LeftAnkleListSize: ${leftAnkleAngles.size}")
                            Log.d("ErrorCheck", "RightAnkleListSize: ${rightAnkleAngles.size}")
                            Log.d("ErrorCheck", "LeftHipListSize: ${leftHipAngles.size}")
                            Log.d("ErrorCheck", "RightHipListSize: ${rightHipAngles.size}")
                            Log.d("ErrorCheck", "TorsoListSize: ${torsoAngles.size}")
                            Log.d("ErrorCheck", "Count: $count")

                            // Log check to see Local Min/Max
                            val LeftHipMin = FindLocalMin(leftHipAngles)
                            val LeftHipMax = FindLocalMax(leftHipAngles)
                            val RightHipMin = FindLocalMin(rightHipAngles)
                            val RightHipMax = FindLocalMax(rightHipAngles)
                            val LeftAnkleMin = FindLocalMin(leftAnkleAngles)
                            val LeftAnkleMax = FindLocalMax(leftAnkleAngles)
                            val RightAnkleMin = FindLocalMin(rightAnkleAngles)
                            val RightAnkleMax = FindLocalMax(rightAnkleAngles)
                            val LeftKneeMin = FindLocalMin(leftKneeAngles)
                            leftKneeMinAngles = LeftKneeMin.toMutableList()
                            val LeftKneeMax = FindLocalMax(leftKneeAngles)
                            leftKneeMaxAngles = LeftKneeMax.toMutableList()
                            val RightKneeMin = FindLocalMin(rightKneeAngles)
                            rightKneeMinAngles = RightKneeMin.toMutableList()
                            val RightKneeMax = FindLocalMax(rightKneeAngles)
                            rightKneeMaxAngles = RightKneeMax.toMutableList()
                            val TorsoMin = FindLocalMin(torsoAngles)
                            torsoMinAngles = TorsoMin.toMutableList()
                            val TorsoMax = FindLocalMax(torsoAngles)
                            torsoMaxAngles = TorsoMax.toMutableList()
                            Log.d("ErrorCheck", "Left Hip Min: $LeftHipMin, Max: $LeftHipMax")
                            Log.d("ErrorCheck", "Right Hip Min: $RightHipMin, Max: $RightHipMax")
                            Log.d("ErrorCheck", "Left Ankle Min: $LeftAnkleMin, Max: $LeftAnkleMax")
                            Log.d(
                                "ErrorCheck",
                                "Right Ankle Min: $RightAnkleMin, Max: $RightAnkleMax"
                            )
                            Log.d("ErrorCheck", "Left Knee Min: $LeftKneeMin, Max: $LeftKneeMax")
                            Log.d("ErrorCheck", "Right Knee Min: $RightKneeMin, Max: $RightKneeMax")
                            Log.d("ErrorCheck", "Torso Min: $TorsoMin, Max: $TorsoMax")
                            var sum = calcStrideLength(participantHeight.toFloat())
                            var strideSpeedAvg = sum / (videoLength * 0.000001)
                            Log.d("ErrorCheck", "Stride Speed AVG(m/s): ${strideSpeedAvg}")
                            Log.d(
                                "ErrorCheck",
                                "Stride Length AVG(m): ${calcStrideLengthAvg(participantHeight.toFloat())}"
                            )
                            Log.d("ErrorCheck", "Stride Angles: ${FindLocalMax(strideAngles)}")
                        }

                        //Update UI to remove all video progressing progress bars and text and make
                        //Angle selection and analysis button clickable and visible.
                        mBinding.SplittingText.visibility = GONE
                        mBinding.CreationText.visibility = GONE
                        mBinding.splittingBar.visibility = GONE
                        mBinding.VideoCreation.visibility = GONE
                        mBinding.splittingProgressValue.visibility = GONE
                        mBinding.CreatingProgressValue.visibility = GONE
                        mBinding.videoViewer.visibility = VISIBLE
                        mBinding.calAngleBtn.visibility = VISIBLE
                        mBinding.chooseAglBtn.isClickable = TRUE

                        Log.d("ErrorCheck", "URI PATH: ${editedUri?.path}, URI: $editedUri")
                        MediaScannerConnection.scanFile(
                            this@SecondActivity,
                            arrayOf(editedUri?.path),
                            null
                        ) { path, uri ->
                            Log.d(
                                "ErrorCheck",
                                "File $path was scanned successfully with URI: $editedUri"
                            )
                        }
                        Log.d("ErrorCheck", "Function URI: ${editedUri}")
                        videoView.setVideoURI(editedUri)



                        videoView.setOnPreparedListener {
                            videoView.start()
                            handler.post(updateRunnable!!)
                        }

                    } catch (e: Exception) {
                        Log.e("ErrorCheck", "Error processing video: ${e.message}", e)
                        Log.e("ErrorCheck", "Generated URI: ${editedUri}")
                        e.printStackTrace()
                    }
                }
            } ?: run {
                Log.e("ErrorCheck", "Gallery URI is NULL")
            }
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
