# Desktop Pet - Streamlined Workflow Guide

## 🎯 **Quick Start**

This guide provides a **simplified workflow** for developing and deploying the Desktop Pet application. All complex scripts have been removed, leaving only essential tools.

---

## 📋 **Essential Files Overview**

### **Core Application:**
- `AdvancedDesktopPet.java` - Main application source code
- `Image/` - Pet sprites and assets
- `music/` - Audio files

### **Development Scripts:**
- `run.bat` - Basic compile and run
- `run_enhanced.bat` - Enhanced compile and run with error handling

### **Deployment Scripts:**
- `create_jar.bat` - Creates JAR file
- `create_minimal_jre.bat` - Creates portable Java runtime
- `create_exe.ps1` - Creates standalone EXE file

### **Troubleshooting:**
- `diagnose_java.bat` - Java installation diagnostics

### **Utility Scripts:**
- `cleanup.bat` - Remove .class files and temporary artifacts

---

## 🧹 **Cleanup Workflow**

### **Clean Project Files**
```batch
cleanup.bat
```

**What it does:**
- Removes all .class files from compilation
- Cleans up temporary directories
- Removes backup files (*.bak)
- Removes log files (*.log)
- Removes system files (Thumbs.db, .DS_Store)
- Optionally removes old deployment artifacts
- Provides cleanup summary

**When to use:**
- Before committing to git
- After multiple compilation attempts
- When switching between development and deployment
- Regular maintenance to keep project clean

---

## 🔧 **Development Workflow**

### **Option 1: Basic Development**
```batch
# Compile and run the application
run.bat
```

**What it does:**
- Compiles `AdvancedDesktopPet.java`
- Runs the application if compilation succeeds
- Shows error messages if compilation fails

### **Option 2: Enhanced Development**
```batch
# More robust compile and run
run_enhanced.bat
```

**What it does:**
- Checks if Java is installed
- Tries to run existing JAR file first
- Falls back to compiling from source if needed
- Better error handling and messages

---

## 🚀 **EXE Deployment Workflow**

### **Step 1: Create JAR File**
```batch
create_jar.bat
```

**What it does:**
- Cleans old class files
- Compiles the Java source code
- Creates `AdvancedDesktopPet.jar`
- Copies JAR to portable folder

**Output:** `AdvancedDesktopPet.jar`

### **Step 2: Create Portable JRE (Optional)**
```batch
create_minimal_jre.bat
```

**What it does:**
- Creates a minimal Java runtime (only needed modules)
- Significantly smaller than full JRE
- Enables true portability (no Java installation needed)
- Creates `minimal-jre/` folder

**Output:** `minimal-jre/` folder (~30-50MB instead of 200+MB)

### **Step 3: Create EXE File**
```batch
create_exe.ps1
```

**What it does:**
- Creates a true Windows EXE file
- Includes C# launcher that finds Java automatically
- Creates desktop shortcut
- Works with both system Java and embedded JRE

**Output:** 
- `DesktopPet.exe` - Main executable
- `DesktopPet.bat` - Backup launcher
- Desktop shortcut

---

## 📦 **Deployment Scenarios**

### **Scenario 1: Full Portable (Recommended)**
```batch
# Create complete portable package
create_jar.bat
create_minimal_jre.bat
create_exe.ps1
```

**Result:** 
- Works on any Windows PC
- No Java installation required
- ~50-80MB total size
- Professional deployment

### **Scenario 2: Lightweight (Requires Java)**
```batch
# Create EXE that requires Java
create_jar.bat
create_exe.ps1
```

**Result:**
- Smaller package size
- Requires Java installation on target PC
- ~1-2MB total size
- Good for tech-savvy users

### **Scenario 3: JAR Only (Developer)**
```batch
# Just create JAR file
create_jar.bat
```

**Result:**
- Requires Java installation
- Run with: `java -jar AdvancedDesktopPet.jar`
- Smallest size (~1MB)
- Good for developers

---

## 🛠️ **Troubleshooting**

### **Java Issues**
If any script fails, run:
```batch
diagnose_java.bat
```

**This will check:**
- ✅ Java installation
- ✅ JDK vs JRE
- ✅ jlink availability
- ✅ JAVA_HOME setting
- ✅ Required directories

### **Common Problems & Solutions**

| Problem | Solution |
|---------|----------|
| `java` command not found | Install Java from [adoptium.net](https://adoptium.net/) |
| `javac` command not found | Install JDK (not JRE) |
| `jlink` command not found | Install JDK 11+ |
| JAVA_HOME not set | Set environment variable to JDK path |
| Minimal JRE creation fails | Run `diagnose_java.bat` for details |
| EXE doesn't work | Check if JAR was created successfully |

---

## 📁 **Final Package Structure**

After successful deployment, you'll have:

```
📦 Distribution Package
├── DesktopPet.exe           # Main executable
├── DesktopPet.bat           # Backup launcher
├── AdvancedDesktopPet.jar   # Application JAR
├── minimal-jre/             # Portable Java runtime
│   └── bin/java.exe         # Java executable
├── Image/                   # Pet sprites
│   ├── chibi01.png
│   ├── chibi02.png
│   ├── chibi03.png
│   ├── enemy01.png
│   ├── enemy02.png
│   └── enemy03.png
└── music/                   # Audio files
```

---

## 🎯 **Usage Instructions**

### **For End Users:**
1. Double-click `DesktopPet.exe`
2. Pet appears on desktop
3. **Controls:**
   - Left-click + drag to move
   - Double-click for jump animation
   - Middle-click for settings
   - Right-click for special animations
   - System tray icon for show/hide

### **For Developers:**
1. Edit `AdvancedDesktopPet.java`
2. Test with `run_enhanced.bat`
3. Deploy with 3-step process above

---

## 📊 **File Size Comparison**

| Deployment Type | Size | Java Required | Portability |
|----------------|------|---------------|-------------|
| JAR Only | ~1MB | ✅ Yes | ❌ Low |
| EXE + System Java | ~2MB | ✅ Yes | ⚠️ Medium |
| EXE + Embedded JRE | ~80MB | ❌ No | ✅ High |

---

## 🚀 **Quick Commands Reference**

```batch
# Development
run.bat                     # Quick test
run_enhanced.bat           # Robust test

# Deployment
create_jar.bat             # Step 1: Create JAR
create_minimal_jre.bat     # Step 2: Create portable JRE
create_exe.ps1             # Step 3: Create EXE

# Troubleshooting
diagnose_java.bat          # Check Java setup

# Cleanup
cleanup.bat                # Remove .class files and temp artifacts
```

---

## 💡 **Tips & Best Practices**

1. **Always test with `run_enhanced.bat` before deploying**
2. **Use full portable deployment for distribution**
3. **Keep `minimal-jre` folder with your EXE**
4. **Test on a clean Windows machine without Java**
5. **Include all files in the Image/ directory**
6. **Don't delete the JAR file after creating EXE**
7. **Run `cleanup.bat` regularly to keep project clean**

---

## 🔗 **Additional Resources**

- **Java Installation:** [adoptium.net](https://adoptium.net/)
- **PowerShell Execution Policy:** Run as Administrator: `Set-ExecutionPolicy RemoteSigned`
- **Windows Defender:** May flag EXE as unknown - add exception if needed

---

**Your desktop pet is now ready for professional deployment! 🎉** 