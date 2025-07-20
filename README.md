# 🐾 Desktop Pet (桌宠) - Interactive Desktop Companion

[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![Platform](https://img.shields.io/badge/Platform-Windows-blue.svg)](https://www.microsoft.com/windows)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-2.0-purple.svg)](https://github.com/yourusername/pet/releases)

---

## About

Desktop Pet is your digital companion—a delightful animated pet that lives on your desktop! Watch your pet walk, play, and interact with your screen, bringing joy and a peaceful atmosphere to your daily workflow. Enjoy smooth animations, calming ambient sounds, and a variety of customizable characters. Desktop Pet is lightweight, open source, and easy to personalize. Whether you want a cute friend to keep you company or a fun way to liven up your workspace, Desktop Pet is here for you.

- Animated desktop pets with natural movement
- Ambient background sounds for a relaxing environment
- Multiple characters and easy customization
- Safe, lightweight, and open source
- Simple to download and use—no installation required

Join our community, contribute, or just enjoy your new digital friend!

---

<div align="center">
  <img src="Image/chibi01.png" alt="Desktop Pet" width="200"/>
  <br/>
  <em>Your cute companion that lives on your desktop! 🎮</em>
</div>

---

## 🌟 Features / 功能特色

### ✨ Core Features / 核心功能
- 🎭 **Transparent Windows** / 透明窗口 - Seamless desktop integration
- 🎮 **Interactive Animations** / 互动动画 - Idle, walking, and special animations
- 🖱️ **Mouse Interactions** / 鼠标交互 - Drag, click, and control your pet
- 🎵 **Music System** / 音乐系统 - Background music and sound effects
- 👹 **Enemy System** / 敌人系统 - Horror mode with chasing enemies
- 🌍 **Multi-language** / 多语言 - English and Chinese support
- 🎨 **Character Import** / 角色导入 - Import custom characters and animations

### 🎯 Advanced Features / 高级功能
- 📱 **System Tray** / 系统托盘 - Control from system tray
- 🖥️ **Multi-screen** / 多屏幕 - Cross-screen movement support
- ⚙️ **Settings Panel** / 设置面板 - Comprehensive configuration options
- 🎪 **Floating Shortcut** / 浮动快捷方式 - Quick access controls
- 🔄 **Real-time Updates** / 实时更新 - Dynamic behavior changes

---

## 🚀 Quick Start / 快速开始

### For Development / 开发环境
```bash
# Clone the repository / 克隆仓库
git clone https://github.com/yourusername/pet.git
cd pet

# Run with enhanced launcher / 使用增强启动器运行
run_enhanced.bat
```

### For End Users / 最终用户
```bash
# Download the portable package / 下载便携包
# Extract and run DesktopPet.exe / 解压并运行 DesktopPet.exe
```

---

## 🎮 Controls / 操作控制

| Action / 操作 | Control / 控制 | Description / 说明 |
|---------------|----------------|-------------------|
| **Move Pet** / 移动宠物 | Left-click + Drag / 左键拖拽 | Drag your pet around the screen |
| **Jump** / 跳跃 | Double-click / 双击 | Make your pet jump |
| **Settings** / 设置 | Middle-click / 中键 | Open settings menu |
| **Special Animation** / 特殊动画 | Right-click / 右键 | Trigger special animations |
| **System Tray** / 系统托盘 | Right-click tray icon / 右键托盘图标 | Show/hide/exit options |

---

## 📦 Installation Options / 安装选项

### 🎯 Development Setup / 开发环境
```bash
# Requirements / 要求
- Java 8+ (JDK recommended)
- Windows OS

# Quick Run / 快速运行
run_enhanced.bat
```

### 📱 Portable Package / 便携包
```bash
# Complete Package / 完整包
create_final_exe.bat

# Features / 特点
- No Java installation required
- Self-contained executable
- ~80MB total size
- Ready to run anywhere
```

---

## 🏗️ Project Structure / 项目结构

```
pet/
├── 📁 src/main/java/           # Source code / 源代码
│   ├── AdvancedDesktopPet.java # Main application / 主程序
│   ├── MusicManager.java       # Music system / 音乐系统
│   └── LocationUtils.java      # Utility functions / 工具函数
├── 📁 resources/               # Game resources / 游戏资源
│   ├── CharacterSets/          # Character animations / 角色动画
│   │   ├── Pets/              # Pet characters / 宠物角色
│   │   └── Enemies/           # Enemy characters / 敌人角色
│   └── music/                 # Audio files / 音频文件
├── 📁 Image/                   # Image assets / 图像资源
├── 📁 scripts/                 # Build scripts / 构建脚本
├── 📁 docs/                    # Documentation / 文档
└── 📁 target/                  # Build output / 构建输出
```

---

## 🎨 Customization / 自定义

### 🎭 Adding Custom Characters / 添加自定义角色
1. **Prepare Images** / 准备图像
   - Create animation frames / 创建动画帧
   - Organize by behavior / 按行为分类
   - Use PNG format / 使用PNG格式

2. **Import Process** / 导入过程
   - Open Character Import Window / 打开角色导入窗口
   - Select animation folders / 选择动画文件夹
   - Configure metadata / 配置元数据
   - Save character set / 保存角色集

### 🎵 Custom Music / 自定义音乐
- Supported formats: WAV, MP3 / 支持格式：WAV, MP3
- Place in `music/` folder / 放置在 `music/` 文件夹
- Configure in settings / 在设置中配置

---

## 🛠️ Development / 开发

### 🔧 Building from Source / 从源码构建
```bash
# Compile / 编译
javac -d target src/main/java/*.java

# Run / 运行
java -cp target AdvancedDesktopPet
```

### 🧪 Testing / 测试
```bash
# Run tests / 运行测试
scripts/run/run_tests.bat

# Test specific features / 测试特定功能
scripts/run/run_enhanced.bat
```

### 📦 Creating Packages / 创建包
```bash
# JAR only / 仅JAR
create_jar.bat

# Portable package / 便携包
create_simple_launcher.bat

# Complete package / 完整包
create_final_exe.bat
```

---

## 🐛 Troubleshooting / 故障排除

### Common Issues / 常见问题

| Issue / 问题 | Solution / 解决方案 |
|--------------|-------------------|
| **Pet not visible** / 宠物不可见 | Check transparency settings / 检查透明度设置 |
| **Music not playing** / 音乐不播放 | Verify audio files in music/ folder / 验证music/文件夹中的音频文件 |
| **Enemies not spawning** / 敌人不生成 | Enable enemy system in settings / 在设置中启用敌人系统 |
| **Import window crashes** / 导入窗口崩溃 | Check image format and size / 检查图像格式和大小 |

### 🔍 Debug Mode / 调试模式
- Press `S` key to open settings / 按 `S` 键打开设置
- Check console output for errors / 检查控制台错误输出
- Verify file paths and permissions / 验证文件路径和权限

---

## 🤝 Contributing / 贡献

We welcome contributions! / 欢迎贡献！

### 📝 How to Contribute / 如何贡献
1. **Fork the repository** / Fork 仓库
2. **Create a feature branch** / 创建功能分支
3. **Make your changes** / 进行更改
4. **Test thoroughly** / 充分测试
5. **Submit a pull request** / 提交拉取请求

### 🎨 Contribution Areas / 贡献领域
- 🎭 **New Characters** / 新角色
- 🎵 **Music and Sound Effects** / 音乐和音效
- 🐛 **Bug Fixes** / 错误修复
- 📚 **Documentation** / 文档
- 🌍 **Translations** / 翻译

---

## 📄 License / 许可证

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

本项目采用 MIT 许可证 - 详情请参阅 [LICENSE](LICENSE) 文件。

---

## 🙏 Acknowledgments / 致谢

- **Java Swing** - GUI framework / GUI框架
- **OpenJDK** - Java runtime / Java运行时
- **Contributors** - Community support / 社区支持

---

## 📞 Support / 支持

- **Issues**: [GitHub Issues](https://github.com/yourusername/pet/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/pet/discussions)
- **Wiki**: [Project Wiki](https://github.com/yourusername/pet/wiki)

---

<div align="center">
  <strong>Made with ❤️ for the desktop pet community</strong>
  <br/>
  <em>为桌面宠物社区而制作 ❤️</em>
</div> 