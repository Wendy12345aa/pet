@echo off
echo Creating Simple Launcher for Desktop Pet
echo ========================================

echo.
echo Creating batch file launcher for the portable package...
echo.

echo Creating DesktopPet.bat launcher...
(
echo @echo off
echo setlocal enabledelayedexpansion
echo.
echo cd /d "%%~dp0"
echo.
echo set "JAVA_EXE=jre\bin\java.exe"
echo set "JAR_FILE=lib\AdvancedDesktopPet.jar"
echo.
echo echo Desktop Pet Launcher
echo echo ===================
echo echo.
echo.
echo if exist "%%JAVA_EXE%%" ^(
echo     echo [INFO] Using embedded JRE - no Java installation required!
echo     "%%JAVA_EXE%%" -jar "%%JAR_FILE%%"
echo ^) else ^(
echo     echo [INFO] Checking for system Java...
echo     java -version ^>nul 2^>^&1
echo     if %%errorlevel%% equ 0 ^(
echo         echo [INFO] Using system Java...
echo         java -jar "%%JAR_FILE%%"
echo     ^) else ^(
echo         echo [ERROR] No Java found. Please install Java or use the embedded version.
echo         pause
echo     ^)
echo ^)
echo.
echo pause
) > "DesktopPet.bat"

echo DesktopPet.bat created successfully!
pause

echo.
echo Copying launcher to portable folder...
copy "DesktopPet.bat" "DesktopPet-Portable-EXE\" >nul
if errorlevel 1 (
    echo ERROR: Failed to copy launcher to portable folder!
    pause
    exit /b 1
)
echo Launcher copied to portable folder successfully!
pause

echo.
echo Creating desktop shortcut...
echo Creating shortcut script...
(
echo @echo off
echo echo Creating desktop shortcut...
echo.
echo set "DESKTOP=%%USERPROFILE%%\Desktop"
echo set "SHORTCUT=%%DESKTOP%%\Desktop Pet.bat"
echo set "TARGET=%%~dp0DesktopPet-Portable-EXE\DesktopPet.bat"
echo.
echo echo @echo off ^> "%%SHORTCUT%%"
echo echo cd /d "%%~dp0DesktopPet-Portable-EXE" ^>^> "%%SHORTCUT%%"
echo echo call "%%TARGET%%" ^>^> "%%SHORTCUT%%"
echo.
echo echo Desktop shortcut created: %%SHORTCUT%%
echo pause
) > "create_shortcut.bat"

echo Shortcut creation script created!
pause

echo.
echo ✓ Simple launcher package created successfully!
echo.
echo The DesktopPet-Portable-EXE folder now contains:
echo   - DesktopPet.bat (batch file launcher)
echo   - jre/ (embedded Java runtime - no Java installation needed!)
echo   - lib/AdvancedDesktopPet.jar (your application)
echo   - resources/Image/ (ALL image files: chibi01-03.png, enemy01-03.png, chibi01.ico)
echo   - resources/music/ (ALL music files: normal.wav, horror.wav)
echo   - chibi01.ico (application icon)
echo.
echo To create desktop shortcut, run: create_shortcut.bat
echo.
echo Distribution options:
echo   1. Zip the entire DesktopPet-Portable-EXE folder
echo   2. Copy the folder to any Windows computer
echo   3. Users can run DesktopPet.bat
echo   4. No Java installation required on target computers!
echo.
echo The package is ready for distribution!
echo Press any key to close...
pause 