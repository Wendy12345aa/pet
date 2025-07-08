@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

set "JAVA_EXE=jre\bin\java.exe"
set "JAR_FILE=lib\AdvancedDesktopPet.jar"

echo Desktop Pet Launcher
echo ===================
echo.

if exist "%JAVA_EXE%" (
    echo [INFO] Using embedded JRE - no Java installation required!
    "%JAVA_EXE%" -jar "%JAR_FILE%"
) else (
    echo [INFO] Checking for system Java...
    java -version >nul 2>&1
    if %errorlevel% equ 0 (
        echo [INFO] Using system Java...
        java -jar "%JAR_FILE%"
    ) else (
        echo [ERROR] No Java found. Please install Java or use the embedded version.
        pause
    )
)

pause
