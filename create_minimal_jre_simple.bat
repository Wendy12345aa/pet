@echo off
echo Creating Minimal JRE for Desktop Pet (Simple Version)
echo ====================================================

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
echo 3. Finding Java installation...
set "JAVA_PATH="
for /f "tokens=*" %%i in ('where java') do (
    set "JAVA_PATH=%%i"
    goto :found_java
)
:found_java

if "%JAVA_PATH%"=="" (
    echo ERROR: Could not find Java installation!
    pause
    exit /b 1
)

echo Found Java at: %JAVA_PATH%

rem Extract the Java home directory
for %%i in ("%JAVA_PATH%") do set "JAVA_DIR=%%~dpi"
set "JAVA_HOME=%JAVA_DIR:~0,-1%"
echo Using JAVA_HOME: %JAVA_HOME%

echo.
echo 4. Creating minimal JRE...
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
    echo Trying alternative approach...
    
    rem Try with just the essential modules
    jlink --module-path "%JAVA_HOME%\jmods" ^
          --add-modules java.base,java.desktop ^
          --output minimal-jre ^
          --compress=2 ^
          --no-header-files ^
          --no-man-pages
    
    if errorlevel 1 (
        echo ERROR: Alternative approach also failed!
        echo Please check your Java installation.
        pause
        exit /b 1
    )
)

echo.
echo 5. Testing minimal JRE...
echo    Testing with a simple Java version check...
minimal-jre\bin\java -version
if errorlevel 1 (
    echo ERROR: Minimal JRE test failed!
    pause
    exit /b 1
)

echo.
echo 6. Copying minimal JRE to portable folder...
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
echo 7. Creating portable launcher...
(
echo @echo off
echo echo Starting Desktop Pet with embedded JRE...
echo minimal-jre\bin\java -jar AdvancedDesktopPet.jar
echo pause
) > "DesktopPet-Portable\run_portable.bat"

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