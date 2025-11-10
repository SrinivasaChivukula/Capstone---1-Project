package GaitVision.com.shared.database

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Videos(
  public val id: Long,
  public val patient_id: Long,
  public val original_video_path: String,
  public val edited_video_path: String,
  public val recorded_at: Long,
  public val stride_length_avg: Double?,
  public val video_length_microseconds: Long?,
)
