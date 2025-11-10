# GaitVision Shared Module

This is the Kotlin Multiplatform shared module for GaitVision, containing code that runs on both Android and iOS.

## Structure

```
shared/
├── src/
│   ├── commonMain/          # Shared code for all platforms
│   │   ├── kotlin/          # Business logic, data models, UI
│   │   ├── sqldelight/      # SQLDelight database schemas
│   │   └── resources/       # Shared resources (images, etc.)
│   ├── androidMain/         # Android-specific implementations
│   │   └── kotlin/          # Android platform code
│   └── iosMain/             # iOS-specific implementations
│       └── kotlin/          # iOS platform code
```

## Platform Abstractions

The module uses Kotlin's `expect/actual` mechanism to provide platform-specific implementations:

- **Camera.kt**: Camera and video recording functionality
- **FileManager.kt**: File system operations
- **PoseDetector.kt**: Pose detection (ML Kit on Android, Vision Framework on iOS)

## Next Steps

1. Migrate data models from app module
2. Set up SQLDelight database schemas
3. Migrate business logic (angle calculations, gait score)
4. Create Compose Multiplatform UI screens

