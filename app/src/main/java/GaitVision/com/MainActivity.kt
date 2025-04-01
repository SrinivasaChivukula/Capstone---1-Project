package GaitVision.com

import GaitVision.com.databinding.ActivityMainBinding
import android.os.Bundle
import android.widget.Button
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.EditText
import android.graphics.Bitmap

class MainActivity : ComponentActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private var videoUri: Uri?=null
    private val REQUESTCODE_CAMERA=1
    private val REQUESTCODE_GALLERY=2
    private val REQUEST_CODE_PERMISSIONS = 101

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
            }
        }

    private val takePictureLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Handle the captured image if needed
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                // You can use the bitmap here if you want to display it
                // mBinding.imageView5.setImageBitmap(imageBitmap)
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
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun proceedWithMediaAccess() {
        startIntentFromGallary()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                proceedWithMediaAccess()
            } else {
                Toast.makeText(this, "Permissions are required to access media files and camera.", Toast.LENGTH_SHORT).show()
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
        participantId = ""
        participantHeight = 0

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.confirmVidBtn.setOnClickListener {
            val inputId = findViewById<EditText>(R.id.participant_id)
            participantId = inputId.text.toString()
            val heightId = findViewById<EditText>(R.id.height_id)
            var heightText = heightId.text.toString()
            participantHeight = heightText.toIntOrNull() ?: 0
            startActivity(Intent(this, SecondActivity::class.java))
        }

        mBinding.openGalBtn.setOnClickListener { startIntentFromGallary() }

        // Add camera button listener
        mBinding.cameraBtn.setOnClickListener { // Make sure you have a camera_btn in your layout
            openCamera()
        }

        // Typing animation
        val textView = findViewById<TextView>(R.id.textView1)
        val label = " GaitVision"
        val stringBuilder = StringBuilder()

        Thread {
            for(letter in label) {
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

    private val recordVideoLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Handle the recorded video
                val videoUri = result.data?.data
                videoUri?.let {
                    // You can use this URI to display or process the video
                    galleryUri = it  // Assuming you want to store it in your existing variable
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(this, it)
                    val frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST)
                    mBinding.imageView5.setImageBitmap(frame)  // Show first frame as preview
                    retriever.release()
                    Toast.makeText(this, "Video recorded successfully", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Video recording cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    private fun openCamera() {
        try {
            val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30) // 30 seconds max
            videoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 50 * 1024 * 1024) // 50MB max

            val activities = packageManager.queryIntentActivities(videoIntent, PackageManager.MATCH_DEFAULT_ONLY)

            if (activities.isNotEmpty()) {
                recordVideoLauncher.launch(videoIntent)
            } else {
                Toast.makeText(this, "No video recording app found. Available activities: ${activities.size}", Toast.LENGTH_LONG).show()
                if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    Toast.makeText(this, "Device has no camera hardware", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening camera: ${e.message}", Toast.LENGTH_LONG).show()
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

    private fun startIntentFromGallary() {
        getResult.launch("video/*")
    }
}