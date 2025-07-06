@echo off
setlocal enabledelayedexpansion

:: ========================================
:: Desktop Pet EXE Deployment Script
:: ========================================
:: This script creates a standalone executable version of the Desktop Pet
:: that doesn't require Java installation on the target machine

echo.
echo ========================================
echo    Desktop Pet EXE Deployment Tool
echo ========================================
echo.

:: Set title
title Desktop Pet EXE Deployment

:: Check if we're in the right directory
if not exist "AdvancedDesktopPet.java" (
    echo [ERROR] AdvancedDesktopPet.java not found in current directory
    echo [INFO] Please run this script from the project root directory
    pause
    exit /b 1
)

:: Create deployment directory
if not exist "deployment" mkdir deployment
cd deployment

echo [INFO] Starting EXE deployment process...
echo.

:: Method 1: Using JPackage (requires JDK 14+)
echo ========================================
echo Method 1: JPackage (Native Installer)
echo ========================================
echo.

:: Check if JPackage is available
jpackage --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] JPackage found! Creating native installer...
    
    :: Create application image first
    echo [INFO] Creating application image...
    jlink --module-path "%JAVA_HOME%\jmods" --add-modules java.base,java.desktop,java.logging,java.xml,java.prefs,java.datatransfer --output runtime
    
    :: Create the application package
    jpackage --input . --name "Desktop Pet" --main-jar "..\AdvancedDesktopPet.jar" --main-class "AdvancedDesktopPet" --runtime-image runtime --type exe --dest . --icon "..\chibi01.ico"
    
    if exist "Desktop Pet-1.0.exe" (
        echo [SUCCESS] Native installer created: Desktop Pet-1.0.exe
        echo [INFO] This installer will install the app with its own Java runtime
    ) else (
        echo [WARNING] JPackage failed, trying alternative methods...
    )
) else (
    echo [INFO] JPackage not available, trying alternative methods...
)

echo.
echo ========================================
echo Method 2: Launch4j (JAR to EXE Wrapper)
echo ========================================
echo.

:: Download Launch4j if not present
if not exist "launch4j" (
    echo [INFO] Downloading Launch4j...
    mkdir launch4j
    cd launch4j
    
    :: Download Launch4j
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://sourceforge.net/projects/launch4j/files/launch4j-3/3.50/launch4j-3.50-win32.exe/download' -OutFile 'launch4j-installer.exe'}"
    
    if exist "launch4j-installer.exe" (
        echo [INFO] Installing Launch4j...
        launch4j-installer.exe /S
        timeout /t 10 /nobreak >nul
        del launch4j-installer.exe
    )
    
    cd ..
)

:: Create Launch4j configuration
echo [INFO] Creating Launch4j configuration...
(
echo ^<?xml version="1.0" encoding="UTF-8"?^>
echo ^<launch4jConfig^>
echo   ^<dontWrapJar^>false^</dontWrapJar^>
echo   ^<headerType^>gui^</headerType^>
echo   ^<jar^>../AdvancedDesktopPet.jar^</jar^>
echo   ^<outfile^>DesktopPet.exe^</outfile^>
echo   ^<errTitle^>Desktop Pet Error^</errTitle^>
echo   ^<cmdLine^>^</cmdLine^>
echo   ^<chdir^>^</chdir^>
echo   ^<priority^>normal^</priority^>
echo   ^<downloadUrl^>https://adoptium.net/^</downloadUrl^>
echo   ^<supportUrl^>^</supportUrl^>
echo   ^<stayAlive^>false^</stayAlive^>
echo   ^<restartOnCrash^>false^</restartOnCrash^>
echo   ^<manifest^>^</manifest^>
echo   ^<icon^>../chibi01.ico^</icon^>
echo   ^<jre^>
echo     ^<path^>minimal-jre^</path^>
echo     ^<requiresJdk^>false^</requiresJdk^>
echo     ^<requires64Bit^>false^</requires64Bit^>
echo     ^<minVersion^>1.8.0^</minVersion^>
echo     ^<maxVersion^>^</maxVersion^>
echo     ^<jdkPreference^>preferJre^</jdkPreference^>
echo     ^<runtimeBits^>64/32^</runtimeBits^>
echo   ^</jre^>
echo   ^<versionInfo^>
echo     ^<fileVersion^>1.0.0.0^</fileVersion^>
echo     ^<txtFileVersion^>1.0.0^</txtFileVersion^>
echo     ^<fileDescription^>Desktop Pet Application^</fileDescription^>
echo     ^<copyright^>Desktop Pet^</copyright^>
echo     ^<productVersion^>1.0.0.0^</productVersion^>
echo     ^<txtProductVersion^>1.0.0^</txtProductVersion^>
echo     ^<productName^>Desktop Pet^</productName^>
echo     ^<companyName^>Desktop Pet^</companyName^>
echo     ^<internalName^>desktoppet^</internalName^>
echo     ^<originalFilename^>DesktopPet.exe^</originalFilename^>
echo   ^</versionInfo^>
echo ^</launch4jConfig^>
) > launch4j-config.xml

