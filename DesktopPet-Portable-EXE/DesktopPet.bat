@echo off
setlocal enabledelayedexpansion

set "APP_DIR=%~dp0"
set "JAR_FILE=%APP_DIR%lib\AdvancedDesktopPet.jar"
set "JRE_DIR=%APP_DIR%jre"

echo Desktop Pet Launcher
echo ===================
echo.

if exist "%JRE_DIR%\bin\java.exe" (
    echo [INFO] Using embedded JRE - no Java installation required!
    set "JAVA_HOME=%JRE_DIR%"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    "%JAVA_HOME%\bin\java" -jar "%JAR_FILE%"
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
