@echo off
echo GaitVision Android App Runner
echo ============================

REM Set Android SDK path
set ANDROID_HOME=C:\Users\%USERNAME%\AppData\Local\Android\Sdk
set ADB_PATH=%ANDROID_HOME%\platform-tools-2\adb.exe

echo Checking for connected devices...
"%ADB_PATH%" devices

echo.
echo Installing GaitVision app...
"%ADB_PATH%" install -r app\build\outputs\apk\debug\app-debug.apk

if %ERRORLEVEL% EQU 0 (
    echo.
    echo App installed successfully!
    echo Starting GaitVision...
    "%ADB_PATH%" shell am start -n GaitVision.com/.MainActivity
    echo.
    echo GaitVision app is now running on your device!
) else (
    echo.
    echo Installation failed. Please make sure:
    echo 1. A device is connected and USB debugging is enabled
    echo 2. The device is unlocked
    echo 3. You have granted permission for USB debugging
)

pause
