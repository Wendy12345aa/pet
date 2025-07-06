@echo off
setlocal enabledelayedexpansion

:: Desktop Pet Portable Launcher
:: This launcher works without Java installation

set "APP_DIR=%~dp0"
set "JAR_FILE=%APP_DIR%AdvancedDesktopPet.jar"
set "JRE_DIR=%APP_DIR%minimal-jre"

echo ========================================
echo           Desktop Pet Launcher
echo ========================================
echo.

:: Try embedded JRE first (preferred)
if exist "%JRE_DIR%\bin\java.exe" (
    echo [INFO] Using embedded JRE - no Java installation required
    set "JAVA_HOME=%JRE_DIR%"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    "%JAVA_HOME%\bin\java" -jar "%JAR_FILE%"
    goto :end
)

:: Try system Java as fallback
echo [INFO] Checking for system Java...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] Using system Java...
    java -jar "%JAR_FILE%"
    goto :end
)

:: No Java found
echo [ERROR] No Java Runtime Environment found.
echo.
echo This portable version should include an embedded JRE.
echo If you're seeing this error, the JRE files may be missing.
echo.
echo Solutions:
echo 1. Download Java from: https://adoptium.net/
echo 2. Re-run the portable creator to include the JRE
echo 3. Check if antivirus removed the JRE files
echo.
pause

:end
exit /b 0
