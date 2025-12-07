package GaitVision.com.ui

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import GaitVision.com.R
import GaitVision.com.galleryUri
import GaitVision.com.editedUri

class VideoPickerActivity : AppCompatActivity() {

    private var videoUri: Uri? = null
    private lateinit var videoView: VideoView
    private lateinit var tvStatus: TextView
    private lateinit var btnContinue: Button
    private var fromPatientProfile = false

    private lateinit var recordVideoLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickVideoLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_picker)

        initializeViews()
        setupLaunchers()
        setupButtons()

        // Check if we should auto-launch based on intent
        val mode = intent.getStringExtra("mode")
        fromPatientProfile = intent.getBooleanExtra("fromPatientProfile", false)

        when (mode) {
            "record" -> openCameraForVideo()
            "gallery" -> pickVideoFromGallery()
        }

        // If galleryUri already exists, show it
        galleryUri?.let {
            videoUri = it
            displayVideo(it)
        }
    }

    private fun initializeViews() {
        videoView = findViewById(R.id.videoView)
        tvStatus = findViewById(R.id.tvStatus)
        btnContinue = findViewById(R.id.btnContinue)

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        // Initially hide continue button until video is selected
        updateContinueButtonVisibility()
    }

    private fun setupLaunchers() {
        // Launcher for recording video
        recordVideoLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                videoUri?.let { uri ->
                    galleryUri = uri
                    editedUri = null  // Clear old processed video - force re-processing
                    displayVideo(uri)
                    Toast.makeText(this, "Video recorded successfully", Toast.LENGTH_SHORT).show()
                    Log.d("VideoPickerActivity", "Recorded video URI: $uri")
                }
            } else {
                Toast.makeText(this, "Video recording canceled", Toast.LENGTH_SHORT).show()
                // If no video was previously selected, allow retry
                if (galleryUri == null) {
                    tvStatus.text = "No video selected. Try again."
                }
            }
        }

        // Launcher for picking video from gallery - use OpenDocument for persistent access
        pickVideoLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            uri?.let {
                // Take persistable URI permission so we can access it later
                try {
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    Log.w("VideoPickerActivity", "Could not take persistable permission: ${e.message}")
                }
                
                videoUri = it
                galleryUri = it
                editedUri = null  // Clear old processed video - force re-processing
                displayVideo(it)
                Toast.makeText(this, "Video selected", Toast.LENGTH_SHORT).show()
                Log.d("VideoPickerActivity", "Selected video URI: $it")
            } ?: run {
                if (galleryUri == null) {
                    tvStatus.text = "No video selected. Try again."
                }
            }
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnPick).setOnClickListener {
            pickVideoFromGallery()
        }

        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            openCameraForVideo()
        }

        btnContinue.setOnClickListener {
            if (galleryUri != null) {
                if (fromPatientProfile) {
                    // Came from patient profile - go to analysis
                    startActivity(Intent(this, AnalysisActivity::class.java))
                } else {
                    // Came from dashboard - return to dashboard
                    setResult(Activity.RESULT_OK)
                }
                finish()
            } else {
                Toast.makeText(this, "Please select a video first", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun pickVideoFromGallery() {
        pickVideoLauncher.launch(arrayOf("video/*"))
    }

    private fun openCameraForVideo() {
        videoUri = createVideoUri()
        Log.d("VideoPickerActivity", "Created video URI for recording: $videoUri")

        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
            putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1) // High quality
        }
        recordVideoLauncher.launch(intent)
    }

    private fun createVideoUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "gaitvision_${System.currentTimeMillis()}.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/GaitVision")
        }
        return contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun displayVideo(uri: Uri) {
        try {
            videoView.visibility = View.VISIBLE
            videoView.setVideoURI(uri)

            videoView.setOnPreparedListener { mp ->
                // Get video duration
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(this, uri)
                    val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0
                    val durationSeconds = duration / 1000.0
                    tvStatus.text = String.format("Video ready (%.1f seconds)", durationSeconds)
                    retriever.release()
                } catch (e: Exception) {
                    tvStatus.text = "Video ready"
                }
            }

            videoView.setOnErrorListener { _, _, _ ->
                tvStatus.text = "Error loading video"
                true
            }

            // Auto-play preview
            videoView.start()

            updateContinueButtonVisibility()

        } catch (e: Exception) {
            Log.e("VideoPickerActivity", "Error displaying video: ${e.message}", e)
            tvStatus.text = "Error loading video"
        }
    }

    private fun updateContinueButtonVisibility() {
        btnContinue.visibility = if (galleryUri != null) View.VISIBLE else View.GONE
    }

    override fun onPause() {
        super.onPause()
        if (videoView.isPlaying) {
            videoView.pause()
        }
    }
}
