#!/usr/bin/env python3
"""
Train PCA model and export binary files for Android integration.

This script:
1. Loads extracted features
2. Fits StandardScaler and PCA
3. Computes centroids in PCA space
4. Exports binary files for Android
5. Creates visualizations
"""

import numpy as np
from sklearn.decomposition import PCA
from sklearn.preprocessing import StandardScaler
import matplotlib.pyplot as plt
import seaborn as sns
import os


def load_training_data():
    """Load extracted features and labels"""
    X_train = np.load('data/processed/training_features.npy')
    y_train = np.load('data/processed/training_labels.npy')
    return X_train, y_train


def train_pca_model(X_train, y_train, n_components=2):
    """
    Train PCA model on gait features.
    
    Returns:
        scaler: Fitted StandardScaler
        pca: Fitted PCA model
        X_pca: Transformed data in PCA space
    """
    # Fit StandardScaler
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X_train)
    
    # Fit PCA
    pca = PCA(n_components=n_components)
    X_pca = pca.fit_transform(X_scaled)
    
    return scaler, pca, X_pca


def compute_centroids(X_pca, y_train):
    """Compute class centroids in PCA space"""
    clean_mask = (y_train == 0)
    impaired_mask = (y_train == 1)
    
    X_pca_clean = X_pca[clean_mask]
    X_pca_impaired = X_pca[impaired_mask]
    
    clean_centroid = X_pca_clean.mean(axis=0)
    impaired_centroid = X_pca_impaired.mean(axis=0)
    
    return clean_centroid, impaired_centroid


def export_binary_files(pca, scaler, clean_centroid, impaired_centroid, output_dir='models'):
    """Export PCA model files in Android-compatible format"""
    os.makedirs(output_dir, exist_ok=True)
    
    # PCA components matrix (9x2, row-major)
    # sklearn's pca.components_ is (n_components, n_features), we need (n_features, n_components)
    pca_components = pca.components_.T  # Transpose to (9, 2)
    pca_components.astype(np.float32).tofile(f'{output_dir}/pca_components.bin')
    
    # Centroids (2D vectors)
    clean_centroid.astype(np.float32).tofile(f'{output_dir}/clean_centroid_pca.bin')
    impaired_centroid.astype(np.float32).tofile(f'{output_dir}/impaired_centroid_pca.bin')
    
    # Export scaler parameters (needed for PCA normalization)
    # Note: For fair comparison, you may want to use autoencoder's scaler instead
    scaler.mean_.astype(np.float32).tofile(f'{output_dir}/scaler_mean_pca.bin')
    scaler.scale_.astype(np.float32).tofile(f'{output_dir}/scaler_scale_pca.bin')
    
    return {
        'components': f'{output_dir}/pca_components.bin',
        'clean': f'{output_dir}/clean_centroid_pca.bin',
        'impaired': f'{output_dir}/impaired_centroid_pca.bin',
        'scaler_mean': f'{output_dir}/scaler_mean_pca.bin',
        'scaler_scale': f'{output_dir}/scaler_scale_pca.bin'
    }


