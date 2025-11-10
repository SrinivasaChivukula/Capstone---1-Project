package GaitVision.com.shared.data.repository

import GaitVision.com.shared.data.database.DatabaseHelper
import GaitVision.com.shared.data.models.Patient
import GaitVision.com.shared.data.database.toPatient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PatientRepository(private val databaseHelper: DatabaseHelper) {

    private val database get() = databaseHelper.getDatabase()

    // Create
    suspend fun insertPatient(patient: Patient): Long {
        database.gaitVisionDatabaseQueries.createPatient(
            participant_id = patient.participantId,
            first_name = patient.firstName,
            last_name = patient.lastName,
            age = patient.age?.toLong(),
            gender = patient.gender,
            height = patient.height.toLong(),
            created_at = patient.createdAt
        )
        return database.gaitVisionDatabaseQueries.getLastInsertId().executeAsOne()
    }

    suspend fun insertPatients(patients: List<Patient>): List<Long> {
        return patients.map { insertPatient(it) }
    }

    // Read
    suspend fun getPatientById(patientId: Long): Patient? {
        return database.gaitVisionDatabaseQueries.getPatientById(patientId)
            .executeAsOneOrNull()?.toPatient()
    }

    fun getAllPatients(): Flow<List<Patient>> {
        return database.gaitVisionDatabaseQueries.getAllPatients()
            .asFlow()
            .map { it.executeAsList().map { row -> row.toPatient() } }
    }

    fun searchPatients(searchQuery: String): Flow<List<Patient>> {
        val query = "%$searchQuery%"
        return database.gaitVisionDatabaseQueries.searchPatients(query, query)
            .asFlow()
            .map { it.executeAsList().map { row -> row.toPatient() } }
    }

    suspend fun getPatientByParticipantId(participantId: String): Patient? {
        return database.gaitVisionDatabaseQueries.getPatientByParticipantId(participantId)
            .executeAsOneOrNull()?.toPatient()
    }

    // Update
    suspend fun updatePatient(patient: Patient): Boolean {
        database.gaitVisionDatabaseQueries.updatePatient(
            participant_id = patient.participantId,
            first_name = patient.firstName,
            last_name = patient.lastName,
            age = patient.age?.toLong(),
            gender = patient.gender,
            height = patient.height.toLong(),
            created_at = patient.createdAt,
            id = patient.id
        )
        return true
    }

    // Delete
    suspend fun deletePatient(patient: Patient): Boolean {
        return deletePatientById(patient.id)
    }

    suspend fun deletePatientById(patientId: Long): Boolean {
        database.gaitVisionDatabaseQueries.deletePatient(patientId)
        return true
    }

    // Utility
    suspend fun getPatientCount(): Int {
        return database.gaitVisionDatabaseQueries.getPatientCount()
            .executeAsOne().toInt()
    }

    fun getPatientsOrderedByName(): Flow<List<Patient>> {
        return database.gaitVisionDatabaseQueries.getPatientsOrderedByName()
            .asFlow()
            .map { it.executeAsList().map { row -> row.toPatient() } }
    }

    // Business logic
    suspend fun patientExists(patientId: Long): Boolean {
        return getPatientById(patientId) != null
    }

    suspend fun findOrCreatePatientByParticipantId(
        participantId: String,
        height: Int,
        firstName: String = "",
        lastName: String = "",
        age: Int? = null,
        gender: String? = null
    ): Patient {
        val existing = getPatientByParticipantId(participantId)
        return if (existing != null) {
            // Update height if provided and different
            if (existing.height != height) {
                val updated = existing.copy(height = height)
                updatePatient(updated)
                updated
            } else {
                existing
            }
        } else {
            // Create new patient
            val newPatient = Patient(
                participantId = participantId,
                firstName = firstName,
                lastName = lastName,
                age = age,
                gender = gender,
                height = height,
                createdAt = System.currentTimeMillis()
            )
            val id = insertPatient(newPatient)
            newPatient.copy(id = id)
        }
    }
}

