package GaitVision.com

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions

/*

 */
fun processImageStatic(context: Context,uri: Uri){
    //Setup pose detector options using accuracy mode on a still image
    val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
        .build()
    //Create instance of pose detector
    val poseDetector = PoseDetection.getClient(options)

    /*//Get file path (This is wrong, but gets the idea across 10/8/24 Trevor Spencer)
    val file = File("C:\\Users\\tspen\\OneDrive\\Pictures\\LinkedIn Profile picture.jpg")
    //Content file path to URI
    val imageUri = Uri.fromFile(file)
    */
    //Current purpose This will be a test static image. Should be changed to user input later on 10/10/24 Trevor Spencer
    //val uri = Uri.parse("file:///storage/emulated/0/Download/testImage1.jpg")
    val image = InputImage.fromFilePath(context, uri)
    if(image == null)
    {
        //display error with file URI and context.
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

