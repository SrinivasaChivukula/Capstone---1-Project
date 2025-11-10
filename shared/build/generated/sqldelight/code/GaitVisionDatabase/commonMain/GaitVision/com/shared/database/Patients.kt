package GaitVision.com.shared.database

import kotlin.Long
import kotlin.String

public data class Patients(
  public val id: Long,
  public val participant_id: String?,
  public val first_name: String,
  public val last_name: String,
  public val age: Long?,
  public val gender: String?,
  public val height: Long,
  public val created_at: Long,
)
