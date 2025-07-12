@echo off
echo ========================================
echo PORTABLE JRE CREATOR
echo ========================================
echo.

REM Check if we can find the main Java file
if not exist "..\..\src\main\java\AdvancedDesktopPet.java" (
    echo ERROR: AdvancedDesktopPet.java not found in expected location!
    echo Expected location: ..\..\src\main\java\AdvancedDesktopPet.java
    pause
    exit /b 1
)

echo Found AdvancedDesktopPet.java - proceeding...
echo.

REM Navigate to project root
cd ..\..

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java not found in PATH
    echo Please install Java and add it to your PATH
    pause
    exit /b 1
)

echo Java found - proceeding...
echo.

REM Try to find Java installation directory
echo Finding Java installation...
for /f "tokens=2 delims==" %%i in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr "java.home"') do (
    set "AUTO_JAVA_HOME=%%i"
)

REM Remove leading and trailing spaces
for /f "tokens=* delims= " %%i in ("%AUTO_JAVA_HOME%") do set "AUTO_JAVA_HOME=%%i"

echo Found Java at: "%AUTO_JAVA_HOME%"
echo.

REM Remove existing portable-jre if it exists
if exist "portable-jre" (
    echo Removing existing portable-jre...
    rmdir /s /q "portable-jre"
)

REM Create portable-jre directory
echo Creating portable-jre directory...
mkdir "portable-jre"
mkdir "portable-jre\bin"
mkdir "portable-jre\lib"

REM Copy essential files
echo Copying Java runtime files...
echo This may take a moment...

REM Copy executables
if exist "%AUTO_JAVA_HOME%\bin\java.exe" (
    xcopy "%AUTO_JAVA_HOME%\bin\java.exe" "portable-jre\bin\" /Y /Q
    echo Copied java.exe
) else (
    echo WARNING: java.exe not found at "%AUTO_JAVA_HOME%\bin\java.exe"
)

if exist "%AUTO_JAVA_HOME%\bin\javaw.exe" (
    xcopy "%AUTO_JAVA_HOME%\bin\javaw.exe" "portable-jre\bin\" /Y /Q
    echo Copied javaw.exe
) else (
    echo WARNING: javaw.exe not found at "%AUTO_JAVA_HOME%\bin\javaw.exe"
)

if exist "%AUTO_JAVA_HOME%\bin\javac.exe" (
    xcopy "%AUTO_JAVA_HOME%\bin\javac.exe" "portable-jre\bin\" /Y /Q
    echo Copied javac.exe
) else (
    echo WARNING: javac.exe not found at "%AUTO_JAVA_HOME%\bin\javac.exe"
)

REM Copy essential libraries
if exist "%AUTO_JAVA_HOME%\lib" (
    xcopy "%AUTO_JAVA_HOME%\lib\*" "portable-jre\lib\" /E /Y /Q
    echo Copied lib directory
) else (
    echo WARNING: lib directory not found at "%AUTO_JAVA_HOME%\lib"
)

REM Test the portable JRE
echo.
echo Testing portable JRE...
"portable-jre\bin\java.exe" -version >nul 2>&1
if %errorlevel% equ 0 (
    echo SUCCESS: Portable JRE created and tested successfully!
    echo.
    echo Testing compilation...
    cd src\main\java
    "..\..\..\portable-jre\bin\javac.exe" AdvancedDesktopPet.java MusicManager.java LocationUtils.java >nul 2>&1
    if %errorlevel% equ 0 (
        echo SUCCESS: Compilation test passed!
        echo.
        echo SUCCESS: Your portable JRE is ready!
        echo Location: portable-jre\
        echo.
        echo You can now run your application using:
        echo   portable-jre\bin\java.exe -cp src\main\java AdvancedDesktopPet
    ) else (
        echo ERROR: Compilation test failed
    )
    cd ..\..\..
) else (
    echo ERROR: Portable JRE test failed
)

echo.
echo Portable JRE creation completed!
echo Press any key to exit...
pause >nul 