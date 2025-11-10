package GaitVision.com.shared.data.repository

import GaitVision.com.shared.data.database.DatabaseHelper
import GaitVision.com.shared.data.models.AngleData
import GaitVision.com.shared.data.database.toAngleData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AngleDataRepository(private val databaseHelper: DatabaseHelper) {

    private val database get() = databaseHelper.getDatabase()

    // Create operations
    suspend fun insertAngleData(angleData: AngleData): Long {
        database.gaitVisionDatabaseQueries.createAngleData(
            video_id = angleData.videoId,
            frame_number = angleData.frameNumber.toLong(),
            left_ankle_angle = angleData.leftAnkleAngle,
            right_ankle_angle = angleData.rightAnkleAngle,
            left_knee_angle = angleData.leftKneeAngle,
            right_knee_angle = angleData.rightKneeAngle,
            left_hip_angle = angleData.leftHipAngle,
            right_hip_angle = angleData.rightHipAngle,
            torso_angle = angleData.torsoAngle,
            stride_angle = angleData.strideAngle
        )
        return database.gaitVisionDatabaseQueries.getLastInsertId().executeAsOne()
    }

    suspend fun insertAngleDataList(angleDataList: List<AngleData>): List<Long> {
        return angleDataList.map { insertAngleData(it) }
    }

    // Read operations
    suspend fun getAngleDataById(angleDataId: Long): AngleData? {
        return database.gaitVisionDatabaseQueries.getAngleDataById(angleDataId)
            .executeAsOneOrNull()?.toAngleData()
    }

    fun getAngleDataByVideoId(videoId: Long): Flow<List<AngleData>> {
        return database.gaitVisionDatabaseQueries.getAngleDataByVideoId(videoId)
            .asFlow()
            .map { it.executeAsList().map { row -> row.toAngleData() } }
    }

    suspend fun getAngleDataByVideoIdSync(videoId: Long): List<AngleData> {
        return database.gaitVisionDatabaseQueries.getAngleDataByVideoId(videoId)
            .executeAsList()
            .map { it.toAngleData() }
    }

    fun getAllAngleData(): Flow<List<AngleData>> {
        return database.gaitVisionDatabaseQueries.getAllAngleData()
            .asFlow()
            .map { it.executeAsList().map { row -> row.toAngleData() } }
    }

    // Get specific angle lists for a video
    suspend fun getLeftKneeAnglesByVideoId(videoId: Long): List<Float> {
        return database.gaitVisionDatabaseQueries.getLeftKneeAnglesByVideoId(videoId)
            .executeAsList()
    }

    suspend fun getRightKneeAnglesByVideoId(videoId: Long): List<Float> {
        return database.gaitVisionDatabaseQueries.getRightKneeAnglesByVideoId(videoId)
            .executeAsList()
    }

    suspend fun getLeftHipAnglesByVideoId(videoId: Long): List<Float> {
        return database.gaitVisionDatabaseQueries.getLeftHipAnglesByVideoId(videoId)
            .executeAsList()
    }

    suspend fun getRightHipAnglesByVideoId(videoId: Long): List<Float> {
        return database.gaitVisionDatabaseQueries.getRightHipAnglesByVideoId(videoId)
            .executeAsList()
    }

    suspend fun getLeftAnkleAnglesByVideoId(videoId: Long): List<Float> {
        return database.gaitVisionDatabaseQueries.getLeftAnkleAnglesByVideoId(videoId)
            .executeAsList()
    }

    suspend fun getRightAnkleAnglesByVideoId(videoId: Long): List<Float> {
        return database.gaitVisionDatabaseQueries.getRightAnkleAnglesByVideoId(videoId)
            .executeAsList()
    }

    suspend fun getTorsoAnglesByVideoId(videoId: Long): List<Float> {
        return database.gaitVisionDatabaseQueries.getTorsoAnglesByVideoId(videoId)
            .executeAsList()
    }

    suspend fun getStrideAnglesByVideoId(videoId: Long): List<Float> {
        return database.gaitVisionDatabaseQueries.getStrideAnglesByVideoId(videoId)
            .executeAsList()
    }

    // Update operations
    suspend fun updateAngleData(angleData: AngleData): Boolean {
        database.gaitVisionDatabaseQueries.updateAngleData(
            video_id = angleData.videoId,
            frame_number = angleData.frameNumber.toLong(),
            left_ankle_angle = angleData.leftAnkleAngle,
            right_ankle_angle = angleData.rightAnkleAngle,
            left_knee_angle = angleData.leftKneeAngle,
            right_knee_angle = angleData.rightKneeAngle,
            left_hip_angle = angleData.leftHipAngle,
            right_hip_angle = angleData.rightHipAngle,
            torso_angle = angleData.torsoAngle,
            stride_angle = angleData.strideAngle,
            id = angleData.id
        )
        return true
    }

    // Delete operations
    suspend fun deleteAngleData(angleData: AngleData): Boolean {
        return deleteAngleDataById(angleData.id)
    }

    suspend fun deleteAngleDataById(angleDataId: Long): Boolean {
        database.gaitVisionDatabaseQueries.deleteAngleData(angleDataId)
        return true
    }

    suspend fun deleteAngleDataByVideoId(videoId: Long): Boolean {
        database.gaitVisionDatabaseQueries.deleteAngleDataByVideoId(videoId)
        return true
    }

    // Utility operations
    suspend fun getAngleDataCount(): Int {
        return database.gaitVisionDatabaseQueries.getAngleDataCount()
            .executeAsOne().toInt()
    }

    suspend fun getAngleDataCountByVideoId(videoId: Long): Int {
        return database.gaitVisionDatabaseQueries.getAngleDataCountByVideoId(videoId)
            .executeAsOne().toInt()
    }

    suspend fun getMaxFrameNumberForVideo(videoId: Long): Int? {
        return database.gaitVisionDatabaseQueries.getMaxFrameNumberForVideo(videoId)
            .executeAsOneOrNull()?.toInt()
    }

    // Business logic operations
    suspend fun angleDataExists(angleDataId: Long): Boolean {
        return getAngleDataById(angleDataId) != null
    }

    suspend fun hasAngleDataForVideo(videoId: Long): Boolean {
        return getAngleDataCountByVideoId(videoId) > 0
    }
}

