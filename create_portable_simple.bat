@echo off
echo Creating Simple Portable Desktop Pet
echo ====================================

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
echo 2. Creating simple launcher...
(
echo @echo off
echo echo Starting Desktop Pet...
echo java -jar AdvancedDesktopPet.jar
echo pause
) > "DesktopPet-Portable\run.bat"

echo.
echo 3. Creating launcher with Java check...
(
echo @echo off
echo echo Checking Java installation...
echo java -version ^>nul 2^>^&1
echo if errorlevel 1 ^(
echo     echo ERROR: Java not found!
echo     echo Please install Java from: https://adoptium.net/
echo     pause
echo     exit /b 1
echo ^)
echo echo Starting Desktop Pet...
echo java -jar AdvancedDesktopPet.jar
echo pause
) > "DesktopPet-Portable\run_with_check.bat"

echo.
echo 4. Creating simple README...
(
echo Desktop Pet - Portable Version
echo ==============================
echo.
echo REQUIREMENTS: Java 8 or later
echo DOWNLOAD JAVA: https://adoptium.net/
echo.
echo HOW TO RUN:
echo 1. Double-click "run_with_check.bat" ^(recommended^)
echo 2. Or double-click "run.bat" ^(if Java is installed^)
echo 3. Or run: java -jar AdvancedDesktopPet.jar
echo.
echo CONTROLS:
echo - Left-click and drag to move
echo - Middle-click for settings
echo - Right-click for animations
echo - Double-click to jump
) > "DesktopPet-Portable\README.txt"

echo.
echo 5. Testing the launcher...
echo Testing run_with_check.bat...
cd DesktopPet-Portable
run_with_check.bat
cd ..

echo.
echo ========================================
echo SIMPLE PORTABLE VERSION CREATED!
echo ========================================
echo.
echo Files created in DesktopPet-Portable:
echo ✓ run.bat ^(simple launcher^)
echo ✓ run_with_check.bat ^(launcher with Java check^)
echo ✓ README.txt ^(instructions^)
echo.
echo You can now distribute the DesktopPet-Portable folder.
echo.
pause 