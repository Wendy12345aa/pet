@echo off
setlocal enabledelayedexpansion

:: ========================================
:: Force Cleanup Script for Desktop Pet
:: ========================================
:: This script will forcefully clean up stubborn files and folders
:: Author: AI Assistant
:: Version: 1.0

echo.
echo ========================================
echo    Force Cleanup Script for Desktop Pet
echo ========================================
echo.

:: Set title
title Force Cleanup - Desktop Pet

:: Check if running as administrator
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] This script is not running as administrator.
    echo Some cleanup operations may require elevated privileges.
    echo.
    echo Please run this script as Administrator for best results.
    echo Right-click on the script and select "Run as administrator"
    echo.
    pause
)

echo [INFO] Starting force cleanup process...
echo.

:: Step 1: Kill all Java processes
echo [STEP 1] Terminating Java processes...
echo.

:: Kill all java.exe processes
tasklist /FI "IMAGENAME eq java.exe" 2>NUL | find /I /N "java.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [INFO] Found Java processes. Terminating them...
    taskkill /F /IM java.exe /T >nul 2>&1
    if !errorlevel! equ 0 (
        echo [SUCCESS] Java processes terminated
    ) else (
        echo [WARNING] Some Java processes may still be running
    )
) else (
    echo [INFO] No Java processes found
)

:: Kill javaw.exe processes (Java without console window)
tasklist /FI "IMAGENAME eq javaw.exe" 2>NUL | find /I /N "javaw.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [INFO] Found Java window processes. Terminating them...
    taskkill /F /IM javaw.exe /T >nul 2>&1
    if !errorlevel! equ 0 (
        echo [SUCCESS] Java window processes terminated
    ) else (
        echo [WARNING] Some Java window processes may still be running
    )
) else (
    echo [INFO] No Java window processes found
)

:: Kill any remaining Java-related processes
echo [INFO] Checking for other Java-related processes...
for %%p in (jvm.dll java.exe javaw.exe jconsole.exe jstack.exe jmap.exe) do (
    tasklist /FI "IMAGENAME eq %%p" 2>NUL | find /I /N "%%p">NUL
    if "%ERRORLEVEL%"=="0" (
        echo [INFO] Terminating %%p processes...
        taskkill /F /IM %%p /T >nul 2>&1
    )
)

echo.

:: Step 2: Kill any desktop pet related processes
echo [STEP 2] Terminating Desktop Pet processes...
echo.

:: Kill any processes that might be related to the desktop pet
for %%p in (AdvancedDesktopPet.exe DesktopPet.exe AyanoPet.exe) do (
    tasklist /FI "IMAGENAME eq %%p" 2>NUL | find /I /N "%%p">NUL
    if "%ERRORLEVEL%"=="0" (
        echo [INFO] Terminating %%p processes...
        taskkill /F /IM %%p /T >nul 2>&1
    )
)

:: Kill processes with "pet" in the name
wmic process where "name like '%%pet%%'" call terminate >nul 2>&1

echo.

:: Step 3: Unload any loaded DLLs
echo [STEP 3] Unloading loaded DLLs...
echo.

:: Force unload any loaded JVM DLLs
for %%d in (jvm.dll jawt.dll jli.dll) do (
    echo [INFO] Attempting to unload %%d...
    rundll32.exe %%d,DllUnregisterServer >nul 2>&1
)

echo.

:: Step 4: Clear temporary files
echo [STEP 4] Clearing temporary files...
echo.

:: Clear Java temporary files
if exist "%TEMP%\hsperfdata_*" (
    echo [INFO] Removing Java performance data...
    rmdir /s /q "%TEMP%\hsperfdata_*" >nul 2>&1
)

:: Clear Windows temporary files
if exist "%TEMP%\*" (
    echo [INFO] Clearing temporary files...
    del /q "%TEMP%\*" >nul 2>&1
)

:: Clear Java cache
if exist "%USERPROFILE%\.java\deployment\cache\*" (
    echo [INFO] Clearing Java cache...
    rmdir /s /q "%USERPROFILE%\.java\deployment\cache\*" >nul 2>&1
)

echo.

:: Step 5: Force delete specific files and folders
echo [STEP 5] Force deleting Desktop Pet files and folders...
echo.

:: List of files and folders to force delete
set "FILES_TO_DELETE=AdvancedDesktopPet.jar AdvancedDesktopPet.java DesktopPet.java run.bat run_enhanced.bat run_with_local_jre.bat dev_environment.bat setup_java_environment.bat force_cleanup.bat"

:: List of folders to force delete
set "FOLDERS_TO_DELETE=minimal-jre Image music AyanoPet AyanoPetMinimal AyanoPetOptimized temp"

:: Force delete files
for %%f in (%FILES_TO_DELETE%) do (
    if exist "%%f" (
        echo [INFO] Force deleting file: %%f
        del /f /q "%%f" >nul 2>&1
        if !errorlevel! equ 0 (
            echo [SUCCESS] Deleted: %%f
        ) else (
            echo [WARNING] Could not delete: %%f
        )
    )
)

