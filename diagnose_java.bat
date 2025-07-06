@echo off
echo Java Installation Diagnostic
echo ============================

echo.
echo 1. Checking Java version...
java -version 2>nul
if errorlevel 1 (
    echo ✗ Java not found in PATH
    goto :no_java
) else (
    echo ✓ Java found in PATH
)

echo.
echo 2. Checking if it's JDK (not just JRE)...
javac -version 2>nul
if errorlevel 1 (
    echo ✗ javac not found - you have JRE, not JDK
    echo   You need JDK (Java Development Kit) to create minimal JRE
    goto :no_jdk
) else (
    echo ✓ javac found - you have JDK
)

echo.
echo 3. Checking jlink availability...
jlink --version 2>nul
if errorlevel 1 (
    echo ✗ jlink not found
    echo   jlink is only available in Java 9+ (JDK)
    goto :no_jlink
) else (
    echo ✓ jlink found
)

echo.
echo 4. Checking JAVA_HOME...
if "%JAVA_HOME%"=="" (
    echo ✗ JAVA_HOME not set
    echo   This might cause issues with jlink
) else (
    echo ✓ JAVA_HOME is set to: %JAVA_HOME%
)

echo.
echo 5. Finding Java installation location...
for /f "tokens=*" %%i in ('where java') do (
    echo Java executable found at: %%i
    set "JAVA_PATH=%%i"
    goto :found_java
)
:found_java

if "%JAVA_PATH%"=="" (
    echo ✗ Could not find Java executable
    goto :no_java
)

echo.
echo 6. Checking for jmods directory...
for %%i in ("%JAVA_PATH%") do set "JAVA_DIR=%%~dpi"
set "POSSIBLE_JAVA_HOME=%JAVA_DIR:~0,-1%"

echo Checking: %POSSIBLE_JAVA_HOME%\jmods
if exist "%POSSIBLE_JAVA_HOME%\jmods" (
    echo ✓ jmods directory found
) else (
    echo ✗ jmods directory not found
    echo   This means you don't have JDK 9+ or the installation is incomplete
)

echo.
echo ========================================
echo DIAGNOSIS COMPLETE
echo ========================================

if "%JAVA_HOME%"=="" (
    echo.
    echo RECOMMENDATION: Set JAVA_HOME environment variable
    echo   Set it to your JDK installation directory
    echo   Example: set JAVA_HOME=C:\Program Files\Java\jdk-11.0.12
)

echo.
echo If you see any ✗ marks above, that's why the minimal JRE creation failed.
echo.
pause
exit /b 0

:no_java
echo.
echo SOLUTION: Install Java from https://adoptium.net/
echo   Download and install the JDK (not JRE) version 11 or later
echo.
pause
exit /b 1

:no_jdk
echo.
echo SOLUTION: Install JDK (Java Development Kit)
echo   You currently have JRE (Java Runtime Environment)
echo   Download JDK from: https://adoptium.net/
echo   Choose "JDK" not "JRE"
echo.
pause
exit /b 1

:no_jlink
echo.
echo SOLUTION: Upgrade to Java 9+ (JDK)
echo   jlink is only available in Java 9 and later
echo   Download JDK 11+ from: https://adoptium.net/
echo.
pause
exit /b 1 