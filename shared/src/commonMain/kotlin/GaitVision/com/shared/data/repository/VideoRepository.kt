package GaitVision.com.shared.data.repository

import GaitVision.com.shared.data.database.DatabaseHelper
import GaitVision.com.shared.data.models.Video
import GaitVision.com.shared.data.database.toVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VideoRepository(private val databaseHelper: DatabaseHelper) {

    private val database get() = databaseHelper.getDatabase()

    // Create
    suspend fun insertVideo(video: Video): Long {
        database.gaitVisionDatabaseQueries.createVideo(
            patient_id = video.patientId,
            original_video_path = video.originalVideoPath,
            edited_video_path = video.editedVideoPath,
            recorded_at = video.recordedAt,
            stride_length_avg = video.strideLengthAvg,
            video_length_microseconds = video.videoLengthMicroseconds
        )
        return database.gaitVisionDatabaseQueries.getLastInsertId().executeAsOne()
    }

    suspend fun insertVideos(videos: List<Video>): List<Long> {
        return videos.map { insertVideo(it) }
    }

    // Read
    suspend fun getVideoById(videoId: Long): Video? {
        return database.gaitVisionDatabaseQueries.getVideoById(videoId)
            .executeAsOneOrNull()?.toVideo()
    }

    suspend fun getVideoByPatientAndPath(patientId: Long, originalPath: String): Video? {
        return database.gaitVisionDatabaseQueries.getVideoByPatientAndPath(patientId, originalPath)
            .executeAsOneOrNull()?.toVideo()
    }

    fun getVideosByPatientId(patientId: Long): Flow<List<Video>> {
        return database.gaitVisionDatabaseQueries.getVideosByPatientId(patientId)
            .asFlow()
            .map { it.executeAsList().map { row -> row.toVideo() } }
    }

    fun getVideosByPatientIdOrdered(patientId: Long): Flow<List<Video>> {
        return database.gaitVisionDatabaseQueries.getVideosByPatientIdOrdered(patientId)
            .asFlow()
            .map { it.executeAsList().map { row -> row.toVideo() } }
    }

    fun getAllVideos(): Flow<List<Video>> {
        return database.gaitVisionDatabaseQueries.getAllVideos()
            .asFlow()
            .map { it.executeAsList().map { row -> row.toVideo() } }
    }

    // Update
    suspend fun updateVideo(video: Video): Boolean {
        database.gaitVisionDatabaseQueries.updateVideo(
            patient_id = video.patientId,
            original_video_path = video.originalVideoPath,
            edited_video_path = video.editedVideoPath,
            recorded_at = video.recordedAt,
            stride_length_avg = video.strideLengthAvg,
            video_length_microseconds = video.videoLengthMicroseconds,
            id = video.id
        )
        return true
    }

    // Delete
    suspend fun deleteVideo(video: Video): Boolean {
        return deleteVideoById(video.id)
    }

    suspend fun deleteVideoById(videoId: Long): Boolean {
        database.gaitVisionDatabaseQueries.deleteVideo(videoId)
        return true
    }

    suspend fun deleteVideosByPatientId(patientId: Long): Boolean {
        database.gaitVisionDatabaseQueries.deleteVideosByPatientId(patientId)
        return true
    }

    // Utility
    suspend fun getVideoCount(): Int {
        return database.gaitVisionDatabaseQueries.getVideoCount()
            .executeAsOne().toInt()
    }

    suspend fun getVideoCountForPatient(patientId: Long): Int {
        return database.gaitVisionDatabaseQueries.getVideoCountForPatient(patientId)
            .executeAsOne().toInt()
    }

    // Search
    fun searchVideos(searchQuery: String): Flow<List<Video>> {
        val query = "%$searchQuery%"
        return database.gaitVisionDatabaseQueries.searchVideos(query, query, query, query)
            .asFlow()
            .map { it.executeAsList().map { row -> row.toVideo() } }
    }

    // Business logic
    suspend fun videoExists(videoId: Long): Boolean {
        return getVideoById(videoId) != null
    }

    suspend fun hasVideosForPatient(patientId: Long): Boolean {
        return getVideoCountForPatient(patientId) > 0
    }

    suspend fun getLatestVideoForPatient(patientId: Long): Video? {
        return getVideosByPatientIdOrdered(patientId).let { flow ->
            // This is a simplified approach - in production you might want to use firstOrNull on the flow
            var latestVideo: Video? = null
            flow.collect { videos ->
                latestVideo = videos.firstOrNull()
            }
            return latestVideo
        }
    }
}

