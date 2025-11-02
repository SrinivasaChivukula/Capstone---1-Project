package GaitVision.com.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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
    val recordedAt: Long = System.currentTimeMillis()
)
