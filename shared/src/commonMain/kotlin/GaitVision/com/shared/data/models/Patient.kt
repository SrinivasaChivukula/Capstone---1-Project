package GaitVision.com.shared.data.models

/**
 * Patient data model - shared across platforms.
 * Maps to the patients table in SQLDelight.
 */
data class Patient(
    val id: Long = 0,
    val participantId: String? = null, // External participant ID for lookup
    val firstName: String = "",
    val lastName: String = "",
    val age: Int? = null,
    val gender: String? = null,
    val height: Int, // Height in inches
    val createdAt: Long = System.currentTimeMillis()
) {
    // Full name property for convenience
    val fullName: String
        get() = if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
            "$firstName $lastName".trim()
        } else {
            participantId ?: "Unknown"
        }
}

