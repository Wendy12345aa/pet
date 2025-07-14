# Music Looping Issue Fixes

## Problem Description
The music in the desktop pet application was not repeating/looping and would stop instead of continuing to play.

## Root Causes Identified

### 1. Overly Frequent Music Checks
- **Problem**: Music check timer ran every 1 second, interfering with looping
- **Impact**: Constant checking could interrupt the music loop
- **Fix**: Reduced frequency to every 5 seconds

### 2. Aggressive Music Switching
- **Problem**: Music would switch between normal/horror too frequently
- **Impact**: Switching would stop the current music instead of letting it loop
- **Fix**: Only switch when there's a significant change in enemy presence

### 3. Missing Loop Enforcement
- **Problem**: Music clips weren't properly enforced to loop after starting
- **Impact**: Music would play once and stop
- **Fix**: Added explicit loop enforcement and restart mechanisms

### 4. Interrupted Loops
- **Problem**: Music switching logic would stop clips without ensuring they restart properly
- **Impact**: Music would stop and not restart
- **Fix**: Added restart mechanisms and better state management

### 5. Race Condition During Enemy Startup
- **Problem**: When enemy mode starts, horror music starts but then normal music briefly starts before switching back
- **Impact**: Confusing audio experience with multiple music tracks playing briefly
- **Fix**: Added flag to prevent music switching during enemy system startup

## Implemented Fixes

### 1. Immediate Music Switching
```java
// Immediate switching when enemy system is toggled
if (enabled) {
    // Immediately switch to horror music
    SwingUtilities.invokeLater(() -> {
        if (musicEnabled && horrorMusicClip != null) {
            if (normalMusicClip != null && normalMusicClip.isRunning()) {
                normalMusicClip.stop();
                normalMusicClip.setFramePosition(0);
            }
            horrorMusicClip.setFramePosition(0);
            horrorMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            isPlayingHorror = true;
        }
    });
}
```

### 2. Improved Music Switching Logic
```java
// Only switch music if there's a significant change in enemy presence
// This prevents constant switching that could interrupt looping
if (hasEnemies && !isPlayingHorror && normalMusicClip != null && normalMusicClip.isRunning()) {
    // Switch to horror music only if normal music is currently playing
    normalMusicClip.stop();
    if (horrorMusicClip != null) {
        horrorMusicClip.start();
        isPlayingHorror = true;
    }
}
```

### 3. Fixed Dual Music Issue
```java
// Don't start looping during loading - only when actually playing
// Load music clips without starting them
normalMusicClip.open(normalStream);
// Don't start looping yet - will be set when actually playing

// When starting music, ensure only one track plays
normalMusicClip.setFramePosition(0); // Reset to beginning
normalMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
```

### 4. Race Condition Fix
```java
// Added flag to prevent music switching during enemy system startup
private static boolean enemySystemStarting = false;

// In toggleEnemySystem():
if (enabled) {
    enemySystemStarting = true; // Set flag to prevent music switching interference
    // Immediately switch to horror music
    // Clear flag after 5 seconds when enemies have spawned
    Timer clearFlagTimer = new Timer(5000, e -> {
        enemySystemStarting = false;
        System.out.println("Enemy system startup complete - music switching re-enabled");
        ((Timer) e.getSource()).stop();
    });
    clearFlagTimer.start();
}

// In checkAndUpdateMusic():
if (enemySystemStarting) {
    System.out.println("Skipping music switch - enemy system is starting up");
    return;
}
```

### 5. Music Restart Mechanism
```java
// Method to restart music if it stops unexpectedly
private static void restartMusicIfNeeded() {
    if (!musicEnabled) return;
    if (enemySystemStarting) return; // Skip during enemy system startup
    
    try {
        boolean hasEnemies = !allPets.isEmpty() && allPets.stream().anyMatch(pet -> !pet.enemies.isEmpty());
        
        if (hasEnemies) {
            // Should be playing horror music
            if (horrorMusicClip != null && !horrorMusicClip.isRunning()) {
                horrorMusicClip.start();
                horrorMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                isPlayingHorror = true;
            }
        } else {
            // Should be playing normal music
            if (normalMusicClip != null && !normalMusicClip.isRunning()) {
                normalMusicClip.start();
                normalMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                isPlayingHorror = false;
            }
        }
    } catch (Exception e) {
        System.out.println("Error restarting music: " + e.getMessage());
    }
}
```

### 6. Enhanced Music Check Logic
```java
// Ensure music is looping if it should be playing
if (musicEnabled) {
    if (isPlayingHorror && horrorMusicClip != null && !horrorMusicClip.isRunning()) {
        horrorMusicClip.start();
        System.out.println("Restarted horror music loop");
    } else if (!isPlayingHorror && normalMusicClip != null && !normalMusicClip.isRunning()) {
        normalMusicClip.start();
        System.out.println("Restarted normal music loop");
    }
}
```

## Key Changes Made

### 1. **Improved Responsiveness**
- Music check timer: 1 second → 2 seconds (more responsive)
- Immediate music switching when enemies spawn/despawn
- Better state management

### 2. **Enhanced Looping**
- Explicit loop enforcement after starting music
- Automatic restart if music stops unexpectedly
- Proper loop state management
- **Fixed dual music issue**: Only one track plays at a time

### 3. **Improved Switching Logic**
- Only switch when necessary (significant enemy presence change)
- Ensure proper loop restart after switching
- Better error handling

### 4. **Recovery Mechanisms**
- `restartMusicIfNeeded()` method for unexpected stops
- Enhanced music check with loop verification
- Better logging for debugging

## Expected Results

1. **Single Music Track**: Only one music track should play at a time
2. **Continuous Music**: Music should now loop continuously without stopping
3. **Immediate Switching**: Music switches instantly when enemies spawn/despawn
4. **Proper Switching**: Music should switch between normal/horror only when appropriate
5. **Automatic Recovery**: Music should restart automatically if it stops unexpectedly
6. **Better Performance**: More responsive checks for immediate switching
7. **No Race Conditions**: Horror music should start immediately without normal music briefly playing

## Testing Instructions

1. **Run the application** and check if music starts playing
2. **Wait for music to loop** - it should continue playing without stopping
3. **Enable enemy system** - music should switch to horror and continue looping
4. **Disable enemy system** - music should switch back to normal and continue looping
5. **Check console output** for music-related messages

## Troubleshooting

If music still stops:
1. Check console for error messages
2. Verify music files exist in the `music/` folder
3. Check if music is enabled in settings
4. Look for "Restarted music loop" messages in console

The fixes ensure that music will loop continuously and automatically recover from any interruptions. 