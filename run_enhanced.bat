@echo off
setlocal enabledelayedexpansion

echo ========================================
echo           Desktop Pet Launcher
echo ========================================
echo.

:: Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH
    echo [INFO] Please run setup_java_environment.bat first
    echo.
    pause
    exit /b 1
)

:: Check if JAR file exists
if exist "AdvancedDesktopPet.jar" (
    echo [INFO] Starting Desktop Pet from JAR file...
    java -jar AdvancedDesktopPet.jar
) else if exist "AdvancedDesktopPet.java" (
    echo [INFO] Compiling and running Desktop Pet...
    javac AdvancedDesktopPet.java MusicManager.java
    if %errorlevel% equ 0 (
        echo [SUCCESS] Compilation successful
        echo [INFO] Starting Desktop Pet...
        java AdvancedDesktopPet
    ) else (
        echo [ERROR] Compilation failed
        echo [INFO] Please check the Java source code
        pause
    )
) else (
    echo [ERROR] No Desktop Pet files found
    echo [INFO] Please ensure AdvancedDesktopPet.java or AdvancedDesktopPet.jar is present
    pause
)
