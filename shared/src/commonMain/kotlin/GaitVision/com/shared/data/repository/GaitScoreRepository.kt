package GaitVision.com.shared.data.repository

import GaitVision.com.shared.data.database.DatabaseHelper
import GaitVision.com.shared.data.models.GaitScore
import GaitVision.com.shared.data.database.toGaitScore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GaitScoreRepository(private val databaseHelper: DatabaseHelper) {

    private val database get() = databaseHelper.getDatabase()

    // Create operations
    suspend fun insertGaitScore(gaitScore: GaitScore): Long {
        database.gaitVisionDatabaseQueries.createGaitScore(
            patient_id = gaitScore.patientId,
            video_id = gaitScore.videoId,
            overall_score = gaitScore.overallScore,
            recorded_at = gaitScore.recordedAt,
            left_knee_score = gaitScore.leftKneeScore,
            right_knee_score = gaitScore.rightKneeScore,
            left_hip_score = gaitScore.leftHipScore,
            right_hip_score = gaitScore.rightHipScore,
            torso_score = gaitScore.torsoScore
        )
        return database.gaitVisionDatabaseQueries.getLastInsertId().executeAsOne()
    }

    suspend fun insertGaitScores(gaitScores: List<GaitScore>): List<Long> {
        return gaitScores.map { insertGaitScore(it) }
    }

    // Read operations
    suspend fun getGaitScoreById(scoreId: Long): GaitScore? {
        return database.gaitVisionDatabaseQueries.getGaitScoreById(scoreId)
            .executeAsOneOrNull()?.toGaitScore()
    }

    fun getGaitScoresByPatientId(patientId: Long): Flow<List<GaitScore>> {
        return database.gaitVisionDatabaseQueries.getGaitScoresByPatientId(patientId)
            .asFlow()
            .map { it.executeAsList().map { row -> row.toGaitScore() } }
    }

    fun getGaitScoresByPatientIdOrdered(patientId: Long): Flow<List<GaitScore>> {
        return database.gaitVisionDatabaseQueries.getGaitScoresByPatientIdOrdered(patientId)
            .asFlow()
            .map { it.executeAsList().map { row -> row.toGaitScore() } }
    }

    suspend fun getGaitScoreByVideoId(videoId: Long): GaitScore? {
        return database.gaitVisionDatabaseQueries.getGaitScoreByVideoId(videoId)
            .executeAsOneOrNull()?.toGaitScore()
    }

    suspend fun getBestScoreForPatient(patientId: Long): GaitScore? {
        return database.gaitVisionDatabaseQueries.getBestScoreForPatient(patientId)
            .executeAsOneOrNull()?.toGaitScore()
    }

    suspend fun getWorstScoreForPatient(patientId: Long): GaitScore? {
        return database.gaitVisionDatabaseQueries.getWorstScoreForPatient(patientId)
            .executeAsOneOrNull()?.toGaitScore()
    }

    suspend fun getAverageScoreForPatient(patientId: Long): Double? {
        return database.gaitVisionDatabaseQueries.getAverageScoreForPatient(patientId)
            .executeAsOneOrNull()
    }

    fun getAllGaitScores(): Flow<List<GaitScore>> {
        return database.gaitVisionDatabaseQueries.getAllGaitScores()
            .asFlow()
            .map { it.executeAsList().map { row -> row.toGaitScore() } }
    }

    // Update operations
    suspend fun updateGaitScore(gaitScore: GaitScore): Boolean {
        database.gaitVisionDatabaseQueries.updateGaitScore(
            patient_id = gaitScore.patientId,
            video_id = gaitScore.videoId,
            overall_score = gaitScore.overallScore,
            recorded_at = gaitScore.recordedAt,
            left_knee_score = gaitScore.leftKneeScore,
            right_knee_score = gaitScore.rightKneeScore,
            left_hip_score = gaitScore.leftHipScore,
            right_hip_score = gaitScore.rightHipScore,
            torso_score = gaitScore.torsoScore,
            id = gaitScore.id
        )
        return true
    }

    // Delete operations
    suspend fun deleteGaitScore(gaitScore: GaitScore): Boolean {
        return deleteGaitScoreById(gaitScore.id)
    }

    suspend fun deleteGaitScoreById(scoreId: Long): Boolean {
        database.gaitVisionDatabaseQueries.deleteGaitScore(scoreId)
        return true
    }

    suspend fun deleteGaitScoresByPatientId(patientId: Long): Boolean {
        database.gaitVisionDatabaseQueries.deleteGaitScoresByPatientId(patientId)
        return true
    }

    // Utility operations
    suspend fun getGaitScoreCount(): Int {
        return database.gaitVisionDatabaseQueries.getGaitScoreCount()
            .executeAsOne().toInt()
    }

    suspend fun getGaitScoreCountForPatient(patientId: Long): Int {
        return database.gaitVisionDatabaseQueries.getGaitScoreCountForPatient(patientId)
            .executeAsOne().toInt()
    }

    suspend fun getGaitScoreCountForVideo(videoId: Long): Int {
        return database.gaitVisionDatabaseQueries.getGaitScoreCountForVideo(videoId)
            .executeAsOne().toInt()
    }

    // Search operations
    fun searchGaitScores(searchQuery: String): Flow<List<GaitScore>> {
        val query = "%$searchQuery%"
        return database.gaitVisionDatabaseQueries.searchGaitScores(query, query)
            .asFlow()
            .map { it.executeAsList().map { row -> row.toGaitScore() } }
    }

    // Business logic operations
    suspend fun gaitScoreExists(scoreId: Long): Boolean {
        return getGaitScoreById(scoreId) != null
    }

    suspend fun hasScoresForPatient(patientId: Long): Boolean {
        return getGaitScoreCountForPatient(patientId) > 0
    }

    suspend fun hasScoreForVideo(videoId: Long): Boolean {
        return getGaitScoreCountForVideo(videoId) > 0
    }
}

