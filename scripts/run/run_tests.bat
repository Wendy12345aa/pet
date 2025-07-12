@echo off
echo ========================================
echo Desktop Pet - Unit Test Runner (Organized)
echo ========================================
echo.

REM Navigate to project root
cd ..\..

echo [1/6] Checking directory structure...
if not exist "src\main\java" (
    echo ❌ ERROR: Source directory not found!
    echo Please organize your project first using organize_project.bat
    cd scripts\run
    pause
    exit /b 1
)

if not exist "src\test\java" (
    echo ❌ ERROR: Test directory not found!
    echo Please ensure you have test files in src\test\java\
    cd scripts\run
    pause
    exit /b 1
)

echo [2/6] Creating build directories...
if not exist "target\classes" mkdir "target\classes"
if not exist "target\test-classes" mkdir "target\test-classes"

echo [3/6] Checking for JUnit JAR...
if not exist "lib\junit-platform-console-standalone-1.9.2.jar" (
    echo ❌ ERROR: JUnit JAR not found!
    echo Please download from:
    echo https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.9.2/junit-platform-console-standalone-1.9.2.jar
    echo.
    echo Place it in lib\ directory and run this script again.
    cd scripts\run
    pause
    exit /b 1
)

echo [4/6] Compiling main classes...
cd src\main\java
javac -d ..\..\..\target\classes AdvancedDesktopPet.java MusicManager.java LocationUtils.java
if errorlevel 1 (
    echo ❌ ERROR: Main class compilation failed!
    cd ..\..\..\scripts\run
    pause
    exit /b 1
)
cd ..\..\..

echo [5/6] Compiling test classes...
javac -cp "lib\junit-platform-console-standalone-1.9.2.jar;target\classes" -d target\test-classes src\test\java\*.java
if errorlevel 1 (
    echo ❌ ERROR: Test compilation failed!
    echo Make sure you have test files in src\test\java\
    cd scripts\run
    pause
    exit /b 1
)

echo [6/6] Running tests...
java -cp "lib\junit-platform-console-standalone-1.9.2.jar;target\classes;target\test-classes" org.junit.platform.console.ConsoleLauncher --scan-classpath
if errorlevel 1 (
    echo ⚠️  Some tests failed!
) else (
    echo ✅ All tests passed!
)

echo.
echo Test classes available in target\test-classes\ directory

REM Return to scripts directory
cd scripts\run

echo.
echo Test run complete!
pause 