package GaitVision.com

import GaitVision.com.databinding.ActivitySecondBinding
import GaitVision.com.GraphActivity.Companion.lineChartLeftKnee
import GaitVision.com.GraphActivity.Companion.lineChartRightKnee
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.icu.lang.UProperty.MATH
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.pose.Pose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.lang.Math.pow

fun plotLineGraph(lineChart: LineChart, angleData: List<Float>, label: String) {
    val entries = angleData.mapIndexed { index, angle -> Entry(index.toFloat(), angle) }

    val lineDataSet = LineDataSet(entries, label)
    lineDataSet.color = Color.BLUE // Set the color for the line
    lineDataSet.valueTextSize = 12f // Text size for data points
    val lineData = LineData(lineDataSet)

    lineChart.data = lineData
    lineChart.invalidate() // Refresh chart
}

//Angle vectors for average calculations and csv output
val leftAnkleAngles: MutableList<Float> = mutableListOf()
val rightAnkleAngles: MutableList<Float> = mutableListOf()
val leftKneeAngles: MutableList<Float> = mutableListOf()
val rightKneeAngles: MutableList<Float> = mutableListOf()
val leftHipAngles: MutableList<Float> = mutableListOf()
val rightHipAngles: MutableList<Float> = mutableListOf()

private var frameCounter = 0
private val frameSkip = 5  // Only update every 5 frames

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
    List<Bitmap> : List of bitmaps for images picked up from frames
 */
suspend fun getFrameBitmaps(context: Context,fileUri: Uri?, mBinding: ActivitySecondBinding): List<Bitmap>
{
    if(fileUri == null)
    {
        return emptyList()
    }
    //Declare and initialize constants that can be used for frame syncing
    val OPTION_PREVIOUS_SYNC = MediaMetadataRetriever.OPTION_PREVIOUS_SYNC
    val OPTION_NEXT_SYNC = MediaMetadataRetriever.OPTION_NEXT_SYNC
    val OPTION_CLOSEST_SYNC = MediaMetadataRetriever.OPTION_CLOSEST_SYNC
    val OPTION_CLOSEST = MediaMetadataRetriever.OPTION_CLOSEST

    //Declare and initialize variables to be used in function
    val retriever = MediaMetadataRetriever()
    val framesList = mutableListOf<Bitmap>()

    //Set data input
    retriever.setDataSource(context, fileUri)

    //Video length in microseconds
    if(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) == "video/mp4")
    {
        val videoLengthMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
        //Change this for more or less bitmaps
        //1000L = 1 second (1fps)
        val frameInterval = (1000L * 1000L) / 30//Change back to /30

        //Gets capture frame rate for possible use of retrieving frames
        //Floating point number (possibly Int if whole number)
        //val frameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)

        //Int. total number of frames in the video sequence
        //Might need to update API level to 28 (current 24) for this to work.
        //val frameCount = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)

        //Start time of video
        var currTime = 0L

        val videoLengthUs = videoLengthMs * 1000L

        withContext(Dispatchers.Main){mBinding.splittingBar.visibility = VISIBLE}
        withContext(Dispatchers.Main){mBinding.splittingProgressValue.visibility = VISIBLE}
        withContext(Dispatchers.Main){mBinding.splittingProgressValue.text = " 0%"}
        var progress : Int
        //Loop through all video and get frame bitmap at current position
        while(currTime <= videoLengthUs)
        {
            val frame = retriever.getFrameAtTime(currTime, OPTION_CLOSEST)
            if(frame != null)
            {
                framesList.add(frame)
            }
            progress = ((currTime.toDouble() / videoLengthUs)*100).toInt()
            withContext(Dispatchers.Main){mBinding.splittingBar.setProgress(progress)}
            withContext(Dispatchers.Main){mBinding.splittingProgressValue.text = (" " + progress.toString() + "%")}
            currTime += frameInterval
        }
    }
    else if(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) == "image/jpeg" || retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) == "image/png")
    {
        val stream = context.contentResolver.openInputStream(fileUri)
        val frame = BitmapFactory.decodeStream(stream)
        framesList.add(frame)
    }

    //Release resources
    retriever.release()

    //Return bitmap list
    return framesList
}

/*
Name        : processImageBitmap
Parameters  :
    context : This parameter is the interface that contains global information about
              the application environment.
    bitmap  : This is the image bitmap we will use for processing. Can't be NULL
Description : This function will take the context of the application and bitmap of the image/frame
              we will be processing. It then uses the ML Kit image processor to do pose detection on
              the image and get the pixel positions of specific points to be used for angle
              calculation in another function.
Return      :
    Pose    : Returns all of the pose landmarks and their information to calling function for use.
 */
