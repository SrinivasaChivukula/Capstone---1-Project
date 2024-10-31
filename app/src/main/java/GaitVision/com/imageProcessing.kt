package GaitVision.com

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.pose.Pose
import kotlinx.coroutines.tasks.await
import java.io.File
import java.nio.ByteBuffer

private var frameCounter = 0
private val frameSkip = 5  // Only update every 5 frames

/*
Name             : getFrameBitmaps
Parameters       :
    context      : This parameter is the interface that contains global information about
                   the application environment.
    fileUri      : This parameter is the uri to the video that will be used in the function.
Description      : This function takes a uri of a video and sends it through a process to get a
                   bitmap of every frame in the video so pose tracking can be done on it.
Return           :
    List<Bitmap> : List of bitmaps for images picked up from frames
 */
fun getFrameBitmaps(context: Context,fileUri: Uri?): List<Bitmap>
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
    Log.d("ErrorChecking", "MIME type: ${retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)}")

    //Video length in microseconds
    if(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) == "video/mp4")
    {
        val videoLengthMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
        Log.d("ErrorChecking","Video Length: ${videoLengthMs}")
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

        //Loop through all video and get frame bitmap at current position
        while(currTime <= videoLengthUs)
        {
            Log.d("ErrorChecking","Current Time(S): ${currTime}")
            val frame = retriever.getFrameAtTime(currTime, OPTION_CLOSEST)
            if(frame != null)
            {
                Log.d("ErrorChecking","Adding frame to list")
                framesList.add(frame)
            }
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
              calculation in another function
Return: None
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
        Log.d("ErrorChecking", "Bitmap Processed")
        poseDetector.process(image).await()
    }
    catch (e: Exception)
    {
        Log.e("InputImage", "Error creating InputImage from bitmap: ${e.message}")
        null
    }


}

/*
Name: drawOnBitmap
Parameters:
        Bitmap:
Description:
Return:
 */
fun drawOnBitmap(bitmap: Bitmap,
                 pose: Pose?,
                 leftAnkleAngles: MutableList<Float>,
                 rightAnkleAngles: MutableList<Float>,
                 leftKneeAngles: MutableList<Float>,
                 rightKneeAngles: MutableList<Float>,
                 leftHipAngles: MutableList<Float>,
                 rightHipAngles: MutableList<Float>): Bitmap
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

    // Angle Calculations
    // Ankle Angles
    val leftAnkleAngle = GetAngles(leftFootIndexX, leftFootIndexY, leftAnkleX, leftAnkleY, leftKneeX, leftKneeY)
    leftAnkleAngles.add(leftAnkleAngle)
    val rightAnkleAngle = GetAngles(rightFootIndexX, rightFootIndexY, rightAnkleX, rightAnkleY, rightKneeX, rightKneeY)
    rightAnkleAngles.add(rightAnkleAngle)

    // Knee Angles
    val leftKneeAngle = GetAngles(leftAnkleX, leftAnkleY, leftKneeX, leftKneeY, leftHipX, leftHipY)
    leftKneeAngles.add(leftKneeAngle)
    val rightKneeAngle = GetAngles(rightAnkleX, rightAnkleY, rightKneeX, rightKneeY, rightHipX, rightHipY)
    rightKneeAngles.add(rightKneeAngle)

    // Hip Angles
    val leftHipAngle = GetAngles(leftKneeX, leftKneeY, leftHipX, leftHipY, leftShoulderX, leftShoulderY)
    leftHipAngles.add(leftHipAngle)
    val rightHipAngle = GetAngles(rightKneeX, rightKneeY, rightHipX, rightHipY, rightShoulderX, rightShoulderY)
    rightHipAngles.add(rightHipAngle)

    var text = "Right Hip: ${rightHipAngle}\u00B0"
    var canvas = Canvas(bitmap)
    var paint = Paint()
    paint.setARGB(255,0,0,0)
    paint.textSize = 20.0F
    canvas.drawText(text, 25F, 25F, paint)

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

/*
Name:
Parameters:
Description
Return:
 */
suspend fun ProcVid(context: Context, uri: Uri?, outputPath: String): Uri?
{
    //Angle vectors for average calculations and csv output
    val leftAnkleAngles: MutableList<Float> = mutableListOf()
    val rightAnkleAngles: MutableList<Float> = mutableListOf()
    val leftKneeAngles: MutableList<Float> = mutableListOf()
    val rightKneeAngles: MutableList<Float> = mutableListOf()
    val leftHipAngles: MutableList<Float> = mutableListOf()
    val rightHipAngles: MutableList<Float> = mutableListOf()

    //val testList: MutableList<Pair<Float, Long>> = mutableListOf()
    val framesList = getFrameBitmaps(context, uri) // Get frames from the original video
    if(framesList.isEmpty()) return uri

    val firstFrame = framesList[0]
    val width = firstFrame.width
    val height = firstFrame.height

    val mediaMuxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    var format = MediaFormat.createVideoFormat("video/avc",width, height)
    format.setInteger(MediaFormat.KEY_BIT_RATE, 1000000)
    format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
    format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
    format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)

    var encoder = MediaCodec.createEncoderByType("video/avc")
    encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    var inputSurface = encoder.createInputSurface()
    encoder.start()

    val frameDurationUs = 1000000L / 30  // microseconds per frame for 30 fps

    var trackIndex = -1
    var muxerStarted = false
    val bufferInfo = MediaCodec.BufferInfo()

    var frameI = 0
    for ((frameIndex, frame) in framesList.withIndex())
    {
        frameI = frameIndex
        val pose = processImageBitmap(context, frame)
        val modifiedBitmap = drawOnBitmap(frame, pose, leftAnkleAngles, rightAnkleAngles, leftKneeAngles, rightKneeAngles, leftHipAngles, rightHipAngles)
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
    Log.d("ErrorChecking","Video Length: ${videoLengthMs}")

    return Uri.fromFile(File(outputPath))
}