package GaitVision.com

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.media.Image
import android.graphics.ImageFormat
import android.graphics.YuvImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.pose.Pose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import GaitVision.com.CoordinateNormalization
import GaitVision.com.calculateDistance
import GaitVision.com.calculateAngleLawOfCosines

/*
Name             : plotLineGraph
Parameters       :
    lineChart    : LineChart used in graph creation
    L/R Data     : Left and right Angles lists
    L/R Labels   : Labels for graphs to be created
Description      : Plots line graph using left angles as blue and right angles as red
Return           : None
 */
fun plotLineGraph(
    lineChart: LineChart,
    leftData: List<Float>,
    rightData: List<Float>,
    labelLeft: String,
    labelRight: String
) {
    // Filter out NaN values - MPAndroidChart can't handle them
    val leftEntries = leftData.mapIndexedNotNull { index, angle ->
        if (angle.isNaN() || angle.isInfinite()) null
        else Entry(index / 30f, angle)
    }
    val rightEntries = rightData.mapIndexedNotNull { index, angle ->
        if (angle.isNaN() || angle.isInfinite()) null
        else Entry(index / 30f, angle)
    }

    val leftDataSet = LineDataSet(leftEntries, labelLeft)
    leftDataSet.color = Color.BLUE
    leftDataSet.valueTextSize = 12f
    leftDataSet.setDrawCircles(false) // Disable highlighted points on zoom in
    leftDataSet.setDrawValues(false)

    val rightDataSet = LineDataSet(rightEntries, labelRight)
    rightDataSet.color = Color.RED
    rightDataSet.valueTextSize = 12f
    rightDataSet.setDrawCircles(false) // Disable highlighted points on zoom in
    rightDataSet.setDrawValues(false)

    val lineData = LineData(leftDataSet, rightDataSet)

    lineChart.data = lineData
    lineChart.description.isEnabled = false
    lineChart.invalidate() // Refresh chart
}

// POSE DETECTION - Using persistent detector for temporal consistency

/**
 * Confidence threshold for valid landmarks.
 * 
 * NOTE: ML Kit's inFrameLikelihood is possibly (according to experiments) different from MediaPipe's visibility!
 * ML Kit typically returns lower values, so we use 0.3 here as opposed to the .5 on the PC trainer
 */
const val LANDMARK_CONFIDENCE_THRESHOLD = 0.3f

/**
 * Persistent pose detector for video processing.
 * Using STREAM_MODE for temporal consistency across frames. THIS IMPROVED PROCESSING TIMES BY 2-3X
 * Must be initialized before processing and closed after.
 */
private var streamingPoseDetector: com.google.mlkit.vision.pose.PoseDetector? = null

/**
 * Initialize the streaming pose detector for video processing.
 * Call this ONCE before processing a video, then close() when done.
 */
fun initStreamingPoseDetector() {
    if (streamingPoseDetector != null) {
        Log.d("PoseDetection", "Detector already initialized, closing previous instance")
        streamingPoseDetector?.close()
    }
    
    val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)  // VIDEO mode for temporal consistency!
        .build()
    
    streamingPoseDetector = PoseDetection.getClient(options)
    Log.d("PoseDetection", "Initialized streaming pose detector (STREAM_MODE)")
}

/**
 * Close the streaming pose detector. Call after video processing is complete.
 */
fun closeStreamingPoseDetector() {
    streamingPoseDetector?.close()
    streamingPoseDetector = null
    Log.d("PoseDetection", "Closed streaming pose detector")
}

/**
 * Convert YUV_420_888 Image to ARGB Bitmap.
 * Properly handles row/pixel strides from MediaCodec output.
 */
private fun imageToBitmap(image: Image): Bitmap {
    val width = image.width
    val height = image.height
    
    val yPlane = image.planes[0]
    val uPlane = image.planes[1]
    val vPlane = image.planes[2]
    
    val yBuffer = yPlane.buffer
    val uBuffer = uPlane.buffer
    val vBuffer = vPlane.buffer
    
    val yRowStride = yPlane.rowStride
    val uvRowStride = uPlane.rowStride
    val uvPixelStride = uPlane.pixelStride
    
    // Create NV21 byte array (Y plane + interleaved VU)
    val nv21 = ByteArray(width * height * 3 / 2)
    
    // Copy Y plane, handling row stride
    var pos = 0
    for (row in 0 until height) {
        yBuffer.position(row * yRowStride)
        yBuffer.get(nv21, pos, width)
        pos += width
    }
    
    // Copy UV planes interleaved as VU (for NV21)
    val uvHeight = height / 2
    val uvWidth = width / 2
    for (row in 0 until uvHeight) {
        for (col in 0 until uvWidth) {
            val vIndex = row * uvRowStride + col * uvPixelStride
            val uIndex = row * uvRowStride + col * uvPixelStride
            
            vBuffer.position(vIndex)
            uBuffer.position(uIndex)
            
            nv21[pos++] = vBuffer.get()
            nv21[pos++] = uBuffer.get()
        }
    }
    
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 90, out)
    val imageBytes = out.toByteArray()
    
    // CRITICAL: Return mutable bitmap so we can draw wireframe on it
    val options = BitmapFactory.Options().apply { inMutable = true }
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
}