suspend fun processImageBitmap(context: Context, bitmap: Bitmap): Pose?
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

fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap
{
    return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false)
}

/*
Name                   : drawOnBitmap
Parameters             :
    Bitmap             : The bitmap we are drawing the angle text onto.
    Pose               : The pose object holding all the landmarks and their positions of the
                         current bitmap.
    leftAnkleAngles    : The list of angles for left ankle in the video to store into for graphing
                         and CSV output.
    rightAnkleAngles   : The list of angles for right ankle in the video to store into for graphing
                         and CSV output.
    leftKneeAngles     : The list of angles for left knee in the video to store into for graphing
                         and CSV output.
    rightKneeAngles    : The list of angles for right knee in the video to store into for graphing
                         and CSV output.
    leftHipAngles      : The list of angles for left hip in the video to store into for graphing
                         and CSV output.
    rightHipAngles     : The list of angles for right hip in the video to store into for graphing
                         and CSV output.
Description            : This function with take the current bitmap, pose object of the current
                         bitmap and the list used to store Each angle for the entire video and will
                         calculate the angle for each keypoint and store their values. It will then
                         display those values to the screen on the bitmap. We will finally draw the
                         skeleton frame for the lower half of the body onto the bitmap over the
                         positions of the landmarks for visual representation and checking.
Return                 :
    Bitmap             : Returns the edited bitmap with the skeleton frame and angle text on it.
 */
fun drawOnBitmap(bitmap: Bitmap,
                 pose: Pose?,
                 leftAnkleAngles: MutableList<Float>,
                 rightAnkleAngles: MutableList<Float>,
                 leftKneeAngles: MutableList<Float>,
                 rightKneeAngles: MutableList<Float>,
                 leftHipAngles: MutableList<Float>,
                 rightHipAngles: MutableList<Float>, angle : String): Bitmap
{
    //Get all landmarks in image
    //val allPoseLandMarks = pose.getAllPoseLandmarks() //Test case for all landmarks on image
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
    var leftAnkleAngle = GetAngles(leftFootIndexX, leftFootIndexY, leftAnkleX, leftAnkleY, leftKneeX, leftKneeY)
    if (!leftAnkleAngle.isNaN()) {
        leftAnkleAngles.add(leftAnkleAngle)
    }

    var rightAnkleAngle = GetAngles(rightFootIndexX, rightFootIndexY, rightAnkleX, rightAnkleY, rightKneeX, rightKneeY)
    if (!rightAnkleAngle.isNaN()) {
        rightAnkleAngles.add(rightAnkleAngle)
    }

    // Knee Angles
    var leftKneeAngle = GetAngles(leftAnkleX, leftAnkleY, leftKneeX, leftKneeY, leftHipX, leftHipY)
    if (!leftKneeAngle.isNaN()) {
        leftKneeAngles.add(leftKneeAngle)
    }

    var rightKneeAngle = GetAngles(rightAnkleX, rightAnkleY, rightKneeX, rightKneeY, rightHipX, rightHipY)
    if (!rightKneeAngle.isNaN()) {
        rightKneeAngles.add(rightKneeAngle)
    }

    // Hip Angles
    var leftHipAngle = GetAngles(leftKneeX, leftKneeY, leftHipX, leftHipY, leftShoulderX, leftShoulderY)
    if (!leftHipAngle.isNaN()) {
        leftHipAngles.add(leftHipAngle)
    }

    var rightHipAngle = GetAngles(rightKneeX, rightKneeY, rightHipX, rightHipY, rightShoulderX, rightShoulderY)
    if (!rightHipAngle.isNaN()) {
        rightHipAngles.add(rightHipAngle)
    }

    var canvas = Canvas(bitmap)
    var rectPaint = Paint()
    rectPaint.setARGB(255,255,255,255)
    if(angle != "all") {
        //lIMIT RIGHT: 480
        //LIMIT BOTTOM: 270
        canvas.drawRect(0F, 0F, 350F, 150F, rectPaint)
    }
    else if(angle == "all")
    {
        canvas.drawRect(0F,0F,1400F,150F,rectPaint)
    }
    //canvas.drawRect(20F,0F, 20+recLength, 0+recHeight, rectPaint)
    if(angle == "hip")
    {
        var text = "Right Hip: ${rightHipAngle}\u00B0"
        var paint = Paint()
        paint.setARGB(255,0,0,0)
        paint.textSize = 40.0F
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD))
        canvas.drawText(text, 10F, 75F, paint)
        text = "Left Hip: ${leftHipAngle}\u00B0"
        canvas.drawText(text, 10F, 125F, paint)
    }
    else if(angle == "knee")
    {
        var text = "Right Knee: ${rightKneeAngle}\u00B0"
        var paint = Paint()
        paint.setARGB(255,0,0,0)
        paint.textSize = 40.0F
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD))
        canvas.drawText(text, 10F, 75F, paint)
        text = "Left Knee: ${leftKneeAngle}\u00B0"
        canvas.drawText(text, 10F, 125F, paint)
    }
    else if(angle == "ankle")
    {
        var text = "Right Ankle: ${rightAnkleAngle}\u00B0"
        var paint = Paint()
        paint.setARGB(255,0,0,0)
        paint.textSize = 40.0F
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD))
        canvas.drawText(text, 10F, 75F, paint)
        text = "Left Ankle: ${leftAnkleAngle}\u00B0"
        canvas.drawText(text, 10F, 125F, paint)
    }
    else if(angle == "torso")
    {
        var text = "Right Torso: Dummy Text"
        var paint = Paint()
        paint.setARGB(255,0,0,0)
        paint.textSize = 40.0F
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD))
        canvas.drawText(text, 10F, 75F, paint)
        text = "Left Torso: Dummy Text"
        canvas.drawText(text, 10F, 125F, paint)
    }
    else if(angle == "all")
    {
        var text = "Right Hip: ${rightHipAngle}\u00B0"
        var paint = Paint()
        paint.setARGB(255,0,0,0)
        paint.textSize = 40.0F
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD))
        canvas.drawText(text, 10F, 75F, paint)
        text = "Left Hip: ${leftHipAngle}\u00B0"
        canvas.drawText(text, 10F, 125F, paint)

        text = "Right Knee: ${rightKneeAngle}\u00B0"
        canvas.drawText(text, 360F, 75F, paint)
        text = "Left Knee: ${leftKneeAngle}\u00B0"
        canvas.drawText(text, 360F, 125F, paint)

        text = "Right Ankle: ${rightAnkleAngle}\u00B0"
        canvas.drawText(text, 710F, 75F, paint)
        text = "Left Ankle: ${leftAnkleAngle}\u00B0"
        canvas.drawText(text, 710F, 125F, paint)

        text = "Right Torso: Dummy Text"
        canvas.drawText(text, 1060F, 75F, paint)
        text = "Left Torso: Dummy Text"
        canvas.drawText(text, 1060F, 125F, paint)
    }


    var paintCircleRight = Paint()
    var paintCircleLeft = Paint()
    var paintLine = Paint()

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

