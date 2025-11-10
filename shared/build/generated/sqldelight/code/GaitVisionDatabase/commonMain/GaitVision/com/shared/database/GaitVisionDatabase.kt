package GaitVision.com.shared.database

import GaitVision.com.shared.database.shared.newInstance
import GaitVision.com.shared.database.shared.schema
import app.cash.sqldelight.SuspendingTransacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlin.Unit

public interface GaitVisionDatabase : SuspendingTransacter {
  public val angleDataQueries: AngleDataQueries

  public val gaitScoreQueries: GaitScoreQueries

  public val patientQueries: PatientQueries

  public val videoQueries: VideoQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.AsyncValue<Unit>>
      get() = GaitVisionDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): GaitVisionDatabase =
        GaitVisionDatabase::class.newInstance(driver)
  }
}