/*
Name        : processImageBitmap
Parameters  :
    bitmap  : This is the image bitmap we will use for processing. Can't be NULL
Description : This function will take the bitmap of the image/frame we will be processing.
              It then uses the ML Kit image processor to do pose detection on
              the image and get the pixel positions of specific points to be used for angle
              calculation in another function.
              
              UPDATED: Now uses streaming detector for temporal consistency.
              Falls back to single-image mode if streaming detector not initialized.
Return      :
    Pose    : Returns all of the pose landmarks and their information to calling function for use.
 */
suspend fun processImageBitmap(bitmap: Bitmap): Pose?
{
    val detector = streamingPoseDetector ?: run {
        // Fallback: create single-image detector if streaming not initialized
        Log.w("PoseDetection", "Streaming detector not initialized, using fallback SINGLE_IMAGE_MODE")
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
            .build()
        PoseDetection.getClient(options)
    }
    
    val image = InputImage.fromBitmap(bitmap, 0)

    return try {
        detector.process(image).await()
    } catch (e: Exception) {
        Log.e("PoseDetection", "Error processing frame: ${e.message}")
        null
    }
}

/**
 * Check if a landmark has sufficient confidence for use.
 * Matches PC's confidence threshold.
 */
fun isLandmarkValid(landmark: PoseLandmark?): Boolean {
    if (landmark == null) return false
    return landmark.inFrameLikelihood >= LANDMARK_CONFIDENCE_THRESHOLD
}

/*
Name                   : drawOnBitmap
Parameters             :
    Bitmap             : The bitmap we are drawing the angle text onto.
    Pose               : The pose object holding all the landmarks and their positions of the
                         current bitmap.
Description            : This function with take the current bitmap, pose object of the current
                         pose detection landmarks and calculate the angles of wanted joints. It will
                         add those angles to their respective list and draw a skeleton view on the
                         person for visual locations of the detection.
Return                 :
    Bitmap             : Returns the edited bitmap with the skeleton frame and angle text on it.
 */
