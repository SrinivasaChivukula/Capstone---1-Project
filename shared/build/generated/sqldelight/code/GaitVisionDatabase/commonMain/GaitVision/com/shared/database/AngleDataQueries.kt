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

public class AngleDataQueries(
  driver: SqlDriver,
) : SuspendingTransacterImpl(driver) {
  public fun <T : Any> getAngleDataById(id: Long, mapper: (
    id: Long,
    video_id: Long,
    frame_number: Long,
    left_ankle_angle: Double?,
    right_ankle_angle: Double?,
    left_knee_angle: Double?,
    right_knee_angle: Double?,
    left_hip_angle: Double?,
    right_hip_angle: Double?,
    torso_angle: Double?,
    stride_angle: Double?,
  ) -> T): Query<T> = GetAngleDataByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getDouble(3),
      cursor.getDouble(4),
      cursor.getDouble(5),
      cursor.getDouble(6),
      cursor.getDouble(7),
      cursor.getDouble(8),
      cursor.getDouble(9),
      cursor.getDouble(10)
    )
  }

  public fun getAngleDataById(id: Long): Query<Angle_data> = getAngleDataById(id) { id_, video_id,
      frame_number, left_ankle_angle, right_ankle_angle, left_knee_angle, right_knee_angle,
      left_hip_angle, right_hip_angle, torso_angle, stride_angle ->
    Angle_data(
      id_,
      video_id,
      frame_number,
      left_ankle_angle,
      right_ankle_angle,
      left_knee_angle,
      right_knee_angle,
      left_hip_angle,
      right_hip_angle,
      torso_angle,
      stride_angle
    )
  }

  public fun <T : Any> getAngleDataByVideoId(video_id: Long, mapper: (
    id: Long,
    video_id: Long,
    frame_number: Long,
    left_ankle_angle: Double?,
    right_ankle_angle: Double?,
    left_knee_angle: Double?,
    right_knee_angle: Double?,
    left_hip_angle: Double?,
    right_hip_angle: Double?,
    torso_angle: Double?,
    stride_angle: Double?,
  ) -> T): Query<T> = GetAngleDataByVideoIdQuery(video_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getDouble(3),
      cursor.getDouble(4),
      cursor.getDouble(5),
      cursor.getDouble(6),
      cursor.getDouble(7),
      cursor.getDouble(8),
      cursor.getDouble(9),
      cursor.getDouble(10)
    )
  }

  public fun getAngleDataByVideoId(video_id: Long): Query<Angle_data> =
      getAngleDataByVideoId(video_id) { id, video_id_, frame_number, left_ankle_angle,
      right_ankle_angle, left_knee_angle, right_knee_angle, left_hip_angle, right_hip_angle,
      torso_angle, stride_angle ->
    Angle_data(
      id,
      video_id_,
      frame_number,
      left_ankle_angle,
      right_ankle_angle,
      left_knee_angle,
      right_knee_angle,
      left_hip_angle,
      right_hip_angle,
      torso_angle,
      stride_angle
    )
  }

  public fun <T : Any> getAllAngleData(mapper: (
    id: Long,
    video_id: Long,
    frame_number: Long,
    left_ankle_angle: Double?,
    right_ankle_angle: Double?,
    left_knee_angle: Double?,
    right_knee_angle: Double?,
    left_hip_angle: Double?,
    right_hip_angle: Double?,
    torso_angle: Double?,
    stride_angle: Double?,
  ) -> T): Query<T> = Query(13_981_803, arrayOf("angle_data"), driver, "AngleData.sq",
      "getAllAngleData",
      "SELECT angle_data.id, angle_data.video_id, angle_data.frame_number, angle_data.left_ankle_angle, angle_data.right_ankle_angle, angle_data.left_knee_angle, angle_data.right_knee_angle, angle_data.left_hip_angle, angle_data.right_hip_angle, angle_data.torso_angle, angle_data.stride_angle FROM angle_data") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getDouble(3),
      cursor.getDouble(4),
      cursor.getDouble(5),
      cursor.getDouble(6),
      cursor.getDouble(7),
      cursor.getDouble(8),
      cursor.getDouble(9),
      cursor.getDouble(10)
    )
  }

  public fun getAllAngleData(): Query<Angle_data> = getAllAngleData { id, video_id, frame_number,
      left_ankle_angle, right_ankle_angle, left_knee_angle, right_knee_angle, left_hip_angle,
      right_hip_angle, torso_angle, stride_angle ->
    Angle_data(
      id,
      video_id,
      frame_number,
      left_ankle_angle,
      right_ankle_angle,
      left_knee_angle,
      right_knee_angle,
      left_hip_angle,
      right_hip_angle,
      torso_angle,
      stride_angle
    )
  }

  public fun getLastInsertId(): ExecutableQuery<Long> = Query(-1_576_061_863, driver,
      "AngleData.sq", "getLastInsertId", "SELECT last_insert_rowid()") { cursor ->
    cursor.getLong(0)!!
  }

  public fun getLeftKneeAnglesByVideoId(video_id: Long): Query<Double> =
      GetLeftKneeAnglesByVideoIdQuery(video_id) { cursor ->
    cursor.getDouble(0)!!
  }

  public fun getRightKneeAnglesByVideoId(video_id: Long): Query<Double> =
      GetRightKneeAnglesByVideoIdQuery(video_id) { cursor ->
    cursor.getDouble(0)!!
  }

  public fun getLeftHipAnglesByVideoId(video_id: Long): Query<Double> =
      GetLeftHipAnglesByVideoIdQuery(video_id) { cursor ->
    cursor.getDouble(0)!!
  }

  public fun getRightHipAnglesByVideoId(video_id: Long): Query<Double> =
      GetRightHipAnglesByVideoIdQuery(video_id) { cursor ->
    cursor.getDouble(0)!!
  }

  public fun getLeftAnkleAnglesByVideoId(video_id: Long): Query<Double> =
      GetLeftAnkleAnglesByVideoIdQuery(video_id) { cursor ->
    cursor.getDouble(0)!!
  }

  public fun getRightAnkleAnglesByVideoId(video_id: Long): Query<Double> =
      GetRightAnkleAnglesByVideoIdQuery(video_id) { cursor ->
    cursor.getDouble(0)!!
  }

  public fun getTorsoAnglesByVideoId(video_id: Long): Query<Double> =
      GetTorsoAnglesByVideoIdQuery(video_id) { cursor ->
    cursor.getDouble(0)!!
  }

  public fun getStrideAnglesByVideoId(video_id: Long): Query<Double> =
      GetStrideAnglesByVideoIdQuery(video_id) { cursor ->
    cursor.getDouble(0)!!
  }

  public fun getAngleDataCount(): Query<Long> = Query(-796_065_247, arrayOf("angle_data"), driver,
      "AngleData.sq", "getAngleDataCount", "SELECT COUNT(*) FROM angle_data") { cursor ->
    cursor.getLong(0)!!
  }

  public fun getAngleDataCountByVideoId(video_id: Long): Query<Long> =
      GetAngleDataCountByVideoIdQuery(video_id) { cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> getMaxFrameNumberForVideo(video_id: Long, mapper: (MAX: Long?) -> T):
      Query<T> = GetMaxFrameNumberForVideoQuery(video_id) { cursor ->
    mapper(
      cursor.getLong(0)
    )
  }

  public fun getMaxFrameNumberForVideo(video_id: Long): Query<GetMaxFrameNumberForVideo> =
      getMaxFrameNumberForVideo(video_id) { MAX ->
    GetMaxFrameNumberForVideo(
      MAX
    )
  }

  public suspend fun createAngleData(
    video_id: Long,
    frame_number: Long,
    left_ankle_angle: Double?,
    right_ankle_angle: Double?,
    left_knee_angle: Double?,
    right_knee_angle: Double?,
    left_hip_angle: Double?,
    right_hip_angle: Double?,
    torso_angle: Double?,
    stride_angle: Double?,
  ) {
    driver.execute(123_057_402, """
        |INSERT INTO angle_data (video_id, frame_number, left_ankle_angle, right_ankle_angle, left_knee_angle, right_knee_angle, left_hip_angle, right_hip_angle, torso_angle, stride_angle)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 10) {
          bindLong(0, video_id)
          bindLong(1, frame_number)
          bindDouble(2, left_ankle_angle)
          bindDouble(3, right_ankle_angle)
          bindDouble(4, left_knee_angle)
          bindDouble(5, right_knee_angle)
          bindDouble(6, left_hip_angle)
          bindDouble(7, right_hip_angle)
          bindDouble(8, torso_angle)
          bindDouble(9, stride_angle)
        }.await()
    notifyQueries(123_057_402) { emit ->
      emit("angle_data")
    }
  }

  public suspend fun updateAngleData(
    video_id: Long,
    frame_number: Long,
    left_ankle_angle: Double?,
    right_ankle_angle: Double?,
    left_knee_angle: Double?,
    right_knee_angle: Double?,
    left_hip_angle: Double?,
    right_hip_angle: Double?,
    torso_angle: Double?,
    stride_angle: Double?,
    id: Long,
  ) {
    driver.execute(-1_501_260_851, """
        |UPDATE angle_data
        |SET video_id = ?,
        |    frame_number = ?,
        |    left_ankle_angle = ?,
        |    right_ankle_angle = ?,
        |    left_knee_angle = ?,
        |    right_knee_angle = ?,
        |    left_hip_angle = ?,
        |    right_hip_angle = ?,
        |    torso_angle = ?,
        |    stride_angle = ?
        |WHERE id = ?
        """.trimMargin(), 11) {
          bindLong(0, video_id)
          bindLong(1, frame_number)
          bindDouble(2, left_ankle_angle)
          bindDouble(3, right_ankle_angle)
          bindDouble(4, left_knee_angle)
          bindDouble(5, right_knee_angle)
          bindDouble(6, left_hip_angle)
          bindDouble(7, right_hip_angle)
          bindDouble(8, torso_angle)
          bindDouble(9, stride_angle)
          bindLong(10, id)
        }.await()
    notifyQueries(-1_501_260_851) { emit ->
      emit("angle_data")
    }
  }

  public suspend fun deleteAngleData(id: Long) {
    driver.execute(-1_924_553_429, """DELETE FROM angle_data WHERE id = ?""", 1) {
          bindLong(0, id)
        }.await()
    notifyQueries(-1_924_553_429) { emit ->
      emit("angle_data")
    }
  }

  public suspend fun deleteAngleDataByVideoId(video_id: Long) {
    driver.execute(-2_074_193_484, """DELETE FROM angle_data WHERE video_id = ?""", 1) {
          bindLong(0, video_id)
        }.await()
    notifyQueries(-2_074_193_484) { emit ->
      emit("angle_data")
    }
  }

  private inner class GetAngleDataByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("angle_data", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("angle_data", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_272_627_072,
        """SELECT angle_data.id, angle_data.video_id, angle_data.frame_number, angle_data.left_ankle_angle, angle_data.right_ankle_angle, angle_data.left_knee_angle, angle_data.right_knee_angle, angle_data.left_hip_angle, angle_data.right_hip_angle, angle_data.torso_angle, angle_data.stride_angle FROM angle_data WHERE id = ?""",
        mapper, 1) {
      bindLong(0, id)
    }

    override fun toString(): String = "AngleData.sq:getAngleDataById"
  }

  private inner class GetAngleDataByVideoIdQuery<out T : Any>(
    public val video_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("angle_data", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("angle_data", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-377_942_351,
        """SELECT angle_data.id, angle_data.video_id, angle_data.frame_number, angle_data.left_ankle_angle, angle_data.right_ankle_angle, angle_data.left_knee_angle, angle_data.right_knee_angle, angle_data.left_hip_angle, angle_data.right_hip_angle, angle_data.torso_angle, angle_data.stride_angle FROM angle_data WHERE video_id = ? ORDER BY frame_number ASC""",
        mapper, 1) {
      bindLong(0, video_id)
    }

    override fun toString(): String = "AngleData.sq:getAngleDataByVideoId"
  }

  private inner class GetLeftKneeAnglesByVideoIdQuery<out T : Any>(
    public val video_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("angle_data", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("angle_data", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(947_808_230,
        """SELECT left_knee_angle FROM angle_data WHERE video_id = ? AND left_knee_angle IS NOT NULL ORDER BY frame_number ASC""",
        mapper, 1) {
      bindLong(0, video_id)
    }

    override fun toString(): String = "AngleData.sq:getLeftKneeAnglesByVideoId"
  }

  private inner class GetRightKneeAnglesByVideoIdQuery<out T : Any>(
    public val video_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("angle_data", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("angle_data", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_955_187_823,
        """SELECT right_knee_angle FROM angle_data WHERE video_id = ? AND right_knee_angle IS NOT NULL ORDER BY frame_number ASC""",
        mapper, 1) {
      bindLong(0, video_id)
    }

    override fun toString(): String = "AngleData.sq:getRightKneeAnglesByVideoId"
  }

  private inner class GetLeftHipAnglesByVideoIdQuery<out T : Any>(
    public val video_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("angle_data", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("angle_data", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(791_577_734,
        """SELECT left_hip_angle FROM angle_data WHERE video_id = ? AND left_hip_angle IS NOT NULL ORDER BY frame_number ASC""",
        mapper, 1) {
      bindLong(0, video_id)
    }

    override fun toString(): String = "AngleData.sq:getLeftHipAnglesByVideoId"
  }

  private inner class GetRightHipAnglesByVideoIdQuery<out T : Any>(
    public val video_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("angle_data", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("angle_data", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-145_757_475,
        """SELECT right_hip_angle FROM angle_data WHERE video_id = ? AND right_hip_angle IS NOT NULL ORDER BY frame_number ASC""",
        mapper, 1) {
      bindLong(0, video_id)
    }

    override fun toString(): String = "AngleData.sq:getRightHipAnglesByVideoId"
  }

  private inner class GetLeftAnkleAnglesByVideoIdQuery<out T : Any>(
    public val video_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("angle_data", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("angle_data", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_415_375_294,
        """SELECT left_ankle_angle FROM angle_data WHERE video_id = ? AND left_ankle_angle IS NOT NULL ORDER BY frame_number ASC""",
        mapper, 1) {
      bindLong(0, video_id)
    }

    override fun toString(): String = "AngleData.sq:getLeftAnkleAnglesByVideoId"
  }

  private inner class GetRightAnkleAnglesByVideoIdQuery<out T : Any>(
    public val video_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("angle_data", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("angle_data", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_715_595_691,
        """SELECT right_ankle_angle FROM angle_data WHERE video_id = ? AND right_ankle_angle IS NOT NULL ORDER BY frame_number ASC""",
        mapper, 1) {
      bindLong(0, video_id)
    }

    override fun toString(): String = "AngleData.sq:getRightAnkleAnglesByVideoId"
  }

  private inner class GetTorsoAnglesByVideoIdQuery<out T : Any>(
    public val video_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("angle_data", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("angle_data", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(861_512_955,
        """SELECT torso_angle FROM angle_data WHERE video_id = ? AND torso_angle IS NOT NULL ORDER BY frame_number ASC""",
        mapper, 1) {
      bindLong(0, video_id)
    }

    override fun toString(): String = "AngleData.sq:getTorsoAnglesByVideoId"
  }

  private inner class GetStrideAnglesByVideoIdQuery<out T : Any>(
    public val video_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("angle_data", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("angle_data", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_169_959_767,
        """SELECT stride_angle FROM angle_data WHERE video_id = ? AND stride_angle IS NOT NULL ORDER BY frame_number ASC""",
        mapper, 1) {
      bindLong(0, video_id)
    }

    override fun toString(): String = "AngleData.sq:getStrideAnglesByVideoId"
  }

  private inner class GetAngleDataCountByVideoIdQuery<out T : Any>(
    public val video_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("angle_data", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("angle_data", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_605_708_158, """SELECT COUNT(*) FROM angle_data WHERE video_id = ?""",
        mapper, 1) {
      bindLong(0, video_id)
    }

    override fun toString(): String = "AngleData.sq:getAngleDataCountByVideoId"
  }

  private inner class GetMaxFrameNumberForVideoQuery<out T : Any>(
    public val video_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("angle_data", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("angle_data", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_588_160_109,
        """SELECT MAX(frame_number) FROM angle_data WHERE video_id = ?""", mapper, 1) {
      bindLong(0, video_id)
    }

    override fun toString(): String = "AngleData.sq:getMaxFrameNumberForVideo"
  }
}
