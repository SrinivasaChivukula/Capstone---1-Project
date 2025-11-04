#!/usr/bin/env python3
"""
Extract 9-feature vectors from GaitVision CSV exports.

This script processes CSV files exported from the GaitVision app and
extracts the same 9 features used by the autoencoder:
1. Left Knee Min
2. Left Knee Max
3. Right Knee Min
4. Right Knee Max
5. Torso Min
6. Torso Max
7. Stride Length Average
8. Left Knee Range
9. Right Knee Range
"""

import numpy as np
import pandas as pd
import glob
import os
from pathlib import Path


def find_local_min(angles):
    """
    Find local minima in angle time series.
    Mirrors the FindLocalMin() function from AngleCalculations.kt
    """
    local_mins = []
    for i in range(1, len(angles) - 1):
        if angles[i] < angles[i-1] and angles[i] < angles[i+1]:
            local_mins.append(angles[i])
    
    return np.array(local_mins) if local_mins else np.array([angles.min()])


def find_local_max(angles):
    """
    Find local maxima in angle time series.
    Mirrors the FindLocalMax() function from AngleCalculations.kt
    """
    local_maxs = []
    for i in range(1, len(angles) - 1):
        if angles[i] > angles[i-1] and angles[i] > angles[i+1]:
            local_maxs.append(angles[i])
    
    return np.array(local_maxs) if local_maxs else np.array([angles.max()])


def extract_features_from_participant(participant_id, csv_folder):
    """
    Extract 9 features from a participant's CSV files.
    
    Expected CSV files per participant:
    - {participant_id}_LeftKnee.csv      ← USED
    - {participant_id}_RightKnee.csv     ← USED
    - {participant_id}_Torso.csv         ← USED
    - {participant_id}_metadata.csv      ← Contains pre-calculated stride length
    - Plus: LeftHip, RightHip, LeftAnkle, RightAnkle, Stride (exported but not needed here)
    
    Returns:
        np.array: 9-element feature vector
    """
    try:
        # Load the CSV files used for features
        left_knee_df = pd.read_csv(f"{csv_folder}/{participant_id}_LeftKnee.csv")
        right_knee_df = pd.read_csv(f"{csv_folder}/{participant_id}_RightKnee.csv")
        torso_df = pd.read_csv(f"{csv_folder}/{participant_id}_Torso.csv")
        
        # Load metadata - contains stride length already calculated by the app!
        metadata_df = pd.read_csv(f"{csv_folder}/{participant_id}_metadata.csv")
        stride_length_avg = float(metadata_df[metadata_df['Field'] == 'StrideLengthAvg']['Value'].values[0])
        
        # Extract angle arrays
        left_knee_angles = left_knee_df['Angle'].values
        right_knee_angles = right_knee_df['Angle'].values
        torso_angles = torso_df['Angle'].values
        
        # Find local min/max
        left_knee_mins = find_local_min(left_knee_angles)
        left_knee_maxs = find_local_max(left_knee_angles)
        right_knee_mins = find_local_min(right_knee_angles)
        right_knee_maxs = find_local_max(right_knee_angles)
        torso_mins = find_local_min(torso_angles)
        torso_maxs = find_local_max(torso_angles)
        
        # Calculate averages
        left_knee_min_avg = left_knee_mins.mean()
        left_knee_max_avg = left_knee_maxs.mean()
        right_knee_min_avg = right_knee_mins.mean()
        right_knee_max_avg = right_knee_maxs.mean()
        torso_min_avg = torso_mins.mean()
        torso_max_avg = torso_maxs.mean()
        
        # Stride length - use the value already calculated by the app
        # (No need to recalculate - the app already did it correctly!)
        
        # Calculate ranges
        left_knee_range = left_knee_max_avg - left_knee_min_avg
        right_knee_range = right_knee_max_avg - right_knee_min_avg
        
        # Construct feature vector (same order as app)
        features = np.array([
            left_knee_min_avg,
            left_knee_max_avg,
            right_knee_min_avg,
            right_knee_max_avg,
            torso_min_avg,
            torso_max_avg,
            stride_length_avg,
            left_knee_range,
            right_knee_range
        ])
        
        return features
    
    except FileNotFoundError as e:
        print(f"  ⚠️  Missing CSV file for {participant_id}: {e}")
        return None
    except Exception as e:
        print(f"  ❌ Error processing {participant_id}: {e}")
        return None


