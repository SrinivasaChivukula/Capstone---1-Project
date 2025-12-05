# PCA Analysis Mode - Design Document

**Branch:** `feature/pca-analysis-mode`  
**Date:** October 29, 2025  
**Status:** Planning Phase  
**Purpose:** Educational comparison of PCA vs Autoencoder for gait analysis

---

## Table of Contents

1. [Overview](#overview)
2. [Motivation](#motivation)
3. [Current Architecture Analysis](#current-architecture-analysis)
4. [Proposed Design](#proposed-design)
5. [Implementation Plan](#implementation-plan)
6. [Technical Specifications](#technical-specifications)
7. [Testing Strategy](#testing-strategy)
8. [Comparison Metrics](#comparison-metrics)
9. [Risk Assessment](#risk-assessment)
10. [Future Considerations](#future-considerations)

---

## 1. Overview

This design adds a parallel **PCA (Principal Component Analysis) analysis mode** to GaitVision for educational purposes. The goal is to compare traditional linear dimensionality reduction (PCA) against the existing neural autoencoder approach, allowing users to understand the trade-offs between interpretability and model expressiveness.

**Key Principle:** The PCA mode will run in parallel with the autoencoder, sharing all preprocessing pipelines while using different dimensionality reduction techniques.

---

## 2. Motivation

### Educational Goals
- Demonstrate differences between linear (PCA) and non-linear (autoencoder) dimensionality reduction
- Provide comparative analysis capabilities for academic research
- Allow students/researchers to understand when simpler methods suffice

### Technical Goals
- Maintain existing autoencoder functionality (no breaking changes)
- Share data preprocessing pipelines (DRY principle)
- Enable side-by-side comparison of results
- Keep implementation lightweight and maintainable

### Non-Goals
- Replace the autoencoder (it likely performs better)
- Add complex UI features beyond a simple toggle
- Support real-time model switching during video processing

---

## 3. Current Architecture Analysis

### 3.1 Pipeline Overview

```
Video/Camera Input
    ↓
MLKit Pose Detection (imageProcessing.kt)
    ↓
Angle Extraction per Frame (drawOnBitmap)
    ├─ Left/Right Knee Angles
    ├─ Left/Right Hip Angles
    ├─ Left/Right Ankle Angles
    ├─ Torso Angles
    └─ Stride Angles
    ↓
Smoothing (smoothDataUsingMovingAverage)
    ↓
Feature Engineering (SecondActivity.kt)
    ├─ Find Local Min/Max for each angle
    └─ Calculate stride length
    ↓
Feature Vector Construction (LastActivity.kt:121-131)
    [9 features: knee min/max, torso min/max, stride avg, knee ranges]
    ↓
Z-Score Normalization (LastActivity.kt:150-152)
    Using scaler_mean.bin and scaler_scale.bin
    ↓
⚡ AUTOENCODER INFERENCE ⚡ (LastActivity.kt:159)
    encoder_model.tflite: 9D → 2D latent space
    ↓
Centroid Distance Classification (LastActivity.kt:171-179)
    Compare to clean_centroid.npy and impaired_centroid.npy
    ↓
Gait Score Calculation (0-100 scale)
    Score = 100 × [1 - (distClean / (distClean + distImpaired))]
    ↓
Visualization & Export
    ├─ Display gait score
    ├─ Plot angle graphs
    └─ Export CSV files
```

### 3.2 Critical Integration Point

**File:** `LastActivity.kt`, lines 121-190  
**Function:** `onCreate()`

This is where the 9-feature vector is created and processed:

```kotlin
// Feature vector (9 dimensions)
val inputData = floatArrayOf(
    leftKneeMinAngles.average().toFloat(),     // 1
    leftKneeMaxAngles.average().toFloat(),     // 2
    rightKneeMinAngles.average().toFloat(),    // 3
    rightKneeMaxAngles.average().toFloat(),    // 4
    torsoMinAngles.average().toFloat(),        // 5
    torsoMaxAngles.average().toFloat(),        // 6
    calcStrideLengthAvg(participantHeight.toFloat()*39.37F),  // 7
    leftKneeMaxAngles.average().toFloat() - leftKneeMinAngles.average().toFloat(),  // 8
    rightKneeMaxAngles.average().toFloat() - rightKneeMinAngles.average().toFloat() // 9
)

// Standardization
val scaledInput = FloatArray(inputData.size) { i ->
    (inputData[i] - scalerMean[i]) / safeScalerScale[i]
}

// Autoencoder inference (THIS IS WHERE PCA WOULD HOOK IN)
val output = Array(1){FloatArray(2)}
val input = arrayOf(scaledInput)
interpreter.run(input, output)

// Distance to centroids
val distClean = euclideanDistance(output[0], cleanCentroid)
val distImpaired = euclideanDistance(output[0], impairedCentroid)
```

### 3.3 Component Reusability Matrix

| Component | File | Reuse Status | Notes |
|-----------|------|--------------|-------|
| **Pose Detection** | `imageProcessing.kt` (lines 94-213) | ✅ Fully Reusable | MLKit pose landmark extraction |
| **Angle Calculation** | `AngleCalculations.kt` | ✅ Fully Reusable | Law of cosines for joint angles |
| **Parameter Functions** | `Parameter Functions.kt` | ✅ Fully Reusable | Stride calculations, smoothing |
| **Feature Engineering** | `SecondActivity.kt` (lines 398-417) | ✅ Fully Reusable | Local min/max extraction |
| **Standardization** | `LastActivity.kt` (scaler load) | ⚙️ Shared | Same scaler for both methods |
| **Autoencoder Inference** | `LastActivity.kt` (line 159) | ❌ Replaced in PCA mode | TFLite interpreter |
| **Centroid Files** | `clean_centroid.npy`, `impaired_centroid.npy` | ⚙️ Need PCA versions | Different latent spaces |
| **Distance Calculation** | `LastActivity.kt` (euclideanDistance) | ✅ Fully Reusable | Same distance metric |
| **Scoring Logic** | `LastActivity.kt` (lines 177-179) | ✅ Fully Reusable | Same scoring formula |
| **Visualization** | `plotLineGraph()` | ✅ Fully Reusable | MPAndroidChart graphs |
| **CSV Export** | `LastActivity.kt` (writeToFile) | ✅ Fully Reusable | File I/O logic |

---

## 4. Proposed Design

### 4.1 Architecture Changes

```
                    [9D Feature Vector]
                            |
                    [Z-Score Normalize]
                            |
                    ┌───────┴───────┐
                    |               |
            [Autoencoder]      [PCA Matrix]
             (existing)          (new)
                    |               |
                [2D Latent Space]   |
                    |               |
                    └───────┬───────┘
                            |
                [Distance to Centroids]
                            |
                    [Gait Score 0-100]
```

### 4.2 New Components

#### 4.2.1 New Asset Files
- `pca_components.bin` - PCA transformation matrix (9×2 = 18 floats = 72 bytes)
- `clean_centroid_pca.bin` - Clean gait centroid in PCA space (2 floats = 8 bytes)
- `impaired_centroid_pca.bin` - Impaired gait centroid in PCA space (2 floats = 8 bytes)

#### 4.2.2 New Kotlin Functions

**Location:** `LastActivity.kt`

```kotlin
/**
 * Performs PCA transformation using pre-computed principal components.
 * 
 * @param scaledInput The z-score normalized input features (9D)
 * @return The 2D projection in PCA space
 */
private fun pcaTransform(scaledInput: FloatArray): FloatArray {
    val pcaComponents = loadPCAMatrix(this, "pca_components.bin")
    val output = FloatArray(2)
    
    // Matrix multiplication: (1×9) × (9×2) = (1×2)
    for (i in 0..1) {  // For each principal component
        output[i] = 0f
        for (j in 0..8) {  // For each input feature
            output[i] += scaledInput[j] * pcaComponents[j * 2 + i]
        }
    }
    
    return output
}

/**
 * Loads PCA transformation matrix from binary file.
 * Matrix is stored in row-major order (9×2).
 */
private fun loadPCAMatrix(context: Context, filename: String): FloatArray {
    val inputStream = context.assets.open(filename)
    val bytes = inputStream.readBytes()
    inputStream.close()
    
    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    val numFloats = bytes.size / 4  // Should be 18
    
    return FloatArray(numFloats) { buffer.float }
}
```

#### 4.2.3 UI Changes

**File:** `app/src/main/res/layout/activity_last.xml`

Add a toggle switch/button:

```xml
<Switch
    android:id="@+id/pca_mode_switch"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Use PCA Analysis"
    android:checked="false"
    ... />
```

#### 4.2.4 Modified Logic in LastActivity.kt

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_last)
    
    val pcaModeSwitch = findViewById<Switch>(R.id.pca_mode_switch)
    
    // Feature extraction (UNCHANGED)
    val inputData = floatArrayOf(
        leftKneeMinAngles.average().toFloat(),
        // ... (same as before)
    )
    
    // Normalization (UNCHANGED)
    val scalerMean = loadFloatBinFile(this, "scaler_mean.bin")
    val scalerScale = loadFloatBinFile(this, "scaler_scale.bin")
    val scaledInput = FloatArray(inputData.size) { i ->
        (inputData[i] - scalerMean[i]) / safeScalerScale[i]
    }
    
    // *** NEW: Branch based on mode ***
    val analysisMethod: String
    val output2D: FloatArray
    val cleanCentroid: FloatArray
    val impairedCentroid: FloatArray
    
    if (pcaModeSwitch.isChecked) {
        // PCA mode
        analysisMethod = "PCA"
        output2D = pcaTransform(scaledInput)
        cleanCentroid = loadFloatBinFile(this, "clean_centroid_pca.bin")
        impairedCentroid = loadFloatBinFile(this, "impaired_centroid_pca.bin")
    } else {
        // Autoencoder mode (existing)
        analysisMethod = "Autoencoder"
        val tfliteModel = FileUtil.loadMappedFile(this, "encoder_model.tflite")
        val interpreter = Interpreter(tfliteModel)
        val output = Array(1){FloatArray(2)}
        interpreter.run(arrayOf(scaledInput), output)
        output2D = output[0]
        cleanCentroid = loadNpyFloatArray(assets.open("clean_centroid.npy"))
        impairedCentroid = loadNpyFloatArray(assets.open("impaired_centroid.npy"))
    }
    
    // Distance calculation (UNCHANGED)
    val distClean = euclideanDistance(output2D, cleanCentroid)
    val distImpaired = euclideanDistance(output2D, impairedCentroid)
    
    // Scoring (UNCHANGED)
    val gaitIndexUnscaled = 1 - (distClean / (distClean + distImpaired))
    val gaitIndexScaled = gaitIndexUnscaled * 100
    
    // Logging (ENHANCED)
    Log.d("GaitAnalysis", "Method: $analysisMethod")
    Log.d("GaitAnalysis", "2D Output: ${output2D.contentToString()}")
    Log.d("GaitAnalysis", "Distances: Clean=$distClean, Impaired=$distImpaired")
    
    // UI update (UNCHANGED)
    scoreTextView.text = gaitIndexScaled.roundToLong().toString()
    
    // ... rest of visualization code unchanged
}
```

### 4.3 Offline PCA Training Pipeline

**Language:** Python (NumPy, scikit-learn)  
**Location:** External (not in repo, for research use)

```python
import numpy as np
from sklearn.decomposition import PCA
from sklearn.preprocessing import StandardScaler

# Step 1: Collect training data
# Export CSVs from GaitVision app for multiple participants
# Manually extract 9 features per participant

# Example data structure:
# X_train shape: (N_samples, 9)
# y_train shape: (N_samples,) where 0=clean, 1=impaired

X_train = np.array([...])  # Load from exported data
y_train = np.array([...])  # Labels

# Step 2: Fit StandardScaler (should match existing scaler)
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X_train)

# Verify scaler matches existing
print("Scaler mean:", scaler.mean_)
print("Scaler scale:", scaler.scale_)

# Step 3: Fit PCA with 2 components
pca = PCA(n_components=2)
X_pca = pca.fit_transform(X_scaled)

print(f"Explained variance ratio: {pca.explained_variance_ratio_}")
print(f"Total variance explained: {pca.explained_variance_ratio_.sum():.2%}")

# Step 4: Compute centroids in PCA space
clean_mask = (y_train == 0)
impaired_mask = (y_train == 1)

clean_centroid_pca = X_pca[clean_mask].mean(axis=0)
impaired_centroid_pca = X_pca[impaired_mask].mean(axis=0)

print(f"Clean centroid: {clean_centroid_pca}")
print(f"Impaired centroid: {impaired_centroid_pca}")

# Step 5: Export to binary files for Android
# PCA components matrix (9×2, stored as row-major)
pca.components_.T.astype(np.float32).tofile('pca_components.bin')

# Centroids (2D points)
clean_centroid_pca.astype(np.float32).tofile('clean_centroid_pca.bin')
impaired_centroid_pca.astype(np.float32).tofile('impaired_centroid_pca.bin')

print("\n✅ PCA files exported successfully!")
print(f"   pca_components.bin: {pca.components_.T.size * 4} bytes")
print(f"   clean_centroid_pca.bin: {clean_centroid_pca.size * 4} bytes")
print(f"   impaired_centroid_pca.bin: {impaired_centroid_pca.size * 4} bytes")
```

---

## 5. Implementation Plan

### Phase 1: Offline PCA Training (External)
**Estimated Time:** 1-2 days (depends on data collection)

- [ ] Export training data from existing GaitVision app runs
- [ ] Create Python script to extract 9-feature vectors
- [ ] Fit PCA model (verify explained variance ≥ 80%)
- [ ] Compute centroids in PCA space
- [ ] Export binary files (`pca_components.bin`, centroids)
- [ ] Validate binary file formats (little-endian floats)

**Deliverables:**
- `pca_training.py` (Python script)
- `pca_components.bin` (72 bytes)
- `clean_centroid_pca.bin` (8 bytes)
- `impaired_centroid_pca.bin` (8 bytes)

---

### Phase 2: Android Code Implementation
**Estimated Time:** 2-3 days

#### 2.1: Add New Functions (Day 1)
- [ ] Implement `pcaTransform()` in `LastActivity.kt`
- [ ] Implement `loadPCAMatrix()` in `LastActivity.kt`
- [ ] Add unit tests for matrix multiplication
- [ ] Verify output dimensions (9D → 2D)

#### 2.2: Integrate Mode Toggle (Day 1-2)
- [ ] Add Switch UI element to `activity_last.xml`
- [ ] Modify `onCreate()` to branch based on switch state
- [ ] Update centroid loading logic
- [ ] Test both modes independently

#### 2.3: Enhanced Logging & Export (Day 2)
- [ ] Add analysis method to logs
- [ ] Create new CSV export for comparison results
- [ ] Include 2D coordinates in export
- [ ] Add metadata (method, timestamp, participant ID)

#### 2.4: Documentation & Comments (Day 2-3)
- [ ] Add KDoc comments to new functions
- [ ] Update README with PCA mode instructions
- [ ] Document binary file formats
- [ ] Create user guide for comparison workflow

**Deliverables:**
- Modified `LastActivity.kt`
- Modified `activity_last.xml`
- New CSV export format
- Updated documentation

---

### Phase 3: Testing & Validation
**Estimated Time:** 2-3 days

#### 3.1: Unit Tests
- [ ] Test matrix multiplication correctness
- [ ] Test binary file loading
- [ ] Test mode switching
- [ ] Test with edge cases (NaN, zero variance)

#### 3.2: Integration Tests
- [ ] Run both modes on same video
- [ ] Verify output consistency (deterministic)
- [ ] Check UI updates correctly
- [ ] Validate CSV exports

#### 3.3: Comparison Study
- [ ] Collect 10+ test videos (clean + impaired)
- [ ] Run both analysis modes
- [ ] Plot scatter: Autoencoder score vs PCA score
- [ ] Analyze divergence cases (where do they differ?)
- [ ] Document findings

**Deliverables:**
- Test results report
- Comparison analysis (plots, statistics)
- Performance metrics (inference time)

---

### Phase 4: Code Review & Merge
**Estimated Time:** 1 day

- [ ] Self-review all changes
- [ ] Ensure no breaking changes to autoencoder mode
- [ ] Verify .gitignore updated
- [ ] Clean up debug logs
- [ ] Prepare PR description with before/after examples

---

## 6. Technical Specifications

### 6.1 File Formats

#### PCA Components Matrix (`pca_components.bin`)
- **Format:** Binary, little-endian
- **Data Type:** 32-bit float (IEEE 754)
- **Shape:** 9×2 matrix, stored row-major
- **Size:** 72 bytes (18 floats × 4 bytes)
- **Layout:**
  ```
  [PC1_feature0, PC2_feature0,  // Row 0
   PC1_feature1, PC2_feature1,  // Row 1
   ...
   PC1_feature8, PC2_feature8]  // Row 8
  ```

#### Centroid Files (`*_centroid_pca.bin`)
- **Format:** Binary, little-endian
- **Data Type:** 32-bit float
- **Shape:** 1×2 vector
- **Size:** 8 bytes (2 floats × 4 bytes)
- **Layout:** `[x_coordinate, y_coordinate]`

### 6.2 Algorithm Specifications

#### PCA Transformation
- **Input:** 9D feature vector (z-score normalized)
- **Output:** 2D latent space coordinates
- **Operation:** Matrix multiplication `y = X × W`
  - `X`: (1×9) input vector
  - `W`: (9×2) PCA components
  - `y`: (1×2) output vector

#### Centroid Distance Classification
- **Metric:** Euclidean distance (L2 norm)
- **Formula:** `dist = √(Σ(y_i - c_i)²)`
- **Score:** `gait_index = 100 × [1 - (d_clean / (d_clean + d_impaired))]`
- **Range:** [0, 100] where higher = more impaired

### 6.3 Performance Requirements

| Metric | Target | Notes |
|--------|--------|-------|
| PCA inference time | < 1ms | Simple matrix multiplication |
| Memory overhead | < 100 bytes | Only storing 3 small binary files |
| UI responsiveness | No lag | Mode switch should be instant |
| Accuracy vs Autoencoder | Document difference | Not a requirement, just for comparison |

---

## 7. Testing Strategy

### 7.1 Unit Tests

```kotlin
@Test
fun testPCATransform_correctDimensions() {
    val input = FloatArray(9) { it.toFloat() }
    val output = pcaTransform(input)
    assertEquals(2, output.size)
}

@Test
fun testPCATransform_deterministic() {
    val input = floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
    val output1 = pcaTransform(input)
    val output2 = pcaTransform(input)
    assertArrayEquals(output1, output2, 1e-6f)
}

@Test
fun testMatrixMultiplication_manualVerification() {
    // Create simple test case with known output
    val input = floatArrayOf(1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    val components = floatArrayOf(
        2f, 3f,  // First row
        0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f
    )
    // Expected: [2, 3]
    val output = pcaTransform(input)
    assertEquals(2f, output[0], 1e-6f)
    assertEquals(3f, output[1], 1e-6f)
}
```

### 7.2 Integration Tests

**Test Scenarios:**
1. Switch from Autoencoder → PCA mid-session
2. Export CSV with both methods
3. Load same video, run both analyses
4. Verify consistent UI updates

### 7.3 Comparison Validation

**Validation Checklist:**
- [ ] Both modes produce scores in [0, 100]
- [ ] PCA explained variance > 70% (documented)
- [ ] Centroid separation is significant
- [ ] Scores correlate positively (but may diverge)
- [ ] PCA is faster than autoencoder

---

## 8. Comparison Metrics

### 8.1 Quantitative Metrics

| Metric | Autoencoder | PCA | How to Measure |
|--------|-------------|-----|----------------|
| Inference Time | ~5-10ms | ~0.1ms | `System.nanoTime()` |
| Model Size | 50KB (TFLite) | 72 bytes | File size |
| Memory Usage | ~1MB (TF runtime) | ~100 bytes | Android Profiler |
| Explained Variance | N/A | ~70-85% | From training |
| Classification Accuracy | Baseline | To measure | On test set |

### 8.2 Qualitative Metrics

- **Interpretability:** PCA components can be analyzed (feature importance)
- **Robustness:** Test with noisy data, outliers
- **Generalization:** Test with participants not in training set
- **Clinical Utility:** Which method aligns better with expert assessment?

### 8.3 Comparison Report Template

```markdown
## Gait Analysis Comparison: Autoencoder vs PCA

### Test Case: Participant XYZ

**Input Features:**
- Left Knee Min/Max: [30.5, 120.2]
- Right Knee Min/Max: [32.1, 118.5]
- Torso Min/Max: [-5.2, 8.1]
- Stride Length Avg: 1.45m
- Left Knee Range: 89.7
- Right Knee Range: 86.4

**Results:**

| Method | 2D Coords | Dist Clean | Dist Impaired | Gait Score |
|--------|-----------|------------|---------------|------------|
| Autoencoder | [0.12, -0.34] | 0.45 | 0.89 | 33.6 |
| PCA | [0.08, -0.28] | 0.52 | 0.81 | 39.3 |

**Interpretation:**
- Both methods classify as impaired (score < 50)
- PCA is slightly more conservative (higher score)
- Difference: 5.7 points (moderate divergence)

**Clinical Assessment:** [To be filled by expert]
```

---

## 9. Risk Assessment

### 9.1 Technical Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| PCA training data insufficient | Medium | High | Collect 50+ samples before starting |
| Matrix multiplication bugs | Low | High | Comprehensive unit tests, manual verification |
| Binary file format mismatch | Low | Medium | Document format, add validation checks |
| Performance regression | Low | Low | Benchmark both modes before merge |
| UI confusion (which mode active?) | Medium | Low | Clear labeling, persistent indicator |

### 9.2 User Experience Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Users expect same results | High | Medium | Clear documentation that this is for comparison |
| Confusion about which mode to use | Medium | Low | Default to autoencoder, label PCA as "experimental" |
| Incorrect interpretation of differences | Medium | Medium | Provide educational materials on PCA vs NN |

### 9.3 Project Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Feature creep (adding more modes) | Low | Medium | Keep scope limited to PCA only |
| Merge conflicts | Low | Low | Frequent rebases from main |
| Breaking changes to autoencoder | Very Low | High | Extensive testing before merge |

---

## 10. Future Considerations

### 10.1 Potential Extensions

1. **Additional Dimensionality Reduction Methods**
   - t-SNE (non-linear, but computational cost)
   - UMAP (better preservation of global structure)
   - Kernel PCA (non-linear variant)

2. **Interactive Visualization**
   - 2D scatter plot of latent space
   - Show participant's position relative to centroids
   - Overlay decision boundary

3. **Ensemble Methods**
   - Combine autoencoder + PCA predictions
   - Weighted average based on confidence

4. **Model Explainability**
   - PCA component interpretation (feature importance)
   - Autoencoder attention maps
   - SHAP values for both methods

### 10.2 Research Questions

- Does PCA perform better for certain gait patterns?
- What % of variance needs to be explained for clinical utility?
- Can PCA components reveal biomechanical insights?
- Is there a subset of features where PCA is sufficient?

### 10.3 Long-term Vision

This PCA mode serves as a **proof-of-concept** for modular analysis pipelines in GaitVision. Future versions could support:
- Plugin architecture for analysis methods
- Cloud-based model comparison
- Automated A/B testing of different approaches
- Integration with electronic health records

---

## Appendix A: File Modification Checklist

### Files to Modify
- [ ] `Capstone---1-Project/app/src/main/java/GaitVision/com/LastActivity.kt`
- [ ] `Capstone---1-Project/app/src/main/res/layout/activity_last.xml`
- [ ] `Capstone---1-Project/.gitignore`
- [ ] `Capstone---1-Project/README.md` (update with PCA instructions)

### Files to Create
- [ ] `Capstone---1-Project/app/src/main/assets/pca_components.bin`
- [ ] `Capstone---1-Project/app/src/main/assets/clean_centroid_pca.bin`
- [ ] `Capstone---1-Project/app/src/main/assets/impaired_centroid_pca.bin`
- [ ] `Capstone---1-Project/docs/PCA_USAGE.md` (user guide)

### Files to Leave Untouched
- All pose detection code
- All angle calculation code
- All visualization code
- Existing autoencoder assets

---

## Appendix B: Code Review Checklist

- [ ] No hardcoded values (all configurable)
- [ ] Proper error handling (file loading, null checks)
- [ ] KDoc comments on all new functions
- [ ] No breaking changes to existing functionality
- [ ] Consistent code style with existing codebase
- [ ] All TODOs resolved or documented
- [ ] Performance acceptable (no UI lag)
- [ ] Memory leaks checked (asset files closed properly)
- [ ] Logs appropriate (debug level, not verbose)
- [ ] UI/UX consistent with existing design

---

## Appendix C: References

1. **PCA Theory:**
   - Jolliffe, I. T. (2002). *Principal Component Analysis* (2nd ed.). Springer.

2. **Gait Analysis:**
   - Baker, R. (2013). Measuring Walking: A Handbook of Clinical Gait Analysis. Mac Keith Press.

3. **Autoencoder vs PCA:**
   - Hinton, G. E., & Salakhutdinov, R. R. (2006). "Reducing the Dimensionality of Data with Neural Networks." *Science*, 313(5786), 504-507.

4. **Android Development:**
   - TensorFlow Lite Documentation: https://www.tensorflow.org/lite
   - ML Kit Pose Detection: https://developers.google.com/ml-kit/vision/pose-detection

---

**Document Version:** 1.0  
**Last Updated:** October 29, 2025  
**Author:** AI Assistant (Cursor)  
**Status:** ✅ Ready for Implementation

