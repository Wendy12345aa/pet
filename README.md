# Java Desktop Pet (桌宠)

A cute desktop pet application built with Java Swing that lives on your desktop and interacts with you!

## 🚀 **Quick Start**

**For Development:**
```batch
run_enhanced.bat    # Compile and run with error handling
```

**For EXE Deployment:**
```batch
create_jar.bat      # Step 1: Create JAR
create_minimal_jre.bat   # Step 2: Create portable JRE  
create_exe.ps1      # Step 3: Create EXE
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
| **JAR Only** | `create_jar.bat` | ~1MB | Requires Java |
| **EXE + System Java** | `create_jar.bat` → `create_exe.ps1` | ~2MB | Requires Java |
| **Portable EXE** | All 3 commands | ~80MB | No Java needed |

## 🛠️ **Requirements**

- **For Development**: Java 8+ (JDK recommended)
- **For EXE Creation**: Java 11+ (JDK with jlink)
- **For End Users**: None (if using portable EXE)

## 📁 **Project Structure**

```
pet/
├── AdvancedDesktopPet.java     # Main application
├── Image/                      # Pet sprites and assets
├── music/                      # Audio files
├── run.bat                     # Basic development
├── run_enhanced.bat           # Enhanced development
├── create_jar.bat             # JAR creation
├── create_minimal_jre.bat     # Portable JRE creation
├── create_exe.ps1             # EXE creation
├── diagnose_java.bat          # Java troubleshooting
├── cleanup.bat                # Clean up temp files
└── STREAMLINED_WORKFLOW.md    # Complete guide
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
- Run PowerShell as Administrator for EXE creation

## 🎨 **Customization**

You can customize the pet by:
1. **Replace Images**: Update files in `Image/` folder
2. **Add Music**: Add audio files to `music/` folder
3. **Modify Behavior**: Edit `AdvancedDesktopPet.java`
4. **Change Size**: Use settings menu or modify constants

## 📖 **Documentation**

- **[STREAMLINED_WORKFLOW.md](STREAMLINED_WORKFLOW.md)** - Complete development and deployment guide
- **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** - Detailed deployment instructions
- **[SETUP_README.md](SETUP_README.md)** - Setup instructions

Enjoy your new desktop companion! 🐾 