def main():
    print("=" * 60)
    print("GaitVision Feature Extraction")
    print("=" * 60)
    
    # Setup paths - can be customized via command line
    import sys
    if len(sys.argv) >= 3:
        clean_folder = sys.argv[1]
        impaired_folder = sys.argv[2]
        print(f"Using custom paths: {clean_folder}, {impaired_folder}")
    else:
        # Default paths
        clean_folder = "data/clean_csvs"
        impaired_folder = "data/impaired_csvs"
        print("Using default paths (data/clean_csvs, data/impaired_csvs)")
        print("To use custom paths: python extract_features.py <clean_folder> <impaired_folder>")
    
    output_folder = "data/processed"
    
    os.makedirs(output_folder, exist_ok=True)
    
    # Find all participant IDs
    clean_participants = set()
    impaired_participants = set()
    
    for file in glob.glob(f"{clean_folder}/*_LeftKnee.csv"):
        pid = Path(file).stem.replace("_LeftKnee", "")
        clean_participants.add(pid)
    
    for file in glob.glob(f"{impaired_folder}/*_LeftKnee.csv"):
        pid = Path(file).stem.replace("_LeftKnee", "")
        impaired_participants.add(pid)
    
    print(f"\nFound participants:")
    print(f"  Clean: {len(clean_participants)}")
    print(f"  Impaired: {len(impaired_participants)}")
    
    if len(clean_participants) == 0 or len(impaired_participants) == 0:
        print("\n❌ Error: No participants found!")
        print("   Make sure CSV files are in:")
        print(f"   - {clean_folder}/")
        print(f"   - {impaired_folder}/")
        print("\n   Required files per participant (minimum 4):")
        print("   - ParticipantID_LeftKnee.csv")
        print("   - ParticipantID_RightKnee.csv")
        print("   - ParticipantID_Torso.csv")
        print("   - ParticipantID_metadata.csv (NEW! contains stride)")
        print("\n   App exports these too (not needed for training):")
        print("   - ParticipantID_LeftHip.csv, RightHip.csv")
        print("   - ParticipantID_LeftAnkle.csv, RightAnkle.csv")
        print("   - ParticipantID_Stride.csv")
        return
    
    # Extract features
    print("\nExtracting features from clean gait participants...")
    clean_features = []
    for pid in sorted(clean_participants):
        features = extract_features_from_participant(pid, clean_folder)
        if features is not None:
            clean_features.append(features)
            stride_val = features[6]  # Stride is feature #7 (index 6)
            print(f"  ✅ {pid} (stride: {stride_val:.3f}m)")
    
    print(f"\nExtracting features from impaired gait participants...")
    impaired_features = []
    for pid in sorted(impaired_participants):
        features = extract_features_from_participant(pid, impaired_folder)
        if features is not None:
            impaired_features.append(features)
            stride_val = features[6]  # Stride is feature #7 (index 6)
            print(f"  ✅ {pid} (stride: {stride_val:.3f}m)")
    
    # Combine into training dataset
    X_clean = np.array(clean_features)
    X_impaired = np.array(impaired_features)
    X_train = np.vstack([X_clean, X_impaired])
    y_train = np.array([0] * len(X_clean) + [1] * len(X_impaired))
    
    # Save
    np.save(f"{output_folder}/training_features.npy", X_train)
    np.save(f"{output_folder}/training_labels.npy", y_train)
    
    print("\n" + "=" * 60)
    print("✅ Feature extraction complete!")
    print(f"   Total samples: {len(X_train)}")
    print(f"   Clean: {len(X_clean)}, Impaired: {len(X_impaired)}")
    print(f"   Feature shape: {X_train.shape}")
    print(f"\n   Saved to:")
    print(f"   - {output_folder}/training_features.npy")
    print(f"   - {output_folder}/training_labels.npy")
    print("\n   Next step: Run train_pca.py")
    print("=" * 60)


if __name__ == "__main__":
    main()

