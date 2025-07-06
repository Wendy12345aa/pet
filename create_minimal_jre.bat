@echo off
echo Creating Minimal JRE for Desktop Pet
echo ====================================

echo.
echo This script will create a minimal JRE with only the modules needed for the desktop pet.
echo.

echo 1. Checking Java installation...
java -version 2>nul
if errorlevel 1 (
    echo ERROR: Java not found! Please install Java 11 or later.
    pause
    exit /b 1
)

echo.
echo 2. Checking jlink availability...
jlink --version 2>nul
if errorlevel 1 (
    echo ERROR: jlink not found! Please install Java 9 or later (JDK).
    pause
    exit /b 1
)

echo.
echo 3. Creating minimal JRE...
echo    This may take a few minutes...

if exist "minimal-jre" (
    echo Removing existing minimal-jre folder...
    rmdir /s /q "minimal-jre"
)

jlink --module-path "%JAVA_HOME%\jmods" ^
      --add-modules java.base,java.desktop,java.logging,java.prefs,jdk.unsupported ^
      --output minimal-jre ^
      --compress=2 ^
      --no-header-files ^
      --no-man-pages

if errorlevel 1 (
    echo ERROR: Failed to create minimal JRE!
    echo Make sure JAVA_HOME is set correctly.
    pause
    exit /b 1
)

echo.
echo 4. Testing minimal JRE...
echo    Testing with a simple Java version check...
minimal-jre\bin\java -version
if errorlevel 1 (
    echo ERROR: Minimal JRE test failed!
    pause
    exit /b 1
)

echo.
echo 5. Copying minimal JRE to portable folder...
if exist "DesktopPet-Portable\minimal-jre" (
    echo Removing existing minimal-jre from portable folder...
    rmdir /s /q "DesktopPet-Portable\minimal-jre"
)

xcopy "minimal-jre" "DesktopPet-Portable\minimal-jre" /e /i /q
if errorlevel 1 (
    echo ERROR: Failed to copy minimal JRE to portable folder!
    pause
    exit /b 1
)

echo.
echo 6. Creating portable launcher...
echo @echo off > "DesktopPet-Portable\run_portable.bat"
echo echo Starting Desktop Pet with embedded JRE... >> "DesktopPet-Portable\run_portable.bat"
echo minimal-jre\bin\java -jar AdvancedDesktopPet.jar >> "DesktopPet-Portable\run_portable.bat"
echo pause >> "DesktopPet-Portable\run_portable.bat"

echo.
echo 7. Testing portable version...
cd DesktopPet-Portable
echo Testing portable version with embedded JRE...
run_portable.bat

echo.
echo ✓ Minimal JRE created successfully!
echo.
echo The portable folder now contains:
echo   - minimal-jre/ (embedded Java runtime)
echo   - AdvancedDesktopPet.jar (your application)
echo   - Image/ (image files)
echo   - music/ (audio files)
echo   - run_portable.bat (launcher script)
echo.
echo You can now distribute the entire DesktopPet-Portable folder
echo and it will work on any Windows system without requiring Java installation.
echo.
pause 