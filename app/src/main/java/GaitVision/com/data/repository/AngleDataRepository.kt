package GaitVision.com.data.repository

import GaitVision.com.data.AngleData
import GaitVision.com.data.AngleDataDao
import kotlinx.coroutines.flow.Flow

class AngleDataRepository(private val angleDataDao: AngleDataDao) {

    // Create operations
    suspend fun insertAngleData(angleData: AngleData): Long {
        return angleDataDao.insertAngleData(angleData)
    }

    suspend fun insertAngleDataList(angleDataList: List<AngleData>): List<Long> {
        return angleDataDao.insertAngleDataList(angleDataList)
    }

    // Read operations
    suspend fun getAngleDataById(angleDataId: Long): AngleData? {
        return angleDataDao.getAngleDataById(angleDataId)
    }

    fun getAngleDataByVideoId(videoId: Long): Flow<List<AngleData>> {
        return angleDataDao.getAngleDataByVideoId(videoId)
    }

    suspend fun getAngleDataByVideoIdSync(videoId: Long): List<AngleData> {
        return angleDataDao.getAngleDataByVideoIdSync(videoId)
    }

    fun getAllAngleData(): Flow<List<AngleData>> {
        return angleDataDao.getAllAngleData()
    }

    // Get specific angle lists for a video
    suspend fun getLeftKneeAnglesByVideoId(videoId: Long): List<Float> {
        return angleDataDao.getLeftKneeAnglesByVideoId(videoId)
    }

    suspend fun getRightKneeAnglesByVideoId(videoId: Long): List<Float> {
        return angleDataDao.getRightKneeAnglesByVideoId(videoId)
    }

    suspend fun getLeftHipAnglesByVideoId(videoId: Long): List<Float> {
        return angleDataDao.getLeftHipAnglesByVideoId(videoId)
    }

    suspend fun getRightHipAnglesByVideoId(videoId: Long): List<Float> {
        return angleDataDao.getRightHipAnglesByVideoId(videoId)
    }

    suspend fun getLeftAnkleAnglesByVideoId(videoId: Long): List<Float> {
        return angleDataDao.getLeftAnkleAnglesByVideoId(videoId)
    }

    suspend fun getRightAnkleAnglesByVideoId(videoId: Long): List<Float> {
        return angleDataDao.getRightAnkleAnglesByVideoId(videoId)
    }

    suspend fun getTorsoAnglesByVideoId(videoId: Long): List<Float> {
        return angleDataDao.getTorsoAnglesByVideoId(videoId)
    }

    suspend fun getStrideAnglesByVideoId(videoId: Long): List<Float> {
        return angleDataDao.getStrideAnglesByVideoId(videoId)
    }

    // Update operations
    suspend fun updateAngleData(angleData: AngleData): Boolean {
        return angleDataDao.updateAngleData(angleData) > 0
    }

    // Delete operations
    suspend fun deleteAngleData(angleData: AngleData): Boolean {
        return angleDataDao.deleteAngleData(angleData) > 0
    }

    suspend fun deleteAngleDataById(angleDataId: Long): Boolean {
        return angleDataDao.deleteAngleDataById(angleDataId) > 0
    }

    suspend fun deleteAngleDataByVideoId(videoId: Long): Boolean {
        return angleDataDao.deleteAngleDataByVideoId(videoId) > 0
    }

    // Utility operations
    suspend fun getAngleDataCount(): Int {
        return angleDataDao.getAngleDataCount()
    }

    suspend fun getAngleDataCountByVideoId(videoId: Long): Int {
        return angleDataDao.getAngleDataCountByVideoId(videoId)
    }

    suspend fun getMaxFrameNumberForVideo(videoId: Long): Int? {
        return angleDataDao.getMaxFrameNumberForVideo(videoId)
    }

    // Business logic operations
    suspend fun angleDataExists(angleDataId: Long): Boolean {
        return getAngleDataById(angleDataId) != null
    }

    suspend fun hasAngleDataForVideo(videoId: Long): Boolean {
        return getAngleDataCountByVideoId(videoId) > 0
    }
}

