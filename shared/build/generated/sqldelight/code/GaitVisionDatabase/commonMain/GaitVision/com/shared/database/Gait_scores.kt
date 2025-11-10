package GaitVision.com.shared.database

import kotlin.Double
import kotlin.Long

public data class Gait_scores(
  public val id: Long,
  public val patient_id: Long,
  public val video_id: Long,
  public val overall_score: Double,
  public val recorded_at: Long,
  public val left_knee_score: Double?,
  public val right_knee_score: Double?,
  public val left_hip_score: Double?,
  public val right_hip_score: Double?,
  public val torso_score: Double?,
)
