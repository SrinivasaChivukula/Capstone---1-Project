#!/usr/bin/env python3
"""
Setup script for GaitVision PCA training environment.
Verifies environment and creates necessary directories.
"""

import sys
import os

def check_python_version():
    version = sys.version_info
    if version.major < 3 or (version.major == 3 and version.minor < 8):
        print(f"ERROR: Python 3.8+ required (found {version.major}.{version.minor})")
        return False
    print(f"Python {version.major}.{version.minor}.{version.micro}")
    return True

def check_packages():
    required = ['numpy', 'pandas', 'sklearn', 'matplotlib']
    missing = []
    
    for package in required:
        try:
            if package == 'sklearn':
                import sklearn
            else:
                __import__(package)
        except ImportError:
            missing.append(package)
    
    if missing:
        print(f"ERROR: Missing packages: {', '.join(missing)}")
        print("Run: pip install -r requirements.txt")
        return False
    
    print("All required packages installed")
    return True

def create_directories():
    dirs = [
        'data/clean_csvs',
        'data/impaired_csvs',
        'data/processed',
        'models',
        'visualizations',
    ]
    
    for d in dirs:
        os.makedirs(d, exist_ok=True)
    
    print("Directory structure created")
    return True

if __name__ == "__main__":
    print("GaitVision PCA Environment Setup")
    print("-" * 40)
    
    checks = [
        check_python_version,
        check_packages,
        create_directories,
    ]
    
    if all(check() for check in checks):
        print("-" * 40)
        print("Setup complete")
    else:
        print("-" * 40)
        print("Setup failed")
        sys.exit(1)

