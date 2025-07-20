@echo off
chcp 65001 >nul
echo ========================================
echo Desktop Pet - Project Cleanup Tool
echo ========================================
echo.
echo This script will clean up your project by removing:
echo • Empty and unused directories
echo • Old files that have been moved to organized locations
echo • Build artifacts and temporary files
echo • Deployment outputs (can be recreated)
echo.
echo WARNING: This will DELETE files and folders!
echo Make sure you have backups if needed.
echo.
set /p confirm="Do you want to proceed with cleanup? (y/N): "
if /i not "%confirm%"=="y" (
    echo Operation cancelled.
    pause
    exit /b 1
)

echo.
echo 🧹 Starting comprehensive cleanup...
echo.

REM Check if running from the correct directory
if not exist "src\main\java\AdvancedDesktopPet.java" (
    echo ❌ Error: Please run this script from the project root directory
    echo    (where src\main\java\AdvancedDesktopPet.java exists)
    pause
    exit /b 1
)

echo 📋 Step 1: Removing empty directories...
echo.

REM Remove empty utils directory
if exist "src\main\java\utils" (
    echo 🗑️  Removing src\main\java\utils\ (empty)
    rmdir /s /q "src\main\java\utils"
    echo    ✅ Removed
) else (
    echo    ℹ️  src\main\java\utils\ already removed
)

REM Remove empty resources directory
if exist "src\main\java\resources" (
    echo 🗑️  Removing src\main\java\resources\ (empty)
    rmdir /s /q "src\main\java\resources"
    echo    ✅ Removed
) else (
    echo    ℹ️  src\main\java\resources\ already removed
)

REM Remove any other empty directories
if exist "utils" (
    echo 🗑️  Removing utils\ (empty)
    rmdir "utils" 2>nul
    echo    ✅ Removed
)

echo.
echo 📋 Step 2: Removing old source files from root...
echo.

REM Remove source files (now in src/main/java/)
if exist "AdvancedDesktopPet.java" (
    echo 🗑️  Removing AdvancedDesktopPet.java (moved to src\main\java\)
    del "AdvancedDesktopPet.java"
    echo    ✅ Removed
)
if exist "MusicManager.java" (
    echo 🗑️  Removing MusicManager.java (moved to src\main\java\)
    del "MusicManager.java"
    echo    ✅ Removed
)
if exist "LocationUtils.java" (
    echo 🗑️  Removing LocationUtils.java (moved to src\main\java\)
    del "LocationUtils.java"
    echo    ✅ Removed
)

echo.
echo 📋 Step 3: Removing old batch scripts from root...
echo.

REM Remove old batch scripts (now in scripts/)
if exist "run_tests.bat" del "run_tests.bat"
if exist "run_enhanced.bat" del "run_enhanced.bat"
if exist "run.bat" del "run.bat"
if exist "create_jar.bat" del "create_jar.bat"
if exist "create_final_exe.bat" del "create_final_exe.bat"
if exist "create_simple_launcher.bat" del "create_simple_launcher.bat"
if exist "create_exe.ps1" del "create_exe.ps1"
if exist "cleanup.bat" del "cleanup.bat"
if exist "create_shortcut.bat" del "create_shortcut.bat"
if exist "diagnose_java.bat" del "diagnose_java.bat"
if exist "create_portable_jre_debug.bat" del "create_portable_jre_debug.bat"
if exist "organize_project.bat" del "organize_project.bat"
if exist "organize_project_safe.bat" del "organize_project_safe.bat"

echo    ✅ Old scripts removed (now organized in scripts\ directory)

echo.
echo 📋 Step 4: Removing build artifacts...
echo.

REM Clean up class files
if exist "*.class" (
    echo 🗑️  Removing .class files from root
    del "*.class"
    echo    ✅ Removed
)

REM Clean up target directory (build outputs)
if exist "target" (
    echo 🗑️  Removing target\ directory (build outputs)
    rmdir /s /q "target"
    echo    ✅ Removed
)

echo.
echo 📋 Step 5: Removing deployment outputs...
echo.

REM Remove deployment files (can be recreated)
if exist "DesktopPet-EXE.zip" (
    echo 🗑️  Removing DesktopPet-EXE.zip (deployment output)
    del "DesktopPet-EXE.zip"
    echo    ✅ Removed
)
if exist "DesktopPet.exe" (
    echo 🗑️  Removing DesktopPet.exe (deployment output)
    del "DesktopPet.exe"
    echo    ✅ Removed
)
if exist "DesktopPet-EXE" (
    echo 🗑️  Removing DesktopPet-EXE\ folder (deployment output)
    rmdir /s /q "DesktopPet-EXE"
    echo    ✅ Removed
)

echo.
echo 📋 Step 6: Removing historical documentation...
echo.

REM Remove historical folders
if exist "bug_fix" (
    echo 🗑️  Removing bug_fix\ folder (historical documentation)
    rmdir /s /q "bug_fix"
    echo    ✅ Removed
)

echo.
echo ========================================
echo ✅ Cleanup completed successfully!
echo ========================================
echo.
echo 📁 Your project structure is now clean:
echo.
echo ✅ ESSENTIAL FILES (kept):
echo   📄 README.md, QUICK_START.md, STREAMLINED_WORKFLOW.md
echo   📄 character_defaults.properties, chibi01.ico
echo   📄 .gitignore, .gitattributes, pet.iml
echo   📁 resources\CharacterSets\   (character data)
echo   📁 Image\                     (image assets)
echo   📁 music\                     (audio files)
echo   📁 portable-jre\              (Java runtime)
echo   📁 .git\                      (version control)
echo   📁 .idea\                     (IDE files)
echo.
echo ✅ ORGANIZED STRUCTURE:
echo   📁 src\main\java\     - Source code
echo   📁 scripts\run\       - Run scripts
echo   📁 scripts\build\     - Build scripts
echo   📁 scripts\setup\     - Setup scripts
echo   📁 docs\              - Documentation
echo.
echo 🎯 RECOMMENDED WORKFLOW:
echo   Development:  quick_run.bat
echo   Testing:      scripts\run\run_tests.bat
echo   Building:     scripts\build\create_jar.bat
echo   Distribution: scripts\build\create_exe.bat
echo.
echo Your project is now clean and organized! 🎉
pause 