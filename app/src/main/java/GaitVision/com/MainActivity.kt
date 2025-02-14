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
import android.net.Uri
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private var videoUri: Uri?=null
    private val REQUESTCODE_CAMERA=1
    private val REQUESTCODE_GALLERY=2

    private val REQUEST_CODE_PERMISSIONS = 101

    private fun checkPermissions() {
        // Permissions you want to request
        val permissions = arrayOf(
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_AUDIO
        )

        // Check if all permissions are granted
        if (!hasPermissions(*permissions)) {
            // Request permissions
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS)
        } else {
            // Permissions already granted, proceed with your task
            //proceedWithMediaAccess()
        }
    }

    // Function to check if all permissions are granted
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
                // All permissions granted, proceed with your task
                proceedWithMediaAccess()
            } else {
                // Permissions denied, show a message to the user
                Toast.makeText(this, "Permissions are required to access media files.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val getResult: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
//                videoUri = it
                galleryUri = it
//                val intent= Intent(this, SecondActivity::class.java).apply {
//                    putExtra("VIDEO_URI", videoUri.toString())

                intent = Intent(this,SecondActivity::class.java)
                startActivity(intent)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()

        //Initialize all global variables
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

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        //mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.confirmVidBtn.setOnClickListener{startActivity(Intent(this,SecondActivity::class.java))}
        mBinding.openGalBtn.setOnClickListener{startIntentFromGallary()}

        //Creating typing animation
        val textView = findViewById<TextView>(R.id.textView1)
        val label   = " GaitVision"
        val stringBuilder = StringBuilder()

        Thread{
            for(letter in label){
                stringBuilder.append(letter)
                Thread.sleep(150    )
                runOnUiThread{
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

    private fun initClicks()
    {
        mBinding.openGalBtn.setOnClickListener{
            startIntentFromGallary()
        }
    }

    private fun startIntentFromGallary() {
       getResult.launch("video/*")
    }
}