plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.chaquo.python")
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

        ndk {
            abiFilters += listOf("arm64-v8a","x86_64")
        }
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

    flavorDimensions += "pyVersion"
    productFlavors{
        create("py39") { dimension = "pyVersion" }
    }
}

chaquopy {
    productFlavors {
        getByName("py39") { version = "3.9" }
    }
    defaultConfig {
        version = "3.9"
        buildPython("C:\\Users\\tspen\\AppData\\Local\\Programs\\Python\\Python39\\python.exe")

        pip {
            install("pandas==1.5.0")
            install("numpy==1.23.3")
            install("scikit-learn")
            install("scipy==1.8.1")
            install("threadpoolctl==2.1.0")

            options("--only-binary=:all:")
        }
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
    //
    // Graph dependency
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")

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

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation ("com.google.android.material:material:1.9.0")

    implementation("com.chaquo.python:gradle:14.0.2")

}
