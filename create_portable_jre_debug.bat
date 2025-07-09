@echo off
echo ========================================
echo PORTABLE JRE DEBUG SCRIPT
echo ========================================
echo.

echo [DEBUG] Starting portable JRE creation with debug logging...
echo [DEBUG] Current directory: %CD%
echo [DEBUG] Script started at: %date% %time%
echo.

REM Check if we're in the right directory
if not exist "AdvancedDesktopPet.java" (
    echo [ERROR] AdvancedDesktopPet.java not found in current directory!
    echo [ERROR] Please run this script from the project root directory.
    echo [ERROR] Current directory contents:
    dir /b
    pause
    exit /b 1
)

echo [DEBUG] Found AdvancedDesktopPet.java - proceeding...
echo.

REM Check if portable-jre directory already exists
if exist "portable-jre" (
    echo [WARNING] portable-jre directory already exists!
    echo [DEBUG] Checking if it's a valid JRE installation...
    
    if exist "portable-jre\bin\java.exe" (
        echo [DEBUG] Found java.exe in portable-jre\bin\
        echo [DEBUG] Testing JRE version...
        "portable-jre\bin\java.exe" -version 2>&1
        if %errorlevel% equ 0 (
            echo [DEBUG] JRE appears to be working correctly
            echo [INFO] Portable JRE already exists and is functional
            goto :test_compilation
        ) else (
            echo [ERROR] JRE test failed with error code: %errorlevel%
            echo [WARNING] Existing JRE may be corrupted
        )
    ) else (
        echo [ERROR] java.exe not found in portable-jre\bin\
        echo [WARNING] Existing JRE installation appears incomplete
    )
    
    echo [DEBUG] Removing existing portable-jre directory...
    rmdir /s /q "portable-jre" 2>nul
    if %errorlevel% equ 0 (
        echo [DEBUG] Successfully removed existing portable-jre directory
    ) else (
        echo [ERROR] Failed to remove existing portable-jre directory
        echo [ERROR] Error code: %errorlevel%
        pause
        exit /b 1
    )
    echo.
)

echo [DEBUG] Creating portable-jre directory structure...
mkdir "portable-jre" 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Failed to create portable-jre directory
    echo [ERROR] Error code: %errorlevel%
    pause
    exit /b 1
)

mkdir "portable-jre\bin" 2>nul
mkdir "portable-jre\lib" 2>nul
mkdir "portable-jre\conf" 2>nul
mkdir "portable-jre\legal" 2>nul

echo [DEBUG] Directory structure created successfully
echo.

REM Check if we have a system Java installation
echo [DEBUG] Checking for system Java installation...
java -version 2>&1
if %errorlevel% equ 0 (
    echo [DEBUG] System Java found and working
    set "JAVA_AVAILABLE=true"
) else (
    echo [WARNING] System Java not found or not working
    echo [WARNING] Error code: %errorlevel%
    set "JAVA_AVAILABLE=false"
)
echo.

