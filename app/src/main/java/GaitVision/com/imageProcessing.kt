package GaitVision.com

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions

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
fun getFrameBitmaps(context: Context,fileUri: Uri): List<Bitmap>
{
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
        val videoLength = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L

        //Change this for more or less bitmaps
        //1000000L = 1 second
        val frameInterval = 1000000L

        //Gets capture frame rate for possible use of retrieving frames
        //Floating point number (possibly Int if whole number)
        //val frameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)

        //Int. total number of frames in the video sequence
        //Might need to update API level to 28 (current 24) for this to work.
        //val frameCount = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)

        //Start time of video
        var currTime = 0L

        //Loop through all video and get frame bitmap at current position
        while(currTime <= videoLength)
        {
            val frame = retriever.getFrameAtTime(currTime, OPTION_CLOSEST)
            if(frame != null)
            {
                framesList.add(frame)
            }
            currTime += frameInterval
        }
    }
    else if(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) == "image/png" || retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) == "image/png")
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
fun processImageBitmap(context: Context, bitmap: Bitmap)
{
    //Setup pose detector options using accuracy mode on a still image
    val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
        .build()
    //Create instance of pose detector
    val poseDetector = PoseDetection.getClient(options)

    //Try-Catch statement because InputImage throws exception if there was an error creating the InputImage
    try
    {
        val image = InputImage.fromBitmap(bitmap, 0)

        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                //Get all landmarks in image
                //val allPoseLandMarks = pose.getAllPoseLandmarks() //Test case for all landmarks on image
                //Get values for specific locations for calculations
                val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
                val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
                val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
                val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
                val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
                val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
                val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
                val leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
                val rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
                val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
                val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)

                //Get the position of a landmark on the bitmap
                val leftShoulderx = leftShoulder?.position?.x
                val leftShouldery = leftShoulder?.position?.y
                
                Log.d("ErrorChecking","Landmarks detected")
            }
            .addOnFailureListener { e ->
                Log.e("PoseDetection","Error detecting pose: ${e.message}")
            }
    }
    catch (e: Exception)
    {
        Log.e("InputImage", "Error creating InputImage from bitmap: ${e.message}")
    }


}

/*
Name        : processFrames
Parameters  :
    context : This parameter is the interface that contains global information about
              the application environment.
    uri     : This parameter is the uri to the video that will be used to pass to the function to
              get bitmaps of every frame.
Description : This function is will take a file uri from the gallery and send it to a function to
              get a bitmap list of every frame that can be used for pose tracking in a
              separate function.
Return      : None
 */
fun processFrames(context: Context, uri: Uri)
{
    val framesList = getFrameBitmaps(context, uri)
    for(frame in framesList)
    {
        processImageBitmap(context, frame)
    }

}

