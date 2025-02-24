package GaitVision.com

import android.graphics.Bitmap
import android.net.Uri

var galleryUri : Uri? = null
var editedUri : Uri? = null
var frameList : MutableList<Bitmap> = mutableListOf()
var leftAnkleAngles : MutableList<Float> = mutableListOf()
var rightAnkleAngles : MutableList<Float> = mutableListOf()
var leftKneeAngles : MutableList<Float> = mutableListOf()
var rightKneeAngles : MutableList<Float> = mutableListOf()
var leftHipAngles : MutableList<Float> = mutableListOf()
var rightHipAngles : MutableList<Float> = mutableListOf()
var torsoAngles : MutableList<Float> = mutableListOf()
var participantId: String = ""
var participantHeight: Int = 0

var count: Int = 0;

var strideAngles: MutableList<Float> = mutableListOf()
var strideLengths: MutableList<Float> = mutableListOf()
var centerOfMasses: MutableList<Pair<Float, Float>> = mutableListOf()
var minLeftAnkleY: MutableList<Float> = mutableListOf()
var minRightAnkleY: MutableList<Float> = mutableListOf()

var videoLength: Long = 0
