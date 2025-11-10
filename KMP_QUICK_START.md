# Kotlin Multiplatform Quick Start Guide

## Step-by-Step Implementation

### Step 1: Update Project Structure

#### Update `settings.gradle.kts`
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GaitVision"
include(":shared")
include(":androidApp")
```

#### Create `shared/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.koin)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                
                // SQLDelight
                implementation("app.cash.sqldelight:runtime:2.0.0")
                
                // Koin
                implementation("io.insert-koin:koin-core:3.5.0")
                implementation("io.insert-koin:koin-compose:1.1.0")
                
                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            }
        }
        
        val androidMain by getting {
            dependencies {
                // Android-specific dependencies
                implementation("app.cash.sqldelight:android-driver:2.0.0")
                implementation("com.google.mlkit:pose-detection:18.0.0-beta5")
                implementation("org.tensorflow:tensorflow-lite:2.13.0")
            }
        }
        
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                // iOS-specific dependencies
                implementation("app.cash.sqldelight:native-driver:2.0.0")
            }
        }
    }
}

android {
    namespace = "GaitVision.com.shared"
    compileSdk = 34
    
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    
    defaultConfig {
        minSdk = 24
    }
}

sqldelight {
    databases {
        create("GaitVisionDatabase") {
            packageName.set("GaitVision.com.shared.database")
            generateAsync.set(true)
        }
    }
}
```

#### Update `gradle/libs.versions.toml`
```toml
[versions]
kotlin = "2.0.0"
compose-multiplatform = "1.5.10"
sqldelight = "2.0.0"
koin = "3.5.0"

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
koin = { id = "io.insert-koin", version.ref = "koin" }
```

### Step 2: Create Shared Module Structure

```
shared/src/
├── commonMain/
│   ├── kotlin/
│   │   └── GaitVision/
│   │       ├── shared/
│   │       │   ├── data/
│   │       │   │   ├── models/
│   │       │   │   │   ├── Patient.kt
│   │       │   │   │   ├── Video.kt
│   │       │   │   │   ├── GaitScore.kt
│   │       │   │   │   └── AngleData.kt
│   │       │   │   ├── database/
│   │       │   │   │   └── GaitVisionDatabase.kt
│   │       │   │   └── repositories/
│   │       │   │       ├── PatientRepository.kt
│   │       │   │       ├── VideoRepository.kt
│   │       │   │       └── ...
│   │       │   ├── domain/
│   │       │   │   ├── AngleCalculations.kt
│   │       │   │   ├── GaitScoreCalculator.kt
│   │       │   │   └── ImageProcessor.kt
│   │       │   ├── ui/
│   │       │   │   ├── screens/
│   │       │   │   │   ├── MainScreen.kt
│   │       │   │   │   ├── ProcessingScreen.kt
│   │       │   │   │   └── ResultsScreen.kt
│   │       │   │   └── theme/
│   │       │   └── utils/
│   │       └── platform/
│   │           ├── Camera.kt (expect)
│   │           ├── FileManager.kt (expect)
│   │           └── PoseDetector.kt (expect)
│   └── resources/
│       └── (TensorFlow Lite models)
├── androidMain/
│   ├── kotlin/
│   │   └── GaitVision/
│   │       └── platform/
│   │           ├── AndroidCamera.kt (actual)
│   │           ├── AndroidFileManager.kt (actual)
│   │           └── AndroidPoseDetector.kt (actual)
│   └── AndroidManifest.xml
└── iosMain/
    └── kotlin/
        └── GaitVision/
            └── platform/
                ├── IOSCamera.kt (actual)
                ├── IOSFileManager.kt (actual)
                └── IOSPoseDetector.kt (actual)
```

### Step 3: Example - Platform Abstraction Pattern

#### `shared/src/commonMain/kotlin/platform/Camera.kt`
```kotlin
expect class CameraManager {
    suspend fun requestPermissions(): Boolean
    fun hasPermissions(): Boolean
    suspend fun captureVideo(): VideoResult?
    suspend fun pickVideoFromGallery(): VideoResult?
}

data class VideoResult(
    val uri: String,
    val path: String
)
```

#### `shared/src/androidMain/kotlin/platform/AndroidCamera.kt`
```kotlin
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
// ... other Android imports

actual class CameraManager(private val context: Context) {
    actual suspend fun requestPermissions(): Boolean {
        // Android permission logic
        return true
    }
    
    actual fun hasPermissions(): Boolean {
        // Check Android permissions
        return true
    }
    
    actual suspend fun captureVideo(): VideoResult? {
        // Android camera implementation
        return null
    }
    
    actual suspend fun pickVideoFromGallery(): VideoResult? {
        // Android gallery picker
        return null
    }
}
```

#### `shared/src/iosMain/kotlin/platform/IOSCamera.kt`
```kotlin
import platform.AVFoundation.*
import platform.UIKit.*
// ... other iOS imports

