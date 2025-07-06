@echo off
echo Creating Desktop Pet shortcut...

set "SCRIPT_DIR=%~dp0"
set "JAR_PATH=%SCRIPT_DIR%AdvancedDesktopPet.jar"

echo Creating shortcut on desktop...
powershell -Command "& {$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut^('%USERPROFILE%\Desktop\Desktop Pet.lnk'^); $Shortcut.TargetPath = 'java'; $Shortcut.Arguments = '-jar \"%JAR_PATH%\"'; $Shortcut.WorkingDirectory = '%SCRIPT_DIR%'; $Shortcut.IconLocation = '%SCRIPT_DIR%Image\chibi01.ico'; $Shortcut.Save^(^)}"

echo Creating shortcut in start menu...
powershell -Command "& {$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut^('%APPDATA%\Microsoft\Windows\Start Menu\Programs\Desktop Pet.lnk'^); $Shortcut.TargetPath = 'java'; $Shortcut.Arguments = '-jar \"%JAR_PATH%\"'; $Shortcut.WorkingDirectory = '%SCRIPT_DIR%'; $Shortcut.IconLocation = '%SCRIPT_DIR%Image\chibi01.ico'; $Shortcut.Save^(^)}"

echo ✓ Desktop Pet shortcuts created!
echo.
echo You can now:
echo - Run Desktop Pet from the desktop shortcut
echo - Find Desktop Pet in the Start Menu
echo - Or run run_desktop_pet.bat from this folder

Press any key to continue . . . 
