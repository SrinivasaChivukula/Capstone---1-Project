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
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
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
    val leftEntries = leftData.mapIndexed { index, angle ->
        val convertToSecond = index / 30f
        Entry(convertToSecond, angle)
    }
    val rightEntries = rightData.mapIndexed { index, angle ->
        val convertToSecond = index / 30f
        Entry(convertToSecond, angle)
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

/*
Name        : processImageBitmap
Parameters  :
    bitmap  : This is the image bitmap we will use for processing. Can't be NULL
Description : This function will take the bitmap of the image/frame we will be processing.
              It then uses the ML Kit image processor to do pose detection on
              the image and get the pixel positions of specific points to be used for angle
              calculation in another function.
Return      :
    Pose    : Returns all of the pose landmarks and their information to calling function for use.
 */
suspend fun processImageBitmap(bitmap: Bitmap): Pose?
{
    //Setup pose detector options using accuracy mode on a still image
    val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
        .build()

    //Create instance of pose detector
    val poseDetector = PoseDetection.getClient(options)
    val image = InputImage.fromBitmap(bitmap, 0)

    //Try-Catch statement because InputImage throws exception if there was an error creating the InputImage
    return try
    {
        poseDetector.process(image).await()
    }
    catch (e: Exception)
    {
        Log.e("InputImage", "Error creating InputImage from bitmap: ${e.message}")
        null
    }
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

    // Shoulder coordinates
    val leftShoulderX = leftShoulder?.position?.x ?: 0f
    val leftShoulderY = leftShoulder?.position?.y ?: 0f
    val rightShoulderX = rightShoulder?.position?.x ?: 0f
    val rightShoulderY = rightShoulder?.position?.y ?: 0f

    // Hip coordinates
    val leftHipX = leftHip?.position?.x ?: 0f
    val leftHipY = leftHip?.position?.y ?: 0f
    val rightHipX = rightHip?.position?.x ?: 0f
    val rightHipY = rightHip?.position?.y ?: 0f

    // Knee coordinates
    val leftKneeX = leftKnee?.position?.x ?: 0f
    val leftKneeY = leftKnee?.position?.y ?: 0f
    val rightKneeX = rightKnee?.position?.x ?: 0f
    val rightKneeY = rightKnee?.position?.y ?: 0f

    // Ankle coordinates
    val leftAnkleX = leftAnkle?.position?.x ?: 0f
    val leftAnkleY = leftAnkle?.position?.y ?: 0f
    val rightAnkleX = rightAnkle?.position?.x ?: 0f
    val rightAnkleY = rightAnkle?.position?.y ?: 0f

    // Heel coordinates
    val leftHeelX = leftHeel?.position?.x ?: 0f
    val leftHeelY = leftHeel?.position?.y ?: 0f
    val rightHeelX = rightHeel?.position?.x ?: 0f
    val rightHeelY = rightHeel?.position?.y ?: 0f

    // Foot Index coordinates
    val leftFootIndexX = leftFootIndex?.position?.x ?: 0f
    val leftFootIndexY = leftFootIndex?.position?.y ?: 0f
    val rightFootIndexX = rightFootIndex?.position?.x ?: 0f
    val rightFootIndexY = rightFootIndex?.position?.y ?: 0f

    // Angle Calculations (added Not A Number check)
    // Ankle Angles
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

    // Knee Angles
    var leftKneeAngle = GetAngles(leftAnkleX, leftAnkleY, leftKneeX, leftKneeY, leftHipX, leftHipY)
    if (!leftKneeAngle.isNaN()) {
        leftKneeAngles.add(leftKneeAngle)
    }
    else
    {
        count++
    }
    var rightKneeAngle = GetAngles(rightAnkleX, rightAnkleY, rightKneeX, rightKneeY, rightHipX, rightHipY)
    if (!rightKneeAngle.isNaN()) {
        rightKneeAngles.add(rightKneeAngle)
    }
    else
    {
        count++
    }

    // Hip Angles
    var leftHipAngle = GetAngles(leftKneeX, leftKneeY, leftHipX, leftHipY, leftShoulderX, leftShoulderY)
    if (!leftHipAngle.isNaN()) {
        leftHipAngles.add(leftHipAngle)
    }
    else
    {
        count++
    }
    var rightHipAngle = GetAngles(rightKneeX, rightKneeY, rightHipX, rightHipY, rightShoulderX, rightShoulderY)
    if (!rightHipAngle.isNaN()) {
        rightHipAngles.add(rightHipAngle)
    }
    else
    {
        count++
    }

    // Torso Angle
    var torsoAngle = calcTorso((leftHipX+rightHipX)/2,(leftHipY+rightHipY)/2,(rightShoulderX+leftShoulderX)/2,(rightShoulderY+leftShoulderY)/2)
    if (!torsoAngle.isNaN() && torsoAngle > -20 && torsoAngle < 20) {
        torsoAngles.add(torsoAngle)
    }
    else
    {
        count++
        Log.d("ErrorCheck","TorsoAngle: $torsoAngle, shoulder: ($rightShoulderX,$rightShoulderY) ($leftShoulderX,$leftShoulderY), Hip: ($rightHipX,$rightHipY) ($leftHipX,$leftHipY)")
    }



    //Calculate stride angle of current frame
    var strideAngle = calcStrideAngle(leftHeelX,leftHeelY,(leftHipX+rightHipX)/2f,(leftHipY+rightHipY)/2,rightHeelX,rightHeelY)
    strideAngles.add(strideAngle)


    //Draw skeleton view onto frame
    var canvas = Canvas(bitmap)

    var paintCircleRight = Paint()
    var paintCircleLeft = Paint()
    var paintLine = Paint()

    //Set colors of left and right side points and line connecting them
    paintCircleRight.setARGB(255,255,0,0)
    paintCircleLeft.setARGB(255, 0, 0, 255)
    paintLine.setARGB(255, 255,255,255)
    paintLine.strokeWidth = 4f

    //Draw Connection between hip and knee
    canvas.drawLine(rightHipX, rightHipY, rightKneeX,rightKneeY, paintLine)
    canvas.drawLine(leftHipX, leftHipY, leftKneeX,leftKneeY, paintLine)
    //Draw connection between knee an ankle
    canvas.drawLine(rightKneeX, rightKneeY, rightAnkleX,rightAnkleY, paintLine)
    canvas.drawLine(leftKneeX, leftKneeY, leftAnkleX,leftAnkleY, paintLine)
    //Draw connection between ankle and toe
    canvas.drawLine(rightAnkleX, rightAnkleY, rightFootIndexX,rightFootIndexY, paintLine)
    canvas.drawLine(leftAnkleX, leftAnkleY, leftFootIndexX,leftFootIndexY, paintLine)
    //Draw connection between ankle and heel
    canvas.drawLine(rightAnkleX, rightAnkleY, rightHeelX,rightHeelY, paintLine)
    canvas.drawLine(leftAnkleX, leftAnkleY, leftHeelX,leftHeelY, paintLine)
    //Draw connection between heel and toe
    canvas.drawLine(rightHeelX, rightHeelY, rightFootIndexX,rightFootIndexY, paintLine)
    canvas.drawLine(leftHeelX, leftHeelY, leftFootIndexX,leftFootIndexY, paintLine)
    //Draw Hip Points
    canvas.drawCircle(rightHipX,rightHipY,4f, paintCircleRight)
    canvas.drawCircle(leftHipX,leftHipY,4f, paintCircleLeft)
    //Draw Knee Points
    canvas.drawCircle(rightKneeX,rightKneeY,4f, paintCircleRight)
    canvas.drawCircle(leftKneeX,leftKneeY,4f, paintCircleLeft)
    //Draw Ankle Points
    canvas.drawCircle(rightAnkleX,rightAnkleY,4f, paintCircleRight)
    canvas.drawCircle(leftAnkleX,leftAnkleY,4f, paintCircleLeft)
    //Draw Heel Points
    canvas.drawCircle(rightHeelX,rightHeelY,4f, paintCircleRight)
    canvas.drawCircle(leftHeelX,leftHeelY,4f, paintCircleLeft)
    //Draw Toe Points
    canvas.drawCircle(rightFootIndexX,rightFootIndexY,4f, paintCircleRight)
    canvas.drawCircle(leftFootIndexX,leftFootIndexY,4f, paintCircleLeft)

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
        val frameInterval = (1000L * 1000L) / 30
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

    withContext(Dispatchers.Main)
    {
        activity.findViewById<TextView>(R.id.SplittingText).visibility = VISIBLE
        activity.findViewById<TextView>(R.id.CreationText).visibility = VISIBLE
        activity.findViewById<TextView>(R.id.splittingProgressValue).visibility = GONE
        activity.findViewById<TextView>(R.id.CreatingProgressValue).visibility = GONE
    }

    getFrameBitmaps(context, galleryUri, activity)
    withContext(Dispatchers.Main) {
        activity.findViewById<ProgressBar>(R.id.splittingBar).setProgress(100)
        activity.findViewById<TextView>(R.id.splittingProgressValue).text = " 100%"
    }
    if(frameList.isEmpty()) return galleryUri

    val firstFrame = frameList[0]
    val width = firstFrame.width
    val height = firstFrame.height

    val mediaMuxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    val format = MediaFormat.createVideoFormat("video/avc", width, height)
    format.setInteger(MediaFormat.KEY_BIT_RATE, 1000000)
    format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
    format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
    format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
    format.setInteger(MediaFormat.KEY_ROTATION, 0)

    mediaMuxer.setOrientationHint(0)

    val retriever1 = MediaMetadataRetriever()
    retriever1.setDataSource(context, galleryUri)
    val orientation = retriever1.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
    Log.d("ErrorCheck", "Video Orientation: $orientation")
    retriever1.release()

    val encoder = MediaCodec.createEncoderByType("video/avc")
    encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    val inputSurface = encoder.createInputSurface()
    encoder.start()

    val frameDurationUs = 1000000L / 30
    var trackIndex = -1
    var muxerStarted = false
    val bufferInfo = MediaCodec.BufferInfo()
    val listSize = frameList.size
    var progress: Int
    var frameI = 0

    withContext(Dispatchers.Main)
    {
        activity.findViewById<ProgressBar>(R.id.VideoCreation).visibility = VISIBLE
        activity.findViewById<TextView>(R.id.CreatingProgressValue).visibility = VISIBLE
        activity.findViewById<TextView>(R.id.CreatingProgressValue).text = " 0%"
    }

    for ((frameIndex, frame) in frameList.withIndex())
    {
        frameI = frameIndex
        val pose = processImageBitmap(frame)
        val modifiedBitmap = drawOnBitmap(frame, pose)

        val canvas = inputSurface.lockCanvas(null)
        canvas.drawBitmap(modifiedBitmap, 0f, 0f, null)
        inputSurface.unlockCanvasAndPost(canvas)

        while (true) {
            val outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, 10000)
            when {
                outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (!muxerStarted) {
                        trackIndex = mediaMuxer.addTrack(encoder.outputFormat)
                        mediaMuxer.start()
                        muxerStarted = true
                    }
                }
                outputBufferId >= 0 -> {
                    val outputBuffer = encoder.getOutputBuffer(outputBufferId) ?: continue
                    if (muxerStarted) {
                        bufferInfo.presentationTimeUs = frameIndex * frameDurationUs
                        mediaMuxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(outputBufferId, false)
                }
                outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER -> break
            }
        }
        progress = (((frameI + 1).toDouble() / listSize)*100).toInt()

        withContext(Dispatchers.Main)
        {
            activity.findViewById<ProgressBar>(R.id.VideoCreation).setProgress(progress)
            activity.findViewById<TextView>(R.id.CreatingProgressValue).text = (" " + progress.toString() + "%")
        }
    }

    encoder.signalEndOfInputStream()
    while (true) {
        val outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, 10000)
        if (outputBufferId >= 0) {
            val outputBuffer = encoder.getOutputBuffer(outputBufferId) ?: break
            if (muxerStarted) {
                bufferInfo.presentationTimeUs = frameI * frameDurationUs
                mediaMuxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
            }
            encoder.releaseOutputBuffer(outputBufferId, false)
        } else {
            break
        }
    }

    encoder.stop()
    encoder.release()
    mediaMuxer.stop()
    mediaMuxer.release()

    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(context, Uri.fromFile(File(outputPath)))
    retriever.release()

    withContext(Dispatchers.Main)
    {
        activity.findViewById<TextView>(R.id.SplittingText).visibility = GONE
        activity.findViewById<TextView>(R.id.CreationText).visibility = GONE
        activity.findViewById<ProgressBar>(R.id.VideoCreation).visibility = GONE
        activity.findViewById<ProgressBar>(R.id.splittingBar).visibility = GONE
        activity.findViewById<TextView>(R.id.splittingProgressValue).visibility = GONE
        activity.findViewById<TextView>(R.id.CreatingProgressValue).visibility = GONE
    }

    smoothDataUsingMovingAverage(rightKneeAngles, 5)
    smoothDataUsingMovingAverage(leftKneeAngles, 5)
    smoothDataUsingMovingAverage(rightHipAngles, 5)
    smoothDataUsingMovingAverage(leftHipAngles, 5)
    smoothDataUsingMovingAverage(rightAnkleAngles, 5)
    smoothDataUsingMovingAverage(leftAnkleAngles, 5)
    smoothDataUsingMovingAverage(torsoAngles, 5)
    smoothDataUsingMovingAverage(strideAngles, 5)

    val outputFile = File(outputPath)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", outputFile)
    Log.d("ErrorCheck", "Generated URI: $uri")
    return uri
}