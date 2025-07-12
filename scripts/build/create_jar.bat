@echo off
echo ========================================
echo Desktop Pet - JAR Builder (Organized)
echo ========================================
echo.

REM Navigate to project root
cd ..\..

REM Create build output directory if it doesn't exist
if not exist "target" mkdir "target"

REM Navigate to source directory
cd src\main\java

echo Cleaning old class files...
del *.class 2>nul

echo.
echo Compiling Java source to target directory...
javac -d ..\..\..\target AdvancedDesktopPet.java MusicManager.java LocationUtils.java
if errorlevel 1 (
    echo ❌ ERROR: Compilation failed!
    cd ..\..\..\scripts\build
    pause
    exit /b 1
)

echo.
echo Checking generated class files...
dir ..\..\..\target\*.class

echo.
echo Creating JAR file with main class and resources...
jar cfe ..\..\..\target\AdvancedDesktopPet.jar AdvancedDesktopPet -C ..\..\..\target . -C ..\..\.. Image -C ..\..\.. music -C ..\..\.. resources
if errorlevel 1 (
    echo ❌ ERROR: JAR creation failed!
    cd ..\..\..\scripts\build
    pause
    exit /b 1
)

echo.
echo ✅ JAR file created successfully!
echo    Location: target\AdvancedDesktopPet.jar

REM Copy to portable folder if it exists
if exist "..\..\DesktopPet-Portable\" (
    echo.
    echo Copying to portable folder...
    copy "..\..\..\target\AdvancedDesktopPet.jar" "..\..\DesktopPet-Portable\AdvancedDesktopPet.jar"
    if errorlevel 1 (
        echo ⚠️  WARNING: Could not copy to portable folder
    ) else (
        echo ✅ Successfully copied to DesktopPet-Portable folder
    )
)

REM Return to scripts directory
cd ..\..\..\scripts\build

echo.
echo ✅ Done! Your JAR file is ready in target\AdvancedDesktopPet.jar
pause 