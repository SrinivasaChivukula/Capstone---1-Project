# GaitVision Android App Runner
Write-Host "GaitVision Android App Runner" -ForegroundColor Green
Write-Host "============================" -ForegroundColor Green

# Set Android SDK path
$ANDROID_HOME = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
$ADB_PATH = "$ANDROID_HOME\platform-tools-2\adb.exe"

Write-Host "`nChecking for connected devices..." -ForegroundColor Yellow
& $ADB_PATH devices

Write-Host "`nInstalling GaitVision app..." -ForegroundColor Yellow
& $ADB_PATH install -r "app\build\outputs\apk\debug\app-debug.apk"

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nApp installed successfully!" -ForegroundColor Green
    Write-Host "Starting GaitVision..." -ForegroundColor Yellow
    & $ADB_PATH shell am start -n GaitVision.com/.MainActivity
    Write-Host "`nGaitVision app is now running on your device!" -ForegroundColor Green
} else {
    Write-Host "`nInstallation failed. Please make sure:" -ForegroundColor Red
    Write-Host "1. A device is connected and USB debugging is enabled" -ForegroundColor Red
    Write-Host "2. The device is unlocked" -ForegroundColor Red
    Write-Host "3. You have granted permission for USB debugging" -ForegroundColor Red
}

Write-Host "`nPress any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
