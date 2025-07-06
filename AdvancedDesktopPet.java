import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Dictionary;
import javax.swing.UnsupportedLookAndFeelException;
import javax.sound.sampled.*;
import java.net.URL;

public class AdvancedDesktopPet extends JWindow implements MouseListener, MouseMotionListener {
    private static final int DEFAULT_WIDTH = 128;
    private static final int DEFAULT_HEIGHT = 128;
    private static final int ANIMATION_DELAY = 150;
    
    private JLabel petLabel;
    private Timer animationTimer;
    private Timer movementTimer;
    private Timer behaviorTimer;
    
    private ImageIcon idleGif;
    private ImageIcon walkGif;
    private List<ImageIcon> specialAnimations;
    
    // Mouse interaction
    private Point mouseOffset;
    private boolean isDragging = false;
    
    // Movement
    private int targetX, targetY;
    private Random random = new Random();
    private boolean isWalking = false;
    private boolean facingRight = true; // Track facing direction
    private int walkAnimationFrame = 0; // For leg animation sync
    
    // Behaviors
    private int currentBehavior = 1; // 0=idle, 1=walking, 2=special - Start in active mode
    
    // Safety timer to check if pet is lost
    private Timer safetyTimer;
    
    // Enemy system
    private boolean enemyEnabled = false;
    private List<EnemyWindow> enemies = new ArrayList<>();
    private Timer enemySpawnTimer;
    private Timer enemyCleanupTimer;
    private List<ImageIcon> enemyImages = new ArrayList<>();
    private Random enemyRandom = new Random();
    private int maxEnemies = 5; // Increased from 3 to 5
    
    // Tray
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    
    // Settings
    int petWidth = DEFAULT_WIDTH;
    int petHeight = DEFAULT_HEIGHT;
    float transparency = 1.0f; // 0.0 = invisible, 1.0 = opaque
    private static List<AdvancedDesktopPet> allPets = new ArrayList<>();
    private JFrame settingsWindow = null;
    private boolean allowCrossScreen = false; // Allow movement between screens
    private JWindow floatingShortcut = null; // Cyberpunk floating shortcut
    
    // Language support
    private boolean isChinese = false; // false = English, true = Chinese
    private Map<String, String> englishTexts = new HashMap<>();
    private Map<String, String> chineseTexts = new HashMap<>();
    
    // Music system
    private static Clip normalMusicClip;
    private static Clip horrorMusicClip;
    private static boolean musicEnabled = true;
    private static boolean isPlayingHorror = false;
    private static boolean wasPlayingHorrorBeforeDisable = false; // Track state before disable
    private static Timer musicCheckTimer;
    private static boolean musicInitialized = false; // Track if music system is already initialized
    
    // Loading screen
    private JWindow loadingWindow;
    private JLabel loadingLabel;
    private JProgressBar loadingProgress;
    private boolean isLoading = true;
    
    // Component references for easy updating
    private JLabel petCountLabel;
    private JLabel enemyInfoLabel;
    private JLabel maxEnemiesLabel;
    private JButton englishBtn;
    private JButton chineseBtn;
    private JButton duplicateBtn;
    private JButton removeBtn;
    private JButton hideBtn;
    private JButton showBtn;
    private JButton zoomInBtn;
    private JButton zoomOutBtn;
    private JButton testCrossScreenBtn;
    private JButton spawnEnemyBtn;
    private JButton clearEnemiesBtn;
    private JButton forceCleanupBtn;
    private JButton closeBtn;
    private JButton exitBtn;
    private JCheckBox crossScreenBox;
    private JCheckBox musicBox;
    private JCheckBox enemyBox;
    
    // Section header references for language updates
    private JLabel languageSectionLabel;
    private JLabel petManagementSectionLabel;
    private JLabel transparencySectionLabel;
    private JLabel sizeSectionLabel;
    private JLabel movementSectionLabel;
    private JLabel horrorSectionLabel;
    
    public AdvancedDesktopPet() {
        showLoadingScreen();
        initializeLanguages();
        allPets.add(this); // Register this pet
        // Load resources asynchronously
        SwingUtilities.invokeLater(() -> {
            loadResourcesAsync();
        });
    }
    
