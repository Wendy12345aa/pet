@echo off
echo Desktop Pet - Cleanup Tool
echo ==========================

echo.
echo Cleaning up temporary files and compilation artifacts...
echo.

:: Count files before cleanup
set /a CLASS_COUNT=0
set /a TOTAL_CLEANED=0

:: Count .class files
for %%f in (*.class) do (
    if exist "%%f" (
        set /a CLASS_COUNT+=1
    )
)

:: Delete .class files
if %CLASS_COUNT% gtr 0 (
    echo [INFO] Removing %CLASS_COUNT% .class files...
    del *.class 2>nul
    if %errorlevel% equ 0 (
        echo ✓ Successfully removed .class files
        set /a TOTAL_CLEANED+=%CLASS_COUNT%
    ) else (
        echo ✗ Failed to remove some .class files
    )
) else (
    echo [INFO] No .class files found
)

:: Clean up other temporary files
echo.
echo [INFO] Cleaning up other temporary files...

:: Remove temporary directories
if exist "temp" (
    echo [INFO] Removing temp/ directory...
    rmdir /s /q "temp" 2>nul
    if %errorlevel% equ 0 (
        echo ✓ Removed temp/ directory
        set /a TOTAL_CLEANED+=1
    )
)

:: Remove backup files
if exist "*.bak" (
    echo [INFO] Removing backup files...
    del *.bak 2>nul
    if %errorlevel% equ 0 (
        echo ✓ Removed backup files
        set /a TOTAL_CLEANED+=1
    )
)

:: Remove log files
if exist "*.log" (
    echo [INFO] Removing log files...
    del *.log 2>nul
    if %errorlevel% equ 0 (
        echo ✓ Removed log files
        set /a TOTAL_CLEANED+=1
    )
)

:: Remove Windows thumbnail cache
if exist "Thumbs.db" (
    echo [INFO] Removing thumbnail cache...
    attrib -h -s "Thumbs.db" 2>nul
    del "Thumbs.db" 2>nul
    if %errorlevel% equ 0 (
        echo ✓ Removed thumbnail cache
        set /a TOTAL_CLEANED+=1
    )
)

:: Remove macOS system files
if exist ".DS_Store" (
    echo [INFO] Removing macOS system files...
    del ".DS_Store" 2>nul
    if %errorlevel% equ 0 (
        echo ✓ Removed .DS_Store files
        set /a TOTAL_CLEANED+=1
    )
)

:: Clean up old deployment artifacts
if exist "DesktopPet.exe" (
    echo [INFO] Found old EXE file. Remove it? (y/n)
    set /p REMOVE_EXE="Remove DesktopPet.exe? (y/n): "
    if /i "!REMOVE_EXE!"=="y" (
        del "DesktopPet.exe" 2>nul
        if %errorlevel% equ 0 (
            echo ✓ Removed old EXE file
            set /a TOTAL_CLEANED+=1
        )
    )
)

if exist "DesktopPet.bat" (
    echo [INFO] Found old batch launcher. Remove it? (y/n)
    set /p REMOVE_BAT="Remove DesktopPet.bat? (y/n): "
    if /i "!REMOVE_BAT!"=="y" (
        del "DesktopPet.bat" 2>nul
        if %errorlevel% equ 0 (
            echo ✓ Removed old batch launcher
            set /a TOTAL_CLEANED+=1
        )
    )
)

:: Summary
echo.
echo ========================================
echo           CLEANUP SUMMARY
echo ========================================
echo.
if %TOTAL_CLEANED% gtr 0 (
    echo ✓ Cleanup completed successfully!
    echo ✓ Total items cleaned: %TOTAL_CLEANED%
) else (
    echo ✓ Project is already clean!
    echo ✓ No cleanup needed
)

echo.
echo The following files are preserved:
echo ✓ AdvancedDesktopPet.java (source code)
echo ✓ AdvancedDesktopPet.jar (if exists)
echo ✓ Image/ directory (assets)
echo ✓ music/ directory (audio files)
echo ✓ All batch files and scripts
echo ✓ Documentation files
echo.

echo Your project is now clean and ready for development!
echo. 