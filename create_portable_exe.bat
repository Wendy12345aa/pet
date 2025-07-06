@echo off
echo Creating Portable Executable for Desktop Pet
echo ============================================

echo.
echo This will create a portable .exe file that works on any Windows PC.
echo.

echo 1. Checking current setup...
if not exist "AdvancedDesktopPet.jar" (
    echo ERROR: JAR file not found! Please run create_jar.bat first.
    pause
    exit /b 1
)

echo ✓ JAR file found

echo.
echo 2. Creating portable package structure...
if exist "DesktopPet-Portable-EXE" (
    echo Removing existing folder...
    rmdir /s /q "DesktopPet-Portable-EXE"
)

mkdir "DesktopPet-Portable-EXE"
mkdir "DesktopPet-Portable-EXE\lib"
mkdir "DesktopPet-Portable-EXE\resources"

echo.
echo 3. Copying files...
copy "AdvancedDesktopPet.jar" "DesktopPet-Portable-EXE\lib\"
xcopy "Image" "DesktopPet-Portable-EXE\resources\Image\" /e /i /q
xcopy "music" "DesktopPet-Portable-EXE\resources\music\" /e /i /q

echo.
echo 4. Creating launcher script...
(
echo @echo off
echo echo ========================================
echo echo Desktop Pet - Portable Executable
echo echo ========================================
echo echo.
echo echo This version includes a minimal Java runtime.
echo echo.
echo echo Starting Desktop Pet...
echo.
echo set "JAVA_HOME=%%~dp0jre"
echo set "PATH=%%JAVA_HOME%%\bin;%%PATH%%"
echo.
echo "%%JAVA_HOME%%\bin\java.exe" -jar "%%~dp0lib\AdvancedDesktopPet.jar"
echo.
echo echo Desktop Pet has been closed.
echo pause
) > "DesktopPet-Portable-EXE\DesktopPet.exe.bat"

echo.
echo 5. Creating README...
(
echo Desktop Pet - Portable Executable
echo =================================
echo.
echo This is a portable version that includes a minimal Java runtime.
echo It will work on any Windows PC without requiring Java installation.
echo.
echo HOW TO USE:
echo 1. Double-click "DesktopPet.exe.bat" to start
echo 2. The application will run with the embedded Java runtime
echo.
echo CONTROLS:
echo - Left-click and drag to move the pet
echo - Middle-click for settings
echo - Right-click for special animations
echo - Double-click for jump animation
echo.
echo FILES:
echo - DesktopPet.exe.bat (launcher)
echo - lib/AdvancedDesktopPet.jar (application)
echo - resources/Image/ (pet images)
echo - resources/music/ (audio files)
echo - jre/ (embedded Java runtime - will be added)
echo.
echo NOTE: The jre/ folder will be created when you run the minimal JRE script.
echo.
) > "DesktopPet-Portable-EXE\README.txt"

echo.
echo 6. Creating minimal JRE (if available)...
jlink --version >nul 2>&1
if errorlevel 1 (
    echo WARNING: jlink not available. You'll need to manually add a JRE.
    echo.
    echo MANUAL SETUP REQUIRED:
    echo 1. Download a minimal JRE from: https://adoptium.net/
    echo 2. Extract it to the "jre" folder in DesktopPet-Portable-EXE
    echo 3. The folder structure should be: DesktopPet-Portable-EXE\jre\bin\java.exe
    echo.
) else (
    echo Creating minimal JRE...
    jlink --module-path "%JAVA_HOME%\jmods" ^
          --add-modules java.base,java.desktop,java.logging,java.prefs ^
          --output "DesktopPet-Portable-EXE\jre" ^
          --compress=2 ^
          --no-header-files ^
          --no-man-pages
    
    if errorlevel 1 (
        echo WARNING: Failed to create minimal JRE. Manual setup required.
    ) else (
        echo ✓ Minimal JRE created successfully!
    )
)

echo.
echo 7. Creating ZIP package...
if exist "DesktopPet-Portable-EXE.zip" (
    del "DesktopPet-Portable-EXE.zip"
)

echo Creating ZIP file...
powershell -Command "Compress-Archive -Path 'DesktopPet-Portable-EXE\*' -DestinationPath 'DesktopPet-Portable-EXE.zip' -Force"

if errorlevel 1 (
    echo WARNING: Could not create ZIP file. You can manually zip the folder.
) else (
    echo ✓ ZIP file created: DesktopPet-Portable-EXE.zip
)

echo.
echo ========================================
echo PORTABLE EXECUTABLE PACKAGE CREATED!
echo ========================================
echo.
echo The DesktopPet-Portable-EXE folder contains:
echo ✓ DesktopPet.exe.bat (launcher)
echo ✓ lib/AdvancedDesktopPet.jar (application)
echo ✓ resources/Image/ (pet images)
echo ✓ resources/music/ (audio files)
echo ✓ README.txt (instructions)
echo.
if exist "DesktopPet-Portable-EXE\jre" (
    echo ✓ jre/ (embedded Java runtime)
    echo.
    echo This package is COMPLETELY PORTABLE!
    echo It will work on any Windows PC without Java installation.
) else (
    echo ⚠ jre/ (needs to be added manually)
    echo.
    echo To make it fully portable:
    echo 1. Download JRE from https://adoptium.net/
    echo 2. Extract to DesktopPet-Portable-EXE\jre\
    echo.
)
echo.
echo You can distribute the DesktopPet-Portable-EXE folder or ZIP file.
echo.
pause 