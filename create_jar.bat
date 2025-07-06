@echo off
echo Creating Desktop Pet JAR file...
echo =================================

echo.
echo 1. Cleaning old class files...
del *.class 2>nul

echo.
echo 2. Compiling Java source...
javac AdvancedDesktopPet.java
if errorlevel 1 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo.
echo 3. Checking generated class files...
dir *.class

echo.
echo 4. Creating JAR file with main class...
jar cfe AdvancedDesktopPet.jar AdvancedDesktopPet *.class
if errorlevel 1 (
    echo ERROR: JAR creation failed!
    pause
    exit /b 1
)

echo.
echo 5. JAR file created successfully!
echo    Size: 
dir AdvancedDesktopPet.jar

echo.
echo 6. Copying to portable folder...
copy AdvancedDesktopPet.jar DesktopPet-Portable\AdvancedDesktopPet.jar
if errorlevel 1 (
    echo WARNING: Could not copy to portable folder
) else (
    echo Successfully copied to DesktopPet-Portable folder
)

echo.
echo Done! The JAR file is ready.
pause 