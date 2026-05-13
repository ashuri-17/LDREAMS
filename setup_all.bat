@echo off
echo === LDREAMS Full Setup ===
echo.

cd /d "%~dp0"

echo [1/6] Initializing git repository...
git init
git checkout -b main
echo Done.
echo.

echo [2/6] Adding all files...
git add -A
echo Done.
echo.

echo [3/6] Creating initial commit...
git commit -m "Initial commit: LDREAMS lucid dreaming Android app"
echo Done.
echo.

echo [4/6] Downloading Gradle wrapper...
cd gradle\wrapper
curl -L -o gradle-wrapper.jar "https://raw.githubusercontent.com/gradle/gradle/v8.9.0/gradle/wrapper/gradle-wrapper.jar" 2>nul
if not exist gradle-wrapper.jar (
    echo Downloading alternative method...
    cd ..\..
    curl -L -o gradle.zip "https://services.gradle.org/distributions/gradle-8.9-bin.zip"
    echo Extracting...
    powershell -Command "Expand-Archive -Path gradle.zip -DestinationPath gradle-temp -Force"
    copy "gradle-temp\gradle-8.9\lib\gradle-wrapper-*.jar" "gradle\wrapper\gradle-wrapper.jar" 2>nul
    rmdir /s /q gradle-temp 2>nul
    del gradle.zip 2>nul
) else (
    cd ..
)
echo Done.
echo.

echo [5/6] Setting up GitHub...
echo.
echo Creating GitHub repository...
gh repo create LDREAMS --public --source=. --remote=origin --push 2>nul
if %ERRORLEVEL% equ 0 (
    echo Success! Code pushed to GitHub.
) else (
    echo GitHub CLI not configured. To push manually:
    echo   1. Create repo at https://github.com/new
    echo   2. Run:
    echo      git remote add origin https://github.com/YOUR_USERNAME/LDREAMS.git
    echo      git push -u origin main
)
echo.
echo.

echo === Setup Complete! ===
echo.
echo Your LDREAMS Android project is ready.
echo.
echo To build APK:
echo   - With Android SDK:  gradlew.bat assembleDebug
echo   - Via GitHub Actions: Just push to GitHub and go to Actions tab
echo.
pause