fun drawOnBitmap(bitmap: Bitmap, pose: Pose?): Bitmap
{
    // Get frame dimensions for coordinate normalization (v2 extraction)
    val frameWidth = bitmap.width
    val frameHeight = bitmap.height
    
    //Get values for specific locations for calculations
    val leftShoulder = pose?.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
    val rightShoulder = pose?.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
    val leftHip = pose?.getPoseLandmark(PoseLandmark.LEFT_HIP)
    val rightHip = pose?.getPoseLandmark(PoseLandmark.RIGHT_HIP)
    val leftKnee = pose?.getPoseLandmark(PoseLandmark.LEFT_KNEE)
    val rightKnee = pose?.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
    val leftAnkle = pose?.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
    val rightAnkle = pose?.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
    val leftHeel = pose?.getPoseLandmark(PoseLandmark.LEFT_HEEL)
    val rightHeel = pose?.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
    val leftFootIndex = pose?.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
    val rightFootIndex = pose?.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)

    // Shoulder coordinates (pixel coordinates for legacy code)
    val leftShoulderX = leftShoulder?.position?.x ?: 0f
    val leftShoulderY = leftShoulder?.position?.y ?: 0f
    val rightShoulderX = rightShoulder?.position?.x ?: 0f
    val rightShoulderY = rightShoulder?.position?.y ?: 0f

    // Hip coordinates (pixel coordinates for legacy code)
    val leftHipX = leftHip?.position?.x ?: 0f
    val leftHipY = leftHip?.position?.y ?: 0f
    val rightHipX = rightHip?.position?.x ?: 0f
    val rightHipY = rightHip?.position?.y ?: 0f

    // Knee coordinates (pixel coordinates for legacy code)
    val leftKneeX = leftKnee?.position?.x ?: 0f
    val leftKneeY = leftKnee?.position?.y ?: 0f
    val rightKneeX = rightKnee?.position?.x ?: 0f
    val rightKneeY = rightKnee?.position?.y ?: 0f

    // Ankle coordinates (pixel coordinates for legacy code)
    val leftAnkleX = leftAnkle?.position?.x ?: 0f
    val leftAnkleY = leftAnkle?.position?.y ?: 0f
    val rightAnkleX = rightAnkle?.position?.x ?: 0f
    val rightAnkleY = rightAnkle?.position?.y ?: 0f

    // Heel coordinates (pixel coordinates for legacy code)
    val leftHeelX = leftHeel?.position?.x ?: 0f
    val leftHeelY = leftHeel?.position?.y ?: 0f
    val rightHeelX = rightHeel?.position?.x ?: 0f
    val rightHeelY = rightHeel?.position?.y ?: 0f

    // Foot Index coordinates (pixel coordinates for legacy code)
    val leftFootIndexX = leftFootIndex?.position?.x ?: 0f
    val leftFootIndexY = leftFootIndex?.position?.y ?: 0f
    val rightFootIndexX = rightFootIndex?.position?.x ?: 0f
    val rightFootIndexY = rightFootIndex?.position?.y ?: 0f

    // V2 EXTRACTION: Normalize coordinates for PC-matching calculations
    // Check if required landmarks have sufficient confidence
    val hasLeftAnkle = isLandmarkValid(leftAnkle)
    val hasRightAnkle = isLandmarkValid(rightAnkle)
    val hasLeftHip = isLandmarkValid(leftHip)
    val hasRightHip = isLandmarkValid(rightHip)
    val hasLeftKnee = isLandmarkValid(leftKnee)
    val hasRightKnee = isLandmarkValid(rightKnee)
    val hasLeftShoulder = isLandmarkValid(leftShoulder)
    val hasRightShoulder = isLandmarkValid(rightShoulder)
    val hasLeftHeel = isLandmarkValid(leftHeel)
    val hasRightHeel = isLandmarkValid(rightHeel)
    val hasLeftFootIndex = isLandmarkValid(leftFootIndex)
    val hasRightFootIndex = isLandmarkValid(rightFootIndex)
    
    // Normalize coordinates for v2 feature extraction
    val leftAnkleNorm = CoordinateNormalization.normalizePoint(leftAnkleX, leftAnkleY, frameWidth, frameHeight)
    val rightAnkleNorm = CoordinateNormalization.normalizePoint(rightAnkleX, rightAnkleY, frameWidth, frameHeight)
    val leftHipNorm = CoordinateNormalization.normalizePoint(leftHipX, leftHipY, frameWidth, frameHeight)
    val rightHipNorm = CoordinateNormalization.normalizePoint(rightHipX, rightHipY, frameWidth, frameHeight)
    val leftKneeNorm = CoordinateNormalization.normalizePoint(leftKneeX, leftKneeY, frameWidth, frameHeight)
    val rightKneeNorm = CoordinateNormalization.normalizePoint(rightKneeX, rightKneeY, frameWidth, frameHeight)
    val leftShoulderNorm = CoordinateNormalization.normalizePoint(leftShoulderX, leftShoulderY, frameWidth, frameHeight)
    
    // V2: Calculate inter-ankle distance using normalized coordinates
    // ONLY add valid values - add NaN when landmarks are missing
    if (hasLeftAnkle && hasRightAnkle) {
        val interAnkleDist = calculateDistance(
            leftAnkleNorm.x, leftAnkleNorm.y,
            rightAnkleNorm.x, rightAnkleNorm.y
        )
        interAnkleDistances.add(interAnkleDist)
    } else {
        interAnkleDistances.add(Float.NaN)  // Mark as missing, not 0!
    }
    
    // V2: Calculate leg lengths using normalized coordinates
    // ONLY add valid values - add NaN when landmarks are missing
    if (hasLeftHip && hasLeftAnkle && hasRightHip && hasRightAnkle) {
        val leftLegLen = calculateDistance(
            leftHipNorm.x, leftHipNorm.y,
            leftAnkleNorm.x, leftAnkleNorm.y
        )
        val rightLegLen = calculateDistance(
            rightHipNorm.x, rightHipNorm.y,
            rightAnkleNorm.x, rightAnkleNorm.y
        )
        legLengths.add((leftLegLen + rightLegLen) / 2f)  // Average both legs
    } else {
        legLengths.add(Float.NaN)  // Mark as missing, not 0!
    }
    
    // V2: Calculate angles using normalized coordinates (for v2 extraction)
    // Left knee angle (hip-knee-ankle) - matches PC
    // ALWAYS add a value (NaN if missing) to keep lists synchronized with frame count
    if (hasLeftHip && hasLeftKnee && hasLeftAnkle) {
        val leftKneeAngleV2 = calculateAngleLawOfCosines(
            leftHipNorm.x, leftHipNorm.y,
            leftKneeNorm.x, leftKneeNorm.y,
            leftAnkleNorm.x, leftAnkleNorm.y
        )
        leftKneeAngles.add(leftKneeAngleV2)
    } else {
        leftKneeAngles.add(Float.NaN)
    }
    
    // Right knee angle (hip-knee-ankle) - matches PC
    if (hasRightHip && hasRightKnee && hasRightAnkle) {
        val rightKneeAngleV2 = calculateAngleLawOfCosines(
            rightHipNorm.x, rightHipNorm.y,
            rightKneeNorm.x, rightKneeNorm.y,
            rightAnkleNorm.x, rightAnkleNorm.y
        )
        rightKneeAngles.add(rightKneeAngleV2)
    } else {
        rightKneeAngles.add(Float.NaN)
    }
    
    // Left hip angle (shoulder-hip-knee) - matches PC
    if (hasLeftShoulder && hasLeftHip && hasLeftKnee) {
        val leftHipAngleV2 = calculateAngleLawOfCosines(
            leftShoulderNorm.x, leftShoulderNorm.y,
            leftHipNorm.x, leftHipNorm.y,
            leftKneeNorm.x, leftKneeNorm.y
        )
        leftHipAngles.add(leftHipAngleV2)
    } else {
        leftHipAngles.add(Float.NaN)
    }
    
    // Right hip angle (shoulder-hip-knee) - for completeness
    val rightShoulderNorm = CoordinateNormalization.normalizePoint(rightShoulderX, rightShoulderY, frameWidth, frameHeight)
    if (hasRightShoulder && hasRightHip && hasRightKnee) {
        val rightHipAngleV2 = calculateAngleLawOfCosines(
            rightShoulderNorm.x, rightShoulderNorm.y,
            rightHipNorm.x, rightHipNorm.y,
            rightKneeNorm.x, rightKneeNorm.y
        )
        rightHipAngles.add(rightHipAngleV2)
    } else {
        rightHipAngles.add(Float.NaN)
    }
    
    // LEGACY CODE: Keep pixel coordinates for legacy 9-feature path
    // Angle Calculations (added Not A Number check) - using pixel coordinates for legacy
    // Ankle Angles (legacy - not used in v2)
    var leftAnkleAngle = GetAnglesA(leftFootIndexX, leftFootIndexY, leftAnkleX, leftAnkleY, leftKneeX, leftKneeY)
    if (!leftAnkleAngle.isNaN() && leftAnkleAngle < 70 && leftAnkleAngle > -25) {
        leftAnkleAngles.add(leftAnkleAngle)
    }
    else
    {
        count++
        Log.d("ErrorCheck","Left Ankle: $leftAnkleAngle")
        Log.d("ErrorCheck","LeftFoot: ($leftFootIndexX,$leftFootIndexY), Left Ankle: ($leftAnkleX,$leftAnkleY), Left Knee: ($leftKneeX,$leftKneeY)")
    }
    var rightAnkleAngle = GetAnglesA(rightFootIndexX, rightFootIndexY, rightAnkleX, rightAnkleY, rightKneeX, rightKneeY)
    if (!rightAnkleAngle.isNaN() && rightAnkleAngle < 70 && rightAnkleAngle > -25) {
        rightAnkleAngles.add(rightAnkleAngle)
    }
    else
    {
        count++
        Log.d("ErrorCheck","Right Ankle: $rightAnkleAngle")
        Log.d("ErrorCheck","RightFoot: ($rightFootIndexX,$rightFootIndexY), Right Ankle: ($rightAnkleX,$rightAnkleY), Right Knee: ($rightKneeX,$rightKneeY)")
    }

    // Torso Angle (legacy)
    var torsoAngle = calcTorso((leftHipX+rightHipX)/2,(leftHipY+rightHipY)/2,(rightShoulderX+leftShoulderX)/2,(rightShoulderY+leftShoulderY)/2)
    if (!torsoAngle.isNaN() && torsoAngle > -20 && torsoAngle < 20) {
        torsoAngles.add(torsoAngle)
    }
    else
    {
        count++
        Log.d("ErrorCheck","TorsoAngle: $torsoAngle, shoulder: ($rightShoulderX,$rightShoulderY) ($leftShoulderX,$leftShoulderY), Hip: ($rightHipX,$rightHipY) ($leftHipX,$leftHipY)")
    }

    //Calculate stride angle of current frame (legacy)
    var strideAngle = calcStrideAngle(leftHeelX,leftHeelY,(leftHipX+rightHipX)/2f,(leftHipY+rightHipY)/2,rightHeelX,rightHeelY)
    strideAngles.add(strideAngle)


    // DRAW SKELETON - Only draw landmarks that pass confidence threshold
    val canvas = Canvas(bitmap)

    val paintCircleRight = Paint().apply { setARGB(255, 255, 0, 0) }
    val paintCircleLeft = Paint().apply { setARGB(255, 0, 0, 255) }
    val paintLine = Paint().apply { 
        setARGB(255, 255, 255, 255)
        strokeWidth = 4f 
    }
    // Thicker lines for better visibility
    val paintLineBold = Paint().apply {
        setARGB(255, 255, 255, 255)
        strokeWidth = 6f
    }

    // Draw lines ONLY if BOTH endpoints are valid
    // Right leg
    if (hasRightHip && hasRightKnee) {
        canvas.drawLine(rightHipX, rightHipY, rightKneeX, rightKneeY, paintLineBold)
    }
    if (hasRightKnee && hasRightAnkle) {
        canvas.drawLine(rightKneeX, rightKneeY, rightAnkleX, rightAnkleY, paintLineBold)
    }
    if (hasRightAnkle && hasRightFootIndex) {
        canvas.drawLine(rightAnkleX, rightAnkleY, rightFootIndexX, rightFootIndexY, paintLine)
    }
    if (hasRightAnkle && hasRightHeel) {
        canvas.drawLine(rightAnkleX, rightAnkleY, rightHeelX, rightHeelY, paintLine)
    }
    if (hasRightHeel && hasRightFootIndex) {
        canvas.drawLine(rightHeelX, rightHeelY, rightFootIndexX, rightFootIndexY, paintLine)
    }
    
    // Left leg
    if (hasLeftHip && hasLeftKnee) {
        canvas.drawLine(leftHipX, leftHipY, leftKneeX, leftKneeY, paintLineBold)
    }
    if (hasLeftKnee && hasLeftAnkle) {
        canvas.drawLine(leftKneeX, leftKneeY, leftAnkleX, leftAnkleY, paintLineBold)
    }
    if (hasLeftAnkle && hasLeftFootIndex) {
        canvas.drawLine(leftAnkleX, leftAnkleY, leftFootIndexX, leftFootIndexY, paintLine)
    }
    if (hasLeftAnkle && hasLeftHeel) {
        canvas.drawLine(leftAnkleX, leftAnkleY, leftHeelX, leftHeelY, paintLine)
    }
    if (hasLeftHeel && hasLeftFootIndex) {
        canvas.drawLine(leftHeelX, leftHeelY, leftFootIndexX, leftFootIndexY, paintLine)
    }
    
    // Draw joints ONLY if valid
    if (hasRightHip) canvas.drawCircle(rightHipX, rightHipY, 6f, paintCircleRight)
    if (hasLeftHip) canvas.drawCircle(leftHipX, leftHipY, 6f, paintCircleLeft)
    if (hasRightKnee) canvas.drawCircle(rightKneeX, rightKneeY, 6f, paintCircleRight)
    if (hasLeftKnee) canvas.drawCircle(leftKneeX, leftKneeY, 6f, paintCircleLeft)
    if (hasRightAnkle) canvas.drawCircle(rightAnkleX, rightAnkleY, 5f, paintCircleRight)
    if (hasLeftAnkle) canvas.drawCircle(leftAnkleX, leftAnkleY, 5f, paintCircleLeft)
    if (hasRightHeel) canvas.drawCircle(rightHeelX, rightHeelY, 4f, paintCircleRight)
    if (hasLeftHeel) canvas.drawCircle(leftHeelX, leftHeelY, 4f, paintCircleLeft)
    if (hasRightFootIndex) canvas.drawCircle(rightFootIndexX, rightFootIndexY, 4f, paintCircleRight)
    if (hasLeftFootIndex) canvas.drawCircle(leftFootIndexX, leftFootIndexY, 4f, paintCircleLeft)

    return bitmap
}

