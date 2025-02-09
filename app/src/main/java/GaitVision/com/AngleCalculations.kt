package GaitVision.com

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

fun CalculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float
{
    val xDifference = x2 - x1
    val yDifference = y2 - y1
    return sqrt(xDifference.pow(2) + yDifference.pow(2))
}


fun GetAngles(x1: Float,y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Float
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
    val smallerAngle: Float = 180f - Angle
    return String.format("%.2f", smallerAngle).toFloat() //round to 2 decimal places
}

fun FindLocalMax(AngleList: MutableList<Float>): List<Float>
{
    val localMax: MutableList<Float> = mutableListOf()



    for (i in AngleList.indices) {
        //avoid using the first element and last element as current
        if (i - 1 < 0 || i + 1 >= AngleList.size) continue
        //Treating as linked list in order to traverse
        val prev = AngleList[i-1]
        val curr = AngleList[i]
        val next = AngleList[i+1]

        if(prev != null && curr != null && next != null){ //Cancel check if any null
            if(curr > prev && curr > next){
                localMax.add(curr)
            }
        }
    }
    return localMax
}

fun FindLocalMin(AngleList: MutableList<Float>): List<Float>
{
    val localMin: MutableList<Float> = mutableListOf()



    for (i in AngleList.indices) {
        //avoid using the first element and last element as current
        if (i - 1 < 0 || i + 1 >= AngleList.size) continue
        //Treating as linked list in order to traverse
        val prev = AngleList[i-1]
        val curr = AngleList[i]
        val next = AngleList[i+1]

        if(prev != null && curr != null && next != null){ //Cancel check if any null
            if(curr < prev && curr < next){
                localMin.add(curr)
            }
        }
    }
    return localMin
}
