# 🐾 桌面宠物 (Desktop Pet) - 互动桌面伙伴

[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![Platform](https://img.shields.io/badge/Platform-Windows-blue.svg)](https://www.microsoft.com/windows)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-2.0-purple.svg)](https://github.com/yourusername/pet/releases)

<div align="center">
  <img src="Image/chibi01.png" alt="桌面宠物" width="200"/>
  <br/>
  <em>生活在您桌面上的可爱伙伴！🎮</em>
</div>

---

## 🌟 功能特色

### ✨ 核心功能
- 🎭 **透明窗口** - 无缝桌面集成
- 🎮 **互动动画** - 待机、行走和特殊动画
- 🖱️ **鼠标交互** - 拖拽、点击和控制您的宠物
- 🎵 **音乐系统** - 背景音乐和音效
- 👹 **敌人系统** - 恐怖模式与追逐敌人
- 🌍 **多语言** - 英文和中文支持
- 🎨 **角色导入** - 导入自定义角色和动画

### 🎯 高级功能
- 📱 **系统托盘** - 从系统托盘控制
- 🖥️ **多屏幕** - 跨屏幕移动支持
- ⚙️ **设置面板** - 全面的配置选项
- 🎪 **浮动快捷方式** - 快速访问控制
- 🔄 **实时更新** - 动态行为变化

---

## 🚀 快速开始

### 开发环境
```bash
# 克隆仓库
git clone https://github.com/yourusername/pet.git
cd pet

# 使用增强启动器运行
run_enhanced.bat
```

### 最终用户
```bash
# 下载便携包
# 解压并运行 DesktopPet.exe
```

---

## 🎮 操作控制

| 操作 | 控制 | 说明 |
|------|------|------|
| **移动宠物** | 左键拖拽 | 在屏幕上拖拽您的宠物 |
| **跳跃** | 双击 | 让您的宠物跳跃 |
| **设置** | 中键 | 打开设置菜单 |
| **特殊动画** | 右键 | 触发特殊动画 |
| **系统托盘** | 右键托盘图标 | 显示/隐藏/退出选项 |

---

## 📦 安装选项

### 🎯 开发环境
```bash
# 要求
- Java 8+ (推荐 JDK)
- Windows 操作系统

# 快速运行
run_enhanced.bat
```

### 📱 便携包
```bash
# 完整包
create_final_exe.bat

# 特点
- 无需安装 Java
- 自包含可执行文件
- 总大小约 80MB
- 随处可运行
```

---

## 🏗️ 项目结构

```
pet/
├── 📁 src/main/java/           # 源代码
│   ├── AdvancedDesktopPet.java # 主程序
│   ├── MusicManager.java       # 音乐系统
│   └── LocationUtils.java      # 工具函数
├── 📁 resources/               # 游戏资源
│   ├── CharacterSets/          # 角色动画
│   │   ├── Pets/              # 宠物角色
│   │   └── Enemies/           # 敌人角色
│   └── music/                 # 音频文件
├── 📁 Image/                   # 图像资源
├── 📁 scripts/                 # 构建脚本
├── 📁 docs/                    # 文档
└── 📁 target/                  # 构建输出
```

---

## 🎨 自定义

### 🎭 添加自定义角色
1. **准备图像**
   - 创建动画帧
   - 按行为分类
   - 使用 PNG 格式

2. **导入过程**
   - 打开角色导入窗口
   - 选择动画文件夹
   - 配置元数据
   - 保存角色集

### 🎵 自定义音乐
- 支持格式：WAV, MP3
- 放置在 `music/` 文件夹
- 在设置中配置

---

## 🛠️ 开发

### 🔧 从源码构建
```bash
# 编译
javac -d target src/main/java/*.java

# 运行
java -cp target AdvancedDesktopPet
```

### 🧪 测试
```bash
# 运行测试
scripts/run/run_tests.bat

# 测试特定功能
scripts/run/run_enhanced.bat
```

### 📦 创建包
```bash
# 仅 JAR
create_jar.bat

# 便携包
create_simple_launcher.bat

# 完整包
create_final_exe.bat
```

---

## 🐛 故障排除

### 常见问题

| 问题 | 解决方案 |
|------|----------|
| **宠物不可见** | 检查透明度设置 |
| **音乐不播放** | 验证 music/ 文件夹中的音频文件 |
| **敌人不生成** | 在设置中启用敌人系统 |
| **导入窗口崩溃** | 检查图像格式和大小 |

### 🔍 调试模式
- 按 `S` 键打开设置
- 检查控制台错误输出
- 验证文件路径和权限

---

## 🤝 贡献

欢迎贡献！

### 📝 如何贡献
1. **Fork 仓库**
2. **创建功能分支**
3. **进行更改**
4. **充分测试**
5. **提交拉取请求**

### 🎨 贡献领域
- 🎭 **新角色**
- 🎵 **音乐和音效**
- 🐛 **错误修复**
- 📚 **文档**
- 🌍 **翻译**

---

## 📄 许可证

本项目采用 MIT 许可证 - 详情请参阅 [LICENSE](LICENSE) 文件。

---

## 🙏 致谢

- **Java Swing** - GUI 框架
- **OpenJDK** - Java 运行时
- **贡献者** - 社区支持

---

## 📞 支持

- **问题反馈**: [GitHub Issues](https://github.com/yourusername/pet/issues)
- **讨论**: [GitHub Discussions](https://github.com/yourusername/pet/discussions)
- **维基**: [项目维基](https://github.com/yourusername/pet/wiki)

---

<div align="center">
  <strong>为桌面宠物社区而制作 ❤️</strong>
  <br/>
  <em>Made with ❤️ for the desktop pet community</em>
</div> 