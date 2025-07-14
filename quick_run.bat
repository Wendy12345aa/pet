@echo off
echo ========================================
echo Desktop Pet - Quick Runner (Root)
echo ========================================
echo.

echo Compiling from organized source directory...
cd src\main\java
javac AdvancedDesktopPet.java MusicManager.java LocationUtils.java

if errorlevel 1 (
    echo ❌ Compilation failed!
    cd ..\..\..
    pause
    exit /b 1
)

echo ✅ Compilation successful!
echo.
echo Running from root directory (where Image/, music/, resources/ are accessible)...
cd ..\..\..

java -cp "src\main\java" AdvancedDesktopPet

echo.
echo Pet closed.
pause 