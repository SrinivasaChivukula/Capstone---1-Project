package GaitVision.com

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

/*
Name             : CalculateDistance
Parameters       :
    Coordinates   : 2 pairs of x,y coordinates.
Description      : Finds the length of a line using 2 x coordinates and 2 y coordinates.
Return           : Length of a line in arbitrary measurements (Only use in context of ratios)
 */
fun CalculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float
{
    val xDifference = x2 - x1
    val yDifference = y2 - y1
    return sqrt(xDifference.pow(2) + yDifference.pow(2))
}
/*
Name             : GetAngles
Parameters       :
    Coordinates  : 3 pairs of x,y coordinates.
Description      : Uses Calculate Distance to find length of all 3 sides of triangle which then uses law of cosines to find the angle
Return           : Angle altered to be within expected format of Gait-Analysis
 */
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

/*
Name             : FindLocalMax
Parameters       :
    AngleList    : Mutable list of angles
Description      : Finds anytime the graph of angles has a local max in the form x1 < x2 > x3
Return           : Mutable list composed of only local max angles
 */
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

/*
Name             : FindLocalMin
Parameters       :
    AngleList    : Mutable list of angles
Description      : Finds anytime the graph of angles has a local min in the form x1 > x2 < x3
Return           : Mutable list composed of only local min angles
 */
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

/**
 * Calculate Euclidean distance between two 2D points.
 * PC-matching version (matches pc_extraction_v2/angles.py)
 * 
 * @param x1 First point X coordinate
 * @param y1 First point Y coordinate
 * @param x2 Second point X coordinate
 * @param y2 Second point Y coordinate
 * @return Distance in same units as input coordinates
 */
fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
}

/**
 * Calculate angle at point B using law of cosines.
 * Returns the FLEXION angle (180 - full angle).
 * PC-matching version (matches pc_extraction_v2/angles.py)
 * 
 * @param x1 Point A X coordinate
 * @param y1 Point A Y coordinate
 * @param x2 Point B X coordinate (vertex of angle)
 * @param y2 Point B Y coordinate
 * @param x3 Point C X coordinate
 * @param y3 Point C Y coordinate
 * @return Flexion angle in degrees (180 - geometric angle), or NaN if invalid
 */
fun calculateAngleLawOfCosines(
    x1: Float, y1: Float,  // Point A
    x2: Float, y2: Float,  // Point B (vertex)
    x3: Float, y3: Float   // Point C
): Float {
    // Calculate lengths of triangle sides
    val lenAB = calculateDistance(x1, y1, x2, y2)  // A to B
    val lenBC = calculateDistance(x2, y2, x3, y3)  // B to C
    val lenAC = calculateDistance(x1, y1, x3, y3)  // A to C
    
    // Check for zero-length segments
    if (lenAB * lenBC == 0f) {
        return Float.NaN
    }
    
    // Law of cosines: cos(angle_B) = (AB² + BC² - AC²) / (2 * AB * BC)
    var cosAngle = (lenAB.pow(2) + lenBC.pow(2) - lenAC.pow(2)) / (2 * lenAB * lenBC)
    
    // Clamp to valid range for acos
    cosAngle = cosAngle.coerceIn(-1f, 1f)
    
    // Angle at B in degrees
    val angle = acos(cosAngle) * (180f / PI.toFloat())
    
    // Return flexion angle (180 - geometric angle)
    return 180f - angle
}
