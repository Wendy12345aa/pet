@echo off
echo Creating Portable Desktop Pet (Alternative Method)
echo =================================================

echo.
echo This method creates a portable version without requiring jlink.
echo It will create a self-contained package that includes Java instructions.
echo.

echo 1. Checking current setup...
if not exist "DesktopPet-Portable\AdvancedDesktopPet.jar" (
    echo ERROR: JAR file not found in DesktopPet-Portable folder!
    echo Please run create_jar.bat first.
    pause
    exit /b 1
)

echo ✓ JAR file found

echo.
echo 2. Creating enhanced launcher scripts...

echo Creating Windows launcher...
(
echo @echo off
echo echo ========================================
echo echo Desktop Pet - Portable Version
echo echo ========================================
echo echo.
echo echo Checking Java installation...
echo java -version ^>nul 2^>^&1
echo if errorlevel 1 ^(
echo     echo ERROR: Java not found!
echo     echo.
echo     echo This application requires Java to run.
echo     echo Please install Java from: https://adoptium.net/
echo     echo.
echo     echo After installing Java, run this script again.
echo     pause
echo     exit /b 1
echo ^)
echo echo ✓ Java found
echo echo.
echo echo Starting Desktop Pet...
echo java -jar AdvancedDesktopPet.jar
echo echo.
echo echo Desktop Pet has been closed.
echo pause
) > "DesktopPet-Portable\run_desktop_pet.bat"

echo Creating simple launcher...
(
echo @echo off
echo java -jar AdvancedDesktopPet.jar
) > "DesktopPet-Portable\run_simple.bat"

echo.
echo 3. Creating README file...
(
echo Desktop Pet - Portable Version
echo ==============================
echo.
echo This is a portable version of the Desktop Pet application.
echo.
echo REQUIREMENTS:
echo - Java 8 or later must be installed on the target system
echo - Download Java from: https://adoptium.net/
echo.
echo HOW TO RUN:
echo 1. Double-click "run_desktop_pet.bat" ^(recommended^)
echo    - This will check if Java is installed and provide helpful messages
echo.
echo 2. Or double-click "run_simple.bat" ^(if you know Java is installed^)
echo    - This will start the application directly
echo.
echo 3. Or run from command line:
echo    java -jar AdvancedDesktopPet.jar
echo.
echo FILES INCLUDED:
echo - AdvancedDesktopPet.jar ^(the main application^)
echo - Image/ ^(contains pet images^)
echo - music/ ^(contains audio files^)
echo - run_desktop_pet.bat ^(launcher with Java check^)
echo - run_simple.bat ^(simple launcher^)
echo.
echo TROUBLESHOOTING:
echo - If you get "Java not found" error, install Java from https://adoptium.net/
echo - If images don't appear, make sure the Image/ folder is present
echo - If music doesn't play, make sure the music/ folder is present
echo.
echo CONTROLS:
echo - Left-click and drag to move the pet
echo - Middle-click for settings
echo - Right-click for special animations
echo - Double-click for jump animation
echo.
echo Enjoy your desktop pet!
) > "DesktopPet-Portable\README_Portable.txt"

echo.
echo 4. Creating installer script...
(
echo @echo off
echo echo Creating Desktop Pet shortcut...
echo.
echo set "SCRIPT_DIR=%%~dp0"
echo set "JAR_PATH=%%SCRIPT_DIR%%AdvancedDesktopPet.jar"
echo.
echo echo Creating shortcut on desktop...
echo powershell -Command "& {$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut^('%%USERPROFILE%%\Desktop\Desktop Pet.lnk'^); $Shortcut.TargetPath = 'java'; $Shortcut.Arguments = '-jar \"%%JAR_PATH%%\"'; $Shortcut.WorkingDirectory = '%%SCRIPT_DIR%%'; $Shortcut.IconLocation = '%%SCRIPT_DIR%%Image\chibi01.ico'; $Shortcut.Save^(^)}"
echo.
echo echo Creating shortcut in start menu...
echo powershell -Command "& {$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut^('%%APPDATA%%\Microsoft\Windows\Start Menu\Programs\Desktop Pet.lnk'^); $Shortcut.TargetPath = 'java'; $Shortcut.Arguments = '-jar \"%%JAR_PATH%%\"'; $Shortcut.WorkingDirectory = '%%SCRIPT_DIR%%'; $Shortcut.IconLocation = '%%SCRIPT_DIR%%Image\chibi01.ico'; $Shortcut.Save^(^)}"
echo.
echo echo ✓ Desktop Pet shortcuts created!
echo echo.
echo echo You can now:
echo echo - Run Desktop Pet from the desktop shortcut
echo echo - Find Desktop Pet in the Start Menu
echo echo - Or run run_desktop_pet.bat from this folder
echo.
pause
) > "DesktopPet-Portable\install_shortcuts.bat"

echo.
echo 5. Creating ZIP package...
if exist "DesktopPet-Portable-Updated.zip" (
    del "DesktopPet-Portable-Updated.zip"
)

echo Creating ZIP file...
powershell -Command "Compress-Archive -Path 'DesktopPet-Portable\*' -DestinationPath 'DesktopPet-Portable-Updated.zip' -Force"

if errorlevel 1 (
    echo WARNING: Could not create ZIP file. You can manually zip the DesktopPet-Portable folder.
) else (
    echo ✓ ZIP file created: DesktopPet-Portable-Updated.zip
)

echo.
echo ========================================
echo PORTABLE VERSION CREATED SUCCESSFULLY!
echo ========================================
echo.
echo The DesktopPet-Portable folder now contains:
echo ✓ Enhanced launcher scripts
echo ✓ README file with instructions
echo ✓ Installer script for shortcuts
echo ✓ All necessary files
echo.
echo You can now:
echo 1. Test it by running: DesktopPet-Portable\run_desktop_pet.bat
echo 2. Distribute the DesktopPet-Portable folder
echo 3. Or distribute the DesktopPet-Portable-Updated.zip file
echo.
echo The portable version will work on any Windows system with Java installed.
echo.
pause 