    private void showLoadingScreen() {
        loadingWindow = new JWindow();
        loadingWindow.setAlwaysOnTop(true);
        loadingWindow.setSize(400, 200);
        loadingWindow.setLocationRelativeTo(null);
        loadingWindow.setBackground(new Color(0, 0, 0, 0));
        
        JPanel loadingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw cyberpunk background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(20, 20, 40), getWidth(), getHeight(), new Color(40, 20, 60));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw neon border
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(0, 255, 255));
                g2d.drawRect(1, 1, getWidth()-2, getHeight()-2);
                
                g2d.dispose();
            }
        };
        loadingPanel.setLayout(new BorderLayout());
        loadingPanel.setOpaque(false);
        
        // Loading text
        loadingLabel = new JLabel("Loading Ayano...", JLabel.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.BOLD, 18));
        loadingLabel.setForeground(new Color(0, 255, 255));
        loadingLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // Progress bar
        loadingProgress = new JProgressBar(0, 100);
        loadingProgress.setValue(0);
        loadingProgress.setStringPainted(true);
        loadingProgress.setString("Initializing...");
        loadingProgress.setForeground(new Color(0, 255, 255));
        loadingProgress.setBackground(new Color(40, 40, 60));
        loadingProgress.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
        loadingPanel.add(loadingProgress, BorderLayout.SOUTH);
        
        loadingWindow.setContentPane(loadingPanel);
        loadingWindow.setVisible(true);
    }
    
    private void loadResourcesAsync() {
        new Thread(() -> {
            try {
                // Step 1: Initialize languages (10%)
                updateLoadingProgress(10, "Loading languages...");
                Thread.sleep(100);
                
                // Step 2: Initialize pet window (20%)
                updateLoadingProgress(20, "Initializing pet window...");
                SwingUtilities.invokeAndWait(() -> initializePet());
                Thread.sleep(100);
                
                // Step 3: Load animations (40%)
                updateLoadingProgress(40, "Loading animations...");
                SwingUtilities.invokeAndWait(() -> loadAnimations());
                Thread.sleep(100);
                
                // Step 4: Initialize music (60%)
                updateLoadingProgress(60, "Initializing music system...");
                SwingUtilities.invokeAndWait(() -> initializeMusic());
                Thread.sleep(100);
                
                // Step 5: Setup window and tray (80%)
                updateLoadingProgress(80, "Setting up system tray...");
                SwingUtilities.invokeAndWait(() -> {
                    setupWindow();
                    setupTrayIcon();
                });
                Thread.sleep(100);
                
                // Step 6: Create floating shortcut (90%)
                updateLoadingProgress(90, "Creating floating shortcut...");
                SwingUtilities.invokeAndWait(() -> createFloatingShortcut());
                Thread.sleep(100);
                
                // Step 7: Start timers (100%)
                updateLoadingProgress(100, "Starting timers...");
                SwingUtilities.invokeAndWait(() -> startTimers());
                Thread.sleep(200);
                
                // Hide loading screen and show pet
                SwingUtilities.invokeLater(() -> {
                    hideLoadingScreen();
                    isLoading = false;
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    hideLoadingScreen();
                    JOptionPane.showMessageDialog(null, "Error loading Ayano: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void updateLoadingProgress(int progress, String message) {
        SwingUtilities.invokeLater(() -> {
            if (loadingProgress != null) {
                loadingProgress.setValue(progress);
                loadingProgress.setString(message);
            }
        });
    }
    
    private void hideLoadingScreen() {
        if (loadingWindow != null) {
            loadingWindow.setVisible(false);
            loadingWindow.dispose();
            loadingWindow = null;
        }
    }
    
    private void initializeLanguages() {
        // Initialize English texts
        englishTexts.put("settings_title", "Desktop Ayano Settings");
        englishTexts.put("pet_management", "Ayano Management");
        englishTexts.put("duplicate_pet", "Duplicate Ayano");
        englishTexts.put("remove_pet", "Remove Ayano");
        englishTexts.put("active_pets", "Active Ayanos");
        englishTexts.put("transparency", "Transparency");
        englishTexts.put("hide_pet", "Hide Ayano");
        englishTexts.put("show_all_pets", "Show All Ayanos");
        englishTexts.put("size", "Size");
        englishTexts.put("zoom_in", "Zoom In");
        englishTexts.put("zoom_out", "Zoom Out");
        englishTexts.put("movement_settings", "Movement Settings");
        englishTexts.put("allow_cross_screen", "Allow Cross-Screen Movement");
        englishTexts.put("test_cross_screen", "Test Cross-Screen");
        englishTexts.put("music_enabled", "Music Enabled");
        englishTexts.put("horror_mode", "Horror Mode");
        englishTexts.put("enable_enemies", "Enable Enemies");
        englishTexts.put("spawn_enemy_now", "Spawn Enemy Now");
        englishTexts.put("enemies", "Enemies");
        englishTexts.put("max_enemies", "Max Enemies");
        englishTexts.put("clear_all_enemies", "Clear All Enemies");
        englishTexts.put("force_cleanup", "Force Cleanup");
        englishTexts.put("close", "Close");
        englishTexts.put("language", "Language");
        englishTexts.put("english", "English");
        englishTexts.put("chinese", "Chinese");
        englishTexts.put("minimize", "Minimize");
        englishTexts.put("close_window", "Close");
        englishTexts.put("exit_program", "Exit Program");
        englishTexts.put("confirm_exit", "Are you sure you want to exit the program?");
        
        // Initialize Chinese texts
        chineseTexts.put("settings_title", "\u684c\u9762\u963f\u591c\u8bbe\u7f6e");
        chineseTexts.put("pet_management", "\u963f\u591c\u7ba1\u7406");
        chineseTexts.put("duplicate_pet", "\u590d\u5236\u963f\u591c");
        chineseTexts.put("remove_pet", "\u5220\u9664\u963f\u591c");
        chineseTexts.put("active_pets", "\u6d3b\u8dc3\u7684\u963f\u591c");
        chineseTexts.put("transparency", "\u900f\u660e\u5ea6");
        chineseTexts.put("hide_pet", "\u9690\u85cf\u963f\u591c");
        chineseTexts.put("show_all_pets", "\u663e\u793a\u6240\u6709\u963f\u591c");
        chineseTexts.put("size", "\u5927\u5c0f");
        chineseTexts.put("zoom_in", "\u653e\u5927");
        chineseTexts.put("zoom_out", "\u7f29\u5c0f");
        chineseTexts.put("movement_settings", "\u79fb\u52a8\u8bbe\u7f6e");
        chineseTexts.put("allow_cross_screen", "\u5141\u8bb8\u8de8\u5c4f\u5e55\u79fb\u52a8");
        chineseTexts.put("test_cross_screen", "\u6d4b\u8bd5\u8de8\u5c4f\u5e55");
        chineseTexts.put("music_enabled", "\u97f3\u4e50\u5f00\u542f");
        chineseTexts.put("horror_mode", "\u6050\u6016\u6a21\u5f0f");
        chineseTexts.put("enable_enemies", "\u542f\u7528\u654c\u4eba");
        chineseTexts.put("spawn_enemy_now", "\u7acb\u5373\u751f\u6210\u654c\u4eba");
        chineseTexts.put("enemies", "\u654c\u4eba");
        chineseTexts.put("max_enemies", "\u6700\u5927\u654c\u4eba\u6570\u91cf");
        chineseTexts.put("clear_all_enemies", "\u6e05\u9664\u6240\u6709\u654c\u4eba");
        chineseTexts.put("force_cleanup", "\u5f3a\u5236\u6e05\u7406");
        chineseTexts.put("close", "\u5173\u95ed");
        chineseTexts.put("language", "\u8bed\u8a00");
        chineseTexts.put("english", "\u82f1\u8bed");
        chineseTexts.put("chinese", "\u4e2d\u6587");
        chineseTexts.put("minimize", "\u6700\u5c0f\u5316");
        chineseTexts.put("close_window", "\u5173\u95ed\u7a97\u53e3");
        chineseTexts.put("exit_program", "\u9000\u51fa\u7a0b\u5e8f");
        chineseTexts.put("confirm_exit", "\u60a8\u786e\u5b9a\u8981\u9000\u51fa\u7a0b\u5e8f\u5417\uff1f");
    }
    
    private String getText(String key) {
        Map<String, String> currentTexts = isChinese ? chineseTexts : englishTexts;
        String result = currentTexts.getOrDefault(key, key);
        // Remove debug output to reduce console spam
        return result;
    }
    
    private void refreshSettingsWindow() {
        System.out.println("refreshSettingsWindow called. isChinese: " + isChinese);
        if (settingsWindow != null && settingsWindow.isVisible()) {
            System.out.println("Updating existing settings window components");
            
            // Update all text labels
            updateSettingsWindowTexts();
            
            // Update button states
            if (englishBtn != null) {
                englishBtn.setText(getText("english"));
                englishBtn.setEnabled(isChinese);
                englishBtn.setBackground(isChinese ? new Color(70, 70, 90) : new Color(100, 100, 120));
            }
            if (chineseBtn != null) {
                chineseBtn.setText(getText("chinese"));
                chineseBtn.setEnabled(!isChinese);
                chineseBtn.setBackground(!isChinese ? new Color(70, 70, 90) : new Color(100, 100, 120));
            }
            settingsWindow.revalidate();
            settingsWindow.repaint();
            System.out.println("Settings window updated successfully");
        }
    }
    
    private void updateSettingsWindowTexts() {
        if (settingsWindow == null) return;
        
        // Update window title
        settingsWindow.setTitle(getText("settings_title"));
        
        // Update all stored component references
        if (petCountLabel != null) {
            petCountLabel.setText(getText("active_pets") + ": " + allPets.size());
        }
        if (enemyInfoLabel != null) {
            enemyInfoLabel.setText(getText("enemies") + ": " + enemies.size() + " / " + maxEnemies + " active");
        }
        if (maxEnemiesLabel != null) {
            maxEnemiesLabel.setText(getText("max_enemies") + ": " + maxEnemies);
        }
        if (englishBtn != null) {
            englishBtn.setText(getText("english"));
            englishBtn.setEnabled(false);
            englishBtn.setBackground(new Color(100, 100, 120));
        }
        if (chineseBtn != null) {
            chineseBtn.setText(getText("chinese"));
            chineseBtn.setEnabled(true);
            chineseBtn.setBackground(!isChinese ? new Color(70, 70, 90) : new Color(100, 100, 120));
        }
        if (duplicateBtn != null) {
            duplicateBtn.setText(getText("duplicate_pet"));
        }
        if (removeBtn != null) {
            removeBtn.setText(getText("remove_pet"));
        }
        if (hideBtn != null) {
            hideBtn.setText(getText("hide_pet"));
        }
        if (showBtn != null) {
            showBtn.setText(getText("show_all_pets"));
        }
        if (zoomInBtn != null) {
            zoomInBtn.setText(getText("zoom_in"));
        }
        if (zoomOutBtn != null) {
            zoomOutBtn.setText(getText("zoom_out"));
        }
        if (testCrossScreenBtn != null) {
            testCrossScreenBtn.setText(getText("test_cross_screen"));
        }
        if (spawnEnemyBtn != null) {
            spawnEnemyBtn.setText(getText("spawn_enemy_now"));
        }
        if (clearEnemiesBtn != null) {
            clearEnemiesBtn.setText(getText("clear_all_enemies"));
        }
        if (forceCleanupBtn != null) {
            forceCleanupBtn.setText(getText("force_cleanup"));
        }
        if (closeBtn != null) {
            closeBtn.setText(getText("close"));
        }
        if (exitBtn != null) {
            exitBtn.setText(getText("exit_program"));
        }
        if (crossScreenBox != null) {
            crossScreenBox.setText(getText("allow_cross_screen"));
        }
        if (musicBox != null) {
            musicBox.setText(getText("music_enabled"));
        }
        if (enemyBox != null) {
            enemyBox.setText(getText("enable_enemies"));
        }
        
        // Update section headers
        if (languageSectionLabel != null) {
            languageSectionLabel.setText(getText("language"));
        }
        if (petManagementSectionLabel != null) {
            petManagementSectionLabel.setText(getText("pet_management"));
        }
        if (transparencySectionLabel != null) {
            transparencySectionLabel.setText(getText("transparency"));
        }
        if (sizeSectionLabel != null) {
            sizeSectionLabel.setText(getText("size"));
        }
        if (movementSectionLabel != null) {
            movementSectionLabel.setText(getText("movement_settings"));
        }
        if (horrorSectionLabel != null) {
            horrorSectionLabel.setText(getText("horror_mode"));
        }
    }
    
    private void initializePet() {
        setAlwaysOnTop(true);
        setSize(petWidth, petHeight);
        // JWindow is already undecorated by default
        setBackground(new Color(0, 0, 0, 0));
        setLayout(null); // Use absolute positioning for precise control
        
        // Position on primary screen safely
        Rectangle screenBounds = getPrimaryScreenBounds();
        int centerX = screenBounds.x + (screenBounds.width - petWidth) / 2;
        int centerY = screenBounds.y + (screenBounds.height - petHeight) / 2;
        setLocation(centerX, centerY);
        
        petLabel = new JLabel();
        petLabel.setHorizontalAlignment(JLabel.CENTER);
        petLabel.setVerticalAlignment(JLabel.CENTER);
        petLabel.setVisible(true);
        petLabel.setOpaque(false);
        petLabel.setBounds(0, 0, petWidth, petHeight);
        add(petLabel);
        
        addMouseListener(this);
        addMouseMotionListener(this);
        petLabel.addMouseListener(this);
        petLabel.addMouseMotionListener(this);
        
        // Add key listener for settings shortcut
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    createSettingsWindow();
                }
            }
        });
        setFocusable(true); // Allow key events
        
        setVisible(true);
    }
    
    private Rectangle getPrimaryScreenBounds() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice primaryDevice = ge.getDefaultScreenDevice();
        return primaryDevice.getDefaultConfiguration().getBounds();
    }
    
    private void loadAnimations() {
        specialAnimations = new ArrayList<>();
        
        // Try to load PNG files from Image folder first
        idleGif = loadImageSafely("Image/chibi01.png");
        walkGif = loadImageSafely("Image/chibi02.png");
        
        // Load the third image as a special animation
        ImageIcon chibi3 = loadImageSafely("Image/chibi03.png");
        if (chibi3 != null) {
            specialAnimations.add(chibi3);
        }
        
        // Try to load GIF files from resources or current directory as fallback
        if (idleGif == null) {
            idleGif = loadImageSafely("idle.gif");
        }
        if (walkGif == null) {
            walkGif = loadImageSafely("walk.gif");
        }
        
        // Load additional special animations
        for (int i = 1; i <= 5; i++) {
            ImageIcon special = loadImageSafely("special" + i + ".gif");
            if (special != null) {
                specialAnimations.add(special);
            }
        }
        
        // If no images found, create default animations
        if (idleGif == null) {
            idleGif = createDefaultAnimation(new Color(255, 150, 150, 200), ":)");
        }
        if (walkGif == null) {
            walkGif = createDefaultAnimation(new Color(150, 255, 150, 200), ">>>");
        }
        
        // Add default special animations if none loaded
        if (specialAnimations.isEmpty()) {
            specialAnimations.add(createDefaultAnimation(new Color(255, 255, 150, 200), "<3"));
            specialAnimations.add(createDefaultAnimation(new Color(150, 150, 255, 200), "*"));
            specialAnimations.add(createDefaultAnimation(new Color(255, 150, 255, 200), "~"));
        }
        
        // Load enemy images
        loadEnemyImages();
        
        // Set initial animation
        SwingUtilities.invokeLater(() -> updateIdleSprite());
    }
    

    
    private void initializeMusic() {
        // Only initialize music system once for all instances
        if (musicInitialized) {
            return;
        }
        
        try {
            // Check if music files exist
            File normalMusicFile = new File("music/normal.wav");
            File horrorMusicFile = new File("music/horror.wav");
            
            if (!normalMusicFile.exists()) {
                System.out.println("Normal music file not found: music/normal.wav");
                return;
            }
            
            if (!horrorMusicFile.exists()) {
                System.out.println("Horror music file not found: music/horror.wav");
                return;
            }
            
            // Load normal music using Java's native WAV support
            try {
                AudioInputStream normalStream = AudioSystem.getAudioInputStream(normalMusicFile);
                normalMusicClip = AudioSystem.getClip();
                normalMusicClip.open(normalStream);
                normalMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                System.out.println("Normal music loaded successfully");
            } catch (Exception e) {
                System.out.println("Error loading normal music: " + e.getMessage());
            }
            
            // Load horror music using Java's native WAV support
            try {
                AudioInputStream horrorStream = AudioSystem.getAudioInputStream(horrorMusicFile);
                horrorMusicClip = AudioSystem.getClip();
                horrorMusicClip.open(horrorStream);
                horrorMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                System.out.println("Horror music loaded successfully");
            } catch (Exception e) {
                System.out.println("Error loading horror music: " + e.getMessage());
            }
            
            // Start music check timer
            musicCheckTimer = new Timer(1000, e -> checkAndUpdateMusic());
            musicCheckTimer.start();
            
            // Start with normal music if enabled (and ensure horror is stopped)
            if (musicEnabled && normalMusicClip != null) {
                // Make sure horror music is stopped
                if (horrorMusicClip != null && horrorMusicClip.isRunning()) {
                    horrorMusicClip.stop();
                }
                normalMusicClip.start();
                isPlayingHorror = false;
                System.out.println("Started normal music playback");
            }
            
            musicInitialized = true;
            
        } catch (Exception e) {
            System.out.println("Error initializing music: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
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
        
        // Music was just re-enabled - restart appropriate music
        if (normalMusicClip != null && !normalMusicClip.isRunning() && 
            horrorMusicClip != null && !horrorMusicClip.isRunning()) {
            
            boolean hasEnemies = !allPets.isEmpty() && allPets.stream().anyMatch(pet -> !pet.enemies.isEmpty());
            
            if (hasEnemies || wasPlayingHorrorBeforeDisable) {
                // Resume horror music if enemies are present or was playing before
                horrorMusicClip.start();
                isPlayingHorror = true;
                wasPlayingHorrorBeforeDisable = false;
                System.out.println("Music re-enabled - started horror music");
            } else {
                // Resume normal music
                normalMusicClip.start();
                isPlayingHorror = false;
                wasPlayingHorrorBeforeDisable = false;
                System.out.println("Music re-enabled - started normal music");
            }
            return;
        }
        
        boolean hasEnemies = !allPets.isEmpty() && allPets.stream().anyMatch(pet -> !pet.enemies.isEmpty());
        
        if (hasEnemies && !isPlayingHorror) {
            // Switch to horror music
            System.out.println("Enemies detected - switching to horror music");
            if (normalMusicClip != null && normalMusicClip.isRunning()) {
                normalMusicClip.stop();
                System.out.println("Stopped normal music");
            }
            if (horrorMusicClip != null && !horrorMusicClip.isRunning()) {
                horrorMusicClip.start();
                isPlayingHorror = true;
                System.out.println("Started horror music");
            }
        } else if (!hasEnemies && isPlayingHorror) {
            // Switch to normal music
            System.out.println("No enemies - switching to normal music");
            if (horrorMusicClip != null && horrorMusicClip.isRunning()) {
                horrorMusicClip.stop();
                System.out.println("Stopped horror music");
            }
            if (normalMusicClip != null && !normalMusicClip.isRunning()) {
                normalMusicClip.start();
                isPlayingHorror = false;
                System.out.println("Started normal music");
            }
        }
    }
    
    private static void setMusicEnabled(boolean enabled) {
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
    
    private void loadEnemyImages() {
        enemyImages.clear();
        
        // Try to load enemy images from Image folder - using the specific chibi enemy names
        String[] enemyFiles = {"Image\\\u654c\u4ebachibi01.png", "Image\\\u654c\u4ebachibi02.png", "Image\\\u654c\u4ebachibi03.png"};
        
        for (String filename : enemyFiles) {
            ImageIcon enemyImage = loadEnemyImageSafely(filename);
            if (enemyImage != null) {
                enemyImages.add(enemyImage);
            }
        }
        
        // Also try alternative naming patterns
        if (enemyImages.isEmpty()) {
            for (int i = 1; i <= 3; i++) {
                String filename = "Image\\enemy0" + i + ".png";
                ImageIcon enemyImage = loadEnemyImageSafely(filename);
                if (enemyImage != null) {
                    enemyImages.add(enemyImage);
                }
            }
        }
        
        // If no enemy images found, create default scary enemies
        if (enemyImages.isEmpty()) {
            enemyImages.add(createDefaultEnemyAnimation(new Color(255, 0, 0, 200), "X"));
            enemyImages.add(createDefaultEnemyAnimation(new Color(0, 0, 0, 200), "!"));
            enemyImages.add(createDefaultEnemyAnimation(new Color(128, 0, 128, 200), "?"));
        }
    }
    
    private ImageIcon loadEnemyImageSafely(String filename) {
        try {
            // First try to load from resources
            java.net.URL resource = getClass().getResource("/" + filename);
            ImageIcon icon = null;
            
            if (resource != null) {
                icon = new ImageIcon(resource);
            } else {
                // Try to load from current directory
                File file = new File(filename);
                if (file.exists()) {
                    icon = new ImageIcon(filename);
                } else {
                    // Try alternative encoding/path approaches
                    String[] alternatives = {
                        filename.replace("\u654c\u4eba", "enemy"),
                        filename.replace("/", "\\"),
                        "Image\\" + filename.substring(Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\")) + 1),
                        filename
                    };
                    
                    for (String alt : alternatives) {
                        File altFile = new File(alt);
                        if (altFile.exists()) {
                            icon = new ImageIcon(alt);
                            break;
                        }
                    }
                }
            }
            
            // Scale the enemy image
            if (icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }
            
        } catch (Exception e) {
            // Silently fail for faster loading
        }
        return null;
    }
    
    private ImageIcon createDefaultEnemyAnimation(Color color, String emoji) {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw scary circle background
        g2d.setColor(color);
        g2d.fillOval(10, 10, 80, 80);
        
        // Add darker border for scary effect
        g2d.setColor(color.darker());
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(10, 10, 80, 80);
        
        // Draw scary emoji
        Font font = new Font("Segoe UI Emoji", Font.PLAIN, 40);
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);
        
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (100 - fm.stringWidth(emoji)) / 2;
        int textY = (100 + fm.getAscent()) / 2;
        g2d.drawString(emoji, textX, textY);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    private ImageIcon loadImageSafely(String filename) {
        try {
            // First try to load from resources
            java.net.URL resource = getClass().getResource("/" + filename);
            ImageIcon icon = null;
            
            if (resource != null) {
                icon = new ImageIcon(resource);
            } else {
                // Try to load from current directory
                File file = new File(filename);
                if (file.exists()) {
                    icon = new ImageIcon(filename);
                } else {
                    // Try from images subfolder
                    file = new File("images/" + filename);
                    if (file.exists()) {
                        icon = new ImageIcon("images/" + filename);
                    }
                }
            }
            
            // Always scale the image to fit the pet window if loaded successfully
            if (icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                Image img = icon.getImage();
                
                // Use SCALE_SMOOTH for better quality scaling
                Image scaledImg = img.getScaledInstance(petWidth, petHeight, Image.SCALE_SMOOTH);
                
                // Create a new ImageIcon with the scaled image
                return new ImageIcon(scaledImg);
            }
            
        } catch (Exception e) {
            // Silently fail for faster loading
            System.out.println("Error loading image " + filename + ": " + e.getMessage());
        }
        return null;
    }
    
    private ImageIcon createDefaultAnimation(Color color, String emoji) {
        BufferedImage image = new BufferedImage(petWidth, petHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw circle background
        g2d.setColor(color);
        int size = Math.min(petWidth, petHeight) - 20;
        int x = (petWidth - size) / 2;
        int y = (petHeight - size) / 2;
        g2d.fillOval(x, y, size, size);
        
        // Draw emoji
        Font font = new Font("Segoe UI Emoji", Font.PLAIN, size / 3);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);
        
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (petWidth - fm.stringWidth(emoji)) / 2;
        int textY = (petHeight + fm.getAscent()) / 2;
        g2d.drawString(emoji, textX, textY);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    private void setupWindow() {
        // JWindow doesn't have setDefaultCloseOperation, handle with window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }
    
    private void setupTrayIcon() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported on this system.");
            System.out.println("Use middle-click on the pet to access settings.");
            return;
        }
        
        systemTray = SystemTray.getSystemTray();
        BufferedImage trayImage = createTrayIcon();
        trayIcon = new TrayIcon(trayImage, "Advanced Desktop Pet");
        trayIcon.setImageAutoSize(true);
        
        PopupMenu popup = new PopupMenu();
        
        // Show/Hide
        MenuItem showItem = new MenuItem("Show Pet");
        showItem.addActionListener(e -> setVisible(true));
        popup.add(showItem);
        
        MenuItem hideItem = new MenuItem("Hide Pet");
        hideItem.addActionListener(e -> setVisible(false));
        popup.add(hideItem);
        
        popup.addSeparator();
        
        // Behaviors
        Menu behaviorMenu = new Menu("Behaviors");
        
        MenuItem idleItem = new MenuItem("Idle Mode");
        idleItem.addActionListener(e -> setBehavior(0));
        behaviorMenu.add(idleItem);
        
        MenuItem activeItem = new MenuItem("Active Mode");
        activeItem.addActionListener(e -> setBehavior(1));
        behaviorMenu.add(activeItem);
        
        MenuItem specialItem = new MenuItem("Special Animation");
        specialItem.addActionListener(e -> playSpecialAnimation());
        behaviorMenu.add(specialItem);
        
        popup.add(behaviorMenu);
        
        // Settings
        MenuItem settingsItem = new MenuItem("Settings");
        settingsItem.addActionListener(e -> createSettingsWindow());
        popup.add(settingsItem);
        
        popup.addSeparator();
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());
        popup.add(exitItem);
        
        trayIcon.setPopupMenu(popup);
        trayIcon.addActionListener(e -> setVisible(!isVisible()));
        
        try {
            systemTray.add(trayIcon);
            System.out.println("Tray icon added successfully!");
            System.out.println("Look for a blue circle icon in your system tray.");
        } catch (AWTException e) {
            System.out.println("Tray icon error: " + e.getMessage());
            System.out.println("Use middle-click on the pet to access settings.");
        }
    }
    
    private BufferedImage createTrayIcon() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(new Color(100, 150, 255));
        g2d.fillOval(2, 2, 12, 12);
        
        g2d.setColor(Color.WHITE);
        g2d.fillOval(4, 5, 2, 2);
        g2d.fillOval(10, 5, 2, 2);
        
        g2d.setStroke(new BasicStroke(1));
        g2d.drawArc(6, 8, 4, 3, 0, -180);
        
        g2d.dispose();
        return image;
    }
    
    private void startTimers() {
        // Animation timer
        animationTimer = new Timer(ANIMATION_DELAY, e -> updateAnimation());
        animationTimer.start();
        
        // Movement timer
        movementTimer = new Timer(3000 + random.nextInt(5000), e -> {
            if (!isDragging && currentBehavior == 1) {
                startRandomWalk();
            }
            movementTimer.setDelay(3000 + random.nextInt(5000));
        });
        movementTimer.start();
        
        // Behavior timer for random special animations
        behaviorTimer = new Timer(15000 + random.nextInt(20000), e -> {
            if (!isDragging && random.nextInt(3) == 0) {
                playSpecialAnimation();
            }
            behaviorTimer.setDelay(15000 + random.nextInt(20000));
        });
        behaviorTimer.start();
        
        // Safety timer to check if pet is lost or stuck
        safetyTimer = new Timer(12000, e -> {
            checkAndFixPetLocation();
        });
        safetyTimer.start();
        
        // Start enemy system if enabled
        if (enemyEnabled) {
            startEnemySystem();
        }
    }
    
    private void startEnemySystem() {
        if (enemySpawnTimer != null) {
            enemySpawnTimer.stop();
        }
        if (enemyCleanupTimer != null) {
            enemyCleanupTimer.stop();
        }
        
        // Enemy spawn timer - spawn enemies at random intervals
        enemySpawnTimer = new Timer(10000 + enemyRandom.nextInt(20000), e -> {
            if (enemyEnabled && enemies.size() < maxEnemies) {
                spawnEnemy();
            }
            // Randomize next spawn time
            enemySpawnTimer.setDelay(8000 + enemyRandom.nextInt(15000));
        });
        enemySpawnTimer.start();
        
        // Enemy cleanup timer - clean up stuck enemies every 15 seconds (more frequent)
        enemyCleanupTimer = new Timer(15000, e -> {
            if (enemyEnabled) {
                cleanupStuckEnemies();
            }
        });
        enemyCleanupTimer.start();
        
        // Spawn initial enemy after a delay
        Timer initialSpawnTimer = new Timer(3000, e -> {
            if (enemyEnabled) {
                spawnEnemy();
            }
            ((Timer) e.getSource()).stop();
        });
        initialSpawnTimer.start();
    }
    
    private void spawnEnemy() {
        if (enemies.size() >= maxEnemies || enemyImages.isEmpty()) {
            System.out.println("Cannot spawn enemy: " + enemies.size() + "/" + maxEnemies + " enemies, " + 
                             (enemyImages.isEmpty() ? "no images" : "images available"));
            return;
        }
        
        try {
        System.out.println("Spawning enemy... Current enemies: " + enemies.size());
        
        EnemyWindow enemy = new EnemyWindow(this, enemyImages);
        enemies.add(enemy);
        
        // Remove enemy after some time (20-60 seconds)
        Timer despawnTimer = new Timer(20000 + enemyRandom.nextInt(40000), e -> {
                try {
            if (enemies.contains(enemy)) {
                        System.out.println("Despawning enemy...");
                enemy.stopEnemy();
                enemies.remove(enemy);
                System.out.println("Enemy despawned. Remaining enemies: " + enemies.size());
            }
                } catch (Exception ex) {
                    System.out.println("Error during enemy despawn: " + ex.getMessage());
                } finally {
            ((Timer) e.getSource()).stop();
                }
        });
        despawnTimer.start();
            
            System.out.println("Enemy spawned successfully. Total enemies: " + enemies.size());
        } catch (Exception e) {
            System.out.println("Error spawning enemy: " + e.getMessage());
        }
    }
    
    // Method to update enemy info in settings window
    private void updateEnemyInfo() {
        if (settingsWindow != null && settingsWindow.isVisible()) {
            // This will be called to refresh enemy count display
            SwingUtilities.invokeLater(() -> {
                // The labels will be updated when the settings window is refreshed
            });
        }
    }
    
    // Clean up stuck or invalid enemies
    private void cleanupStuckEnemies() {
        if (enemies.isEmpty()) return;
        
        System.out.println("Checking for stuck enemies... Current count: " + enemies.size());
        
        List<EnemyWindow> enemiesToRemove = new ArrayList<>();
        
        for (EnemyWindow enemy : enemies) {
            try {
                // Check if enemy window is still valid
                boolean shouldRemove = false;
                
                // Check if enemy is null
                if (enemy == null) {
                    System.out.println("Found null enemy, marking for removal");
                    shouldRemove = true;
                }
                // Check if enemy is not visible (stuck)
                else if (!enemy.isVisible()) {
                    System.out.println("Found invisible enemy, marking for removal");
                    shouldRemove = true;
                }
                // Check if enemy is not displayable (disposed)
                else if (!enemy.isDisplayable()) {
                    System.out.println("Found disposed enemy, marking for removal");
                    shouldRemove = true;
                }
                // Check if enemy has invalid location (way off screen)
                else if (enemy.getLocation().x < -10000 || enemy.getLocation().y < -10000) {
                    System.out.println("Found enemy with invalid location, marking for removal");
                    shouldRemove = true;
                }
                // Check if enemy timers are null (indicating it's broken)
                else if (enemy.hasNullTimers()) {
                    System.out.println("Found enemy with null timers, marking for removal");
                    shouldRemove = true;
                }
                
                if (shouldRemove) {
                    enemiesToRemove.add(enemy);
                }
                
            } catch (Exception e) {
                System.out.println("Error checking enemy status, marking for removal: " + e.getMessage());
                enemiesToRemove.add(enemy);
            }
        }
        
        // Remove stuck enemies
        for (EnemyWindow enemy : enemiesToRemove) {
            try {
                System.out.println("Removing stuck enemy: " + (enemy != null ? enemy.hashCode() : "null"));
                
                // Stop all timers first
                if (enemy != null) {
                    enemy.stopAllTimers();
                    
                    // Force dispose
                    enemy.setVisible(false);
                    enemy.dispose();
                }
                
                enemies.remove(enemy);
                System.out.println("Removed stuck enemy");
                
            } catch (Exception e) {
                System.out.println("Error removing stuck enemy: " + e.getMessage());
                // Force remove from list even if dispose fails
                enemies.remove(enemy);
            }
        }
        
        if (!enemiesToRemove.isEmpty()) {
            System.out.println("Cleaned up " + enemiesToRemove.size() + " stuck enemies. Remaining: " + enemies.size());
        }
    }
    
    // Force remove all enemies (emergency cleanup)
    public void forceRemoveAllEnemies() {
        System.out.println("Force removing all enemies...");
        
        // Stop the enemy system first
        stopEnemySystem();
        
        // Force dispose all enemy windows on EDT
        SwingUtilities.invokeLater(() -> {
            System.out.println("Force disposing all enemy windows...");
            
            // Create a copy of the list to avoid concurrent modification
            List<EnemyWindow> enemiesToForceRemove = new ArrayList<>(enemies);
            
            for (EnemyWindow enemy : enemiesToForceRemove) {
                try {
                    System.out.println("Force disposing enemy: " + enemy.hashCode());
                    
                    // Stop all timers first
                    enemy.stopAllTimers();
                    
                    // Force dispose the window
                    enemy.setVisible(false);
                    enemy.dispose();
                    
                    // Remove from list
                    enemies.remove(enemy);
                    
                } catch (Exception e) {
                    System.out.println("Error force disposing enemy: " + e.getMessage());
                }
            }
            
            // Clear the list completely
            enemies.clear();
            
            // Run garbage collection to clean up any remaining references
            System.gc();
            
            System.out.println("Force cleanup completed. Enemies remaining: " + enemies.size());
        });
    }
    
    private void stopEnemySystem() {
        System.out.println("Stopping enemy system... Current enemies: " + enemies.size());
        
        // Stop timers first
        if (enemySpawnTimer != null) {
            enemySpawnTimer.stop();
            enemySpawnTimer = null;
        }
        
        if (enemyCleanupTimer != null) {
            enemyCleanupTimer.stop();
            enemyCleanupTimer = null;
        }
        
        // Stop and remove all enemies
        List<EnemyWindow> enemiesToRemove = new ArrayList<>(enemies);
        for (EnemyWindow enemy : enemiesToRemove) {
            try {
                System.out.println("Stopping enemy: " + enemy.hashCode());
            enemy.stopEnemy();
            } catch (Exception e) {
                System.out.println("Error stopping enemy: " + e.getMessage());
                // Try to force dispose if normal stop fails
                try {
                    enemy.stopAllTimers();
                    enemy.setVisible(false);
                    enemy.dispose();
                } catch (Exception ex) {
                    System.out.println("Error force disposing enemy: " + ex.getMessage());
                }
            }
        }
        
        // Clear the list
        enemies.clear();
        System.out.println("Enemy system stopped. All enemies removed.");
    }
    
    private void toggleEnemySystem(boolean enabled) {
        enemyEnabled = enabled;
        
        if (enabled) {
            System.out.println("Enemy system enabled! Enemies will start spawning...");
            startEnemySystem();
            createScreenFlashEffect(); // Horror effect when enabled
        } else {
            System.out.println("Enemy system disabled. Stopping all enemies...");
            stopEnemySystem();
        }
    }
    
    private void createScreenFlashEffect() {
        // Create a full-screen flash effect for horror
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        
        for (GraphicsDevice device : devices) {
            Rectangle screenBounds = device.getDefaultConfiguration().getBounds();
            
            JWindow flashWindow = new JWindow();
            flashWindow.setAlwaysOnTop(true);
            flashWindow.setBounds(screenBounds);
            flashWindow.setBackground(new Color(255, 0, 0, 100)); // Red flash
            flashWindow.setVisible(true);
            
            // Flash effect timer
            Timer flashTimer = new Timer(200, new ActionListener() {
                int flashCount = 0;
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    flashCount++;
                    flashWindow.setVisible(flashCount % 2 == 0);
                    
                    if (flashCount >= 6) {
                        flashWindow.dispose();
                        ((Timer) e.getSource()).stop();
                    }
                }
            });
            flashTimer.start();
        }
    }
    
    public void createHorrorShake() {
        // Make the pet shake when enemies are near
        Point originalLocation = getLocation();
        Timer shakeTimer = new Timer(50, new ActionListener() {
            int shakeCount = 0;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                shakeCount++;
                
                if (shakeCount <= 10) {
                    int shakeX = random.nextInt(6) - 3;
                    int shakeY = random.nextInt(6) - 3;
                    setLocation(originalLocation.x + shakeX, originalLocation.y + shakeY);
                } else {
                    setLocation(originalLocation);
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        shakeTimer.start();
    }
    
    private void checkAndFixPetLocation() {
        Point currentLocation = getLocation();
        
        // Only rescue if pet is truly lost (way off screen) or stuck, not during normal cross-screen movement
        if (isPetTrulyLost(currentLocation)) {
            System.out.println("Pet is truly lost! Rescuing to safe location...");
            
            // Move pet to a safe location
            Point safeLocation = getSafeLocation();
            setLocation(safeLocation);
            
            // Stop current walking and start fresh
            isWalking = false;
            updateIdleSprite();
            
            // Start a new walk after a short delay
            Timer rescueTimer = new Timer(1000, e -> {
                if (!isDragging) {
                    startRandomWalk();
                }
                ((Timer) e.getSource()).stop();
            });
            rescueTimer.start();
        }
        
        // Check if pet has been stuck in the same location for too long
        checkForStuckPet(currentLocation);
    }
    
    private boolean isPetTrulyLost(Point currentLocation) {
        // Check if pet is way off screen (beyond reasonable bounds)
        Rectangle combinedBounds = getCombinedScreenBounds();
        
        // Allow much more tolerance for cross-screen movement
        int bigTolerance = 200; // Much larger tolerance
        
        boolean wayOffScreen = currentLocation.x < combinedBounds.x - bigTolerance ||
                              currentLocation.y < combinedBounds.y - bigTolerance ||
                              currentLocation.x > combinedBounds.x + combinedBounds.width + bigTolerance ||
                              currentLocation.y > combinedBounds.y + combinedBounds.height + bigTolerance;
        
        // Also check if pet is completely invisible (outside all screen bounds by a large margin)
        boolean completelyInvisible = !isLocationPartiallyVisible(currentLocation);
        
        // Only consider pet "truly lost" if it's way off screen AND completely invisible
        return wayOffScreen && completelyInvisible;
    }
    
    private boolean isLocationPartiallyVisible(Point location) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        
        Rectangle petBounds = new Rectangle(location.x, location.y, petWidth, petHeight);
        
        // Check if any part of the pet is visible on any screen
        for (GraphicsDevice device : devices) {
            GraphicsConfiguration config = device.getDefaultConfiguration();
            Rectangle screenBounds = config.getBounds();
            
            // Allow for pets that are partially off-screen during transitions
            if (petBounds.intersects(screenBounds)) {
                return true; // Pet is at least partially visible
            }
        }
        
        return false; // Pet is completely invisible
    }
    
    private Point lastKnownLocation = null;
    private int stuckCounter = 0;
    
    private void checkForStuckPet(Point currentLocation) {
        if (lastKnownLocation != null) {
            double distance = currentLocation.distance(lastKnownLocation);
            
            // If pet hasn't moved much and isn't being dragged and isn't walking
            if (distance < 5 && !isDragging && !isWalking) {
                stuckCounter++;
                
                // If stuck for more than 6 safety checks (30 seconds), help it
                if (stuckCounter >= 6) {
                    System.out.println("Pet appears to be idle for too long! Encouraging movement...");
                    
                    // Don't force rescue, just encourage new movement
                    if (currentBehavior == 1) { // Only if in active mode
                        startRandomWalk();
                    }
                    
                    stuckCounter = 0; // Reset counter
                }
            } else {
                stuckCounter = 0; // Reset if pet is moving or walking
            }
        }
        
        lastKnownLocation = new Point(currentLocation);
    }
    
    private Point getSafeLocation() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        
        // Try to find a safe location on the primary screen first
        GraphicsDevice primaryDevice = ge.getDefaultScreenDevice();
        Rectangle primaryBounds = primaryDevice.getDefaultConfiguration().getBounds();
        
        // Use center of primary screen with some offset
        int safeX = primaryBounds.x + primaryBounds.width / 2 - petWidth / 2;
        int safeY = primaryBounds.y + primaryBounds.height / 2 - petHeight / 2;
        
        // Ensure the safe location is actually within bounds
        Point safeLocation = new Point(safeX, safeY);
        return ensurePetFullyVisible(safeLocation);
    }
    
    private Point getSafeTarget(Point currentLocation) {
        Rectangle screenBounds;
        
        if (allowCrossScreen) {
            // Find which screen the pet is on or use primary
            screenBounds = findScreenForLocation(currentLocation);
        } else {
            screenBounds = getUsableScreenBounds();
        }
        
        // Create target away from edges with larger padding
        int largePadding = Math.max(100, petWidth / 2); // Dynamic padding based on pet size
        int minX = screenBounds.x + largePadding;
        int maxX = screenBounds.x + screenBounds.width - petWidth - largePadding;
        int minY = screenBounds.y + largePadding;
        int maxY = screenBounds.y + screenBounds.height - petHeight - largePadding;
        
        // Ensure valid bounds
        if (maxX <= minX) {
            minX = screenBounds.x + Math.max(50, petWidth / 4);
            maxX = screenBounds.x + screenBounds.width - petWidth - Math.max(50, petWidth / 4);
        }
        if (maxY <= minY) {
            minY = screenBounds.y + Math.max(50, petHeight / 4);
            maxY = screenBounds.y + screenBounds.height - petHeight - Math.max(50, petHeight / 4);
        }
        
        // Ensure we have at least some space to work with
        if (maxX <= minX) maxX = minX + petWidth;
        if (maxY <= minY) maxY = minY + petHeight;
        
        // Generate target away from current position
        int targetX, targetY;
        do {
            targetX = minX + random.nextInt(Math.max(1, maxX - minX));
            targetY = minY + random.nextInt(Math.max(1, maxY - minY));
        } while (Math.abs(targetX - currentLocation.x) < 150 || Math.abs(targetY - currentLocation.y) < 150);
        
        // Ensure the target is fully visible
        Point target = new Point(targetX, targetY);
        Point safeTarget = ensurePetFullyVisible(target);
        targetX = safeTarget.x;
        targetY = safeTarget.y;
        
        return new Point(targetX, targetY);
    }
    
    private Rectangle findScreenForLocation(Point location) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        
        // Find which screen contains the location
        for (GraphicsDevice device : devices) {
            Rectangle screenBounds = device.getDefaultConfiguration().getBounds();
            if (screenBounds.contains(location)) {
                return screenBounds;
            }
        }
        
        // If not found on any screen, return primary screen
        return getPrimaryScreenBounds();
    }
    
    private void updateAnimation() {
        // Animation updates are handled by GIFs automatically
        // This can be used for additional effects
    }
    
    private void setBehavior(int behavior) {
        currentBehavior = behavior;
        switch (behavior) {
            case 0: // Idle
                isWalking = false;
                updateIdleSprite();
                break;
            case 1: // Active/Walking
                if (!isDragging) {
                    startRandomWalk();
                }
                break;
        }
    }
    
    private void startRandomWalk() {
        if (isDragging) return;
        
        if (allowCrossScreen) {
            // Select a random valid screen first, then pick target on that screen
            selectTargetOnRandomScreen();
        } else {
            // Original single-screen behavior
            Rectangle screenBounds = getUsableScreenBounds();
            
            int padding = 50;
            int minX = screenBounds.x + padding;
            int maxX = screenBounds.x + screenBounds.width - petWidth - padding;
            int minY = screenBounds.y + padding;
            int maxY = screenBounds.y + screenBounds.height - petHeight - padding;
            
            if (maxX <= minX) maxX = minX + petWidth;
            if (maxY <= minY) maxY = minY + petHeight;
            
            targetX = minX + random.nextInt(maxX - minX);
            targetY = minY + random.nextInt(maxY - minY);
        }
        
        isWalking = true;
        walkAnimationFrame = 0;
        
        // Determine direction and flip image if needed
        Point current = getLocation();
        boolean shouldFaceRight = targetX > current.x;
        
        if (shouldFaceRight != facingRight) {
            facingRight = shouldFaceRight;
        }
        
        // Set walking animation with direction
        updateWalkingSprite();
        
        Timer walkTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Point current = getLocation();
                int dx = targetX - current.x;
                int dy = targetY - current.y;
                
                if (Math.abs(dx) < 5 && Math.abs(dy) < 5) {
                    isWalking = false;
                    updateIdleSprite();
                    ((Timer) e.getSource()).stop();
                    return;
                }
                
                int stepX = dx == 0 ? 0 : (dx > 0 ? 3 : -3);
                int stepY = dy == 0 ? 0 : (dy > 0 ? 3 : -3);
                
                Point newLocation = new Point(current.x + stepX, current.y + stepY);
                Point safeNewLocation = ensurePetFullyVisible(newLocation);
                
                // Check if we're stuck (not making progress towards target)
                if (safeNewLocation.equals(current)) {
                    // We're stuck, pick a new target
                    System.out.println("Pet is stuck trying to reach target, picking new target...");
                    ((Timer) e.getSource()).stop();
                    isWalking = false;
                    updateIdleSprite();
                    
                    // Start a new walk after a short delay
                    Timer retryTimer = new Timer(1000, evt -> {
                        if (!isDragging) {
                            startRandomWalk();
                        }
                        ((Timer) evt.getSource()).stop();
                    });
                    retryTimer.start();
                    return;
                }
                
                setLocation(safeNewLocation);
                
                // Update walking animation frame for leg sync
                walkAnimationFrame = (walkAnimationFrame + 1) % 8;
                if (walkAnimationFrame % 4 == 0) {
                    updateWalkingSprite(); // Update sprite every 4 frames for leg movement
                }
            }
        });
        walkTimer.start();
    }
    
    private void selectTargetOnRandomScreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        
        if (devices.length == 0) {
            // Fallback to primary screen
            Rectangle bounds = getPrimaryScreenBounds();
            selectTargetOnScreen(bounds);
            return;
        }
        
        // Pick a random screen
        GraphicsDevice targetDevice = devices[random.nextInt(devices.length)];
        Rectangle screenBounds = targetDevice.getDefaultConfiguration().getBounds();
        
        selectTargetOnScreen(screenBounds);
    }
    
    private void selectTargetOnScreen(Rectangle screenBounds) {
        // Use dynamic padding based on pet size to avoid edges better
        int padding = Math.max(80, petWidth / 2); // Dynamic padding based on pet size
        int minX = screenBounds.x + padding;
        int maxX = screenBounds.x + screenBounds.width - petWidth - padding;
        int minY = screenBounds.y + padding;
        int maxY = screenBounds.y + screenBounds.height - petHeight - padding;
        
        // Ensure valid bounds with fallback
        if (maxX <= minX) {
            minX = screenBounds.x + Math.max(30, petWidth / 4);
            maxX = screenBounds.x + screenBounds.width - petWidth - Math.max(30, petWidth / 4);
        }
        if (maxY <= minY) {
            minY = screenBounds.y + Math.max(30, petHeight / 4);
            maxY = screenBounds.y + screenBounds.height - petHeight - Math.max(30, petHeight / 4);
        }
        
        // Ensure we have at least some space to work with
        if (maxX <= minX) maxX = minX + petWidth;
        if (maxY <= minY) maxY = minY + petHeight;
        
        targetX = minX + random.nextInt(Math.max(1, maxX - minX));
        targetY = minY + random.nextInt(Math.max(1, maxY - minY));
        
        // Ensure the target is fully visible
        Point target = new Point(targetX, targetY);
        Point safeTarget = ensurePetFullyVisible(target);
        targetX = safeTarget.x;
        targetY = safeTarget.y;
    }
    
    private Rectangle getUsableScreenBounds() {
        if (allowCrossScreen) {
            return getCombinedScreenBounds();
        } else {
            // Get the screen device that contains the pet (original behavior)
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] devices = ge.getScreenDevices();
            
            Point currentLocation = getLocation();
            
            // Find which screen the pet is currently on
            for (GraphicsDevice device : devices) {
                GraphicsConfiguration config = device.getDefaultConfiguration();
                Rectangle screenBounds = config.getBounds();
                
                if (screenBounds.contains(currentLocation)) {
                    return screenBounds;
                }
            }
            
            // If not found on any screen, use primary screen
            GraphicsDevice primaryDevice = ge.getDefaultScreenDevice();
            return primaryDevice.getDefaultConfiguration().getBounds();
        }
    }
    
    private Rectangle getCombinedScreenBounds() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        
        Rectangle combined = null;
        
        for (GraphicsDevice device : devices) {
            GraphicsConfiguration config = device.getDefaultConfiguration();
            Rectangle screenBounds = config.getBounds();
            
            if (combined == null) {
                combined = new Rectangle(screenBounds);
            } else {
                combined = combined.union(screenBounds);
            }
        }
        
        return combined != null ? combined : getPrimaryScreenBounds();
    }
    
    private boolean isLocationValid(Point location) {
        if (allowCrossScreen) {
            // For cross-screen mode, be much more permissive
            // Allow pets to be partially off-screen during transitions
            return isLocationPartiallyVisible(location);
        } else {
            // Single-screen validation with edge tolerance
            Rectangle screenBounds = getUsableScreenBounds();
            int tolerance = 50; // Allow some movement beyond screen edge
            return location.x >= screenBounds.x - tolerance && 
                   location.y >= screenBounds.y - tolerance && 
                   location.x + petWidth <= screenBounds.x + screenBounds.width + tolerance && 
                   location.y + petHeight <= screenBounds.y + screenBounds.height + tolerance;
        }
    }
    
    private boolean isLocationOnAnyScreen(Point location) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        
        Rectangle petBounds = new Rectangle(location.x, location.y, petWidth, petHeight);
        
        // Check if the pet would be completely within any screen
        for (GraphicsDevice device : devices) {
            GraphicsConfiguration config = device.getDefaultConfiguration();
            Rectangle screenBounds = config.getBounds();
            
            if (screenBounds.contains(petBounds)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isMovementValid(Point current, Point newLocation) {
        if (allowCrossScreen) {
            // For cross-screen movement, be more permissive
            // Allow movement as long as we're progressing toward target and within reasonable bounds
            Rectangle combinedBounds = getCombinedScreenBounds();
            
            // Basic bounds check - allow some flexibility at edges
            boolean withinReasonableBounds = 
                newLocation.x >= combinedBounds.x - 50 && 
                newLocation.y >= combinedBounds.y - 50 && 
                newLocation.x + petWidth <= combinedBounds.x + combinedBounds.width + 50 && 
                newLocation.y + petHeight <= combinedBounds.y + combinedBounds.height + 50;
            
            if (!withinReasonableBounds) {
                return false; // Don't go too far outside screen area
            }
            
            // Check if we're making progress toward the target (more lenient)
            double currentDistance = Math.sqrt(Math.pow(targetX - current.x, 2) + Math.pow(targetY - current.y, 2));
            double newDistance = Math.sqrt(Math.pow(targetX - newLocation.x, 2) + Math.pow(targetY - newLocation.y, 2));
            
            // Allow movement if we're getting closer to target OR if we're on any screen OR if we're not moving away significantly
            return newDistance < currentDistance || isLocationOnAnyScreen(newLocation) || (newDistance - currentDistance < 20);
        } else {
            // Single-screen validation with some edge tolerance
            Rectangle screenBounds = getUsableScreenBounds();
            int edgeTolerance = 25; // Allow pet to go slightly beyond screen edge
            
            return newLocation.x >= screenBounds.x - edgeTolerance && 
                   newLocation.y >= screenBounds.y - edgeTolerance && 
                   newLocation.x + petWidth <= screenBounds.x + screenBounds.width + edgeTolerance && 
                   newLocation.y + petHeight <= screenBounds.y + screenBounds.height + edgeTolerance;
        }
    }
    
    private void updateWalkingSprite() {
        ImageIcon sprite = (walkAnimationFrame % 8 < 4) ? walkGif : idleGif; // Alternate between walk and idle for leg movement
        petLabel.setIcon(getFlippedIcon(sprite));
    }
    
    private void updateIdleSprite() {
        petLabel.setIcon(getFlippedIcon(idleGif));
    }
    
    private ImageIcon getFlippedIcon(ImageIcon original) {
        if (facingRight || original == null) {
            return original; // Return original if facing right or null
        }
        
        // Flip the image horizontally for left-facing direction
        Image img = original.getImage();
        BufferedImage flipped = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = flipped.createGraphics();
        
        // Apply horizontal flip transformation
        g2d.drawImage(img, img.getWidth(null), 0, -img.getWidth(null), img.getHeight(null), null);
        g2d.dispose();
        
        return new ImageIcon(flipped);
    }
    
    private void playSpecialAnimation() {
        if (specialAnimations.isEmpty()) return;
        
        ImageIcon special = specialAnimations.get(random.nextInt(specialAnimations.size()));
        ImageIcon originalIcon = (ImageIcon) petLabel.getIcon();
        
        petLabel.setIcon(special);
        
        Timer resetTimer = new Timer(2000, e -> {
            petLabel.setIcon(originalIcon);
            ((Timer) e.getSource()).stop();
        });
        resetTimer.start();
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        mouseOffset = e.getPoint();
        isDragging = true;
        isWalking = false;
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        isDragging = false;
        if (!isWalking) {
            updateIdleSprite();
        }
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (isDragging && mouseOffset != null) {
            Point mouseOnScreen = e.getLocationOnScreen();
            Point newLocation = new Point(
                mouseOnScreen.x - mouseOffset.x,
                mouseOnScreen.y - mouseOffset.y
            );
            
            // Always ensure the pet is fully visible when dragging
            Point safeLocation = ensurePetFullyVisible(newLocation);
            setLocation(safeLocation);
        }
    }
    
    private Point getClosestValidLocation(Point requestedLocation) {
        if (allowCrossScreen) {
            return getClosestValidLocationOnAnyScreen(requestedLocation);
        } else {
            // Original single-screen behavior
            Rectangle screenBounds = getUsableScreenBounds();
            
            int validX = Math.max(screenBounds.x, 
                         Math.min(requestedLocation.x, screenBounds.x + screenBounds.width - petWidth));
            int validY = Math.max(screenBounds.y, 
                         Math.min(requestedLocation.y, screenBounds.y + screenBounds.height - petHeight));
            
            return new Point(validX, validY);
        }
    }
    
    private Point getClosestValidLocationOnAnyScreen(Point requestedLocation) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        
        Point closestLocation = null;
        double minDistance = Double.MAX_VALUE;
        
        // Find the closest valid location on any screen
        for (GraphicsDevice device : devices) {
            GraphicsConfiguration config = device.getDefaultConfiguration();
            Rectangle screenBounds = config.getBounds();
            
            // Clamp the requested location to this screen
            int validX = Math.max(screenBounds.x, 
                         Math.min(requestedLocation.x, screenBounds.x + screenBounds.width - petWidth));
            int validY = Math.max(screenBounds.y, 
                         Math.min(requestedLocation.y, screenBounds.y + screenBounds.height - petHeight));
            
            Point candidate = new Point(validX, validY);
            
            // Calculate distance from requested location
            double distance = requestedLocation.distance(candidate);
            
            if (distance < minDistance) {
                minDistance = distance;
                closestLocation = candidate;
            }
        }
        
        return closestLocation != null ? closestLocation : getPrimaryScreenCenter();
    }
    
    private Point getPrimaryScreenCenter() {
        Rectangle bounds = getPrimaryScreenBounds();
        return new Point(
            bounds.x + (bounds.width - petWidth) / 2,
            bounds.y + (bounds.height - petHeight) / 2
        );
    }
    
    private Point ensurePetFullyVisible(Point currentLocation) {
        if (allowCrossScreen) {
            return ensurePetFullyVisibleOnAnyScreen(currentLocation);
        } else {
            return ensurePetFullyVisibleOnCurrentScreen(currentLocation);
        }
    }
    
    private Point ensurePetFullyVisibleOnCurrentScreen(Point currentLocation) {
        Rectangle screenBounds = getUsableScreenBounds();
        
        // Calculate the bounds of the pet
        int petRight = currentLocation.x + petWidth;
        int petBottom = currentLocation.y + petHeight;
        
        // Check if pet is being cut off
        boolean needsRepositioning = false;
        int newX = currentLocation.x;
        int newY = currentLocation.y;
        
        // Check right edge (pet extends beyond right edge)
        if (petRight > screenBounds.x + screenBounds.width) {
            newX = screenBounds.x + screenBounds.width - petWidth;
            needsRepositioning = true;
            System.out.println("Pet was cut off on right edge. Pet right: " + petRight + ", Screen right: " + (screenBounds.x + screenBounds.width));
        }
        
        // Check bottom edge (pet extends beyond bottom edge)
        if (petBottom > screenBounds.y + screenBounds.height) {
            newY = screenBounds.y + screenBounds.height - petHeight;
            needsRepositioning = true;
            System.out.println("Pet was cut off on bottom edge. Pet bottom: " + petBottom + ", Screen bottom: " + (screenBounds.y + screenBounds.height));
        }
        
        // Check left edge (pet extends beyond left edge)
        if (newX < screenBounds.x) {
            newX = screenBounds.x;
            needsRepositioning = true;
            System.out.println("Pet was cut off on left edge. Pet left: " + currentLocation.x + ", Screen left: " + screenBounds.x);
        }
        
        // Check top edge (pet extends beyond top edge)
        if (newY < screenBounds.y) {
            newY = screenBounds.y;
            needsRepositioning = true;
            System.out.println("Pet was cut off on top edge. Pet top: " + currentLocation.y + ", Screen top: " + screenBounds.y);
        }
        
        // Additional safety check: ensure the new position is actually valid
        if (needsRepositioning) {
            // Make sure the new position doesn't cause the pet to be cut off in the other direction
            if (newX + petWidth > screenBounds.x + screenBounds.width) {
                newX = screenBounds.x + screenBounds.width - petWidth;
            }
            if (newY + petHeight > screenBounds.y + screenBounds.height) {
                newY = screenBounds.y + screenBounds.height - petHeight;
            }
            if (newX < screenBounds.x) {
                newX = screenBounds.x;
            }
            if (newY < screenBounds.y) {
                newY = screenBounds.y;
            }
            
            System.out.println("Pet repositioned from (" + currentLocation.x + ", " + currentLocation.y + 
                             ") to (" + newX + ", " + newY + ")");
        }
        
        return new Point(newX, newY);
    }
    
    private Point ensurePetFullyVisibleOnAnyScreen(Point currentLocation) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        
        // Find which screen the pet is currently on
        for (GraphicsDevice device : devices) {
            Rectangle screenBounds = device.getDefaultConfiguration().getBounds();
            
            // Check if pet is fully within this screen
            if (currentLocation.x >= screenBounds.x && 
                currentLocation.y >= screenBounds.y &&
                currentLocation.x + petWidth <= screenBounds.x + screenBounds.width &&
                currentLocation.y + petHeight <= screenBounds.y + screenBounds.height) {
                
                // Pet is fully visible on this screen, no need to move
                return currentLocation;
            }
        }
        
        // Pet is not fully visible on any screen, find the best screen to place it
        GraphicsDevice primaryDevice = ge.getDefaultScreenDevice();
        Rectangle primaryBounds = primaryDevice.getDefaultConfiguration().getBounds();
        
        // Place pet in center of primary screen
        int centerX = primaryBounds.x + (primaryBounds.width - petWidth) / 2;
        int centerY = primaryBounds.y + (primaryBounds.height - petHeight) / 2;
        
        System.out.println("Pet was not fully visible on any screen, moved to primary screen center: (" + centerX + ", " + centerY + ")");
        return new Point(centerX, centerY);
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            // Double-click for jump
            animateJump();
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            // Right-click for special animation
            playSpecialAnimation();
        } else if (e.getButton() == MouseEvent.BUTTON2) {
            // Middle-click for settings
            createSettingsWindow();
        }
    }
    
    private void animateJump() {
        Point current = getLocation();
        Timer jumpTimer = new Timer(20, new ActionListener() {
            int jumpStep = 0;
            int originalY = current.y;
            
            @Override
            public void actionPerformed(ActionEvent evt) {
                jumpStep++;
                int jumpHeight = (int) (40 * Math.sin(Math.PI * jumpStep / 25));
                setLocation(current.x, originalY - jumpHeight);
                
                if (jumpStep >= 25) {
                    setLocation(current.x, originalY);
                    ((Timer) evt.getSource()).stop();
                }
            }
        });
        jumpTimer.start();
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseMoved(MouseEvent e) {}
    
    private void duplicatePet() {
        SwingUtilities.invokeLater(() -> {
            new AdvancedDesktopPet();
        });
    }
    
    private void removePet() {
        if (allPets.size() <= 1) {
            JOptionPane.showMessageDialog(settingsWindow, 
                "Cannot remove the last pet!", 
                "Warning", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        allPets.remove(this);
        
        // Stop timers
        if (animationTimer != null) animationTimer.stop();
        if (movementTimer != null) movementTimer.stop();
        if (behaviorTimer != null) behaviorTimer.stop();
        if (safetyTimer != null) safetyTimer.stop();
        
        // Stop enemy system
        stopEnemySystem();
        
        // Remove floating shortcut
        if (floatingShortcut != null) {
            floatingShortcut.dispose();
            floatingShortcut = null;
        }
        
        // Remove from tray if this is the last pet
        if (allPets.isEmpty() && systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
        }
        
        // Close settings window
        if (settingsWindow != null) {
            settingsWindow.setVisible(false);
            settingsWindow = null;
        }
        
        // Dispose of the pet
        dispose();
    }
    
    private void updateTransparency() {
        // Update window opacity
        if (transparency <= 0.1f) {
            setVisible(false);
        } else {
            setVisible(true);
            try {
                setOpacity(transparency);
            } catch (Exception e) {
                // Fallback for systems that don't support opacity
                System.out.println("Transparency not supported on this system");
            }
        }
        
        // Update all enemies' transparency
        for (EnemyWindow enemy : enemies) {
            enemy.updateFromPetSettings();
        }
    }
    
    private void showAllPets() {
        for (AdvancedDesktopPet pet : allPets) {
            pet.setVisible(true);
            pet.transparency = Math.max(0.3f, pet.transparency); // Minimum visibility
            pet.updateTransparency();
        }
    }
    
    private void updateSize(int zoomPercent) {
        // Calculate new size
        int newWidth = (int) (DEFAULT_WIDTH * (zoomPercent / 100.0));
        int newHeight = (int) (DEFAULT_HEIGHT * (zoomPercent / 100.0));
        
        // Update pet size
        petWidth = newWidth;
        petHeight = newHeight;
        setSize(petWidth, petHeight);
        
        // Update petLabel bounds to match new size
        if (petLabel != null) {
            petLabel.setBounds(0, 0, petWidth, petHeight);
            petLabel.setPreferredSize(new Dimension(petWidth, petHeight));
        }
        
        // Move pet to center of screen after size change to avoid boundary issues
        Rectangle screenBounds = getPrimaryScreenBounds();
        int centerX = screenBounds.x + (screenBounds.width - petWidth) / 2;
        int centerY = screenBounds.y + (screenBounds.height - petHeight) / 2;
        setLocation(centerX, centerY);
        
        // Update all enemies' size
        for (EnemyWindow enemy : enemies) {
            enemy.updateFromPetSettings();
        }
        
        // Reload and rescale images
        reloadImagesWithNewSize();
        
        // Stop current walking to prevent conflicts
        isWalking = false;
        updateIdleSprite();
        
        // Force repaint to ensure proper display
        revalidate();
        repaint();
    }
    
    private void reloadImagesWithNewSize() {
        // Reload images with new size
        SwingUtilities.invokeLater(() -> {
            ImageIcon oldIdle = idleGif;
            ImageIcon oldWalk = walkGif;
            
            // Reload main images
            idleGif = loadImageSafely("Image/chibi01.png");
            walkGif = loadImageSafely("Image/chibi02.png");
            
            // Reload special animations
            specialAnimations.clear();
            ImageIcon chibi3 = loadImageSafely("Image/chibi03.png");
            if (chibi3 != null) {
                specialAnimations.add(chibi3);
            }
            
            // If no images found, create default animations with new size
            if (idleGif == null) {
                idleGif = createDefaultAnimation(new Color(255, 150, 150, 200), ":)");
            }
            if (walkGif == null) {
                walkGif = createDefaultAnimation(new Color(150, 255, 150, 200), ">>>");
            }
            
            // Update current sprite
            updateIdleSprite();
            
            // Force repaint to ensure the new images are displayed
            if (petLabel != null) {
                petLabel.revalidate();
                petLabel.repaint();
            }
        });
    }
    
    private void exitApplication() {
        // Stop all timers for all pets
        for (AdvancedDesktopPet pet : allPets) {
            if (pet.animationTimer != null) pet.animationTimer.stop();
            if (pet.movementTimer != null) pet.movementTimer.stop();
            if (pet.behaviorTimer != null) pet.behaviorTimer.stop();
            if (pet.safetyTimer != null) pet.safetyTimer.stop();
            pet.stopEnemySystem(); // Stop enemy system
            if (pet.settingsWindow != null) pet.settingsWindow.dispose();
            if (pet.floatingShortcut != null) pet.floatingShortcut.dispose();
        }
        
        // Remove tray icon
        if (systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
        }
        
        // Clear pets list
        allPets.clear();
        
        System.exit(0);
    }
    
    private void createSettingsWindow() {
        createCyberpunkSettingsWindow();
    }
    
    private void createFloatingShortcut() {
        if (floatingShortcut != null) {
            return;
        }

        floatingShortcut = new JWindow();
        floatingShortcut.setAlwaysOnTop(true);
        floatingShortcut.setSize(80, 80); // Smaller floating shortcut
        floatingShortcut.setBackground(new Color(0, 0, 0, 0));
        
        // Position it offset from the pet
        Point petLocation = getLocation();
        floatingShortcut.setLocation(petLocation.x + 150, petLocation.y - 50);

        // Create cyberpunk-styled shortcut
        JPanel shortcutPanel = createCyberpunkShortcut();
        floatingShortcut.add(shortcutPanel);
        
        floatingShortcut.setVisible(true);
    }
    
    private JPanel createCyberpunkShortcut() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Cyberpunk gradient background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(0, 255, 255, 100), 
                                                         80, 80, new Color(255, 0, 255, 100));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(5, 5, 70, 70, 15, 15);
                
                // Neon border
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(0, 255, 255, 200));
                g2d.drawRoundRect(5, 5, 70, 70, 15, 15);
                
                // Inner glow
                g2d.setColor(new Color(255, 0, 255, 80));
                g2d.drawRoundRect(7, 7, 66, 66, 12, 12);
                
                // Draw custom settings gear icon
                drawSettingsGear(g2d, 40, 40, 15, Color.WHITE);
                
                // Add glow effect
                g2d.setColor(new Color(0, 255, 255, 100));
                drawSettingsGear(g2d, 41, 41, 15, new Color(0, 255, 255, 100));
            }
        };
        
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(80, 80));
        
        // Shared mouseOffset variable for both listeners
        final Point[] mouseOffset = new Point[1]; // Array to make it effectively final
        
        // Add mouse listeners for dragging
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseOffset[0] = e.getPoint();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    // Toggle settings window visibility
                    if (settingsWindow == null) {
                        createCyberpunkSettingsWindow();
                    } else {
                        boolean isVisible = settingsWindow.isVisible();
                        settingsWindow.setVisible(!isVisible);
                        if (!isVisible) {
                            settingsWindow.toFront();
                        }
                    }
                }
            }
        });
        
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouseOffset[0] != null) {
                    Point mouseOnScreen = e.getLocationOnScreen();
                    floatingShortcut.setLocation(
                        mouseOnScreen.x - mouseOffset[0].x,
                        mouseOnScreen.y - mouseOffset[0].y
                    );
                }
            }
        });
        
        return panel;
    }
    
    private void drawSettingsGear(Graphics2D g2d, int centerX, int centerY, int radius, Color color) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2));
        
        // Draw outer circle
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Draw inner circle
        int innerRadius = radius / 2;
        g2d.drawOval(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2);
        
        // Draw gear teeth
        int numTeeth = 8;
        int toothLength = radius / 3;
        for (int i = 0; i < numTeeth; i++) {
            double angle = (2 * Math.PI * i) / numTeeth;
            int x1 = centerX + (int)(radius * Math.cos(angle));
            int y1 = centerY + (int)(radius * Math.sin(angle));
            int x2 = centerX + (int)((radius + toothLength) * Math.cos(angle));
            int y2 = centerY + (int)((radius + toothLength) * Math.sin(angle));
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        // Draw center dot
        g2d.fillOval(centerX - 2, centerY - 2, 4, 4);
    }
    
    private void createCyberpunkSettingsWindow() {
        if (settingsWindow != null) {
            settingsWindow.setVisible(true);
            settingsWindow.toFront();
            return;
        }

        // Create stylish settings window
        settingsWindow = new JFrame();
        settingsWindow.setTitle(getText("settings_title"));
        settingsWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        settingsWindow.setSize(600, 500);
        settingsWindow.setResizable(false);
        settingsWindow.setLocationRelativeTo(null);
        settingsWindow.setUndecorated(true); // Remove default decorations
        
        // Create main panel with cyberpunk styling
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Dark background
                GradientPaint bgGradient = new GradientPaint(0, 0, new Color(20, 20, 35), 
                                                           600, 500, new Color(35, 20, 45));
                g2d.setPaint(bgGradient);
                g2d.fillRect(0, 0, 600, 500);
                
                // Border
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(80, 80, 120));
                g2d.drawRect(1, 1, 598, 498);
                
                // Title bar
                g2d.setColor(new Color(60, 60, 100, 100));
                g2d.fillRect(0, 0, 600, 30);
                g2d.setStroke(new BasicStroke(1));
                g2d.setColor(new Color(100, 100, 140));
                g2d.drawLine(0, 30, 600, 30);
            }
        };
        
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15)); // Remove top border
        
        // Title panel with minimize button
        JPanel titlePanel = createTitlePanel();
        titlePanel.setPreferredSize(new Dimension(600, 30)); // Ensure it covers the top bar
        
        // Content panel with grid layout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Language Section
        languageSectionLabel = addSection(contentPanel, gbc, 0, getText("language"));
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        englishBtn = createButton(getText("english"));
        englishBtn.setEnabled(isChinese); // Only enable if not already English
        if (!isChinese) {
            englishBtn.setBackground(new Color(100, 100, 120));
        }
        englishBtn.addActionListener(e -> {
            System.out.println("English button clicked. Current isChinese: " + isChinese);
            if (isChinese) {
                isChinese = false;
                System.out.println("Switching to English. New isChinese: " + isChinese);
                refreshSettingsWindow();
            }
        });
        contentPanel.add(englishBtn, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        chineseBtn = createButton(getText("chinese"));
        chineseBtn.setEnabled(!isChinese); // Only enable if not already Chinese
        if (isChinese) {
            chineseBtn.setBackground(new Color(100, 100, 120));
        }
        chineseBtn.addActionListener(e -> {
            System.out.println("Chinese button clicked. Current isChinese: " + isChinese);
            if (!isChinese) {
                isChinese = true;
                System.out.println("Switching to Chinese. New isChinese: " + isChinese);
                refreshSettingsWindow();
            }
        });
        contentPanel.add(chineseBtn, gbc);
        
        // Pet Management Section
        petManagementSectionLabel = addSection(contentPanel, gbc, 2, getText("pet_management"));
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        duplicateBtn = createButton(getText("duplicate_pet"));
        duplicateBtn.addActionListener(e -> duplicatePet());
        contentPanel.add(duplicateBtn, gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        removeBtn = createButton(getText("remove_pet"));
        removeBtn.addActionListener(e -> removePet());
        contentPanel.add(removeBtn, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        petCountLabel = createLabel(getText("active_pets") + ": " + allPets.size());
        contentPanel.add(petCountLabel, gbc);
        
        // Transparency Section
        transparencySectionLabel = addSection(contentPanel, gbc, 5, getText("transparency"));
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        JSlider transparencySlider = createSlider(0, 100, (int)(transparency * 100));
        transparencySlider.setPreferredSize(new Dimension(350, 40));
        transparencySlider.addChangeListener(e -> {
            transparency = transparencySlider.getValue() / 100.0f;
            updateTransparency();
        });
        contentPanel.add(transparencySlider, gbc);
        gbc.weightx = 0;
        
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
        hideBtn = createButton(getText("hide_pet"));
        hideBtn.addActionListener(e -> {
            setVisible(false);
            settingsWindow.setVisible(false);
        });
        contentPanel.add(hideBtn, gbc);
        
        gbc.gridx = 1; gbc.gridy = 7;
        showBtn = createButton(getText("show_all_pets"));
        showBtn.addActionListener(e -> showAllPets());
        contentPanel.add(showBtn, gbc);
        
        // Size Section
        sizeSectionLabel = addSection(contentPanel, gbc, 8, getText("size"));
        
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        JSlider zoomSlider = createSlider(50, 300, (int)((petWidth / (double)DEFAULT_WIDTH) * 100));
        zoomSlider.setPreferredSize(new Dimension(350, 40));
        zoomSlider.addChangeListener(e -> {
            int zoomPercent = zoomSlider.getValue();
            updateSize(zoomPercent);
        });
        contentPanel.add(zoomSlider, gbc);
        gbc.weightx = 0;
        
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 1;
        zoomInBtn = createButton(getText("zoom_in"));
        zoomInBtn.addActionListener(e -> {
            int currentZoom = (int)((petWidth / (double)DEFAULT_WIDTH) * 100);
            int newZoom = Math.min(300, currentZoom + 25);
            zoomSlider.setValue(newZoom);
        });
        contentPanel.add(zoomInBtn, gbc);
        
        gbc.gridx = 1; gbc.gridy = 10;
        zoomOutBtn = createButton(getText("zoom_out"));
        zoomOutBtn.addActionListener(e -> {
            int currentZoom = (int)((petWidth / (double)DEFAULT_WIDTH) * 100);
            int newZoom = Math.max(50, currentZoom - 25);
            zoomSlider.setValue(newZoom);
        });
        contentPanel.add(zoomOutBtn, gbc);
        
        // Movement Section
        movementSectionLabel = addSection(contentPanel, gbc, 11, getText("movement_settings"));
        
        gbc.gridx = 0; gbc.gridy = 12; gbc.gridwidth = 1;
        crossScreenBox = createCheckBox(getText("allow_cross_screen"), allowCrossScreen);
        crossScreenBox.addActionListener(e -> allowCrossScreen = crossScreenBox.isSelected());
        contentPanel.add(crossScreenBox, gbc);
        
        gbc.gridx = 1; gbc.gridy = 12;
        testCrossScreenBtn = createButton(getText("test_cross_screen"));
        testCrossScreenBtn.addActionListener(e -> moveToRandomScreen());
        contentPanel.add(testCrossScreenBtn, gbc);
        
        gbc.gridx = 0; gbc.gridy = 13; gbc.gridwidth = 1;
        musicBox = createCheckBox(getText("music_enabled"), musicEnabled);
        musicBox.addActionListener(e -> {
            setMusicEnabled(musicBox.isSelected());
        });
        contentPanel.add(musicBox, gbc);
        
        // Horror Section
        horrorSectionLabel = addSection(contentPanel, gbc, 14, getText("horror_mode"));
        
        gbc.gridx = 0; gbc.gridy = 15; gbc.gridwidth = 1;
        enemyBox = createCheckBox(getText("enable_enemies"), enemyEnabled);
        enemyBox.addActionListener(e -> toggleEnemySystem(enemyBox.isSelected()));
        contentPanel.add(enemyBox, gbc);
        
        gbc.gridx = 1; gbc.gridy = 15;
        spawnEnemyBtn = createButton(getText("spawn_enemy_now"));
        spawnEnemyBtn.addActionListener(e -> {
            if (enemyEnabled) {
                spawnEnemy();
            } else {
                JOptionPane.showMessageDialog(settingsWindow, 
                    getText("enable_enemies") + " first!", 
                    getText("enemies") + " Disabled", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        contentPanel.add(spawnEnemyBtn, gbc);
        
        gbc.gridx = 0; gbc.gridy = 16; gbc.gridwidth = 2;
        enemyInfoLabel = createLabel(getText("enemies") + ": " + enemies.size() + " / " + maxEnemies + " active");
        contentPanel.add(enemyInfoLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 17; gbc.gridwidth = 2;
        maxEnemiesLabel = createLabel(getText("max_enemies") + ": " + maxEnemies);
        contentPanel.add(maxEnemiesLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 18; gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        JSlider maxEnemiesSlider = createSlider(1, 10, maxEnemies);
        maxEnemiesSlider.setPreferredSize(new Dimension(350, 40));
        maxEnemiesSlider.addChangeListener(e -> {
            maxEnemies = maxEnemiesSlider.getValue();
            if (maxEnemiesLabel != null) {
                maxEnemiesLabel.setText(getText("max_enemies") + ": " + maxEnemies);
            }
            if (enemyInfoLabel != null) {
                enemyInfoLabel.setText(getText("enemies") + ": " + enemies.size() + " / " + maxEnemies + " active");
            }
        });
        contentPanel.add(maxEnemiesSlider, gbc);
        gbc.weightx = 0;
        
        gbc.gridx = 0; gbc.gridy = 19; gbc.gridwidth = 1;
        clearEnemiesBtn = createButton(getText("clear_all_enemies"));
        clearEnemiesBtn.addActionListener(e -> stopEnemySystem());
        contentPanel.add(clearEnemiesBtn, gbc);
        
        gbc.gridx = 1; gbc.gridy = 19;
        forceCleanupBtn = createButton(getText("force_cleanup"));
        forceCleanupBtn.addActionListener(e -> forceRemoveAllEnemies());
        contentPanel.add(forceCleanupBtn, gbc);
        
        gbc.gridx = 0; gbc.gridy = 20; gbc.gridwidth = 1;
        closeBtn = createButton(getText("close"));
        closeBtn.addActionListener(e -> {
            settingsWindow.setVisible(false);
        });
        contentPanel.add(closeBtn, gbc);
        
        gbc.gridx = 1; gbc.gridy = 20;
        exitBtn = createButton(getText("exit_program"));
        exitBtn.setBackground(new Color(150, 50, 50)); // Red background for exit button
        exitBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(settingsWindow,
                getText("confirm_exit"),
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        contentPanel.add(exitBtn, gbc);
        
        // Add content to scroll pane for better layout
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        settingsWindow.add(mainPanel);
        
        // Make window draggable by title panel
        addDragFunctionality(settingsWindow, titlePanel);
        
        settingsWindow.setVisible(true);
        
        // Update pet count
        Timer updateTimer = new Timer(1000, e -> {
            petCountLabel.setText(getText("active_pets") + ": " + allPets.size());
        });
        updateTimer.start();
        
        settingsWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                settingsWindow.setVisible(false);
                updateTimer.stop();
            }
            
            @Override
            public void windowClosed(WindowEvent e) {
                settingsWindow = null;
                updateTimer.stop();
            }
        });
    }
    
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Dark gradient background
                GradientPaint bgGradient = new GradientPaint(0, 0, new Color(40, 40, 60), 
                                                           600, 30, new Color(60, 40, 80));
                g2d.setPaint(bgGradient);
                g2d.fillRect(0, 0, 600, 30);
                
                // Border
                g2d.setStroke(new BasicStroke(1));
                g2d.setColor(new Color(100, 100, 120));
                g2d.drawLine(0, 30, 600, 30);
            }
        };
        titlePanel.setOpaque(false);
        titlePanel.setPreferredSize(new Dimension(600, 30));
        titlePanel.setLayout(new BorderLayout());
        
        // Title text
        JLabel titleLabel = new JLabel(getText("settings_title"));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        // Button panel for minimize and maximize
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        
        // Close button
        JButton closeBtn = createCloseButton();
        closeBtn.addActionListener(e -> settingsWindow.setVisible(false));
        
        // Minimize button
        JButton minimizeBtn = createMinimizeButton();
        minimizeBtn.addActionListener(e -> settingsWindow.setVisible(false));
        
        buttonPanel.add(minimizeBtn);
        buttonPanel.add(closeBtn);
        titlePanel.add(buttonPanel, BorderLayout.EAST);
        
        return titlePanel;
    }
    
    private JButton createIconButton(String icon, String tooltip) {
        JButton button = new JButton(icon);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 0, 0, 0));
        button.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        button.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setToolTipText(tooltip);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(100, 100, 100, 100));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0, 0, 0, 0));
            }
        });
        
        return button;
    }
    
    private JButton createCloseButton() {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw close icon (X)
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                
                int size = Math.min(getWidth(), getHeight()) - 8;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                // Draw X lines
                g2d.drawLine(x, y, x + size, y + size);
                g2d.drawLine(x + size, y, x, y + size);
            }
        };
        
        button.setPreferredSize(new Dimension(30, 30));
        button.setBackground(new Color(0, 0, 0, 0));
        button.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        button.setFocusPainted(false);
        button.setToolTipText("Close window");
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(255, 0, 0, 100)); // Red background on hover
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0, 0, 0, 0));
            }
        });
        
        return button;
    }
    
    private JButton createMinimizeButton() {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw minimize icon (horizontal line)
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                
                int width = getWidth() - 16;
                int height = 2;
                int x = (getWidth() - width) / 2;
                int y = (getHeight() - height) / 2;
                
                // Draw horizontal line
                g2d.drawLine(x, y, x + width, y);
            }
        };
        
        button.setPreferredSize(new Dimension(30, 30));
        button.setBackground(new Color(0, 0, 0, 0));
        button.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        button.setFocusPainted(false);
        button.setToolTipText("Minimize window");
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(100, 100, 100, 100)); // Gray background on hover
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0, 0, 0, 0));
            }
        });
        
        return button;
    }
    
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(70, 70, 90));
        // Use a font that supports both English and Chinese characters
        button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(90, 90, 110));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(70, 70, 90));
            }
        });
        
        return button;
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        // Use a font that supports both English and Chinese characters
        label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return label;
    }
    
    private JSlider createSlider(int min, int max, int value) {
        JSlider slider = new JSlider(min, max, value);
        slider.setOpaque(false);
        slider.setMajorTickSpacing(25);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));
        slider.setFocusable(false);
        slider.setForeground(Color.WHITE);
        
        // Set custom label font for better readability
        slider.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        
        // Create custom label table for better spacing
        Dictionary<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = min; i <= max; i += 25) {
            JLabel label = new JLabel(String.valueOf(i));
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
            labelTable.put(i, label);
        }
        slider.setLabelTable(labelTable);
        
        return slider;
    }
    
    private JCheckBox createCheckBox(String text, boolean selected) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setForeground(Color.WHITE);
        checkBox.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        checkBox.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        checkBox.setOpaque(false);
        checkBox.setFocusable(false);
        checkBox.setSelected(selected);
        return checkBox;
    }
    
    private JLabel addSection(JPanel panel, GridBagConstraints gbc, int y, String title) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        JLabel sectionTitle = createLabel(title);
        sectionTitle.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        sectionTitle.setForeground(new Color(200, 200, 255)); // Slightly highlighted
        panel.add(sectionTitle, gbc);
        return sectionTitle;
    }
    
    private JPanel createCyberpunkTitlePanel() {
        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Dark cyberpunk background
                GradientPaint bgGradient = new GradientPaint(0, 0, new Color(10, 10, 25), 
                                                           500, 30, new Color(25, 10, 35));
                g2d.setPaint(bgGradient);
                g2d.fillRect(0, 0, 500, 30);
                
                // Neon border
                g2d.setStroke(new BasicStroke(3));
                g2d.setColor(new Color(0, 255, 255));
                g2d.drawLine(0, 30, 500, 30);
                
                // Title text
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
                FontMetrics fm = g2d.getFontMetrics();
                String title = "◤ CYBER PET CONTROL ◥";
                int x = (500 - fm.stringWidth(title)) / 2;
                int y = (30 + fm.getAscent()) / 2;
                g2d.drawString(title, x, y);
            }
        };
        titlePanel.setOpaque(false);
        titlePanel.setPreferredSize(new Dimension(500, 30));
        return titlePanel;
    }
    
    private JButton createCyberpunkButton(String text, Color neonColor) {
        JButton button = new JButton(text);
        button.setForeground(neonColor);
        button.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(neonColor.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(neonColor);
            }
        });
        
        return button;
    }
    
    private JLabel createCyberpunkLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        label.setOpaque(false);
        return label;
    }
    
    private JSlider createCyberpunkSlider(int min, int max, int value) {
        JSlider slider = new JSlider(min, max, value);
        slider.setOpaque(false);
        slider.setMajorTickSpacing(25);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        slider.setFocusable(false);
        return slider;
    }
    
    private JCheckBox createCyberpunkCheckBox(String text, boolean selected) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setForeground(Color.WHITE);
        checkBox.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        checkBox.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        checkBox.setOpaque(false);
        checkBox.setFocusable(false);
        checkBox.setSelected(selected);
        return checkBox;
    }
    
    private void addCyberpunkSection(JPanel panel, GridBagConstraints gbc, int y, String title) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        JLabel sectionTitle = createCyberpunkLabel(title);
        panel.add(sectionTitle, gbc);
        gbc.gridy++; // Move to the next row for content
    }
    
    private void addDragFunctionality(JFrame frame, JPanel titlePanel) {
        // Shared mouseOffset variable for both listeners
        final Point[] mouseOffset = new Point[1]; // Array to make it effectively final
        
        titlePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseOffset[0] = e.getPoint();
            }
        });
        
        titlePanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouseOffset[0] != null) {
                    Point mouseOnScreen = e.getLocationOnScreen();
                    frame.setLocation(
                        mouseOnScreen.x - mouseOffset[0].x,
                        mouseOnScreen.y - mouseOffset[0].y
                    );
                }
            }
        });
    }
    
    private void moveToRandomScreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        
        if (devices.length <= 1) {
            System.out.println("Only one screen detected, cannot move to another screen");
            return;
        }
        
        // Get current screen
        Point currentLocation = getLocation();
        GraphicsDevice currentScreen = null;
        
        for (GraphicsDevice device : devices) {
            Rectangle bounds = device.getDefaultConfiguration().getBounds();
            if (bounds.contains(currentLocation)) {
                currentScreen = device;
                break;
            }
        }
        
        // Pick a different screen
        GraphicsDevice targetScreen = null;
        do {
            targetScreen = devices[random.nextInt(devices.length)];
        } while (targetScreen == currentScreen);
        
        // Move to center of target screen
        Rectangle targetBounds = targetScreen.getDefaultConfiguration().getBounds();
        int newX = targetBounds.x + (targetBounds.width - petWidth) / 2;
        int newY = targetBounds.y + (targetBounds.height - petHeight) / 2;
        
        System.out.println("Moving from screen " + currentScreen.getIDstring() + " to screen " + targetScreen.getIDstring());
        System.out.println("New location: (" + newX + ", " + newY + ")");
        
        setLocation(newX, newY);
    }
    
    public static void main(String[] args) {
        // Use system look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName()) || "System".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Use default look and feel if system one fails
        }
        
        SwingUtilities.invokeLater(() -> {
            new AdvancedDesktopPet();
        });
    }

    @Override
    public void setLocation(int x, int y) {
        // Temporarily disable boundary checking to see if that's the issue
        super.setLocation(x, y);
        
        // Uncomment this to re-enable boundary checking:
        /*
        Point requested = new Point(x, y);
        Point safe = ensurePetFullyVisible(requested);
        
        // Only log if the position was actually changed
        if (!safe.equals(requested)) {
            System.out.println("setLocation clamped from (" + x + ", " + y + ") to (" + safe.x + ", " + safe.y + ")");
        }
        
        super.setLocation(safe.x, safe.y);
        */
    }

    @Override
    public void setLocation(Point p) {
        setLocation(p.x, p.y);
    }
}