REM Try to copy from system Java installation
if "%JAVA_AVAILABLE%"=="true" (
    echo [DEBUG] Attempting to copy from system Java installation...
    
    REM Find Java home
    for /f "tokens=*" %%i in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr "java.home"') do (
        set "JAVA_HOME_LINE=%%i"
    )
    
    echo [DEBUG] Java home line: %JAVA_HOME_LINE%
    
    REM Extract Java home path
    for /f "tokens=2 delims==" %%i in ("%JAVA_HOME_LINE%") do (
        set "JAVA_HOME=%%i"
    )
    
    REM Remove quotes and trim
    set "JAVA_HOME=%JAVA_HOME:"=%"
    set "JAVA_HOME=%JAVA_HOME: =%"
    
    echo [DEBUG] Extracted JAVA_HOME: %JAVA_HOME%
    
    if exist "%JAVA_HOME%" (
        echo [DEBUG] JAVA_HOME directory exists, attempting to copy files...
        
        REM Copy essential files
        echo [DEBUG] Copying bin directory...
        xcopy "%JAVA_HOME%\bin\*" "portable-jre\bin\" /E /Y /Q 2>nul
        if %errorlevel% equ 0 (
            echo [DEBUG] Successfully copied bin directory
        ) else (
            echo [ERROR] Failed to copy bin directory
            echo [ERROR] Error code: %errorlevel%
        )
        
        echo [DEBUG] Copying lib directory...
        xcopy "%JAVA_HOME%\lib\*" "portable-jre\lib\" /E /Y /Q 2>nul
        if %errorlevel% equ 0 (
            echo [DEBUG] Successfully copied lib directory
        ) else (
            echo [ERROR] Failed to copy lib directory
            echo [ERROR] Error code: %errorlevel%
        )
        
        echo [DEBUG] Copying conf directory...
        xcopy "%JAVA_HOME%\conf\*" "portable-jre\conf\" /E /Y /Q 2>nul
        if %errorlevel% equ 0 (
            echo [DEBUG] Successfully copied conf directory
        ) else (
            echo [ERROR] Failed to copy conf directory
            echo [ERROR] Error code: %errorlevel%
        )
        
        echo [DEBUG] Copying legal directory...
        xcopy "%JAVA_HOME%\legal\*" "portable-jre\legal\" /E /Y /Q 2>nul
        if %errorlevel% equ 0 (
            echo [DEBUG] Successfully copied legal directory
        ) else (
            echo [ERROR] Failed to copy legal directory
            echo [ERROR] Error code: %errorlevel%
        )
        
        echo [DEBUG] Copying release file...
        copy "%JAVA_HOME%\release" "portable-jre\" 2>nul
        if %errorlevel% equ 0 (
            echo [DEBUG] Successfully copied release file
        ) else (
            echo [ERROR] Failed to copy release file
            echo [ERROR] Error code: %errorlevel%
        )
        
    ) else (
        echo [ERROR] JAVA_HOME directory does not exist: %JAVA_HOME%
    )
) else (
    echo [WARNING] Cannot copy from system Java - not available
)

echo.

REM Check if we have the files we need
echo [DEBUG] Verifying portable JRE installation...
if exist "portable-jre\bin\java.exe" (
    echo [DEBUG] Found java.exe
) else (
    echo [ERROR] java.exe not found in portable-jre\bin\
    echo [ERROR] Portable JRE installation is incomplete!
)

if exist "portable-jre\bin\javaw.exe" (
    echo [DEBUG] Found javaw.exe
) else (
    echo [WARNING] javaw.exe not found in portable-jre\bin\
)

if exist "portable-jre\lib\rt.jar" (
    echo [DEBUG] Found rt.jar
) else (
    echo [WARNING] rt.jar not found in portable-jre\lib\
)

echo.

REM Test the portable JRE
echo [DEBUG] Testing portable JRE...
if exist "portable-jre\bin\java.exe" (
    echo [DEBUG] Running: portable-jre\bin\java.exe -version
    "portable-jre\bin\java.exe" -version 2>&1
    if %errorlevel% equ 0 (
        echo [DEBUG] Portable JRE test successful
        set "PORTABLE_JRE_WORKING=true"
    ) else (
        echo [ERROR] Portable JRE test failed
        echo [ERROR] Error code: %errorlevel%
        set "PORTABLE_JRE_WORKING=false"
    )
) else (
    echo [ERROR] Cannot test portable JRE - java.exe not found
    set "PORTABLE_JRE_WORKING=false"
)

echo.

:test_compilation
REM Test compilation with portable JRE
if "%PORTABLE_JRE_WORKING%"=="true" (
    echo [DEBUG] Testing compilation with portable JRE...
    echo [DEBUG] Running: portable-jre\bin\javac.exe -cp . AdvancedDesktopPet.java
    
    "portable-jre\bin\javac.exe" -cp . AdvancedDesktopPet.java 2>&1
    if %errorlevel% equ 0 (
        echo [DEBUG] Compilation successful with portable JRE
        set "COMPILATION_SUCCESS=true"
    ) else (
        echo [ERROR] Compilation failed with portable JRE
        echo [ERROR] Error code: %errorlevel%
        set "COMPILATION_SUCCESS=false"
    )
) else (
    echo [WARNING] Cannot test compilation - portable JRE not working
    set "COMPILATION_SUCCESS=false"
)

