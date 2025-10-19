package GaitVision.com.data.repository

import GaitVision.com.data.Patient
import GaitVision.com.data.PatientDao
import kotlinx.coroutines.flow.Flow

class PatientRepository(private val patientDao: PatientDao) {

    // Create
    suspend fun insertPatient(patient: Patient): Long {
        return patientDao.insertPatient(patient)
    }

    suspend fun insertPatients(patients: List<Patient>): List<Long> {
        return patientDao.insertPatients(patients)
    }

    // Read
    suspend fun getPatientById(patientId: Long): Patient? {
        return patientDao.getPatientById(patientId)
    }

    fun getAllPatients(): Flow<List<Patient>> {
        return patientDao.getAllPatients()
    }

    fun searchPatients(searchQuery: String): Flow<List<Patient>> {
        val query = "%$searchQuery%"
        return patientDao.searchPatients(query)
    }

    // Update
    suspend fun updatePatient(patient: Patient): Boolean {
        return patientDao.updatePatient(patient) > 0
    }

    // Delete
    suspend fun deletePatient(patient: Patient): Boolean {
        return patientDao.deletePatient(patient) > 0
    }

    suspend fun deletePatientById(patientId: Long): Boolean {
        return patientDao.deletePatientById(patientId) > 0
    }

    // Utility
    suspend fun getPatientCount(): Int {
        return patientDao.getPatientCount()
    }

    fun getPatientsOrderedByName(): Flow<List<Patient>> {
        return patientDao.getPatientsOrderedByName()
    }

    // logic
    suspend fun patientExists(patientId: Long): Boolean {
        return getPatientById(patientId) != null
    }

    suspend fun findPatientByName(firstName: String, lastName: String): Patient? {
        return patientDao.getAllPatients().let { flow ->
            // simplified approach
            val patients = mutableListOf<Patient>()
            flow.collect { patients.addAll(it) }
            return patients.find {
                it.firstName.equals(firstName, ignoreCase = true) &&
                it.lastName.equals(lastName, ignoreCase = true)
            }
        }
    }
}
