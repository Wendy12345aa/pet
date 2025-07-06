@echo off
echo Downloading Minimal JRE for Desktop Pet
echo =======================================

echo.
echo This script will help you download and set up a minimal JRE
echo for the portable Desktop Pet application.
echo.

echo 1. Checking if DesktopPet-Portable-EXE exists...
if not exist "DesktopPet-Portable-EXE" (
    echo ERROR: DesktopPet-Portable-EXE folder not found!
    echo Please run create_portable_exe.bat first.
    pause
    exit /b 1
)

echo ✓ DesktopPet-Portable-EXE folder found

echo.
echo 2. Creating jre directory...
if not exist "DesktopPet-Portable-EXE\jre" (
    mkdir "DesktopPet-Portable-EXE\jre"
    echo ✓ Created jre directory
) else (
    echo ✓ jre directory already exists
)

echo.
echo 3. Checking for existing JRE...
if exist "DesktopPet-Portable-EXE\jre\bin\java.exe" (
    echo ✓ JRE already exists in DesktopPet-Portable-EXE\jre\
    echo.
    echo The portable version is ready to use!
    echo You can run DesktopPet-Portable-EXE\DesktopPet.exe.bat
    pause
    exit /b 0
)

echo.
echo 4. Manual JRE Setup Required
echo ============================
echo.
echo Since automatic JRE creation may not work on all systems,
echo you need to manually download and extract a JRE.
echo.
echo STEPS TO FOLLOW:
echo.
echo 1. Download JRE from one of these sources:
echo    - https://adoptium.net/temurin/releases/ (recommended)
echo    - https://www.oracle.com/java/technologies/downloads/
echo    - https://www.azul.com/downloads/?package=jre
echo.
echo 2. Choose:
echo    - Windows x64 JRE (not JDK)
echo    - Version 8, 11, or 17 (any will work)
echo    - ZIP format (not installer)
echo.
echo 3. Extract the downloaded ZIP file
echo.
echo 4. Copy ALL contents from the extracted folder to:
echo    DesktopPet-Portable-EXE\jre\
echo.
echo 5. Verify the structure looks like:
echo    DesktopPet-Portable-EXE\jre\bin\java.exe
echo    DesktopPet-Portable-EXE\jre\lib\
echo    DesktopPet-Portable-EXE\jre\conf\
echo.
echo 6. Test by running:
echo    DesktopPet-Portable-EXE\DesktopPet.exe.bat
echo.
echo ALTERNATIVE: Use system Java
echo ============================
echo.
echo If you don't want to include a JRE, you can modify the launcher
echo to use the system Java (if installed):
echo.
echo 1. Edit DesktopPet-Portable-EXE\DesktopPet.exe.bat
echo 2. Replace the JAVA_HOME line with:
echo    set "JAVA_HOME="
echo 3. Replace the java command with:
echo    java -jar "%%~dp0lib\AdvancedDesktopPet.jar"
echo.
echo This will work on PCs that have Java installed.
echo.
echo Would you like me to:
echo 1. Open the JRE download page in your browser
echo 2. Create a modified launcher that uses system Java
echo 3. Exit
echo.
set /p choice="Enter your choice (1-3): "

if "%choice%"=="1" (
    echo Opening JRE download page...
    start https://adoptium.net/temurin/releases/
    echo.
    echo Browser opened. Follow the manual steps above.
) else if "%choice%"=="2" (
    echo Creating system Java launcher...
    (
    echo @echo off
    echo echo ========================================
    echo echo Desktop Pet - System Java Version
    echo echo ========================================
    echo echo.
    echo echo This version uses the system Java installation.
    echo echo.
    echo echo Starting Desktop Pet...
    echo.
    echo java -jar "%%~dp0lib\AdvancedDesktopPet.jar"
    echo.
    echo echo Desktop Pet has been closed.
    echo pause
    ) > "DesktopPet-Portable-EXE\DesktopPet-SystemJava.bat"
    echo ✓ Created DesktopPet-SystemJava.bat
    echo.
    echo This launcher will work on PCs with Java installed.
    echo Users without Java will need to install it first.
) else (
    echo Exiting...
)

echo.
echo ========================================
echo JRE Setup Instructions Complete
echo ========================================
echo.
echo Remember: The portable version needs either:
echo - A JRE in the jre/ folder, OR
echo - Java installed on the target PC
echo.
pause 