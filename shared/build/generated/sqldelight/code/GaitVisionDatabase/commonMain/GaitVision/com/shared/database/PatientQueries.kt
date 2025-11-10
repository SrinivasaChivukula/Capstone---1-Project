package GaitVision.com.shared.database

import app.cash.sqldelight.ExecutableQuery
import app.cash.sqldelight.Query
import app.cash.sqldelight.SuspendingTransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class PatientQueries(
  driver: SqlDriver,
) : SuspendingTransacterImpl(driver) {
  public fun getLastInsertId(): ExecutableQuery<Long> = Query(-1_792_957_327, driver, "Patient.sq",
      "getLastInsertId", "SELECT last_insert_rowid()") { cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> getPatientById(id: Long, mapper: (
    id: Long,
    participant_id: String?,
    first_name: String,
    last_name: String,
    age: Long?,
    gender: String?,
    height: Long,
    created_at: Long,
  ) -> T): Query<T> = GetPatientByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1),
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4),
      cursor.getString(5),
      cursor.getLong(6)!!,
      cursor.getLong(7)!!
    )
  }

  public fun getPatientById(id: Long): Query<Patients> = getPatientById(id) { id_, participant_id,
      first_name, last_name, age, gender, height, created_at ->
    Patients(
      id_,
      participant_id,
      first_name,
      last_name,
      age,
      gender,
      height,
      created_at
    )
  }

  public fun <T : Any> getPatientByParticipantId(participant_id: String?, mapper: (
    id: Long,
    participant_id: String?,
    first_name: String,
    last_name: String,
    age: Long?,
    gender: String?,
    height: Long,
    created_at: Long,
  ) -> T): Query<T> = GetPatientByParticipantIdQuery(participant_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1),
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4),
      cursor.getString(5),
      cursor.getLong(6)!!,
      cursor.getLong(7)!!
    )
  }

  public fun getPatientByParticipantId(participant_id: String?): Query<Patients> =
      getPatientByParticipantId(participant_id) { id, participant_id_, first_name, last_name, age,
      gender, height, created_at ->
    Patients(
      id,
      participant_id_,
      first_name,
      last_name,
      age,
      gender,
      height,
      created_at
    )
  }

  public fun <T : Any> getAllPatients(mapper: (
    id: Long,
    participant_id: String?,
    first_name: String,
    last_name: String,
    age: Long?,
    gender: String?,
    height: Long,
    created_at: Long,
  ) -> T): Query<T> = Query(1_112_777_704, arrayOf("patients"), driver, "Patient.sq",
      "getAllPatients",
      "SELECT patients.id, patients.participant_id, patients.first_name, patients.last_name, patients.age, patients.gender, patients.height, patients.created_at FROM patients") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1),
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4),
      cursor.getString(5),
      cursor.getLong(6)!!,
      cursor.getLong(7)!!
    )
  }

  public fun getAllPatients(): Query<Patients> = getAllPatients { id, participant_id, first_name,
      last_name, age, gender, height, created_at ->
    Patients(
      id,
      participant_id,
      first_name,
      last_name,
      age,
      gender,
      height,
      created_at
    )
  }

  public fun <T : Any> searchPatients(
    first_name: String,
    last_name: String,
    mapper: (
      id: Long,
      participant_id: String?,
      first_name: String,
      last_name: String,
      age: Long?,
      gender: String?,
      height: Long,
      created_at: Long,
    ) -> T,
  ): Query<T> = SearchPatientsQuery(first_name, last_name) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1),
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4),
      cursor.getString(5),
      cursor.getLong(6)!!,
      cursor.getLong(7)!!
    )
  }

  public fun searchPatients(first_name: String, last_name: String): Query<Patients> =
      searchPatients(first_name, last_name) { id, participant_id, first_name_, last_name_, age,
      gender, height, created_at ->
    Patients(
      id,
      participant_id,
      first_name_,
      last_name_,
      age,
      gender,
      height,
      created_at
    )
  }

  public fun getPatientCount(): Query<Long> = Query(-1_514_786_031, arrayOf("patients"), driver,
      "Patient.sq", "getPatientCount", "SELECT COUNT(*) FROM patients") { cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> getPatientsOrderedByName(mapper: (
    id: Long,
    participant_id: String?,
    first_name: String,
    last_name: String,
    age: Long?,
    gender: String?,
    height: Long,
    created_at: Long,
  ) -> T): Query<T> = Query(-1_241_232_198, arrayOf("patients"), driver, "Patient.sq",
      "getPatientsOrderedByName",
      "SELECT patients.id, patients.participant_id, patients.first_name, patients.last_name, patients.age, patients.gender, patients.height, patients.created_at FROM patients ORDER BY last_name, first_name") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1),
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4),
      cursor.getString(5),
      cursor.getLong(6)!!,
      cursor.getLong(7)!!
    )
  }

  public fun getPatientsOrderedByName(): Query<Patients> = getPatientsOrderedByName { id,
      participant_id, first_name, last_name, age, gender, height, created_at ->
    Patients(
      id,
      participant_id,
      first_name,
      last_name,
      age,
      gender,
      height,
      created_at
    )
  }

  public suspend fun createPatient(
    participant_id: String?,
    first_name: String,
    last_name: String,
    age: Long?,
    gender: String?,
    height: Long,
    created_at: Long,
  ) {
    driver.execute(911_986_234, """
        |INSERT INTO patients (participant_id, first_name, last_name, age, gender, height, created_at)
        |VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 7) {
          bindString(0, participant_id)
          bindString(1, first_name)
          bindString(2, last_name)
          bindLong(3, age)
          bindString(4, gender)
          bindLong(5, height)
          bindLong(6, created_at)
        }.await()
    notifyQueries(911_986_234) { emit ->
      emit("patients")
    }
  }

  public suspend fun updatePatient(
    participant_id: String?,
    first_name: String,
    last_name: String,
    age: Long?,
    gender: String?,
    height: Long,
    created_at: Long,
    id: Long,
  ) {
    driver.execute(1_464_485_325, """
        |UPDATE patients
        |SET participant_id = ?,
        |    first_name = ?,
        |    last_name = ?,
        |    age = ?,
        |    gender = ?,
        |    height = ?,
        |    created_at = ?
        |WHERE id = ?
        """.trimMargin(), 8) {
          bindString(0, participant_id)
          bindString(1, first_name)
          bindString(2, last_name)
          bindLong(3, age)
          bindString(4, gender)
          bindLong(5, height)
          bindLong(6, created_at)
          bindLong(7, id)
        }.await()
    notifyQueries(1_464_485_325) { emit ->
      emit("patients")
    }
  }

  public suspend fun deletePatient(id: Long) {
    driver.execute(-1_539_303_765, """DELETE FROM patients WHERE id = ?""", 1) {
          bindLong(0, id)
        }.await()
    notifyQueries(-1_539_303_765) { emit ->
      emit("gait_scores")
      emit("patients")
      emit("videos")
    }
  }

  private inner class GetPatientByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("patients", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("patients", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(228_209_040,
        """SELECT patients.id, patients.participant_id, patients.first_name, patients.last_name, patients.age, patients.gender, patients.height, patients.created_at FROM patients WHERE id = ?""",
        mapper, 1) {
      bindLong(0, id)
    }

    override fun toString(): String = "Patient.sq:getPatientById"
  }

  private inner class GetPatientByParticipantIdQuery<out T : Any>(
    public val participant_id: String?,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("patients", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("patients", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(null,
        """SELECT patients.id, patients.participant_id, patients.first_name, patients.last_name, patients.age, patients.gender, patients.height, patients.created_at FROM patients WHERE participant_id ${ if (participant_id == null) "IS" else "=" } ? LIMIT 1""",
        mapper, 1) {
      bindString(0, participant_id)
    }

    override fun toString(): String = "Patient.sq:getPatientByParticipantId"
  }

  private inner class SearchPatientsQuery<out T : Any>(
    public val first_name: String,
    public val last_name: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("patients", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("patients", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-559_328_731,
        """SELECT patients.id, patients.participant_id, patients.first_name, patients.last_name, patients.age, patients.gender, patients.height, patients.created_at FROM patients WHERE first_name LIKE ? OR last_name LIKE ?""",
        mapper, 2) {
      bindString(0, first_name)
      bindString(1, last_name)
    }

    override fun toString(): String = "Patient.sq:searchPatients"
  }
}
