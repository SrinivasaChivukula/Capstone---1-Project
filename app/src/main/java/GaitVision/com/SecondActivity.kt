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
import android.os.Handler
import android.os.Looper
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
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE
import java.security.AccessController.getContext


class SecondActivity : ComponentActivity()
{

    private lateinit var mBinding: ActivitySecondBinding
    private var videoUri: Uri?=null
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable : Runnable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.calAngleBtn.setOnClickListener{
            updateRunnable?.let{
                handler.removeCallbacks(it)
            }
            startActivity(Intent(this, LastActivity::class.java))
        }

        val chooseAngleBtn = findViewById<Button>(R.id.choose_agl_btn)
        val popupMenu = PopupMenu(this, chooseAngleBtn)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        if(galleryUri == null)
        {
            startActivity(Intent(this, MainActivity::class.java))
        }

        popupMenu.setOnMenuItemClickListener { menuItem -> val id = menuItem.itemId

            if (id == R.id.menu_hip){
                updateRunnable?.let {
                    handler.removeCallbacks(it)
                }
                processAngle("HIP ANGLES")
            }
            else if (id == R.id.menu_knee){
                updateRunnable?.let {
                    handler.removeCallbacks(it)
                }
                processAngle("KNEE ANGLES")
            }
            else if (id == R.id.menu_ankle){
                updateRunnable?.let {
                    handler.removeCallbacks(it)
                }
                processAngle("ANKLE ANGLES")
            }
            else if (id == R.id.menu_torso){
                updateRunnable?.let {
                    handler.removeCallbacks(it)
                }
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

    fun processAngle(angle: String)
    {
        // toggle here
        val angleChoice = findViewById<TextView>(R.id.choose_agl_btn)
        //val name = "ALL ANGLES"
        val name = angle
        angleChoice.text = name.toString()

        count = 0
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
                    mBinding.AnkleAngle.visibility = GONE
                    mBinding.KneeAngle.visibility = GONE
                    mBinding.HipAngle.visibility = GONE
                    mBinding.TorsoAngle.visibility = GONE
                    mBinding.chooseAglBtn.isClickable = FALSE
                    if(frameList.isEmpty())
                    {
                        editedUri = withContext(Dispatchers.IO)
                        {
                            ProcVidEmpty(
                                this@SecondActivity,
                                outputFilePath,
                                mBinding,
                                name
                            )
                        }
                        Log.d("ErrorCheck","VideoSize: ${frameList.size}")
                        Log.d("ErrorCheck","LeftKneeListSize: ${leftKneeAngles.size}")
                        Log.d("ErrorCheck","RightKneeListSize: ${rightKneeAngles.size}")
                        Log.d("ErrorCheck","LeftAnkleListSize: ${leftAnkleAngles.size}")
                        Log.d("ErrorCheck","RightAnkleListSize: ${rightAnkleAngles.size}")
                        Log.d("ErrorCheck","LeftHipListSize: ${leftHipAngles.size}")
                        Log.d("ErrorCheck","RightHipListSize: ${rightHipAngles.size}")
                        Log.d("ErrorCheck","TorsoListSize: ${torsoAngles.size}")
                        Log.d("ErrorCheck","Count: $count")
                        Log.d("ErrorCheck","Coms: ${centerOfMasses}")


                        // Log check to see example of mutable list
                        Log.d("MutableListContents", "leftKneeAngles after processing: $leftKneeAngles")
                        Log.d("MutableListContents", "rightKneeAngles after processing: $rightKneeAngles")
                        val LeftHipMin = FindLocalMin(leftHipAngles)
                        val LeftHipMax = FindLocalMax(leftHipAngles)
                        Log.d("ErrorCheck", "Left Hip Min: $LeftHipMin, Max: $LeftHipMax")

                        val RightHipMin = FindLocalMin(rightHipAngles)
                        val RightHipMax = FindLocalMax(rightHipAngles)
                        Log.d("ErrorCheck", "Right Hip Min: $RightHipMin, Max: $RightHipMax")

                        Log.d("ErrorCheck","---------------")
                        val LeftAnkleMin = FindLocalMin(leftAnkleAngles)
                        val LeftAnkleMax = FindLocalMax(leftAnkleAngles)
                        Log.d("ErrorCheck", "Left Ankle Min: $LeftAnkleMin, Max: $LeftAnkleMax")

                        val RightAnkleMin = FindLocalMin(rightAnkleAngles)
                        val RightAnkleMax = FindLocalMax(rightAnkleAngles)
                        Log.d("ErrorCheck", "Right Ankle Min: $RightAnkleMin, Max: $RightAnkleMax")
                        // Log check to see Local Min/Max
                        val LeftKneeMin = FindLocalMin(leftKneeAngles)
                        val LeftKneeMax = FindLocalMax(leftKneeAngles)
                        Log.d("ErrorCheck", "Left Knee Min: $LeftKneeMin, Max: $LeftKneeMax")

                        val RightKneeMin = FindLocalMin(rightKneeAngles)
                        val RightKneeMax = FindLocalMax(rightKneeAngles)
                        Log.d("ErrorCheck", "Right Knee Min: $RightKneeMin, Max: $RightKneeMax")

                        val TorsoMin = FindLocalMin(torsoAngles)
                        val TorsoMax = FindLocalMax(torsoAngles)
                        Log.d("ErrorCheck", "Torso Min: $TorsoMin, Max: $TorsoMax")
                        val stanceTimesL = calculateStanceTimes(leftAnkleAngles)
                        val avgStanceTimeL = averageStanceTime(stanceTimesL)

                        val swingTimesL = calculateSwingTimes(leftAnkleAngles)
                        val avgSwingTimeL = averageSwingTime(swingTimesL)
                        Log.d("ErrorCheck","Average Swing Time Left(s): $avgSwingTimeL seconds")
                        val swingTimesR = calculateSwingTimes(rightAnkleAngles)
                        val avgSwingTimeR = averageSwingTime(swingTimesR)
                        Log.d("ErrorCheck","Average Swing Time Right(s): $avgSwingTimeR seconds")
                        Log.d("ErrorCheck","Left Step Time(s): $avgStanceTimeL seconds")
                        val stanceTimesR = calculateStanceTimes(rightAnkleAngles)
                        val avgStanceTimeR = averageStanceTime(stanceTimesR)
                        Log.d("ErrorCheck","Right Step Time(s): $avgStanceTimeR seconds")
                        Log.d("ErrorCheck","Swing-Stance Ratio Left: ${calculateSwingStanceRatio(avgSwingTimeL,avgStanceTimeL)}")
                        Log.d("ErrorCheck","Swing-Stance Ratio Right: ${calculateSwingStanceRatio(avgSwingTimeR,avgStanceTimeR)}")

                        var sum = calcStrideLength(participantHeight.toFloat()) // Change height here to ur height in inches
                        var strideSpeedAvg = sum / (videoLength * 0.000001)

                        Log.d("ErrorCheck", "Stride Speed AVG(In/s): ${strideSpeedAvg}")
                        Log.d("ErrorCheck", "Stride Length AVG(In): ${calcStrideLengthAvg(participantHeight.toFloat())}") // Change height here to ur height in inches
                        Log.d("ErrorCheck","---------------")
                        Log.d("ErrorCheck","Stride Angles: ${FindLocalMax(strideAngles)}")
                        Log.d("ErrorCheck","Lowest Ankle Left Placements: ${minLeftAnkleY.max()}")
                        Log.d("ErrorCheck","Lowest Ankle Right Placements: ${minRightAnkleY.max()}")

                        Log.d("ErrorCheck","Stance Times Left(s): ${calculateStanceTimes(leftAnkleAngles)}")
                        Log.d("ErrorCheck","Stance Times Right(s): ${calculateStanceTimes(rightAnkleAngles)}")
                        Log.d("ErrorCheck","Swing Times Left(s): ${calculateSwingTimes(leftAnkleAngles)}")
                        Log.d("ErrorCheck","Swing Times Right(s): ${calculateSwingTimes(rightAnkleAngles)}")
                    }
                    else
                    {
                        editedUri = withContext(Dispatchers.IO) {
                            ProcVidCon(
                                this@SecondActivity,
                                outputFilePath,
                                mBinding,
                                name
                            )
                        }
                        Log.d("ErrorCheck","VideoSize: ${frameList.size}")
                        Log.d("ErrorCheck","LeftKneeListSize: ${leftKneeAngles.size}")
                        Log.d("ErrorCheck","RightKneeListSize: ${rightKneeAngles.size}")
                        Log.d("ErrorCheck","LeftAnkleListSize: ${leftAnkleAngles.size}")
                        Log.d("ErrorCheck","RightAnkleListSize: ${rightAnkleAngles.size}")
                        Log.d("ErrorCheck","LeftHipListSize: ${leftHipAngles.size}")
                        Log.d("ErrorCheck","RightHipListSize: ${rightHipAngles.size}")
                        Log.d("ErrorCheck","TorsoListSize: ${torsoAngles.size}")
                        Log.d("ErrorCheck","Count: $count")
                        Log.d("ErrorCheck","Coms: ${centerOfMasses}")


                        // Log check to see example of mutable list
                        Log.d("MutableListContents", "leftKneeAngles after processing: $leftKneeAngles")
                        Log.d("MutableListContents", "rightKneeAngles after processing: $rightKneeAngles")
                        val LeftHipMin = FindLocalMin(leftHipAngles)
                        val LeftHipMax = FindLocalMax(leftHipAngles)
                        Log.d("ErrorCheck", "Left Hip Min: $LeftHipMin, Max: $LeftHipMax")

                        val RightHipMin = FindLocalMin(rightHipAngles)
                        val RightHipMax = FindLocalMax(rightHipAngles)
                        Log.d("ErrorCheck", "Right Hip Min: $RightHipMin, Max: $RightHipMax")

                        Log.d("ErrorCheck","---------------")
                        val LeftAnkleMin = FindLocalMin(leftAnkleAngles)
                        val LeftAnkleMax = FindLocalMax(leftAnkleAngles)
                        Log.d("ErrorCheck", "Left Ankle Min: $LeftAnkleMin, Max: $LeftAnkleMax")

                        val RightAnkleMin = FindLocalMin(rightAnkleAngles)
                        val RightAnkleMax = FindLocalMax(rightAnkleAngles)
                        Log.d("ErrorCheck", "Right Ankle Min: $RightAnkleMin, Max: $RightAnkleMax")
// Log check to see Local Min/Max
                        val LeftKneeMin = FindLocalMin(leftKneeAngles)
                        val LeftKneeMax = FindLocalMax(leftKneeAngles)
                        Log.d("ErrorCheck", "Left Knee Min: $LeftKneeMin, Max: $LeftKneeMax")

                        val RightKneeMin = FindLocalMin(rightKneeAngles)
                        val RightKneeMax = FindLocalMax(rightKneeAngles)
                        Log.d("ErrorCheck", "Right Knee Min: $RightKneeMin, Max: $RightKneeMax")

                        val TorsoMin = FindLocalMin(torsoAngles)
                        val TorsoMax = FindLocalMax(torsoAngles)
                        Log.d("ErrorCheck", "Torso Min: $TorsoMin, Max: $TorsoMax")
                        val stanceTimesL = calculateStanceTimes(leftAnkleAngles)
                        val avgStanceTimeL = averageStanceTime(stanceTimesL)

                        val swingTimesL = calculateSwingTimes(leftAnkleAngles)
                        val avgSwingTimeL = averageSwingTime(swingTimesL)
                        Log.d("ErrorCheck","Average Swing Time Left(s): $avgSwingTimeL seconds")
                        val swingTimesR = calculateSwingTimes(rightAnkleAngles)
                        val avgSwingTimeR = averageSwingTime(swingTimesR)
                        Log.d("ErrorCheck","Average Swing Time Right(s): $avgSwingTimeR seconds")
                        Log.d("ErrorCheck","Left Step Time(s): $avgStanceTimeL seconds")
                        val stanceTimesR = calculateStanceTimes(rightAnkleAngles)
                        val avgStanceTimeR = averageStanceTime(stanceTimesR)
                        Log.d("ErrorCheck","Right Step Time(s): $avgStanceTimeR seconds")
                        Log.d("ErrorCheck","Swing-Stance Ratio Left: ${calculateSwingStanceRatio(avgSwingTimeL,avgStanceTimeL)}")
                        Log.d("ErrorCheck","Swing-Stance Ratio Right: ${calculateSwingStanceRatio(avgSwingTimeR,avgStanceTimeR)}")

                        var sum = calcStrideLength(participantHeight.toFloat()) // Change height here to ur height in inches
                        var strideSpeedAvg = sum / (videoLength * 0.000001)

                        Log.d("ErrorCheck", "Stride Speed AVG(In/s): ${strideSpeedAvg}")
                        Log.d("ErrorCheck", "Stride Length AVG(In): ${calcStrideLengthAvg(participantHeight.toFloat())}") // Change height here to ur height in inches
                        Log.d("ErrorCheck","---------------")
                        Log.d("ErrorCheck","Stride Angles: ${FindLocalMax(strideAngles)}")
                        Log.d("ErrorCheck","Lowest Ankle Left Placements: ${minLeftAnkleY.max()}")
                        Log.d("ErrorCheck","Lowest Ankle Right Placements: ${minRightAnkleY.max()}")

                        Log.d("ErrorCheck","Stance Times Left(s): ${calculateStanceTimes(leftAnkleAngles)}")
                        Log.d("ErrorCheck","Stance Times Right(s): ${calculateStanceTimes(rightAnkleAngles)}")
                        Log.d("ErrorCheck","Swing Times Left(s): ${calculateSwingTimes(leftAnkleAngles)}")
                        Log.d("ErrorCheck","Swing Times Right(s): ${calculateSwingTimes(rightAnkleAngles)}")
                    }
                    mBinding.SplittingText.visibility = GONE
                    mBinding.CreationText.visibility = GONE
                    mBinding.splittingBar.visibility = GONE
                    mBinding.VideoCreation.visibility = GONE
                    mBinding.splittingProgressValue.visibility = GONE
                    mBinding.CreatingProgressValue.visibility = GONE
                    mBinding.videoViewer.visibility = VISIBLE
                    mBinding.calAngleBtn.visibility = VISIBLE
                    mBinding.chooseAglBtn.isClickable = TRUE

                    MediaScannerConnection.scanFile(
                        this@SecondActivity,
                        arrayOf(editedUri?.path),
                        null
                    ) { path, uri ->
                        Log.d("GalleryUpdate", "File $path was scanned successfully with URI: $uri")
                    }
                    Log.d("ErrorChecking", "Function URI: ${editedUri}")
                    videoView.setVideoURI(editedUri)


                    updateRunnable = object : Runnable
                    {
                        override fun run()
                        {
                            if(videoView.isPlaying) {
                                var currentPos = videoView.currentPosition
                                var interval = 33
                                var index = currentPos / interval
                                if(name == "HIP ANGLES")
                                {
                                    mBinding.AnkleAngle.visibility = GONE
                                    mBinding.KneeAngle.visibility = GONE
                                    mBinding.HipAngle.visibility = VISIBLE
                                    mBinding.TorsoAngle.visibility = GONE
                                    var string = ""
                                    if (index < rightHipAngles.size) {
                                        string += "Right Hip:\n" + rightHipAngles[index].toString()

                                    } else {
                                        string+= "Right Hip:\nERROR"

                                    }
                                    if (index < leftHipAngles.size) {
                                        string += "\nLeft Hip:\n" + leftHipAngles[index].toString()
                                    } else {
                                        string += "\nLeft Hip:\nERROR"
                                    }
                                    mBinding.HipAngle.text = string
                                }
                                else if(name == "KNEE ANGLES")
                                {
                                    mBinding.AnkleAngle.visibility = GONE
                                    mBinding.KneeAngle.visibility = VISIBLE
                                    mBinding.HipAngle.visibility = GONE
                                    mBinding.TorsoAngle.visibility = GONE
                                    var string = ""
                                    if (index < rightKneeAngles.size) {
                                        string += "Right Knee:\n" + rightKneeAngles[index].toString()

                                    } else {
                                        string += "Right Knee:\nERROR"

                                    }
                                    if (index < leftKneeAngles.size) {
                                        string += "\nLeft Knee:\n" + leftKneeAngles[index].toString()
                                    } else {
                                        string += "\nLeft Knee:\nERROR"
                                    }
                                    mBinding.KneeAngle.text = string
                                }
                                else if(name == "ANKLE ANGLES")
                                {
                                    mBinding.AnkleAngle.visibility = VISIBLE
                                    mBinding.KneeAngle.visibility = GONE
                                    mBinding.HipAngle.visibility = GONE
                                    mBinding.TorsoAngle.visibility = GONE
                                    var string = ""
                                    if (index < rightAnkleAngles.size) {
                                        string += "Right Ankle:\n" + rightAnkleAngles[index].toString()

                                    } else {
                                        string += "Right Ankle:\nERROR"

                                    }
                                    if (index < leftAnkleAngles.size) {
                                        string += "\nLeft Ankle:\n" + leftAnkleAngles[index].toString()
                                    } else {
                                        string += "\nLeft Ankle:\nERROR"
                                    }
                                    mBinding.AnkleAngle.text = string
                                }
                                else if(name == "TORSO ANGLE")
                                {
                                    mBinding.AnkleAngle.visibility = GONE
                                    mBinding.KneeAngle.visibility = GONE
                                    mBinding.HipAngle.visibility = GONE
                                    mBinding.TorsoAngle.visibility = VISIBLE
                                    var string = ""
                                    if (index < torsoAngles.size) {
                                        string += "\nTorso:\n" + torsoAngles[index].toString()

                                    } else {
                                        string += "\nTorso:\nERROR"
                                    }
                                    mBinding.TorsoAngle.text = string
                                }
                                else if(name == "ALL ANGLES")
                                {
                                    mBinding.AnkleAngle.visibility = VISIBLE
                                    mBinding.KneeAngle.visibility = VISIBLE
                                    mBinding.HipAngle.visibility = VISIBLE
                                    mBinding.TorsoAngle.visibility = VISIBLE
                                    var stringA = ""
                                    if (index < rightAnkleAngles.size) {
                                        stringA += "Right Ankle:\n" + rightAnkleAngles[index].toString()

                                    } else {
                                        stringA += "Right Ankle:\nERROR"

                                    }
                                    if (index < leftAnkleAngles.size) {
                                        stringA += "\nLeft Ankle:\n" + leftAnkleAngles[index].toString()
                                    } else {
                                        stringA += "\nLeft Ankle:\nERROR"
                                    }
                                    mBinding.AnkleAngle.text = stringA

                                    var stringK = ""
                                    if (index < rightKneeAngles.size) {
                                        stringK += "Right Knee:\n" + rightKneeAngles[index].toString()

                                    } else {
                                        stringK += "Right Knee:\nERROR"

                                    }
                                    if (index < leftKneeAngles.size) {
                                        stringK += "\nLeft Knee:\n" + leftKneeAngles[index].toString()
                                    } else {
                                        stringK += "\nLeft Knee:\nERROR"
                                    }
                                    mBinding.KneeAngle.text = stringK

                                    var stringH = ""
                                    if (index < rightHipAngles.size) {
                                        stringH += "Right Hip:\n" + rightHipAngles[index].toString()

                                    } else {
                                        stringH += "Right Hip:\nERROR"

                                    }
                                    if (index < leftHipAngles.size) {
                                        stringH += "\nLeft Hip:\n" + leftHipAngles[index].toString()
                                    } else {
                                        stringH += "\nLeft Hip:\nERROR"
                                    }
                                    mBinding.HipAngle.text = stringH

                                    var stringT = ""
                                    if (index < torsoAngles.size) {
                                        stringT += "\nTorso:\n" + torsoAngles[index].toString()

                                    } else {
                                        stringT += "\nTorso:\nERROR"
                                    }
                                    mBinding.TorsoAngle.text = stringT
                                }
                            }

                            handler.postDelayed(this, 33)
                        }
                    }
                    videoView.setOnPreparedListener {
                        videoView.start()
                        handler.post(updateRunnable!!)
                    }
                    videoView.setOnCompletionListener {
//                                handler.removeCallbacks(updateRunnable)
                        Log.d("ErrorCheck", "Right Ankle Angles: ${rightAnkleAngles}")
                        Log.d("ErrorCheck", "Left Ankle Angles: ${leftAnkleAngles}")
                        Log.d("ErrorCheck", "Last Right Ankle: ${rightAnkleAngles[rightAnkleAngles.size-1]}\n")
                        Log.d("ErrorCheck", "Last Left Ankle: ${leftAnkleAngles[leftAnkleAngles.size-1]}\n")
                    }
                } catch(e:Exception){
                    Log.e("ErrorChecking","Error processing video: ${e.message}")
                }
            }
        } ?:run{
            Log.e("ErrorChecking", "Gallery URI is NULL")
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