echo.

REM Test execution with portable JRE
if "%PORTABLE_JRE_WORKING%"=="true" (
    echo [DEBUG] Testing execution with portable JRE...
    echo [DEBUG] Running: portable-jre\bin\java.exe -cp . AdvancedDesktopPet
    
    timeout /t 3 /nobreak >nul
    "portable-jre\bin\java.exe" -cp . AdvancedDesktopPet 2>&1 &
    if %errorlevel% equ 0 (
        echo [DEBUG] Execution test successful
        set "EXECUTION_SUCCESS=true"
    ) else (
        echo [ERROR] Execution test failed
        echo [ERROR] Error code: %errorlevel%
        set "EXECUTION_SUCCESS=false"
    )
    
    REM Wait a moment then kill the process
    timeout /t 2 /nobreak >nul
    taskkill /f /im java.exe 2>nul
) else (
    echo [WARNING] Cannot test execution - portable JRE not working
    set "EXECUTION_SUCCESS=false"
)

echo.

REM Create debug report
echo [DEBUG] Creating debug report...
echo ======================================== > portable_jre_debug_report.txt
echo PORTABLE JRE DEBUG REPORT >> portable_jre_debug_report.txt
echo ======================================== >> portable_jre_debug_report.txt
echo Generated: %date% %time% >> portable_jre_debug_report.txt
echo. >> portable_jre_debug_report.txt

echo SYSTEM INFORMATION: >> portable_jre_debug_report.txt
echo - OS: %OS% >> portable_jre_debug_report.txt
echo - Architecture: %PROCESSOR_ARCHITECTURE% >> portable_jre_debug_report.txt
echo - Current Directory: %CD% >> portable_jre_debug_report.txt
echo. >> portable_jre_debug_report.txt

echo JAVA STATUS: >> portable_jre_debug_report.txt
echo - System Java Available: %JAVA_AVAILABLE% >> portable_jre_debug_report.txt
echo - Portable JRE Working: %PORTABLE_JRE_WORKING% >> portable_jre_debug_report.txt
echo - Compilation Success: %COMPILATION_SUCCESS% >> portable_jre_debug_report.txt
echo - Execution Success: %EXECUTION_SUCCESS% >> portable_jre_debug_report.txt
echo. >> portable_jre_debug_report.txt

echo PORTABLE JRE CONTENTS: >> portable_jre_debug_report.txt
if exist "portable-jre" (
    dir /s /b "portable-jre" >> portable_jre_debug_report.txt
) else (
    echo portable-jre directory not found >> portable_jre_debug_report.txt
)

echo [DEBUG] Debug report saved to: portable_jre_debug_report.txt

echo.

REM Final status
echo ========================================
echo FINAL STATUS
echo ========================================
echo System Java Available: %JAVA_AVAILABLE%
echo Portable JRE Working: %PORTABLE_JRE_WORKING%
echo Compilation Success: %COMPILATION_SUCCESS%
echo Execution Success: %EXECUTION_SUCCESS%
echo.

if "%PORTABLE_JRE_WORKING%"=="true" (
    echo [SUCCESS] Portable JRE created and tested successfully!
    echo [INFO] You can now use the portable JRE for your application.
) else (
    echo [ERROR] Portable JRE creation failed!
    echo [ERROR] Check the debug report for details: portable_jre_debug_report.txt
    echo [ERROR] You may need to install Java manually or fix the installation.
)

echo.
echo [DEBUG] Script completed at: %date% %time%
echo [DEBUG] Press any key to exit...
pause >nul 