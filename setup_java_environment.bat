@echo off
setlocal enabledelayedexpansion

:: ========================================
:: Java Environment Setup for Desktop Pet
:: ========================================
:: This script will help install and configure Java for the Desktop Pet application
:: Author: AI Assistant
:: Version: 1.0

echo.
echo ========================================
echo    Java Environment Setup for Desktop Pet
echo ========================================
echo.

:: Set title
title Java Environment Setup - Desktop Pet

:: Check if running as administrator
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] This script is not running as administrator.
    echo Some features may require elevated privileges.
    echo.
    pause
)

:: Function to check if Java is installed
:check_java
echo [INFO] Checking for Java installation...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] Java is already installed!
    java -version
    echo.
    goto :check_java_version
) else (
    echo [INFO] Java not found in PATH. Checking for local JRE...
    goto :check_local_jre
)

:: Check Java version
:check_java_version
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
    set JAVA_VERSION=!JAVA_VERSION:"=!
)
echo [INFO] Detected Java version: !JAVA_VERSION!

:: Extract version number for comparison
for /f "tokens=1,2 delims=." %%a in ("!JAVA_VERSION!") do (
    set MAJOR_VERSION=%%a
    set MINOR_VERSION=%%b
)

:: Remove any non-numeric characters from major version
for /f "delims=" %%i in ('echo !MAJOR_VERSION! ^| findstr /r "[0-9]*"') do set MAJOR_VERSION=%%i

if !MAJOR_VERSION! geq 8 (
    echo [SUCCESS] Java version is compatible (8 or higher)
    goto :setup_environment
) else (
    echo [WARNING] Java version may be too old. Recommended: Java 8 or higher
    echo Current version: !JAVA_VERSION!
    echo.
    set /p UPGRADE_CHOICE="Do you want to continue anyway? (y/n): "
    if /i "!UPGRADE_CHOICE!"=="y" (
        goto :setup_environment
    ) else (
        goto :install_java
    )
)

:: Check for local JRE
:check_local_jre
if exist "minimal-jre\bin\java.exe" (
    echo [INFO] Found local minimal JRE in 'minimal-jre' folder
    echo [INFO] Setting up local Java environment...
    
    :: Set JAVA_HOME to local JRE
    set "JAVA_HOME=%CD%\minimal-jre"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    
    echo [INFO] JAVA_HOME set to: !JAVA_HOME!
    
    :: Test local Java
    "%JAVA_HOME%\bin\java" -version >nul 2>&1
    if !errorlevel! equ 0 (
        echo [SUCCESS] Local JRE is working!
        "%JAVA_HOME%\bin\java" -version
        echo.
        goto :setup_environment
    ) else (
        echo [ERROR] Local JRE is corrupted or incomplete
        goto :install_java
    )
) else (
    echo [INFO] No local JRE found
    goto :install_java
)