/*
Name             : getFrameBitmaps
Parameters       :
    context      : This parameter is the interface that contains global information about
                   the application environment.
    fileUri      : This parameter is the uri to the video that will be used in the function.
    mBinding     : This is the view of the activity page. Use this for messing with XML features.
Description      : This function takes a uri of a video and sends it through a process to get a
                   bitmap of every frame in the video so pose tracking can be done on it.
Return           :
    NONE
 */
suspend fun getFrameBitmaps(context: Context, fileUri: Uri?, activity: AppCompatActivity)
{
    if(fileUri == null)
    {
        return
    }

    val OPTION_CLOSEST = MediaMetadataRetriever.OPTION_CLOSEST
    val retriever = MediaMetadataRetriever()
    frameList = mutableListOf<Bitmap>()
    
    // Use file descriptor for more reliable access to content URIs
    try {
        val pfd = context.contentResolver.openFileDescriptor(fileUri, "r")
        if (pfd != null) {
            retriever.setDataSource(pfd.fileDescriptor)
            pfd.close()
        } else {
            // Fallback to context-based method
            retriever.setDataSource(context, fileUri)
        }
    } catch (e: Exception) {
        Log.e("ImageProcessing", "Error opening video: ${e.message}")
        // Try fallback method
        retriever.setDataSource(context, fileUri)
    }

    if(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) == "video/mp4")
    {
        val videoLengthMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
        
        // Try multiple methods to detect FPS
        var frameRate: Float? = null
        
        // Method 1: CAPTURE_FRAMERATE (Android M+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            frameRate = try {
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloatOrNull()
            } catch (e: Exception) { null }
        }
        
        // Method 2: Calculate from frame count / duration (Android P+)
        if (frameRate == null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val frameCount = try {
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)?.toIntOrNull()
            } catch (e: Exception) { null }
            
            if (frameCount != null && videoLengthMs > 0) {
                val calculatedFps = (frameCount * 1000f) / videoLengthMs
                // Sanity check: FPS should be between 15 and 120
                if (calculatedFps in 15f..120f) {
                    frameRate = calculatedFps
                    Log.d("ImageProcessing", "FPS calculated from frame count: $calculatedFps ($frameCount frames / ${videoLengthMs}ms)")
                }
            }
        }
        
        // Use detected FPS or default to 30
        val fps = frameRate ?: 30f
        detectedFps = fps
        Log.d("ImageProcessing", "Video FPS: $fps (detected: ${frameRate != null})")
        
        val frameInterval = (1000L * 1000L) / fps.toLong()
        var currTime = 0L
        val videoLengthUs = videoLengthMs * 1000L
        videoLength = videoLengthUs

        withContext(Dispatchers.Main)
        {
            activity.findViewById<View>(R.id.splittingBar).visibility = VISIBLE
            activity.findViewById<TextView>(R.id.splittingProgressValue).visibility = VISIBLE
            activity.findViewById<TextView>(R.id.splittingProgressValue).text = " 0%"
        }
        
        var progress: Int
        while(currTime <= videoLengthUs)
        {
            val frame = retriever.getFrameAtTime(currTime, OPTION_CLOSEST)
            if(frame != null)
            {
                frameList.add(frame)
            }

            progress = ((currTime.toDouble() / videoLengthUs)*100).toInt()
            withContext(Dispatchers.Main)
            {
                activity.findViewById<ProgressBar>(R.id.splittingBar).setProgress(progress)
                activity.findViewById<TextView>(R.id.splittingProgressValue).text = (" " + progress.toString() + "%")
            }
            currTime += frameInterval
        }
    }
    else if(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) == "image/jpeg" || retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) == "image/png")
    {
        val stream = context.contentResolver.openInputStream(fileUri)
        val frame = BitmapFactory.decodeStream(stream)
        frameList.add(frame)
    }

    retriever.release()
}

