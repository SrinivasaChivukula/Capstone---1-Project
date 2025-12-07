package GaitVision.com

import android.graphics.Bitmap
import android.net.Uri

//Any URI or frames used in application
var galleryUri : Uri? = null
var editedUri : Uri? = null
var frameList : MutableList<Bitmap> = mutableListOf()

//All angles list for use in application
var leftAnkleAngles : MutableList<Float> = mutableListOf()
var rightAnkleAngles : MutableList<Float> = mutableListOf()
var leftKneeAngles : MutableList<Float> = mutableListOf()
var rightKneeAngles : MutableList<Float> = mutableListOf()
var leftHipAngles : MutableList<Float> = mutableListOf()
var rightHipAngles : MutableList<Float> = mutableListOf()
var torsoAngles : MutableList<Float> = mutableListOf()
var strideAngles: MutableList<Float> = mutableListOf()

// NEW: For 6/9-feature extraction (v2) needed for the stride replacement feature, dont remove
var interAnkleDistances: MutableList<Float> = mutableListOf()
var legLengths: MutableList<Float> = mutableListOf()

// NEW: Video FPS (detected from video, default 30)
var detectedFps: Float = 30f

//User input for ID and height
var participantId: String = ""
var participantHeight: Int = 0

//Database IDs for current session
var currentPatientId: Long? = null
var currentVideoId: Long? = null

//Variable for counting angle faults
var count: Int = 0;

//All angle list used in gait score analysis
var leftKneeMinAngles : MutableList<Float> = mutableListOf()
var leftKneeMaxAngles : MutableList<Float> = mutableListOf()
var rightKneeMinAngles : MutableList<Float> = mutableListOf()
var rightKneeMaxAngles : MutableList<Float> = mutableListOf()
var torsoMinAngles : MutableList<Float> = mutableListOf()
var torsoMaxAngles : MutableList<Float> = mutableListOf()

//Variable for keeping track of video length we processed on
var videoLength: Long = 0
