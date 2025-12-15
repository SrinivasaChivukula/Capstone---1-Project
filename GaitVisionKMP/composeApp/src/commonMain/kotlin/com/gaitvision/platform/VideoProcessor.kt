package com.gaitvision.platform

interface VideoProcessor {
    /**
     * Processes a video file to extract frames, run pose detection,
     * draw the skeleton overlay, and save the result.
     *
     * @param inputPath Absolute path to the input video file.
     * @param outputPath Absolute path where the processed video should be saved.
     * @param onProgress Callback for progress updates (0-100).
     * @return The path to the processed video, or null if failed.
     */
    suspend fun processVideo(
        inputPath: String, 
        outputPath: String,
        onProgress: (Int) -> Unit,
        onPoseDetected: (Pose) -> Unit
    ): String?
}
