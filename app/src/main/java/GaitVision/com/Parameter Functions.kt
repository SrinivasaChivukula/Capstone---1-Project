package GaitVision.com
import kotlin.math.exp
import kotlin.math.PI
import kotlin.math.pow
import android.util.Log
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sqrt

fun symmetry()
{
    var strideAngleMaxs: List<Float> = FindLocalMax(strideAngles)
    var strideLengthMaxs: List<Float>

    for(i in strideAngleMaxs.indices)
    {
        Log.d("ErrorCheck", "Stride Angle: ${strideAngleMaxs[i]} Stride Length: ${strideLengths[i]}")
    }

}

fun swingTimeWithFrameIndex(){

}

fun stepTime()
{

}


fun centerOfMass(lHipX: Float, lHipY: Float, rHipX: Float, rHipY: Float,lShoulderX: Float, lShoulderY: Float, rShoulderX: Float, rShoulderY: Float)
{
    var comX = (lHipX + rHipX + lShoulderX + rShoulderX) / 4F
    var comY = (lHipY + rHipY + lShoulderY + rShoulderY) / 4F
    centerOfMasses.add(Pair(comX,comY))
}

fun calcTorso(hipX: Float, hipY: Float, shoulderX: Float, shoulderY: Float): Float {
    // Calculate the differences in X and Y coordinates
    val deltaX = hipX - shoulderX
    val deltaY = hipY - shoulderY

    // Calculate the torso angle relative to the vertical axis
    val angle = atan2(deltaX.toDouble(), deltaY.toDouble()) * (180 / PI) // Convert to degrees

    return angle.toFloat()
}

fun calcStrideAngle(leftHeelX: Float, leftHeelY: Float, hipx: Float, hipY: Float, rightHeelX: Float, rightHeelY: Float) : Float
{
    var htol = sqrt(((leftHeelX - hipx)*(leftHeelX - hipx))+((leftHeelY-hipY)*(leftHeelY-hipY)))
    var htor = sqrt(((rightHeelX - hipx)*(rightHeelX - hipx))+((rightHeelY-hipY)*(rightHeelY-hipY)))
    var ltor = sqrt(((leftHeelX - rightHeelX)*(leftHeelX - rightHeelX))+((leftHeelY-rightHeelY)*(leftHeelY-rightHeelY)))

    var cosAngle : Float = ((htol * htol) + (htor * htor) - (ltor * ltor))/(2*htol*htor)
    var angle = acos(cosAngle) * (180/PI.toFloat())
    strideLengths.add(ltor)
    return String.format("%.2f", angle).toFloat()
}




fun smoothDataUsingGaussianFilter(data: MutableList<Float>, sigma: Double) {
    val smoothedData = mutableListOf<Float>()
    val windowSize = (sigma * 6).toInt()  // Usually window size is 6 * sigma

    // Calculate the Gaussian kernel
    val kernel = Array(windowSize) { i ->
        val x = i - windowSize / 2
        exp(-0.5 * (x / sigma).pow(2)) / (sigma * sqrt(2 * PI))
    }

    // Normalize the kernel
    val kernelSum = kernel.sum()
    val normalizedKernel = kernel.map { it / kernelSum }

    for (i in data.indices) {
        // Apply the Gaussian filter (convolution with the kernel)
        var smoothedValue = 0.0
        for (j in 0 until windowSize) {
            val index = i + j - windowSize / 2
            if (index in data.indices) {
                smoothedValue += data[index] * normalizedKernel[j]
            }
        }
        smoothedData.add(smoothedValue.toFloat())
    }

    // Update the original list with the smoothed values
    data.clear()
    data.addAll(smoothedData)
}

fun smoothDataUsingEMA(data: MutableList<Float>, alpha: Float) {
    var previousEMA = data[0]  // Initialize with the first data point

    for (i in data.indices) {
        // Calculate the new EMA
        val newEMA = alpha * data[i] + (1 - alpha) * previousEMA
        data[i] = newEMA  // Update the list in place
        previousEMA = newEMA
    }
}

fun smoothDataUsingMovingAverage(data: MutableList<Float>, windowSize: Int) {
    val smoothedData = mutableListOf<Float>()

    for (i in data.indices) {
        // Calculate the window range
        val windowStart = maxOf(i - windowSize / 2, 0)
        val windowEnd = minOf(i + windowSize / 2, data.size - 1)

        // Calculate the average for the current window
        val window = data.subList(windowStart, windowEnd + 1)
        val avg = window.average().toFloat()

        smoothedData.add(avg)
    }

    // Update the original list with the smoothed values
    data.clear()  // Clear the original list
    data.addAll(smoothedData)  // Add the smoothed data back
}