actual class CameraManager {
    actual suspend fun requestPermissions(): Boolean {
        // iOS permission logic using AVFoundation
        return true
    }
    
    actual fun hasPermissions(): Boolean {
        // Check iOS permissions
        return true
    }
    
    actual suspend fun captureVideo(): VideoResult? {
        // iOS camera implementation using UIImagePickerController
        return null
    }
    
    actual suspend fun pickVideoFromGallery(): VideoResult? {
        // iOS gallery picker
        return null
    }
}
```

### Step 4: Example - Database Migration (Room → SQLDelight)

#### Create `shared/src/commonMain/sqldelight/Patient.sq`
```sql
CREATE TABLE patient (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    participant_id TEXT NOT NULL UNIQUE,
    height INTEGER NOT NULL,
    created_at INTEGER NOT NULL
);

createPatient:
INSERT INTO patient (participant_id, height, created_at)
VALUES (?, ?, ?);

getPatientById:
SELECT * FROM patient WHERE id = ?;

getPatientByParticipantId:
SELECT * FROM patient WHERE participant_id = ?;

getAllPatients:
SELECT * FROM patient ORDER BY created_at DESC;
```

#### Update Repository
```kotlin
// shared/src/commonMain/kotlin/data/repositories/PatientRepository.kt
class PatientRepository(
    private val database: GaitVisionDatabase
) {
    suspend fun findOrCreatePatientByParticipantId(
        participantId: String,
        height: Int
    ): Patient {
        val existing = database.gaitVisionDatabaseQueries
            .getPatientByParticipantId(participantId)
            .executeAsOneOrNull()
            
        return existing ?: run {
            val id = database.gaitVisionDatabaseQueries
                .createPatient(participantId, height, System.currentTimeMillis())
            // Return created patient
        }
    }
}
```

### Step 5: Example - Compose UI Migration

#### `shared/src/commonMain/kotlin/ui/screens/MainScreen.kt`
```kotlin
@Composable
fun MainScreen(
    onNavigateToProcessing: (String, Int, String) -> Unit,
    cameraManager: CameraManager
) {
    var participantId by remember { mutableStateOf("") }
    var selectedHeight by remember { mutableStateOf(69) } // 5'9"
    var videoUri by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "GaitVision",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = participantId,
            onValueChange = { participantId = it },
            label = { Text("Participant ID") }
        )
        
        // Height selector
        // Video preview
        // Buttons for camera/gallery
        
        Button(
            onClick = {
                onNavigateToProcessing(participantId, selectedHeight, videoUri ?: "")
            },
            enabled = participantId.isNotEmpty() && videoUri != null
        ) {
            Text("Confirm Video")
        }
    }
}
```

### Step 6: Android App Module

#### `androidApp/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose)
}

android {
    namespace = "GaitVision.com"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "GaitVision.com"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":shared"))
    // Other Android-specific dependencies
}
```

#### `androidApp/src/main/java/MainActivity.kt`
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                MainScreen(
                    onNavigateToProcessing = { id, height, uri ->
                        // Navigate to processing screen
                    },
                    cameraManager = AndroidCameraManager(this)
                )
            }
        }
    }
}
```

### Step 7: iOS App Setup

#### Create Xcode Project
1. Open Xcode
2. Create new iOS App project
3. Add shared framework as dependency
4. Configure build settings

#### `iosApp/ContentView.swift` (SwiftUI wrapper)
```swift
import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

## Migration Priority Order

1. **Week 1-2: Setup & Database**
   - Set up KMP project structure
   - Migrate Room to SQLDelight
   - Test database operations

2. **Week 3-4: Business Logic**
   - Move data models to commonMain
   - Extract angle calculations
   - Extract gait score logic
   - Set up TensorFlow Lite

3. **Week 5-7: Platform APIs**
   - Implement camera abstractions
   - Implement pose detection (ML Kit for Android, Vision for iOS)
   - Implement file operations

4. **Week 8-11: UI Migration**
   - Convert screens to Compose
   - Implement platform-specific UI components
   - Test on both platforms

5. **Week 12-14: Polish & Testing**
   - Fix platform-specific issues
   - Performance optimization
   - Comprehensive testing

## Common Pitfalls to Avoid

1. **Don't mix platform-specific code in commonMain**
   - Use expect/actual pattern
   - Keep platform code in platform-specific source sets

2. **Don't forget iOS-specific requirements**
   - Info.plist permissions
   - iOS deployment target
   - App Store guidelines

3. **Test early and often**
   - Test on both platforms simultaneously
   - Don't wait until the end

4. **Handle platform differences gracefully**
   - File paths differ
   - Permission models differ
   - UI conventions differ

## Next Immediate Steps

1. ✅ Review migration plan
2. ⬜ Set up KMP project structure
3. ⬜ Install Xcode and iOS development tools
4. ⬜ Create proof-of-concept (simple screen)
5. ⬜ Begin database migration

