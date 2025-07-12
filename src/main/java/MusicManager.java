import javax.sound.sampled.*;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * Manages all music-related functionality for the desktop pet application.
 * Handles normal/horror music switching, looping, and race condition prevention.
 */
public class MusicManager {
    // Music system
    private static Clip normalMusicClip;
    private static Clip horrorMusicClip;
    private static boolean musicEnabled = true;
    private static boolean isPlayingHorror = false;
    private static boolean wasPlayingHorrorBeforeDisable = false; // Track state before disable
    private static Timer musicCheckTimer;
    private static boolean musicInitialized = false; // Track if music system is already initialized
    private static boolean enemySystemStarting = false; // Prevent music switching during enemy startup
    
    // Reference to pet list for enemy detection
    private static List<AdvancedDesktopPet> allPets = new ArrayList<>();
    
    /**
     * Initialize the music system
     */
    public static void initialize() {
        if (musicInitialized) {
            System.out.println("Music system already initialized");
            return;
        }
        
        try {
            // Load normal music
            normalMusicClip = loadMusicClip("music/normal.wav");
            if (normalMusicClip != null) {
                normalMusicClip.setFramePosition(0);
                System.out.println("Normal music loaded successfully");
            }
            
            // Load horror music
            horrorMusicClip = loadMusicClip("music/horror.wav");
            if (horrorMusicClip != null) {
                horrorMusicClip.setFramePosition(0);
                System.out.println("Horror music loaded successfully");
            }
            
            // Start music check timer (every 2 seconds for responsiveness)
            musicCheckTimer = new Timer(2000, e -> checkAndUpdateMusic());
            musicCheckTimer.start();
            
            // Start music restart timer (every 10 seconds)
            Timer restartTimer = new Timer(10000, e -> restartMusicIfNeeded());
            restartTimer.start();
            
            musicInitialized = true;
            System.out.println("Music system initialized successfully");
            
            // Start normal music if enabled
            if (musicEnabled && normalMusicClip != null) {
                normalMusicClip.setFramePosition(0);
                normalMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                isPlayingHorror = false;
                System.out.println("Started normal music with looping");
            }
            
        } catch (Exception e) {
            System.out.println("Error initializing music system: " + e.getMessage());
        }
    }
    
