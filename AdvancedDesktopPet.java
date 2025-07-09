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
import java.util.Set;
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
    private int currentBehavior = 1; // 0=idle, 1=walking, 2=special, 3=pain - Start in active mode
    
    // Character Set Integration
    private CharacterSetManager characterSetManager;
    private Timer multiFrameAnimationTimer;
    private boolean isPainAnimationActive = false;
    private int painCycleCount = 0; // Track pain animation cycles
    private int maxPainCycles = 3; // Maximum pain cycles before running away
    private boolean isPowerModeActive = false; // Power mode - immune to pain
    private Timer powerModeTimer; // Timer for power mode duration
    
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
    
    // Independent enemy sizing (no longer proportional)
    public int enemyWidth = DEFAULT_WIDTH;
    public int enemyHeight = DEFAULT_HEIGHT;
    
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
    private CharacterImportWindow characterImportWindow = null;
    
    // Language support
    private boolean isChinese = false; // false = English, true = Chinese
    private Map<String, String> englishTexts = new HashMap<>();
    private Map<String, String> chineseTexts = new HashMap<>();
    
    // Music system - now managed by MusicManager
    private static boolean musicEnabled = true;
    
    // Loading screen
    private JWindow loadingWindow;
    private JLabel loadingLabel;
    private JProgressBar loadingProgress;
    private boolean isLoading = true;
    
    // Component references for easy updating
    private JLabel petCountLabel;
    private JLabel enemyInfoLabel;
    private JLabel maxEnemiesLabel;
    private JLabel characterSectionLabel;
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
    private JButton debugBtn;
    private JButton importCharacterBtn;
    private JButton switchCharacterBtn;
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
        MusicManager.updatePetList(allPets); // Update music manager
        

        
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
        loadingLabel = new JLabel("Loading Pet...", JLabel.CENTER);
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
                    JOptionPane.showMessageDialog(null, "Error loading Pet: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        englishTexts.put("settings_title", "Desktop Pet Settings");
        englishTexts.put("pet_management", "Pet Management");
        englishTexts.put("duplicate_pet", "Duplicate Pet");
        englishTexts.put("remove_pet", "Remove Pet");
        englishTexts.put("active_pets", "Active Pets");
        englishTexts.put("transparency", "Transparency");
        englishTexts.put("hide_pet", "Hide Pet");
        englishTexts.put("show_all_pets", "Show All Pets");
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
        englishTexts.put("character_section", "Character Import");
        englishTexts.put("import_character", "Import Characters");
        englishTexts.put("switch_character", "Switch Character");
        englishTexts.put("Debug_Enemy_Count", "Debug Enemy Count");
        
        // Character Import Window translations
        englishTexts.put("import_window_title", "Character Set Import Manager");
        englishTexts.put("character_set_info", "Character Set Information");
        englishTexts.put("type", "Type:");
        englishTexts.put("existing_sets", "Existing Sets:");
        englishTexts.put("set_name", "Set Name:");
        englishTexts.put("author", "Author:");
        englishTexts.put("description", "Description:");
        englishTexts.put("pet_character", "Pet Character");
        englishTexts.put("enemy_character", "Enemy Character");
        englishTexts.put("create_new", "-- Create New --");
        englishTexts.put("animation_import", "Animation Import");
        englishTexts.put("preview", "Preview");
        englishTexts.put("idle_animation", "Idle Animation");
        englishTexts.put("walking_animation", "Walking Animation");
        englishTexts.put("special_animation", "Special Animation");
        englishTexts.put("pain_animation", "Pain Animation");
        englishTexts.put("import_images", "Import Images");
        englishTexts.put("export_set", "Export Set");
        englishTexts.put("save_set", "Save Set");
        englishTexts.put("delete_set", "Delete Set");
        englishTexts.put("test_in_pet", "Test in Pet");
        englishTexts.put("edit_properties", "Edit Properties");
        englishTexts.put("set_as_default", "Set as Default");
        englishTexts.put("no_character_set", "No Character Set");
        englishTexts.put("no_character_set_selected", "No character set selected. Please select or create a character set first.");
        englishTexts.put("default_pet_set", "Default Pet Set");
        englishTexts.put("default_enemy_set", "Default Enemy Set");
        englishTexts.put("set_default_pet", "Set default pet character to: ");
        englishTexts.put("set_default_enemy", "Set default enemy character to: ");
        englishTexts.put("default_char_message", "This character will be used when the program starts.");
        englishTexts.put("default_enemy_message", "This character will be used for new enemies.");
        englishTexts.put("error", "Error");
        englishTexts.put("error_setting_default", "Error setting default character: ");
        
        // Initialize Chinese texts
        chineseTexts.put("settings_title", "\u684c\u9762\u684c\u5ba0\u8bbe\u7f6e");
        chineseTexts.put("pet_management", "\u684c\u5ba0\u7ba1\u7406");
        chineseTexts.put("duplicate_pet", "\u590d\u5236\u684c\u5ba0");
        chineseTexts.put("remove_pet", "\u5220\u9664\u684c\u5ba0");
        chineseTexts.put("active_pets", "\u6d3b\u8dc3\u7684\u684c\u5ba0");
        chineseTexts.put("transparency", "\u900f\u660e\u5ea6");
        chineseTexts.put("hide_pet", "\u9690\u85cf\u684c\u5ba0");
        chineseTexts.put("show_all_pets", "\u663e\u793a\u6240\u6709\u684c\u5ba0");
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
        chineseTexts.put("character_section", "\u89d2\u8272\u5bfc\u5165");
        chineseTexts.put("import_character", "\u5bfc\u5165\u89d2\u8272");
        chineseTexts.put("switch_character", "\u5207\u6362\u89d2\u8272");
        chineseTexts.put("Debug_Enemy_Count", "\u6e21\u5be9\u6574\u6570\u654c\u4eba");
        
        // Character Import Window Chinese translations
        chineseTexts.put("import_window_title", "\u89d2\u8272\u96c6\u5408\u5bfc\u5165\u7ba1\u7406\u5668");
        chineseTexts.put("character_set_info", "\u89d2\u8272\u96c6\u5408\u4fe1\u606f");
        chineseTexts.put("type", "\u7c7b\u578b\uff1a");
        chineseTexts.put("existing_sets", "\u5b58\u5728\u7684\u96c6\u5408\uff1a");
        chineseTexts.put("set_name", "\u96c6\u5408\u540d\u79f0\uff1a");
        chineseTexts.put("author", "\u4f5c\u8005\uff1a");
        chineseTexts.put("description", "\u63cf\u8ff0\uff1a");
        chineseTexts.put("pet_character", "\u5b9c\u7269\u89d2\u8272");
        chineseTexts.put("enemy_character", "\u654c\u4eba\u89d2\u8272");
        chineseTexts.put("create_new", "-- \u521b\u5efa\u65b0\u7684 --");
        chineseTexts.put("animation_import", "\u52a8\u753b\u5bfc\u5165");
        chineseTexts.put("preview", "\u9884\u89c8");
        chineseTexts.put("idle_animation", "\u7a7a\u95f2\u52a8\u753b");
        chineseTexts.put("walking_animation", "\u884c\u8d70\u52a8\u753b");
        chineseTexts.put("special_animation", "\u7279\u6b8a\u52a8\u753b");
        chineseTexts.put("pain_animation", "\u75bc\u75db\u52a8\u753b");
        chineseTexts.put("import_images", "\u5bfc\u5165\u56fe\u7247");
        chineseTexts.put("export_set", "\u5bfc\u51fa\u96c6\u5408");
        chineseTexts.put("save_set", "\u4fdd\u5b58\u96c6\u5408");
        chineseTexts.put("delete_set", "\u5220\u9664\u96c6\u5408");
        chineseTexts.put("test_in_pet", "\u5728\u5b9c\u7269\u4e2d\u6d4b\u8bd5");
        chineseTexts.put("edit_properties", "\u7f16\u8f91\u5c5e\u6027");
        chineseTexts.put("set_as_default", "\u8bbe\u4e3a\u9ed8\u8ba4");
        chineseTexts.put("no_character_set", "\u6ca1\u6709\u89d2\u8272\u96c6\u5408");
        chineseTexts.put("no_character_set_selected", "\u6ca1\u6709\u9009\u62e9\u89d2\u8272\u96c6\u5408\u3002\u8bf7\u5148\u9009\u62e9\u6216\u521b\u5efa\u4e00\u4e2a\u89d2\u8272\u96c6\u5408\u3002");
        chineseTexts.put("default_pet_set", "\u9ed8\u8ba4\u5b9c\u7269\u96c6\u5408");
        chineseTexts.put("default_enemy_set", "\u9ed8\u8ba4\u654c\u4eba\u96c6\u5408");
        chineseTexts.put("set_default_pet", "\u5c06\u9ed8\u8ba4\u5b9c\u7269\u89d2\u8272\u8bbe\u7f6e\u4e3a\uff1a");
        chineseTexts.put("set_default_enemy", "\u5c06\u9ed8\u8ba4\u654c\u4eba\u89d2\u8272\u8bbe\u7f6e\u4e3a\uff1a");
        chineseTexts.put("default_char_message", "\u6b64\u89d2\u8272\u5c06\u5728\u7a0b\u5e8f\u542f\u52a8\u65f6\u4f7f\u7528\u3002");
        chineseTexts.put("default_enemy_message", "\u6b64\u89d2\u8272\u5c06\u7528\u4e8e\u65b0\u7684\u654c\u4eba\u3002");
        chineseTexts.put("error", "\u9519\u8bef");
        chineseTexts.put("error_setting_default", "\u8bbe\u7f6e\u9ed8\u8ba4\u89d2\u8272\u65f6\u51fa\u9519\uff1a");
    }
    
    public String getText(String key) {
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
        if (debugBtn != null) {
            debugBtn.setText(getText("Debug_Enemy_Count"));
        }
        if (characterSectionLabel != null) {
            characterSectionLabel.setText(getText("character_section"));
        }
        if (importCharacterBtn != null) {
            importCharacterBtn.setText(getText("import_character"));
        }
        if (switchCharacterBtn != null) {
            switchCharacterBtn.setText(getText("switch_character"));
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
        System.out.println("Loading animations...");
        specialAnimations = new ArrayList<>();
        
        // Initialize character set manager
        characterSetManager = CharacterSetManager.getInstance();
        
        // Load from character set or fallback to legacy loading
        if (loadFromCharacterSet()) {
            // Loaded animations from character set
        } else {
            System.out.println("Loading legacy animations");
            loadLegacyAnimations();
        }
        
        // Load enemy images
        System.out.println("About to load enemy images...");
        loadEnemyImages();
        
        // Initialize multi-frame animation timer
        initializeMultiFrameAnimationTimer();
        
        // Set initial animation
        SwingUtilities.invokeLater(() -> updateIdleSprite());
    }
    
    /**
     * Load animations from current character set
     */
    private boolean loadFromCharacterSet() {
        try {
            CharacterSet currentSet = characterSetManager.getCurrentPetCharacterSet();
            if (currentSet == null || !currentSet.isComplete()) {
                return false;
            }
            
            // First, determine the optimal size for the character set
            autoResizeForCharacterSet(currentSet);
            
            // Load idle animation
            AnimationSequence idleSeq = currentSet.getIdleAnimation();
            if (idleSeq.getFrameCount() > 0) {
                idleGif = idleSeq.getCurrentFrame().getImage();
            }
            
            // Load walking animation
            AnimationSequence walkingSeq = currentSet.getWalkingAnimation();
            if (walkingSeq.getFrameCount() > 0) {
                walkGif = walkingSeq.getCurrentFrame().getImage();
            }
            
            // Load special animation
            specialAnimations.clear();
            AnimationSequence specialSeq = currentSet.getSpecialAnimation();
            if (specialSeq.getFrameCount() > 0) {
                for (AnimationFrame frame : specialSeq.getFrames()) {
                    specialAnimations.add(frame.getImage());
                }
            }
            
            // Pain animation will be handled separately when triggered
            
            return true;
            
        } catch (Exception e) {
            System.out.println("Error loading from character set: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Auto-resize pet window to fit character set images
     */
    private void autoResizeForCharacterSet(CharacterSet characterSet) {
        try {
            int maxWidth = DEFAULT_WIDTH;
            int maxHeight = DEFAULT_HEIGHT;
            
            // Check all animation sequences to find the largest image dimensions
            AnimationSequence[] sequences = {
                characterSet.getIdleAnimation(),
                characterSet.getWalkingAnimation(),
                characterSet.getSpecialAnimation(),
                characterSet.getPainAnimation()
            };
            
            for (AnimationSequence sequence : sequences) {
                if (sequence != null && sequence.getFrameCount() > 0) {
                    for (AnimationFrame frame : sequence.getFrames()) {
                        if (frame.getImage() != null) {
                            int imageWidth = frame.getImage().getIconWidth();
                            int imageHeight = frame.getImage().getIconHeight();
                            
                            if (imageWidth > maxWidth) maxWidth = imageWidth;
                            if (imageHeight > maxHeight) maxHeight = imageHeight;
                        }
                    }
                }
            }
            
            // Only resize if we found larger images
            if (maxWidth > petWidth || maxHeight > petHeight) {
                System.out.println("Auto-resizing pet window from " + petWidth + "x" + petHeight + 
                                 " to " + maxWidth + "x" + maxHeight + " to fit character set images");
                
                petWidth = maxWidth;
                petHeight = maxHeight;
                
                // Update window and label size
                setSize(petWidth, petHeight);
                if (petLabel != null) {
                    petLabel.setBounds(0, 0, petWidth, petHeight);
                    petLabel.setPreferredSize(new Dimension(petWidth, petHeight));
                }
                
                // Update all enemies' size to match
                for (EnemyWindow enemy : enemies) {
                    enemy.updateFromPetSettings();
                }
                
                // Force repaint
                revalidate();
                repaint();
            }
            
        } catch (Exception e) {
            System.out.println("Error auto-resizing for character set: " + e.getMessage());
        }
    }
    
    /**
     * Fallback to legacy animation loading
     */
    private void loadLegacyAnimations() {
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
    }
    
    /**
     * Initialize timer for multi-frame animations
     */
    private void initializeMultiFrameAnimationTimer() {
        multiFrameAnimationTimer = new Timer(150, e -> updateMultiFrameAnimation());
        multiFrameAnimationTimer.setRepeats(true); // Ensure it repeats
        // Don't start automatically - will be started when needed
    }
    
    /**
     * Update multi-frame animation for character sets
     */
    private void updateMultiFrameAnimation() {
        try {
            CharacterSet currentSet = characterSetManager.getCurrentPetCharacterSet();
            if (currentSet == null) return;
            
            AnimationSequence currentSequence = null;
            
            // Determine which animation sequence to use based on current state
            if (isPainAnimationActive) {
                currentSequence = currentSet.getPainAnimation();
                // If pain animation is missing, fall back to idle
                if (currentSequence.getFrameCount() == 0) {
                    currentSequence = currentSet.getIdleAnimation();
                }
            } else if (isWalking) { // walking - check isWalking state first
                currentSequence = currentSet.getWalkingAnimation();
                // If walking animation is missing, fall back to idle
                if (currentSequence.getFrameCount() == 0) {
                    currentSequence = currentSet.getIdleAnimation();
                }
            } else if (currentBehavior == 2) { // special
                currentSequence = currentSet.getSpecialAnimation();
                // If special animation is missing, fall back to idle
                if (currentSequence.getFrameCount() == 0) {
                    currentSequence = currentSet.getIdleAnimation();
                }
            } else { // idle (default)
                currentSequence = currentSet.getIdleAnimation();
            }
            
            if (currentSequence != null && currentSequence.getFrameCount() > 0) {
                AnimationFrame nextFrame = currentSequence.nextFrame();
                if (nextFrame != null) {
                    ImageIcon frameImage = nextFrame.getImage();
                    if (frameImage != null) {
                        // Apply horizontal flip if needed
                        ImageIcon displayImage = facingRight ? frameImage : getFlippedIcon(frameImage);
                        petLabel.setIcon(displayImage);
                        
                        // Update timer delay based on frame duration
                        int frameDuration = nextFrame.getDuration();
                        if (frameDuration > 0) {
                            multiFrameAnimationTimer.setDelay(frameDuration);
                        }
                        
                        // Debug output for pain animation
                        if (isPainAnimationActive) {
                            System.out.println("Pain animation frame: " + currentSequence.getCurrentFrameIndex() + 
                                             "/" + currentSequence.getFrameCount() + 
                                             " (duration: " + frameDuration + "ms)");
                            
                            // Check if we completed a cycle (reached the end of animation)
                            if (currentSequence.getCurrentFrameIndex() == 0) {
                                painCycleCount++;
                                System.out.println("Pain cycle completed: " + painCycleCount + "/" + maxPainCycles);
                                
                                // Stop pain after 3 cycles and run away
                                if (painCycleCount >= maxPainCycles) {
                                    System.out.println("Pain cycles completed, stopping pain and running away");
                                    stopPainAnimation();
                                }
                            }
                        }
                    }
                }
            }
            
        } catch (Exception ex) {
            System.out.println("Error in multi-frame animation update: " + ex.getMessage());
        }
    }
    
    /**
     * Start pain animation
     */
    /**
     * Check if pet is currently vulnerable to pain
     */
    public boolean isVulnerableToPain() {
        return !isPainAnimationActive && !isPowerModeActive;
    }
    
    public void startPainAnimation() {
        if (isPainAnimationActive) return; // Already in pain animation
        if (isPowerModeActive) {
            System.out.println("Pet is in power mode - immune to pain!");
            return; // Cannot start pain during power mode
        }
        
        isPainAnimationActive = true;
        painCycleCount = 0; // Reset cycle counter
        System.out.println("Starting pain animation (will run for " + maxPainCycles + " cycles)");
        
        // Stop any current movement and prevent new movement during pain
        isWalking = false;
        
        // Stop ALL movement-related timers during pain
        if (movementTimer != null && movementTimer.isRunning()) {
            movementTimer.stop();
            System.out.println("Stopped movement timer during pain");
        }
        
        if (behaviorTimer != null && behaviorTimer.isRunning()) {
            behaviorTimer.stop();
            System.out.println("Stopped behavior timer during pain");
        }
        
        // Stop current animation timers
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        
        // Start multi-frame animation for pain
        if (multiFrameAnimationTimer != null) {
            CharacterSet currentSet = characterSetManager.getCurrentPetCharacterSet();
            if (currentSet != null) {
                if (currentSet.getPainAnimation().getFrameCount() > 0) {
                    currentSet.getPainAnimation().reset(); // Reset to first frame
                    multiFrameAnimationTimer.start();
                    System.out.println("Started pain animation with " + currentSet.getPainAnimation().getFrameCount() + " frames");
                } else {
                    // No pain animation available, just use idle animation during pain
                    System.out.println("No pain animation available, using idle animation");
                    currentSet.getIdleAnimation().reset();
                    multiFrameAnimationTimer.start();
                }
                
                // No fixed timer - pain will stop after 3 cycles
            }
        }
    }
    
    /**
     * Stop pain animation and return to normal
     */
    public void stopPainAnimation() {
        if (!isPainAnimationActive) return;
        
        isPainAnimationActive = false;
        System.out.println("Stopping pain animation");
        
        // Stop multi-frame timer
        if (multiFrameAnimationTimer != null && multiFrameAnimationTimer.isRunning()) {
            multiFrameAnimationTimer.stop();
        }
        
        // Resume normal animation
        if (animationTimer != null && !animationTimer.isRunning()) {
            animationTimer.start();
        }
        
        // Restart movement and behavior timers
        if (movementTimer != null && !movementTimer.isRunning()) {
            movementTimer.start();
            System.out.println("Restarted movement timer after pain");
        }
        
        if (behaviorTimer != null && !behaviorTimer.isRunning()) {
            behaviorTimer.start();
            System.out.println("Restarted behavior timer after pain");
        }
        
        // Start power mode after pain ends
        startPowerMode();
        
        // Trigger a faster escape behavior after pain
        if (currentBehavior == 1) { // If in walking behavior
            // Start a faster escape walk immediately
            Timer postPainEscapeTimer = new Timer(100, e -> {
                if (!isDragging && !isPainAnimationActive) {
                    System.out.println("Starting post-pain escape run");
                    startEscapeRun();
                }
                ((Timer) e.getSource()).stop();
            });
            postPainEscapeTimer.start();
        }
        
        // Update to current behavior sprite
        updateCurrentBehaviorSprite();
    }
    
    /**
     * Start escape run - faster movement away from enemies
     */
    private void startEscapeRun() {
        if (isDragging || isPainAnimationActive) return; // Can still escape during power mode
        
        System.out.println("Starting escape run - faster movement");
        
        // Find the nearest enemy to run away from
        Point enemyLocation = findNearestEnemyLocation();
        if (enemyLocation != null) {
            // Calculate direction away from enemy
            Point current = getLocation();
            int dx = current.x - enemyLocation.x;
            int dy = current.y - enemyLocation.y;
            
            // Normalize direction and set target far away from enemy
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance > 0) {
                dx = (int) ((dx / distance) * 300); // Run 300 pixels away
                dy = (int) ((dy / distance) * 300);
            } else {
                // If enemy is at same position, run in random direction
                dx = random.nextInt(200) - 100;
                dy = random.nextInt(200) - 100;
            }
            
            targetX = current.x + dx;
            targetY = current.y + dy;
        } else {
            // No enemy found, run to random location
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
        
        System.out.println("Escape target set to: (" + targetX + ", " + targetY + ")");
        
        // Determine direction and flip image if needed
        Point current = getLocation();
        boolean shouldFaceRight = targetX > current.x;
        
        if (shouldFaceRight != facingRight) {
            facingRight = shouldFaceRight;
        }
        
        // Ensure walking animation is properly started
        updateWalkingSprite();
        
        // Faster escape timer (30ms instead of 50ms for faster movement)
        Timer escapeTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Stop movement immediately if pain animation is active (but allow during power mode)
                if (isPainAnimationActive) {
                    System.out.println("Stopping escape during pain animation");
                    isWalking = false;
                    updateIdleSprite();
                    ((Timer) e.getSource()).stop();
                    return;
                }
                
                Point current = getLocation();
                int dx = targetX - current.x;
                int dy = targetY - current.y;
                
                if (Math.abs(dx) < 5 && Math.abs(dy) < 5) {
                    System.out.println("Pet reached escape target, stopping escape");
                    isWalking = false;
                    updateIdleSprite();
                    ((Timer) e.getSource()).stop();
                    
                    // Return to normal behavior after escape
                    Timer returnToNormalTimer = new Timer(1000, evt -> {
                        if (!isDragging && !isPainAnimationActive) {
                            System.out.println("Returning to normal behavior after escape");
                            startRandomWalk();
                        }
                        ((Timer) evt.getSource()).stop();
                    });
                    returnToNormalTimer.start();
                    return;
                }
                
                // Faster movement (5 pixels instead of 3)
                int stepX = dx == 0 ? 0 : (dx > 0 ? 5 : -5);
                int stepY = dy == 0 ? 0 : (dy > 0 ? 5 : -5);
                
                Point newLocation = new Point(current.x + stepX, current.y + stepY);
                Point safeNewLocation = ensurePetFullyVisible(newLocation);
                
                // Don't move if pain animation is active
                if (!isPainAnimationActive) {
                    setLocation(safeNewLocation);
                }
                
                // Update walking animation frame for leg sync
                walkAnimationFrame = (walkAnimationFrame + 1) % 8;
                
                // Ensure walking animation is active during movement
                if (isWalking) {
                    updateWalkingSprite();
                }
            }
        });
        escapeTimer.start();
    }
    
    /**
     * Start power mode - pet becomes immune to pain for 3 seconds
     */
    private void startPowerMode() {
        if (isPowerModeActive) return; // Already in power mode
        
        isPowerModeActive = true;
        System.out.println("POWER MODE ACTIVATED! Pet is immune to pain for 3 seconds!");
        
        // Create power mode timer for 3 seconds
        powerModeTimer = new Timer(3000, e -> {
            stopPowerMode();
            ((Timer) e.getSource()).stop();
        });
        powerModeTimer.start();
    }
    
    /**
     * Stop power mode - pet can be hurt again
     */
    private void stopPowerMode() {
        if (!isPowerModeActive) return;
        
        isPowerModeActive = false;
        System.out.println("Power mode ended - pet can be hurt again");
        
        if (powerModeTimer != null) {
            powerModeTimer.stop();
        }
    }
    
    /**
     * Find the location of the nearest enemy
     */
    private Point findNearestEnemyLocation() {
        Point current = getLocation();
        Point nearestEnemy = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (EnemyWindow enemy : enemies) {
            if (enemy != null && enemy.isVisible()) {
                Point enemyLocation = enemy.getLocation();
                if (enemyLocation != null) {
                    double distance = current.distance(enemyLocation);
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestEnemy = enemyLocation;
                    }
                }
            }
        }
        
        return nearestEnemy;
    }
    
    /**
     * Update sprite based on current behavior
     */
    public void updateCurrentBehaviorSprite() {
        switch (currentBehavior) {
            case 0:
                updateIdleSprite();
                break;
            case 1:
                updateWalkingSprite();
                break;
            case 2:
                // Special animation will be handled by playSpecialAnimation
                break;
            default:
                updateIdleSprite();
                break;
        }
    }
    
    /**
     * Switch to a different character set
     */
    public void switchCharacterSet(String setName, boolean isPet) {
        try {
            if (isPet) {
                characterSetManager.setCurrentPetCharacterSet(setName);
                
                // Reload animations with new character set (this will auto-resize if needed)
                loadAnimations();
                
                // Update current sprite
                updateCurrentBehaviorSprite();
            } else {
                characterSetManager.setCurrentEnemyCharacterSet(setName);
                
                // Enemy size is now independent, no recalculation needed
                
                // Reload enemy images with new character set (at proportional size)
                loadEnemyImagesFromCharacterSet();
                
                // Update all existing enemies with new images
                updateExistingEnemiesWithNewImages();
            }
            
            System.out.println("Switched to character set: " + setName + " (Pet: " + isPet + ")");
            
        } catch (Exception e) {
            System.out.println("Error switching character set: " + e.getMessage());
        }
    }
    
    /**
     * Public method to reload animations (for external classes like import window)
     */
    public void reloadAnimations() {
        loadAnimations();
    }
    
    /**
     * Get all active pets (for external classes)
     */
    public static List<AdvancedDesktopPet> getAllPets() {
        return allPets;
    }
    

    
    private void initializeMusic() {
        // Initialize music system using MusicManager
        MusicManager.initialize();
        MusicManager.updatePetList(allPets);
    }
    
    // Music methods now handled by MusicManager
    
    // Getter for enemies list (used by MusicManager)
    public List<EnemyWindow> getEnemies() {
        return enemies;
    }
    
    private static void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        MusicManager.setMusicEnabled(enabled);
    }
    
    private void loadEnemyImages() {
        System.out.println("Loading enemy images...");
        enemyImages.clear();
        
        // Try to load enemy images from Image folder (resources root)
        for (int i = 1; i <= 3; i++) {
            String filename = "Image/enemy0" + i + ".png";
            ImageIcon enemyImage = loadEnemyImageSafely(filename);
            if (enemyImage != null) {
                enemyImages.add(enemyImage);
            }
        }
        
        // Also try alternative naming patterns if none found
        if (enemyImages.isEmpty()) {
            for (int i = 1; i <= 3; i++) {
                String filename = "resources/Image/enemy0" + i + ".png";
                ImageIcon enemyImage = loadEnemyImageSafely(filename);
                if (enemyImage != null) {
                    enemyImages.add(enemyImage);
                }
            }
        }
        
        // If no enemy images found, create default scary enemies
        if (enemyImages.isEmpty()) {
            System.out.println("No enemy images found - using default animations");
            enemyImages.add(createDefaultEnemyAnimation(new Color(255, 0, 0, 200), "X"));
            enemyImages.add(createDefaultEnemyAnimation(new Color(0, 0, 0, 200), "!"));
            enemyImages.add(createDefaultEnemyAnimation(new Color(128, 0, 128, 200), "?"));
        } else {
            System.out.println("Loaded " + enemyImages.size() + " enemy images successfully");
        }
    }
    
    /**
     * Load enemy images from current character set
     */
    private void loadEnemyImagesFromCharacterSet() {
        // Loading enemy images from character set...
        System.out.println("Current pet size: " + petWidth + "x" + petHeight);
        enemyImages.clear();
        
        CharacterSet currentEnemySet = characterSetManager.getCurrentEnemyCharacterSet();
        // Current enemy character set: " + (currentEnemySet != null ? currentEnemySet.getName() : "null")
        
        if (currentEnemySet != null && !currentEnemySet.getName().equals("default")) {
            // Load from character set animations
            if (currentEnemySet.getIdleAnimation().getFrameCount() > 0) {
                for (AnimationFrame frame : currentEnemySet.getIdleAnimation().getFrames()) {
                    if (frame.getOriginalImage() != null) {
                                            // Scale enemy image to independent size
                    Image img = frame.getOriginalImage().getImage();
                    Image scaledImg = img.getScaledInstance(enemyWidth, enemyHeight, Image.SCALE_SMOOTH);
                    enemyImages.add(new ImageIcon(scaledImg));
                    System.out.println("Scaled enemy idle frame to " + enemyWidth + "x" + enemyHeight);
                    }
                }
            }
            
            // Also add walking animation frames if available
            if (currentEnemySet.getWalkingAnimation().getFrameCount() > 0) {
                for (AnimationFrame frame : currentEnemySet.getWalkingAnimation().getFrames()) {
                    if (frame.getOriginalImage() != null) {
                        // Scale enemy image to independent size
                        Image img = frame.getOriginalImage().getImage();
                        Image scaledImg = img.getScaledInstance(enemyWidth, enemyHeight, Image.SCALE_SMOOTH);
                        enemyImages.add(new ImageIcon(scaledImg));
                        System.out.println("Scaled enemy walking frame to " + enemyWidth + "x" + enemyHeight);
                    }
                }
            }
            
            // Add special animation frames if available
            if (currentEnemySet.getSpecialAnimation().getFrameCount() > 0) {
                for (AnimationFrame frame : currentEnemySet.getSpecialAnimation().getFrames()) {
                    if (frame.getOriginalImage() != null) {
                        // Scale enemy image to independent size
                        Image img = frame.getOriginalImage().getImage();
                        Image scaledImg = img.getScaledInstance(enemyWidth, enemyHeight, Image.SCALE_SMOOTH);
                        enemyImages.add(new ImageIcon(scaledImg));
                        System.out.println("Scaled enemy special frame to " + enemyWidth + "x" + enemyHeight);
                    }
                }
            }
            
            // Add pain animation frames if available
            if (currentEnemySet.getPainAnimation().getFrameCount() > 0) {
                for (AnimationFrame frame : currentEnemySet.getPainAnimation().getFrames()) {
                    if (frame.getOriginalImage() != null) {
                        // Scale enemy image to independent size
                        Image img = frame.getOriginalImage().getImage();
                        Image scaledImg = img.getScaledInstance(enemyWidth, enemyHeight, Image.SCALE_SMOOTH);
                        enemyImages.add(new ImageIcon(scaledImg));
                        System.out.println("Scaled enemy pain frame to " + enemyWidth + "x" + enemyHeight);
                    }
                }
            }
        }
        
        // Fallback to default enemy loading if no character set images
        if (enemyImages.isEmpty()) {
            System.out.println("No character set enemy images found, falling back to default loading");
            loadEnemyImages();
        } else {
            // Loaded " + enemyImages.size() + " enemy images from character set: " + currentEnemySet.getName() + " at independent size " + enemyWidth + "x" + enemyHeight
        }
    }
    
    /**
     * Update all existing enemies with new images after character set switch
     */
    private void updateExistingEnemiesWithNewImages() {
        if (enemies.isEmpty() || enemyImages.isEmpty()) {
            return;
        }
        
        // Updating " + enemies.size() + " existing enemies with new character set images
        
        // Update each enemy with new images
        for (EnemyWindow enemy : enemies) {
            try {
                // Update the enemy's image list and refresh its appearance
                enemy.updateEnemyImages(enemyImages);
            } catch (Exception e) {
                System.out.println("Error updating enemy with new images: " + e.getMessage());
            }
        }
    }
    
    /**
     * Calculate enemy-to-pet size ratio based on original image dimensions
     */
    /**
     * Set default enemy size based on current pet size when first enabled
     */
    private void initializeEnemySize() {
        // Only set default enemy size if it hasn't been set yet
        if (enemyWidth == DEFAULT_WIDTH && enemyHeight == DEFAULT_HEIGHT) {
            enemyWidth = petWidth;
            enemyHeight = petHeight;
            System.out.println("Initialized enemy size to current pet size: " + enemyWidth + "x" + enemyHeight);
        }
    }
    
    private ImageIcon loadEnemyImageSafely(String filename) {
        try {
            // First try to load from resources
            java.net.URL resource = getClass().getResource("/" + filename);
            ImageIcon icon = null;
            
            if (resource != null) {
                icon = new ImageIcon(resource);
                System.out.println("Successfully loaded enemy image from resources: " + filename);
            } else {
                // Try alternative resource paths
                String[] resourcePaths = {
                    "/" + filename,
                    "/" + filename.replace("resources/", ""),
                    "/Image/" + filename.substring(filename.lastIndexOf("/") + 1),
                    "/resources/Image/" + filename.substring(filename.lastIndexOf("/") + 1)
                };
                
                for (String path : resourcePaths) {
                    resource = getClass().getResource(path);
                    if (resource != null) {
                        icon = new ImageIcon(resource);
                        System.out.println("Successfully loaded enemy image from resources: " + path);
                        break;
                    }
                }
                
                // If resource loading failed, try to load from current directory
                if (icon == null) {
                File file = new File(filename);
                if (file.exists()) {
                    icon = new ImageIcon(filename);
                        System.out.println("Successfully loaded enemy image from file: " + filename);
                } else {
                        // Try alternative file paths
                    String[] alternatives = {
                            filename.replace("resources/", ""),
                            "Image/" + filename.substring(filename.lastIndexOf("/") + 1),
                            "resources/Image/" + filename.substring(filename.lastIndexOf("/") + 1),
                            filename.replace("/", "\\")
                    };
                    
                    for (String alt : alternatives) {
                        File altFile = new File(alt);
                        if (altFile.exists()) {
                            icon = new ImageIcon(alt);
                                System.out.println("Successfully loaded enemy image from file: " + alt);
                            break;
                            }
                        }
                    }
                }
            }
            
            // Scale the enemy image to independent size
            if (icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(enemyWidth, enemyHeight, Image.SCALE_SMOOTH);
                System.out.println("Scaled legacy enemy image to " + enemyWidth + "x" + enemyHeight);
                return new ImageIcon(scaledImg);
            } else {
                System.out.println("Failed to load enemy image: " + filename);
            }
            
        } catch (Exception e) {
            System.out.println("Error loading enemy image " + filename + ": " + e.getMessage());
        }
        return null;
    }
    
    private ImageIcon createDefaultEnemyAnimation(Color color, String emoji) {
        // Use independent enemy size for default enemy
        BufferedImage image = new BufferedImage(enemyWidth, enemyHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw scary circle background - scale to enemy size
        g2d.setColor(color);
        int circleSize = Math.min(enemyWidth, enemyHeight) - 20;
        int circleX = (enemyWidth - circleSize) / 2;
        int circleY = (enemyHeight - circleSize) / 2;
        g2d.fillOval(circleX, circleY, circleSize, circleSize);
        
        // Add darker border for scary effect
        g2d.setColor(color.darker());
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(circleX, circleY, circleSize, circleSize);
        
        // Draw scary emoji - scale font to enemy size
        Font font = new Font("Segoe UI Emoji", Font.PLAIN, circleSize / 2);
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);
        
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (enemyWidth - fm.stringWidth(emoji)) / 2;
        int textY = (enemyHeight + fm.getAscent()) / 2;
        g2d.drawString(emoji, textX, textY);
        
        g2d.dispose();
        System.out.println("Created default enemy animation at independent size " + enemyWidth + "x" + enemyHeight);
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
        
        // Movement timer - more frequent to keep pet active
        movementTimer = new Timer(2000 + random.nextInt(3000), e -> {
            if (!isDragging && currentBehavior == 1 && !isWalking) {
                System.out.println("Movement timer triggered - starting random walk");
                startRandomWalk();
            }
            movementTimer.setDelay(2000 + random.nextInt(3000));
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
        
        // Safety timer to check if pet is lost or stuck - check every 5 seconds
        safetyTimer = new Timer(5000, e -> {
            checkAndFixPetLocation();
        });
        safetyTimer.start();
        
        // Start enemy system if enabled
        if (enemyEnabled) {
            startEnemySystem();
        }
        
        // Add a movement watchdog timer to ensure pet keeps moving
        Timer movementWatchdog = new Timer(10000, e -> {
            if (!isDragging && !isWalking && currentBehavior == 1) {
                // Movement watchdog: Pet hasn't moved for 10 seconds - forcing movement
                startRandomWalk();
            }
        });
        movementWatchdog.start();
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
        
        // More frequent cleanup - check and fix issues every 10 seconds
        enemyCleanupTimer = new Timer(10000, e -> {
            if (enemyEnabled) {
                // Focus on recovery first, only remove if completely broken
                checkEnemyHealth();
                // Only do aggressive cleanup for truly broken enemies
                cleanupOnlyBrokenEnemies();
                // Check for orphaned enemies (windows that exist but aren't in the list)
                cleanupOrphanedEnemies();
            }
        });
        enemyCleanupTimer.start();
        
        // Initialize enemy size and load enemy images before spawning
        initializeEnemySize();
        loadEnemyImagesFromCharacterSet();
        
        // Spawn initial enemy after a delay
        Timer initialSpawnTimer = new Timer(3000, e -> {
            if (enemyEnabled) {
                spawnEnemy();
            }
            ((Timer) e.getSource()).stop();
        });
        initialSpawnTimer.start();
    }
    
        // Recovery-focused enemy health check - tries to fix issues before removing
    private void checkEnemyHealth() {
        if (enemies.isEmpty()) return;
        
        for (EnemyWindow enemy : enemies) {
            try {
                if (enemy != null && enemy.isVisible() && enemy.isDisplayable()) {
                    // Try to recover enemies with issues
                    if (enemy.hasNullTimers()) {
                        System.out.println("Attempting to recover enemy with null timers: " + enemy.hashCode());
                        enemy.restartTimers();
                    } else if (enemy.isStuck()) {
                        System.out.println("Attempting to recover stuck enemy: " + enemy.hashCode());
                        enemy.restartTimers();
                    }
                }
            } catch (Exception e) {
                System.out.println("Error during enemy health check: " + e.getMessage());
            }
        }
    }
    
    // Only remove enemies that are completely broken and unrecoverable
    private void cleanupOnlyBrokenEnemies() {
        if (enemies.isEmpty()) return;
        
        System.out.println("Checking for completely broken enemies...");
        
        List<EnemyWindow> enemiesToRemove = new ArrayList<>();
        
        for (EnemyWindow enemy : enemies) {
            try {
                boolean shouldRemove = false;
                
                // Only remove if enemy is completely broken
                if (enemy == null) {
                    System.out.println("Found null enemy, marking for removal");
                    shouldRemove = true;
                }
                // Only remove if window is completely disposed
                else if (!enemy.isDisplayable()) {
                    System.out.println("Found disposed enemy, marking for removal");
                    shouldRemove = true;
                }
                // Only remove if location is completely invalid
                else if (enemy.getLocation().x < -50000 || enemy.getLocation().y < -50000) {
                    System.out.println("Found enemy with severely invalid location, marking for removal");
                    shouldRemove = true;
                }
                // Only remove if enemy has been completely non-functional for a very long time
                else if (enemy.hasBeenCompletelyBrokenForTooLong()) {
                    System.out.println("Found enemy that has been completely broken for too long, marking for removal");
                    shouldRemove = true;
                }
                
                if (shouldRemove) {
                    enemiesToRemove.add(enemy);
                }
                
            } catch (Exception e) {
                System.out.println("Error checking enemy status: " + e.getMessage());
                // Only remove if we can't even check the enemy
                enemiesToRemove.add(enemy);
            }
        }
        
        // Remove only completely broken enemies
        for (EnemyWindow enemy : enemiesToRemove) {
            try {
                System.out.println("Removing completely broken enemy: " + (enemy != null ? enemy.hashCode() : "null"));
                
                if (enemy != null) {
                    enemy.stopAllTimers();
                    enemy.setVisible(false);
                    enemy.dispose();
                }
                
                enemies.remove(enemy);
                
            } catch (Exception e) {
                System.out.println("Error removing broken enemy: " + e.getMessage());
                enemies.remove(enemy);
            }
        }
        
        // Switch back to normal music if no enemies remain after cleanup
        if (enemies.isEmpty() && musicEnabled) {
            MusicManager.switchToNormalMusic();
        }
        
        if (!enemiesToRemove.isEmpty()) {
            System.out.println("Removed " + enemiesToRemove.size() + " completely broken enemies. Remaining: " + enemies.size());
        }
    }
    
    private void spawnEnemy() {
        if (!enemyEnabled) {
            System.out.println("Cannot spawn enemy: Enemy system is disabled. Enable it in Settings > Horror Mode > Enable Enemies");
            return;
        }
        
        if (enemies.size() >= maxEnemies || enemyImages.isEmpty()) {
            System.out.println("Cannot spawn enemy: " + enemies.size() + "/" + maxEnemies + " enemies, " + 
                             (enemyImages.isEmpty() ? "no images" : "images available"));
            return;
        }
        
        try {
        System.out.println("Spawning enemy... Current enemies: " + enemies.size());
        
        EnemyWindow enemy = new EnemyWindow(this, enemyImages);
        enemies.add(enemy);
        
        // Immediately switch to horror music if this is the first enemy
        if (enemies.size() == 1 && musicEnabled) {
            MusicManager.switchToHorrorMusic();
        }
        
        // Remove enemy after some time (20-60 seconds)
        Timer despawnTimer = new Timer(20000 + enemyRandom.nextInt(40000), e -> {
                try {
            if (enemies.contains(enemy)) {
                        System.out.println("Despawning enemy...");
                enemy.stopEnemy();
                enemies.remove(enemy);
                
                // Switch back to normal music if no enemies remain
                if (enemies.isEmpty() && musicEnabled) {
                    MusicManager.switchToNormalMusic();
                }
                
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
    
    // Method to get actual count of visible enemy windows vs list count
    public void debugEnemyCounts() {
        try {
            int listCount = enemies.size();
            int actualWindowCount = 0;
            int orphanedCount = 0;
            
            Window[] allWindows = Window.getWindows();
            for (Window window : allWindows) {
                if (window instanceof EnemyWindow) {
                    actualWindowCount++;
                    if (!enemies.contains(window)) {
                        orphanedCount++;
                    }
                }
            }
            
            System.out.println("=== ENEMY COUNT DEBUG ===");
            System.out.println("Enemies in list: " + listCount);
            System.out.println("Actual enemy windows: " + actualWindowCount);
            System.out.println("Orphaned enemy windows: " + orphanedCount);
            System.out.println("=========================");
            
            if (orphanedCount > 0) {
                System.out.println("WARNING: Found " + orphanedCount + " orphaned enemy windows!");
                cleanupOrphanedEnemies();
            }
            
        } catch (Exception e) {
            System.out.println("Error in enemy count debug: " + e.getMessage());
        }
    }
    
    // Enhanced cleanup for stuck or invalid enemies
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
                // Check if enemy is completely broken for too long
                else if (enemy.hasBeenCompletelyBrokenForTooLong()) {
                    System.out.println("Found completely broken enemy, marking for removal");
                    shouldRemove = true;
                }
                // Check if enemy has been running too long (prevent memory leaks)
                else if (enemy.hasBeenRunningTooLong()) {
                    System.out.println("Found enemy running too long, marking for removal");
                    shouldRemove = true;
                }
                // Check if enemy is stuck in one position for too long (more aggressive detection)
                else if (enemy.isStuckForTooLong()) {
                    System.out.println("Found enemy stuck in position, marking for removal");
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
    
    // Enhanced force remove all enemies (emergency cleanup)
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
                    // Force remove from list even if dispose fails
                    enemies.remove(enemy);
                }
            }
            
            // Clear the list completely
            enemies.clear();
            
            // Run garbage collection to clean up any remaining references
            System.gc();
            
            System.out.println("Force cleanup completed. Enemies remaining: " + enemies.size());
            
            // Restart enemy system after a delay if it was enabled
            if (enemyEnabled) {
                Timer restartTimer = new Timer(5000, e -> {
                    System.out.println("Restarting enemy system after force cleanup...");
                    startEnemySystem();
                    ((Timer) e.getSource()).stop();
                });
                restartTimer.start();
            }
        });
    }
    
    // Additional emergency cleanup method for hanging enemies
    public void emergencyCleanupHangingEnemies() {
        System.out.println("Emergency cleanup for hanging enemies...");
        
        // Force cleanup all enemies
        forceRemoveAllEnemies();
        
        // Additional cleanup steps
        SwingUtilities.invokeLater(() -> {
            try {
                // Clear any remaining references
                System.gc();
                
                // Reset enemy system state
                enemyEnabled = false;
                
                // Wait a moment then re-enable if it was enabled
                Timer reenableTimer = new Timer(3000, e -> {
                    if (enemyEnabled) {
                        System.out.println("Re-enabling enemy system after emergency cleanup...");
                        startEnemySystem();
                    }
                    ((Timer) e.getSource()).stop();
                });
                reenableTimer.start();
                
                System.out.println("Emergency cleanup completed");
            } catch (Exception e) {
                System.out.println("Error during emergency cleanup: " + e.getMessage());
            }
        });
    }
    
    // Ultra-aggressive cleanup for persistent hanging enemies
    public void ultraAggressiveCleanup() {
        System.out.println("Ultra-aggressive cleanup for persistent hanging enemies...");
        
        // Stop all timers immediately
        if (enemySpawnTimer != null) {
            enemySpawnTimer.stop();
            enemySpawnTimer = null;
        }
        if (enemyCleanupTimer != null) {
            enemyCleanupTimer.stop();
            enemyCleanupTimer = null;
        }
        
        // Force dispose all enemies on EDT with maximum force
        SwingUtilities.invokeLater(() -> {
            try {
                // Create a copy and clear the list immediately
                List<EnemyWindow> enemiesToKill = new ArrayList<>(enemies);
                enemies.clear();
                
                for (EnemyWindow enemy : enemiesToKill) {
                    try {
                        System.out.println("Ultra-aggressive disposal of enemy: " + enemy.hashCode());
                        
                        // Stop all timers
                        enemy.stopAllTimers();
                        
                        // Force dispose with multiple attempts
                        enemy.setVisible(false);
                        enemy.dispose();
                        
                        // Additional force disposal
                        try {
                            enemy.setVisible(false);
                            enemy.dispose();
                        } catch (Exception ex) {
                            // Ignore errors, just keep trying
                        }
                        
                    } catch (Exception e) {
                        System.out.println("Error in ultra-aggressive cleanup: " + e.getMessage());
                    }
                }
                
                // Force garbage collection multiple times
                System.gc();
                System.gc();
                System.gc();
                
                System.out.println("Ultra-aggressive cleanup completed. Enemies remaining: " + enemies.size());
                
                // Restart enemy system after a longer delay
                if (enemyEnabled) {
                    Timer restartTimer = new Timer(10000, e -> {
                        System.out.println("Restarting enemy system after ultra-aggressive cleanup...");
                        startEnemySystem();
                        ((Timer) e.getSource()).stop();
                    });
                    restartTimer.start();
                }
                
            } catch (Exception e) {
                System.out.println("Error during ultra-aggressive cleanup: " + e.getMessage());
            }
        });
    }
    
    // Find and clean up orphaned enemy windows (enemies that exist but aren't in the list)
    public void cleanupOrphanedEnemies() {
        System.out.println("Searching for orphaned enemy windows...");
        
        try {
            // Get all windows owned by this application
            Window[] allWindows = Window.getWindows();
            int orphanedCount = 0;
            
            for (Window window : allWindows) {
                // Check if this is an orphaned enemy window
                if (window instanceof EnemyWindow && !enemies.contains(window)) {
                    EnemyWindow orphanedEnemy = (EnemyWindow) window;
                    System.out.println("Found orphaned enemy window: " + orphanedEnemy.hashCode());
                    
                    try {
                        // Stop all timers
                        orphanedEnemy.stopAllTimers();
                        
                        // Force dispose
                        orphanedEnemy.setVisible(false);
                        orphanedEnemy.dispose();
                        
                        orphanedCount++;
                        System.out.println("Disposed orphaned enemy: " + orphanedEnemy.hashCode());
                        
                    } catch (Exception e) {
                        System.out.println("Error disposing orphaned enemy: " + e.getMessage());
                    }
                }
            }
            
            if (orphanedCount > 0) {
                System.out.println("Cleaned up " + orphanedCount + " orphaned enemy windows");
                // Force garbage collection
                System.gc();
            } else {
                System.out.println("No orphaned enemy windows found");
            }
            
        } catch (Exception e) {
            System.out.println("Error during orphaned enemy cleanup: " + e.getMessage());
        }
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
            MusicManager.setEnemySystemStarting(true); // Set flag to prevent music switching interference
            startEnemySystem();
            createScreenFlashEffect(); // Horror effect when enabled
            // Immediately switch to horror music
            MusicManager.switchToHorrorMusic();
            
            // Clear the flag after enemies have had time to spawn
            Timer clearFlagTimer = new Timer(5000, e -> {
                MusicManager.setEnemySystemStarting(false);
                System.out.println("Enemy system startup complete - music switching re-enabled");
                ((Timer) e.getSource()).stop();
            });
            clearFlagTimer.start();
            
        } else {
            System.out.println("Enemy system disabled. Stopping all enemies...");
            MusicManager.setEnemySystemStarting(false); // Clear flag immediately when disabling
            stopEnemySystem();
            // Immediately switch back to normal music
            MusicManager.switchToNormalMusic();
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
            
            // If pet hasn't moved much and isn't being dragged
            if (distance < 5 && !isDragging) {
                stuckCounter++;
                
                // If stuck for more than 2 safety checks (10 seconds), help it
                if (stuckCounter >= 2) {
                    // Pet appears to be idle for too long! Encouraging movement...
                    
                    // Force movement based on current behavior
                    if (currentBehavior == 1) {
                                    // Pet should be walking - restart movement
                        startRandomWalk();
                    } else if (currentBehavior == 0) {
                                    // Pet is idle - change to walking mode
                        currentBehavior = 1;
                        startRandomWalk();
                    }
                    
                    stuckCounter = 0; // Reset counter
                }
            } else {
                stuckCounter = 0; // Reset if pet is moving
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
        if (isDragging || isPainAnimationActive) return; // Don't start walking during pain
        
        // Starting random walk for pet...
        
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
        
        System.out.println("Movement target set to: (" + targetX + ", " + targetY + ")");
        
        // Determine direction and flip image if needed
        Point current = getLocation();
        boolean shouldFaceRight = targetX > current.x;
        
        if (shouldFaceRight != facingRight) {
            facingRight = shouldFaceRight;
        }
        
        // Ensure walking animation is properly started
        updateWalkingSprite();
        
        Timer walkTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Stop movement immediately if pain animation is active
                if (isPainAnimationActive) {
                    System.out.println("Stopping movement during pain animation");
                    isWalking = false;
                    updateIdleSprite();
                    ((Timer) e.getSource()).stop();
                    return;
                }
                
                Point current = getLocation();
                int dx = targetX - current.x;
                int dy = targetY - current.y;
                
                if (Math.abs(dx) < 5 && Math.abs(dy) < 5) {
                    // Pet reached target, stopping movement
                    isWalking = false;
                    updateIdleSprite();
                    ((Timer) e.getSource()).stop();
                    
                    // Schedule next movement automatically if in walking behavior - shorter delay
                    if (currentBehavior == 1 && !isDragging && !isPainAnimationActive) {
                        Timer nextMovementTimer = new Timer(1000 + random.nextInt(2000), evt -> {
                            if (!isDragging && currentBehavior == 1 && !isPainAnimationActive) {
                                System.out.println("Auto-starting next movement after reaching target");
                                startRandomWalk();
                            }
                            ((Timer) evt.getSource()).stop();
                        });
                        nextMovementTimer.start();
                    }
                    return;
                }
                
                int stepX = dx == 0 ? 0 : (dx > 0 ? 3 : -3);
                int stepY = dy == 0 ? 0 : (dy > 0 ? 3 : -3);
                
                Point newLocation = new Point(current.x + stepX, current.y + stepY);
                Point safeNewLocation = ensurePetFullyVisible(newLocation);
                
                // Check if we're stuck (not making progress towards target)
                if (safeNewLocation.equals(current)) {
                    // We're stuck, pick a new target
                    // Pet is stuck trying to reach target, picking new target...
                    ((Timer) e.getSource()).stop();
                    isWalking = false;
                    updateIdleSprite();
                    
                    // Start a new walk after a short delay
                    Timer retryTimer = new Timer(1000, evt -> {
                        if (!isDragging && !isPainAnimationActive) {
                            startRandomWalk();
                        }
                        ((Timer) evt.getSource()).stop();
                    });
                    retryTimer.start();
                    return;
                }
                
                // Don't move if pain animation is active
                if (!isPainAnimationActive) {
                    setLocation(safeNewLocation);
                }
                
                // Update walking animation frame for leg sync (for legacy animations only)
                walkAnimationFrame = (walkAnimationFrame + 1) % 8;
                
                // Ensure walking animation is active during movement
                if (isWalking) {
                    updateWalkingSprite();
                }
                
                // Only update walking sprite for legacy animations (character sets handle their own timing)
                CharacterSet currentSet = characterSetManager.getCurrentPetCharacterSet();
                if (currentSet == null || currentSet.getWalkingAnimation().getFrameCount() == 0) {
                    // Legacy animation - update sprite every 4 frames for leg movement
                    if (walkAnimationFrame % 4 == 0) {
                        updateWalkingSprite();
                    }
                } else {
                    // Character set animations are handled by the multi-frame animation timer
                    if (walkAnimationFrame % 20 == 0) { // Debug every 20 frames to avoid spam
                        // Using character set animation for movement (frame " + walkAnimationFrame + ")
                    }
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
        // Check if we have character sets available for multi-frame animation
        CharacterSet currentSet = characterSetManager.getCurrentPetCharacterSet();
        if (currentSet != null && currentSet.getWalkingAnimation().getFrameCount() > 0) {
            // Use character set multi-frame animation
            if (isWalking) {
                // Pet is walking - ensure walking animation is active
                if (!multiFrameAnimationTimer.isRunning()) {
                    currentSet.getWalkingAnimation().reset();
                    multiFrameAnimationTimer.start();
                    // Started multi-frame walking animation for character set: " + currentSet.getName()
                }
                // If timer is already running and we're walking, it will continue with walking frames
            } else {
                // Pet is not walking - stop walking animation if it's running
                if (multiFrameAnimationTimer.isRunning()) {
                    multiFrameAnimationTimer.stop();
                    System.out.println("Stopped multi-frame walking animation (not walking anymore)");
                }
            }
        } else {
            // Fall back to legacy animation system
            System.out.println("Using legacy animation for walking (currentSet: " + 
                             (currentSet != null ? currentSet.getName() + " with " + currentSet.getWalkingAnimation().getFrameCount() + " frames" : "null") + ")");
            ImageIcon sprite = (walkAnimationFrame % 8 < 4) ? walkGif : idleGif; // Alternate between walk and idle for leg movement
            petLabel.setIcon(getFlippedIcon(sprite));
        }
    }
    
    private void updateIdleSprite() {
        // Only stop multi-frame animation if we're actually going to idle (not walking)
        if (multiFrameAnimationTimer.isRunning() && !isWalking && !isPainAnimationActive) {
            multiFrameAnimationTimer.stop();
            System.out.println("Stopped multi-frame animation");
        }
        
        // Only update idle sprite if we're actually idle (not walking and not in pain)
        if (!isWalking && !isPainAnimationActive) {
            // Check if we have character sets available
            CharacterSet currentSet = characterSetManager.getCurrentPetCharacterSet();
            if (currentSet != null && currentSet.getIdleAnimation().getFrameCount() > 0) {
                // Use character set idle animation
                AnimationFrame idleFrame = currentSet.getIdleAnimation().getCurrentFrame();
                if (idleFrame != null) {
                    petLabel.setIcon(getFlippedIcon(idleFrame.getImage()));
                } else {
                    // Fallback to legacy
        petLabel.setIcon(getFlippedIcon(idleGif));
                }
            } else {
                // Fall back to legacy animation system
                petLabel.setIcon(getFlippedIcon(idleGif));
            }
        }
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
        // Check if we have character sets available
        CharacterSet currentSet = characterSetManager.getCurrentPetCharacterSet();
        if (currentSet != null && currentSet.getSpecialAnimation().getFrameCount() > 0) {
            // Use character set special animation
            currentBehavior = 2; // Set to special behavior
            
            // Stop current animation and start special animation
            if (multiFrameAnimationTimer.isRunning()) {
                multiFrameAnimationTimer.stop();
            }
            
            currentSet.getSpecialAnimation().reset();
            multiFrameAnimationTimer.start();
            
            // Auto-return to idle after special animation
            Timer resetTimer = new Timer(2000, e -> {
                currentBehavior = 0; // Return to idle
                updateIdleSprite();
                ((Timer) e.getSource()).stop();
            });
            resetTimer.start();
            
        } else if (currentSet != null) {
            // Character set exists but has no special animation - just briefly change to idle and back
            // Character set has no special animation, skipping...
            return; // Skip special animation entirely
        } else {
            // Fall back to legacy special animation
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
                            // Pet was cut off on right edge. Pet right: " + petRight + ", Screen right: " + (screenBounds.x + screenBounds.width)
        }
        
        // Check bottom edge (pet extends beyond bottom edge)
        if (petBottom > screenBounds.y + screenBounds.height) {
            newY = screenBounds.y + screenBounds.height - petHeight;
            needsRepositioning = true;
                            // Pet was cut off on bottom edge. Pet bottom: " + petBottom + ", Screen bottom: " + (screenBounds.y + screenBounds.height)
        }
        
        // Check left edge (pet extends beyond left edge)
        if (newX < screenBounds.x) {
            newX = screenBounds.x;
            needsRepositioning = true;
                            // Pet was cut off on left edge. Pet left: " + currentLocation.x + ", Screen left: " + screenBounds.x
        }
        
        // Check top edge (pet extends beyond top edge)
        if (newY < screenBounds.y) {
            newY = screenBounds.y;
            needsRepositioning = true;
                            // Pet was cut off on top edge. Pet top: " + currentLocation.y + ", Screen top: " + screenBounds.y
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
        MusicManager.updatePetList(allPets); // Update music manager
        
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
        
        // Reload and rescale images (both legacy and character sets)
        reloadImagesWithNewSize();
        rescaleCharacterSetImages();
        
        // Enemy size is now independent - no calculation needed
        // Simply reload enemy images at their current independent size
        loadEnemyImagesFromCharacterSet();
        
        // Update all enemies' size and give them new properly-sized images
        for (EnemyWindow enemy : enemies) {
            enemy.updateFromPetSettings();
            // Also update enemy with newly-sized images
            enemy.updateEnemyImages(enemyImages);
        }
        
        // Stop current walking to prevent conflicts
        isWalking = false;
        updateIdleSprite();
        
        // Force repaint to ensure proper display
        revalidate();
        repaint();
    }
    
    private void updateEnemySize(int zoomPercent) {
        // Calculate new enemy size based on percentage
        int newEnemyWidth = (int) (DEFAULT_WIDTH * (zoomPercent / 100.0));
        int newEnemyHeight = (int) (DEFAULT_HEIGHT * (zoomPercent / 100.0));
        
        System.out.println("Updating enemy size to " + zoomPercent + "% - " + newEnemyWidth + "x" + newEnemyHeight);
        
        // Update enemy size variables
        enemyWidth = newEnemyWidth;
        enemyHeight = newEnemyHeight;
        
        // Reload enemy images at new size
        loadEnemyImagesFromCharacterSet();
        
        // Update all existing enemies with new size and images
        for (EnemyWindow enemy : enemies) {
            enemy.updateFromPetSettings();
            enemy.updateEnemyImages(enemyImages);
        }
        
        System.out.println("Enemy size updated successfully to " + enemyWidth + "x" + enemyHeight);
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
    
    /**
     * Rescale character set images to match new pet size
     */
    private void rescaleCharacterSetImages() {
        try {
            CharacterSet currentSet = characterSetManager.getCurrentPetCharacterSet();
            if (currentSet == null || !currentSet.isComplete()) {
                return; // No character set to rescale
            }
            
            // Rescaling character set images to " + petWidth + "x" + petHeight
            
            // Rescale all animation sequences
            rescaleAnimationSequence(currentSet.getIdleAnimation());
            rescaleAnimationSequence(currentSet.getWalkingAnimation());
            rescaleAnimationSequence(currentSet.getSpecialAnimation());
            rescaleAnimationSequence(currentSet.getPainAnimation());
            
            // Reload the animations into the main variables
            loadFromCharacterSet();
            
        } catch (Exception e) {
            System.out.println("Error rescaling character set images: " + e.getMessage());
        }
    }
    
    /**
     * Rescale all frames in an animation sequence using original high-quality images
     */
    private void rescaleAnimationSequence(AnimationSequence sequence) {
        if (sequence == null || sequence.getFrameCount() == 0) {
            return;
        }
        
        try {
            // Scale each frame using its original image (preserves quality)
            for (AnimationFrame frame : sequence.getFrames()) {
                if (frame.getOriginalImage() != null) {
                    frame.scaleToSize(petWidth, petHeight);
                }
            }
            
            // Reset sequence to first frame
            sequence.reset();
            
        } catch (Exception e) {
            System.out.println("Error rescaling animation sequence: " + e.getMessage());
        }
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
        settingsWindow.setSize(800, 650);
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
                                                           800, 650, new Color(35, 20, 45));
                g2d.setPaint(bgGradient);
                g2d.fillRect(0, 0, 800, 650);
                
                // Border
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(80, 80, 120));
                g2d.drawRect(1, 1, 798, 648);
                
                // Title bar
                g2d.setColor(new Color(60, 60, 100, 100));
                g2d.fillRect(0, 0, 800, 30);
                g2d.setStroke(new BasicStroke(1));
                g2d.setColor(new Color(100, 100, 140));
                g2d.drawLine(0, 30, 800, 30);
            }
        };
        
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15)); // Remove top border
        
        // Title panel with minimize button
        JPanel titlePanel = createTitlePanel();
        titlePanel.setPreferredSize(new Dimension(800, 30)); // Ensure it covers the top bar
        
        // Content panel with grid layout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 30, 15, 30); // Much more generous spacing
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
        gbc.insets = new Insets(10, 30, 20, 30); // Extra bottom spacing for sliders
        JSlider transparencySlider = createSlider(0, 100, (int)(transparency * 100));
        transparencySlider.setPreferredSize(new Dimension(500, 50));
        transparencySlider.addChangeListener(e -> {
            transparency = transparencySlider.getValue() / 100.0f;
            updateTransparency();
        });
        contentPanel.add(transparencySlider, gbc);
        gbc.weightx = 0;
        gbc.insets = new Insets(15, 30, 15, 30); // Reset to normal spacing
        
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
        gbc.insets = new Insets(10, 30, 20, 30); // Extra bottom spacing for sliders
        JSlider zoomSlider = createSlider(50, 300, (int)((petWidth / (double)DEFAULT_WIDTH) * 100));
        zoomSlider.setPreferredSize(new Dimension(500, 50));
        zoomSlider.addChangeListener(e -> {
            int zoomPercent = zoomSlider.getValue();
            updateSize(zoomPercent);
        });
        contentPanel.add(zoomSlider, gbc);
        gbc.weightx = 0;
        gbc.insets = new Insets(15, 30, 15, 30); // Reset to normal spacing
        
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
        
        // Enemy Size Section
        addSection(contentPanel, gbc, 11, "Enemy Size (50-300%)");
        
        gbc.gridx = 0; gbc.gridy = 12; gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 30, 20, 30); // Extra bottom spacing for sliders
        JSlider enemySizeSlider = createSlider(50, 300, (int)((enemyWidth / (double)DEFAULT_WIDTH) * 100));
        enemySizeSlider.setPreferredSize(new Dimension(500, 50));
        enemySizeSlider.addChangeListener(e -> {
            int enemySizePercent = enemySizeSlider.getValue();
            updateEnemySize(enemySizePercent);
        });
        contentPanel.add(enemySizeSlider, gbc);
        gbc.weightx = 0;
        gbc.insets = new Insets(15, 30, 15, 30); // Reset to normal spacing
        
        gbc.gridx = 0; gbc.gridy = 13; gbc.gridwidth = 1;
        JButton enemyZoomInBtn = createButton("Enemy +25%");
        enemyZoomInBtn.addActionListener(e -> {
            int currentZoom = (int)((enemyWidth / (double)DEFAULT_WIDTH) * 100);
            int newZoom = Math.min(300, currentZoom + 25);
            enemySizeSlider.setValue(newZoom);
        });
        contentPanel.add(enemyZoomInBtn, gbc);
        
        gbc.gridx = 1; gbc.gridy = 13;
        JButton enemyZoomOutBtn = createButton("Enemy -25%");
        enemyZoomOutBtn.addActionListener(e -> {
            int currentZoom = (int)((enemyWidth / (double)DEFAULT_WIDTH) * 100);
            int newZoom = Math.max(50, currentZoom - 25);
            enemySizeSlider.setValue(newZoom);
        });
        contentPanel.add(enemyZoomOutBtn, gbc);
        
        // Movement Section
        movementSectionLabel = addSection(contentPanel, gbc, 14, getText("movement_settings"));
        
        gbc.gridx = 0; gbc.gridy = 15; gbc.gridwidth = 1;
        crossScreenBox = createCheckBox(getText("allow_cross_screen"), allowCrossScreen);
        crossScreenBox.addActionListener(e -> allowCrossScreen = crossScreenBox.isSelected());
        contentPanel.add(crossScreenBox, gbc);
        
        gbc.gridx = 1; gbc.gridy = 15;
        testCrossScreenBtn = createButton(getText("test_cross_screen"));
        testCrossScreenBtn.addActionListener(e -> moveToRandomScreen());
        contentPanel.add(testCrossScreenBtn, gbc);
        
        gbc.gridx = 0; gbc.gridy = 16; gbc.gridwidth = 1;
        musicBox = createCheckBox(getText("music_enabled"), musicEnabled);
        musicBox.addActionListener(e -> {
            setMusicEnabled(musicBox.isSelected());
        });
        contentPanel.add(musicBox, gbc);
        
        // Horror Section
        horrorSectionLabel = addSection(contentPanel, gbc, 17, getText("horror_mode"));
        
        gbc.gridx = 0; gbc.gridy = 18; gbc.gridwidth = 1;
        enemyBox = createCheckBox(getText("enable_enemies"), enemyEnabled);
        enemyBox.addActionListener(e -> toggleEnemySystem(enemyBox.isSelected()));
        contentPanel.add(enemyBox, gbc);
        
        // Add warning label if enemy system is disabled
        if (!enemyEnabled) {
            gbc.gridx = 0; gbc.gridy = 19; gbc.gridwidth = 2;
            gbc.insets = new Insets(2, 30, 2, 5);
            JLabel warningLabel = createLabel("WARNING: Enemy system is disabled. Enable above to see imported enemies.");
            warningLabel.setForeground(Color.ORANGE);
            contentPanel.add(warningLabel, gbc);
            gbc.insets = new Insets(5, 30, 5, 30);
        }
        
        gbc.gridx = 1; gbc.gridy = 18;
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
        
        gbc.gridx = 0; gbc.gridy = 20; gbc.gridwidth = 2;
        enemyInfoLabel = createLabel(getText("enemies") + ": " + enemies.size() + " / " + maxEnemies + " active");
        contentPanel.add(enemyInfoLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 21; gbc.gridwidth = 2;
        maxEnemiesLabel = createLabel(getText("max_enemies") + ": " + maxEnemies);
        contentPanel.add(maxEnemiesLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 22; gbc.gridwidth = 2;
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
        
        gbc.gridx = 0; gbc.gridy = 23; gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        clearEnemiesBtn = createButton(getText("clear_all_enemies"));
        clearEnemiesBtn.addActionListener(e -> stopEnemySystem());
        contentPanel.add(clearEnemiesBtn, gbc);
        
        gbc.gridx = 1; gbc.gridy = 23;
        gbc.weightx = 1.0;
        forceCleanupBtn = createButton(getText("force_cleanup"));
        forceCleanupBtn.addActionListener(e -> {
            System.out.println("Force cleanup button pressed");
            forceRemoveAllEnemies();
            // Also clean up orphaned enemies immediately
            cleanupOrphanedEnemies();
            // Also try ultra-aggressive cleanup if force cleanup doesn't work
            Timer ultraCleanupTimer = new Timer(5000, evt -> {
                if (!enemies.isEmpty()) {
                    System.out.println("Force cleanup didn't work, trying ultra-aggressive cleanup");
                    ultraAggressiveCleanup();
                }
                // Check for orphaned enemies again after ultra-aggressive cleanup
                cleanupOrphanedEnemies();
                ((Timer) evt.getSource()).stop();
            });
            ultraCleanupTimer.start();
        });
        contentPanel.add(forceCleanupBtn, gbc);
        
        gbc.gridx = 0; gbc.gridy = 24; gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        debugBtn = createButton(getText("Debug_Enemy_Count"));
        debugBtn.addActionListener(e -> debugEnemyCounts());
        contentPanel.add(debugBtn, gbc);
        gbc.weightx = 0; // Reset weight
        
        // Character Import Section
        characterSectionLabel = addSection(contentPanel, gbc, 25, getText("character_section"));
        
        gbc.gridx = 0; gbc.gridy = 26; gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        importCharacterBtn = createButton(getText("import_character"));
        importCharacterBtn.addActionListener(e -> openCharacterImportWindow());
        contentPanel.add(importCharacterBtn, gbc);
        
        gbc.gridx = 1; gbc.gridy = 26;
        gbc.weightx = 1.0;
        switchCharacterBtn = createButton(getText("switch_character"));
        switchCharacterBtn.addActionListener(e -> showCharacterSwitchDialog());
        contentPanel.add(switchCharacterBtn, gbc);
        
        gbc.gridx = 0; gbc.gridy = 27; gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        closeBtn = createButton(getText("close"));
        closeBtn.addActionListener(e -> {
            settingsWindow.setVisible(false);
        });
        contentPanel.add(closeBtn, gbc);
        
        gbc.gridx = 1; gbc.gridy = 27;
        gbc.weightx = 1.0;
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
        button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14)); // Larger font
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); // More padding
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 40)); // Larger button size
        
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
        label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14)); // Larger font
        label.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); // More padding
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
        gbc.insets = new Insets(30, 30, 15, 30); // Extra top spacing for sections
        JLabel sectionTitle = createLabel(title);
        sectionTitle.setFont(new Font("Microsoft YaHei", Font.BOLD, 16)); // Larger font
        sectionTitle.setForeground(new Color(200, 200, 255)); // Slightly highlighted
        panel.add(sectionTitle, gbc);
        gbc.insets = new Insets(15, 30, 15, 30); // Reset to normal spacing
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
    
    /**
     * Open the character import window
     */
    private void openCharacterImportWindow() {
        System.out.println("Opening character import window...");
        
        // Debug: Check timer states before opening window
        debugTimerStates("Before opening import window");
        
        if (characterImportWindow == null) {
            characterImportWindow = new CharacterImportWindow(this);
            
            // Add window listener to ensure pet continues moving when import window is active
            characterImportWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    System.out.println("Import window opened - ensuring pet timers remain active");
                    ensureTimersActive();
                }
                
                @Override
                public void windowClosed(WindowEvent e) {
                    // Import window closed - pet should continue moving
                    ensureTimersActive();
                }
                
                @Override
                public void windowIconified(WindowEvent e) {
                    System.out.println("Import window iconified - ensuring pet timers remain active");
                    ensureTimersActive();
                }
                
                @Override
                public void windowDeiconified(WindowEvent e) {
                    System.out.println("Import window deiconified - ensuring pet timers remain active");
                    ensureTimersActive();
                }
            });
        }
        
        characterImportWindow.setVisible(true);
        characterImportWindow.toFront();
        
        // Debug: Check timer states after opening window
        debugTimerStates("After opening import window");
        
        // Ensure timers remain active
        ensureTimersActive();
    }
    
    /**
     * Debug method to check timer states
     */
    private void debugTimerStates(String context) {
        System.out.println("=== Timer States (" + context + ") ===");
        System.out.println("Animation timer running: " + (animationTimer != null && animationTimer.isRunning()));
        System.out.println("Movement timer running: " + (movementTimer != null && movementTimer.isRunning()));
        System.out.println("Behavior timer running: " + (behaviorTimer != null && behaviorTimer.isRunning()));
        System.out.println("Safety timer running: " + (safetyTimer != null && safetyTimer.isRunning()));
        System.out.println("Multi-frame animation timer running: " + (multiFrameAnimationTimer != null && multiFrameAnimationTimer.isRunning()));
        System.out.println("Current behavior: " + currentBehavior + " (0=idle, 1=walking, 2=special, 3=pain)");
        System.out.println("Is walking: " + isWalking);
        System.out.println("Is dragging: " + isDragging);
        System.out.println("================================");
    }
    
    /**
     * Ensure all timers remain active
     */
    public void ensureTimersActive() {
        System.out.println("Ensuring timers remain active...");
        
        // Restart animation timer if stopped
        if (animationTimer == null || !animationTimer.isRunning()) {
            System.out.println("Restarting animation timer");
            if (animationTimer != null) animationTimer.stop();
            animationTimer = new Timer(ANIMATION_DELAY, e -> updateAnimation());
            animationTimer.start();
        }
        
        // Restart movement timer if stopped
        if (movementTimer == null || !movementTimer.isRunning()) {
            System.out.println("Restarting movement timer");
            if (movementTimer != null) movementTimer.stop();
            movementTimer = new Timer(2000 + random.nextInt(3000), e -> {
                if (!isDragging && currentBehavior == 1 && !isWalking) {
                    System.out.println("Movement timer triggered - starting random walk");
                    startRandomWalk();
                }
                movementTimer.setDelay(2000 + random.nextInt(3000));
            });
            movementTimer.start();
        }
        
        // Restart behavior timer if stopped
        if (behaviorTimer == null || !behaviorTimer.isRunning()) {
            System.out.println("Restarting behavior timer");
            if (behaviorTimer != null) behaviorTimer.stop();
            behaviorTimer = new Timer(15000 + random.nextInt(20000), e -> {
                if (!isDragging && random.nextInt(3) == 0) {
                    playSpecialAnimation();
                }
                behaviorTimer.setDelay(15000 + random.nextInt(20000));
            });
            behaviorTimer.start();
        }
        
        // Restart safety timer if stopped
        if (safetyTimer == null || !safetyTimer.isRunning()) {
            System.out.println("Restarting safety timer");
            if (safetyTimer != null) safetyTimer.stop();
            safetyTimer = new Timer(5000, e -> {
                checkAndFixPetLocation();
            });
            safetyTimer.start();
        }
        
        // If pet is supposed to be walking but isn't, restart movement
        if (currentBehavior == 1 && !isWalking && !isDragging) {
            startRandomWalk();
        }
        
        // Also check if multi-frame animation timer should be running
        CharacterSet currentSet = characterSetManager.getCurrentPetCharacterSet();
        if (currentSet != null && isWalking) {
            if (currentSet.getWalkingAnimation().getFrameCount() > 0 && !multiFrameAnimationTimer.isRunning()) {
                // Pet is walking but multi-frame animation not running - restarting animation
                currentSet.getWalkingAnimation().reset();
                multiFrameAnimationTimer.start();
            }
        }
        
        System.out.println("Timer restart complete");
    }
    
    /**
     * Show dialog to switch between character sets
     */
    private void showCharacterSwitchDialog() {
        CharacterSetManager manager = CharacterSetManager.getInstance();
        
        // Get available character sets
        Set<String> petSets = manager.getPetCharacterSetNames();
        Set<String> enemySets = manager.getEnemyCharacterSetNames();
        
        if (petSets.isEmpty() && enemySets.isEmpty()) {
            JOptionPane.showMessageDialog(settingsWindow, 
                "No character sets available. Import some characters first!", 
                "No Character Sets", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Create options for selection
        String[] options = {"Switch Pet Characters", "Switch Enemy Characters", "Set Default Pet", "Set Default Enemy", "Cancel"};
        int choice = JOptionPane.showOptionDialog(settingsWindow, 
            "What would you like to do with character sets?", 
            "Character Management", 
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            options, 
            options[0]);
        
        if (choice == 0) { // Switch Pet Characters
            if (petSets.isEmpty()) {
                JOptionPane.showMessageDialog(settingsWindow, 
                    "No pet character sets available.", 
                    "No Pet Characters", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            String selectedSet = showCharacterSelectionDialog(settingsWindow, "Switch Pet Character", petSets, true);
            
            if (selectedSet != null) {
                switchCharacterSet(selectedSet, true);
                JOptionPane.showMessageDialog(settingsWindow, 
                    "Switched to pet character set: " + selectedSet, 
                    "Character Switch Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } else if (choice == 1) { // Switch Enemy Characters
            if (enemySets.isEmpty()) {
                JOptionPane.showMessageDialog(settingsWindow, 
                    "No enemy character sets available.", 
                    "No Enemy Characters", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            String selectedSet = showCharacterSelectionDialog(settingsWindow, "Switch Enemy Character", enemySets, false);
            
            if (selectedSet != null) {
                switchCharacterSet(selectedSet, false);
                JOptionPane.showMessageDialog(settingsWindow, 
                    "Switched to enemy character set: " + selectedSet, 
                    "Character Switch Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } else if (choice == 2) { // Set Default Pet
            if (petSets.isEmpty()) {
                JOptionPane.showMessageDialog(settingsWindow, 
                    "No pet character sets available.", 
                    "No Pet Characters", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            String selectedSet = showCharacterSelectionDialog(settingsWindow, "Set Default Pet Character", petSets, true);
            
            if (selectedSet != null) {
                manager.setDefaultPetCharacterSet(selectedSet);
                JOptionPane.showMessageDialog(settingsWindow, 
                    "Set default pet character set to: " + selectedSet + "\nThis will be used when the program starts.", 
                    "Default Set Successfully", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } else if (choice == 3) { // Set Default Enemy
            if (enemySets.isEmpty()) {
                JOptionPane.showMessageDialog(settingsWindow, 
                    "No enemy character sets available.", 
                    "No Enemy Characters", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            String selectedSet = showCharacterSelectionDialog(settingsWindow, "Set Default Enemy Character", enemySets, false);
            
            if (selectedSet != null) {
                manager.setDefaultEnemyCharacterSet(selectedSet);
                JOptionPane.showMessageDialog(settingsWindow, 
                    "Set default enemy character set to: " + selectedSet + "\nThis will be used when the program starts.", 
                    "Default Set Successfully", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
        // choice == 4 is Cancel, do nothing
    }
    
    /**
     * Show character selection dialog with previews
     */
    private String showCharacterSelectionDialog(JFrame parent, String title, Set<String> characterSets, boolean isPet) {
        if (characterSets.isEmpty()) return null;
        
        JDialog dialog = new JDialog(parent, title, true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(parent);
        
        String[] selectedCharacter = {null}; // Use array to modify from inner class
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create scrollable panel for character list
        JPanel characterPanel = new JPanel();
        characterPanel.setLayout(new BoxLayout(characterPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(characterPanel);
        scrollPane.setPreferredSize(new Dimension(580, 300));
        
        ButtonGroup buttonGroup = new ButtonGroup();
        
        // Add each character as a row with preview image and name
        for (String setName : characterSets) {
            JPanel rowPanel = new JPanel(new BorderLayout());
            rowPanel.setBorder(BorderFactory.createEtchedBorder());
            rowPanel.setPreferredSize(new Dimension(560, 80));
            
            // Get character set to extract preview image
            CharacterSet characterSet = isPet ? 
                characterSetManager.getPetCharacterSet(setName) :
                characterSetManager.getEnemyCharacterSet(setName);
            
            // Create preview image
            JLabel imageLabel = new JLabel();
            imageLabel.setPreferredSize(new Dimension(64, 64));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            
            if (characterSet != null) {
                // Try to get first frame of idle animation for preview
                AnimationSequence idleSeq = characterSet.getIdleAnimation();
                if (idleSeq.getFrameCount() > 0) {
                    AnimationFrame firstFrame = idleSeq.getFrames().get(0);
                    if (firstFrame.getOriginalImage() != null) {
                        Image img = firstFrame.getOriginalImage().getImage();
                        Image scaled = img.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                        imageLabel.setIcon(new ImageIcon(scaled));
                    }
                }
            }
            
            // If no preview available, show placeholder
            if (imageLabel.getIcon() == null) {
                imageLabel.setText("No Preview");
                imageLabel.setOpaque(true);
                imageLabel.setBackground(Color.LIGHT_GRAY);
            }
            
            // Create radio button with character name
            JRadioButton radioButton = new JRadioButton(setName);
            radioButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            buttonGroup.add(radioButton);
            
            // Create info panel with name and details
            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.add(radioButton, BorderLayout.NORTH);
            
            // Add description if available
            if (characterSet != null && !characterSet.getDescription().isEmpty()) {
                JLabel descLabel = new JLabel("<html><i>" + characterSet.getDescription() + "</i></html>");
                descLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
                infoPanel.add(descLabel, BorderLayout.CENTER);
            }
            
            // Add author if available
            if (characterSet != null && !characterSet.getAuthorName().isEmpty()) {
                JLabel authorLabel = new JLabel("By: " + characterSet.getAuthorName());
                authorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                infoPanel.add(authorLabel, BorderLayout.SOUTH);
            }
            
            // Add click listener to select radio button when clicking anywhere on row
            rowPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    radioButton.setSelected(true);
                }
            });
            
            rowPanel.add(imageLabel, BorderLayout.WEST);
            rowPanel.add(infoPanel, BorderLayout.CENTER);
            
            characterPanel.add(rowPanel);
            
            // Select first item by default
            if (selectedCharacter[0] == null) {
                radioButton.setSelected(true);
                selectedCharacter[0] = setName;
            }
            
            // Update selection when radio button changes
            radioButton.addActionListener(e -> selectedCharacter[0] = setName);
        }
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        okButton.addActionListener(e -> dialog.dispose());
        cancelButton.addActionListener(e -> {
            selectedCharacter[0] = null;
            dialog.dispose();
        });
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
        
        return selectedCharacter[0];
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
        super.setLocation(p);
    }
}

// ============================================================================
// CHARACTER IMPORT SYSTEM - CORE DATA STRUCTURES
// ============================================================================

/**
 * Represents a single animation frame with image and timing
 */
class AnimationFrame {
    private ImageIcon image;
    private ImageIcon originalImage; // Store original high-quality version
    private String imagePath;
    private int duration; // Duration in milliseconds
    
    public AnimationFrame(ImageIcon image, String imagePath, int duration) {
        this.originalImage = image; // Store original
        this.image = image;
        this.imagePath = imagePath;
        this.duration = duration;
    }
    
    public ImageIcon getImage() { return image; }
    public ImageIcon getOriginalImage() { return originalImage; }
    public String getImagePath() { return imagePath; }
    public int getDuration() { return duration; }
    
    public void setImage(ImageIcon image) { this.image = image; }
    public void setDuration(int duration) { this.duration = duration; }
    
    /**
     * Scale this frame to new dimensions using original high-quality image
     */
    public void scaleToSize(int width, int height) {
        if (originalImage != null) {
            Image originalImg = originalImage.getImage();
            Image scaledImg = originalImg.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            this.image = new ImageIcon(scaledImg);
        }
    }
}

/**
 * Represents a sequence of animation frames (walking, idle, pain, etc.)
 */
class AnimationSequence {
    private String name;
    private List<AnimationFrame> frames;
    private boolean loop;
    private int currentFrame;
    
    public AnimationSequence(String name, boolean loop) {
        this.name = name;
        this.loop = loop;
        this.frames = new ArrayList<>();
        this.currentFrame = 0;
    }
    
    public void addFrame(AnimationFrame frame) {
        frames.add(frame);
    }
    
    public void removeFrame(int index) {
        if (index >= 0 && index < frames.size()) {
            frames.remove(index);
        }
    }
    
    public AnimationFrame getCurrentFrame() {
        if (frames.isEmpty()) return null;
        return frames.get(currentFrame);
    }
    
    public AnimationFrame nextFrame() {
        if (frames.isEmpty()) return null;
        
        currentFrame++;
        if (currentFrame >= frames.size()) {
            if (loop) {
                currentFrame = 0;
            } else {
                currentFrame = frames.size() - 1; // Stay at last frame
            }
        }
        return frames.get(currentFrame);
    }
    
    public void reset() {
        currentFrame = 0;
    }
    
    // Getters
    public String getName() { return name; }
    public List<AnimationFrame> getFrames() { return frames; }
    public boolean isLoop() { return loop; }
    public int getCurrentFrameIndex() { return currentFrame; }
    public int getFrameCount() { return frames.size(); }
    
    // Setters
    public void setLoop(boolean loop) { this.loop = loop; }
}

/**
 * Represents a complete character set with all animations
 */
class CharacterSet {
    private String name;
    private String description;
    private String authorName;
    private String setPath; // Directory path for this character set
    
    // Animation sequences
    private AnimationSequence idleAnimation;
    private AnimationSequence walkingAnimation;
    private AnimationSequence specialAnimation;
    private AnimationSequence painAnimation;
    
    public CharacterSet(String name, String setPath) {
        this.name = name;
        this.setPath = setPath;
        this.description = "";
        this.authorName = "";
        
        // Initialize animation sequences
        this.idleAnimation = new AnimationSequence("idle", true);
        this.walkingAnimation = new AnimationSequence("walking", true);
        this.specialAnimation = new AnimationSequence("special", false);
        this.painAnimation = new AnimationSequence("pain", true); // Pain should loop during the effect
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAuthorName() { return authorName; }
    public String getSetPath() { return setPath; }
    public AnimationSequence getIdleAnimation() { return idleAnimation; }
    public AnimationSequence getWalkingAnimation() { return walkingAnimation; }
    public AnimationSequence getSpecialAnimation() { return specialAnimation; }
    public AnimationSequence getPainAnimation() { return painAnimation; }
    
    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setSetPath(String setPath) { this.setPath = setPath; }
    public void setIdleAnimation(AnimationSequence idleAnimation) { this.idleAnimation = idleAnimation; }
    public void setWalkingAnimation(AnimationSequence walkingAnimation) { this.walkingAnimation = walkingAnimation; }
    public void setSpecialAnimation(AnimationSequence specialAnimation) { this.specialAnimation = specialAnimation; }
    public void setPainAnimation(AnimationSequence painAnimation) { this.painAnimation = painAnimation; }
    
    /**
     * Check if character set has all required animations
     */
    public boolean isComplete() {
        // Only require idle and walking animations for basic functionality
        // Special and pain animations are optional
        return idleAnimation.getFrameCount() > 0 && 
               walkingAnimation.getFrameCount() > 0;
    }
    
    /**
     * Check if character set has all animation types (including optional ones)
     */
    public boolean isFullyComplete() {
        return idleAnimation.getFrameCount() > 0 && 
               walkingAnimation.getFrameCount() > 0 &&
               specialAnimation.getFrameCount() > 0 &&
               painAnimation.getFrameCount() > 0;
    }
    
    /**
     * Get animation sequence by name
     */
    public AnimationSequence getAnimationByName(String animationName) {
        switch (animationName.toLowerCase()) {
            case "idle": return idleAnimation;
            case "walking": return walkingAnimation;
            case "special": return specialAnimation;
            case "pain": return painAnimation;
            default: return null;
        }
    }
}

/**
 * Manages character sets and handles switching between them
 */
class CharacterSetManager {
    private static CharacterSetManager instance;
    private Map<String, CharacterSet> petCharacterSets;
    private Map<String, CharacterSet> enemyCharacterSets;
    private String currentPetSet;
    private String currentEnemySet;
    private String characterSetsPath;
    
    private CharacterSetManager() {
        this.petCharacterSets = new HashMap<>();
        this.enemyCharacterSets = new HashMap<>();
        this.characterSetsPath = "resources/CharacterSets/";
        this.currentPetSet = "default";
        this.currentEnemySet = "default";
        
        // Create character sets directory if it doesn't exist
        createCharacterSetsDirectory();
        
        // Initialize default character sets
        initializeDefaultCharacterSets();
    }
    
    public static CharacterSetManager getInstance() {
        if (instance == null) {
            instance = new CharacterSetManager();
        }
        return instance;
    }
    
    private void createCharacterSetsDirectory() {
        try {
            File dir = new File(characterSetsPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // Create subdirectories
            new File(characterSetsPath + "Pets/").mkdirs();
            new File(characterSetsPath + "Enemies/").mkdirs();
            new File(characterSetsPath + "Pets/default/").mkdirs();
            new File(characterSetsPath + "Enemies/default/").mkdirs();
        } catch (Exception e) {
            System.out.println("Error creating character sets directory: " + e.getMessage());
        }
    }
    
    private void initializeDefaultCharacterSets() {
        // Create default pet character set using existing images
        CharacterSet defaultPetSet = new CharacterSet("default", characterSetsPath + "Pets/default/");
        
        // Note: We'll populate these with existing images in the integration phase
        petCharacterSets.put("default", defaultPetSet);
        
        // Create default enemy character set
        CharacterSet defaultEnemySet = new CharacterSet("default", characterSetsPath + "Enemies/default/");
        enemyCharacterSets.put("default", defaultEnemySet);
        
        System.out.println("Character Set Manager initialized with default sets");
        
        // Load existing character sets from disk
        loadCharacterSetsFromDisk();
        
        // Load default character sets from config file
        loadDefaultCharacterSets();
        
        // Don't automatically select imported character sets unless user set a default
        System.out.println("Using character set: " + currentPetSet + ". Use settings to switch characters.");
    }
    
    // Pet character set management
    public void addPetCharacterSet(CharacterSet characterSet) {
        petCharacterSets.put(characterSet.getName(), characterSet);
    }
    
    public CharacterSet getCurrentPetCharacterSet() {
        return petCharacterSets.get(currentPetSet);
    }
    
    public void setCurrentPetCharacterSet(String setName) {
        if (petCharacterSets.containsKey(setName)) {
            currentPetSet = setName;
            System.out.println("Switched to pet character set: " + setName);
        }
    }
    
    // Enemy character set management
    public void addEnemyCharacterSet(CharacterSet characterSet) {
        enemyCharacterSets.put(characterSet.getName(), characterSet);
    }
    
    public CharacterSet getCurrentEnemyCharacterSet() {
        return enemyCharacterSets.get(currentEnemySet);
    }
    
    public void setCurrentEnemyCharacterSet(String setName) {
        if (enemyCharacterSets.containsKey(setName)) {
            currentEnemySet = setName;
            System.out.println("Switched to enemy character set: " + setName);
        }
    }
    
    // General methods
    public Set<String> getPetCharacterSetNames() {
        return petCharacterSets.keySet();
    }
    
    public Set<String> getEnemyCharacterSetNames() {
        return enemyCharacterSets.keySet();
    }
    
    public boolean hasPetCharacterSet(String name) {
        return petCharacterSets.containsKey(name);
    }
    
    public boolean hasEnemyCharacterSet(String name) {
        return enemyCharacterSets.containsKey(name);
    }
    
    public CharacterSet getPetCharacterSet(String name) {
        return petCharacterSets.get(name);
    }
    
    public CharacterSet getEnemyCharacterSet(String name) {
        return enemyCharacterSets.get(name);
    }
    
    /**
     * Remove a pet character set
     */
    public boolean removePetCharacterSet(String name) {
        if (!petCharacterSets.containsKey(name) || name.equals("default")) {
            return false; // Cannot remove default or non-existent sets
        }
        CharacterSet removed = petCharacterSets.remove(name);
        
        // If this was the current set, switch to default
        if (name.equals(currentPetSet)) {
            currentPetSet = "default";
        }
        
        return removed != null;
    }
    
    /**
     * Remove an enemy character set
     */
    public boolean removeEnemyCharacterSet(String name) {
        if (!enemyCharacterSets.containsKey(name) || name.equals("default")) {
            return false; // Cannot remove default or non-existent sets
        }
        CharacterSet removed = enemyCharacterSets.remove(name);
        
        // If this was the current set, switch to default
        if (name.equals(currentEnemySet)) {
            currentEnemySet = "default";
        }
        
        return removed != null;
    }
    
    /**
     * Set a character set as the startup default
     */
    public void setDefaultPetCharacterSet(String setName) {
        if (petCharacterSets.containsKey(setName)) {
            try {
                // Save to a simple config file
                File configFile = new File("character_defaults.properties");
                StringBuilder content = new StringBuilder();
                content.append("default_pet_character_set=").append(setName).append("\n");
                if (configFile.exists()) {
                    // Read existing content and update pet setting
                    java.util.Properties props = new java.util.Properties();
                    props.load(new java.io.FileInputStream(configFile));
                    props.setProperty("default_pet_character_set", setName);
                    props.store(new java.io.FileOutputStream(configFile), "Character Set Defaults");
                } else {
                    java.nio.file.Files.write(configFile.toPath(), content.toString().getBytes());
                }
                System.out.println("Set default pet character set to: " + setName);
            } catch (Exception e) {
                System.out.println("Error saving default character set: " + e.getMessage());
            }
        }
    }
    
    public void setDefaultEnemyCharacterSet(String setName) {
        if (enemyCharacterSets.containsKey(setName)) {
            try {
                // Save to a simple config file
                File configFile = new File("character_defaults.properties");
                java.util.Properties props = new java.util.Properties();
                if (configFile.exists()) {
                    props.load(new java.io.FileInputStream(configFile));
                }
                props.setProperty("default_enemy_character_set", setName);
                props.store(new java.io.FileOutputStream(configFile), "Character Set Defaults");
                System.out.println("Set default enemy character set to: " + setName);
                System.out.println("Config file saved to: " + configFile.getAbsolutePath());
                
                // Also immediately set as current to ensure it's active
                currentEnemySet = setName;
                System.out.println("Immediately activated enemy character set: " + setName);
            } catch (Exception e) {
                System.out.println("Error saving default character set: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Warning: Cannot set default enemy character set '" + setName + "' - not found in available sets");
            System.out.println("Available enemy character sets: " + enemyCharacterSets.keySet());
        }
    }
    
    /**
     * Load default character sets from config file
     */
    public void loadDefaultCharacterSets() {
        try {
            File configFile = new File("character_defaults.properties");
            if (configFile.exists()) {
                System.out.println("Loading default character sets from: " + configFile.getAbsolutePath());
                java.util.Properties props = new java.util.Properties();
                props.load(new java.io.FileInputStream(configFile));
                
                String defaultPet = props.getProperty("default_pet_character_set");
                String defaultEnemy = props.getProperty("default_enemy_character_set");
                
                System.out.println("Config file contains - Pet: " + defaultPet + ", Enemy: " + defaultEnemy);
                
                if (defaultPet != null && petCharacterSets.containsKey(defaultPet)) {
                    currentPetSet = defaultPet;
                    System.out.println("Loaded default pet character set: " + defaultPet);
                } else if (defaultPet != null) {
                    System.out.println("Warning: Default pet character set '" + defaultPet + "' not found in available sets");
                }
                
                if (defaultEnemy != null && enemyCharacterSets.containsKey(defaultEnemy)) {
                    currentEnemySet = defaultEnemy;
                    System.out.println("Loaded default enemy character set: " + defaultEnemy);
                } else if (defaultEnemy != null) {
                    System.out.println("Warning: Default enemy character set '" + defaultEnemy + "' not found in available sets");
                }
            } else {
                System.out.println("No character defaults config file found at: " + configFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("Error loading default character sets: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load character sets from disk
     */
    public void loadCharacterSetsFromDisk() {
        loadPetCharacterSets();
        loadEnemyCharacterSets();
    }
    
    private void loadPetCharacterSets() {
        try {
            File petsDir = new File(characterSetsPath + "Pets/");
            if (petsDir.exists() && petsDir.isDirectory()) {
                File[] setDirs = petsDir.listFiles(File::isDirectory);
                if (setDirs != null) {
                    System.out.println("Loading pet character sets from: " + petsDir.getAbsolutePath());
                    for (File setDir : setDirs) {
                        String setName = setDir.getName();
                        if (!petCharacterSets.containsKey(setName)) {
                            CharacterSet characterSet = loadCharacterSetFromDirectory(setDir, setName);
                            if (characterSet != null) {
                                petCharacterSets.put(setName, characterSet);
                                System.out.println("Loaded pet character set: " + setName);
                            }
                        }
                    }
                }
            } else {
                System.out.println("Pet character sets directory does not exist: " + petsDir.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("Error loading pet character sets: " + e.getMessage());
        }
    }
    
    private void loadEnemyCharacterSets() {
        try {
            File enemiesDir = new File(characterSetsPath + "Enemies/");
            if (enemiesDir.exists() && enemiesDir.isDirectory()) {
                File[] setDirs = enemiesDir.listFiles(File::isDirectory);
                if (setDirs != null) {
                    for (File setDir : setDirs) {
                        String setName = setDir.getName();
                        if (!enemyCharacterSets.containsKey(setName)) {
                            CharacterSet characterSet = loadCharacterSetFromDirectory(setDir, setName);
                            if (characterSet != null) {
                                enemyCharacterSets.put(setName, characterSet);
                                System.out.println("Successfully loaded enemy character set: " + setName);
                            }
                        }
                    }
                }
            }
            
            // Debug: Show loaded enemy character sets
            System.out.println("Total enemy character sets loaded: " + enemyCharacterSets.size());
            for (String setName : enemyCharacterSets.keySet()) {
                System.out.println("  - " + setName);
            }
        } catch (Exception e) {
            System.out.println("Error loading enemy character sets: " + e.getMessage());
        }
    }
    
    private CharacterSet loadCharacterSetFromDirectory(File setDir, String setName) {
        try {
            CharacterSet characterSet = new CharacterSet(setName, setDir.getAbsolutePath() + "/");
            
            // Load metadata
            Map<String, String> metadata = CharacterFileManager.loadCharacterSetMetadata(setDir);
            if (metadata.containsKey("description")) {
                characterSet.setDescription(metadata.get("description"));
            }
            if (metadata.containsKey("author")) {
                characterSet.setAuthorName(metadata.get("author"));
            }
            
            // Load animation sequences
            File idleDir = new File(setDir, "idle");
            File walkingDir = new File(setDir, "walking");
            File specialDir = new File(setDir, "special");
            File painDir = new File(setDir, "pain");
            
            if (idleDir.exists()) {
                AnimationSequence idleSeq = CharacterFileManager.loadAnimationFromDirectory(idleDir, "idle", true);
                characterSet.getIdleAnimation().getFrames().clear();
                characterSet.getIdleAnimation().getFrames().addAll(idleSeq.getFrames());
            }
            
            if (walkingDir.exists()) {
                AnimationSequence walkingSeq = CharacterFileManager.loadAnimationFromDirectory(walkingDir, "walking", true);
                characterSet.getWalkingAnimation().getFrames().clear();
                characterSet.getWalkingAnimation().getFrames().addAll(walkingSeq.getFrames());
            }
            
            if (specialDir.exists()) {
                AnimationSequence specialSeq = CharacterFileManager.loadAnimationFromDirectory(specialDir, "special", false);
                characterSet.getSpecialAnimation().getFrames().clear();
                characterSet.getSpecialAnimation().getFrames().addAll(specialSeq.getFrames());
            }
            
            if (painDir.exists()) {
                AnimationSequence painSeq = CharacterFileManager.loadAnimationFromDirectory(painDir, "pain", false);
                characterSet.getPainAnimation().getFrames().clear();
                characterSet.getPainAnimation().getFrames().addAll(painSeq.getFrames());
            }
            
            System.out.println("Loaded character set: " + setName + " from " + setDir.getAbsolutePath());
            return characterSet;
            
        } catch (Exception e) {
            System.out.println("Error loading character set from directory: " + e.getMessage());
            return null;
        }
    }
}

// ============================================================================
// CHARACTER IMPORT SYSTEM - FILE MANAGEMENT UTILITIES
// ============================================================================

/**
 * Handles file operations for character sets
 */
class CharacterFileManager {
    private static final String[] SUPPORTED_FORMATS = {".png", ".jpg", ".jpeg", ".gif"};
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB max
    
    /**
     * Copy and organize imported images into character set directory
     */
    public static boolean importImageFiles(File[] imageFiles, String targetDirectory, String animationType) {
        try {
            File targetDir = new File(targetDirectory);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            
            // Create animation subdirectory
            File animationDir = new File(targetDir, animationType);
            if (!animationDir.exists()) {
                animationDir.mkdirs();
            }
            
            int frameIndex = 0;
            for (File imageFile : imageFiles) {
                if (isValidImageFile(imageFile)) {
                    String extension = getFileExtension(imageFile.getName());
                    String targetFileName = String.format("%s_frame_%03d%s", animationType, frameIndex, extension);
                    File targetFile = new File(animationDir, targetFileName);
                    
                    // Copy file
                    copyFile(imageFile, targetFile);
                    frameIndex++;
                    
                    System.out.println("Imported: " + imageFile.getName() + " -> " + targetFileName);
                }
            }
            
            return frameIndex > 0; // Return true if at least one file was imported
            
        } catch (Exception e) {
            System.out.println("Error importing image files: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load animation sequence from directory
     */
    public static AnimationSequence loadAnimationFromDirectory(File animationDir, String animationType, boolean loop) {
        AnimationSequence sequence = new AnimationSequence(animationType, loop);
        
        try {
            if (!animationDir.exists() || !animationDir.isDirectory()) {
                return sequence;
            }
            
            // Get all image files and sort them
            File[] imageFiles = animationDir.listFiles((dir, name) -> {
                String lowercaseName = name.toLowerCase();
                for (String format : SUPPORTED_FORMATS) {
                    if (lowercaseName.endsWith(format)) {
                        return true;
                    }
                }
                return false;
            });
            
            if (imageFiles != null) {
                // Sort files by name to ensure correct frame order
                java.util.Arrays.sort(imageFiles);
                
                for (File imageFile : imageFiles) {
                    ImageIcon image = loadAndScaleImagePreserveAspect(imageFile, 256); // Use larger max size
                    if (image != null) {
                        AnimationFrame frame = new AnimationFrame(image, imageFile.getAbsolutePath(), 150); // Default duration
                        sequence.addFrame(frame);
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error loading animation from directory: " + e.getMessage());
        }
        
        return sequence;
    }
    
    /**
     * Save character set metadata to file
     */
    public static boolean saveCharacterSetMetadata(CharacterSet characterSet) {
        try {
            File setDir = new File(characterSet.getSetPath());
            if (!setDir.exists()) {
                setDir.mkdirs();
            }
            
            File metadataFile = new File(setDir, "metadata.properties");
            
            // Create properties content
            StringBuilder content = new StringBuilder();
            content.append("name=").append(characterSet.getName()).append("\n");
            content.append("description=").append(characterSet.getDescription()).append("\n");
            content.append("author=").append(characterSet.getAuthorName()).append("\n");
            content.append("created=").append(System.currentTimeMillis()).append("\n");
            content.append("version=1.0").append("\n");
            
            // Write to file
            java.nio.file.Files.write(metadataFile.toPath(), content.toString().getBytes());
            
            return true;
            
        } catch (Exception e) {
            System.out.println("Error saving character set metadata: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load character set metadata from file
     */
    public static Map<String, String> loadCharacterSetMetadata(File setDirectory) {
        Map<String, String> metadata = new HashMap<>();
        
        try {
            File metadataFile = new File(setDirectory, "metadata.properties");
            if (metadataFile.exists()) {
                List<String> lines = java.nio.file.Files.readAllLines(metadataFile.toPath());
                for (String line : lines) {
                    if (line.contains("=")) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            metadata.put(parts[0].trim(), parts[1].trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading character set metadata: " + e.getMessage());
        }
        
        return metadata;
    }
    
    /**
     * Validate image file
     */
    public static boolean isValidImageFile(File file) {
        if (!file.exists() || !file.isFile()) {
            return false;
        }
        
        // Check file size
        if (file.length() > MAX_FILE_SIZE) {
            System.out.println("File too large: " + file.getName());
            return false;
        }
        
        // Check file extension
        String fileName = file.getName().toLowerCase();
        for (String format : SUPPORTED_FORMATS) {
            if (fileName.endsWith(format)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get file extension
     */
    public static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }
    
    /**
     * Copy file from source to destination
     */
    public static void copyFile(File source, File destination) throws Exception {
        java.nio.file.Files.copy(source.toPath(), destination.toPath(), 
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * Load and scale image to specified dimensions
     */
    public static ImageIcon loadAndScaleImage(File imageFile, int width, int height) {
        try {
            ImageIcon originalIcon = new ImageIcon(imageFile.getAbsolutePath());
            if (originalIcon.getImage() != null) {
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + imageFile.getName() + " - " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Load image with preserved aspect ratio, targeting a specific maximum size
     */
    public static ImageIcon loadAndScaleImagePreserveAspect(File imageFile, int maxSize) {
        try {
            ImageIcon originalIcon = new ImageIcon(imageFile.getAbsolutePath());
            if (originalIcon.getImage() != null) {
                int originalWidth = originalIcon.getIconWidth();
                int originalHeight = originalIcon.getIconHeight();
                
                // If original image is reasonable size, use it directly (more generous range)
                if (originalWidth <= maxSize && originalHeight <= maxSize && 
                    originalWidth >= maxSize/4 && originalHeight >= maxSize/4) {
                    return originalIcon;
                }
                
                // Calculate new dimensions preserving aspect ratio
                double aspectRatio = (double) originalWidth / originalHeight;
                int newWidth, newHeight;
                
                if (originalWidth > originalHeight) {
                    newWidth = maxSize;
                    newHeight = (int) (maxSize / aspectRatio);
                } else {
                    newHeight = maxSize;
                    newWidth = (int) (maxSize * aspectRatio);
                }
                
                Image scaledImage = originalIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + imageFile.getName() + " - " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Create directory structure for character set
     */
    public static boolean createCharacterSetDirectoryStructure(String setPath) {
        try {
            // Main set directory
            File setDir = new File(setPath);
            if (!setDir.exists() && !setDir.mkdirs()) {
                return false;
            }
            
            // Animation subdirectories
            String[] animationTypes = {"idle", "walking", "special", "pain"};
            for (String animationType : animationTypes) {
                File animationDir = new File(setDir, animationType);
                if (!animationDir.exists() && !animationDir.mkdirs()) {
                    return false;
                }
            }
            
            System.out.println("Created character set directory structure: " + setPath);
            return true;
            
        } catch (Exception e) {
            System.out.println("Error creating directory structure: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete character set directory and all contents
     */
    public static boolean deleteCharacterSet(String setPath) {
        try {
            File setDir = new File(setPath);
            return deleteDirectoryRecursively(setDir);
        } catch (Exception e) {
            System.out.println("Error deleting character set: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean deleteDirectoryRecursively(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectoryRecursively(file);
                }
            }
        }
        return directory.delete();
    }
}

// ============================================================================
// CHARACTER IMPORT SYSTEM - IMPORT WINDOW UI
// ============================================================================

/**
 * Character Set Item for dropdown with name and thumbnail
 */
class CharacterSetItem {
    private String name;
    private ImageIcon thumbnail;
    private boolean isNewItem;
    
    public CharacterSetItem(String name, ImageIcon thumbnail) {
        this.name = name;
        this.thumbnail = thumbnail;
        this.isNewItem = false;
    }
    
    public CharacterSetItem(String name) {
        this.name = name;
        this.thumbnail = null;
        this.isNewItem = name.equals("-- Create New --");
    }
    
    public String getName() { return name; }
    public ImageIcon getThumbnail() { return thumbnail; }
    public boolean isNewItem() { return isNewItem; }
    
    @Override
    public String toString() { return name; }
}

/**
 * Custom combo box renderer for character sets
 */
class CharacterSetComboRenderer extends DefaultListCellRenderer {
    private static final int THUMBNAIL_SIZE = 32;
    
    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if (value instanceof CharacterSetItem) {
            CharacterSetItem item = (CharacterSetItem) value;
            setText(item.getName());
            
            if (item.getThumbnail() != null) {
                // Scale thumbnail to fit
                Image img = item.getThumbnail().getImage();
                Image scaledImg = img.getScaledInstance(THUMBNAIL_SIZE, THUMBNAIL_SIZE, Image.SCALE_SMOOTH);
                setIcon(new ImageIcon(scaledImg));
            } else {
                setIcon(null);
            }
        }
        
        return this;
    }
}

/**
 * Dedicated window for importing and managing character sets
 */
class CharacterImportWindow extends JFrame {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int PREVIEW_SIZE = 150;
    
    private CharacterSetManager characterSetManager;
    private CharacterSet currentWorkingSet;
    private String currentSetType; // "pet" or "enemy"
    private AdvancedDesktopPet parentPet; // Reference to parent for translation access
    
    // UI Components
    private JTextField setNameField;
    private JTextField authorField;
    private JTextArea descriptionArea;
    private JComboBox<String> setTypeCombo;
    private JComboBox<CharacterSetItem> existingSetCombo;
    
    // Animation panels
    private AnimationImportPanel idlePanel;
    private AnimationImportPanel walkingPanel;
    private AnimationImportPanel specialPanel;
    private AnimationImportPanel painPanel;
    
    // Preview panel
    private JPanel previewPanel;
    private JLabel previewLabel;
    private Timer previewTimer;
    private int previewFrameIndex = 0;
    
    // Control buttons
    private JButton importButton;
    private JButton exportButton;
    private JButton saveButton;
    private JButton deleteButton;
    private JButton testButton;
    private JButton editButton;
    private JButton setDefaultButton;
    
    public CharacterImportWindow(AdvancedDesktopPet parent) {
        super("Character Set Import Manager");
        
        this.parentPet = parent;
        characterSetManager = CharacterSetManager.getInstance();
        currentSetType = "pet";
        
        // Update title with proper translation
        setTitle(getText("import_window_title"));
        
        initializeUI();
        initializeEventHandlers();
        
        // Load existing character sets
        refreshExistingSetsList();
        
        // Create new empty set by default
        createNewCharacterSet();
        
        System.out.println("Character Import Window initialized");
        
        // Add periodic timer to ensure pet keeps moving while import window is active
        Timer petActivityTimer = new Timer(5000, e -> {
            if (isVisible()) {
                // Find all pets and ensure they remain active
                for (AdvancedDesktopPet pet : AdvancedDesktopPet.getAllPets()) {
                    pet.ensureTimersActive();
                }
            }
        });
        petActivityTimer.start();
        
        // Stop the timer when window is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                petActivityTimer.stop();
            }
        });
    }
    
    /**
     * Get translated text from parent pet instance
     */
    private String getText(String key) {
        if (parentPet != null) {
            return parentPet.getText(key);
        }
        return key; // fallback to key if parent is null
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        
        // Create main layout
        setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create main content panel
        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
        
        // Create footer panel
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
        
        // Initialize preview timer
        previewTimer = new Timer(200, e -> updatePreview());
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setBorder(BorderFactory.createTitledBorder(getText("character_set_info")));
        
        // Set type selection
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(getText("type")), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        setTypeCombo = new JComboBox<>(new String[]{getText("pet_character"), getText("enemy_character")});
        panel.add(setTypeCombo, gbc);
        
        // Existing sets
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(getText("existing_sets")), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        existingSetCombo = new JComboBox<>();
        existingSetCombo.setEditable(false);
        existingSetCombo.setRenderer(new CharacterSetComboRenderer());
        panel.add(existingSetCombo, gbc);
        
        // Set name
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(getText("set_name")), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        setNameField = new JTextField();
        panel.add(setNameField, gbc);
        
        // Author
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(getText("author")), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        authorField = new JTextField();
        panel.add(authorField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(getText("description")), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        panel.add(descScrollPane, gbc);
        
        return panel;
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create left panel for animations
        JPanel leftPanel = createAnimationPanel();
        panel.add(leftPanel, BorderLayout.CENTER);
        
        // Create right panel for preview
        JPanel rightPanel = createPreviewPanel();
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createAnimationPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(getText("animation_import")));
        
        // Create animation import panels
        idlePanel = new AnimationImportPanel(getText("idle_animation"), true);
        walkingPanel = new AnimationImportPanel(getText("walking_animation"), true);
        specialPanel = new AnimationImportPanel(getText("special_animation"), false);
        painPanel = new AnimationImportPanel(getText("pain_animation"), false);
        
        panel.add(idlePanel);
        panel.add(walkingPanel);
        panel.add(specialPanel);
        panel.add(painPanel);
        
        return panel;
    }
    
    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(getText("preview")));
        panel.setPreferredSize(new Dimension(200, 0));
        
        // Preview display
        previewPanel = new JPanel(new BorderLayout());
        previewPanel.setPreferredSize(new Dimension(PREVIEW_SIZE, PREVIEW_SIZE));
        previewPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        previewLabel = new JLabel();
        previewLabel.setHorizontalAlignment(JLabel.CENTER);
        previewLabel.setVerticalAlignment(JLabel.CENTER);
        previewPanel.add(previewLabel, BorderLayout.CENTER);
        
        panel.add(previewPanel, BorderLayout.NORTH);
        
        // Preview controls
        JPanel controlPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder(getText("preview_controls")));
        
        JButton previewIdleBtn = new JButton(getText("preview_idle"));
        JButton previewWalkBtn = new JButton(getText("preview_walk"));
        JButton previewSpecialBtn = new JButton(getText("preview_special"));
        JButton previewPainBtn = new JButton(getText("preview_pain"));
        JButton stopPreviewBtn = new JButton(getText("stop_preview"));
        
        controlPanel.add(previewIdleBtn);
        controlPanel.add(previewWalkBtn);
        controlPanel.add(previewSpecialBtn);
        controlPanel.add(previewPainBtn);
        controlPanel.add(stopPreviewBtn);
        
        panel.add(controlPanel, BorderLayout.CENTER);
        
        // Add preview event handlers
        previewIdleBtn.addActionListener(e -> startPreview("idle"));
        previewWalkBtn.addActionListener(e -> startPreview("walking"));
        previewSpecialBtn.addActionListener(e -> startPreview("special"));
        previewPainBtn.addActionListener(e -> startPreview("pain"));
        stopPreviewBtn.addActionListener(e -> stopPreview());
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        importButton = new JButton(getText("import_images"));
        exportButton = new JButton(getText("export_set"));
        saveButton = new JButton(getText("save_set"));
        deleteButton = new JButton(getText("delete_set"));
        testButton = new JButton(getText("test_in_pet"));
        editButton = new JButton(getText("edit_properties"));
        setDefaultButton = new JButton(getText("set_as_default"));
        JButton closeButton = new JButton(getText("close"));
        
        panel.add(importButton);
        panel.add(exportButton);
        panel.add(saveButton);
        panel.add(deleteButton);
        panel.add(testButton);
        panel.add(editButton);
        panel.add(setDefaultButton);
        panel.add(closeButton);
        
        closeButton.addActionListener(e -> setVisible(false));
        
        return panel;
    }
    
    private void initializeEventHandlers() {
        // Set type change
        setTypeCombo.addActionListener(e -> {
            currentSetType = setTypeCombo.getSelectedIndex() == 0 ? "pet" : "enemy";
            refreshExistingSetsList();
        });
        
        // Existing set selection
        existingSetCombo.addActionListener(e -> {
            CharacterSetItem selectedItem = (CharacterSetItem) existingSetCombo.getSelectedItem();
            if (selectedItem != null && !selectedItem.isNewItem()) {
                loadExistingCharacterSet(selectedItem.getName());
            }
        });
        
        // Save button
        saveButton.addActionListener(e -> saveCurrentCharacterSet());
        
        // Delete button
        deleteButton.addActionListener(e -> deleteCurrentCharacterSet());
        
        // Test button
        testButton.addActionListener(e -> testCharacterSetInPet());
        
        // Import button
        importButton.addActionListener(e -> importImagesDialog());
        
        // Export button
        exportButton.addActionListener(e -> exportCharacterSet());
        
        // Edit button
        editButton.addActionListener(e -> openCharacterEditDialog());
        
        // Set Default button
        setDefaultButton.addActionListener(e -> setCurrentCharacterAsDefault());
    }
    
    private void setCurrentCharacterAsDefault() {
        if (currentWorkingSet == null) {
            JOptionPane.showMessageDialog(this, 
                getText("no_character_set_selected"), 
                getText("no_character_set"), 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String setName = currentWorkingSet.getName();
        
        try {
            if (currentSetType.equals("pet")) {
                characterSetManager.setDefaultPetCharacterSet(setName);
                JOptionPane.showMessageDialog(this, 
                    getText("set_default_pet") + setName + "\n" +
                    getText("default_char_message"), 
                    getText("default_pet_set"), 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                characterSetManager.setDefaultEnemyCharacterSet(setName);
                JOptionPane.showMessageDialog(this, 
                    getText("set_default_enemy") + setName + "\n" +
                    getText("default_enemy_message"), 
                    getText("default_enemy_set"), 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            System.out.println("Successfully set default " + currentSetType + " character to: " + setName);
            
        } catch (Exception ex) {
            System.out.println("Error setting default character: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                getText("error_setting_default") + ex.getMessage(), 
                getText("error"), 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshExistingSetsList() {
        existingSetCombo.removeAllItems();
        existingSetCombo.addItem(new CharacterSetItem(getText("create_new")));
        
        Set<String> setNames = currentSetType.equals("pet") ? 
            characterSetManager.getPetCharacterSetNames() : 
            characterSetManager.getEnemyCharacterSetNames();
        
        for (String setName : setNames) {
            ImageIcon thumbnail = getCharacterSetThumbnail(setName, currentSetType.equals("pet"));
            existingSetCombo.addItem(new CharacterSetItem(setName, thumbnail));
        }
    }
    
    /**
     * Get thumbnail image for a character set
     */
    private ImageIcon getCharacterSetThumbnail(String setName, boolean isPet) {
        try {
            CharacterSet characterSet = isPet ? 
                characterSetManager.getPetCharacterSet(setName) : 
                characterSetManager.getEnemyCharacterSet(setName);
            
            if (characterSet != null) {
                // Try to get thumbnail from idle animation first
                AnimationSequence idleSeq = characterSet.getIdleAnimation();
                if (idleSeq != null && idleSeq.getFrameCount() > 0) {
                    return idleSeq.getFrames().get(0).getImage();
                }
                
                // If no idle animation, try walking animation
                AnimationSequence walkSeq = characterSet.getWalkingAnimation();
                if (walkSeq != null && walkSeq.getFrameCount() > 0) {
                    return walkSeq.getFrames().get(0).getImage();
                }
                
                // If no walking animation, try any other animation
                AnimationSequence specialSeq = characterSet.getSpecialAnimation();
                if (specialSeq != null && specialSeq.getFrameCount() > 0) {
                    return specialSeq.getFrames().get(0).getImage();
                }
                
                AnimationSequence painSeq = characterSet.getPainAnimation();
                if (painSeq != null && painSeq.getFrameCount() > 0) {
                    return painSeq.getFrames().get(0).getImage();
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting thumbnail for " + setName + ": " + e.getMessage());
        }
        
        return null; // No thumbnail available
    }
    
    private void createNewCharacterSet() {
        String setName = "New_" + currentSetType + "_" + System.currentTimeMillis();
        String setPath = "resources/CharacterSets/" + 
                         (currentSetType.equals("pet") ? "Pets" : "Enemies") + "/" + setName + "/";
        
        currentWorkingSet = new CharacterSet(setName, setPath);
        
        // Update UI
        setNameField.setText(setName);
        authorField.setText("");
        descriptionArea.setText("");
        
        // Clear animation panels
        idlePanel.clearImages();
        walkingPanel.clearImages();
        specialPanel.clearImages();
        painPanel.clearImages();
        
        System.out.println("Created new character set: " + setName + " at path: " + setPath);
    }
    
    private void loadExistingCharacterSet(String setName) {
        try {
            // Load the SPECIFIC character set that was selected, not the current one
            currentWorkingSet = currentSetType.equals("pet") ? 
                characterSetManager.getPetCharacterSet(setName) : 
                characterSetManager.getEnemyCharacterSet(setName);
            
            if (currentWorkingSet != null) {
                System.out.println("Loading character set: " + setName + " (Type: " + currentSetType + ")");
                
                // Update UI with character set data
                setNameField.setText(currentWorkingSet.getName());
                authorField.setText(currentWorkingSet.getAuthorName());
                descriptionArea.setText(currentWorkingSet.getDescription());
                
                // Load animation frames into panels
                loadAnimationFramesIntoPanel(currentWorkingSet.getIdleAnimation(), idlePanel);
                loadAnimationFramesIntoPanel(currentWorkingSet.getWalkingAnimation(), walkingPanel);
                loadAnimationFramesIntoPanel(currentWorkingSet.getSpecialAnimation(), specialPanel);
                loadAnimationFramesIntoPanel(currentWorkingSet.getPainAnimation(), painPanel);
                
                // Automatically switch the pet to use this character set
                if (currentSetType.equals("pet")) {
                    characterSetManager.setCurrentPetCharacterSet(setName);
                    
                    // Force reload animations on all pets to apply the new character set
                    for (AdvancedDesktopPet pet : AdvancedDesktopPet.getAllPets()) {
                        pet.reloadAnimations(); // This will trigger auto-resize
                    }
                    
                    System.out.println("Automatically switched pet to character set: " + setName);
                } else {
                    characterSetManager.setCurrentEnemyCharacterSet(setName);
                    
                    // Reload enemy images with new character set
                    for (AdvancedDesktopPet pet : AdvancedDesktopPet.getAllPets()) {
                        pet.switchCharacterSet(setName, false);
                    }
                    
                    System.out.println("Automatically switched enemies to character set: " + setName);
                }
                
            } 
            
        } catch (Exception e) {
            System.out.println("Error loading character set: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading character set: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadAnimationFramesIntoPanel(AnimationSequence sequence, AnimationImportPanel panel) {
        panel.clearImages();
        
        for (AnimationFrame frame : sequence.getFrames()) {
            panel.addImage(frame.getImage());
        }
    }
    
    private void saveCurrentCharacterSet() {
        if (currentWorkingSet == null) {
            System.out.println("Cannot save: currentWorkingSet is null");
            return;
        }
        
        try {
            // Update character set with UI data
            String newName = setNameField.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Character set name cannot be empty!", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            currentWorkingSet.setName(newName);
            currentWorkingSet.setAuthorName(authorField.getText().trim());
            currentWorkingSet.setDescription(descriptionArea.getText().trim());
            
            // Update the set path if the name changed
            String newPath = "resources/CharacterSets/" + 
                           (currentSetType.equals("pet") ? "Pets" : "Enemies") + "/" + newName + "/";
            currentWorkingSet.setSetPath(newPath);
            
            System.out.println("Saving character set: " + newName + " to " + newPath);
            
            // Create directory structure
            boolean dirCreated = CharacterFileManager.createCharacterSetDirectoryStructure(newPath);
            if (!dirCreated) {
                throw new Exception("Failed to create directory structure");
            }
            
            // Save animation frames
            saveAnimationFrames(currentWorkingSet.getIdleAnimation(), idlePanel, "idle");
            saveAnimationFrames(currentWorkingSet.getWalkingAnimation(), walkingPanel, "walking");
            saveAnimationFrames(currentWorkingSet.getSpecialAnimation(), specialPanel, "special");
            saveAnimationFrames(currentWorkingSet.getPainAnimation(), painPanel, "pain");
            
            // Save metadata
            boolean metadataSaved = CharacterFileManager.saveCharacterSetMetadata(currentWorkingSet);
            if (!metadataSaved) {
                throw new Exception("Failed to save metadata");
            }
            
            // Add to manager
            if (currentSetType.equals("pet")) {
                characterSetManager.addPetCharacterSet(currentWorkingSet);
                System.out.println("Added pet character set to manager: " + newName);
            } else {
                characterSetManager.addEnemyCharacterSet(currentWorkingSet);
                System.out.println("Added enemy character set to manager: " + newName);
            }
            
            // Force the manager to reload from disk to ensure persistence
            characterSetManager.loadCharacterSetsFromDisk();
            
            JOptionPane.showMessageDialog(this, "Character set '" + newName + "' saved successfully!", 
                                        "Success", JOptionPane.INFORMATION_MESSAGE);
            
            refreshExistingSetsList();
            
        } catch (Exception e) {
            System.out.println("Error saving character set: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving character set: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveAnimationFrames(AnimationSequence sequence, AnimationImportPanel panel, String animationType) {
        // Clear existing frames
        sequence.getFrames().clear();
        
        // Add new frames from panel
        List<ImageIcon> images = panel.getImages();
        for (int i = 0; i < images.size(); i++) {
            ImageIcon image = images.get(i);
            String framePath = currentWorkingSet.getSetPath() + animationType + "/" + 
                              animationType + "_frame_" + String.format("%03d", i) + ".png";
            
            // Actually save the image to disk
            boolean saved = saveImageIconToFile(image, framePath);
            if (saved) {
                AnimationFrame frame = new AnimationFrame(image, framePath, 150); // Default duration
                sequence.addFrame(frame);
                System.out.println("Saved frame: " + framePath);
            } else {
                System.out.println("Failed to save frame: " + framePath);
            }
        }
    }
    
    /**
     * Save an ImageIcon to a file on disk
     */
    private boolean saveImageIconToFile(ImageIcon imageIcon, String filePath) {
        try {
            // Create parent directories if they don't exist
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // Convert ImageIcon to BufferedImage
            Image image = imageIcon.getImage();
            BufferedImage bufferedImage = new BufferedImage(
                image.getWidth(null), 
                image.getHeight(null), 
                BufferedImage.TYPE_INT_ARGB
            );
            
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            
            // Save as PNG file
            return ImageIO.write(bufferedImage, "png", file);
            
        } catch (Exception e) {
            System.out.println("Error saving image to file: " + e.getMessage());
            return false;
        }
    }
    
    private void deleteCurrentCharacterSet() {
        if (currentWorkingSet == null) return;
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this character set?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                String setName = currentWorkingSet.getName();
                String setPath = currentWorkingSet.getSetPath();
                
                // Delete from disk
                CharacterFileManager.deleteCharacterSet(setPath);
                
                // Remove from manager
                boolean removed = false;
                if (currentSetType.equals("pet")) {
                    removed = characterSetManager.removePetCharacterSet(setName);
                } else {
                    removed = characterSetManager.removeEnemyCharacterSet(setName);
                }
                
                if (removed) {
                    JOptionPane.showMessageDialog(this, "Character set '" + setName + "' deleted successfully!", 
                                                "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Character set deleted from disk but could not be removed from memory.", 
                                                "Partial Success", JOptionPane.WARNING_MESSAGE);
                }
                
                // Create new set
                createNewCharacterSet();
                refreshExistingSetsList();
                
            } catch (Exception e) {
                System.out.println("Error deleting character set: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error deleting character set: " + e.getMessage(), 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void testCharacterSetInPet() {
        if (currentWorkingSet == null) return;
        
        // First save the current set
        saveCurrentCharacterSet();
        
        try {
            // Switch to this character set
            if (currentSetType.equals("pet")) {
                characterSetManager.setCurrentPetCharacterSet(currentWorkingSet.getName());
            } else {
                characterSetManager.setCurrentEnemyCharacterSet(currentWorkingSet.getName());
            }
            
            // Force reload animations to apply the new character set
            if (!AdvancedDesktopPet.getAllPets().isEmpty()) {
                AdvancedDesktopPet mainPet = AdvancedDesktopPet.getAllPets().get(0);
                mainPet.reloadAnimations(); // This will trigger auto-resize
            }
            
            String message = "Character set applied to pet! Check your desktop pet.\n" +
                            "The pet window has been automatically resized to fit your character images.";
            
            if (currentSetType.equals("enemy")) {
                message += "\n\nNOTE: To see enemy characters, you need to enable the enemy system in Settings > Horror Mode > Enable Enemies";
            }
            
            JOptionPane.showMessageDialog(this, 
                message, 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            System.out.println("Error testing character set: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error testing character set: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void importImagesDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image Files", "png", "jpg", "jpeg", "gif"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            
            // Show dialog to select animation type
            String[] options = {"Idle", "Walking", "Special", "Pain"};
            String selectedType = (String) JOptionPane.showInputDialog(this, 
                "Select animation type for these images:", "Animation Type", 
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            
            if (selectedType != null) {
                importImagesForAnimation(selectedFiles, selectedType.toLowerCase());
            }
        }
    }
    
    private void importImagesForAnimation(File[] files, String animationType) {
        try {
            AnimationImportPanel targetPanel = null;
            
            switch (animationType) {
                case "idle":
                    targetPanel = idlePanel;
                    break;
                case "walking":
                    targetPanel = walkingPanel;
                    break;
                case "special":
                    targetPanel = specialPanel;
                    break;
                case "pain":
                    targetPanel = painPanel;
                    break;
            }
            
            if (targetPanel != null) {
                for (File file : files) {
                    if (CharacterFileManager.isValidImageFile(file)) {
                        ImageIcon image = CharacterFileManager.loadAndScaleImagePreserveAspect(file, 256);
                        if (image != null) {
                            targetPanel.addImage(image);
                        }
                    }
                }
                
                JOptionPane.showMessageDialog(this, "Images imported successfully!", 
                                            "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            System.out.println("Error importing images: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error importing images: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportCharacterSet() {
        if (currentWorkingSet == null) return;
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            
            try {
                // Export functionality would be implemented here
                JOptionPane.showMessageDialog(this, getText("export_functionality_coming_soon"), 
                                            getText("info"), JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                System.out.println("Error exporting character set: " + e.getMessage());
                JOptionPane.showMessageDialog(this, getText("error_exporting_character_set") + e.getMessage(), 
                                            getText("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Open character edit dialog for modifying properties
     */
    private void openCharacterEditDialog() {
        if (currentWorkingSet == null) {
            JOptionPane.showMessageDialog(this, getText("please_create_or_select_character_set_first"), 
                                        getText("no_character_set"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        CharacterEditDialog editDialog = new CharacterEditDialog(this, currentWorkingSet, currentSetType);
        editDialog.setVisible(true);
        
        if (editDialog.isConfirmed()) {
            // Refresh the UI with updated data
            setNameField.setText(currentWorkingSet.getName());
            authorField.setText(currentWorkingSet.getAuthorName());
            descriptionArea.setText(currentWorkingSet.getDescription());
            
            // Update set type if changed
            String newType = editDialog.getSelectedType();
            if (!newType.equals(currentSetType)) {
                currentSetType = newType;
                setTypeCombo.setSelectedIndex(newType.equals("pet") ? 0 : 1);
                refreshExistingSetsList();
            }
            
            // Refresh animation panels if images were modified
            loadAnimationFramesIntoPanel(currentWorkingSet.getIdleAnimation(), idlePanel);
            loadAnimationFramesIntoPanel(currentWorkingSet.getWalkingAnimation(), walkingPanel);
            loadAnimationFramesIntoPanel(currentWorkingSet.getSpecialAnimation(), specialPanel);
            loadAnimationFramesIntoPanel(currentWorkingSet.getPainAnimation(), painPanel);
        }
    }
    
    private String currentPreviewType = "";
    
    private void stopPreview() {
        previewTimer.stop();
        previewLabel.setIcon(null);
        previewLabel.setText(getText("preview_stopped"));
        currentPreviewType = "";
        System.out.println("Preview stopped");
    }
    
    private void startPreview(String animationType) {
        if (currentWorkingSet == null) return;
        
        previewTimer.stop();
        currentPreviewType = animationType;
        
        AnimationSequence sequence = currentWorkingSet.getAnimationByName(animationType);
        if (sequence != null && sequence.getFrameCount() > 0) {
            previewFrameIndex = 0;
            sequence.reset();
            previewTimer.start();
            System.out.println("Started preview for: " + animationType + " with " + sequence.getFrameCount() + " frames");
        } else {
            // If no character set sequence, try to get from panels
            AnimationImportPanel panel = getAnimationPanel(animationType);
            if (panel != null && panel.getImageCount() > 0) {
                previewFrameIndex = 0;
                previewTimer.start();
                System.out.println("Started preview from panel for: " + animationType + " with " + panel.getImageCount() + " images");
            } else {
                System.out.println("No images found for preview: " + animationType);
            }
        }
    }
    
    private AnimationImportPanel getAnimationPanel(String animationType) {
        switch (animationType.toLowerCase()) {
            case "idle": return idlePanel;
            case "walking": return walkingPanel;
            case "special": return specialPanel;
            case "pain": return painPanel;
            default: return null;
        }
    }
    
    private void updatePreview() {
        if (currentWorkingSet == null || currentPreviewType.isEmpty()) return;
        
        try {
            // First try to get from character set
            AnimationSequence sequence = currentWorkingSet.getAnimationByName(currentPreviewType);
            if (sequence != null && sequence.getFrameCount() > 0) {
                AnimationFrame frame = sequence.getFrames().get(previewFrameIndex);
                if (frame != null && frame.getImage() != null) {
                    // Scale image to fit preview panel
                    Image img = frame.getImage().getImage();
                    Image scaledImg = img.getScaledInstance(PREVIEW_SIZE, PREVIEW_SIZE, Image.SCALE_SMOOTH);
                    previewLabel.setIcon(new ImageIcon(scaledImg));
                    
                    // Move to next frame
                    previewFrameIndex++;
                    if (previewFrameIndex >= sequence.getFrameCount()) {
                        previewFrameIndex = 0; // Loop back to start
                    }
                    
                    // Update timer delay based on frame duration
                    previewTimer.setDelay(frame.getDuration());
                    return;
                }
            }
            
            // If character set doesn't have frames, try to get from panels
            AnimationImportPanel panel = getAnimationPanel(currentPreviewType);
            if (panel != null && panel.getImageCount() > 0) {
                List<ImageIcon> images = panel.getImages();
                if (previewFrameIndex < images.size()) {
                    ImageIcon frameImage = images.get(previewFrameIndex);
                    if (frameImage != null) {
                        // Scale image to fit preview panel
                        Image img = frameImage.getImage();
                        Image scaledImg = img.getScaledInstance(PREVIEW_SIZE, PREVIEW_SIZE, Image.SCALE_SMOOTH);
                        previewLabel.setIcon(new ImageIcon(scaledImg));
                        
                        // Move to next frame
                        previewFrameIndex++;
                        if (previewFrameIndex >= images.size()) {
                            previewFrameIndex = 0; // Loop back to start
                        }
                        
                        // Use default delay for panel images
                        previewTimer.setDelay(200);
                        return;
                    }
                }
            }
            
            // If we get here, no valid frames found
            previewTimer.stop();
            previewLabel.setIcon(null);
            previewLabel.setText(getText("no_frames_available"));
            System.out.println("No frames available for preview: " + currentPreviewType);
            
        } catch (Exception e) {
            System.out.println("Error in preview update: " + e.getMessage());
            previewTimer.stop();
        }
    }
}

/**
 * Character Edit Dialog for modifying character properties
 */
class CharacterEditDialog extends JDialog {
    private CharacterSet characterSet;
    private String originalType;
    private boolean confirmed = false;
    
    private JTextField nameField;
    private JTextField authorField;
    private JTextArea descriptionField;
    private JComboBox<String> typeCombo;
    private JButton flipAllIdleBtn;
    private JButton flipAllWalkingBtn;
    private JButton flipAllSpecialBtn;
    private JButton flipAllPainBtn;
    private JButton replaceIdleBtn;
    private JButton replaceWalkingBtn;
    private JButton replaceSpecialBtn;
    private JButton replaceePainBtn;
    
    public CharacterEditDialog(JFrame parent, CharacterSet characterSet, String currentType) {
        super(parent, "Edit Character Properties", true);
        this.characterSet = characterSet;
        this.originalType = currentType;
        
        initializeUI();
        loadCurrentData();
    }
    
    private void initializeUI() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        // Create main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Name field
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        nameField = new JTextField(20);
        mainPanel.add(nameField, gbc);
        
        // Author field
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        authorField = new JTextField(20);
        mainPanel.add(authorField, gbc);
        
        // Type combo
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        typeCombo = new JComboBox<>(new String[]{"pet", "enemy"});
        mainPanel.add(typeCombo, gbc);
        
        // Description field
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 0.3;
        descriptionField = new JTextArea(3, 20);
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionField);
        mainPanel.add(scrollPane, gbc);
        
        // Add flip and replace panels
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 0.4;
        
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actionPanel.add(createFlipPanel());
        actionPanel.add(createReplacePanel());
        mainPanel.add(actionPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        okButton.addActionListener(e -> {
            saveChanges();
            confirmed = true;
            dispose();
        });
        
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Load current data
        loadCurrentData();
    }
    
    private JPanel createFlipPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Flip Images Horizontally"));
        
        flipAllIdleBtn = new JButton("Flip Idle");
        flipAllWalkingBtn = new JButton("Flip Walking");
        flipAllSpecialBtn = new JButton("Flip Special");
        flipAllPainBtn = new JButton("Flip Pain");
        
        flipAllIdleBtn.addActionListener(e -> flipAnimationImages("idle"));
        flipAllWalkingBtn.addActionListener(e -> flipAnimationImages("walking"));
        flipAllSpecialBtn.addActionListener(e -> flipAnimationImages("special"));
        flipAllPainBtn.addActionListener(e -> flipAnimationImages("pain"));
        
        panel.add(flipAllIdleBtn);
        panel.add(flipAllWalkingBtn);
        panel.add(flipAllSpecialBtn);
        panel.add(flipAllPainBtn);
        
        return panel;
    }
    
    private JPanel createReplacePanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Replace Animation Images"));
        
        replaceIdleBtn = new JButton("Replace Idle");
        replaceWalkingBtn = new JButton("Replace Walking");  
        replaceSpecialBtn = new JButton("Replace Special");
        replaceePainBtn = new JButton("Replace Pain");
        
        replaceIdleBtn.addActionListener(e -> replaceAnimationImages("idle"));
        replaceWalkingBtn.addActionListener(e -> replaceAnimationImages("walking"));
        replaceSpecialBtn.addActionListener(e -> replaceAnimationImages("special"));
        replaceePainBtn.addActionListener(e -> replaceAnimationImages("pain"));
        
        panel.add(replaceIdleBtn);
        panel.add(replaceWalkingBtn);
        panel.add(replaceSpecialBtn);
        panel.add(replaceePainBtn);
        
        return panel;
    }
    
    private void loadCurrentData() {
        if (characterSet != null) {
            nameField.setText(characterSet.getName());
            authorField.setText(characterSet.getAuthorName());
            descriptionField.setText(characterSet.getDescription());
            typeCombo.setSelectedItem(originalType);
        }
    }
    
    private void saveChanges() {
        if (characterSet != null) {
            characterSet.setName(nameField.getText());
            characterSet.setAuthorName(authorField.getText());
            characterSet.setDescription(descriptionField.getText());
        }
    }
    
    private void flipAnimationImages(String animationType) {
        AnimationSequence sequence = characterSet.getAnimationByName(animationType);
        if (sequence == null || sequence.getFrameCount() == 0) {
            JOptionPane.showMessageDialog(this, "No " + animationType + " images to flip.", 
                                        "No Images", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Flip all images in the sequence
        for (AnimationFrame frame : sequence.getFrames()) {
            ImageIcon flippedImage = flipImageHorizontally(frame.getImage());
            frame.setImage(flippedImage);
        }
        
        JOptionPane.showMessageDialog(this, "Flipped " + sequence.getFrameCount() + " " + animationType + " images.", 
                                    "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void replaceAnimationImages(String animationType) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "png", "jpg", "jpeg", "gif"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            if (selectedFiles.length > 0) {
                // Replace animation sequence
                AnimationSequence sequence = characterSet.getAnimationByName(animationType);
                if (sequence == null) {
                    // Create new sequence if it doesn't exist
                    sequence = new AnimationSequence(animationType, animationType.equals("idle") || animationType.equals("walking"));
                    
                    // Add to character set
                    if (animationType.equals("idle")) {
                        characterSet.setIdleAnimation(sequence);
                    } else if (animationType.equals("walking")) {
                        characterSet.setWalkingAnimation(sequence);
                    } else if (animationType.equals("special")) {
                        characterSet.setSpecialAnimation(sequence);
                    } else if (animationType.equals("pain")) {
                        characterSet.setPainAnimation(sequence);
                    }
                }
                
                // Clear existing frames
                sequence.getFrames().clear();
                
                // Add new frames
                for (File file : selectedFiles) {
                    try {
                        ImageIcon imageIcon = new ImageIcon(file.getPath());
                        AnimationFrame frame = new AnimationFrame(imageIcon, file.getPath(), 200); // Default 200ms duration
                        sequence.addFrame(frame);
                    } catch (Exception e) {
                        System.out.println("Error loading image: " + file.getName() + " - " + e.getMessage());
                    }
                }
                
                JOptionPane.showMessageDialog(this, "Replaced " + animationType + " animation with " + 
                                            sequence.getFrameCount() + " new images.", 
                                            "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private ImageIcon flipImageHorizontally(ImageIcon original) {
        BufferedImage originalImage = new BufferedImage(
            original.getIconWidth(), original.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = originalImage.createGraphics();
        g2d.drawImage(original.getImage(), 0, 0, null);
        g2d.dispose();
        
        BufferedImage flippedImage = new BufferedImage(
            originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2dFlipped = flippedImage.createGraphics();
        g2dFlipped.drawImage(originalImage, originalImage.getWidth(), 0, -originalImage.getWidth(), 
                           originalImage.getHeight(), null);
        g2dFlipped.dispose();
        
        return new ImageIcon(flippedImage);
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public String getSelectedType() {
        return (String) typeCombo.getSelectedItem();
    }
}

/**
 * Panel for importing animation frames
 */
class AnimationImportPanel extends JPanel {
    private String animationName;
    private boolean isLooping;
    private List<ImageIcon> images;
    private JPanel imagePanel;
    private JScrollPane scrollPane;
    
    public AnimationImportPanel(String animationName, boolean isLooping) {
        this.animationName = animationName;
        this.isLooping = isLooping;
        this.images = new ArrayList<>();
        
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(animationName + (isLooping ? " (Loop)" : " (Once)")));
        
        // Create image panel
        imagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scrollPane = new JScrollPane(imagePanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(0, 100));
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Add button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Images");
        JButton clearButton = new JButton("Clear");
        JButton flipButton = new JButton("Flip All");
        
        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(flipButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Event handlers
        addButton.addActionListener(e -> addImagesDialog());
        clearButton.addActionListener(e -> clearImages());
        flipButton.addActionListener(e -> flipAllImages());
    }
    
    private void addImagesDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image Files", "png", "jpg", "jpeg", "gif"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            
            for (File file : selectedFiles) {
                if (CharacterFileManager.isValidImageFile(file)) {
                    ImageIcon image = CharacterFileManager.loadAndScaleImagePreserveAspect(file, 256);
                    if (image != null) {
                        addImage(image);
                    }
                }
            }
        }
    }
    
    public void addImage(ImageIcon image) {
        images.add(image);
        
        // Create thumbnail for display
        JLabel thumbnail = new JLabel(image);
        thumbnail.setBorder(BorderFactory.createRaisedBevelBorder());
        thumbnail.setPreferredSize(new Dimension(80, 80));
        
        // Add remove functionality
        thumbnail.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = getImageIndex(thumbnail);
                    if (index >= 0) {
                        removeImage(index);
                    }
                }
            }
        });
        
        imagePanel.add(thumbnail);
        imagePanel.revalidate();
        imagePanel.repaint();
    }
    
    public void clearImages() {
        images.clear();
        imagePanel.removeAll();
        imagePanel.revalidate();
        imagePanel.repaint();
    }
    
    public List<ImageIcon> getImages() {
        return new ArrayList<>(images);
    }
    
    public int getImageCount() {
        return images.size();
    }
    
    private int getImageIndex(JLabel thumbnail) {
        Component[] components = imagePanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] == thumbnail) {
                return i;
            }
        }
        return -1;
    }
    
    private void removeImage(int index) {
        if (index >= 0 && index < images.size()) {
            images.remove(index);
            imagePanel.remove(index);
            imagePanel.revalidate();
            imagePanel.repaint();
        }
    }
    
    /**
     * Flip all images horizontally for correct face direction
     */
    private void flipAllImages() {
        if (images.isEmpty()) return;
        
        try {
            // Create new flipped images
            List<ImageIcon> flippedImages = new ArrayList<>();
            
            for (ImageIcon original : images) {
                ImageIcon flipped = flipImageHorizontally(original);
                if (flipped != null) {
                    flippedImages.add(flipped);
                }
            }
            
            // Replace original images with flipped ones
            images.clear();
            images.addAll(flippedImages);
            
            // Update UI
            imagePanel.removeAll();
            for (ImageIcon flippedImage : flippedImages) {
                JLabel thumbnail = new JLabel(flippedImage);
                thumbnail.setBorder(BorderFactory.createRaisedBevelBorder());
                thumbnail.setPreferredSize(new Dimension(80, 80));
                
                // Add remove functionality
                thumbnail.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            int index = getImageIndex(thumbnail);
                            if (index >= 0) {
                                removeImage(index);
                            }
                        }
                    }
                });
                
                imagePanel.add(thumbnail);
            }
            
            imagePanel.revalidate();
            imagePanel.repaint();
            
            System.out.println("Flipped " + flippedImages.size() + " images in " + animationName);
            
        } catch (Exception e) {
            System.out.println("Error flipping images: " + e.getMessage());
        }
    }
    
    /**
     * Flip an image horizontally
     */
    private ImageIcon flipImageHorizontally(ImageIcon original) {
        if (original == null) return null;
        
        try {
            Image img = original.getImage();
            int width = img.getWidth(null);
            int height = img.getHeight(null);
            
            BufferedImage flipped = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = flipped.createGraphics();
            
            // Apply horizontal flip transformation
            g2d.drawImage(img, width, 0, -width, height, null);
            g2d.dispose();
            
            return new ImageIcon(flipped);
            
        } catch (Exception e) {
            System.out.println("Error flipping image: " + e.getMessage());
            return original;
        }
    }
}

// Enemy class that follows the pet
class EnemyWindow extends JWindow {
    // Constants
    private static final int DEFAULT_WIDTH = 128;
    private static final int DEFAULT_HEIGHT = 128;
    
    private JLabel enemyLabel;
    private Timer followTimer;
    private Timer horrorEffectTimer;
    private Timer animationTimer;
    private AdvancedDesktopPet targetPet;
    
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
    
    // Music system - now managed by MusicManager
    private static boolean musicEnabled = true;
    
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
    
    // Enemy-specific variables
    private int enemyWidth = DEFAULT_WIDTH;
    private int enemyHeight = DEFAULT_HEIGHT;
    private float enemyTransparency = 1.0f;
    private boolean enemyFacingRight = true;
    private ImageIcon currentEnemyImage;
    private int currentAnimationFrame = 0;
    private Point lastLocation;
    private int flickerCount = 0;
    private long lastCollisionTime = 0; // Track last collision time for cooldown
    private static final long COLLISION_COOLDOWN_MS = 2000; // 2 second cooldown between collisions
    
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
        // Set label bounds to exactly match the enemy window size
        enemyLabel.setBounds(0, 0, enemyWidth, enemyHeight);
        enemyLabel.setPreferredSize(new Dimension(enemyWidth, enemyHeight));
        add(enemyLabel);
        
        // Also add mouse listener to the label itself to ensure clicks are captured
        enemyLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("ENEMY LABEL CLICK DETECTED: button=" + e.getButton() + 
                                   ", clickCount=" + e.getClickCount() + 
                                   ", x=" + e.getX() + ", y=" + e.getY() + 
                                   ", source=" + e.getSource().getClass().getName() +
                                   ", Enemy ID: " + EnemyWindow.this.hashCode());
                
                // Only trigger pain mode on real left mouse button click
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                    System.out.println("ENEMY: Label clicked by user! Starting pain mode...");
                    startEnemyPainAnimation();
                } else {
                    System.out.println("ENEMY: Label click ignored - not a left single click (button=" + e.getButton() + ", clicks=" + e.getClickCount() + ")");
                }
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("ENEMY LABEL MOUSE PRESSED: button=" + e.getButton() + ", Enemy ID: " + EnemyWindow.this.hashCode());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("ENEMY LABEL MOUSE RELEASED: button=" + e.getButton() + ", Enemy ID: " + EnemyWindow.this.hashCode());
            }
        });
        
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
        
        // Add mouse listener for enemy click handling
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("ENEMY CLICK DETECTED: button=" + e.getButton() + 
                                   ", clickCount=" + e.getClickCount() + 
                                   ", x=" + e.getX() + ", y=" + e.getY() + 
                                   ", source=" + e.getSource().getClass().getName() +
                                   ", Enemy ID: " + EnemyWindow.this.hashCode());
                
                // Only trigger pain mode on real left mouse button click
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                    System.out.println("ENEMY: Clicked by user! Starting pain mode...");
                    startEnemyPainAnimation();
                } else {
                    System.out.println("ENEMY: Click ignored - not a left single click (button=" + e.getButton() + ", clicks=" + e.getClickCount() + ")");
                }
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("ENEMY MOUSE PRESSED: button=" + e.getButton() + ", Enemy ID: " + EnemyWindow.this.hashCode());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("ENEMY MOUSE RELEASED: button=" + e.getButton() + ", Enemy ID: " + EnemyWindow.this.hashCode());
            }
        });
        
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
        try {
            if (followTimer != null) {
                followTimer.stop();
            }
            followTimer = new Timer(100, e -> {
                try {
                    followPet();
                } catch (Exception ex) {
                    System.out.println("Error in followPet timer: " + ex.getMessage());
                    // Restart timer if it fails
                    if (followTimer != null) {
                        followTimer.restart();
                    }
                }
            });
            followTimer.start();
        } catch (Exception e) {
            System.out.println("Error starting follow timer: " + e.getMessage());
        }
    }
    
    private void startHorrorEffects() {
        try {
            if (horrorEffectTimer != null) {
                horrorEffectTimer.stop();
            }
            horrorEffectTimer = new Timer(3000 + random.nextInt(4000), e -> {
                try {
                    // Don't create horror effects if enemy is in pain mode
                    if (!isEnemyPainActive) {
                        createHorrorEffect();
                    }
                    // Randomize next horror effect timing
                    if (horrorEffectTimer != null) {
                        horrorEffectTimer.setDelay(2000 + random.nextInt(6000));
                    }
                } catch (Exception ex) {
                    System.out.println("Error in horror effect timer: " + ex.getMessage());
                    // Restart timer if it fails
                    if (horrorEffectTimer != null) {
                        horrorEffectTimer.restart();
                    }
                }
            });
            horrorEffectTimer.start();
        } catch (Exception e) {
            System.out.println("Error starting horror effect timer: " + e.getMessage());
        }
    }
    
    private void startAnimation() {
        if (enemyImages.size() > 1) {
            try {
                if (animationTimer != null) {
                    animationTimer.stop();
                }
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
                        if (animationTimer != null) {
                            animationTimer.setDelay(300 + random.nextInt(1200));
                        }
                    } catch (Exception ex) {
                        System.out.println("Error in enemy animation: " + ex.getMessage());
                        // Restart timer if it fails
                        if (animationTimer != null) {
                            animationTimer.restart();
                        }
                    }
                });
                animationTimer.start();
            } catch (Exception e) {
                System.out.println("Error starting animation timer: " + e.getMessage());
            }
        }
    }
    
    private void followPet() {
        if (targetPet == null) return;
        
        // Don't move if enemy is in pain mode
        if (isEnemyPainActive) {
            return;
        }
        
        try {
            Point petLocation = targetPet.getLocation();
            Point currentLocation = getLocation();
            
            // Validate locations
            if (petLocation == null || currentLocation == null) {
                return;
            }
            
            // --- Improved collision detection: trigger pain effect only on actual touch ---
            // Calculate distance to pet
            double distance = Math.sqrt(Math.pow(petLocation.x - currentLocation.x, 2) + 
                                       Math.pow(petLocation.y - currentLocation.y, 2));
            
            // Calculate dynamic distance threshold based on pet and enemy sizes
            // Use the larger of pet width/height and enemy width/height as base, then add some padding
            int petMaxSize = Math.max(targetPet.getWidth(), targetPet.getHeight());
            int enemyMaxSize = Math.max(getWidth(), getHeight());
            int maxSize = Math.max(petMaxSize, enemyMaxSize);
            int distanceThreshold = maxSize / 2; // Half the size of the larger object
            
                    // Debug size information (only print occasionally to avoid spam)
        if (random.nextInt(1000) == 0) { // 0.1% chance to print debug info (further reduced)
            System.out.println("Size debug - Pet: " + targetPet.getWidth() + "x" + targetPet.getHeight() + 
                             ", Enemy: " + getWidth() + "x" + getHeight() + 
                             ", Max size: " + maxSize + ", Distance threshold: " + distanceThreshold + "px");
        }
            
            // Only trigger pain if enemy is within the dynamic threshold and rectangles actually overlap
            if (distance <= distanceThreshold) {
                Rectangle enemyRect = new Rectangle(currentLocation.x, currentLocation.y, getWidth(), getHeight());
                Rectangle petRect = new Rectangle(petLocation.x, petLocation.y, targetPet.getWidth(), targetPet.getHeight());
                
                if (enemyRect.intersects(petRect)) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastCollisionTime > COLLISION_COOLDOWN_MS && targetPet.isVulnerableToPain()) {
                        System.out.println("COLLISION DETECTED! Distance: " + String.format("%.1f", distance) + 
                                         "px (threshold: " + distanceThreshold + "px), Enemy at (" + currentLocation.x + "," + currentLocation.y + 
                                         "), Pet at (" + petLocation.x + "," + petLocation.y + ")");
                        targetPet.startPainAnimation();
                        lastCollisionTime = currentTime;
                    } else if (currentTime - lastCollisionTime <= COLLISION_COOLDOWN_MS) {
                        System.out.println("Collision ignored due to cooldown - Distance: " + String.format("%.1f", distance) + 
                                         "px (threshold: " + distanceThreshold + "px), Time since last collision: " + (currentTime - lastCollisionTime) + "ms");
                    } else if (!targetPet.isVulnerableToPain()) {
                        System.out.println("Collision ignored - Pet is not vulnerable (in pain or power mode) - Distance: " + String.format("%.1f", distance) + 
                                         "px (threshold: " + distanceThreshold + "px)");
                    }
                } else {
                    System.out.println("Close but no collision - Distance: " + String.format("%.1f", distance) + 
                                     "px (threshold: " + distanceThreshold + "px), Rectangles don't overlap");
                }
            }
            // --- End collision detection ---
            
            // Calculate dynamic follow distance based on sizes
            int followDistance = maxSize * 3 / 4; // 75% of the larger object's size
            
            // Follow pet but maintain some distance (don't get too close)
            if (distance > followDistance) {
                // Move towards pet
                int stepSize = 2 + random.nextInt(3); // Random step size for creepy movement
                int dx = petLocation.x - currentLocation.x;
                int dy = petLocation.y - currentLocation.y;
                    
                // Removed excessive follow logging to reduce spam
                
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
                updateEnemyDirection(dx); // Pass the actual dx value, not inverted
                
                // Validate new location before setting it
                Point newLocation = new Point(currentLocation.x + dx, currentLocation.y + dy);
                if (isValidLocation(newLocation)) {
                    setLocation(newLocation);
                }
            } else {
                // If too close, make pet shake and occasionally move away (stalking behavior)
                if (random.nextInt(30) == 0) {
                    targetPet.createHorrorShake(); // Make pet shake when enemy is close
                }
                
                if (random.nextInt(20) == 0) {
                    int escapeX = random.nextInt(6) - 3;
                    int escapeY = random.nextInt(6) - 3;
                    Point newLocation = new Point(currentLocation.x + escapeX, currentLocation.y + escapeY);
                    
                    if (isValidLocation(newLocation)) {
                        setLocation(newLocation);
                        // Update direction for escape movement
                        updateEnemyDirection(escapeX); // Pass actual escape direction
                    }
                }
            }
            
            // Update last location for next frame
            lastLocation = new Point(currentLocation);
        } catch (Exception e) {
            // Try to recover by restarting timers
            try {
                restartTimers();
            } catch (Exception ex) {
                // Silent recovery
            }
        }
    }
    
    // Helper method to validate location
    private boolean isValidLocation(Point location) {
        try {
            // Check if location is within reasonable bounds
            return location != null && 
                   location.x > -10000 && location.x < 10000 && 
                   location.y > -10000 && location.y < 10000;
        } catch (Exception e) {
            return false;
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
    
    // Check if enemy is truly stuck (only for severe cases)
    public boolean isStuck() {
        try {
            // Only consider truly problematic cases, not normal stationary behavior
            // Check if enemy has been in exactly the same position for a very long time
            if (lastLocation != null) {
                Point currentLocation = getLocation();
                // Only consider stuck if enemy hasn't moved at all for a very long time
                // This allows for normal horror effects and stalking behavior
                return currentLocation.equals(lastLocation);
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error checking if enemy is stuck: " + e.getMessage());
            return false; // Don't assume stuck if we can't check
        }
    }
    
    // Check if enemy has been stuck in one position for too long (more aggressive detection)
    public boolean isStuckForTooLong() {
        try {
            if (lastLocation != null) {
                Point currentLocation = getLocation();
                // If enemy hasn't moved for more than 30 seconds, consider it stuck
                // This is more aggressive than the basic isStuck() check
                if (currentLocation.equals(lastLocation)) {
                    // Use a simple time-based check - if enemy has been in same position
                    // for more than 30 seconds, it's probably stuck
                    long currentTime = System.currentTimeMillis();
                    long creationTime = this.hashCode(); // Use hashCode as timestamp proxy
                    long stuckTime = currentTime - creationTime;
                    
                    // Consider stuck if in same position for more than 30 seconds
                    return stuckTime > 30000; // 30 seconds
                }
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error checking if enemy is stuck for too long: " + e.getMessage());
            return false;
        }
    }
    
    // Restart all timers for recovery
    public void restartTimers() {
        try {
            System.out.println("Restarting timers for enemy: " + this.hashCode());
            
            // Stop existing timers first
            stopAllTimers();
            
            // Restart timers
            startFollowing();
            startHorrorEffects();
            startAnimation();
            
            System.out.println("Timers restarted successfully for enemy: " + this.hashCode());
        } catch (Exception e) {
            System.out.println("Error restarting timers: " + e.getMessage());
        }
    }
    
    // Check if enemy has been running too long (prevent memory leaks)
    public boolean hasBeenRunningTooLong() {
        try {
            // Enemies should be automatically despawned after 20-60 seconds
            // If they're still running after 5 minutes, something is wrong
            long currentTime = System.currentTimeMillis();
            long creationTime = this.hashCode(); // Use hashCode as a simple timestamp proxy
            long runningTime = currentTime - creationTime;
            
            // If enemy has been running for more than 5 minutes, consider it too long
            return runningTime > 300000; // 5 minutes in milliseconds
        } catch (Exception e) {
            System.out.println("Error checking enemy running time: " + e.getMessage());
            return false;
        }
    }
    
    // Check if enemy has been completely broken for a very long time (only remove after multiple recovery attempts fail)
    public boolean hasBeenCompletelyBrokenForTooLong() {
        try {
            // Only consider completely broken if enemy has null timers AND has been running for a very long time
            // This gives multiple recovery attempts before giving up
            if (hasNullTimers()) {
                long currentTime = System.currentTimeMillis();
                long creationTime = this.hashCode();
                long runningTime = currentTime - creationTime;
                
                // Only consider completely broken after 5 minutes of being non-functional
                return runningTime > 300000; // 5 minutes in milliseconds
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error checking if enemy is completely broken: " + e.getMessage());
            return false;
        }
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
            // Update size using independent enemy sizing from the pet
            enemyWidth = targetPet.enemyWidth;
            enemyHeight = targetPet.enemyHeight;
            setSize(enemyWidth, enemyHeight);
            
            // Update label bounds to match new window size
            if (enemyLabel != null) {
                enemyLabel.setBounds(0, 0, enemyWidth, enemyHeight);
                enemyLabel.setPreferredSize(new Dimension(enemyWidth, enemyHeight));
            }
            
            System.out.println("Enemy size updated to independent: " + enemyWidth + "x" + enemyHeight + 
                             " (Pet size: " + targetPet.petWidth + "x" + targetPet.petHeight + ")");
            
            // Scale the current enemy image to match the new independent size
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
            
            // Force repaint to ensure proper display at new size
            revalidate();
            repaint();
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
        boolean shouldFaceRight = dx > 0; // Face right if moving right (dx > 0)
        
        if (shouldFaceRight && !enemyFacingRight) {
            // Should face right but currently facing left
            enemyFacingRight = true;
            updateEnemySprite();
        } else if (!shouldFaceRight && enemyFacingRight) {
            // Should face left but currently facing right
            enemyFacingRight = false;
            updateEnemySprite();
        }
        // Add debug info only when direction changes
        if (shouldFaceRight != enemyFacingRight) {
            System.out.println("Enemy direction check - dx: " + dx + ", should face: " + (shouldFaceRight ? "RIGHT" : "LEFT") + ", currently facing: " + (enemyFacingRight ? "RIGHT" : "LEFT"));
        }
    }
    
    // Update enemy sprite with correct direction
    private void updateEnemySprite() {
        if (currentEnemyImage != null) {
            enemyLabel.setIcon(getFlippedEnemyIcon(currentEnemyImage));
        }
    }
    
    /**
     * Update enemy with new image list (for character set switching)
     */
    public void updateEnemyImages(List<ImageIcon> newImages) {
        if (newImages != null && !newImages.isEmpty()) {
            // Update the image list
            this.enemyImages = new ArrayList<>(newImages);
            
            // Update current image to first one from new set
            this.currentEnemyImage = newImages.get(0);
            
            // Update the display
            updateEnemySprite();
            
            // Updated enemy with new character set images (" + newImages.size() + " images)
        }
    }
    
    // Enemy pain state variables
    private boolean isEnemyPainActive = false;
    private int enemyPainCycleCount = 0;
    private static final int ENEMY_MAX_PAIN_CYCLES = 3;
    private Timer enemyPainTimer;
    
    /**
     * Start enemy pain animation when clicked
     */
    private void startEnemyPainAnimation() {
        System.out.println("ENEMY: startEnemyPainAnimation() called - ID: " + EnemyWindow.this.hashCode() + ", isEnemyPainActive: " + isEnemyPainActive);
        
        if (isEnemyPainActive) {
            System.out.println("ENEMY: Already in pain mode, ignoring click");
            return;
        }
        
        System.out.println("ENEMY: Starting pain mode (3 cycles) - ID: " + EnemyWindow.this.hashCode());
        isEnemyPainActive = true;
        System.out.println("ENEMY: isEnemyPainActive set to TRUE - ID: " + EnemyWindow.this.hashCode());
        enemyPainCycleCount = 0;
        
        // Add visual feedback - make enemy flicker red or change appearance
        startEnemyPainVisualEffect();
        
        // Stop all movement timers immediately
        if (followTimer != null && followTimer.isRunning()) {
            followTimer.stop();
        }
        if (horrorEffectTimer != null && horrorEffectTimer.isRunning()) {
            horrorEffectTimer.stop();
        }
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        
        // Start pain cycle timer (similar to pet pain system)
        if (enemyPainTimer != null) {
            enemyPainTimer.stop();
        }
        
        enemyPainTimer = new Timer(500, e -> { // 500ms per cycle
            enemyPainCycleCount++;
            
            if (enemyPainCycleCount >= ENEMY_MAX_PAIN_CYCLES) {
                // Pain mode ended, restart normal behavior
                System.out.println("ENEMY: Pain completed, returning to normal - ID: " + EnemyWindow.this.hashCode());
                stopEnemyPainAnimation();
            }
        });
        enemyPainTimer.start();
    }
    
    /**
     * Start visual pain effect for enemy
     */
    private void startEnemyPainVisualEffect() {
        System.out.println("ENEMY: Starting visual pain effect - ID: " + EnemyWindow.this.hashCode());
        
        // Immediate visual feedback - make enemy flash red or change color
        enemyLabel.setBackground(Color.RED);
        enemyLabel.setOpaque(true);
        repaint();
        
        // Create a pain effect timer that makes the enemy flicker or change appearance
        Timer painVisualTimer = new Timer(200, new ActionListener() {
            private int flickerCount = 0;
            private boolean isVisible = true;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isEnemyPainActive) {
                    // Pain ended, restore normal appearance
                    setVisible(true);
                    enemyLabel.setOpaque(false);
                    enemyLabel.setBackground(null);
                    repaint();
                    ((Timer) e.getSource()).stop();
                    return;
                }
                
                flickerCount++;
                
                // Flicker effect - alternate between visible and invisible
                isVisible = !isVisible;
                setVisible(isVisible);
                
                // Stop flickering after 6 cycles (1.2 seconds total)
                if (flickerCount >= 6) {
                    setVisible(true);
                    enemyLabel.setOpaque(false);
                    enemyLabel.setBackground(null);
                    repaint();
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        painVisualTimer.start();
    }
    
    /**
     * Stop enemy pain animation and return to normal
     */
    private void stopEnemyPainAnimation() {
        if (!isEnemyPainActive) return;
        
        System.out.println("ENEMY: Pain mode ended, returning to normal - ID: " + EnemyWindow.this.hashCode());
        isEnemyPainActive = false;
        System.out.println("ENEMY: isEnemyPainActive set to FALSE - ID: " + EnemyWindow.this.hashCode());
        enemyPainCycleCount = 0;
        
        // Stop pain timer
        if (enemyPainTimer != null && enemyPainTimer.isRunning()) {
            enemyPainTimer.stop();
        }
        
        // Restart all timers
        if (followTimer != null && !followTimer.isRunning()) {
            followTimer.start();
        }
        if (horrorEffectTimer != null && !horrorEffectTimer.isRunning()) {
            horrorEffectTimer.start();
        }
        if (animationTimer != null && !animationTimer.isRunning()) {
            animationTimer.start();
        }
    }
} 