@echo off
setlocal enabledelayedexpansion

echo ========================================
echo        Development Environment
echo ========================================
echo.

:: Set development environment variables
if defined JAVA_HOME (
    echo [INFO] Using JAVA_HOME: %JAVA_HOME%
) else (
    echo [WARNING] JAVA_HOME not set
)

:: Show Java version
echo [INFO] Java version:
java -version

:: Show available files
echo [INFO] Available files:
dir *.java *.jar 2>nul

:: Compile if source exists
if exist "AdvancedDesktopPet.java" (
    echo [INFO] Compiling source code...
    javac AdvancedDesktopPet.java
    if %errorlevel% equ 0 (
        echo [SUCCESS] Compilation successful
    ) else (
        echo [ERROR] Compilation failed
    )
)

echo [INFO] Development environment ready
echo [INFO] Use 'run_enhanced.bat' to start the application

pause
