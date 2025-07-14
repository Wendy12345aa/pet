@echo off
chcp 65001 >nul
echo.
echo 🧹 Cleaning up unused folders and files...
echo.

REM Check if running from the correct directory
if not exist "src\main\java\AdvancedDesktopPet.java" (
    echo ❌ Error: Please run this script from the project root directory
    echo    (where src\main\java\AdvancedDesktopPet.java exists)
    pause
    exit /b 1
)

echo 📋 Analysis of folders to remove:
echo.

REM Check and remove examples folder
if exist "examples" (
    echo 🗑️  Removing examples/ folder (unused development examples)
    rmdir /s /q "examples"
    echo    ✅ examples/ removed
) else (
    echo    ℹ️  examples/ already removed
)

REM Check and remove bug_fix folder
if exist "bug_fix" (
    echo 🗑️  Removing bug_fix/ folder (historical documentation)
    rmdir /s /q "bug_fix"
    echo    ✅ bug_fix/ removed
) else (
    echo    ℹ️  bug_fix/ already removed
)

REM Check and remove deployment output files
if exist "DesktopPet-EXE.zip" (
    echo 🗑️  Removing DesktopPet-EXE.zip (deployment output - 224MB)
    del "DesktopPet-EXE.zip"
    echo    ✅ DesktopPet-EXE.zip removed
) else (
    echo    ℹ️  DesktopPet-EXE.zip already removed
)

if exist "DesktopPet.exe" (
    echo 🗑️  Removing DesktopPet.exe (deployment output)
    del "DesktopPet.exe"
    echo    ✅ DesktopPet.exe removed
) else (
    echo    ℹ️  DesktopPet.exe already removed
)

if exist "DesktopPet-EXE" (
    echo 🗑️  Removing DesktopPet-EXE/ folder (deployment output)
    rmdir /s /q "DesktopPet-EXE"
    echo    ✅ DesktopPet-EXE/ removed
) else (
    echo    ℹ️  DesktopPet-EXE/ already removed
)

echo.
echo 📊 Summary of what was removed:
echo    • examples/ - Unused development examples
echo    • bug_fix/ - Historical bug fix documentation
echo    • DesktopPet-EXE.zip - Large deployment file (224MB)
echo    • DesktopPet.exe - Deployment executable
echo    • DesktopPet-EXE/ - Deployment folder
echo.
echo 📁 Folders that were KEPT (still needed):
echo    • portable-jre/ - Used by build scripts
echo    • target/ - Build output (in .gitignore)
echo    • .idea/ - IDE files (in .gitignore)
echo    • src/ - Source code
echo    • resources/ - Game resources
echo    • Image/ - Image assets
echo    • music/ - Audio files
echo    • scripts/ - Build scripts
echo    • docs/ - Documentation
echo.
echo ✅ Cleanup completed! Repository is now cleaner.
echo.
echo 💡 Note: Deployment files can be recreated using:
echo    • create_final_exe.bat - Creates new deployment package
echo.
pause 