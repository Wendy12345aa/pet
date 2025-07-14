# Java Desktop Pet (桌宠)

A cute desktop pet application built with Java Swing that lives on your desktop and interacts with you!

## 🚀 **Quick Start**

**For Development:**
```batch
run_enhanced.bat    # Compile and run with error handling
```

Or manually:
```batch
javac AdvancedDesktopPet.java MusicManager.java
java AdvancedDesktopPet
```

**For Portable Package Creation:**
```batch
create_jar.bat              # Step 1: Create JAR
create_simple_launcher.bat   # Step 2: Create portable launcher
```

**For Complete Portable Package:**
```batch
create_final_exe.bat         # Complete workflow (JAR + JRE + Launcher)
```

📖 **See [STREAMLINED_WORKFLOW.md](STREAMLINED_WORKFLOW.md) for complete instructions**

## Features ✅

- **透明窗口 (Transparent Window)**: Fully transparent background
- **窗口置顶 (Always On Top)**: Pet stays on top of other windows
- **无边框 (Borderless)**: Clean, frameless window
- **动画显示 (Animations)**: Smooth idle and walking animations
- **鼠标交互 (Mouse Interactions)**:
  - Drag the pet around your screen
  - Double-click to make it jump
  - Middle-click for settings menu
  - Right-click for special animations
- **托盘图标 (System Tray)**: Control pet from system tray
- **自动移动 (Auto Movement)**: Pet randomly walks around the screen
- **敌人系统 (Enemy System)**: Horror mode with enemy pets
- **音乐系统 (Music System)**: Background music and sound effects
- **多语言支持 (Multi-language)**: English and Chinese support

## 🎯 **Controls**

- **Left-click + Drag**: Move the pet around
- **Double-click**: Make the pet jump
- **Middle-click**: Open settings menu
- **Right-click**: Trigger special animations
- **System Tray**: Right-click tray icon for show/hide/exit

## 📦 **Deployment Options**

| Option | Command | Size | Portability |
|--------|---------|------|-------------|
| **JAR Only** | `create_jar.bat` | ~42MB | Requires Java |
| **Portable Package** | `create_simple_launcher.bat` | ~80MB | No Java needed |
| **Complete Package** | `create_final_exe.bat` | ~80MB | No Java needed |

## 🛠️ **Requirements**

- **For Development**: Java 8+ (JDK recommended)
- **For Portable Package Creation**: Java 11+ (JDK with jlink)
- **For End Users**: None (if using portable package)

## 📁 **Project Structure**

```
pet/
├── AdvancedDesktopPet.java     # Main application
├── MusicManager.java           # Music system (required)
├── Image/                      # Pet sprites and assets
│   ├── chibi01.png             # Main pet character
│   ├── chibi02.png             # Alternative pet character
│   ├── chibi03.png             # Alternative pet character
│   ├── enemy01.png             # Enemy character
│   ├── enemy02.png             # Enemy character
│   ├── enemy03.png             # Enemy character
│   └── chibi01.ico             # Application icon
├── music/                      # Audio files
│   ├── normal.wav              # Normal background music
│   └── horror.wav              # Horror mode music
├── DesktopPet-Portable-EXE/    # Final portable package
│   ├── DesktopPet.bat          # Launcher (no Java needed)
│   ├── jre/                    # Embedded Java runtime
│   ├── lib/AdvancedDesktopPet.jar # Application
│   ├── resources/Image/        # All image files
│   ├── resources/music/        # All music files
│   └── chibi01.ico             # Application icon
├── run.bat                     # Basic development launcher
├── run_enhanced.bat            # Enhanced development launcher
├── create_jar.bat              # JAR creation
├── create_simple_launcher.bat  # Portable launcher creation
├── create_final_exe.bat        # Complete package creation
├── diagnose_java.bat           # Java troubleshooting
├── cleanup.bat                 # Clean up temp files
└── STREAMLINED_WORKFLOW.md     # Complete guide
```

## 🔧 **Troubleshooting**

If you encounter issues:
```batch
diagnose_java.bat    # Check Java installation
cleanup.bat          # Clean up .class files and temp artifacts
```

Common solutions:
- Install Java from [adoptium.net](https://adoptium.net/)
- Use JDK (not JRE) for development
- Ensure you have Java 11+ for portable package creation

## 🎨 **Customization**

You can customize the pet by:
1. **Replace Images**: Update files in `Image/` folder
2. **Add Music**: Add audio files to `music/` folder
3. **Modify Behavior**: Edit `AdvancedDesktopPet.java` and `MusicManager.java`
4. **Change Size**: Use settings menu or modify constants

## 📦 **Distribution**

The `DesktopPet-Portable-EXE` folder contains everything needed:
- **DesktopPet.bat** - Launcher (works without Java installation)
- **jre/** - Embedded Java runtime
- **lib/AdvancedDesktopPet.jar** - Your application
- **resources/Image/** - All character images
- **resources/music/** - All music files
- **chibi01.ico** - Application icon

Users can simply:
1. Extract the folder anywhere
2. Double-click `DesktopPet.bat`
3. No Java installation required!

## 📚 **Documentation**

- **[STREAMLINED_WORKFLOW.md](STREAMLINED_WORKFLOW.md)** - Complete development and deployment guide

Enjoy your new desktop companion! 🐾 