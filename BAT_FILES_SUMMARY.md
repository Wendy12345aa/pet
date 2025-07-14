# Desktop Pet - Batch Files Summary

After cleanup, this project has **one .bat file per function** to avoid confusion and duplication.

## 📁 Root Directory

### `quick_run.bat`
- **Function**: Quick development run
- **What it does**: Compiles Java files to `target/` directory and runs the application
- **Use when**: You want to quickly test changes during development
- **Output**: Clean - no .class files in source directory

### `DesktopPet.bat`
- **Function**: Production run from JAR
- **What it does**: Runs the application from a compiled JAR file
- **Use when**: You have a built JAR file and want to run the application
- **Prerequisites**: Requires `lib/AdvancedDesktopPet.jar` and portable JRE

### `cleanup_root.bat`
- **Function**: Clean up root directory
- **What it does**: Removes old files that have been moved to organized directories
- **Use when**: After organizing the project structure
- **Safety**: Asks for confirmation before deleting

### `cleanup_empty_folders.bat`
- **Function**: Remove empty directories
- **What it does**: Removes empty placeholder directories created during organization
- **Use when**: After initial project setup
- **Safety**: Asks for confirmation before deleting

## 📁 scripts/run/

### `run_tests.bat`
- **Function**: Run unit tests
- **What it does**: Compiles and runs JUnit tests
- **Use when**: You want to verify your code works correctly
- **Prerequisites**: Requires JUnit JAR in `lib/` directory

## 📁 scripts/build/

### `create_jar.bat`
- **Function**: Build JAR file
- **What it does**: Compiles Java files to `target/` and creates a JAR with all resources
- **Use when**: You want to create a distributable JAR file
- **Output**: `target/AdvancedDesktopPet.jar`

### `create_exe.bat`
- **Function**: Build EXE file
- **What it does**: Creates a Windows executable that runs the JAR
- **Use when**: You want to create a Windows executable
- **Prerequisites**: Requires built JAR file and portable JRE
- **Output**: `DesktopPet-EXE/` package

## 📁 scripts/setup/

### `create_portable_jre.bat`
- **Function**: Create portable Java runtime
- **What it does**: Copies essential Java files to create a portable JRE
- **Use when**: You want to distribute the application without requiring Java installation
- **Output**: `portable-jre/` directory

## 🎯 Recommended Workflow

1. **Development**: Use `quick_run.bat` for quick testing
2. **Testing**: Use `scripts/run/run_tests.bat` to run unit tests
3. **Building**: Use `scripts/build/create_jar.bat` to create JAR
4. **Distribution**: Use `scripts/build/create_exe.bat` to create EXE
5. **Setup**: Use `scripts/setup/create_portable_jre.bat` for portable JRE

## ✅ Benefits of This Organization

- **No duplicates**: Only one script per function
- **Clean source**: All .class files go to `target/` directory
- **Organized structure**: Scripts are in logical directories
- **Clear purpose**: Each script has a specific, well-defined function
- **Professional**: Follows standard project organization practices 