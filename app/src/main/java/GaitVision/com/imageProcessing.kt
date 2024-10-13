package GaitVision.com

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions

/*
Name        : processImageBitmap
Parameters  :
    context : This parameter is the interface that contains global information about
              the application environment.
    bitmap  : This is the image bitmap we will use for processing.
Description : This function will take the context of the application and bitmap of the image/frame
              we will be processing. It then uses the ML Kit image processor to do pose detection on
              the image and get the pixel positions of specific points to be used for angle
              calculation in another function
Return: None
 */
fun processImageBitmap(context: Context, bitmap: Bitmap){
    //Setup pose detector options using accuracy mode on a still image
    val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
        .build()
    //Create instance of pose detector
    val poseDetector = PoseDetection.getClient(options)
    val image = InputImage.fromBitmap(bitmap, 0)
    if(image == null)
    {
        //display error with image bitmap.
    }

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

        }
        .addOnFailureListener { e ->
            println("Error detecting pose: ${e.message}")
        }

}

