@echo off
echo Creating desktop shortcut...

set "DESKTOP=%USERPROFILE%\Desktop"
set "SHORTCUT=%DESKTOP%\Desktop Pet.bat"
set "TARGET=%~dp0DesktopPet-Portable-EXE\DesktopPet.bat"

echo @echo off > "%SHORTCUT%"
echo cd /d "%~dp0DesktopPet-Portable-EXE" >> "%SHORTCUT%"
echo call "%TARGET%" >> "%SHORTCUT%"

echo Desktop shortcut created: %SHORTCUT%
pause
