@echo off
setlocal enabledelayedexpansion

REM Hide console window
if "%~1" neq "NOCONSOLE" (
    start /b "" "%~f0" NOCONSOLE
    exit /b
)

cd /d "%~dp0"

if exist "jre\bin\javaw.exe" (
    start /b "" "jre\bin\javaw.exe" -jar "lib\AdvancedDesktopPet.jar"
) else (
    start /b "" javaw -jar "lib\AdvancedDesktopPet.jar"
)

exit /b
