# Kotlin Multiplatform Migration Plan for GaitVision

## Executive Summary

This document outlines the recommended approach to migrate GaitVision from an Android-only Kotlin app to a Kotlin Multiplatform (KMP) app that runs on both Android and iOS.

## Current Architecture Analysis

### Platform-Specific Components (Need Platform-Specific Implementations)
1. **UI Layer**
   - Android Activities (MainActivity, SecondActivity, CameraActivity, LastActivity)
   - XML Layouts
   - ViewBinding/DataBinding
   - Jetpack Compose (partially used)

2. **Platform APIs**
   - Camera/Video Recording (MediaStore, Camera APIs)
   - File System Access (ContentResolver, MediaMetadataRetriever)
   - Permissions (Android Permission System)
   - ML Kit Pose Detection (Android-specific)

3. **Database**
   - Room Database (Android-specific)

### Shared Business Logic (Can be in Common Module)
1. **Data Models**
   - Patient, Video, GaitScore, AngleData entities
   - Repository pattern implementations

2. **Core Algorithms**
   - Angle calculations (AngleCalculations.kt)
   - Gait score prediction logic
   - Image processing algorithms
   - Parameter functions

3. **TensorFlow Lite**
   - Model inference (can work on iOS with proper setup)

## Recommended Migration Strategy

### Option 1: **Compose Multiplatform (Recommended)**
**Best for:** Modern UI, code sharing, maintainability

**Pros:**
- ✅ Maximum code sharing (UI + business logic)
- ✅ Single UI codebase using Jetpack Compose
- ✅ Native performance on both platforms
- ✅ Modern, declarative UI approach
- ✅ Growing ecosystem and community support

**Cons:**
- ⚠️ Requires rewriting XML layouts to Compose
- ⚠️ Some platform-specific UI components may need custom implementations
- ⚠️ Learning curve if team is new to Compose

**Architecture:**
```
shared/
├── commonMain/
│   ├── data/
│   │   ├── models/
│   │   ├── database/ (SQLDelight instead of Room)
│   │   └── repositories/
│   ├── domain/
│   │   ├── usecases/
│   │   └── business logic/
│   ├── ui/
│   │   └── compose/ (shared UI)
│   └── utils/
├── androidMain/
│   └── platform/
│       ├── camera/
│       ├── permissions/
│       └── mlkit/
└── iosMain/
    └── platform/
        ├── camera/
        ├── permissions/
        └── vision framework/
```

### Option 2: **Native UI with Shared Business Logic**
**Best for:** Maximum native look/feel, minimal UI changes

**Pros:**
- ✅ Keep existing Android XML layouts
- ✅ Native iOS UI (SwiftUI/UIKit)
- ✅ Platform-specific optimizations
- ✅ Easier for platform-specific developers

**Cons:**
- ❌ Duplicate UI code (Android XML + iOS SwiftUI/UIKit)
- ❌ More maintenance overhead
- ❌ Less code sharing

**Architecture:**
```
shared/
├── commonMain/
│   ├── data/
│   ├── domain/
│   └── utils/
├── androidMain/
│   └── ui/ (existing XML layouts)
└── iosMain/
    └── ui/ (SwiftUI/UIKit)
```

## Detailed Migration Plan (Option 1 - Compose Multiplatform)

### Phase 1: Project Structure Setup

1. **Create KMP Project Structure**
   ```
   GaitVision/
   ├── shared/
   │   ├── build.gradle.kts
   │   └── src/
   │       ├── commonMain/
   │       ├── androidMain/
   │       └── iosMain/
   ├── androidApp/
   │   └── build.gradle.kts
   ├── iosApp/
   │   └── (Xcode project)
   └── build.gradle.kts
   ```

2. **Update Gradle Configuration**
   - Add KMP plugin
   - Configure targets (android, ios)
   - Set up Compose Multiplatform

### Phase 2: Database Migration

**Replace Room with SQLDelight**
- Room is Android-only
- SQLDelight works on both platforms
- Similar API, easier migration

**Steps:**
1. Create SQLDelight schema files
2. Generate Kotlin code from SQL
3. Update repositories to use SQLDelight
4. Migrate existing Room entities

### Phase 3: Business Logic Extraction

**Move to `commonMain`:**
1. Data models (Patient, Video, GaitScore, AngleData)
2. Angle calculation functions
3. Gait score prediction logic
4. TensorFlow Lite inference (with platform-specific model loading)
5. Repository implementations (using SQLDelight)

### Phase 4: Platform-Specific Implementations

**Android (`androidMain`):**
- Camera/Video recording (existing code)
- ML Kit Pose Detection
- File system access
- Permissions

**iOS (`iosMain`):**
- AVFoundation for camera/video
- Vision Framework for pose detection (alternative to ML Kit)
- FileManager for file access
- iOS permission APIs

### Phase 5: UI Migration to Compose Multiplatform

