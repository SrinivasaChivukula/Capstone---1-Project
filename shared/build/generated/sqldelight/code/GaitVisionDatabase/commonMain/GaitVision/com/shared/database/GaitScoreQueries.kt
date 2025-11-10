package GaitVision.com.shared.database

import app.cash.sqldelight.ExecutableQuery
import app.cash.sqldelight.Query
import app.cash.sqldelight.SuspendingTransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Double
import kotlin.Long
import kotlin.String

public class GaitScoreQueries(
  driver: SqlDriver,
) : SuspendingTransacterImpl(driver) {
  public fun <T : Any> getGaitScoreById(id: Long, mapper: (
    id: Long,
    patient_id: Long,
    video_id: Long,
    overall_score: Double,
    recorded_at: Long,
    left_knee_score: Double?,
    right_knee_score: Double?,
    left_hip_score: Double?,
    right_hip_score: Double?,
    torso_score: Double?,
  ) -> T): Query<T> = GetGaitScoreByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getDouble(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5),
      cursor.getDouble(6),
      cursor.getDouble(7),
      cursor.getDouble(8),
      cursor.getDouble(9)
    )
  }

  public fun getGaitScoreById(id: Long): Query<Gait_scores> = getGaitScoreById(id) { id_,
      patient_id, video_id, overall_score, recorded_at, left_knee_score, right_knee_score,
      left_hip_score, right_hip_score, torso_score ->
    Gait_scores(
      id_,
      patient_id,
      video_id,
      overall_score,
      recorded_at,
      left_knee_score,
      right_knee_score,
      left_hip_score,
      right_hip_score,
      torso_score
    )
  }

  public fun <T : Any> getGaitScoresByPatientId(patient_id: Long, mapper: (
    id: Long,
    patient_id: Long,
    video_id: Long,
    overall_score: Double,
    recorded_at: Long,
    left_knee_score: Double?,
    right_knee_score: Double?,
    left_hip_score: Double?,
    right_hip_score: Double?,
    torso_score: Double?,
  ) -> T): Query<T> = GetGaitScoresByPatientIdQuery(patient_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getDouble(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5),
      cursor.getDouble(6),
      cursor.getDouble(7),
      cursor.getDouble(8),
      cursor.getDouble(9)
    )
  }

  public fun getGaitScoresByPatientId(patient_id: Long): Query<Gait_scores> =
      getGaitScoresByPatientId(patient_id) { id, patient_id_, video_id, overall_score, recorded_at,
      left_knee_score, right_knee_score, left_hip_score, right_hip_score, torso_score ->
    Gait_scores(
      id,
      patient_id_,
      video_id,
      overall_score,
      recorded_at,
      left_knee_score,
      right_knee_score,
      left_hip_score,
      right_hip_score,
      torso_score
    )
  }

  public fun <T : Any> getGaitScoresByPatientIdOrdered(patient_id: Long, mapper: (
    id: Long,
    patient_id: Long,
    video_id: Long,
    overall_score: Double,
    recorded_at: Long,
    left_knee_score: Double?,
    right_knee_score: Double?,
    left_hip_score: Double?,
    right_hip_score: Double?,
    torso_score: Double?,
  ) -> T): Query<T> = GetGaitScoresByPatientIdOrderedQuery(patient_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getDouble(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5),
      cursor.getDouble(6),
      cursor.getDouble(7),
      cursor.getDouble(8),
      cursor.getDouble(9)
    )
  }

  public fun getGaitScoresByPatientIdOrdered(patient_id: Long): Query<Gait_scores> =
      getGaitScoresByPatientIdOrdered(patient_id) { id, patient_id_, video_id, overall_score,
      recorded_at, left_knee_score, right_knee_score, left_hip_score, right_hip_score,
      torso_score ->
    Gait_scores(
      id,
      patient_id_,
      video_id,
      overall_score,
      recorded_at,
      left_knee_score,
      right_knee_score,
      left_hip_score,
      right_hip_score,
      torso_score
    )
  }

  public fun <T : Any> getGaitScoreByVideoId(video_id: Long, mapper: (
    id: Long,
    patient_id: Long,
    video_id: Long,
    overall_score: Double,
    recorded_at: Long,
    left_knee_score: Double?,
    right_knee_score: Double?,
    left_hip_score: Double?,
    right_hip_score: Double?,
    torso_score: Double?,
  ) -> T): Query<T> = GetGaitScoreByVideoIdQuery(video_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getDouble(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5),
      cursor.getDouble(6),
      cursor.getDouble(7),
      cursor.getDouble(8),
      cursor.getDouble(9)
    )
  }

  public fun getGaitScoreByVideoId(video_id: Long): Query<Gait_scores> =
      getGaitScoreByVideoId(video_id) { id, patient_id, video_id_, overall_score, recorded_at,
      left_knee_score, right_knee_score, left_hip_score, right_hip_score, torso_score ->
    Gait_scores(
      id,
      patient_id,
      video_id_,
      overall_score,
      recorded_at,
      left_knee_score,
      right_knee_score,
      left_hip_score,
      right_hip_score,
      torso_score
    )
  }

  public fun <T : Any> getBestScoreForPatient(patient_id: Long, mapper: (
    id: Long,
    patient_id: Long,
    video_id: Long,
    overall_score: Double,
    recorded_at: Long,
    left_knee_score: Double?,
    right_knee_score: Double?,
    left_hip_score: Double?,
    right_hip_score: Double?,
    torso_score: Double?,
  ) -> T): Query<T> = GetBestScoreForPatientQuery(patient_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getDouble(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5),
      cursor.getDouble(6),
      cursor.getDouble(7),
      cursor.getDouble(8),
      cursor.getDouble(9)
    )
  }

  public fun getBestScoreForPatient(patient_id: Long): Query<Gait_scores> =
      getBestScoreForPatient(patient_id) { id, patient_id_, video_id, overall_score, recorded_at,
      left_knee_score, right_knee_score, left_hip_score, right_hip_score, torso_score ->
    Gait_scores(
      id,
      patient_id_,
      video_id,
      overall_score,
      recorded_at,
      left_knee_score,
      right_knee_score,
      left_hip_score,
      right_hip_score,
      torso_score
    )
  }

  public fun <T : Any> getWorstScoreForPatient(patient_id: Long, mapper: (
    id: Long,
    patient_id: Long,
    video_id: Long,
    overall_score: Double,
    recorded_at: Long,
    left_knee_score: Double?,
    right_knee_score: Double?,
    left_hip_score: Double?,
    right_hip_score: Double?,
    torso_score: Double?,
  ) -> T): Query<T> = GetWorstScoreForPatientQuery(patient_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getDouble(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5),
      cursor.getDouble(6),
      cursor.getDouble(7),
      cursor.getDouble(8),
      cursor.getDouble(9)
    )
  }

  public fun getWorstScoreForPatient(patient_id: Long): Query<Gait_scores> =
      getWorstScoreForPatient(patient_id) { id, patient_id_, video_id, overall_score, recorded_at,
      left_knee_score, right_knee_score, left_hip_score, right_hip_score, torso_score ->
    Gait_scores(
      id,
      patient_id_,
      video_id,
      overall_score,
      recorded_at,
      left_knee_score,
      right_knee_score,
      left_hip_score,
      right_hip_score,
      torso_score
    )
  }

  public fun <T : Any> getAverageScoreForPatient(patient_id: Long, mapper: (AVG: Double?) -> T):
      Query<T> = GetAverageScoreForPatientQuery(patient_id) { cursor ->
    mapper(
      cursor.getDouble(0)
    )
  }

  public fun getAverageScoreForPatient(patient_id: Long): Query<GetAverageScoreForPatient> =
      getAverageScoreForPatient(patient_id) { AVG ->
    GetAverageScoreForPatient(
      AVG
    )
  }

  public fun <T : Any> getAllGaitScores(mapper: (
    id: Long,
    patient_id: Long,
    video_id: Long,
    overall_score: Double,
    recorded_at: Long,
    left_knee_score: Double?,
    right_knee_score: Double?,
    left_hip_score: Double?,
    right_hip_score: Double?,
    torso_score: Double?,
  ) -> T): Query<T> = Query(1_368_734_568, arrayOf("gait_scores"), driver, "GaitScore.sq",
      "getAllGaitScores",
      "SELECT gait_scores.id, gait_scores.patient_id, gait_scores.video_id, gait_scores.overall_score, gait_scores.recorded_at, gait_scores.left_knee_score, gait_scores.right_knee_score, gait_scores.left_hip_score, gait_scores.right_hip_score, gait_scores.torso_score FROM gait_scores") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getDouble(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5),
      cursor.getDouble(6),
      cursor.getDouble(7),
      cursor.getDouble(8),
      cursor.getDouble(9)
    )
  }

  public fun getAllGaitScores(): Query<Gait_scores> = getAllGaitScores { id, patient_id, video_id,
      overall_score, recorded_at, left_knee_score, right_knee_score, left_hip_score,
      right_hip_score, torso_score ->
    Gait_scores(
      id,
      patient_id,
      video_id,
      overall_score,
      recorded_at,
      left_knee_score,
      right_knee_score,
      left_hip_score,
      right_hip_score,
      torso_score
    )
  }

  public fun getLastInsertId(): ExecutableQuery<Long> = Query(-1_483_228_311, driver,
      "GaitScore.sq", "getLastInsertId", "SELECT last_insert_rowid()") { cursor ->
    cursor.getLong(0)!!
  }

  public fun getGaitScoreCount(): Query<Long> = Query(-1_919_211_455, arrayOf("gait_scores"),
      driver, "GaitScore.sq", "getGaitScoreCount", "SELECT COUNT(*) FROM gait_scores") { cursor ->
    cursor.getLong(0)!!
  }

  public fun getGaitScoreCountForPatient(patient_id: Long): Query<Long> =
      GetGaitScoreCountForPatientQuery(patient_id) { cursor ->
    cursor.getLong(0)!!
  }

  public fun getGaitScoreCountForVideo(video_id: Long): Query<Long> =
      GetGaitScoreCountForVideoQuery(video_id) { cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> getGaitScoresWithPatientInfo(patient_id: Long, mapper: (
    id: Long,
    patient_id: Long,
    video_id: Long,
    overall_score: Double,
    recorded_at: Long,
    left_knee_score: Double?,
    right_knee_score: Double?,
    left_hip_score: Double?,
    right_hip_score: Double?,
    torso_score: Double?,
  ) -> T): Query<T> = GetGaitScoresWithPatientInfoQuery(patient_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getDouble(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5),
      cursor.getDouble(6),
      cursor.getDouble(7),
      cursor.getDouble(8),
      cursor.getDouble(9)
    )
  }

  public fun getGaitScoresWithPatientInfo(patient_id: Long): Query<Gait_scores> =
      getGaitScoresWithPatientInfo(patient_id) { id, patient_id_, video_id, overall_score,
      recorded_at, left_knee_score, right_knee_score, left_hip_score, right_hip_score,
      torso_score ->
    Gait_scores(
      id,
      patient_id_,
      video_id,
      overall_score,
      recorded_at,
      left_knee_score,
      right_knee_score,
      left_hip_score,
      right_hip_score,
      torso_score
    )
  }

  public fun <T : Any> searchGaitScores(
    first_name: String,
    last_name: String,
    mapper: (
      id: Long,
      patient_id: Long,
      video_id: Long,
      overall_score: Double,
      recorded_at: Long,
      left_knee_score: Double?,
      right_knee_score: Double?,
      left_hip_score: Double?,
      right_hip_score: Double?,
      torso_score: Double?,
    ) -> T,
  ): Query<T> = SearchGaitScoresQuery(first_name, last_name) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getDouble(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5),
      cursor.getDouble(6),
      cursor.getDouble(7),
      cursor.getDouble(8),
      cursor.getDouble(9)
    )
  }

  public fun searchGaitScores(first_name: String, last_name: String): Query<Gait_scores> =
      searchGaitScores(first_name, last_name) { id, patient_id, video_id, overall_score,
      recorded_at, left_knee_score, right_knee_score, left_hip_score, right_hip_score,
      torso_score ->
    Gait_scores(
      id,
      patient_id,
      video_id,
      overall_score,
      recorded_at,
      left_knee_score,
      right_knee_score,
      left_hip_score,
      right_hip_score,
      torso_score
    )
  }

  public suspend fun createGaitScore(
    patient_id: Long,
    video_id: Long,
    overall_score: Double,
    recorded_at: Long,
    left_knee_score: Double?,
    right_knee_score: Double?,
    left_hip_score: Double?,
    right_hip_score: Double?,
    torso_score: Double?,
  ) {
    driver.execute(-1_232_244_998, """
        |INSERT INTO gait_scores (patient_id, video_id, overall_score, recorded_at, left_knee_score, right_knee_score, left_hip_score, right_hip_score, torso_score)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 9) {
          bindLong(0, patient_id)
          bindLong(1, video_id)
          bindDouble(2, overall_score)
          bindLong(3, recorded_at)
          bindDouble(4, left_knee_score)
          bindDouble(5, right_knee_score)
          bindDouble(6, left_hip_score)
          bindDouble(7, right_hip_score)
          bindDouble(8, torso_score)
        }.await()
    notifyQueries(-1_232_244_998) { emit ->
      emit("gait_scores")
    }
  }

  public suspend fun updateGaitScore(
    patient_id: Long,
    video_id: Long,
    overall_score: Double,
    recorded_at: Long,
    left_knee_score: Double?,
    right_knee_score: Double?,
    left_hip_score: Double?,
    right_hip_score: Double?,
    torso_score: Double?,
    id: Long,
  ) {
    driver.execute(1_438_404_045, """
        |UPDATE gait_scores
        |SET patient_id = ?,
        |    video_id = ?,
        |    overall_score = ?,
        |    recorded_at = ?,
        |    left_knee_score = ?,
        |    right_knee_score = ?,
        |    left_hip_score = ?,
        |    right_hip_score = ?,
        |    torso_score = ?
        |WHERE id = ?
        """.trimMargin(), 10) {
          bindLong(0, patient_id)
          bindLong(1, video_id)
          bindDouble(2, overall_score)
          bindLong(3, recorded_at)
          bindDouble(4, left_knee_score)
          bindDouble(5, right_knee_score)
          bindDouble(6, left_hip_score)
          bindDouble(7, right_hip_score)
          bindDouble(8, torso_score)
          bindLong(9, id)
        }.await()
    notifyQueries(1_438_404_045) { emit ->
      emit("gait_scores")
    }
  }

  public suspend fun deleteGaitScore(id: Long) {
    driver.execute(1_015_111_467, """DELETE FROM gait_scores WHERE id = ?""", 1) {
          bindLong(0, id)
        }.await()
    notifyQueries(1_015_111_467) { emit ->
      emit("gait_scores")
    }
  }

  public suspend fun deleteGaitScoresByPatientId(patient_id: Long) {
    driver.execute(-471_976_799, """DELETE FROM gait_scores WHERE patient_id = ?""", 1) {
          bindLong(0, patient_id)
        }.await()
    notifyQueries(-471_976_799) { emit ->
      emit("gait_scores")
    }
  }

  private inner class GetGaitScoreByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("gait_scores", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("gait_scores", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_739_183_712,
        """SELECT gait_scores.id, gait_scores.patient_id, gait_scores.video_id, gait_scores.overall_score, gait_scores.recorded_at, gait_scores.left_knee_score, gait_scores.right_knee_score, gait_scores.left_hip_score, gait_scores.right_hip_score, gait_scores.torso_score FROM gait_scores WHERE id = ?""",
        mapper, 1) {
      bindLong(0, id)
    }

    override fun toString(): String = "GaitScore.sq:getGaitScoreById"
  }

  private inner class GetGaitScoresByPatientIdQuery<out T : Any>(
    public val patient_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("gait_scores", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("gait_scores", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_056_765_540,
        """SELECT gait_scores.id, gait_scores.patient_id, gait_scores.video_id, gait_scores.overall_score, gait_scores.recorded_at, gait_scores.left_knee_score, gait_scores.right_knee_score, gait_scores.left_hip_score, gait_scores.right_hip_score, gait_scores.torso_score FROM gait_scores WHERE patient_id = ?""",
        mapper, 1) {
      bindLong(0, patient_id)
    }

    override fun toString(): String = "GaitScore.sq:getGaitScoresByPatientId"
  }

  private inner class GetGaitScoresByPatientIdOrderedQuery<out T : Any>(
    public val patient_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("gait_scores", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("gait_scores", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_037_918_569,
        """SELECT gait_scores.id, gait_scores.patient_id, gait_scores.video_id, gait_scores.overall_score, gait_scores.recorded_at, gait_scores.left_knee_score, gait_scores.right_knee_score, gait_scores.left_hip_score, gait_scores.right_hip_score, gait_scores.torso_score FROM gait_scores WHERE patient_id = ? ORDER BY recorded_at DESC""",
        mapper, 1) {
      bindLong(0, patient_id)
    }

    override fun toString(): String = "GaitScore.sq:getGaitScoresByPatientIdOrdered"
  }

  private inner class GetGaitScoreByVideoIdQuery<out T : Any>(
    public val video_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("gait_scores", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("gait_scores", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-2_000_214_831,
        """SELECT gait_scores.id, gait_scores.patient_id, gait_scores.video_id, gait_scores.overall_score, gait_scores.recorded_at, gait_scores.left_knee_score, gait_scores.right_knee_score, gait_scores.left_hip_score, gait_scores.right_hip_score, gait_scores.torso_score FROM gait_scores WHERE video_id = ?""",
        mapper, 1) {
      bindLong(0, video_id)
    }

    override fun toString(): String = "GaitScore.sq:getGaitScoreByVideoId"
  }

  private inner class GetBestScoreForPatientQuery<out T : Any>(
    public val patient_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("gait_scores", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("gait_scores", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-117_123_637,
        """SELECT gait_scores.id, gait_scores.patient_id, gait_scores.video_id, gait_scores.overall_score, gait_scores.recorded_at, gait_scores.left_knee_score, gait_scores.right_knee_score, gait_scores.left_hip_score, gait_scores.right_hip_score, gait_scores.torso_score FROM gait_scores WHERE patient_id = ? ORDER BY overall_score DESC LIMIT 1""",
        mapper, 1) {
      bindLong(0, patient_id)
    }

    override fun toString(): String = "GaitScore.sq:getBestScoreForPatient"
  }

  private inner class GetWorstScoreForPatientQuery<out T : Any>(
    public val patient_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("gait_scores", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("gait_scores", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-412_729_806,
        """SELECT gait_scores.id, gait_scores.patient_id, gait_scores.video_id, gait_scores.overall_score, gait_scores.recorded_at, gait_scores.left_knee_score, gait_scores.right_knee_score, gait_scores.left_hip_score, gait_scores.right_hip_score, gait_scores.torso_score FROM gait_scores WHERE patient_id = ? ORDER BY overall_score ASC LIMIT 1""",
        mapper, 1) {
      bindLong(0, patient_id)
    }

    override fun toString(): String = "GaitScore.sq:getWorstScoreForPatient"
  }

  private inner class GetAverageScoreForPatientQuery<out T : Any>(
    public val patient_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("gait_scores", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("gait_scores", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(2_069_119_120,
        """SELECT AVG(overall_score) FROM gait_scores WHERE patient_id = ?""", mapper, 1) {
      bindLong(0, patient_id)
    }

    override fun toString(): String = "GaitScore.sq:getAverageScoreForPatient"
  }

  private inner class GetGaitScoreCountForPatientQuery<out T : Any>(
    public val patient_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("gait_scores", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("gait_scores", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_645_340_387,
        """SELECT COUNT(*) FROM gait_scores WHERE patient_id = ?""", mapper, 1) {
      bindLong(0, patient_id)
    }

    override fun toString(): String = "GaitScore.sq:getGaitScoreCountForPatient"
  }

  private inner class GetGaitScoreCountForVideoQuery<out T : Any>(
    public val video_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("gait_scores", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("gait_scores", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(942_598_291, """SELECT COUNT(*) FROM gait_scores WHERE video_id = ?""",
        mapper, 1) {
      bindLong(0, video_id)
    }

    override fun toString(): String = "GaitScore.sq:getGaitScoreCountForVideo"
  }

  private inner class GetGaitScoresWithPatientInfoQuery<out T : Any>(
    public val patient_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("gait_scores", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("gait_scores", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(793_824_552, """
    |SELECT gait_scores.id, gait_scores.patient_id, gait_scores.video_id, gait_scores.overall_score, gait_scores.recorded_at, gait_scores.left_knee_score, gait_scores.right_knee_score, gait_scores.left_hip_score, gait_scores.right_hip_score, gait_scores.torso_score FROM gait_scores
    |WHERE patient_id = ?
    |ORDER BY recorded_at DESC
    """.trimMargin(), mapper, 1) {
      bindLong(0, patient_id)
    }

    override fun toString(): String = "GaitScore.sq:getGaitScoresWithPatientInfo"
  }

  private inner class SearchGaitScoresQuery<out T : Any>(
    public val first_name: String,
    public val last_name: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("gait_scores", "patients", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("gait_scores", "patients", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(792_219_237, """
    |SELECT gs.id, gs.patient_id, gs.video_id, gs.overall_score, gs.recorded_at, gs.left_knee_score, gs.right_knee_score, gs.left_hip_score, gs.right_hip_score, gs.torso_score FROM gait_scores gs
    |INNER JOIN patients p ON gs.patient_id = p.id
    |WHERE p.first_name LIKE ? OR p.last_name LIKE ?
    |ORDER BY gs.recorded_at DESC
    """.trimMargin(), mapper, 2) {
      bindString(0, first_name)
      bindString(1, last_name)
    }

    override fun toString(): String = "GaitScore.sq:searchGaitScores"
  }
}
