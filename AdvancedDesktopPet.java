import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.UnsupportedLookAndFeelException;

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
    private List<ImageIcon> enemyImages = new ArrayList<>();
    private Random enemyRandom = new Random();
    private int maxEnemies = 3;
    
    // Tray
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    
    // Settings
    private int petWidth = DEFAULT_WIDTH;
    private int petHeight = DEFAULT_HEIGHT;
    private boolean soundEnabled = true;
    private float transparency = 1.0f; // 0.0 = invisible, 1.0 = opaque
    private static List<AdvancedDesktopPet> allPets = new ArrayList<>();
    private JFrame settingsWindow = null;
    private boolean allowCrossScreen = true; // Allow movement between screens
    private JWindow floatingShortcut = null; // Cyberpunk floating shortcut
    
    public AdvancedDesktopPet() {
        allPets.add(this); // Register this pet
        initializePet();
        loadAnimations();
        setupWindow();
        setupTrayIcon();
        createFloatingShortcut(); // Add cyberpunk floating shortcut
        startTimers();
    }
    
    private void initializePet() {
        setAlwaysOnTop(true);
        setSize(petWidth, petHeight);
        // JWindow is already undecorated by default
        setBackground(new Color(0, 0, 0, 0));
        
        // Position on primary screen safely
        Rectangle screenBounds = getPrimaryScreenBounds();
        int centerX = screenBounds.x + (screenBounds.width - petWidth) / 2;
        int centerY = screenBounds.y + (screenBounds.height - petHeight) / 2;
        setLocation(centerX, centerY);
        
        petLabel = new JLabel();
        petLabel.setHorizontalAlignment(JLabel.CENTER);
        petLabel.setVerticalAlignment(JLabel.CENTER);
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
    
    private void loadEnemyImages() {
        enemyImages.clear();
        
        // Try to load enemy images from Image folder - using the specific chibi enemy names
        String[] enemyFiles = {"Image\\敌人chibi01.png", "Image\\敌人chibi02.png", "Image\\敌人chibi03.png"};
        
        for (String filename : enemyFiles) {
            ImageIcon enemyImage = loadEnemyImageSafely(filename);
            if (enemyImage != null) {
                enemyImages.add(enemyImage);
                System.out.println("Loaded enemy image: " + filename);
            }
        }
        
        // Also try alternative naming patterns
        if (enemyImages.isEmpty()) {
            for (int i = 1; i <= 3; i++) {
                String filename = "Image\\enemy0" + i + ".png";
                ImageIcon enemyImage = loadEnemyImageSafely(filename);
                if (enemyImage != null) {
                    enemyImages.add(enemyImage);
                    System.out.println("Loaded enemy image: " + filename);
                }
            }
        }
        
        // If no enemy images found, create default scary enemies
        if (enemyImages.isEmpty()) {
            System.out.println("No enemy images found, creating default scary enemies...");
            enemyImages.add(createDefaultEnemyAnimation(new Color(255, 0, 0, 200), "X"));
            enemyImages.add(createDefaultEnemyAnimation(new Color(0, 0, 0, 200), "!"));
            enemyImages.add(createDefaultEnemyAnimation(new Color(128, 0, 128, 200), "?"));
        }
        
        System.out.println("Total enemy images loaded: " + enemyImages.size());
    }
    
    private ImageIcon loadEnemyImageSafely(String filename) {
        try {
            System.out.println("Attempting to load enemy image: " + filename);
            
            // First try to load from resources
            java.net.URL resource = getClass().getResource("/" + filename);
            ImageIcon icon = null;
            
            if (resource != null) {
                System.out.println("Found in resources: " + filename);
                icon = new ImageIcon(resource);
            } else {
                // Try to load from current directory
                File file = new File(filename);
                System.out.println("Checking file exists: " + filename + " -> " + file.exists());
                System.out.println("Absolute path: " + file.getAbsolutePath());
                
                if (file.exists()) {
                    System.out.println("Loading from file: " + filename);
                    icon = new ImageIcon(filename);
                } else {
                    System.out.println("File not found: " + filename);
                    
                    // Try alternative encoding/path approaches
                    String[] alternatives = {
                        filename.replace("敌人", "enemy"),
                        filename.replace("/", "\\"),
                        "Image\\" + filename.substring(Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\")) + 1),
                        filename
                    };
                    
                    for (String alt : alternatives) {
                        File altFile = new File(alt);
                        System.out.println("Trying alternative: " + alt + " -> " + altFile.exists());
                        if (altFile.exists()) {
                            icon = new ImageIcon(alt);
                            System.out.println("Successfully loaded alternative: " + alt);
                            break;
                        }
                    }
                }
            }
            
            // Scale the enemy image
            if (icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                System.out.println("Successfully loaded and scaling: " + filename);
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            } else {
                System.out.println("Failed to load or invalid image: " + filename);
            }
            
        } catch (Exception e) {
            System.out.println("Exception loading enemy image " + filename + ": " + e.getMessage());
            e.printStackTrace();
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
            
            // Scale the image to fit the pet window if loaded successfully
            if (icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(petWidth, petHeight, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }
            
        } catch (Exception e) {
            System.out.println("Could not load " + filename + ": " + e.getMessage());
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
        
        Menu settingsMenu = new Menu("Advanced Settings");
        
        CheckboxMenuItem soundItem = new CheckboxMenuItem("Sound Enabled", soundEnabled);
        soundItem.addItemListener(e -> soundEnabled = e.getStateChange() == ItemEvent.SELECTED);
        settingsMenu.add(soundItem);
        
        popup.add(settingsMenu);
        
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
        safetyTimer = new Timer(5000, e -> {
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
        
        // Enemy spawn timer - spawn enemies at random intervals
        enemySpawnTimer = new Timer(10000 + enemyRandom.nextInt(20000), e -> {
            if (enemyEnabled && enemies.size() < maxEnemies) {
                spawnEnemy();
            }
            // Randomize next spawn time
            enemySpawnTimer.setDelay(8000 + enemyRandom.nextInt(15000));
        });
        enemySpawnTimer.start();
        
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
            return;
        }
        
        System.out.println("Spawning enemy... Current enemies: " + enemies.size());
        
        EnemyWindow enemy = new EnemyWindow(this, enemyImages);
        enemies.add(enemy);
        
        // Remove enemy after some time (20-60 seconds)
        Timer despawnTimer = new Timer(20000 + enemyRandom.nextInt(40000), e -> {
            if (enemies.contains(enemy)) {
                enemy.stopEnemy();
                enemies.remove(enemy);
                System.out.println("Enemy despawned. Remaining enemies: " + enemies.size());
            }
            ((Timer) e.getSource()).stop();
        });
        despawnTimer.start();
    }
    
    private void stopEnemySystem() {
        if (enemySpawnTimer != null) {
            enemySpawnTimer.stop();
        }
        
        // Stop and remove all enemies
        for (EnemyWindow enemy : enemies) {
            enemy.stopEnemy();
        }
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
        
        return new Point(safeX, safeY);
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
        int largePadding = 100; // Larger padding to avoid edges
        int minX = screenBounds.x + largePadding;
        int maxX = screenBounds.x + screenBounds.width - petWidth - largePadding;
        int minY = screenBounds.y + largePadding;
        int maxY = screenBounds.y + screenBounds.height - petHeight - largePadding;
        
        // Ensure valid bounds
        if (maxX <= minX) {
            minX = screenBounds.x + 50;
            maxX = screenBounds.x + screenBounds.width - petWidth - 50;
        }
        if (maxY <= minY) {
            minY = screenBounds.y + 50;
            maxY = screenBounds.y + screenBounds.height - petHeight - 50;
        }
        
        // Generate target away from current position
        int targetX, targetY;
        do {
            targetX = minX + random.nextInt(Math.max(1, maxX - minX));
            targetY = minY + random.nextInt(Math.max(1, maxY - minY));
        } while (Math.abs(targetX - currentLocation.x) < 150 || Math.abs(targetY - currentLocation.y) < 150);
        
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
                
                // Check boundaries before moving - be more permissive for cross-screen movement
                if (isMovementValid(current, newLocation)) {
                    setLocation(newLocation);
                } else {
                    // If we hit a boundary, try to find a safe location first
                    Point safeLocation = getClosestValidLocation(newLocation);
                    if (safeLocation != null && !safeLocation.equals(current)) {
                        setLocation(safeLocation);
                    } else {
                        // If we can't find a safe location, stop walking and find a new target
                        isWalking = false;
                        updateIdleSprite();
                        ((Timer) e.getSource()).stop();
                        
                        // Schedule a new walk after a short delay
                        Timer retryTimer = new Timer(1000, evt -> {
                            startRandomWalk();
                            ((Timer) evt.getSource()).stop();
                        });
                        retryTimer.start();
                        return;
                    }
                }
                
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
        // Use larger padding to avoid edges better
        int padding = 80; // Increased padding
        int minX = screenBounds.x + padding;
        int maxX = screenBounds.x + screenBounds.width - petWidth - padding;
        int minY = screenBounds.y + padding;
        int maxY = screenBounds.y + screenBounds.height - petHeight - padding;
        
        // Ensure valid bounds with fallback
        if (maxX <= minX) {
            minX = screenBounds.x + 30;
            maxX = screenBounds.x + screenBounds.width - petWidth - 30;
        }
        if (maxY <= minY) {
            minY = screenBounds.y + 30;
            maxY = screenBounds.y + screenBounds.height - petHeight - 30;
        }
        
        // Ensure we have at least some space to work with
        if (maxX <= minX) maxX = minX + petWidth;
        if (maxY <= minY) maxY = minY + petHeight;
        
        targetX = minX + random.nextInt(Math.max(1, maxX - minX));
        targetY = minY + random.nextInt(Math.max(1, maxY - minY));
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
            
            // Check if the new location is valid before setting it
            if (isLocationValid(newLocation)) {
                setLocation(newLocation);
            } else {
                // If invalid, find the closest valid location
                Point validLocation = getClosestValidLocation(newLocation);
                setLocation(validLocation);
            }
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
        
        // Reload and rescale images
        reloadImagesWithNewSize();
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
                
                // Center icon
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                FontMetrics fm = g2d.getFontMetrics();
                String text = "⚙";
                int x = (80 - fm.stringWidth(text)) / 2;
                int y = (80 + fm.getAscent()) / 2;
                g2d.drawString(text, x, y);
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
    
    private void createCyberpunkSettingsWindow() {
        if (settingsWindow != null) {
            settingsWindow.setVisible(true);
            settingsWindow.toFront();
            return;
        }

        // Create stylish settings window
        settingsWindow = new JFrame();
        settingsWindow.setTitle("Desktop Pet Settings");
        settingsWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        settingsWindow.setSize(480, 500);
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
                                                           480, 500, new Color(35, 20, 45));
                g2d.setPaint(bgGradient);
                g2d.fillRect(0, 0, 480, 500);
                
                // Border
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(80, 80, 120));
                g2d.drawRect(1, 1, 478, 498);
                
                // Title bar
                g2d.setColor(new Color(60, 60, 100, 100));
                g2d.fillRect(0, 0, 480, 30);
                g2d.setStroke(new BasicStroke(1));
                g2d.setColor(new Color(100, 100, 140));
                g2d.drawLine(0, 30, 480, 30);
            }
        };
        
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(35, 15, 15, 15));
        
        // Title panel with minimize button
        JPanel titlePanel = createTitlePanel();
        
        // Content panel with grid layout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Pet Management Section
        addSection(contentPanel, gbc, 0, "Pet Management");
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        JButton duplicateBtn = createButton("Duplicate Pet");
        duplicateBtn.addActionListener(e -> duplicatePet());
        contentPanel.add(duplicateBtn, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        JButton removeBtn = createButton("Remove Pet");
        removeBtn.addActionListener(e -> removePet());
        contentPanel.add(removeBtn, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JLabel petCountLabel = createLabel("Active Pets: " + allPets.size());
        contentPanel.add(petCountLabel, gbc);
        
        // Transparency Section
        addSection(contentPanel, gbc, 3, "Transparency");
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JSlider transparencySlider = createSlider(0, 100, (int)(transparency * 100));
        transparencySlider.addChangeListener(e -> {
            transparency = transparencySlider.getValue() / 100.0f;
            updateTransparency();
        });
        contentPanel.add(transparencySlider, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        JButton hideBtn = createButton("Hide Pet");
        hideBtn.addActionListener(e -> {
            setVisible(false);
            settingsWindow.setVisible(false);
        });
        contentPanel.add(hideBtn, gbc);
        
        gbc.gridx = 1; gbc.gridy = 5;
        JButton showBtn = createButton("Show All Pets");
        showBtn.addActionListener(e -> showAllPets());
        contentPanel.add(showBtn, gbc);
        
        // Size Section
        addSection(contentPanel, gbc, 6, "Size");
        
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        JSlider zoomSlider = createSlider(50, 300, (int)((petWidth / (double)DEFAULT_WIDTH) * 100));
        zoomSlider.addChangeListener(e -> {
            int zoomPercent = zoomSlider.getValue();
            updateSize(zoomPercent);
        });
        contentPanel.add(zoomSlider, gbc);
        
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1;
        JButton zoomInBtn = createButton("Zoom In");
        zoomInBtn.addActionListener(e -> {
            int currentZoom = (int)((petWidth / (double)DEFAULT_WIDTH) * 100);
            int newZoom = Math.min(300, currentZoom + 25);
            zoomSlider.setValue(newZoom);
        });
        contentPanel.add(zoomInBtn, gbc);
        
        gbc.gridx = 1; gbc.gridy = 8;
        JButton zoomOutBtn = createButton("Zoom Out");
        zoomOutBtn.addActionListener(e -> {
            int currentZoom = (int)((petWidth / (double)DEFAULT_WIDTH) * 100);
            int newZoom = Math.max(50, currentZoom - 25);
            zoomSlider.setValue(newZoom);
        });
        contentPanel.add(zoomOutBtn, gbc);
        
        // Movement Section
        addSection(contentPanel, gbc, 9, "Movement Settings");
        
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 1;
        JCheckBox crossScreenBox = createCheckBox("Allow Cross-Screen Movement", allowCrossScreen);
        crossScreenBox.addActionListener(e -> allowCrossScreen = crossScreenBox.isSelected());
        contentPanel.add(crossScreenBox, gbc);
        
        gbc.gridx = 1; gbc.gridy = 10;
        JButton testCrossScreenBtn = createButton("Test Cross-Screen");
        testCrossScreenBtn.addActionListener(e -> moveToRandomScreen());
        contentPanel.add(testCrossScreenBtn, gbc);
        
        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 1;
        JCheckBox soundBox = createCheckBox("Sound Enabled", soundEnabled);
        soundBox.addActionListener(e -> soundEnabled = soundBox.isSelected());
        contentPanel.add(soundBox, gbc);
        
        // Horror Section
        addSection(contentPanel, gbc, 12, "Horror Mode");
        
        gbc.gridx = 0; gbc.gridy = 13; gbc.gridwidth = 1;
        JCheckBox enemyBox = createCheckBox("Enable Enemies", enemyEnabled);
        enemyBox.addActionListener(e -> toggleEnemySystem(enemyBox.isSelected()));
        contentPanel.add(enemyBox, gbc);
        
        gbc.gridx = 1; gbc.gridy = 13;
        JButton spawnEnemyBtn = createButton("Spawn Enemy Now");
        spawnEnemyBtn.addActionListener(e -> {
            if (enemyEnabled) {
                spawnEnemy();
            } else {
                JOptionPane.showMessageDialog(settingsWindow, 
                    "Please enable enemies first!", 
                    "Enemies Disabled", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        contentPanel.add(spawnEnemyBtn, gbc);
        
        gbc.gridx = 0; gbc.gridy = 14; gbc.gridwidth = 2;
        JLabel enemyInfoLabel = createLabel("Enemies: " + enemies.size() + " / " + maxEnemies + " active");
        contentPanel.add(enemyInfoLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 15; gbc.gridwidth = 1;
        JButton clearEnemiesBtn = createButton("Clear All Enemies");
        clearEnemiesBtn.addActionListener(e -> stopEnemySystem());
        contentPanel.add(clearEnemiesBtn, gbc);
        
        gbc.gridx = 1; gbc.gridy = 15;
        JButton closeBtn = createButton("Close");
        closeBtn.addActionListener(e -> {
            settingsWindow.setVisible(false);
        });
        contentPanel.add(closeBtn, gbc);
        
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
            petCountLabel.setText("Active Pets: " + allPets.size());
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
                                                           480, 30, new Color(60, 40, 80));
                g2d.setPaint(bgGradient);
                g2d.fillRect(0, 0, 480, 30);
                
                // Border
                g2d.setStroke(new BasicStroke(1));
                g2d.setColor(new Color(100, 100, 120));
                g2d.drawLine(0, 30, 480, 30);
            }
        };
        titlePanel.setOpaque(false);
        titlePanel.setPreferredSize(new Dimension(480, 30));
        titlePanel.setLayout(new BorderLayout());
        
        // Title text
        JLabel titleLabel = new JLabel("Desktop Pet Settings");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        // Button panel for minimize and maximize
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        
        // Maximize button
        JButton maximizeBtn = new JButton("□");
        maximizeBtn.setForeground(Color.WHITE);
        maximizeBtn.setBackground(new Color(0, 0, 0, 0));
        maximizeBtn.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        maximizeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        maximizeBtn.setFocusPainted(false);
        maximizeBtn.addActionListener(e -> {
            if (settingsWindow.isVisible()) {
                settingsWindow.setVisible(false);
            } else {
                settingsWindow.setVisible(true);
                settingsWindow.toFront();
            }
        });
        maximizeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                maximizeBtn.setBackground(new Color(100, 100, 100, 100));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                maximizeBtn.setBackground(new Color(0, 0, 0, 0));
            }
        });
        
        // Minimize button
        JButton minimizeBtn = new JButton("−");
        minimizeBtn.setForeground(Color.WHITE);
        minimizeBtn.setBackground(new Color(0, 0, 0, 0));
        minimizeBtn.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        minimizeBtn.setFont(new Font("Arial", Font.BOLD, 16));
        minimizeBtn.setFocusPainted(false);
        minimizeBtn.addActionListener(e -> settingsWindow.setVisible(false));
        minimizeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                minimizeBtn.setBackground(new Color(100, 100, 100, 100));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                minimizeBtn.setBackground(new Color(0, 0, 0, 0));
            }
        });
        
        buttonPanel.add(maximizeBtn);
        buttonPanel.add(minimizeBtn);
        titlePanel.add(buttonPanel, BorderLayout.EAST);
        
        return titlePanel;
    }
    
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(70, 70, 90));
        button.setFont(new Font("Arial", Font.PLAIN, 12));
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
        label.setFont(new Font("Arial", Font.PLAIN, 12));
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
        slider.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        slider.setFocusable(false);
        slider.setForeground(Color.WHITE);
        return slider;
    }
    
    private JCheckBox createCheckBox(String text, boolean selected) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setForeground(Color.WHITE);
        checkBox.setFont(new Font("Arial", Font.PLAIN, 12));
        checkBox.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        checkBox.setOpaque(false);
        checkBox.setFocusable(false);
        checkBox.setSelected(selected);
        return checkBox;
    }
    
    private void addSection(JPanel panel, GridBagConstraints gbc, int y, String title) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        JLabel sectionTitle = createLabel(title);
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle.setForeground(new Color(200, 200, 255)); // Slightly highlighted
        panel.add(sectionTitle, gbc);
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
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
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
        button.setFont(new Font("Arial", Font.BOLD, 14));
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
        label.setFont(new Font("Arial", Font.BOLD, 14));
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
        checkBox.setFont(new Font("Arial", Font.BOLD, 14));
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
    private int enemyWidth = 100;
    private int enemyHeight = 100;
    private ImageIcon currentEnemyImage;
    private List<ImageIcon> enemyImages;
    private int flickerCount = 0;
    private int currentAnimationFrame = 0;
    
    public EnemyWindow(AdvancedDesktopPet pet, List<ImageIcon> images) {
        this.targetPet = pet;
        this.enemyImages = images;
        
        initializeEnemy();
        startFollowing();
        startHorrorEffects();
        startAnimation();
    }
    
    private void initializeEnemy() {
        setAlwaysOnTop(true);
        setSize(enemyWidth, enemyHeight);
        setBackground(new Color(0, 0, 0, 0));
        
        enemyLabel = new JLabel();
        enemyLabel.setHorizontalAlignment(JLabel.CENTER);
        enemyLabel.setVerticalAlignment(JLabel.CENTER);
        add(enemyLabel);
        
        // Load random enemy image
        if (!enemyImages.isEmpty()) {
            currentEnemyImage = enemyImages.get(random.nextInt(enemyImages.size()));
            enemyLabel.setIcon(currentEnemyImage);
        }
        
        // Start at a random position near the pet
        Point petLocation = targetPet.getLocation();
        int offsetX = random.nextInt(400) - 200; // Random offset -200 to +200
        int offsetY = random.nextInt(400) - 200;
        setLocation(petLocation.x + offsetX, petLocation.y + offsetY);
        
        setVisible(true);
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
                currentAnimationFrame = (currentAnimationFrame + 1) % enemyImages.size();
                currentEnemyImage = enemyImages.get(currentAnimationFrame);
                enemyLabel.setIcon(currentEnemyImage);
                
                // Randomize next animation frame timing for creepy effect
                animationTimer.setDelay(300 + random.nextInt(1200));
            });
            animationTimer.start();
        }
    }
    
    private void followPet() {
        if (targetPet == null) return;
        
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
            
            // Normalize movement
            if (Math.abs(dx) > stepSize) dx = dx > 0 ? stepSize : -stepSize;
            if (Math.abs(dy) > stepSize) dy = dy > 0 ? stepSize : -stepSize;
            
            // Add some randomness to movement for creepy effect
            dx += random.nextInt(3) - 1;
            dy += random.nextInt(3) - 1;
            
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
            }
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
        if (enemyImages.size() > 1) {
            ImageIcon newImage;
            do {
                newImage = enemyImages.get(random.nextInt(enemyImages.size()));
            } while (newImage == currentEnemyImage);
            
            currentEnemyImage = newImage;
            enemyLabel.setIcon(currentEnemyImage);
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
                    int frameIndex = rapidCount % enemyImages.size();
                    currentEnemyImage = enemyImages.get(frameIndex);
                    enemyLabel.setIcon(currentEnemyImage);
                    
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
        if (followTimer != null) followTimer.stop();
        if (horrorEffectTimer != null) horrorEffectTimer.stop();
        if (animationTimer != null) animationTimer.stop();
        dispose();
    }
} 