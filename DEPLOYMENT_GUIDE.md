# Desktop Pet - EXE Deployment Guide

This guide explains how to deploy your Java desktop pet application as an executable (.exe) that doesn't require Java installation on the target machine.

## 🎯 Quick Start (Recommended)

### Method 1: Portable Package (Easiest)

1. **Run the portable creator:**
   ```batch
   create_portable_exe.bat
   ```

2. **Create the EXE:**
   - Go to the `DesktopPet-Portable/` folder
   - Right-click on `create_exe.ps1` and select "Run with PowerShell"
   - This creates `DesktopPet.exe`

3. **Distribute:**
   - Share the entire `DesktopPet-Portable/` folder
   - Users just double-click `DesktopPet.exe`

### Method 2: Direct EXE Creation

1. **Run the PowerShell script:**
   ```powershell
   .\create_exe.ps1
   ```

2. **Result:**
   - Creates `DesktopPet.exe` in the current directory
   - Creates `DesktopPet.bat` as backup
   - Creates desktop shortcut

## 📋 Deployment Methods

### 1. Portable Package (Recommended)

**Pros:**
- ✅ No Java installation required
- ✅ Includes embedded JRE
- ✅ Easy to distribute
- ✅ Works on any Windows machine

**Cons:**
- 📦 Larger file size (~50-100MB)
- 🔧 Requires PowerShell for EXE creation

**Files created:**
```
DesktopPet-Portable/
├── AdvancedDesktopPet.jar
├── DesktopPet.exe.bat
├── DesktopPet.exe (after running create_exe.ps1)
├── create_exe.ps1
├── create-shortcut.bat
├── install.bat
├── README.txt
├── minimal-jre/
├── Image/
└── music/
```

### 2. Launch4j Method

**Pros:**
- ✅ Creates true EXE
- ✅ Professional appearance
- ✅ Customizable

**Cons:**
- ❌ Requires Java on target machine
- 🔧 More complex setup

**Steps:**
1. Download Launch4j from https://launch4j.sourceforge.net/
2. Use the configuration in `create_exe_deployment.bat`
3. Run Launch4j with the generated config

### 3. JPackage Method (Advanced)

**Pros:**
- ✅ Creates native installer
- ✅ Includes custom JRE
- ✅ Professional distribution

**Cons:**
- ❌ Requires JDK 14+
- 🔧 Complex setup
- 📦 Large installer size

**Steps:**
1. Install JDK 14 or later
2. Run `create_exe_deployment.bat`
3. Look for `Desktop Pet-1.0.exe` in deployment folder

### 4. PowerShell EXE Creator

**Pros:**
- ✅ Simple to use
- ✅ No external tools needed
- ✅ Creates true EXE

**Cons:**
- 🔧 Requires PowerShell execution policy changes
- 📦 Still requires JAR + JRE files

**Steps:**
1. Run `create_exe.ps1`
2. Creates `DesktopPet.exe` and `DesktopPet.bat`

## 🛠️ Setup Instructions

### Prerequisites

1. **Java Development Kit (JDK):**
   ```batch
   java -version
   javac -version
   ```

2. **PowerShell Execution Policy:**
   ```powershell
   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
   ```

3. **Project Files:**
   - `AdvancedDesktopPet.java` (source)
   - `AdvancedDesktopPet.jar` (compiled)
   - `minimal-jre/` (embedded JRE)
   - `Image/` and `music/` (resources)

### Step-by-Step Deployment

#### Option A: Quick Deployment

1. **Compile and package:**
   ```batch
   javac AdvancedDesktopPet.java
   jar cf AdvancedDesktopPet.jar *.class
   ```

2. **Create portable version:**
   ```batch
   create_portable_exe.bat
   ```

3. **Create EXE:**
   ```powershell
   cd DesktopPet-Portable
   .\create_exe.ps1
   ```

#### Option B: Manual Deployment

1. **Create deployment folder:**
   ```batch
   mkdir DesktopPet-Portable
   ```

2. **Copy files:**
   ```batch
   copy AdvancedDesktopPet.jar DesktopPet-Portable\
   copy chibi01.ico DesktopPet-Portable\
   xcopy Image DesktopPet-Portable\Image\ /E /I /Y
   xcopy music DesktopPet-Portable\music\ /E /I /Y
   xcopy minimal-jre DesktopPet-Portable\minimal-jre\ /E /I /Y
   ```

3. **Create launcher:**
   ```batch
   # Copy DesktopPet.exe.bat content to DesktopPet-Portable\DesktopPet.exe.bat
   ```

4. **Create EXE:**
   ```powershell
   .\create_exe.ps1
   ```

## 📦 Distribution

### For End Users

**Portable Version (Recommended):**
1. Extract `DesktopPet-Portable.zip`
2. Double-click `DesktopPet.exe`
3. No installation required

