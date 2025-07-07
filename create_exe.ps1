# Desktop Pet EXE Creator
# This script creates a true EXE file that launches the Java application

Write-Host "Desktop Pet EXE Creator" -ForegroundColor Green
Write-Host "=========================" -ForegroundColor Green
Write-Host ""

# Check if we're in the right directory
if (-not (Test-Path "AdvancedDesktopPet.jar")) {
    Write-Host "[ERROR] AdvancedDesktopPet.jar not found in current directory" -ForegroundColor Red
    Write-Host "[INFO] Please run this script from the project root directory" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# Create the C# launcher code
$csharpCode = @"
using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Windows.Forms;

public class DesktopPetLauncher
{
    [STAThread]
    public static void Main()
    {
        try
        {
            string appDir = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
            string jarFile = Path.Combine(appDir, "lib", "AdvancedDesktopPet.jar");
            string jreDir = Path.Combine(appDir, "jre");
            
            // Check if JAR file exists
            if (!File.Exists(jarFile))
            {
                MessageBox.Show("AdvancedDesktopPet.jar not found!", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }
            
            // Try embedded JRE first
            string javaExe = Path.Combine(jreDir, "bin", "java.exe");
            if (File.Exists(javaExe))
            {
                // Use embedded JRE
                ProcessStartInfo psi = new ProcessStartInfo();
                psi.FileName = javaExe;
                psi.Arguments = $"-jar \"{jarFile}\"";
                psi.WorkingDirectory = appDir;
                psi.UseShellExecute = false;
                psi.CreateNoWindow = true;
                
                Process.Start(psi);
            }
            else
            {
                // Try system Java
                try
                {
                    ProcessStartInfo psi = new ProcessStartInfo();
                    psi.FileName = "java";
                    psi.Arguments = $"-jar \"{jarFile}\"";
                    psi.WorkingDirectory = appDir;
                    psi.UseShellExecute = false;
                    psi.CreateNoWindow = true;
                    
                    Process.Start(psi);
                }
                catch (Exception)
                {
                    MessageBox.Show(
                        "No Java Runtime Environment found!\n\n" +
                        "This portable version should include an embedded JRE.\n" +
                        "If you're seeing this error, the JRE files may be missing.\n\n" +
                        "Solutions:\n" +
                        "1. Download Java from: https://adoptium.net/\n" +
                        "2. Re-run the portable creator to include the JRE\n" +
                        "3. Check if antivirus removed the JRE files",
                        "Java Not Found",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning
                    );
                }
            }
        }
        catch (Exception ex)
        {
            MessageBox.Show($"Error: {ex.Message}", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
    }
}
"@

Write-Host "[INFO] Creating EXE launcher..." -ForegroundColor Cyan

try {
    # Compile the C# code to an EXE
    Add-Type -TypeDefinition $csharpCode -OutputAssembly "DesktopPet.exe" -Reference "System.Windows.Forms.dll"
    
    if (Test-Path "DesktopPet.exe") {
        Write-Host "[SUCCESS] EXE created successfully: DesktopPet.exe" -ForegroundColor Green
        
        # Create a simple batch file as backup
        $batchContent = @"
@echo off
setlocal enabledelayedexpansion

set "APP_DIR=%~dp0"
set "JAR_FILE=%APP_DIR%lib\AdvancedDesktopPet.jar"
set "JRE_DIR=%APP_DIR%jre"

if exist "%JRE_DIR%\bin\java.exe" (
    echo [INFO] Using embedded JRE - no Java installation required!
    set "JAVA_HOME=%JRE_DIR%"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    "%JAVA_HOME%\bin\java" -jar "%JAR_FILE%"
) else (
    echo [INFO] Checking for system Java...
    java -version >nul 2>&1
    if %errorlevel% equ 0 (
        echo [INFO] Using system Java...
        java -jar "%JAR_FILE%"
    ) else (
        echo [ERROR] No Java found. Please install Java or use the embedded version.
        pause
    )
)
"@
        
        $batchContent | Out-File -FilePath "DesktopPet.bat" -Encoding ASCII
        
        Write-Host "[INFO] Backup batch file created: DesktopPet.bat" -ForegroundColor Cyan
        
        # Create desktop shortcut
        Write-Host "[INFO] Creating desktop shortcut..." -ForegroundColor Cyan
        $desktop = [Environment]::GetFolderPath("Desktop")
        $shortcut = Join-Path $desktop "Desktop Pet.lnk"
        $exePath = Join-Path $PSScriptRoot "DesktopPet.exe"
        
        $WshShell = New-Object -comObject WScript.Shell
        $Shortcut = $WshShell.CreateShortcut($shortcut)
        $Shortcut.TargetPath = $exePath
        $Shortcut.WorkingDirectory = $PSScriptRoot
        $Shortcut.IconLocation = Join-Path $PSScriptRoot "chibi01.ico"
        $Shortcut.Save()
        
        Write-Host "[SUCCESS] Desktop shortcut created!" -ForegroundColor Green
        
    } else {
        Write-Host "[ERROR] Failed to create EXE file" -ForegroundColor Red
    }
}
catch {
    Write-Host "[ERROR] Failed to compile EXE: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "[INFO] You can still use the batch file: DesktopPet.bat" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Deployment Summary:" -ForegroundColor Green
Write-Host "==================" -ForegroundColor Green
Write-Host "✓ DesktopPet.exe - True executable (if created successfully)" -ForegroundColor White
Write-Host "✓ DesktopPet.bat - Batch file launcher (always created)" -ForegroundColor White
Write-Host "✓ Desktop shortcut - Created on desktop" -ForegroundColor White
Write-Host ""
Write-Host "Usage:" -ForegroundColor Cyan
Write-Host "- Double-click DesktopPet.exe to run" -ForegroundColor White
Write-Host "- Or double-click DesktopPet.bat as alternative" -ForegroundColor White
Write-Host "- The application works without Java installation!" -ForegroundColor Green
Write-Host ""

Read-Host "Press Enter to exit"