Add-Type -TypeDefinition @"
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
            string batchFile = Path.Combine(appDir, "DesktopPet.exe.bat");
ECHO is off.
            if (File.Exists(batchFile))
            {
                ProcessStartInfo psi = new ProcessStartInfo();
                psi.FileName = "cmd.exe";
                psi.Arguments = "/c \"" + batchFile + "\"";
                psi.WorkingDirectory = appDir;
                psi.UseShellExecute = false;
                psi.CreateNoWindow = true;
ECHO is off.
                Process.Start(psi);
            }
            else
            {
                MessageBox.Show("Launcher file not found", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
        catch (Exception ex)
        {
            MessageBox.Show("Error: " + ex.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
    }
}
"@

$exePath = Join-Path $PSScriptRoot "DesktopPet.exe"
$dllPath = Join-Path $PSScriptRoot "System.Windows.Forms.dll"

# Compile the launcher
Add-Type -TypeDefinition $typeDefinition -OutputAssembly $exePath -Reference "System.Windows.Forms.dll"

if (Test-Path $exePath) {
    Write-Host "EXE created successfully: $exePath"
} else {
    Write-Host "Failed to create EXE"
}