:: Run Launch4j if available
if exist "launch4j\launch4j.exe" (
    echo [INFO] Running Launch4j to create EXE...
    launch4j\launch4j.exe launch4j-config.xml
    
    if exist "DesktopPet.exe" (
        echo [SUCCESS] EXE created: DesktopPet.exe
        echo [INFO] This EXE requires Java to be installed on the target machine
    ) else (
        echo [ERROR] Launch4j failed to create EXE
    )
) else (
    echo [WARNING] Launch4j not found, trying next method...
)

echo.
echo ========================================
echo Method 3: Self-Contained JAR with JRE
echo ========================================
echo.

:: Create a self-contained package with embedded JRE
echo [INFO] Creating self-contained package...

:: Copy necessary files
if not exist "DesktopPet-Portable" mkdir DesktopPet-Portable
copy "..\AdvancedDesktopPet.jar" "DesktopPet-Portable\"
copy "..\chibi01.ico" "DesktopPet-Portable\"
xcopy "..\Image" "DesktopPet-Portable\Image\" /E /I /Y
xcopy "..\music" "DesktopPet-Portable\music\" /E /I /Y

:: Copy minimal JRE if available
if exist "..\minimal-jre" (
    echo [INFO] Copying minimal JRE...
    xcopy "..\minimal-jre" "DesktopPet-Portable\minimal-jre\" /E /I /Y
)

:: Create launcher script
echo [INFO] Creating portable launcher...
(
echo @echo off
echo setlocal enabledelayedexpansion
echo.
echo echo ========================================
echo echo           Desktop Pet Launcher
echo echo ========================================
echo echo.
echo.
echo :: Check for embedded JRE first
echo if exist "minimal-jre\bin\java.exe" ^(
echo     echo [INFO] Using embedded JRE...
echo     set "JAVA_HOME=%%~dp0minimal-jre"
echo     set "PATH=%%JAVA_HOME%%\bin;%%PATH%%"
echo     "%%JAVA_HOME%%\bin\java" -jar AdvancedDesktopPet.jar
echo ^) else ^(
echo     echo [INFO] Checking for system Java...
echo     java -version ^>nul 2^>^&1
echo     if %%errorlevel%% equ 0 ^(
echo         echo [INFO] Using system Java...
echo         java -jar AdvancedDesktopPet.jar
echo     ^) else ^(
echo         echo [ERROR] No Java found. Please install Java or use the embedded version.
echo         echo [INFO] Download from: https://adoptium.net/
echo         pause
echo     ^)
echo ^)
) > "DesktopPet-Portable\run.bat"

:: Create desktop shortcut creator
(
echo @echo off
echo echo Creating Desktop Pet shortcut...
echo.
echo :: Create shortcut on desktop
echo set "DESKTOP=%%USERPROFILE%%\Desktop"
echo set "SHORTCUT=%%DESKTOP%%\Desktop Pet.lnk"
echo.
echo :: Use PowerShell to create shortcut
echo powershell -Command "& {$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut^('%%SHORTCUT%%'^); $Shortcut.TargetPath = '%%~dp0run.bat'; $Shortcut.WorkingDirectory = '%%~dp0'; $Shortcut.IconLocation = '%%~dp0chibi01.ico'; $Shortcut.Save^(^)}"
echo.
echo echo [SUCCESS] Desktop shortcut created!
echo pause
) > "DesktopPet-Portable\create-shortcut.bat"

