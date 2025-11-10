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

public class VideoQueries(
  driver: SqlDriver,
) : SuspendingTransacterImpl(driver) {
  public fun <T : Any> getVideoById(id: Long, mapper: (
    id: Long,
    patient_id: Long,
    original_video_path: String,
    edited_video_path: String,
    recorded_at: Long,
    stride_length_avg: Double?,
    video_length_microseconds: Long?,
  ) -> T): Query<T> = GetVideoByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5),
      cursor.getLong(6)
    )
  }

  public fun getVideoById(id: Long): Query<Videos> = getVideoById(id) { id_, patient_id,
      original_video_path, edited_video_path, recorded_at, stride_length_avg,
      video_length_microseconds ->
    Videos(
      id_,
      patient_id,
      original_video_path,
      edited_video_path,
      recorded_at,
      stride_length_avg,
      video_length_microseconds
    )
  }

  public fun <T : Any> getVideoByPatientAndPath(
    patient_id: Long,
    original_video_path: String,
    mapper: (
      id: Long,
      patient_id: Long,
      original_video_path: String,
      edited_video_path: String,
      recorded_at: Long,
      stride_length_avg: Double?,
      video_length_microseconds: Long?,
    ) -> T,
  ): Query<T> = GetVideoByPatientAndPathQuery(patient_id, original_video_path) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5),
      cursor.getLong(6)
    )
  }

  public fun getVideoByPatientAndPath(patient_id: Long, original_video_path: String): Query<Videos>
      = getVideoByPatientAndPath(patient_id, original_video_path) { id, patient_id_,
      original_video_path_, edited_video_path, recorded_at, stride_length_avg,
      video_length_microseconds ->
    Videos(
      id,
      patient_id_,
      original_video_path_,
      edited_video_path,
      recorded_at,
      stride_length_avg,
      video_length_microseconds
    )
  }

  public fun <T : Any> getVideosByPatientId(patient_id: Long, mapper: (
    id: Long,
    patient_id: Long,
    original_video_path: String,
    edited_video_path: String,
    recorded_at: Long,
    stride_length_avg: Double?,
    video_length_microseconds: Long?,
  ) -> T): Query<T> = GetVideosByPatientIdQuery(patient_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5),
      cursor.getLong(6)
    )
  }

  public fun getVideosByPatientId(patient_id: Long): Query<Videos> =
      getVideosByPatientId(patient_id) { id, patient_id_, original_video_path, edited_video_path,
      recorded_at, stride_length_avg, video_length_microseconds ->
    Videos(
      id,
      patient_id_,
      original_video_path,
      edited_video_path,
      recorded_at,
      stride_length_avg,
      video_length_microseconds
    )
  }

  public fun <T : Any> getVideosByPatientIdOrdered(patient_id: Long, mapper: (
    id: Long,
    patient_id: Long,
    original_video_path: String,
    edited_video_path: String,
    recorded_at: Long,
    stride_length_avg: Double?,
    video_length_microseconds: Long?,
  ) -> T): Query<T> = GetVideosByPatientIdOrderedQuery(patient_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5),
      cursor.getLong(6)
    )
  }

  public fun getVideosByPatientIdOrdered(patient_id: Long): Query<Videos> =
      getVideosByPatientIdOrdered(patient_id) { id, patient_id_, original_video_path,
      edited_video_path, recorded_at, stride_length_avg, video_length_microseconds ->
    Videos(
      id,
      patient_id_,
      original_video_path,
      edited_video_path,
      recorded_at,
      stride_length_avg,
      video_length_microseconds
    )
  }

  public fun <T : Any> getAllVideos(mapper: (
    id: Long,
    patient_id: Long,
    original_video_path: String,
    edited_video_path: String,
    recorded_at: Long,
    stride_length_avg: Double?,
    video_length_microseconds: Long?,
  ) -> T): Query<T> = Query(603_257_896, arrayOf("videos"), driver, "Video.sq", "getAllVideos",
      "SELECT videos.id, videos.patient_id, videos.original_video_path, videos.edited_video_path, videos.recorded_at, videos.stride_length_avg, videos.video_length_microseconds FROM videos") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5),
      cursor.getLong(6)
    )
  }

  public fun getAllVideos(): Query<Videos> = getAllVideos { id, patient_id, original_video_path,
      edited_video_path, recorded_at, stride_length_avg, video_length_microseconds ->
    Videos(
      id,
      patient_id,
      original_video_path,
      edited_video_path,
      recorded_at,
      stride_length_avg,
      video_length_microseconds
    )
  }

  public fun getLastInsertId(): ExecutableQuery<Long> = Query(-2_093_865_925, driver, "Video.sq",
      "getLastInsertId", "SELECT last_insert_rowid()") { cursor ->
    cursor.getLong(0)!!
  }

  public fun getVideoCount(): Query<Long> = Query(-1_966_587_035, arrayOf("videos"), driver,
      "Video.sq", "getVideoCount", "SELECT COUNT(*) FROM videos") { cursor ->
    cursor.getLong(0)!!
  }

  public fun getVideoCountForPatient(patient_id: Long): Query<Long> =
      GetVideoCountForPatientQuery(patient_id) { cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> searchVideos(
    first_name: String,
    last_name: String,
    original_video_path: String,
    edited_video_path: String,
    mapper: (
      id: Long,
      patient_id: Long,
      original_video_path: String,
      edited_video_path: String,
      recorded_at: Long,
      stride_length_avg: Double?,
      video_length_microseconds: Long?,
    ) -> T,
  ): Query<T> = SearchVideosQuery(first_name, last_name, original_video_path, edited_video_path) {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getLong(4)!!,
      cursor.getDouble(5),
      cursor.getLong(6)
    )
  }

  public fun searchVideos(
    first_name: String,
    last_name: String,
    original_video_path: String,
    edited_video_path: String,
  ): Query<Videos> = searchVideos(first_name, last_name, original_video_path, edited_video_path) {
      id, patient_id, original_video_path_, edited_video_path_, recorded_at, stride_length_avg,
      video_length_microseconds ->
    Videos(
      id,
      patient_id,
      original_video_path_,
      edited_video_path_,
      recorded_at,
      stride_length_avg,
      video_length_microseconds
    )
  }

  public suspend fun createVideo(
    patient_id: Long,
    original_video_path: String,
    edited_video_path: String,
    recorded_at: Long,
    stride_length_avg: Double?,
    video_length_microseconds: Long?,
  ) {
    driver.execute(-1_579_338_310, """
        |INSERT INTO videos (patient_id, original_video_path, edited_video_path, recorded_at, stride_length_avg, video_length_microseconds)
        |VALUES (?, ?, ?, ?, ?, ?)
        """.trimMargin(), 6) {
          bindLong(0, patient_id)
          bindString(1, original_video_path)
          bindString(2, edited_video_path)
          bindLong(3, recorded_at)
          bindDouble(4, stride_length_avg)
          bindLong(5, video_length_microseconds)
        }.await()
    notifyQueries(-1_579_338_310) { emit ->
      emit("videos")
    }
  }

  public suspend fun updateVideo(
    patient_id: Long,
    original_video_path: String,
    edited_video_path: String,
    recorded_at: Long,
    stride_length_avg: Double?,
    video_length_microseconds: Long?,
    id: Long,
  ) {
    driver.execute(-1_659_210_227, """
        |UPDATE videos
        |SET patient_id = ?,
        |    original_video_path = ?,
        |    edited_video_path = ?,
        |    recorded_at = ?,
        |    stride_length_avg = ?,
        |    video_length_microseconds = ?
        |WHERE id = ?
        """.trimMargin(), 7) {
          bindLong(0, patient_id)
          bindString(1, original_video_path)
          bindString(2, edited_video_path)
          bindLong(3, recorded_at)
          bindDouble(4, stride_length_avg)
          bindLong(5, video_length_microseconds)
          bindLong(6, id)
        }.await()
    notifyQueries(-1_659_210_227) { emit ->
      emit("videos")
    }
  }

  public suspend fun deleteVideo(id: Long) {
    driver.execute(2_087_380_587, """DELETE FROM videos WHERE id = ?""", 1) {
          bindLong(0, id)
        }.await()
    notifyQueries(2_087_380_587) { emit ->
      emit("angle_data")
      emit("gait_scores")
      emit("videos")
    }
  }

  public suspend fun deleteVideosByPatientId(patient_id: Long) {
    driver.execute(1_198_971_873, """DELETE FROM videos WHERE patient_id = ?""", 1) {
          bindLong(0, patient_id)
        }.await()
    notifyQueries(1_198_971_873) { emit ->
      emit("angle_data")
      emit("gait_scores")
      emit("videos")
    }
  }

  private inner class GetVideoByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("videos", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("videos", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-2_003_122_500,
        """SELECT videos.id, videos.patient_id, videos.original_video_path, videos.edited_video_path, videos.recorded_at, videos.stride_length_avg, videos.video_length_microseconds FROM videos WHERE id = ?""",
        mapper, 1) {
      bindLong(0, id)
    }

    override fun toString(): String = "Video.sq:getVideoById"
  }

  private inner class GetVideoByPatientAndPathQuery<out T : Any>(
    public val patient_id: Long,
    public val original_video_path: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("videos", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("videos", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(954_797_048,
        """SELECT videos.id, videos.patient_id, videos.original_video_path, videos.edited_video_path, videos.recorded_at, videos.stride_length_avg, videos.video_length_microseconds FROM videos WHERE patient_id = ? AND original_video_path = ? LIMIT 1""",
        mapper, 2) {
      bindLong(0, patient_id)
      bindString(1, original_video_path)
    }

    override fun toString(): String = "Video.sq:getVideoByPatientAndPath"
  }

  private inner class GetVideosByPatientIdQuery<out T : Any>(
    public val patient_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("videos", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("videos", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-527_054_144,
        """SELECT videos.id, videos.patient_id, videos.original_video_path, videos.edited_video_path, videos.recorded_at, videos.stride_length_avg, videos.video_length_microseconds FROM videos WHERE patient_id = ?""",
        mapper, 1) {
      bindLong(0, patient_id)
    }

    override fun toString(): String = "Video.sq:getVideosByPatientId"
  }

  private inner class GetVideosByPatientIdOrderedQuery<out T : Any>(
    public val patient_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("videos", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("videos", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-279_302_771,
        """SELECT videos.id, videos.patient_id, videos.original_video_path, videos.edited_video_path, videos.recorded_at, videos.stride_length_avg, videos.video_length_microseconds FROM videos WHERE patient_id = ? ORDER BY recorded_at DESC""",
        mapper, 1) {
      bindLong(0, patient_id)
    }

    override fun toString(): String = "Video.sq:getVideosByPatientIdOrdered"
  }

  private inner class GetVideoCountForPatientQuery<out T : Any>(
    public val patient_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("videos", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("videos", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-706_832_575, """SELECT COUNT(*) FROM videos WHERE patient_id = ?""",
        mapper, 1) {
      bindLong(0, patient_id)
    }

    override fun toString(): String = "Video.sq:getVideoCountForPatient"
  }

  private inner class SearchVideosQuery<out T : Any>(
    public val first_name: String,
    public val last_name: String,
    public val original_video_path: String,
    public val edited_video_path: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("videos", "patients", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("videos", "patients", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_164_645_797, """
    |SELECT v.id, v.patient_id, v.original_video_path, v.edited_video_path, v.recorded_at, v.stride_length_avg, v.video_length_microseconds FROM videos v
    |INNER JOIN patients p ON v.patient_id = p.id
    |WHERE p.first_name LIKE ? OR p.last_name LIKE ? OR v.original_video_path LIKE ? OR v.edited_video_path LIKE ?
    |ORDER BY v.recorded_at DESC
    """.trimMargin(), mapper, 4) {
      bindString(0, first_name)
      bindString(1, last_name)
      bindString(2, original_video_path)
      bindString(3, edited_video_path)
    }

    override fun toString(): String = "Video.sq:searchVideos"
  }
}
