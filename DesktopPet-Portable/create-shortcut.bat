@echo off
echo Creating Desktop Pet shortcut...

set "DESKTOP=%USERPROFILE%\Desktop"
set "SHORTCUT=%DESKTOP%\Desktop Pet.lnk"
set "TARGET=%~dp0DesktopPet.exe"

if exist "%TARGET%" (
    echo Creating shortcut to Desktop Pet...
    powershell -Command "& {$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut^('%SHORTCUT%'^); $Shortcut.TargetPath = '%TARGET%'; $Shortcut.WorkingDirectory = '%~dp0'; $Shortcut.IconLocation = '%~dp0chibi01.ico'; $Shortcut.Save^(^)}"
    echo [SUCCESS] Desktop shortcut created
) else (
    echo [ERROR] DesktopPet.exe not found. Run create_exe.ps1 first.
)

pause