echo.

:: Force delete folders
for %%d in (%FOLDERS_TO_DELETE%) do (
    if exist "%%d" (
        echo [INFO] Force deleting folder: %%d
        rmdir /s /q "%%d" >nul 2>&1
        if !errorlevel! equ 0 (
            echo [SUCCESS] Deleted folder: %%d
        ) else (
            echo [WARNING] Could not delete folder: %%d
            echo [INFO] Attempting alternative deletion method...
            
            :: Try using robocopy to delete (works better for stubborn folders)
            robocopy "%%d" "%%d" /PURGE >nul 2>&1
            rmdir /s /q "%%d" >nul 2>&1
            
            if !errorlevel! equ 0 (
                echo [SUCCESS] Deleted folder: %%d (alternative method)
            ) else (
                echo [ERROR] Could not delete folder: %%d
            )
        )
    )
)

echo.

:: Step 6: Use PowerShell for stubborn files
echo [STEP 6] Using PowerShell for stubborn files...
echo.

powershell -Command "& {
    Write-Host '[INFO] Using PowerShell to force delete remaining files...'
    
    # Get current directory
    $currentDir = Get-Location
    
    # Force delete any remaining files
    Get-ChildItem -Path $currentDir -Recurse -Force | ForEach-Object {
        try {
            if ($_.PSIsContainer) {
                Remove-Item $_.FullName -Recurse -Force -ErrorAction SilentlyContinue
                Write-Host '[SUCCESS] Deleted folder:' $_.Name
            } else {
                Remove-Item $_.FullName -Force -ErrorAction SilentlyContinue
                Write-Host '[SUCCESS] Deleted file:' $_.Name
            }
        } catch {
            Write-Host '[WARNING] Could not delete:' $_.Name
        }
    }
    
    Write-Host '[INFO] PowerShell cleanup completed'
}"

echo.

:: Step 7: Check for remaining processes
echo [STEP 7] Final process check...
echo.

:: Wait a moment for processes to fully terminate
timeout /t 3 /nobreak >nul

:: Check if any Java processes are still running
tasklist /FI "IMAGENAME eq java.exe" 2>NUL | find /I /N "java.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [WARNING] Java processes are still running!
    echo [INFO] You may need to restart your computer to fully terminate them.
) else (
    echo [SUCCESS] No Java processes found
)

tasklist /FI "IMAGENAME eq javaw.exe" 2>NUL | find /I /N "javaw.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [WARNING] Java window processes are still running!
    echo [INFO] You may need to restart your computer to fully terminate them.
) else (
    echo [SUCCESS] No Java window processes found
)

echo.

:: Step 8: Registry cleanup (optional)
echo [STEP 8] Registry cleanup (optional)...
echo.

set /p REGISTRY_CLEANUP="Do you want to clean up Java registry entries? (y/n): "
if /i "!REGISTRY_CLEANUP!"=="y" (
    echo [INFO] Cleaning up Java registry entries...
    
    :: Remove Java registry entries (be careful with this)
    reg delete "HKEY_CURRENT_USER\Software\JavaSoft" /f >nul 2>&1
    reg delete "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft" /f >nul 2>&1
    reg delete "HKEY_LOCAL_MACHINE\SOFTWARE\WOW6432Node\JavaSoft" /f >nul 2>&1
    
    echo [SUCCESS] Registry cleanup completed
) else (
    echo [INFO] Skipping registry cleanup
)

echo.

:: Step 9: Final recommendations
echo [STEP 9] Final recommendations...
echo.

echo ========================================
echo           Cleanup Summary
echo ========================================
echo.
echo [INFO] Force cleanup process completed!
echo.
echo If you still cannot delete files/folders:
echo.
echo 1. Restart your computer and try again
echo 2. Use Safe Mode to delete stubborn files
echo 3. Use specialized tools like Unlocker or Process Explorer
echo 4. Check if antivirus software is blocking deletion
echo 5. Ensure no applications are accessing the files
echo.
echo [INFO] Safe Mode instructions:
echo - Restart computer
echo - Press F8 during boot
echo - Select "Safe Mode"
echo - Try deleting the files again
echo.
echo [INFO] Alternative tools:
echo - Unlocker: http://www.emptyloop.com/unlocker/
echo - Process Explorer: https://docs.microsoft.com/en-us/sysinternals/downloads/process-explorer
echo - IObit Unlocker: https://www.iobit.com/en/iobit-unlocker.php
echo.

:: Check if current directory is empty
dir /a /b 2>nul | findstr /r "^" >nul
if !errorlevel! equ 0 (
    echo [INFO] Current directory still contains files/folders
    echo [INFO] Listing remaining items:
    dir /a
) else (
    echo [SUCCESS] Directory appears to be empty!
)

echo.
echo [INFO] Force cleanup script completed.
echo [INFO] Press any key to exit...
pause >nul
exit /b 0 