// Enemy class that follows the pet
class EnemyWindow extends JWindow {
    private JLabel enemyLabel;
    private Timer followTimer;
    private Timer horrorEffectTimer;
    private Timer animationTimer;
    private AdvancedDesktopPet targetPet;
    private Random random = new Random();
    private boolean isVisible = true;
    private int enemyWidth;
    private int enemyHeight;
    private float enemyTransparency = 1.0f;
    private ImageIcon currentEnemyImage;
    private List<ImageIcon> enemyImages;
    private int flickerCount = 0;
    private int currentAnimationFrame = 0;
    private boolean enemyFacingRight = true; // Track enemy facing direction
    private Point lastLocation = null; // Track last position for direction calculation
    
    public EnemyWindow(AdvancedDesktopPet pet, List<ImageIcon> images) {
        this.targetPet = pet;
        this.enemyImages = images;
        
        // Get size and transparency from target pet
        this.enemyWidth = pet.petWidth;
        this.enemyHeight = pet.petHeight;
        this.enemyTransparency = pet.transparency;
        
        initializeEnemy();
        startFollowing();
        startHorrorEffects();
        startAnimation();
    }
    
    private void initializeEnemy() {
        setAlwaysOnTop(true);
        setSize(enemyWidth, enemyHeight);
        setBackground(new Color(0, 0, 0, 0));
        
        // Apply transparency
        updateEnemyTransparency();
        
        enemyLabel = new JLabel();
        enemyLabel.setHorizontalAlignment(JLabel.CENTER);
        enemyLabel.setVerticalAlignment(JLabel.CENTER);
        add(enemyLabel);
        
        // Load random enemy image and scale it to match enemy size
        if (!enemyImages.isEmpty()) {
            try {
                ImageIcon originalImage = enemyImages.get(random.nextInt(enemyImages.size()));
                if (originalImage != null && originalImage.getImage() != null) {
                    Image scaledImage = originalImage.getImage().getScaledInstance(
                        enemyWidth, enemyHeight, Image.SCALE_SMOOTH);
                    currentEnemyImage = new ImageIcon(scaledImage);
                    enemyLabel.setIcon(getFlippedEnemyIcon(currentEnemyImage));
                }
            } catch (Exception e) {
                System.out.println("Error loading initial enemy image: " + e.getMessage());
            }
        }
        
        // Start at a random position near the pet
        Point petLocation = targetPet.getLocation();
        int offsetX = random.nextInt(400) - 200; // Random offset -200 to +200
        int offsetY = random.nextInt(400) - 200;
        setLocation(petLocation.x + offsetX, petLocation.y + offsetY);
        
        setVisible(true);
        
        // Add window listener for proper cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Enemy window closing: " + EnemyWindow.this.hashCode());
                stopEnemy();
            }
            
