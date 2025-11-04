package GaitVision.com.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AngleDataDao {

    // Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAngleData(angleData: AngleData): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAngleDataList(angleDataList: List<AngleData>): List<Long>

    // Read
    @Query("SELECT * FROM angle_data WHERE id = :angleDataId")
    suspend fun getAngleDataById(angleDataId: Long): AngleData?

    @Query("SELECT * FROM angle_data WHERE videoId = :videoId ORDER BY frameNumber ASC")
    fun getAngleDataByVideoId(videoId: Long): Flow<List<AngleData>>

    @Query("SELECT * FROM angle_data WHERE videoId = :videoId ORDER BY frameNumber ASC")
    suspend fun getAngleDataByVideoIdSync(videoId: Long): List<AngleData>

    @Query("SELECT * FROM angle_data")
    fun getAllAngleData(): Flow<List<AngleData>>

    // Get specific angles for a video
    @Query("SELECT leftKneeAngle FROM angle_data WHERE videoId = :videoId AND leftKneeAngle IS NOT NULL ORDER BY frameNumber ASC")
    suspend fun getLeftKneeAnglesByVideoId(videoId: Long): List<Float>

    @Query("SELECT rightKneeAngle FROM angle_data WHERE videoId = :videoId AND rightKneeAngle IS NOT NULL ORDER BY frameNumber ASC")
    suspend fun getRightKneeAnglesByVideoId(videoId: Long): List<Float>

    @Query("SELECT leftHipAngle FROM angle_data WHERE videoId = :videoId AND leftHipAngle IS NOT NULL ORDER BY frameNumber ASC")
    suspend fun getLeftHipAnglesByVideoId(videoId: Long): List<Float>

    @Query("SELECT rightHipAngle FROM angle_data WHERE videoId = :videoId AND rightHipAngle IS NOT NULL ORDER BY frameNumber ASC")
    suspend fun getRightHipAnglesByVideoId(videoId: Long): List<Float>

    @Query("SELECT leftAnkleAngle FROM angle_data WHERE videoId = :videoId AND leftAnkleAngle IS NOT NULL ORDER BY frameNumber ASC")
    suspend fun getLeftAnkleAnglesByVideoId(videoId: Long): List<Float>

    @Query("SELECT rightAnkleAngle FROM angle_data WHERE videoId = :videoId AND rightAnkleAngle IS NOT NULL ORDER BY frameNumber ASC")
    suspend fun getRightAnkleAnglesByVideoId(videoId: Long): List<Float>

    @Query("SELECT torsoAngle FROM angle_data WHERE videoId = :videoId AND torsoAngle IS NOT NULL ORDER BY frameNumber ASC")
    suspend fun getTorsoAnglesByVideoId(videoId: Long): List<Float>

    @Query("SELECT strideAngle FROM angle_data WHERE videoId = :videoId AND strideAngle IS NOT NULL ORDER BY frameNumber ASC")
    suspend fun getStrideAnglesByVideoId(videoId: Long): List<Float>

    // Update
    @Update
    suspend fun updateAngleData(angleData: AngleData): Int

    // Delete
    @Delete
    suspend fun deleteAngleData(angleData: AngleData): Int

    @Query("DELETE FROM angle_data WHERE id = :angleDataId")
    suspend fun deleteAngleDataById(angleDataId: Long): Int

    @Query("DELETE FROM angle_data WHERE videoId = :videoId")
    suspend fun deleteAngleDataByVideoId(videoId: Long): Int

    // Utility
    @Query("SELECT COUNT(*) FROM angle_data")
    suspend fun getAngleDataCount(): Int

    @Query("SELECT COUNT(*) FROM angle_data WHERE videoId = :videoId")
    suspend fun getAngleDataCountByVideoId(videoId: Long): Int

    @Query("SELECT MAX(frameNumber) FROM angle_data WHERE videoId = :videoId")
    suspend fun getMaxFrameNumberForVideo(videoId: Long): Int?
}