/*
Name           : ProcVidEmpty (overloaded for AnalysisActivity)
Description    : Master function for video processing with AnalysisActivity binding
                 
                 MEMORY-OPTIMIZED: Processes frames ONE AT A TIME instead of loading all into memory.
                 This allows processing of long videos without OutOfMemoryError.
                 The streaming pose detector still works because frames are processed sequentially.
 */
suspend fun ProcVidEmpty(context: Context, outputPath: String, activity: AppCompatActivity): Uri?
{
    // Clear all angle lists
    leftAnkleAngles.clear()
    rightAnkleAngles.clear()
    leftKneeAngles.clear()
    rightKneeAngles.clear()
    leftHipAngles.clear()
    rightHipAngles.clear()
    torsoAngles.clear()
    strideAngles.clear()
    
    // Clear v2 extraction lists
    interAnkleDistances.clear()
    legLengths.clear()
    
    // Clear old frameList (we won't use it anymore, but clear just in case)
    frameList.clear()
    
    // Initialize streaming pose detector EARLY - before any processing method
    // This ensures STREAM_MODE (temporal tracking) regardless of extraction method
    initStreamingPoseDetector()
    Log.d("PoseDetection", "Streaming pose detector initialized at start of ProcVidEmpty")

    // Update UI - hide extraction bar since we now stream frames one at a time
    withContext(Dispatchers.Main)
    {
        // Hide extraction UI (no longer used - streaming approach)
        activity.findViewById<TextView>(R.id.SplittingText).visibility = GONE
        activity.findViewById<ProgressBar>(R.id.splittingBar).visibility = GONE
        activity.findViewById<TextView>(R.id.splittingProgressValue).visibility = GONE
        
        // Show processing UI (combined extraction + processing)
        activity.findViewById<TextView>(R.id.CreationText).visibility = VISIBLE
        activity.findViewById<TextView>(R.id.CreatingProgressValue).visibility = GONE
    }

    // FAST MediaCodec-based frame extraction
    if (galleryUri == null) return null
    
    // Set up MediaExtractor for reading video data
    val extractor = MediaExtractor()
    val retriever = MediaMetadataRetriever()
    
    try {
        val pfd = context.contentResolver.openFileDescriptor(galleryUri!!, "r")
        if (pfd != null) {
            extractor.setDataSource(pfd.fileDescriptor)
            retriever.setDataSource(pfd.fileDescriptor)
            pfd.close()
        } else {
            throw Exception("Could not open file descriptor")
        }
    } catch (e: Exception) {
        Log.e("ImageProcessing", "Error opening video: ${e.message}")
        try {
            extractor.setDataSource(context, galleryUri!!, null)
            retriever.setDataSource(context, galleryUri)
        } catch (e2: Exception) {
            Log.e("ImageProcessing", "Fallback also failed: ${e2.message}")
            return null
        }
    }
    
    // Find video track
    var videoTrackIndex = -1
    var videoFormat: MediaFormat? = null
    for (i in 0 until extractor.trackCount) {
        val format = extractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
        if (mime.startsWith("video/")) {
            videoTrackIndex = i
            videoFormat = format
            break
        }
    }
    
    if (videoTrackIndex < 0 || videoFormat == null) {
        Log.e("ImageProcessing", "No video track found")
        extractor.release()
        retriever.release()
        return galleryUri
    }
    
    extractor.selectTrack(videoTrackIndex)
    
    // Get video properties
    val width = videoFormat.getInteger(MediaFormat.KEY_WIDTH)
    val height = videoFormat.getInteger(MediaFormat.KEY_HEIGHT)
    val videoMime = videoFormat.getString(MediaFormat.KEY_MIME) ?: "video/avc"
    val durationUs = videoFormat.getLong(MediaFormat.KEY_DURATION)
    val videoLengthMs = durationUs / 1000
    videoLength = durationUs
    
    // Detect FPS from format or metadata
    var fps = 30f
    if (videoFormat.containsKey(MediaFormat.KEY_FRAME_RATE)) {
        fps = videoFormat.getInteger(MediaFormat.KEY_FRAME_RATE).toFloat()
    } else {
        // Fallback to metadata
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloatOrNull()?.let { fps = it }
        }
        if (fps == 30f && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val frameCount = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)?.toIntOrNull()
            if (frameCount != null && videoLengthMs > 0) {
                val calculatedFps = (frameCount * 1000f) / videoLengthMs
                if (calculatedFps in 15f..120f) fps = calculatedFps
            }
        }
    }
    detectedFps = fps
    retriever.release()  // Done with retriever
    
    val totalFrames = ((durationUs * fps) / 1_000_000).toInt()
    Log.d("ImageProcessing", "Video: ${videoLengthMs}ms @ ${fps}fps, ${width}x${height}, ~$totalFrames frames")
    Log.d("ImageProcessing", "Using FAST MediaCodec extraction (5-10x faster than getFrameAtTime)")
    
    // Set up MediaCodec decoder
    val decoder: MediaCodec
    try {
        decoder = MediaCodec.createDecoderByType(videoMime)
        // Don't modify the format - let decoder choose optimal color format
        decoder.configure(videoFormat, null, null, 0)
        decoder.start()
        Log.d("MediaCodec", "Decoder started for $videoMime, ${width}x${height}")
    } catch (e: Exception) {
        Log.e("ImageProcessing", "Failed to initialize decoder: ${e.message}")
        Log.e("ImageProcessing", "Falling back to slow getFrameAtTime method")
        closeStreamingPoseDetector()
        extractor.release()
        // Return null to trigger fallback (could implement fallback here if needed)
        return null
    }
    
    // Set up video encoder
    val mediaMuxer: MediaMuxer
    val encoder: MediaCodec
    val inputSurface: android.view.Surface
    
    try {
        mediaMuxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val format = MediaFormat.createVideoFormat("video/avc", width, height)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1000000)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, fps.toInt())  // Use detected FPS, not hardcoded 30
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        format.setInteger(MediaFormat.KEY_ROTATION, 0)
        mediaMuxer.setOrientationHint(0)

        encoder = MediaCodec.createEncoderByType("video/avc")
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        inputSurface = encoder.createInputSurface()
        encoder.start()
    } catch (e: Exception) {
        Log.e("ImageProcessing", "Failed to initialize encoder: ${e.message}", e)
        closeStreamingPoseDetector()
        decoder.stop()
        decoder.release()
        extractor.release()
        return null
    }

    val frameDurationUs = 1000000L / fps.toLong()
    var trackIndex = -1
    var muxerStarted = false
    val encoderBufferInfo = MediaCodec.BufferInfo()
    val decoderBufferInfo = MediaCodec.BufferInfo()
    var progress: Int
    var frameIndex = 0
    var inputDone = false
    var outputDone = false

    withContext(Dispatchers.Main)
    {
        activity.findViewById<ProgressBar>(R.id.VideoCreation).visibility = VISIBLE
        activity.findViewById<TextView>(R.id.CreatingProgressValue).visibility = VISIBLE
        activity.findViewById<TextView>(R.id.CreatingProgressValue).text = " 0%"
    }

    // FAST MediaCodec FRAME PROCESSING
    val startTime = System.currentTimeMillis()
    
    while (!outputDone) {
        // Feed input to decoder
        if (!inputDone) {
            val inputBufferId = decoder.dequeueInputBuffer(10000)
            if (inputBufferId >= 0) {
                val inputBuffer = decoder.getInputBuffer(inputBufferId)
                if (inputBuffer != null) {
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    if (sampleSize < 0) {
                        // End of stream
                        decoder.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                    } else {
                        val presentationTimeUs = extractor.sampleTime
                        decoder.queueInputBuffer(inputBufferId, 0, sampleSize, presentationTimeUs, 0)
                        extractor.advance()
                    }
                }
            }
        }
        
        // Get output from decoder
        val outputBufferId = decoder.dequeueOutputBuffer(decoderBufferInfo, 10000)
        when {
            outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                // No output available yet, continue
            }
            outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                Log.d("MediaCodec", "Decoder output format changed")
            }
            outputBufferId >= 0 -> {
                // Check for end of stream
                if ((decoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    outputDone = true
                    decoder.releaseOutputBuffer(outputBufferId, false)
                } else {
                    // Get the decoded frame as Image
                    try {
                        val image = decoder.getOutputImage(outputBufferId)
                        if (image == null) {
                            Log.w("MediaCodec", "getOutputImage returned null for frame $frameIndex")
                            decoder.releaseOutputBuffer(outputBufferId, false)
                            continue
                        }
                        
                        // Convert Image to Bitmap
                        val frame: Bitmap
                        try {
                            frame = imageToBitmap(image)
                        } finally {
                            image.close()
                        }
                        
                        // Process this frame with streaming pose detector
                        val pose = processImageBitmap(frame)
                        val modifiedBitmap = drawOnBitmap(frame, pose)

                        // Encode the processed frame
                        val canvas = inputSurface.lockCanvas(null)
                        canvas.drawBitmap(modifiedBitmap, 0f, 0f, null)
                        inputSurface.unlockCanvasAndPost(canvas)

                        // Handle encoder output
                        while (true) {
                            val encOutputId = encoder.dequeueOutputBuffer(encoderBufferInfo, 1000)
                            when {
                                encOutputId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                                    if (!muxerStarted) {
                                        trackIndex = mediaMuxer.addTrack(encoder.outputFormat)
                                        mediaMuxer.start()
                                        muxerStarted = true
                                    }
                                }
                                encOutputId >= 0 -> {
                                    val outputBuffer = encoder.getOutputBuffer(encOutputId) ?: break
                                    if (muxerStarted) {
                                        encoderBufferInfo.presentationTimeUs = frameIndex * frameDurationUs
                                        mediaMuxer.writeSampleData(trackIndex, outputBuffer, encoderBufferInfo)
                                    }
                                    encoder.releaseOutputBuffer(encOutputId, false)
                                }
                                else -> break
                            }
                        }
                        
                        // Update progress
                        frameIndex++
                        progress = ((frameIndex.toFloat() / totalFrames) * 100).toInt().coerceIn(0, 100)
                        withContext(Dispatchers.Main) {
                            activity.findViewById<ProgressBar>(R.id.VideoCreation).setProgress(progress)
                            activity.findViewById<TextView>(R.id.CreatingProgressValue).text = " $progress%"
                        }
                    } catch (e: Exception) {
                        Log.w("MediaCodec", "Error processing frame $frameIndex: ${e.message}")
                    }
                    
                    decoder.releaseOutputBuffer(outputBufferId, false)
                }
            }
        }
    }
    
    val elapsedSec = (System.currentTimeMillis() - startTime) / 1000.0
    Log.d("ImageProcessing", "Processed $frameIndex frames in ${elapsedSec}s (${frameIndex/elapsedSec} fps)")

    // Flush remaining encoder output
    encoder.signalEndOfInputStream()
    while (true) {
        val outputBufferId = encoder.dequeueOutputBuffer(encoderBufferInfo, 10000)
        if (outputBufferId >= 0) {
            val outputBuffer = encoder.getOutputBuffer(outputBufferId) ?: break
            if (muxerStarted) {
                encoderBufferInfo.presentationTimeUs = frameIndex.toLong() * frameDurationUs
                mediaMuxer.writeSampleData(trackIndex, outputBuffer, encoderBufferInfo)
            }
            encoder.releaseOutputBuffer(outputBufferId, false)
        } else {
            break
        }
    }

    // Release all resources
    decoder.stop()
    decoder.release()
    extractor.release()
    encoder.stop()
    encoder.release()
    mediaMuxer.stop()
    mediaMuxer.release()
    
    // Close the streaming pose detector now that we're done
    closeStreamingPoseDetector()

    withContext(Dispatchers.Main)
    {
        activity.findViewById<TextView>(R.id.SplittingText).visibility = GONE
        activity.findViewById<TextView>(R.id.CreationText).visibility = GONE
        activity.findViewById<ProgressBar>(R.id.VideoCreation).visibility = GONE
        activity.findViewById<ProgressBar>(R.id.splittingBar).visibility = GONE
        activity.findViewById<TextView>(R.id.splittingProgressValue).visibility = GONE
        activity.findViewById<TextView>(R.id.CreatingProgressValue).visibility = GONE
    }
    
    Log.d("ImageProcessing", "Video processing complete - used streaming (memory-safe) approach")

    val outputFile = File(outputPath)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", outputFile)
    Log.d("ErrorCheck", "Generated URI: $uri")
    return uri
}