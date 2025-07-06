@echo off
echo ========================================
echo Desktop Pet - Portable Executable
echo ========================================
echo.
echo This version includes a minimal Java runtime.
echo.
echo Starting Desktop Pet...

set "JAVA_HOME=%~dp0jre"
set "PATH=%JAVA_HOME%\bin;%PATH%"

"%JAVA_HOME%\bin\java.exe" -jar "%~dp0lib\AdvancedDesktopPet.jar"

echo Desktop Pet has been closed.
pause
