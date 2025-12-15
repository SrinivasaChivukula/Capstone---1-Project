# GaitVision

**2D Gait Analysis for Clinical Use**

An Android application designed to provide accessible gait analysis tools for clinical assessment, particularly in resource-limited settings. GaitVision uses computer vision and machine learning to analyze walking patterns with minimal hardware requirements.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Requirements](#requirements)
- [Technology Stack](#technology-stack)
- [Installation](#installation)
- [Usage](#usage)
- [Acknowledgments](#acknowledgments)
- [References](#references)

---

## Overview

GaitVision is an Android application focused on solving the problem of limited technology access for gait analysis. The application requires minimal hardware: an Android phone (Android 7.0+) with camera access or stored videos.

The app analyzes walking patterns, calculates joint angles, and generates gait scores for clinical assessment and rehabilitation monitoring.

---

## Features

- Record or select videos from device storage
- Pose estimation using Google ML Kit
- Calculates knee, hip, ankle, torso, and stride angles
- Displays angle measurements overlaid on video frames
- TensorFlow Lite autoencoder model generates gait scores
- Interactive graphs of angle data over time
- Export angle measurements as CSV files
- Store patient profiles, videos, and track analysis history

---

## Requirements

### For End Users
- Android 7.0 (API level 24) or higher
- Camera, Storage, and Media access permissions
- Sufficient storage for videos and analysis data

### For Developers
- Android Studio (latest stable version)
- Java 8 or higher
- Gradle (included via Gradle Wrapper)
- Android SDK (API level 24-34)

---

## Technology Stack

### Core Technologies
- Kotlin
- Android XML with View Binding and Data Binding
- MVVM with Repository pattern
- Room Database (SQLite)

### Machine Learning & Computer Vision
- Google ML Kit Pose Detection
- TensorFlow Lite (autoencoder model: `encoder_model.tflite`)

### Libraries
- MPAndroidChart (Apache 2.0)
- Coroutines
- Lifecycle Components

---

## Installation

### For End Users (APK Installation)

1. Transfer the `.apk` file to your Android-compatible device
2. Open the `.apk` file using a file manager
3. Click "Install" and allow installation from unknown sources if prompted
4. Grant all required permissions when prompted

### For Developers

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd Capstone---1-Project
   ```

2. Open the project in Android Studio

3. Sync Gradle dependencies (Android Studio will do this automatically)

4. Build the project:
   ```bash
   ./gradlew build
   ```

5. Run on an emulator or connected device:
   ```bash
   ./gradlew installDebug
   ```
   Or use the provided scripts:
   - Windows: `run_app.bat` or `run_app.ps1`
   - Linux/Mac: `./gradlew installDebug`

---

## Usage

### Basic Workflow

1. Create or select a patient profile with demographic information

2. Select or record video:
   - Click "Record Video" to capture a new video, or
   - Click "Select Video" to choose an existing video from storage

3. Record walking pattern:
   - Have the participant walk normally
   - Ensure at least 2 complete gait cycles (approximately 5 seconds)
   - Record from a side view for best results

4. Perform analysis:
   - Click "Perform Analysis" to process the video
   - Wait for analysis to complete

5. View and export results:
   - Review the annotated video showing angles at each timepoint
   - Click "View Analysis" to see detailed results and graphs
   - Export CSV file containing all angle measurements

---

## Acknowledgments

Special thanks to:

- Guna Sindhuja Siripurapu
- Dr. Rita Patterson
- Dr. Mark Albert
- University of North Texas

---

## References

- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) by Phil Jay (Apache 2.0 License)
- [Google ML Kit Pose Detection](https://developers.google.com/ml-kit/vision/pose-detection)
- [TensorFlow Lite](https://www.tensorflow.org/lite)
- [Android Room](https://developer.android.com/training/data-storage/room)

---

_Last Updated: Dec 2025_
