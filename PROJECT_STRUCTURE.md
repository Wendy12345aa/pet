# Project Structure

## Files Overview

### Core Applications
- **`DesktopPet.java`** - Basic desktop pet with all essential features
- **`AdvancedDesktopPet.java`** - Enhanced version with additional behaviors and GIF support

### Helper Files
- **`run.bat`** - Windows batch script for easy compilation and execution
- **`README.md`** - Main documentation with features and usage instructions
- **`PROJECT_STRUCTURE.md`** - This file, explaining the project structure

## File Descriptions

### DesktopPet.java (Basic Version)
- **Size**: ~320 lines
- **Features**:
  - Transparent, borderless window
  - Always on top
  - Animated pet (simple colored circles with faces)
  - Mouse drag interaction
  - Double-click to jump
  - Random movement across screen
  - System tray integration
  - Show/Hide functionality

### AdvancedDesktopPet.java (Enhanced Version)
- **Size**: ~440 lines
- **Features**:
  - All basic features plus:
  - GIF animation support (loads external files)
  - Multiple behavior modes (Idle/Active)
  - Special animations triggered randomly
  - Right-click for special effects
  - Enhanced tray menu with behavior controls
  - Settings menu (sound toggle)
  - Better error handling for missing assets
  - Fallback to text-based animations if no GIFs found

## Usage

### Quick Start (Basic Version)
```bash
javac DesktopPet.java
java DesktopPet
```

### Enhanced Experience (Advanced Version)  
```bash
javac AdvancedDesktopPet.java
java AdvancedDesktopPet
```

### Windows Users
Simply double-click `run.bat` to compile and run the basic version.

## Customization Guide

### Adding Your Own Pet Images
1. Create an `images/` folder
2. Add these GIF files:
   - `idle.gif` - Pet's idle animation
   - `walk.gif` - Pet's walking animation
   - `special1.gif` to `special5.gif` - Special animations

### Modifying Pet Behavior
- **Animation Speed**: Change `ANIMATION_DELAY` constant
- **Movement Speed**: Adjust timer delays in movement methods
- **Pet Size**: Modify `DEFAULT_WIDTH` and `DEFAULT_HEIGHT`
- **Colors**: Edit color arrays in animation creation methods

## System Requirements
- Java 7+ (for transparency support)
- Windows/macOS/Linux with Java support
- System tray support (for tray functionality)

Enjoy your desktop companion! 🐾 