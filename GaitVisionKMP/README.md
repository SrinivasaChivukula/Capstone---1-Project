# GaitVision KMP

This is the Kotlin Multiplatform migration of the GaitVision project.

## Structure

- **`composeApp`**: The main shared module.
  - `commonMain`: Shared code (UI, Logic, Data).
  - `androidMain`: Android-specific implementation.
  - `iosMain`: iOS-specific implementation.
- **`iosApp`**: (To be created) The Xcode project for the iOS application.

## Migrated Components

The following components have been migrated to `commonMain`:

1.  **Math Logic**:
    - `com.gaitvision.logic.AngleCalculations`
    - `com.gaitvision.logic.ParameterFunctions`
2.  **Data Models** (Room Entities):
    - `Patient`
    - `Video`
    - `GaitScore`
3.  **Platform Interfaces**:
    - `com.gaitvision.platform.PoseDetector`
    - `com.gaitvision.platform.VideoProcessor`

## Next Steps

1.  **Implement Platform Interfaces**:
    - Implement `PoseDetector` in `androidMain` using ML Kit Android.
    - Implement `VideoProcessor` in `androidMain` using `MediaCodec`.
    - Implement the same interfaces in `iosMain` using iOS native frameworks.
2.  **UI Implementation**:
    - Port the XML layouts to Jetpack Compose in `commonMain`.
3.  **Database Setup**:
    - Configure the Room database driver for Android and iOS.
