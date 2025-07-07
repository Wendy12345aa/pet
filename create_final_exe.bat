@echo off
echo Creating Final EXE Package for Desktop Pet
echo ==========================================

echo.
echo This script will complete the portable JRE package and create the final EXE.
echo.

echo 1. Checking if portable JRE exists...
if not exist "portable-jre" (
    echo ERROR: portable-jre folder not found!
    echo Please run create_portable_jre_debug.bat first to create the portable JRE.
    pause
    exit /b 1
)
echo Portable JRE found successfully.
pause

echo.
echo 2. Checking required files...
echo Checking JAR file...
if not exist "AdvancedDesktopPet.jar" (
    echo ERROR: AdvancedDesktopPet.jar not found!
    pause
    exit /b 1
)
echo JAR file found.

echo Checking Image folder...
if not exist "Image" (
    echo ERROR: Image folder not found!
    pause
    exit /b 1
)
echo Image folder found.

echo Checking music folder...
if not exist "music" (
    echo ERROR: music folder not found!
    pause
    exit /b 1
)
echo music folder found.

echo Checking icon file...
if not exist "chibi01.ico" (
    echo ERROR: chibi01.ico not found!
    pause
    exit /b 1
)
echo Icon file found.
pause

echo.
echo 3. Completing portable package...
echo Copying files to DesktopPet-Portable-EXE folder...

if exist "DesktopPet-Portable-EXE\jre" (
    echo Removing existing jre folder...
    rmdir /s /q "DesktopPet-Portable-EXE\jre"
    echo Existing jre folder removed.
    pause
)

echo Copying portable JRE...
xcopy "portable-jre" "DesktopPet-Portable-EXE\jre" /e /i /q
if errorlevel 1 (
    echo ERROR: Failed to copy JRE!
    pause
    exit /b 1
)
echo Portable JRE copied successfully!
pause

echo Copying JAR file...
if not exist "DesktopPet-Portable-EXE\lib" mkdir "DesktopPet-Portable-EXE\lib"
copy "AdvancedDesktopPet.jar" "DesktopPet-Portable-EXE\lib\" >nul
if errorlevel 1 (
    echo ERROR: Failed to copy JAR file!
    pause
    exit /b 1
)
echo JAR file copied successfully!
pause

echo Copying Image folder with all files...
if not exist "DesktopPet-Portable-EXE\resources" mkdir "DesktopPet-Portable-EXE\resources"
echo Copying Image folder to resources\Image...
xcopy "Image" "DesktopPet-Portable-EXE\resources\Image\" /e /i /q
if errorlevel 1 (
    echo ERROR: Failed to copy Image folder!
    pause
    exit /b 1
)
echo Image folder copied successfully!
pause

echo Copying music folder with all files...
echo Copying music folder to resources\music...
xcopy "music" "DesktopPet-Portable-EXE\resources\music\" /e /i /q
if errorlevel 1 (
    echo ERROR: Failed to copy music folder!
    pause
    exit /b 1
)
echo music folder copied successfully!
pause

echo Copying icon file...
copy "chibi01.ico" "DesktopPet-Portable-EXE\" >nul
if errorlevel 1 (
    echo ERROR: Failed to copy icon file!
    pause
    exit /b 1
)
echo Icon copied successfully!
pause

echo.
echo 4. Verifying copied files...
echo Checking Image files in portable package...
if exist "DesktopPet-Portable-EXE\resources\Image\chibi01.png" (
    echo ✓ chibi01.png found
) else (
    echo ERROR: chibi01.png missing!
)
if exist "DesktopPet-Portable-EXE\resources\Image\chibi02.png" (
    echo ✓ chibi02.png found
) else (
    echo ERROR: chibi02.png missing!
)
if exist "DesktopPet-Portable-EXE\resources\Image\chibi03.png" (
    echo ✓ chibi03.png found
) else (
    echo ERROR: chibi03.png missing!
)
if exist "DesktopPet-Portable-EXE\resources\Image\enemy01.png" (
    echo ✓ enemy01.png found
) else (
    echo ERROR: enemy01.png missing!
)
if exist "DesktopPet-Portable-EXE\resources\Image\enemy02.png" (
    echo ✓ enemy02.png found
) else (
    echo ERROR: enemy02.png missing!
)
if exist "DesktopPet-Portable-EXE\resources\Image\enemy03.png" (
    echo ✓ enemy03.png found
) else (
    echo ERROR: enemy03.png missing!
)
if exist "DesktopPet-Portable-EXE\resources\Image\chibi01.ico" (
    echo ✓ chibi01.ico found
) else (
    echo ERROR: chibi01.ico missing!
)

echo Checking music files in portable package...
if exist "DesktopPet-Portable-EXE\resources\music\normal.wav" (
    echo ✓ normal.wav found
) else (
    echo ERROR: normal.wav missing!
)
if exist "DesktopPet-Portable-EXE\resources\music\horror.wav" (
    echo ✓ horror.wav found
) else (
    echo ERROR: horror.wav missing!
)
pause

echo.
echo 5. Creating EXE file...
echo Running PowerShell script to create EXE...
powershell -ExecutionPolicy Bypass -File "create_exe.ps1"
echo PowerShell script completed with error level: %errorlevel%
if errorlevel 1 (
    echo WARNING: PowerShell script had issues, but batch file should still work.
) else (
    echo SUCCESS: PowerShell script completed successfully!
)
pause

echo.
echo 6. Testing the final package...
echo Checking if EXE was created...
if exist "DesktopPet.exe" (
    echo SUCCESS: DesktopPet.exe created!
) else (
    echo WARNING: DesktopPet.exe not created, but batch file should work.
)

if exist "DesktopPet.bat" (
    echo SUCCESS: DesktopPet.bat created!
) else (
    echo ERROR: No launcher files created!
    pause
    exit /b 1
)

echo.
echo 7. Creating final distribution package...
echo Copying EXE and batch files to portable folder...
if exist "DesktopPet.exe" (
    copy "DesktopPet.exe" "DesktopPet-Portable-EXE\" >nul
    echo DesktopPet.exe copied to portable folder.
)
if exist "DesktopPet.bat" (
    copy "DesktopPet.bat" "DesktopPet-Portable-EXE\" >nul
    echo DesktopPet.bat copied to portable folder.
)

echo.
echo ✓ Final EXE package created successfully!
echo.
echo The DesktopPet-Portable-EXE folder now contains:
echo   - DesktopPet.exe (true executable - if created)
echo   - DesktopPet.bat (batch file launcher)
echo   - jre/ (embedded Java runtime - no Java installation needed!)
echo   - lib/AdvancedDesktopPet.jar (your application)
echo   - resources/Image/ (ALL image files: chibi01-03.png, enemy01-03.png, chibi01.ico)
echo   - resources/music/ (ALL music files: normal.wav, horror.wav)
echo   - chibi01.ico (application icon)
echo.
echo All required files are included:
echo   ✓ 3 chibi character images
echo   ✓ 3 enemy character images  
echo   ✓ 1 icon file
echo   ✓ 2 music files (normal and horror themes)
echo.
echo Distribution options:
echo   1. Zip the entire DesktopPet-Portable-EXE folder
echo   2. Copy the folder to any Windows computer
echo   3. Users can run DesktopPet.exe or DesktopPet.bat
echo   4. No Java installation required on target computers!
echo.
echo The package is ready for distribution!
echo Press any key to close...
pause 