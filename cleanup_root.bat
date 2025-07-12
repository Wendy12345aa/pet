@echo off
echo ========================================
echo Desktop Pet - Root Cleanup Tool
echo ========================================
echo.
echo This will remove the redundant files from your root directory.
echo These files are now safely organized in proper directories:
echo.
echo   Source files:     src\main\java\
echo   Scripts:          scripts\run\ and scripts\build\
echo   Documentation:    docs\
echo   Examples:         examples\
echo.
echo WARNING: This will DELETE the original files from root!
echo Make sure the organized versions work before proceeding.
echo.
set /p confirm="Do you want to clean up the root directory? (y/N): "
if /i not "%confirm%"=="y" (
    echo Operation cancelled.
    pause
    exit /b 1
)

echo.
echo Cleaning up root directory...

REM Remove source files (now in src/main/java/)
echo [1/5] Removing source files from root...
if exist "AdvancedDesktopPet.java" del "AdvancedDesktopPet.java"
if exist "MusicManager.java" del "MusicManager.java" 
if exist "LocationUtils.java" del "LocationUtils.java"

REM Remove old batch scripts (now in scripts/)
echo [2/5] Removing old batch scripts from root...
if exist "run_tests.bat" del "run_tests.bat"
if exist "run_enhanced.bat" del "run_enhanced.bat"
if exist "run.bat" del "run.bat"
if exist "DesktopPet.bat" del "DesktopPet.bat"
if exist "create_jar.bat" del "create_jar.bat"
if exist "create_final_exe.bat" del "create_final_exe.bat"
if exist "create_simple_launcher.bat" del "create_simple_launcher.bat"
if exist "create_exe.ps1" del "create_exe.ps1"
if exist "cleanup.bat" del "cleanup.bat"
if exist "create_shortcut.bat" del "create_shortcut.bat"
if exist "diagnose_java.bat" del "diagnose_java.bat"
if exist "create_portable_jre_debug.bat" del "create_portable_jre_debug.bat"

REM Remove organization scripts (no longer needed)
echo [3/5] Removing organization scripts...
if exist "organize_project.bat" del "organize_project.bat"
if exist "organize_project_safe.bat" del "organize_project_safe.bat"

REM Remove empty directories
echo [4/5] Removing empty directories...
if exist "utils" rmdir "utils" 2>nul

REM Clean up any remaining .class files
echo [5/5] Cleaning up class files...
if exist "*.class" del "*.class"

echo.
echo ========================================
echo Root cleanup complete!
echo ========================================
echo.
echo Your root directory is now clean with only essential files:
echo.
echo ✅ REMAINING IN ROOT (Essential):
echo   📄 README.md
echo   📄 QUICK_START.md  
echo   📄 STREAMLINED_WORKFLOW.md
echo   📄 character_defaults.properties
echo   📄 chibi01.ico
echo   📄 .gitignore
echo   📄 .gitattributes
echo   📄 pet.iml
echo   📁 resources\
echo   📁 Image\
echo   📁 music\
echo   📁 bug_fix\
echo   📁 DesktopPet-Portable-EXE\
echo   📁 portable-jre\
echo   📁 .git\
echo   📁 .idea\
echo.
echo ✅ ORGANIZED STRUCTURE:
echo   📁 src\main\java\     - Your source code
echo   📁 scripts\run\       - Run scripts  
echo   📁 scripts\build\     - Build scripts
echo   📁 docs\              - Documentation
echo   📁 examples\          - Examples
echo   📁 lib\               - External libraries
echo   📁 target\            - Build outputs
echo.
echo 🎯 NEW WORKFLOW:
echo   Development:  scripts\run\run_enhanced.bat
echo   Building:     scripts\build\create_jar.bat
echo   Testing:      scripts\run\run_tests.bat
echo.
echo Your project is now professional and organized! 🎉
pause 