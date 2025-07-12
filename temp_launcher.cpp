#include <windows.h>
#include <string>
#include <iostream>

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    // Get the directory where the EXE is located
    char exePath[MAX_PATH];
    GetModuleFileNameA(NULL, exePath, MAX_PATH);
ECHO is off.
    // Extract directory path
    std::string dir = exePath;
    size_t pos = dir.find_last_of("\\\/");
    if (pos != std::string::npos) {
        dir = dir.substr(0, pos);
    }
ECHO is off.
    // Build command to run Java
    std::string javaCmd = "\"" + dir + "\\jre\\bin\\javaw.exe\" -jar \"" + dir + "\\lib\\AdvancedDesktopPet.jar\"";
ECHO is off.
    // Execute the command
    STARTUPINFOA si = {0};
    PROCESS_INFORMATION pi = {0};
    si.cb = sizeof(si);
    si.dwFlags = STARTF_USESHOWWINDOW;
    si.wShowWindow = SW_HIDE; // Hide console window
ECHO is off.
    if (CreateProcessA(NULL, const_cast<char*>(javaCmd.c_str()), NULL, NULL, FALSE, 0, NULL, dir.c_str(), &si, &pi)) {
        CloseHandle(pi.hProcess);
        CloseHandle(pi.hThread);
        return 0;
    } else {
        MessageBoxA(NULL, "Failed to start Desktop Pet. Please check if Java is installed.", "Error", MB_OK | MB_ICONERROR);
        return 1;
    }
}
