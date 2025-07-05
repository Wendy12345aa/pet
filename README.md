# Java Desktop Pet (桌宠)

A cute desktop pet application built with Java Swing that lives on your desktop and interacts with you!

## Features ✅

- **透明窗口 (Transparent Window)**: Fully transparent background
- **窗口置顶 (Always On Top)**: Pet stays on top of other windows
- **无边框 (Borderless)**: Clean, frameless window
- **动画显示 (Animations)**: Smooth idle and walking animations
- **鼠标交互 (Mouse Interactions)**:
  - Drag the pet around your screen
  - Double-click to make it jump
- **托盘图标 (System Tray)**: Control pet from system tray
- **自动移动 (Auto Movement)**: Pet randomly walks around the screen

## How to Use

### Compile and Run
```bash
# Compile
javac DesktopPet.java

# Run
java DesktopPet
```

### Interactions
- **Drag**: Click and drag the pet to move it around
- **Double-click**: Make the pet jump
- **System Tray**: Right-click the tray icon to show/hide or exit
- **Auto Movement**: Pet will randomly walk around every 3-7 seconds

### System Tray Menu
- **Show Pet**: Make the pet visible
- **Hide Pet**: Hide the pet (still running in background)
- **Exit**: Close the application completely

## Customization

You can easily customize the pet by:

1. **Replace Animations**: Modify the `loadAnimations()` method to load your own images or GIFs
2. **Change Size**: Adjust `WINDOW_WIDTH` and `WINDOW_HEIGHT` constants
3. **Animation Speed**: Modify `ANIMATION_DELAY` for faster/slower animations
4. **Movement Pattern**: Edit the `startRandomWalk()` method for different movement behaviors

## Loading Custom Images/GIFs

To use your own pet images, replace the `createCircleImage()` calls with actual image loading:

```java
// Example: Load GIF animation
ImageIcon petGif = new ImageIcon("path/to/your/pet.gif");
petLabel.setIcon(petGif);
```

## Requirements

- Java 7+ (for transparent window support)
- Any operating system with Java support
- System tray support (available on most modern systems)

## Technical Details

- Built with Java Swing/AWT
- Uses `JWindow` for borderless transparent window
- `SystemTray` for tray icon functionality
- `Timer` for animations and movement
- Mouse event handling for interactions

Enjoy your new desktop companion! 🐾 