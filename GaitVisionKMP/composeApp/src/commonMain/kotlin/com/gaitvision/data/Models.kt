package com.gaitvision.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

import kotlinx.datetime.Clock

@Entity(
    tableName = "patients",
    indices = [Index(value = ["participantId"], unique = false)]
)
data class Patient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val participantId: String? = null, // External participant ID for lookup
    val firstName: String = "",
    val lastName: String = "",
    val age: Int? = null,
    val gender: String? = null,
    val height: Int, // Height in inches
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    // Full name property for convenience
    val fullName: String
        get() = if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
            "$firstName $lastName".trim()
        } else {
            participantId ?: "Unknown"
        }
}

@Entity(
    tableName = "videos",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Video(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val originalVideoPath: String, // Original video URI path (galleryUri)
    val editedVideoPath: String, // Processed video URI path (editedUri)
    val recordedAt: Long = Clock.System.now().toEpochMilliseconds(),
    val strideLengthAvg: Double? = null, // Average stride length in meters
    val videoLengthMicroseconds: Long? = null // Video length in microseconds
)

@Entity(
    tableName = "gait_scores",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Video::class,
            parentColumns = ["id"],
            childColumns = ["videoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GaitScore(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val videoId: Long,
    val overallScore: Double,
    val recordedAt: Long = Clock.System.now().toEpochMilliseconds(),
    // Additional gait metrics that could be useful
    val leftKneeScore: Double? = null,
    val rightKneeScore: Double? = null,
    val leftHipScore: Double? = null,
    val rightHipScore: Double? = null,
    val torsoScore: Double? = null
)
