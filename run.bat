@echo off
echo Compiling Advanced Desktop Pet...
javac AdvancedDesktopPet.java MusicManager.java

if %errorlevel% equ 0 (
    echo Compilation successful!
    echo Starting Advanced Desktop Pet...
    java AdvancedDesktopPet
) else (
    echo Compilation failed!
    pause
) 