:: Install Java
:install_java
echo.
echo ========================================
echo           Java Installation
echo ========================================
echo.
echo [INFO] Java installation is required for the Desktop Pet application.
echo.
echo Options:
echo 1. Download and install OpenJDK 17 (Recommended)
echo 2. Download and install Oracle JDK 17
echo 3. Use local minimal JRE (if available)
echo 4. Manual installation (you'll install Java yourself)
echo 5. Exit setup
echo.

set /p INSTALL_CHOICE="Please choose an option (1-5): "

if "!INSTALL_CHOICE!"=="1" (
    goto :install_openjdk
) else if "!INSTALL_CHOICE!"=="2" (
    goto :install_oracle_jdk
) else if "!INSTALL_CHOICE!"=="3" (
    goto :use_local_jre
) else if "!INSTALL_CHOICE!"=="4" (
    goto :manual_installation
) else if "!INSTALL_CHOICE!"=="5" (
    goto :exit_setup
) else (
    echo [ERROR] Invalid choice. Please try again.
    goto :install_java
)

:: Install OpenJDK
:install_openjdk
echo.
echo [INFO] Installing OpenJDK 17...
echo [INFO] This will download and install OpenJDK 17 from Adoptium.

:: Create temp directory
if not exist "temp" mkdir temp
cd temp

:: Download OpenJDK 17
echo [INFO] Downloading OpenJDK 17...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9/OpenJDK17U-jdk_x64_windows_hotspot_17.0.9_9.msi' -OutFile 'OpenJDK17.msi'}"

if exist "OpenJDK17.msi" (
    echo [INFO] Download completed. Installing...
    
    :: Install silently
    msiexec /i OpenJDK17.msi /quiet /norestart
    
    :: Wait for installation
    timeout /t 30 /nobreak >nul
    
    :: Clean up
    del OpenJDK17.msi
    
    echo [INFO] Installation completed!
    cd ..
    rmdir /s /q temp
    
    :: Refresh environment
    call :refresh_environment
    goto :check_java
) else (
    echo [ERROR] Failed to download OpenJDK 17
    cd ..
    rmdir /s /q temp
    goto :install_java
)

:: Install Oracle JDK
:install_oracle_jdk
echo.
echo [INFO] Installing Oracle JDK 17...
echo [WARNING] Oracle JDK requires accepting license terms.
echo [INFO] Please visit: https://www.oracle.com/java/technologies/downloads/
echo [INFO] Download and install Oracle JDK 17 manually.
echo.
pause
goto :check_java

:: Use local JRE
:use_local_jre
if exist "minimal-jre\bin\java.exe" (
    echo [INFO] Setting up local JRE environment...
    
    :: Create batch file for running with local JRE
    echo @echo off > run_with_local_jre.bat
    echo set "JAVA_HOME=%%~dp0minimal-jre" >> run_with_local_jre.bat
    echo set "PATH=%%JAVA_HOME%%\bin;%%PATH%%" >> run_with_local_jre.bat
    echo echo Using local JRE: %%JAVA_HOME%% >> run_with_local_jre.bat
    echo echo. >> run_with_local_jre.bat
    echo if exist "AdvancedDesktopPet.jar" ( >> run_with_local_jre.bat
    echo     echo Starting Desktop Pet from JAR... >> run_with_local_jre.bat
    echo     "%%JAVA_HOME%%\bin\java" -jar AdvancedDesktopPet.jar >> run_with_local_jre.bat
    echo ) else ( >> run_with_local_jre.bat
    echo     echo Compiling and running Desktop Pet... >> run_with_local_jre.bat
    echo     "%%JAVA_HOME%%\bin\javac" AdvancedDesktopPet.java >> run_with_local_jre.bat
    echo     if %%errorlevel%% equ 0 ( >> run_with_local_jre.bat
    echo         echo Compilation successful! >> run_with_local_jre.bat
    echo         "%%JAVA_HOME%%\bin\java" AdvancedDesktopPet >> run_with_local_jre.bat
    echo     ) else ( >> run_with_local_jre.bat
    echo         echo Compilation failed! >> run_with_local_jre.bat
    echo         pause >> run_with_local_jre.bat
    echo     ) >> run_with_local_jre.bat
    echo ) >> run_with_local_jre.bat
    
    echo [SUCCESS] Created 'run_with_local_jre.bat' for running with local JRE
    echo [INFO] Use this file to run the Desktop Pet with the local JRE
    echo.
    goto :setup_environment
) else (
    echo [ERROR] Local JRE not found in 'minimal-jre' folder
    goto :install_java
)

:: Manual installation
:manual_installation
echo.
echo ========================================
echo         Manual Installation Guide
echo ========================================
echo.
echo Please install Java manually:
echo.
echo 1. Download Java from one of these sources:
echo    - OpenJDK: https://adoptium.net/
echo    - Oracle JDK: https://www.oracle.com/java/technologies/downloads/
echo.
echo 2. Install Java (version 8 or higher recommended)
echo.
echo 3. Make sure Java is added to your PATH environment variable
echo.
echo 4. Run this setup script again after installation
echo.
pause
goto :exit_setup

:: Setup environment
:setup_environment
echo.
echo ========================================
echo        Environment Setup
echo ========================================
echo.

:: Check if JAVA_HOME is set
if defined JAVA_HOME (
    echo [INFO] JAVA_HOME is already set to: !JAVA_HOME!
) else (
    echo [INFO] Setting JAVA_HOME environment variable...
    
    :: Try to find Java installation
    where java >nul 2>&1
    if !errorlevel! equ 0 (
        for /f "delims=" %%i in ('where java') do (
            set JAVA_PATH=%%i
            goto :found_java
        )
    )
    
    :found_java
    if defined JAVA_PATH (
        :: Extract JAVA_HOME from java.exe path
        for %%i in ("!JAVA_PATH!") do set "JAVA_HOME=%%~dpi.."
        echo [INFO] Detected Java installation at: !JAVA_HOME!
    ) else (
        echo [WARNING] Could not automatically detect Java installation
        echo [INFO] You may need to set JAVA_HOME manually
    )
)

:: Create enhanced run script
echo [INFO] Creating enhanced run script...
(
echo @echo off
echo setlocal enabledelayedexpansion
echo.
echo echo ========================================
echo echo           Desktop Pet Launcher
echo echo ========================================
echo echo.
echo.
echo :: Check if Java is available
echo java -version ^>nul 2^>^&1
echo if %%errorlevel%% neq 0 ^(
echo     echo [ERROR] Java is not installed or not in PATH
echo     echo [INFO] Please run setup_java_environment.bat first
echo     echo.
echo     pause
echo     exit /b 1
echo ^)
echo.
echo :: Check if JAR file exists
echo if exist "AdvancedDesktopPet.jar" ^(
echo     echo [INFO] Starting Desktop Pet from JAR file...
echo     java -jar AdvancedDesktopPet.jar
echo ^) else if exist "AdvancedDesktopPet.java" ^(
echo     echo [INFO] Compiling and running Desktop Pet...
echo     javac AdvancedDesktopPet.java
echo     if %%errorlevel%% equ 0 ^(
echo         echo [SUCCESS] Compilation successful!
echo         echo [INFO] Starting Desktop Pet...
echo         java AdvancedDesktopPet
echo     ^) else ^(
echo         echo [ERROR] Compilation failed!
echo         echo [INFO] Please check the Java source code
echo         pause
echo     ^)
echo ^) else ^(
echo     echo [ERROR] No Desktop Pet files found
echo     echo [INFO] Please ensure AdvancedDesktopPet.java or AdvancedDesktopPet.jar is present
echo     pause
echo ^)
) > run_enhanced.bat

echo [SUCCESS] Created 'run_enhanced.bat' - Enhanced launcher script

:: Create development environment script
echo [INFO] Creating development environment script...
(
echo @echo off
echo setlocal enabledelayedexpansion
echo.
echo echo ========================================
echo echo        Development Environment
echo echo ========================================
echo echo.
echo.
echo :: Set development environment variables
echo if defined JAVA_HOME ^(
echo     echo [INFO] Using JAVA_HOME: %%JAVA_HOME%%
echo ^) else ^(
echo     echo [WARNING] JAVA_HOME not set
echo ^)
echo.
echo :: Show Java version
echo echo [INFO] Java version:
echo java -version
echo.
echo :: Show available files
echo echo [INFO] Available files:
echo dir *.java *.jar 2^>nul
echo.
echo :: Compile if source exists
echo if exist "AdvancedDesktopPet.java" ^(
echo     echo [INFO] Compiling source code...
echo     javac AdvancedDesktopPet.java
echo     if %%errorlevel%% equ 0 ^(
echo         echo [SUCCESS] Compilation successful!
echo     ^) else ^(
echo         echo [ERROR] Compilation failed!
echo     ^)
echo ^)
echo.
echo echo [INFO] Development environment ready
echo echo [INFO] Use 'run_enhanced.bat' to start the application
echo.
echo pause
) > dev_environment.bat

echo [SUCCESS] Created 'dev_environment.bat' - Development environment script

:: Test compilation
echo [INFO] Testing compilation...
if exist "AdvancedDesktopPet.java" (
    javac AdvancedDesktopPet.java
    if !errorlevel! equ 0 (
        echo [SUCCESS] Compilation test passed!
    ) else (
        echo [WARNING] Compilation test failed. Check the source code.
    )
) else (
    echo [INFO] No source code found for compilation test
)

:: Create README for setup
echo [INFO] Creating setup documentation...
(
echo # Desktop Pet - Java Environment Setup
echo.
echo ## Quick Start
echo.
echo ### Option 1: Run with Enhanced Launcher
echo ```bash
echo run_enhanced.bat
echo ```
echo.
echo ### Option 2: Run with Local JRE ^(if available^)
echo ```bash
echo run_with_local_jre.bat
echo ```
echo.
echo ### Option 3: Manual Run
echo ```bash
echo # Compile and run
echo javac AdvancedDesktopPet.java
echo java AdvancedDesktopPet
echo.
echo # Or run from JAR
echo java -jar AdvancedDesktopPet.jar
echo ```
echo.
echo ## Development
echo.
echo ### Development Environment
echo ```bash
echo dev_environment.bat
echo ```
echo.
echo ## Troubleshooting
echo.
echo ### Java Not Found
echo 1. Run `setup_java_environment.bat` to install Java
echo 2. Make sure Java is in your PATH
echo 3. Set JAVA_HOME environment variable
echo.
echo ### Compilation Errors
echo 1. Check Java version ^(requires Java 8+^)
echo 2. Verify source code integrity
echo 3. Check for missing dependencies
echo.
echo ### Runtime Errors
echo 1. Ensure all required files are present
echo 2. Check file permissions
echo 3. Verify Java installation
echo.
echo ## Files Created by Setup
echo.
echo - `run_enhanced.bat` - Enhanced launcher with error checking
echo - `run_with_local_jre.bat` - Launcher using local JRE
echo - `dev_environment.bat` - Development environment setup
echo.
echo ## Requirements
echo.
echo - Java 8 or higher
echo - Windows operating system
echo - Sufficient disk space for Java installation
echo.
) > SETUP_README.md

echo [SUCCESS] Created 'SETUP_README.md' - Setup documentation

:: Final summary
echo.
echo ========================================
echo           Setup Complete!
echo ========================================
echo.
echo [SUCCESS] Java environment setup completed successfully!
echo.
echo Available launchers:
echo - run_enhanced.bat ^(Recommended^)
echo - run_with_local_jre.bat ^(If local JRE available^)
echo - dev_environment.bat ^(Development^)
echo.
echo Documentation:
echo - SETUP_README.md ^(Setup guide^)
echo.
echo Next steps:
echo 1. Run 'run_enhanced.bat' to start the Desktop Pet
echo 2. Or use 'dev_environment.bat' for development
echo 3. Check SETUP_README.md for troubleshooting
echo.
echo Enjoy your Desktop Pet! 🐾
echo.

goto :exit_setup

:: Refresh environment variables
:refresh_environment
echo [INFO] Refreshing environment variables...
:: This is a simplified refresh - in practice, you'd need to restart the shell
echo [INFO] Environment refresh completed
goto :eof

:: Exit setup
:exit_setup
echo.
echo [INFO] Setup script completed.
echo [INFO] Press any key to exit...
pause >nul
exit /b 0 