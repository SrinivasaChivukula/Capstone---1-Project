package GaitVision.com

import GaitVision.com.databinding.ActivityMainBinding
import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.ArrayAdapter
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.EditText
import android.widget.Spinner

import android.graphics.Bitmap
import android.util.Log

class MainActivity : ComponentActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private var videoUri: Uri? = null
    private val REQUESTCODE_CAMERA = 1
    private val REQUESTCODE_GALLERY = 2
    private val REQUEST_CODE_PERMISSIONS = 101

    private lateinit var recordVideoLauncher: ActivityResultLauncher<Intent>

    private val getResult: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                galleryUri = it
                val OPTION_CLOSEST = MediaMetadataRetriever.OPTION_CLOSEST
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this, galleryUri)
                val frame = retriever.getFrameAtTime(0, OPTION_CLOSEST)
                mBinding.imageView5.setImageBitmap(frame)
                retriever.release()

                Log.d(
                    "ErrorChecking",
                    "galleryUri(RFAR): $galleryUri, galleryPath(RFAR): ${galleryUri?.path}"
                )
            }
        }


    private fun checkPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )

        if (!hasPermissions(*permissions)) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun hasPermissions(vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun proceedWithMediaAccess() {
        startIntentFromGallary()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                proceedWithMediaAccess()
            } else {
                Toast.makeText(
                    this,
                    "Permissions are required to access media files and camera.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()

        // Initialize all global variables
        galleryUri = null
        editedUri = null
        frameList.clear()
        leftAnkleAngles.clear()
        rightAnkleAngles.clear()
        leftKneeAngles.clear()
        rightKneeAngles.clear()
        leftHipAngles.clear()
        rightHipAngles.clear()
        torsoAngles.clear()
        leftKneeMinAngles.clear()
        leftKneeMaxAngles.clear()
        rightKneeMinAngles.clear()
        rightKneeMaxAngles.clear()
        torsoMinAngles.clear()
        torsoMaxAngles.clear()
        participantId = ""
        participantHeight = 0

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        recordVideoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Video saved at: $videoUri", Toast.LENGTH_LONG).show()
                    Log.d(
                        "ErrorChecking",
                        "VideoUri(SAFR): $videoUri, VideoPath(SAFR): ${videoUri?.path}"
                    )

                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(this, videoUri)
                    val frame =
                        retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST)
                    mBinding.imageView5.setImageBitmap(frame)  // Show first frame as preview
                    retriever.release()
                    galleryUri = videoUri
                    Log.d(
                        "ErrorChecking",
                        "galleryUri(SAFR): $galleryUri, galleryPath(SAFR): ${galleryUri?.path}"
                    )

                } else {
                    Toast.makeText(this, "Video recording canceled", Toast.LENGTH_SHORT).show()
                }
            }

        //Initialize spinner Values
        val feetSpinner = findViewById<Spinner>(R.id.feet_spinner)
        val inchesSpinner = findViewById<Spinner>(R.id.inches_spinner)

        //Spinner Adapter for feet variable
        val feetAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.feet_array)
        )
        feetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        feetSpinner.adapter = feetAdapter

        //Spinner adapter for inches variable
        val inchesAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.inches_array)
        )
        inchesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        inchesSpinner.adapter = inchesAdapter

        //Default Selection is 5 feet 9 inches
        feetSpinner.setSelection(1)
        inchesSpinner.setSelection(9)
        
        mBinding.confirmVidBtn.setOnClickListener {
            val inputId = findViewById<EditText>(R.id.participant_id)
            participantId = inputId.text.toString()

            //feet and inches values are gathered from the spinners
            val feet = feetSpinner.selectedItem.toString().toIntOrNull() ?: 0
            val inches = inchesSpinner.selectedItem.toString().toIntOrNull() ?: 0
            participantHeight = (feet * 12) + inches

            startActivity(Intent(this, SecondActivity::class.java))
        }
            mBinding.openGalBtn.setOnClickListener { startIntentFromGallary() }

        mBinding.cameraBtn.setOnClickListener{
            openCameraForVideo()
        }

            //Creating typing animation
            val textView = findViewById<TextView>(R.id.textView1)
            val label = " GaitVision"
            val stringBuilder = StringBuilder()

            Thread {
                for (letter in label) {
                    stringBuilder.append(letter)
                    Thread.sleep(150)
                    runOnUiThread {
                        textView.text = stringBuilder.toString()
                    }
                }
            }.start()

            val sharedPref = getSharedPreferences("HelpPrefs", Context.MODE_PRIVATE)
            val isHelpShown = sharedPref.getBoolean("Help01Shown", false)

            if (!isHelpShown) {
                showHelpDialog()

                val editor = sharedPref.edit()
                editor.putBoolean("Help01Shown", true)
                editor.apply()
            }

            val help01Btn = findViewById<Button>(R.id.help01_btn)
            help01Btn.setOnClickListener {
                showHelpDialog()
            }

        }


        private fun showHelpDialog() {
            val dialogBinding = layoutInflater.inflate(R.layout.help01_dialog, null)

            val myDialog = Dialog(this)
            myDialog.setContentView(dialogBinding)

            myDialog.setCancelable(false)
            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.show()

            val yes01Btn = dialogBinding.findViewById<Button>(R.id.help01_yes)
            yes01Btn.setOnClickListener {
                myDialog.dismiss()
            }
        }

        private fun initClicks() {
            mBinding.openGalBtn.setOnClickListener {
                startIntentFromGallary()
            }
        }

        private fun startIntentFromGallary() {
            getResult.launch("video/*")
        }

    private fun openCameraForVideo() {
        videoUri = createVideoUri()
        Log.d("ErrorChecking", "VideoUri(OCFV): $videoUri, VideoPath(OCFV): ${videoUri?.path}")
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
            putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1) // High-quality video
        }
        recordVideoLauncher.launch(intent)
    }

    private fun createVideoUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "recordedvideo${System.currentTimeMillis()}.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/GaitVision")
        }
        return contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    }