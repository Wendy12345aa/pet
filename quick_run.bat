@echo off
setlocal

REM ========================================
REM Desktop Pet - Quick Runner (Root)
REM ========================================
echo.

REM Create build output directory if it doesn't exist
if not exist ..\..\..\target mkdir ..\..\..\target

REM Compile all Java files to target directory
cd src\main\java
javac -d ..\..\..\target AdvancedDesktopPet.java MusicManager.java LocationUtils.java

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

java -cp target AdvancedDesktopPet

echo.
echo Pet closed.
pause
endlocal 