fun GetAnglesA(x1: Float,y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Float
{
    // Get Distances
    val Length1: Float = CalculateDistance(x1,y1,x2,y2)
    val Length2: Float = CalculateDistance(x2,y2,x3,y3)
    val Length3: Float = CalculateDistance(x1,y1,x3,y3)
    //use law of COS to help find angle
    val CosAngle: Float = (Length1 * Length1 + Length2 * Length2 - Length3 * Length3) / (2 * Length1 * Length2)
    //find Angle then convert from radian to degree
    val Angle: Float = acos(CosAngle) * (180/PI.toFloat())
    //Might change later
    val smallerAngle: Float = Angle - 90f
    return String.format("%.2f", smallerAngle).toFloat() //round to 2 decimal places
}

fun calculateStanceTimes(
    angles: MutableList<Float>,
    threshold: Float = 20f,
    minStanceDuration: Float = 0.1f,
    frameRate: Int = 30
): List<Pair<Float, Float>> {
    val stanceTimes = mutableListOf<Pair<Float, Float>>()
    var inStance = false
    var stanceStartTime: Float? = null

    // Iterate over each frame's angle.
    for (i in angles.indices) {
        val timestamp = i / frameRate.toFloat()  // Convert frame index to seconds.
        val angle = angles[i]

        // Assume the foot is in stance if the absolute anatomical ankle angle is within the threshold.
        if (!inStance) {
            if (kotlin.math.abs(angle) <= threshold) {
                inStance = true
                stanceStartTime = timestamp
            }
        } else {
            // If the angle leaves the threshold, the stance phase has ended.
            if (kotlin.math.abs(angle) > threshold) {
                inStance = false
                val stanceEndTime = timestamp
                val duration = stanceEndTime - (stanceStartTime ?: stanceEndTime)
                if (duration >= minStanceDuration) {
                    stanceTimes.add(Pair(stanceStartTime ?: 0f, stanceEndTime))
                }
            }
        }
    }

    // In case the stance phase continues until the last frame:
    if (inStance && stanceStartTime != null) {
        val stanceEndTime = angles.size / frameRate.toFloat()
        val duration = stanceEndTime - stanceStartTime
        if (duration >= minStanceDuration) {
            stanceTimes.add(Pair(stanceStartTime, stanceEndTime))
        }
    }

    return stanceTimes
}

fun averageStanceTime(stanceTimes: List<Pair<Float, Float>>): Float {
    if (stanceTimes.isEmpty()) return 0f

    var totalDuration = 0f
    for ((start, end) in stanceTimes) {
        totalDuration += (end - start)
    }
    return totalDuration / stanceTimes.size
}

fun calculateSwingTimes(
    angles: MutableList<Float>,
    // Use a threshold that defines when the foot is considered "in swing".
    // For example, if the anatomical ankle angle (deviation from neutral) is greater than 20°,
    // we consider the foot to be in swing.
    threshold: Float = 20f,
    minSwingDuration: Float = 0.1f,  // Minimum duration (in seconds) to count as a valid swing phase.
    frameRate: Int = 30             // Frames per second.
): List<Pair<Float, Float>> {
    val swingTimes = mutableListOf<Pair<Float, Float>>()
    var inSwing = false
    var swingStartTime: Float? = null

    // Loop through each frame in the angle time series.
    for (i in angles.indices) {
        val timestamp = i / frameRate.toFloat()  // Convert frame index to seconds.
        val angle = angles[i]

        // Here, we define the swing phase as when the absolute anatomical angle is above the threshold.
        if (!inSwing) {
            // Not currently in swing, so check if we are entering swing.
            if (kotlin.math.abs(angle) > threshold) {
                inSwing = true
                swingStartTime = timestamp
            }
        } else {
            // We are in swing. Check if the foot is returning to a stance (angle falls within threshold).
            if (kotlin.math.abs(angle) <= threshold) {
                inSwing = false
                val swingEndTime = timestamp
                val duration = swingEndTime - (swingStartTime ?: swingEndTime)
                if (duration >= minSwingDuration) {
                    swingTimes.add(Pair(swingStartTime ?: 0f, swingEndTime))
                }
            }
        }
    }

    // If a swing phase is ongoing at the end of the sequence, record it.
    if (inSwing && swingStartTime != null) {
        val swingEndTime = angles.size / frameRate.toFloat()
        val duration = swingEndTime - swingStartTime
        if (duration >= minSwingDuration) {
            swingTimes.add(Pair(swingStartTime, swingEndTime))
        }
    }

    return swingTimes
}
fun averageSwingTime(swingTimes: List<Pair<Float, Float>>): Float {
    if (swingTimes.isEmpty()) return 0f

    var totalDuration = 0f
    for ((start, end) in swingTimes) {
        totalDuration += (end - start)
    }
    return totalDuration / swingTimes.size
}

fun calculateSwingStanceRatio(
    swingTime: Float,
    stanceTime:Float
): Float {
    // Return the ratio
    return swingTime / stanceTime
}

fun calcStrideLength(heightInches: Float) : Float
{
    var maxAngles: List<Float>
    maxAngles = FindLocalMax(strideAngles)
    var sum: Float = 0f
    for(i in maxAngles.indices)
    {
        // Estimate hip-to-heel distance as 40-45% of height
        val hipToHeelDistance = heightInches * 0.40f  // You can also use 0.45f for the upper bound

        // Convert max angle to radians
        val angleInRadians = Math.toRadians(maxAngles[i].toDouble()).toFloat()

        // Use the law of cosines to calculate the distance between the two heels (stride length)
        val strideLength = sqrt(
            hipToHeelDistance * hipToHeelDistance +
                    hipToHeelDistance * hipToHeelDistance -
                    2 * hipToHeelDistance * hipToHeelDistance * cos(angleInRadians)
        )
        sum += strideLength
    }
    return sum
}

fun calcStrideLengthAvg(heightInches: Float) : Float
{
    var maxAngles: List<Float>
    maxAngles = FindLocalMax(strideAngles)
    var sum: Float = 0f
    for(i in maxAngles.indices)
    {
        // Estimate hip-to-heel distance as 40-45% of height
        val hipToHeelDistance = heightInches * 0.40f  // You can also use 0.45f for the upper bound

        // Convert max angle to radians
        val angleInRadians = Math.toRadians(maxAngles[i].toDouble()).toFloat()

        // Use the law of cosines to calculate the distance between the two heels (stride length)
        val strideLength = sqrt(
            hipToHeelDistance * hipToHeelDistance +
                    hipToHeelDistance * hipToHeelDistance -
                    2 * hipToHeelDistance * hipToHeelDistance * cos(angleInRadians)
        )
        sum += strideLength
    }
    return sum/maxAngles.size
}