package GaitVision.com.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "angle_data",
    foreignKeys = [ForeignKey(
        entity = Video::class,
        parentColumns = ["id"],
        childColumns = ["videoId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class AngleData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val videoId: Long,
    val frameNumber: Int,
    // Angle measurements for each frame
    val leftAnkleAngle: Float? = null,
    val rightAnkleAngle: Float? = null,
    val leftKneeAngle: Float? = null,
    val rightKneeAngle: Float? = null,
    val leftHipAngle: Float? = null,
    val rightHipAngle: Float? = null,
    val torsoAngle: Float? = null,
    val strideAngle: Float? = null
)

