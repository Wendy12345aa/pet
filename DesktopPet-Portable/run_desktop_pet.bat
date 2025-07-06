@echo off
echo ========================================
echo Desktop Pet - Portable Version
echo ========================================
echo.
echo Checking Java installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found!
    echo.
    echo This application requires Java to run.
    echo Please install Java from: https://adoptium.net/
    echo.
    echo After installing Java, run this script again.
    pause
    exit /b 1
)
echo ✓ Java found
echo.
echo Starting Desktop Pet...
java -jar AdvancedDesktopPet.jar
echo.
echo Desktop Pet has been closed.
pause