    /**
     * Load a music clip from file
     */
    private static Clip loadMusicClip(String filename) {
        try {
            // Try to load from resources first
            java.net.URL resource = MusicManager.class.getResource("/" + filename);
            AudioInputStream audioStream = null;
            
            if (resource != null) {
                audioStream = AudioSystem.getAudioInputStream(resource);
            } else {
                // Try to load from current directory
                File file = new File(filename);
                if (file.exists()) {
                    audioStream = AudioSystem.getAudioInputStream(file);
                } else {
                    // Try alternative paths
                    String[] alternatives = {
                        "music/" + filename.substring(filename.lastIndexOf("/") + 1),
                        filename.replace("/", "\\"),
                        "music\\" + filename.substring(filename.lastIndexOf("/") + 1)
                    };
                    
                    for (String alt : alternatives) {
                        File altFile = new File(alt);
                        if (altFile.exists()) {
                            audioStream = AudioSystem.getAudioInputStream(altFile);
                            break;
                        }
                    }
                }
            }
            
            if (audioStream != null) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                return clip;
            }
            
        } catch (Exception e) {
            System.out.println("Error loading music file " + filename + ": " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Check and update music based on enemy presence
     */
    private static void checkAndUpdateMusic() {
        if (!musicEnabled) {
            // If music is disabled, stop all music and remember state
            if (normalMusicClip != null && normalMusicClip.isRunning()) {
                normalMusicClip.stop();
                System.out.println("Music disabled - stopped normal music");
                wasPlayingHorrorBeforeDisable = false;
            }
            if (horrorMusicClip != null && horrorMusicClip.isRunning()) {
                horrorMusicClip.stop();
                wasPlayingHorrorBeforeDisable = true;
                System.out.println("Music disabled - stopped horror music");
            }
            return;
        }
        
        // Check if no music is currently playing (music was just re-enabled or stopped)
        if (normalMusicClip != null && !normalMusicClip.isRunning() && 
            horrorMusicClip != null && !horrorMusicClip.isRunning()) {
            
            boolean hasEnemies = !allPets.isEmpty() && allPets.stream().anyMatch(pet -> !pet.getEnemies().isEmpty());
            
            if (hasEnemies || wasPlayingHorrorBeforeDisable) {
                // Start horror music if enemies are present or was playing before
                horrorMusicClip.setFramePosition(0); // Reset to beginning
                horrorMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                isPlayingHorror = true;
                wasPlayingHorrorBeforeDisable = false;
                System.out.println("Music re-enabled - started horror music with looping");
            } else {
                // Start normal music
                normalMusicClip.setFramePosition(0); // Reset to beginning
                normalMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                isPlayingHorror = false;
                wasPlayingHorrorBeforeDisable = false;
                System.out.println("Music re-enabled - started normal music with looping");
            }
            return;
        }
        
        // Only switch music if there's a significant change in enemy presence
        // This prevents constant switching that could interrupt looping
        // Skip switching if enemy system is starting up
        if (enemySystemStarting) {
            System.out.println("Skipping music switch - enemy system is starting up");
            return;
        }
        
        boolean hasEnemies = !allPets.isEmpty() && allPets.stream().anyMatch(pet -> !pet.getEnemies().isEmpty());
        
        if (hasEnemies && !isPlayingHorror && normalMusicClip != null && normalMusicClip.isRunning()) {
            // Switch to horror music only if normal music is currently playing
            System.out.println("Enemies detected - switching to horror music");
            normalMusicClip.stop();
            normalMusicClip.setFramePosition(0); // Reset to beginning
            if (horrorMusicClip != null) {
                horrorMusicClip.setFramePosition(0); // Reset to beginning
                horrorMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                isPlayingHorror = true;
                System.out.println("Started horror music with looping");
            }
        } else if (!hasEnemies && isPlayingHorror && horrorMusicClip != null && horrorMusicClip.isRunning()) {
            // Switch to normal music only if horror music is currently playing
            System.out.println("No enemies - switching to normal music");
            horrorMusicClip.stop();
            horrorMusicClip.setFramePosition(0); // Reset to beginning
            if (normalMusicClip != null) {
                normalMusicClip.setFramePosition(0); // Reset to beginning
                normalMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                isPlayingHorror = false;
                System.out.println("Started normal music with looping");
            }
        }
        
        // Ensure music is looping if it should be playing
        if (musicEnabled) {
            if (isPlayingHorror && horrorMusicClip != null && !horrorMusicClip.isRunning()) {
                horrorMusicClip.setFramePosition(0); // Reset to beginning
                horrorMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                System.out.println("Restarted horror music loop");
            } else if (!isPlayingHorror && normalMusicClip != null && !normalMusicClip.isRunning()) {
                normalMusicClip.setFramePosition(0); // Reset to beginning
                normalMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                System.out.println("Restarted normal music loop");
            }
        }
    }
    
    /**
     * Set music enabled/disabled
     */
    public static void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        if (musicEnabled) {
            checkAndUpdateMusic(); // Start music if enabled
        } else {
            // Stop all music
            if (normalMusicClip != null && normalMusicClip.isRunning()) {
                normalMusicClip.stop();
            }
            if (horrorMusicClip != null && horrorMusicClip.isRunning()) {
                horrorMusicClip.stop();
            }
            isPlayingHorror = false;
        }
    }
    
    /**
     * Restart music if it stops unexpectedly
     */
    private static void restartMusicIfNeeded() {
        if (!musicEnabled) return;
        if (enemySystemStarting) return; // Skip during enemy system startup
        
        try {
            boolean hasEnemies = !allPets.isEmpty() && allPets.stream().anyMatch(pet -> !pet.getEnemies().isEmpty());
            
            if (hasEnemies) {
                // Should be playing horror music
                if (horrorMusicClip != null && !horrorMusicClip.isRunning()) {
                    horrorMusicClip.setFramePosition(0); // Reset to beginning
                    horrorMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                    isPlayingHorror = true;
                    System.out.println("Restarted horror music loop");
                }
            } else {
                // Should be playing normal music
                if (normalMusicClip != null && !normalMusicClip.isRunning()) {
                    normalMusicClip.setFramePosition(0); // Reset to beginning
                    normalMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                    isPlayingHorror = false;
                    System.out.println("Restarted normal music loop");
                }
            }
        } catch (Exception e) {
            System.out.println("Error restarting music: " + e.getMessage());
        }
    }
    
    /**
     * Set enemy system starting flag to prevent race conditions
     */
    public static void setEnemySystemStarting(boolean starting) {
        enemySystemStarting = starting;
        if (starting) {
            System.out.println("Music system: Enemy system starting - preventing music switches");
        } else {
            System.out.println("Music system: Enemy system startup complete - music switching re-enabled");
        }
    }
    
    /**
     * Immediately switch to horror music (for enemy system startup)
     */
    public static void switchToHorrorMusic() {
        if (!musicEnabled || horrorMusicClip == null) return;
        
        SwingUtilities.invokeLater(() -> {
            if (normalMusicClip != null && normalMusicClip.isRunning()) {
                normalMusicClip.stop();
                normalMusicClip.setFramePosition(0);
            }
            horrorMusicClip.setFramePosition(0);
            horrorMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            isPlayingHorror = true;
            System.out.println("Immediately switched to horror music");
        });
    }
    
    /**
     * Immediately switch to normal music (for enemy system shutdown)
     */
    public static void switchToNormalMusic() {
        if (!musicEnabled || normalMusicClip == null) return;
        
        SwingUtilities.invokeLater(() -> {
            if (horrorMusicClip != null && horrorMusicClip.isRunning()) {
                horrorMusicClip.stop();
                horrorMusicClip.setFramePosition(0);
            }
            normalMusicClip.setFramePosition(0);
            normalMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            isPlayingHorror = false;
            System.out.println("Immediately switched to normal music");
        });
    }
    
    /**
     * Update the pet list reference for enemy detection
     */
    public static void updatePetList(List<AdvancedDesktopPet> pets) {
        allPets = pets;
    }
    
    /**
     * Check if music is currently playing horror
     */
    public static boolean isPlayingHorror() {
        return isPlayingHorror;
    }
    
    /**
     * Check if music is enabled
     */
    public static boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    /**
     * Cleanup music resources
     */
    public static void cleanup() {
        if (musicCheckTimer != null) {
            musicCheckTimer.stop();
        }
        
        if (normalMusicClip != null) {
            normalMusicClip.stop();
            normalMusicClip.close();
        }
        
        if (horrorMusicClip != null) {
            horrorMusicClip.stop();
            horrorMusicClip.close();
        }
        
        System.out.println("Music system cleaned up");
    }
} 