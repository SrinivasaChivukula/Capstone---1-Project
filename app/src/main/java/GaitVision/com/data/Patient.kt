package GaitVision.com.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val gender: String,
    val height: Int // Height in inches
) {
    // Full name property for convenience
    val fullName: String
        get() = "$firstName $lastName"
}
