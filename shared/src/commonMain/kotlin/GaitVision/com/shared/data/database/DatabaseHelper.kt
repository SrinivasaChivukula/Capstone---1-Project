package GaitVision.com.shared.data.database

import GaitVision.com.shared.data.models.*
import GaitVision.com.shared.database.GaitVisionDatabase
// Import SQLDelight generated types (will be available after code generation)
import GaitVision.com.shared.database.Patient as PatientRow
import GaitVision.com.shared.database.Video as VideoRow
import GaitVision.com.shared.database.GaitScore as GaitScoreRow
import GaitVision.com.shared.database.AngleData as AngleDataRow

/**
 * Helper class to initialize and provide access to the database.
 */
class DatabaseHelper(private val driverFactory: DatabaseDriverFactory) {
    private val database: GaitVisionDatabase by lazy {
        GaitVisionDatabase(driverFactory.createDriver())
    }
    
    fun getDatabase(): GaitVisionDatabase = database
}

/**
 * Extension functions to convert SQLDelight row types to our data models.
 * These functions will be available once SQLDelight generates the database code.
 */

// Patient mappers
fun PatientRow.toPatient(): Patient {
    return Patient(
        id = id,
        participantId = participant_id,
        firstName = first_name,
        lastName = last_name,
        age = age?.toInt(),
        gender = gender,
        height = height.toInt(),
        createdAt = created_at
    )
}

fun Patient.toPatientRow(): PatientRow {
    return PatientRow(
        id = id,
        participant_id = participantId,
        first_name = firstName,
        last_name = lastName,
        age = age?.toLong(),
        gender = gender,
        height = height.toLong(),
        created_at = createdAt
    )
}

// Video mappers
fun VideoRow.toVideo(): Video {
    return Video(
        id = id,
        patientId = patient_id,
        originalVideoPath = original_video_path,
        editedVideoPath = edited_video_path,
        recordedAt = recorded_at,
        strideLengthAvg = stride_length_avg,
        videoLengthMicroseconds = video_length_microseconds
    )
}

fun Video.toVideoRow(): VideoRow {
    return VideoRow(
        id = id,
        patient_id = patientId,
        original_video_path = originalVideoPath,
        edited_video_path = editedVideoPath,
        recorded_at = recordedAt,
        stride_length_avg = strideLengthAvg,
        video_length_microseconds = videoLengthMicroseconds
    )
}

// GaitScore mappers
fun GaitScoreRow.toGaitScore(): GaitScore {
    return GaitScore(
        id = id,
        patientId = patient_id,
        videoId = video_id,
        overallScore = overall_score,
        recordedAt = recorded_at,
        leftKneeScore = left_knee_score,
        rightKneeScore = right_knee_score,
        leftHipScore = left_hip_score,
        rightHipScore = right_hip_score,
        torsoScore = torso_score
    )
}

fun GaitScore.toGaitScoreRow(): GaitScoreRow {
    return GaitScoreRow(
        id = id,
        patient_id = patientId,
        video_id = videoId,
        overall_score = overallScore,
        recorded_at = recordedAt,
        left_knee_score = leftKneeScore,
        right_knee_score = rightKneeScore,
        left_hip_score = leftHipScore,
        right_hip_score = rightHipScore,
        torso_score = torsoScore
    )
}

// AngleData mappers
fun AngleDataRow.toAngleData(): AngleData {
    return AngleData(
        id = id,
        videoId = video_id,
        frameNumber = frame_number.toInt(),
        leftAnkleAngle = left_ankle_angle,
        rightAnkleAngle = right_ankle_angle,
        leftKneeAngle = left_knee_angle,
        rightKneeAngle = right_knee_angle,
        leftHipAngle = left_hip_angle,
        rightHipAngle = right_hip_angle,
        torsoAngle = torso_angle,
        strideAngle = stride_angle
    )
}

fun AngleData.toAngleDataRow(): AngleDataRow {
    return AngleDataRow(
        id = id,
        video_id = videoId,
        frame_number = frameNumber.toLong(),
        left_ankle_angle = leftAnkleAngle,
        right_ankle_angle = rightAnkleAngle,
        left_knee_angle = leftKneeAngle,
        right_knee_angle = rightKneeAngle,
        left_hip_angle = leftHipAngle,
        right_hip_angle = rightHipAngle,
        torso_angle = torsoAngle,
        stride_angle = strideAngle
    )
}
