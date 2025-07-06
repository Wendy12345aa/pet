@echo off
echo Checking Portable Folder Structure
echo ==================================

echo.
echo Current directory: %CD%

echo.
echo 1. Checking if DesktopPet-Portable folder exists...
if exist "DesktopPet-Portable" (
    echo ✓ DesktopPet-Portable folder found
) else (
    echo ✗ DesktopPet-Portable folder NOT found!
    pause
    exit /b 1
)

echo.
echo 2. Checking JAR file...
if exist "DesktopPet-Portable\AdvancedDesktopPet.jar" (
    echo ✓ JAR file found
    dir "DesktopPet-Portable\AdvancedDesktopPet.jar"
) else (
    echo ✗ JAR file NOT found!
)

echo.
echo 3. Checking Image folder...
if exist "DesktopPet-Portable\Image" (
    echo ✓ Image folder found
    echo   Contents:
    dir "DesktopPet-Portable\Image\*.png" 2>nul
    dir "DesktopPet-Portable\Image\*.ico" 2>nul
) else (
    echo ✗ Image folder NOT found!
)

echo.
echo 4. Checking music folder...
if exist "DesktopPet-Portable\music" (
    echo ✓ Music folder found
    echo   Contents:
    dir "DesktopPet-Portable\music\*.wav" 2>nul
) else (
    echo ✗ Music folder NOT found!
)

echo.
echo 5. Checking Java installation...
java -version 2>nul
if errorlevel 1 (
    echo ✗ Java NOT found in PATH!
) else (
    echo ✓ Java found
)

echo.
echo 6. Testing JAR file...
cd DesktopPet-Portable
echo Testing from portable directory: %CD%
java -jar AdvancedDesktopPet.jar
if errorlevel 1 (
    echo ✗ JAR execution failed!
    echo Check the error messages above.
) else (
    echo ✓ JAR executed successfully
)

echo.
echo Diagnostic complete.
pause 