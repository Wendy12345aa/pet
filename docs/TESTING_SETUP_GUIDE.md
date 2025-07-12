# Desktop Pet - Testing Setup Guide

## 🚀 **Quick Setup**

### **Step 1: Download JUnit**
Download the JUnit standalone JAR (all-in-one):
```
https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.9.2/junit-platform-console-standalone-1.9.2.jar
```

Place it in your project root directory (same folder as `AdvancedDesktopPet.java`)

### **Step 2: Run the Tests**
```batch
run_tests.bat
```

That's it! The batch script will:
- Create the test directory structure
- Compile your main classes
- Compile your test classes
- Run the tests
- Show you the results

---

## 📝 **What's Included**

### **Test Files Created:**
- `src/test/java/AnimationSequenceTest.java` - Tests for animation frame logic
- `run_tests.bat` - Automated test runner

### **What Gets Tested:**
- ✅ **Animation frame navigation** (next, previous, reset)
- ✅ **Looping vs non-looping behavior** 
- ✅ **Frame management** (add, remove, count)
- ✅ **Edge cases** (empty sequences, invalid indices)
- ✅ **Property management** (name, duration, loop settings)

---

## 🎯 **Example Test Output**

When you run `run_tests.bat`, you should see:
```
Desktop Pet - Unit Test Runner
================================

1. Creating test directory structure...
2. Checking for JUnit JAR...
3. Compiling main classes...
4. Compiling test classes...
5. Running tests...

Thanks for using JUnit! Support its development at https://junit.org/sponsoring

├─ AnimationSequenceTest ✔
│  ├─ testEmptySequence() ✔
│  ├─ testSequenceBasicProperties() ✔
│  ├─ testAddFrames() ✔
│  ├─ testFrameNavigation() ✔
│  ├─ testLoopingBehavior() ✔
│  ├─ testNonLoopingBehavior() ✔
│  ├─ testReset() ✔
│  ├─ testRemoveFrame() ✔
│  ├─ testRemoveInvalidIndex() ✔
│  ├─ testSetLoopProperty() ✔
│  └─ testFrameListAccess() ✔

Test run finished after 156 ms
[        11 containers found      ]
[         0 containers skipped    ]
[        11 containers successful ]
[         0 containers failed     ]
[        11 tests found           ]
[         0 tests skipped         ]
[        11 tests successful      ]
[         0 tests failed          ]

All tests passed!
```

---

## 🔧 **Adding More Tests**

### **Create New Test Files:**
1. Create new `.java` files in `src/test/java/`
2. Follow the pattern in `AnimationSequenceTest.java`
3. Use `@Test` annotation for each test method
4. Run `run_tests.bat` to execute all tests

### **Good Next Candidates:**
- `CharacterSetTest.java` - Test character set validation
- `LocationUtilsTest.java` - Test location calculation methods
- `CharacterFileManagerTest.java` - Test file validation utilities

### **Example New Test:**
```java
public class CharacterSetTest {
    @Test
    void testEmptyCharacterSetIsIncomplete() {
        CharacterSet set = new CharacterSet("test", "/path");
        assertFalse(set.isComplete());
    }
    
    @Test
    void testCharacterSetWithIdleAndWalkingIsComplete() {
        CharacterSet set = new CharacterSet("test", "/path");
        set.getIdleAnimation().addFrame(mockFrame());
        set.getWalkingAnimation().addFrame(mockFrame());
        assertTrue(set.isComplete());
    }
}
```

---

## 🐛 **Troubleshooting**

### **"JUnit JAR not found"**
- Make sure you downloaded the exact file from the URL above
- Place it in the same directory as `AdvancedDesktopPet.java`
- The filename should be exactly: `junit-platform-console-standalone-1.9.2.jar`

### **"Test compilation failed"**
- Make sure you have test files in `src/test/java/`
- The test files should have proper imports and `@Test` annotations
- Check that your main classes compile successfully first

### **"Some tests failed"**
- This is normal if you find bugs! 
- Read the test output to see which tests failed
- Fix the bugs in your main code or update the tests if needed

---

## 📊 **Benefits You'll See**

### **Immediate:**
- 🐛 **Catch bugs early** - Tests fail when you break something
- 🔍 **Validate edge cases** - Tests cover unusual scenarios
- 📚 **Document behavior** - Tests show how your code should work

### **Long-term:**
- 🔧 **Safer refactoring** - Change code with confidence
- 🚀 **Faster development** - Automated testing saves time
- 💪 **Better code quality** - Tests encourage good design

---

## 🎉 **Next Steps**

1. **Download JUnit JAR** and place it in your project root
2. **Run `run_tests.bat`** to see the tests pass
3. **Make a small change** to `AnimationSequence` class and run tests again
4. **Watch a test fail** - this shows the tests are working!
5. **Fix the change** and watch the tests pass again
6. **Add more tests** for other classes

Welcome to the world of unit testing! 🎯 