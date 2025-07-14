@echo off
echo Desktop Pet EXE Creator
echo =======================
echo.

cd ..\..

if not exist "portable-jre" (
    echo ERROR: portable-jre not found
    pause
    exit /b 1
)

if not exist "target\AdvancedDesktopPet.jar" (
    echo ERROR: JAR file not found
    pause
    exit /b 1
)

echo Prerequisites OK
pause

echo Trying PowerShell EXE creation...
powershell -Command "Add-Type -TypeDefinition 'using System;using System.Diagnostics;using System.IO;using System.Windows.Forms;class Program{static void Main(){try{string exeDir=Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().Location);string javaExe=Path.Combine(exeDir,\"jre\",\"bin\",\"javaw.exe\");string jarFile=Path.Combine(exeDir,\"lib\",\"AdvancedDesktopPet.jar\");if(File.Exists(javaExe)&&File.Exists(jarFile)){ProcessStartInfo psi=new ProcessStartInfo();psi.FileName=javaExe;psi.Arguments=\"-jar \\\"\"+jarFile+\"\\\"\";psi.WorkingDirectory=exeDir;psi.WindowStyle=ProcessWindowStyle.Hidden;psi.CreateNoWindow=true;Process.Start(psi);}else{MessageBox.Show(\"Desktop Pet files not found.\",\"Error\");}}catch(Exception ex){MessageBox.Show(\"Error: \"+ex.Message,\"Error\");}}}' -ReferencedAssemblies System.Windows.Forms,System.Drawing -OutputAssembly 'DesktopPet.exe' -OutputType WindowsApplication"

if exist "DesktopPet.exe" (
    echo SUCCESS: EXE created!
    
    echo Creating package...
    if exist "DesktopPet-EXE" rmdir /s /q "DesktopPet-EXE"
    mkdir "DesktopPet-EXE"
    
    xcopy "portable-jre" "DesktopPet-EXE\jre\" /E /I /Q
    mkdir "DesktopPet-EXE\lib"
    copy "target\AdvancedDesktopPet.jar" "DesktopPet-EXE\lib\"
    xcopy "Image" "DesktopPet-EXE\Image\" /E /I /Q
    xcopy "music" "DesktopPet-EXE\music\" /E /I /Q
    xcopy "resources" "DesktopPet-EXE\resources\" /E /I /Q
    copy "character_defaults.properties" "DesktopPet-EXE\"
    copy "chibi01.ico" "DesktopPet-EXE\"
    copy "DesktopPet.exe" "DesktopPet-EXE\"
    
    echo.
    echo SUCCESS: EXE package created!
    echo Location: DesktopPet-EXE\
    echo Run: DesktopPet-EXE\DesktopPet.exe
    
) else (
    echo FAILED: Could not create EXE
)

echo.
pause 