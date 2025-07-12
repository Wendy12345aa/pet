@echo off
echo ========================================
echo Desktop Pet - Empty Folder Cleanup
echo ========================================
echo.
echo Removing unnecessary empty folders created during organization:
echo.
echo ❌ src\main\java\utils\          (empty)
echo ❌ src\main\java\resources\      (empty placeholder)
echo.
echo ✅ Keeping real resources at root level:
echo   📁 resources\CharacterSets\   (real data)
echo   📁 Image\                     (real images)  
echo   📁 music\                     (real sounds)
echo.
set /p confirm="Remove empty folders? (y/N): "
if /i not "%confirm%"=="y" (
    echo Operation cancelled.
    pause
    exit /b 1
)

echo.
echo Cleaning up empty folders...

REM Remove empty utils directory
if exist "src\main\java\utils" (
    echo Removing empty utils directory...
    rmdir /s /q "src\main\java\utils"
    echo ✅ Removed src\main\java\utils\
)

REM Remove empty resources directory and its empty subdirectories
if exist "src\main\java\resources" (
    echo Removing empty resources directory...
    rmdir /s /q "src\main\java\resources"
    echo ✅ Removed src\main\java\resources\
)

echo.
echo ========================================
echo Cleanup complete!
echo ========================================
echo.
echo Your project structure is now clean:
echo.
echo ✅ ORGANIZED SOURCE:
echo   📁 src\main\java\
echo   ├── AdvancedDesktopPet.java
echo   ├── MusicManager.java
echo   └── LocationUtils.java
echo.
echo ✅ REAL RESOURCES (at root):
echo   📁 resources\CharacterSets\   (your character data)
echo   📁 Image\                     (chibi01.png, etc.)
echo   📁 music\                     (normal.wav, horror.wav)
echo.
echo ✅ ORGANIZED SCRIPTS:
echo   📁 scripts\run\              (run_enhanced_fixed.bat)
echo   📁 scripts\build\            (create_jar.bat)
echo.
echo No more empty folders cluttering your project! 🎉
pause 