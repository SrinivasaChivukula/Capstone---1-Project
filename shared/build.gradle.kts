plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget()
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.materialIconsExtended)
                
                // Note: Navigation will be handled via state management for now
                // Can add navigation library later if needed
                
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")
                
                // SQLDelight
                implementation("app.cash.sqldelight:runtime:${libs.versions.sqldelight.get()}")
                
                // Koin
                implementation("io.insert-koin:koin-core:${libs.versions.koin.get()}")
                implementation("io.insert-koin:koin-compose:1.1.0")
                
                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.serialization.get()}")
            }
        }
        
        val androidMain by getting {
            dependencies {
                // Android-specific SQLDelight driver
                implementation("app.cash.sqldelight:android-driver:${libs.versions.sqldelight.get()}")
                
                // ML Kit for pose detection
                implementation("com.google.mlkit:pose-detection:18.0.0-beta5")
                implementation("com.google.mlkit:pose-detection-accurate:18.0.0-beta5")
            }
        }
        
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                // iOS-specific SQLDelight driver
                implementation("app.cash.sqldelight:native-driver:${libs.versions.sqldelight.get()}")
            }
        }
    }
}

android {
    namespace = "GaitVision.com.shared"
    compileSdk = 34
    
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
    
    defaultConfig {
        minSdk = 24
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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

