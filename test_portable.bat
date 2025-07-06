@echo off
echo Testing Portable Desktop Pet
echo =============================

echo.
echo Running from portable directory...
cd DesktopPet-Portable

echo.
echo Current directory: %CD%
echo.

echo Starting Desktop Pet...
java -jar AdvancedDesktopPet.jar

echo.
echo If the pet appeared, the portable version is working!
echo If you see error messages, please share them.
pause 