@echo off
echo Testing Enemy Hanging Fixes
echo ===========================
echo.
echo This script will help you test the enemy system fixes.
echo.
echo 1. Starting the desktop pet application...
echo 2. The enemy system will be enabled by default
echo 3. Watch for any hanging enemies
echo 4. Use the settings window to test cleanup functions
echo.
echo Press any key to start the application...
pause >nul

echo Starting AdvancedDesktopPet...
java -cp . AdvancedDesktopPet

echo.
echo Application closed. Check the console output for any error messages.
pause 