def validate_exports(pca, scaler, clean_centroid, impaired_centroid, output_dir='models'):
    """Validate exported binary files can be read correctly"""
    # Load files
    loaded_components = np.fromfile(f'{output_dir}/pca_components.bin', 
                                   dtype=np.float32).reshape(9, 2)
    loaded_clean = np.fromfile(f'{output_dir}/clean_centroid_pca.bin', 
                               dtype=np.float32)
    loaded_impaired = np.fromfile(f'{output_dir}/impaired_centroid_pca.bin', 
                                  dtype=np.float32)
    loaded_mean = np.fromfile(f'{output_dir}/scaler_mean_pca.bin', dtype=np.float32)
    loaded_scale = np.fromfile(f'{output_dir}/scaler_scale_pca.bin', dtype=np.float32)
    
    # Verify shapes
    assert loaded_components.shape == (9, 2), "Components shape mismatch"
    assert loaded_clean.shape == (2,), "Clean centroid shape mismatch"
    assert loaded_impaired.shape == (2,), "Impaired centroid shape mismatch"
    assert loaded_mean.shape == (9,), "Scaler mean shape mismatch"
    assert loaded_scale.shape == (9,), "Scaler scale shape mismatch"
    
    # Verify values match
    assert np.allclose(pca.components_.T, loaded_components), "Components value mismatch"
    assert np.allclose(clean_centroid, loaded_clean), "Clean centroid mismatch"
    assert np.allclose(impaired_centroid, loaded_impaired), "Impaired centroid mismatch"
    assert np.allclose(scaler.mean_, loaded_mean), "Scaler mean mismatch"
    assert np.allclose(scaler.scale_, loaded_scale), "Scaler scale mismatch"
    
    # Test transformation with loaded scaler
    X_train, _ = load_training_data()
    test_sample = X_train[0]
    
    # Manual normalization using loaded scaler
    test_scaled = (test_sample - loaded_mean) / loaded_scale
    
    # Manual PCA transformation using loaded components
    manual_result = test_scaled @ loaded_components
    
    # sklearn transformation (using fitted scaler and pca)
    sklearn_scaled = scaler.transform(test_sample.reshape(1, -1))
    sklearn_result = pca.transform(sklearn_scaled)[0]
    
    assert np.allclose(manual_result, sklearn_result, rtol=1e-4), "Transformation test failed"
    
    return True


def create_visualizations(X_pca, y_train, pca, clean_centroid, impaired_centroid, 
                         output_dir='visualizations'):
    """Create PCA visualization plots"""
    os.makedirs(output_dir, exist_ok=True)
    
    # Separate classes
    clean_mask = (y_train == 0)
    impaired_mask = (y_train == 1)
    
    # Create main scatter plot
    plt.figure(figsize=(12, 8))
    
    # Plot samples
    plt.scatter(X_pca[clean_mask, 0], X_pca[clean_mask, 1], 
               c='green', alpha=0.6, s=100, label='Clean', edgecolors='darkgreen')
    plt.scatter(X_pca[impaired_mask, 0], X_pca[impaired_mask, 1], 
               c='red', alpha=0.6, s=100, label='Impaired', edgecolors='darkred')
    
    # Plot centroids
    plt.scatter(*clean_centroid, c='darkgreen', marker='X', s=500, 
               edgecolors='black', linewidths=2, label='Clean Centroid', zorder=10)
    plt.scatter(*impaired_centroid, c='darkred', marker='X', s=500, 
               edgecolors='black', linewidths=2, label='Impaired Centroid', zorder=10)
    
    # Draw line between centroids
    plt.plot([clean_centroid[0], impaired_centroid[0]], 
            [clean_centroid[1], impaired_centroid[1]], 
            'k--', alpha=0.5, linewidth=2)
    
    # Labels and formatting
    var1, var2 = pca.explained_variance_ratio_
    plt.xlabel(f'PC1 ({var1:.1%} variance explained)', fontsize=12)
    plt.ylabel(f'PC2 ({var2:.1%} variance explained)', fontsize=12)
    plt.title('PCA: Gait Analysis in 2D Latent Space', fontsize=14, fontweight='bold')
    plt.legend(fontsize=10, loc='best')
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    
    plt.savefig(f'{output_dir}/pca_2d_space.png', dpi=150, bbox_inches='tight')
    
    # Create component loadings plot
    plt.figure(figsize=(10, 6))
    feature_names = ['L Knee Min', 'L Knee Max', 'R Knee Min', 'R Knee Max',
                    'Torso Min', 'Torso Max', 'Stride', 'L Range', 'R Range']
    
    x_pos = np.arange(len(feature_names))
    width = 0.35
    
    plt.bar(x_pos - width/2, pca.components_[0], width, label='PC1', alpha=0.8)
    plt.bar(x_pos + width/2, pca.components_[1], width, label='PC2', alpha=0.8)
    
    plt.xlabel('Features', fontsize=12)
    plt.ylabel('Loading', fontsize=12)
    plt.title('PCA Component Loadings', fontsize=14, fontweight='bold')
    plt.xticks(x_pos, feature_names, rotation=45, ha='right')
    plt.legend()
    plt.grid(True, alpha=0.3, axis='y')
    plt.tight_layout()
    
    plt.savefig(f'{output_dir}/pca_loadings.png', dpi=150, bbox_inches='tight')
    print("Visualizations saved to visualizations/")
    
    plt.close('all')


