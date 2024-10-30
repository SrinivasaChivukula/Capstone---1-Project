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
fun drawOnBitmap(bitmap: Bitmap, pose: Pose?): Bitmap
{
    var text = "${pose?.getPoseLandmark(26)?.position?.x}"
    var canvas = Canvas(bitmap)
    var paint = Paint()
    paint.setARGB(255,0,0,0)
    paint.textSize = 20.0F
    canvas.drawText(text, 10F, 10F, paint)

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

    for ((frameIndex, frame) in framesList.withIndex())
    {
        val pose = processImageBitmap(context, frame)
        val modifiedBitmap = drawOnBitmap(frame, pose)
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

    return Uri.fromFile(File(outputPath))
}