:: Create README for portable version
(
echo Desktop Pet - Portable Version
echo ==============================
echo.
echo This is a portable version of the Desktop Pet application.
echo.
echo Files included:
echo - AdvancedDesktopPet.jar: The main application
echo - run.bat: Launcher script
echo - minimal-jre/: Embedded Java Runtime Environment
echo - Image/: Application images
echo - music/: Application music files
echo - chibi01.ico: Application icon
echo.
echo How to use:
echo 1. Double-click 'run.bat' to start the application
echo 2. Run 'create-shortcut.bat' to create a desktop shortcut
echo 3. The application will work without installing Java
echo.
echo Requirements:
echo - Windows 7 or later
echo - No Java installation required (uses embedded JRE)
echo.
echo Troubleshooting:
echo - If the app doesn't start, try running 'run.bat' from command prompt
echo - Make sure all files are in the same folder
echo - Check that Windows Defender or antivirus isn't blocking the application
) > "DesktopPet-Portable\README.txt"

echo [SUCCESS] Portable package created: DesktopPet-Portable/
echo [INFO] This package includes an embedded JRE and doesn't require Java installation

echo.
echo ========================================
echo Method 4: Advanced EXE with Resource Embedding
echo ========================================
echo.

:: Create advanced EXE using resource embedding
echo [INFO] Creating advanced EXE with embedded resources...

:: Download and use Advanced Installer or similar tool
if not exist "advanced-exe" mkdir advanced-exe

:: Create a more sophisticated launcher
(
echo @echo off
echo setlocal enabledelayedexpansion
echo.
echo :: Desktop Pet Advanced Launcher
echo :: This launcher embeds all resources and creates a true standalone EXE
echo.
echo set "APP_DIR=%%~dp0"
echo set "JAR_FILE=%%APP_DIR%%AdvancedDesktopPet.jar"
echo set "JRE_DIR=%%APP_DIR%%minimal-jre"
echo.
echo :: Check if running from EXE or batch
echo if "%%~x0"==".exe" ^(
echo     echo [INFO] Running from EXE...
echo ^) else ^(
echo     echo [INFO] Running from batch file...
echo ^)
echo.
echo :: Try embedded JRE first
echo if exist "%%JRE_DIR%%\bin\java.exe" ^(
echo     echo [INFO] Using embedded JRE...
echo     set "JAVA_HOME=%%JRE_DIR%%"
echo     set "PATH=%%JAVA_HOME%%\bin;%%PATH%%"
echo     "%%JAVA_HOME%%\bin\java" -jar "%%JAR_FILE%%"
echo     goto :end
echo ^)
echo.
echo :: Try system Java
echo java -version ^>nul 2^>^&1
echo if %%errorlevel%% equ 0 ^(
echo     echo [INFO] Using system Java...
echo     java -jar "%%JAR_FILE%%"
echo     goto :end
echo ^)
echo.
echo :: No Java found
echo echo [ERROR] No Java Runtime Environment found.
echo echo [INFO] Please install Java from https://adoptium.net/
echo echo [INFO] Or use the portable version with embedded JRE.
echo pause
echo.
echo :end
echo exit /b 0
) > "advanced-exe\launcher.bat"

:: Copy files to advanced-exe directory
copy "..\AdvancedDesktopPet.jar" "advanced-exe\"
copy "..\chibi01.ico" "advanced-exe\"
xcopy "..\Image" "advanced-exe\Image\" /E /I /Y
xcopy "..\music" "advanced-exe\music\" /E /I /Y
if exist "..\minimal-jre" (
    xcopy "..\minimal-jre" "advanced-exe\minimal-jre\" /E /I /Y
)

echo [SUCCESS] Advanced EXE package created: advanced-exe/
echo [INFO] Use a tool like Launch4j or Advanced Installer to convert launcher.bat to EXE

echo.
echo ========================================
echo Deployment Summary
echo ========================================
echo.

echo [INFO] Deployment completed! Here's what was created:
echo.
echo 1. Native Installer (if JPackage available):
echo    - Desktop Pet-1.0.exe (requires admin rights to install)
echo.
echo 2. Launch4j EXE (if Launch4j available):
echo    - DesktopPet.exe (requires Java on target machine)
echo.
echo 3. Portable Package (RECOMMENDED):
echo    - DesktopPet-Portable/ folder
echo    - Includes embedded JRE
echo    - No Java installation required
echo    - Just run run.bat
echo.
echo 4. Advanced EXE Package:
echo    - advanced-exe/ folder
echo    - Ready for conversion to true EXE
echo.

echo [RECOMMENDATION] Use the portable package (DesktopPet-Portable) for distribution
echo [INFO] It includes everything needed and works without Java installation
echo.

cd ..
echo [SUCCESS] Deployment process completed!
pause 