def generate_report(X_train, y_train, pca, clean_centroid, impaired_centroid, 
                   output_dir='visualizations'):
    """Generate text report with PCA analysis results"""
    n_clean = np.sum(y_train == 0)
    n_impaired = np.sum(y_train == 1)
    
    centroid_distance = np.linalg.norm(clean_centroid - impaired_centroid)
    
    report = f"""
================================================================================
GaitVision PCA Training Report
================================================================================

DATASET SUMMARY
---------------
Total samples: {len(X_train)}
  - Clean gait: {n_clean} samples
  - Impaired gait: {n_impaired} samples
  - Class balance: {n_clean/len(X_train):.1%} clean, {n_impaired/len(X_train):.1%} impaired

INPUT FEATURES
--------------
Feature dimension: {X_train.shape[1]}
Features:
  1. Left Knee Min Angle
  2. Left Knee Max Angle
  3. Right Knee Min Angle
  4. Right Knee Max Angle
  5. Torso Min Angle
  6. Torso Max Angle
  7. Stride Length Average
  8. Left Knee Range
  9. Right Knee Range

PCA RESULTS
-----------
Number of components: {pca.n_components_}
Explained variance ratio:
  - PC1: {pca.explained_variance_ratio_[0]:.2%}
  - PC2: {pca.explained_variance_ratio_[1]:.2%}
  - Total: {pca.explained_variance_ratio_.sum():.2%}

CENTROIDS IN PCA SPACE
----------------------
Clean gait centroid: [{clean_centroid[0]:.4f}, {clean_centroid[1]:.4f}]
Impaired gait centroid: [{impaired_centroid[0]:.4f}, {impaired_centroid[1]:.4f}]
Distance between centroids: {centroid_distance:.4f}

EXPORTED FILES
--------------
models/pca_components.bin (72 bytes) - PCA transformation matrix (9×2)
models/clean_centroid_pca.bin (8 bytes) - Clean gait centroid
models/impaired_centroid_pca.bin (8 bytes) - Impaired gait centroid
models/scaler_mean_pca.bin (36 bytes) - Scaler mean (9 features)
models/scaler_scale_pca.bin (36 bytes) - Scaler scale (9 features)

NOTE: For fair comparison with autoencoder, you may want to use the SAME
scaler (scaler_mean.bin, scaler_scale.bin) that the autoencoder uses.
These PCA-specific scaler files are exported for completeness, but you can
choose to use either scaler in your Android implementation.

NEXT STEPS
----------
1. Copy .bin files to Android assets:
   cp models/*.bin ../app/src/main/assets/

2. Build and test the app

================================================================================
"""
    
    report_path = f'{output_dir}/pca_report.txt'
    with open(report_path, 'w', encoding='utf-8') as f:
        f.write(report)
    
    print(f"Report saved to {report_path}")
    
    return report


def main():
    print("GaitVision PCA Model Training")
    print("-" * 40)
    
    # Load data
    X_train, y_train = load_training_data()
    print(f"Loaded {len(X_train)} samples with {X_train.shape[1]} features")
    
    # Train PCA
    scaler, pca, X_pca = train_pca_model(X_train, y_train)
    variance = pca.explained_variance_ratio_.sum()
    print(f"PCA fitted: {pca.n_components_} components, {variance:.1%} variance")
    
    # Compute centroids
    clean_centroid, impaired_centroid = compute_centroids(X_pca, y_train)
    centroid_dist = np.linalg.norm(clean_centroid - impaired_centroid)
    print(f"Centroid separation: {centroid_dist:.2f}")
    
    # Export binary files
    files = export_binary_files(pca, scaler, clean_centroid, impaired_centroid)
    print(f"Exported {len(files)} binary files to models/")
    
    # Validate exports
    validate_exports(pca, scaler, clean_centroid, impaired_centroid)
    
    # Create visualizations
    create_visualizations(X_pca, y_train, pca, clean_centroid, impaired_centroid)
    
    # Generate report
    generate_report(X_train, y_train, pca, clean_centroid, impaired_centroid)
    
    print("-" * 40)
    print("Training complete")


if __name__ == "__main__":
    main()

