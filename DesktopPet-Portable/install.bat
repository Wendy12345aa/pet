@echo off
echo Installing Desktop Pet...

:: Create program files directory
set "INSTALL_DIR=%PROGRAMFILES%\DesktopPet"
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"

:: Copy files
xcopy /E /I /Y "%~dp0*" "%INSTALL_DIR%\"

:: Create start menu shortcut
set "START_MENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs\DesktopPet"
if not exist "%START_MENU%" mkdir "%START_MENU%"

powershell -Command "& {$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut^('%START_MENU%\Desktop Pet.lnk'^); $Shortcut.TargetPath = '%INSTALL_DIR%\DesktopPet.exe'; $Shortcut.WorkingDirectory = '%INSTALL_DIR%'; $Shortcut.IconLocation = '%INSTALL_DIR%\chibi01.ico'; $Shortcut.Save^(^)}"

:: Create desktop shortcut
set "DESKTOP=%USERPROFILE%\Desktop"
powershell -Command "& {$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut^('%DESKTOP%\Desktop Pet.lnk'^); $Shortcut.TargetPath = '%INSTALL_DIR%\DesktopPet.exe'; $Shortcut.WorkingDirectory = '%INSTALL_DIR%'; $Shortcut.IconLocation = '%INSTALL_DIR%\chibi01.ico'; $Shortcut.Save^(^)}"

echo [SUCCESS] Desktop Pet installed successfully
echo You can now run it from the Start Menu or Desktop shortcut.
pause