**Installer Version:**
1. Run `Desktop Pet-1.0.exe`
2. Follow installation wizard
3. Launch from Start Menu

### File Structure for Distribution

```
DesktopPet-Portable/
├── DesktopPet.exe          # Main executable
├── DesktopPet.exe.bat      # Backup launcher
├── AdvancedDesktopPet.jar  # Java application
├── chibi01.ico            # Application icon
├── minimal-jre/           # Embedded Java runtime
│   ├── bin/
│   ├── lib/
│   └── conf/
├── Image/                 # Application images
│   ├── chibi01.png
│   ├── chibi02.png
│   └── chibi03.png
├── music/                 # Application music
│   ├── normal.wav
│   └── horror.wav
├── create-shortcut.bat    # Creates desktop shortcut
├── install.bat           # System installer
└── README.txt            # User documentation
```

## 🔧 Troubleshooting

### Common Issues

**1. "Execution policy" error:**
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

**2. "Java not found" error:**
- Ensure `minimal-jre/` folder is included
- Check antivirus hasn't removed JRE files
- Verify all files are in the same directory

**3. "JAR file not found" error:**
- Ensure `AdvancedDesktopPet.jar` is in the same folder as the EXE
- Check file permissions

**4. Application doesn't start:**
- Try running `DesktopPet.exe.bat` from command prompt
- Check Windows Event Viewer for errors
- Ensure all resource files are present

### Debug Mode

**Enable debug output:**
```batch
DesktopPet.exe.bat
```

**Check Java version:**
```batch
minimal-jre\bin\java -version
```

**Test JAR directly:**
```batch
minimal-jre\bin\java -jar AdvancedDesktopPet.jar
```

## 🎨 Customization

### Changing the Icon

1. Replace `chibi01.ico` with your custom icon
2. Update shortcut creation scripts
3. Re-run EXE creation

### Modifying the Launcher

Edit the C# code in `create_exe.ps1` to:
- Add startup splash screen
- Include version information
- Add command-line options
- Customize error messages

### Adding Dependencies

If your application uses additional libraries:
1. Include JAR files in the distribution
2. Update the launcher to include classpath
3. Test thoroughly

## 📊 File Size Optimization

### Reduce JRE Size

1. **Use jlink to create minimal runtime:**
   ```batch
   jlink --module-path "%JAVA_HOME%\jmods" --add-modules java.base,java.desktop,java.logging --output minimal-runtime
   ```

2. **Remove unnecessary modules:**
   - Remove `java.xml` if not using XML
   - Remove `java.prefs` if not using preferences
   - Remove `java.datatransfer` if not using clipboard

### Compress Resources

1. **Optimize images:**
   - Convert to PNG with compression
   - Reduce image dimensions if possible
   - Use palette optimization

2. **Compress audio:**
   - Convert WAV to MP3 or OGG
   - Reduce bitrate for smaller files

## 🔒 Security Considerations

### Code Signing

For professional distribution:
1. Obtain a code signing certificate
2. Sign the EXE file
3. Sign the JAR file

### Antivirus Whitelisting

Common antivirus software may flag the application:
1. Submit to antivirus vendors for whitelisting
2. Include digital signature
3. Provide clear documentation

### User Permissions

The application may require:
- System tray access
- Always-on-top permission
- File system access for resources

## 📈 Performance Tips

### Startup Optimization

1. **Preload resources:**
   - Load images in background
   - Cache frequently used data
   - Use lazy loading for animations

2. **Memory management:**
   - Dispose of unused resources
   - Use weak references for caches
   - Monitor memory usage

### Runtime Performance

1. **Animation optimization:**
   - Use hardware acceleration
   - Limit frame rate
   - Optimize drawing operations

2. **System resource usage:**
   - Minimize CPU usage when idle
   - Use efficient timers
   - Avoid blocking operations

## 🎯 Best Practices

### Distribution

1. **Test on clean systems:**
   - Windows 7, 8, 10, 11
   - Different screen resolutions
   - Various Java versions

2. **Include documentation:**
   - README with clear instructions
   - Troubleshooting guide
   - Feature list

3. **Version management:**
   - Include version numbers
   - Provide update mechanism
   - Maintain changelog

### User Experience

1. **First-time setup:**
   - Clear installation instructions
   - Automatic shortcut creation
   - Welcome message

2. **Error handling:**
   - User-friendly error messages
   - Automatic recovery options
   - Support contact information

3. **Accessibility:**
   - High contrast mode
   - Keyboard shortcuts
   - Screen reader support

## 📞 Support

### For Developers

- Check the source code comments
- Review the Java console output
- Use debug mode for troubleshooting

### For End Users

- Read the README.txt file
- Check the troubleshooting section
- Contact support with error details

---

**Note:** This deployment guide covers the most common scenarios. For advanced customization or enterprise deployment, consider using professional tools like InstallShield, Advanced Installer, or similar products. 