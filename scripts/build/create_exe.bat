@echo off
echo Desktop Pet EXE Creator (with Icon)
echo ===================================
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

if not exist "chibi01.ico" (
    echo ERROR: chibi01.ico not found
    pause
    exit /b 1
)

echo Prerequisites OK
pause

echo Creating EXE with chibi01.ico icon...

REM Create a temporary C++ launcher with icon resource
echo Creating launcher source...
(
echo #include ^<windows.h^>
echo #include ^<string^>
echo #include ^<iostream^>
echo.
echo int WINAPI WinMain^(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow^) {
echo     try {
echo         char exePath[MAX_PATH];
echo         GetModuleFileNameA^(NULL, exePath, MAX_PATH^);
echo         std::string exeDir = std::string^(exePath^);
echo         exeDir = exeDir.substr^(0, exeDir.find_last_of^("\\"^)^);
echo.
echo         std::string javaExe = exeDir + "\\jre\\bin\\javaw.exe";
echo         std::string jarFile = exeDir + "\\lib\\AdvancedDesktopPet.jar";
echo.
echo         if ^(GetFileAttributesA^(javaExe.c_str^(^) != INVALID_FILE_ATTRIBUTES ^&^& GetFileAttributesA^(jarFile.c_str^(^) != INVALID_FILE_ATTRIBUTES^) {
echo             STARTUPINFOA si = {0};
echo             PROCESS_INFORMATION pi = {0};
echo             si.cb = sizeof^(si^);
echo             si.dwFlags = STARTF_USESHOWWINDOW;
echo             si.wShowWindow = SW_HIDE;
echo.
echo             std::string cmdLine = "javaw.exe -jar \"" + jarFile + "\"";
echo.
echo             if ^(CreateProcessA^(javaExe.c_str^(^), ^(LPSTR^)cmdLine.c_str^(^), NULL, NULL, FALSE, CREATE_NO_WINDOW, NULL, exeDir.c_str^(^), ^&si, ^&pi^)^) {
echo                 CloseHandle^(pi.hProcess^);
echo                 CloseHandle^(pi.hThread^);
echo                 return 0;
echo             }
echo         }
echo.
echo         MessageBoxA^(NULL, "Desktop Pet files not found.", "Error", MB_OK ^| MB_ICONERROR^);
echo         return 1;
echo     } catch ^(...^) {
echo         MessageBoxA^(NULL, "Unexpected error occurred.", "Error", MB_OK ^| MB_ICONERROR^);
echo         return 1;
echo     }
echo }
) > temp_launcher.cpp

REM Create resource file for the icon
echo Creating resource file...
(
echo #include ^<windows.h^>
echo.
echo IDI_ICON1 ICON "chibi01.ico"
) > temp_launcher.rc

echo Compiling with icon...
REM Try to compile with Visual Studio compiler if available
where cl >nul 2>&1
if %errorlevel% equ 0 (
    echo Using Visual Studio compiler...
    cl /O2 /Fe:DesktopPet.exe temp_launcher.cpp temp_launcher.rc /link /SUBSYSTEM:WINDOWS
) else (
    echo Visual Studio compiler not found, trying MinGW...
    where g++ >nul 2>&1
    if %errorlevel% equ 0 (
        echo Using MinGW compiler...
        windres temp_launcher.rc temp_launcher.o
        g++ -O2 -o DesktopPet.exe temp_launcher.cpp temp_launcher.o -mwindows
    ) else (
        echo No C++ compiler found. Creating basic EXE without icon...
        powershell -Command "Add-Type -TypeDefinition 'using System;using System.Diagnostics;using System.IO;using System.Windows.Forms;class Program{static void Main(){try{string exeDir=Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().Location);string javaExe=Path.Combine(exeDir,\"jre\",\"bin\",\"javaw.exe\");string jarFile=Path.Combine(exeDir,\"lib\",\"AdvancedDesktopPet.jar\");if(File.Exists(javaExe)&&File.Exists(jarFile)){ProcessStartInfo psi=new ProcessStartInfo();psi.FileName=javaExe;psi.Arguments=\"-jar \\\"\"+jarFile+\"\\\"\";psi.WorkingDirectory=exeDir;psi.WindowStyle=ProcessWindowStyle.Hidden;psi.CreateNoWindow=true;Process.Start(psi);}else{MessageBox.Show(\"Desktop Pet files not found.\",\"Error\");}}catch(Exception ex){MessageBox.Show(\"Error: \"+ex.Message,\"Error\");}}}' -ReferencedAssemblies System.Windows.Forms,System.Drawing -OutputAssembly 'DesktopPet.exe' -OutputType WindowsApplication"
    )
)

REM Clean up temporary files
del temp_launcher.cpp 2>nul
del temp_launcher.rc 2>nul
del temp_launcher.o 2>nul
del temp_launcher.obj 2>nul

if exist "DesktopPet.exe" (
    echo SUCCESS: EXE created with chibi01.ico icon!
    
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
    echo SUCCESS: EXE package created with chibi01.ico icon!
    echo Location: DesktopPet-EXE\
    echo Run: DesktopPet-EXE\DesktopPet.exe
    
) else (
    echo FAILED: Could not create EXE
)

echo.
pause 