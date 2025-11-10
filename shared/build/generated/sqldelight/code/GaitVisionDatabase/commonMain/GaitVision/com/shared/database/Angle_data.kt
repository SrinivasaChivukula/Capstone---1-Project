package GaitVision.com.shared.database

import kotlin.Double
import kotlin.Long

public data class Angle_data(
  public val id: Long,
  public val video_id: Long,
  public val frame_number: Long,
  public val left_ankle_angle: Double?,
  public val right_ankle_angle: Double?,
  public val left_knee_angle: Double?,
  public val right_knee_angle: Double?,
  public val left_hip_angle: Double?,
  public val right_hip_angle: Double?,
  public val torso_angle: Double?,
  public val stride_angle: Double?,
)
