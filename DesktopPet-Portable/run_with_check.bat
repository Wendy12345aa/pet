@echo off
echo Checking Java installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found!
    echo Please install Java from: https://adoptium.net/
    pause
    exit /b 1
)
echo Starting Desktop Pet...
java -jar AdvancedDesktopPet.jar
pause
