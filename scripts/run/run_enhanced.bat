@echo off
echo ========================================
echo Desktop Pet - Enhanced Runner (FIXED)
echo ========================================
echo.

echo [1/3] Compiling source files...
cd src\main\java
javac AdvancedDesktopPet.java MusicManager.java LocationUtils.java

if errorlevel 1 (
    echo.
    echo ❌ ERROR: Compilation failed!
    echo Check your Java source code for errors.
    cd ..\..\..\scripts\run
    pause
    exit /b 1
)

echo ✅ Compilation successful!

echo [2/3] Navigating to root directory (where resources are)...
cd ..\..\..

echo [3/3] Starting Desktop Pet...
echo NOTE: Running from root so Image/, music/, and resources/ are accessible
java -cp "src\main\java" AdvancedDesktopPet

echo.
echo Desktop Pet closed.
cd scripts\run
pause 