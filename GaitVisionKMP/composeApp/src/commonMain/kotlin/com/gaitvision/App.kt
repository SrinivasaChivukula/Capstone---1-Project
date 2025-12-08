
@Composable
fun App(
    poseDetector: PoseDetector,
    videoProcessor: VideoProcessor,
    database: AppDatabase
) {
    GaitVisionTheme {
        AppNavigation(
            poseDetector = poseDetector,
            videoProcessor = videoProcessor,
            database = database
        )
    }
}