class GraphActivity : ComponentActivity() {
    companion object {
        lateinit var lineChartLeftKnee: LineChart
        lateinit var lineChartRightKnee: LineChart
        lateinit var lineChartLeftAnkle: LineChart
        lateinit var lineChartRightAnkle: LineChart
        lateinit var lineChartLeftHip: LineChart
        lateinit var lineChartRightHip: LineChart
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        lineChartLeftKnee = findViewById(R.id.lineChartLeftKnee)
        lineChartRightKnee = findViewById(R.id.lineChartRightKnee)
        lineChartLeftAnkle = findViewById(R.id.lineChartLeftAnkle)
        lineChartRightAnkle = findViewById(R.id.lineChartRightAnkle)
        lineChartLeftHip = findViewById(R.id.lineChartLeftHip)
        lineChartRightHip = findViewById(R.id.lineChartRightHip)

        plotLineGraph(lineChartLeftKnee, leftKneeAngles, "Left Knee Angles")
        plotLineGraph(lineChartRightKnee, rightKneeAngles, "Right Knee Angles")
        plotLineGraph(lineChartLeftAnkle, leftAnkleAngles, "Left Ankle Angles")
        plotLineGraph(lineChartRightAnkle, rightAnkleAngles, "Right Ankle Angles")
        plotLineGraph(lineChartLeftHip, leftHipAngles, "Left Hip Angles")
        plotLineGraph(lineChartRightHip, rightHipAngles, "Right Hip Angles")


        val uploadCSVBtn = findViewById<Button>(R.id.upload_csv_btn)
        uploadCSVBtn.setOnClickListener {
            val intent = Intent(this, ThirdActivity::class.java)
            startActivity(intent)
        }
    }
}

fun ensureLandscapeOrientation(bitmap: Bitmap): Bitmap {
    return if (bitmap.width < bitmap.height) {
        // Rotate the bitmap 90 degrees to landscape
        val matrix = Matrix()
        matrix.postRotate(90f)
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap // Already landscape
    }
}

