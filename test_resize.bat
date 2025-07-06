@echo off
echo Testing Desktop Pet Resize Functionality
echo ========================================

echo.
echo 1. Starting the desktop pet application...
java -cp . AdvancedDesktopPet

echo.
echo 2. Instructions for testing:
echo    - The pet should appear on your desktop
echo    - Right-click on the pet and select "Settings" (or middle-click)
echo    - In the settings window, try adjusting the size slider
echo    - The pet should resize properly without being cut off
echo    - If you see any console output about image loading, that's normal
echo.
echo 3. If the pet appears cut off:
echo    - Check the console output for any error messages
echo    - Make sure the Image folder contains chibi01.png, chibi02.png, chibi03.png
echo    - The application will create default animations if images are missing
echo.
pause 