**Convert XML layouts to Compose:**
1. MainActivity → MainScreen composable
2. SecondActivity → ProcessingScreen composable
3. CameraActivity → CameraScreen composable
4. LastActivity → ResultsScreen composable

**Platform-specific UI:**
- Camera preview (platform-specific)
- Video player (platform-specific)
- File picker (platform-specific)

### Phase 6: TensorFlow Lite Setup

**iOS Considerations:**
- Use TensorFlow Lite iOS framework
- Load models from bundle (similar to Android assets)
- Ensure model compatibility

## Key Technical Decisions

### 1. Pose Detection on iOS

**Option A: Vision Framework (Recommended)**
- Native iOS framework
- Good accuracy
- No external dependencies

**Option B: MediaPipe**
- Cross-platform
- More features
- Larger dependency

**Option C: Custom KMP Pose Detection**
- Maximum code sharing
- May require custom implementation

### 2. Database Solution

**SQLDelight (Recommended)**
- ✅ Works on both platforms
- ✅ Type-safe SQL
- ✅ Good performance
- ✅ Coroutines support

**Alternative: Realm KMP**
- Commercial option
- More features
- Larger dependency

### 3. Dependency Injection

**Koin (Recommended)**
- ✅ Works on both platforms
- ✅ Lightweight
- ✅ Easy to use

**Alternative: Kodein**
- Similar features
- Different API

### 4. Image Processing

**Strategy:**
- Core algorithms in `commonMain`
- Platform-specific image loading/bitmap handling
- Use expect/actual for platform differences

## Implementation Checklist

### Setup & Configuration
- [ ] Create KMP project structure
- [ ] Configure Gradle for KMP
- [ ] Set up iOS target
- [ ] Configure Compose Multiplatform
- [ ] Set up CI/CD for both platforms

### Database
- [ ] Install SQLDelight
- [ ] Create schema files
- [ ] Migrate entities
- [ ] Update repositories
- [ ] Test database operations

### Business Logic
- [ ] Move data models to commonMain
- [ ] Extract angle calculations
- [ ] Extract gait score logic
- [ ] Set up TensorFlow Lite for both platforms
- [ ] Create platform-specific model loaders

### Platform APIs
- [ ] Implement Android camera/permissions
- [ ] Implement iOS camera/permissions
- [ ] Implement Android pose detection (ML Kit)
- [ ] Implement iOS pose detection (Vision Framework)
- [ ] Create expect/actual for file operations

### UI
- [ ] Convert MainActivity to Compose
- [ ] Convert SecondActivity to Compose
- [ ] Convert CameraActivity to Compose
- [ ] Convert LastActivity to Compose
- [ ] Implement platform-specific camera previews
- [ ] Test UI on both platforms

### Testing
- [ ] Unit tests for shared logic
- [ ] Platform-specific tests
- [ ] UI tests
- [ ] Integration tests

## Estimated Timeline

- **Phase 1-2 (Setup & Database):** 1-2 weeks
- **Phase 3 (Business Logic):** 1-2 weeks
- **Phase 4 (Platform APIs):** 2-3 weeks
- **Phase 5 (UI Migration):** 3-4 weeks
- **Phase 6 (Testing & Polish):** 2-3 weeks

**Total: 9-14 weeks** (depending on team size and experience)

## Resources & Dependencies

### Required Dependencies
```kotlin
// Compose Multiplatform
compose-multiplatform

// SQLDelight
sqldelight

// Coroutines
kotlinx-coroutines-core

// Koin (DI)
koin-core, koin-compose

// TensorFlow Lite
tensorflow-lite (platform-specific)

// Image Loading
coil (Android), Nuke (iOS) or KMP image loader
```

### Learning Resources
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [SQLDelight](https://cashapp.github.io/sqldelight/)
- [KMP Best Practices](https://kotlinlang.org/docs/multiplatform-mobile-understand-project-structure.html)

## Risks & Mitigations

### Risk 1: ML Kit vs Vision Framework Differences
**Mitigation:** Create abstraction layer, test thoroughly on both platforms

### Risk 2: Performance Differences
**Mitigation:** Profile on both platforms, optimize as needed

### Risk 3: UI/UX Consistency
**Mitigation:** Use Compose Multiplatform for consistent look, platform-specific where needed

### Risk 4: Learning Curve
**Mitigation:** Start with small features, gradually migrate

## Next Steps

1. **Review and approve this plan**
2. **Set up development environment** (Xcode, iOS Simulator)
3. **Create proof-of-concept** (simple screen with shared logic)
4. **Begin Phase 1** (project structure setup)
5. **Iterate and test** continuously

---

## Recommendation

**I recommend Option 1 (Compose Multiplatform)** because:
1. Maximum code sharing reduces maintenance
2. Modern, maintainable codebase
3. Better long-term scalability
4. Growing ecosystem and community
5. Your app already uses Compose partially

The migration will require effort, but the benefits of a single codebase for both platforms will pay off in the long run.

