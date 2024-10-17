plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    viewBinding {
        enable = true
    }
    dataBinding{
        enable = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // constraintLayout dependency
    implementation(libs.androidx.constraintlayout)

    /*
        Type: Base SDK
        Usage: Used for applications that are more time sensitive. Excels in speed over accuracy.
            - Processing live stream.
        Cons:
            - Lower accuracy
     */
    implementation("com.google.mlkit:pose-detection:18.0.0-beta5")
    /*
        Type: Accurate SDK
        Usage: Used for applications that need accuracy. Excels in accuracy over speed.
            - Processing stored video.
        Cons:
            - Slower processing speed
            - More CPU usage
            - More power and battery required
     */
    implementation("com.google.mlkit:pose-detection-accurate:18.0.0-beta5")

    implementation("androidx.core:core-ktx:1.7.0")
}