            @Override
            public void windowClosed(WindowEvent e) {
                System.out.println("Enemy window closed: " + EnemyWindow.this.hashCode());
            }
        });
    }
    
    private void startFollowing() {
        followTimer = new Timer(100, e -> followPet());
        followTimer.start();
    }
    
    private void startHorrorEffects() {
        horrorEffectTimer = new Timer(3000 + random.nextInt(4000), e -> {
            createHorrorEffect();
            // Randomize next horror effect timing
            horrorEffectTimer.setDelay(2000 + random.nextInt(6000));
        });
        horrorEffectTimer.start();
    }
    
    private void startAnimation() {
        if (enemyImages.size() > 1) {
            // Create animation timer to cycle through enemy frames
            animationTimer = new Timer(500 + random.nextInt(1000), e -> {
                try {
                currentAnimationFrame = (currentAnimationFrame + 1) % enemyImages.size();
                    ImageIcon originalImage = enemyImages.get(currentAnimationFrame);
                    if (originalImage != null && originalImage.getImage() != null) {
                        Image scaledImage = originalImage.getImage().getScaledInstance(
                            enemyWidth, enemyHeight, Image.SCALE_SMOOTH);
                        currentEnemyImage = new ImageIcon(scaledImage);
                        enemyLabel.setIcon(getFlippedEnemyIcon(currentEnemyImage));
                    }
                
                // Randomize next animation frame timing for creepy effect
                animationTimer.setDelay(300 + random.nextInt(1200));
                } catch (Exception ex) {
                    System.out.println("Error in enemy animation: " + ex.getMessage());
                }
            });
            animationTimer.start();
        }
    }
    
    private void followPet() {
        if (targetPet == null) return;
        
        try {
        Point petLocation = targetPet.getLocation();
        Point currentLocation = getLocation();
        
        // Calculate distance to pet
        double distance = Math.sqrt(Math.pow(petLocation.x - currentLocation.x, 2) + 
                                   Math.pow(petLocation.y - currentLocation.y, 2));
        
        // Follow pet but maintain some distance (don't get too close)
        if (distance > 80) {
            // Move towards pet
            int stepSize = 2 + random.nextInt(3); // Random step size for creepy movement
            int dx = petLocation.x - currentLocation.x;
            int dy = petLocation.y - currentLocation.y;
                
                System.out.println("Enemy following pet - dx: " + dx + ", enemy to " + (dx > 0 ? "LEFT" : "RIGHT") + " of pet, should face: " + (dx > 0 ? "RIGHT" : "LEFT") + ", current facing: " + (enemyFacingRight ? "RIGHT" : "LEFT"));
            
            // Normalize movement
            if (Math.abs(dx) > stepSize) dx = dx > 0 ? stepSize : -stepSize;
            if (Math.abs(dy) > stepSize) dy = dy > 0 ? stepSize : -stepSize;
            
            // Add some randomness to movement for creepy effect
                int randomX = random.nextInt(3) - 1;
                int randomY = random.nextInt(3) - 1;
                dx += randomX;
                dy += randomY;
                
                // Update enemy direction based on position relative to pet
                // If enemy is to the left of pet (dx > 0), enemy should face right
                // If enemy is to the right of pet (dx < 0), enemy should face left
                updateEnemyDirection(dx > 0 ? -1 : 1);
            
            setLocation(currentLocation.x + dx, currentLocation.y + dy);
        } else {
            // If too close, make pet shake and occasionally move away (stalking behavior)
            if (random.nextInt(30) == 0) {
                targetPet.createHorrorShake(); // Make pet shake when enemy is close
            }
            
            if (random.nextInt(20) == 0) {
                int escapeX = random.nextInt(6) - 3;
                int escapeY = random.nextInt(6) - 3;
                setLocation(currentLocation.x + escapeX, currentLocation.y + escapeY);
                    
                    // Update direction for escape movement
                    updateEnemyDirection(escapeX > 0 ? 1 : -1);
                }
            }
            
            // Update last location for next frame
            lastLocation = new Point(currentLocation);
        } catch (Exception e) {
            System.out.println("Error in enemy followPet: " + e.getMessage());
        }
    }
    
    private void createHorrorEffect() {
        int effectType = random.nextInt(5);
        
        switch (effectType) {
            case 0: // Disappear and reappear
                disappearAndReappear();
                break;
            case 1: // Flicker effect
                startFlicker();
                break;
            case 2: // Teleport jump scare
                teleportJumpScare();
                break;
            case 3: // Change image
                changeEnemyImage();
                break;
            case 4: // Rapid animation horror
                rapidAnimationHorror();
                break;
        }
    }
    
    private void disappearAndReappear() {
        setVisible(false);
        
        Timer reappearTimer = new Timer(1000 + random.nextInt(3000), e -> {
            // Reappear at a new location near the pet
            Point petLocation = targetPet.getLocation();
            int offsetX = random.nextInt(300) - 150;
            int offsetY = random.nextInt(300) - 150;
            setLocation(petLocation.x + offsetX, petLocation.y + offsetY);
            setVisible(true);
            ((Timer) e.getSource()).stop();
        });
        reappearTimer.start();
    }
    
    private void startFlicker() {
        flickerCount = 0;
        Timer flickerTimer = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flickerCount++;
                setVisible(!isVisible());
                
                if (flickerCount >= 6) {
                    setVisible(true);
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        flickerTimer.start();
    }
    
    private void teleportJumpScare() {
        // Teleport closer to pet for jump scare
        Point petLocation = targetPet.getLocation();
        int closeOffsetX = random.nextInt(100) - 50;
        int closeOffsetY = random.nextInt(100) - 50;
        setLocation(petLocation.x + closeOffsetX, petLocation.y + closeOffsetY);
        
        // Flash red briefly
        Color originalBg = getBackground();
        setBackground(new Color(255, 0, 0, 100));
        
        Timer flashTimer = new Timer(300, e -> {
            setBackground(originalBg);
            ((Timer) e.getSource()).stop();
        });
        flashTimer.start();
    }
    
    private void changeEnemyImage() {
        if (enemyImages.size() > 1 && currentEnemyImage != null) {
            ImageIcon originalImage;
            int attempts = 0;
            do {
                originalImage = enemyImages.get(random.nextInt(enemyImages.size()));
                attempts++;
                // Prevent infinite loop
                if (attempts > 10) break;
            } while (originalImage != null && currentEnemyImage != null && 
                     originalImage.getImage().equals(currentEnemyImage.getImage()));
            
            if (originalImage != null) {
                try {
                    Image scaledImage = originalImage.getImage().getScaledInstance(
                        enemyWidth, enemyHeight, Image.SCALE_SMOOTH);
                    currentEnemyImage = new ImageIcon(scaledImage);
                    enemyLabel.setIcon(getFlippedEnemyIcon(currentEnemyImage));
                } catch (Exception e) {
                    System.out.println("Error scaling enemy image: " + e.getMessage());
                }
            }
        }
    }
    
    private void rapidAnimationHorror() {
        if (enemyImages.size() > 1) {
            // Temporarily stop normal animation
            if (animationTimer != null) animationTimer.stop();
            
            // Create rapid animation effect
            Timer rapidTimer = new Timer(80, new ActionListener() {
                int rapidCount = 0;
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    rapidCount++;
                    
                    // Rapidly cycle through all frames
                    try {
                    int frameIndex = rapidCount % enemyImages.size();
                        ImageIcon originalImage = enemyImages.get(frameIndex);
                        if (originalImage != null && originalImage.getImage() != null) {
                            Image scaledImage = originalImage.getImage().getScaledInstance(
                                enemyWidth, enemyHeight, Image.SCALE_SMOOTH);
                            currentEnemyImage = new ImageIcon(scaledImage);
                            enemyLabel.setIcon(getFlippedEnemyIcon(currentEnemyImage));
                        }
                    } catch (Exception ex) {
                        System.out.println("Error in rapid animation: " + ex.getMessage());
                    }
                    
                    // Stop after 15 rapid cycles
                    if (rapidCount >= 15) {
                        ((Timer) e.getSource()).stop();
                        
                        // Resume normal animation after rapid effect
                        if (animationTimer != null) {
                            animationTimer.start();
                        }
                    }
                }
            });
            rapidTimer.start();
        }
    }
    
    public void stopEnemy() {
        System.out.println("Stopping enemy: " + this.hashCode());
        
        try {
            // Stop all timers first
            stopAllTimers();
            
            // Hide and dispose the window
            setVisible(false);
        dispose();
            
            System.out.println("Enemy stopped successfully: " + this.hashCode());
            
        } catch (Exception e) {
            System.out.println("Error stopping enemy: " + e.getMessage());
            // Try to force dispose even if there's an error
            try {
                setVisible(false);
                dispose();
            } catch (Exception ex) {
                System.out.println("Error force disposing enemy: " + ex.getMessage());
            }
        }
    }
    
    // Public method to stop all timers
    public void stopAllTimers() {
        if (followTimer != null) {
            followTimer.stop();
            followTimer = null;
        }
        if (horrorEffectTimer != null) {
            horrorEffectTimer.stop();
            horrorEffectTimer = null;
        }
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
    }
    
    // Public method to check if timers are null (for stuck detection)
    public boolean hasNullTimers() {
        return followTimer == null && horrorEffectTimer == null && animationTimer == null;
    }
    
    // Update enemy transparency
    private void updateEnemyTransparency() {
        if (enemyTransparency <= 0.1f) {
            setVisible(false);
        } else {
            setVisible(true);
            try {
                setOpacity(enemyTransparency);
            } catch (Exception e) {
                // Fallback for systems that don't support opacity
                System.out.println("Enemy transparency not supported on this system");
            }
        }
    }
    
    // Update enemy size and transparency from pet settings
    public void updateFromPetSettings() {
        if (targetPet != null) {
            // Update size
            enemyWidth = targetPet.petWidth;
            enemyHeight = targetPet.petHeight;
            setSize(enemyWidth, enemyHeight);
            
            System.out.println("Enemy size updated to: " + enemyWidth + "x" + enemyHeight + 
                             " (Pet size: " + targetPet.petWidth + "x" + targetPet.petHeight + ")");
            
            // Scale the current enemy image to match the new size
            if (currentEnemyImage != null && currentEnemyImage.getImage() != null) {
                try {
                    Image scaledImage = currentEnemyImage.getImage().getScaledInstance(
                        enemyWidth, enemyHeight, Image.SCALE_SMOOTH);
                    currentEnemyImage = new ImageIcon(scaledImage);
                    enemyLabel.setIcon(getFlippedEnemyIcon(currentEnemyImage));
                } catch (Exception e) {
                    System.out.println("Error scaling enemy image in update: " + e.getMessage());
                }
            }
            
            // Update transparency
            enemyTransparency = targetPet.transparency;
            updateEnemyTransparency();
        }
    }
    
    // Flip enemy image based on facing direction
    private ImageIcon getFlippedEnemyIcon(ImageIcon original) {
        if (enemyFacingRight || original == null) {
            return original; // Return original if facing right or null
        }
        
        // Flip the image horizontally for left-facing direction
        try {
            Image img = original.getImage();
            BufferedImage flipped = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = flipped.createGraphics();
            
            // Apply horizontal flip transformation
            g2d.drawImage(img, img.getWidth(null), 0, -img.getWidth(null), img.getHeight(null), null);
            g2d.dispose();
            
            return new ImageIcon(flipped);
        } catch (Exception e) {
            System.out.println("Error flipping enemy image: " + e.getMessage());
            return original;
        }
    }
    
    // Update enemy direction based on movement
    private void updateEnemyDirection(int dx) {
        if (dx > 0 && !enemyFacingRight) {
            // Moving right, should face right
            enemyFacingRight = true;
            updateEnemySprite();
            System.out.println("Enemy now facing RIGHT (dx: " + dx + ")");
        } else if (dx < 0 && enemyFacingRight) {
            // Moving left, should face left
            enemyFacingRight = false;
            updateEnemySprite();
            System.out.println("Enemy now facing LEFT (dx: " + dx + ")");
        }
    }
    
    // Update enemy sprite with correct direction
    private void updateEnemySprite() {
        if (currentEnemyImage != null) {
            enemyLabel.setIcon(getFlippedEnemyIcon(currentEnemyImage));
        }
    }
} 