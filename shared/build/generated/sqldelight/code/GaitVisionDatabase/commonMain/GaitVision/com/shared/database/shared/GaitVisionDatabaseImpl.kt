package GaitVision.com.shared.database.shared

import GaitVision.com.shared.database.AngleDataQueries
import GaitVision.com.shared.database.GaitScoreQueries
import GaitVision.com.shared.database.GaitVisionDatabase
import GaitVision.com.shared.database.PatientQueries
import GaitVision.com.shared.database.VideoQueries
import app.cash.sqldelight.SuspendingTransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<GaitVisionDatabase>.schema: SqlSchema<QueryResult.AsyncValue<Unit>>
  get() = GaitVisionDatabaseImpl.Schema

internal fun KClass<GaitVisionDatabase>.newInstance(driver: SqlDriver): GaitVisionDatabase =
    GaitVisionDatabaseImpl(driver)

private class GaitVisionDatabaseImpl(
  driver: SqlDriver,
) : SuspendingTransacterImpl(driver), GaitVisionDatabase {
  override val angleDataQueries: AngleDataQueries = AngleDataQueries(driver)

  override val gaitScoreQueries: GaitScoreQueries = GaitScoreQueries(driver)

  override val patientQueries: PatientQueries = PatientQueries(driver)

  override val videoQueries: VideoQueries = VideoQueries(driver)

  public object Schema : SqlSchema<QueryResult.AsyncValue<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.AsyncValue<Unit> = QueryResult.AsyncValue {
      driver.execute(null, """
          |CREATE TABLE angle_data (
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    video_id INTEGER NOT NULL,
          |    frame_number INTEGER NOT NULL,
          |    left_ankle_angle REAL,
          |    right_ankle_angle REAL,
          |    left_knee_angle REAL,
          |    right_knee_angle REAL,
          |    left_hip_angle REAL,
          |    right_hip_angle REAL,
          |    torso_angle REAL,
          |    stride_angle REAL,
          |    FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE
          |)
          """.trimMargin(), 0).await()
      driver.execute(null, """
          |CREATE TABLE gait_scores (
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    patient_id INTEGER NOT NULL,
          |    video_id INTEGER NOT NULL,
          |    overall_score REAL NOT NULL,
          |    recorded_at INTEGER NOT NULL,
          |    left_knee_score REAL,
          |    right_knee_score REAL,
          |    left_hip_score REAL,
          |    right_hip_score REAL,
          |    torso_score REAL,
          |    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
          |    FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE
          |)
          """.trimMargin(), 0).await()
      driver.execute(null, """
          |CREATE TABLE patients (
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    participant_id TEXT,
          |    first_name TEXT NOT NULL DEFAULT '',
          |    last_name TEXT NOT NULL DEFAULT '',
          |    age INTEGER,
          |    gender TEXT,
          |    height INTEGER NOT NULL,
          |    created_at INTEGER NOT NULL
          |)
          """.trimMargin(), 0).await()
      driver.execute(null, """
          |CREATE TABLE videos (
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    patient_id INTEGER NOT NULL,
          |    original_video_path TEXT NOT NULL,
          |    edited_video_path TEXT NOT NULL,
          |    recorded_at INTEGER NOT NULL,
          |    stride_length_avg REAL,
          |    video_length_microseconds INTEGER,
          |    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
          |)
          """.trimMargin(), 0).await()
      driver.execute(null, "CREATE INDEX index_angle_data_video_id ON angle_data(video_id)",
          0).await()
      driver.execute(null,
          "CREATE INDEX index_angle_data_frame_number ON angle_data(video_id, frame_number)",
          0).await()
      driver.execute(null, "CREATE INDEX index_gait_scores_patient_id ON gait_scores(patient_id)",
          0).await()
      driver.execute(null, "CREATE INDEX index_gait_scores_video_id ON gait_scores(video_id)",
          0).await()
      driver.execute(null, "CREATE INDEX index_patients_participant_id ON patients(participant_id)",
          0).await()
      driver.execute(null, "CREATE INDEX index_videos_patient_id ON videos(patient_id)", 0).await()
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.AsyncValue<Unit> = QueryResult.AsyncValue {
    }
  }
}