/*
Name           : ProcVid
Parameters     :
    context    : This parameter is the interface that contains global information about
                 the application environment.
    uri        : This is the video Uri that we will be working on.
    outputPath : This is the output path the new video with all the processing on should be
                 saved to.
    mBinding   : This is the view of the activity page. Use this for messing with XML features.
Description    : This is the master function of the entire video processing sequence.
                 It will call all the helper functions that are needed to run the processing
                 and video encoding.
Return         :
    Uri        : This is the new video's uri that has all the drawing and pose detection
                 displayed on it.
 */
suspend fun ProcVid(context: Context, uri: Uri?, outputPath: String, mBinding: ActivitySecondBinding, angle : String): Uri?
{

    withContext(Dispatchers.Main){mBinding.SplittingText.visibility = VISIBLE}
    withContext(Dispatchers.Main){mBinding.CreationText.visibility = VISIBLE}
    withContext(Dispatchers.Main){mBinding.splittingProgressValue.visibility = GONE}
    withContext(Dispatchers.Main){mBinding.CreatingProgressValue.visibility = GONE}
    //val testList: MutableList<Pair<Float, Long>> = mutableListOf()
    val framesList = getFrameBitmaps(context, uri, mBinding) // Get frames from the original video
    if(framesList.isEmpty()) return uri

    val firstFrame = framesList[0]
    val width = firstFrame.width
    val height = firstFrame.height

    val mediaMuxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    var format = MediaFormat.createVideoFormat("video/avc", 1920, 1080)
    format.setInteger(MediaFormat.KEY_BIT_RATE, 1000000)
    format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
    format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
    format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
    format.setInteger(MediaFormat.KEY_ROTATION, 0)

    mediaMuxer.setOrientationHint(0)

    val retriever1 = MediaMetadataRetriever()
    retriever1.setDataSource(context, uri)
    Log.d("errorchecking","Video Orientation: ${retriever1.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)}")
    retriever1.release()

    var encoder = MediaCodec.createEncoderByType("video/avc")
    encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    var inputSurface = encoder.createInputSurface()
    encoder.start()

    val frameDurationUs = 1000000L / 30  // microseconds per frame for 30 fps

    var trackIndex = -1
    var muxerStarted = false
    val bufferInfo = MediaCodec.BufferInfo()

    val listSize = framesList.size
    var progress : Int

    var frameI = 0
    withContext(Dispatchers.Main){mBinding.VideoCreation.visibility = VISIBLE}
    withContext(Dispatchers.Main){mBinding.CreatingProgressValue.visibility = VISIBLE}
    withContext(Dispatchers.Main){mBinding.CreatingProgressValue.text = " 0%"}
    for ((frameIndex, frame) in framesList.withIndex())
    {
        frameI = frameIndex
        val oriFrame = ensureLandscapeOrientation(frame)
        val orientedFrame = resizeBitmap(oriFrame,1920,1080)
        val pose = processImageBitmap(context, orientedFrame)
        val modifiedBitmap = drawOnBitmap(orientedFrame, pose, leftAnkleAngles, rightAnkleAngles, leftKneeAngles, rightKneeAngles, leftHipAngles, rightHipAngles, angle)
        // Log check to see example of mutable list
        Log.d("MutableListContents", "leftKneeAngles after processing: $leftKneeAngles")
        Log.d("MutableListContents", "rightKneeAngles after processing: $rightKneeAngles")
        // Draw the frame onto the encoder input surface
        val canvas = inputSurface.lockCanvas(null)
        canvas.drawBitmap(modifiedBitmap, 0f, 0f, null)
        inputSurface.unlockCanvasAndPost(canvas)

        // Drain encoder output buffers
        while (true) {
            val outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, 10000)
            when {
                outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    // Add track and start the muxer once
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
        withContext(Dispatchers.Main){mBinding.VideoCreation.setProgress(progress)}
        withContext(Dispatchers.Main){mBinding.CreatingProgressValue.text = (" " + progress.toString() + "%")}
    }

    // Signal end of input stream and finalize remaining buffers
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


    // Stop and release encoder and muxer
    encoder.stop()
    encoder.release()
    mediaMuxer.stop()
    mediaMuxer.release()

    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(context, Uri.fromFile(File(outputPath)))
    val videoLengthMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
    retriever.release()

    withContext(Dispatchers.Main){mBinding.SplittingText.visibility = GONE}
    withContext(Dispatchers.Main){mBinding.CreationText.visibility = GONE}
    withContext(Dispatchers.Main){mBinding.VideoCreation.visibility = GONE}
    withContext(Dispatchers.Main){mBinding.splittingBar.visibility = GONE}
    withContext(Dispatchers.Main){mBinding.splittingProgressValue.visibility = GONE}
    withContext(Dispatchers.Main){mBinding.CreatingProgressValue.visibility = GONE}


    return Uri.fromFile(File(outputPath))
}

