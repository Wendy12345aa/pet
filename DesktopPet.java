import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DesktopPet extends JWindow implements MouseListener, MouseMotionListener {
    private static final int WINDOW_WIDTH = 128;
    private static final int WINDOW_HEIGHT = 128;
    private static final int ANIMATION_DELAY = 150; // milliseconds
    
    private JLabel petLabel;
    private Timer animationTimer;
    private Timer movementTimer;
    private List<ImageIcon> idleFrames;
    private List<ImageIcon> walkFrames;
    private int currentFrame = 0;
    private boolean isWalking = false;
    
    // Mouse dragging variables
    private Point mouseOffset;
    private boolean isDragging = false;
    
    // Random movement variables
    private int targetX, targetY;
    private Random random = new Random();
    
    // Tray icon
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    
    public DesktopPet() {
        initializePet();
        loadAnimations();
        setupWindow();
        setupTrayIcon();
        startAnimations();
        startRandomMovement();
    }
    
    private void initializePet() {
        setAlwaysOnTop(true);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        // JWindow is already undecorated by default
        
        // Make window transparent (Java 7+)
        setBackground(new Color(0, 0, 0, 0));
        
        // Center on screen initially
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(
            (screenSize.width - WINDOW_WIDTH) / 2,
            (screenSize.height - WINDOW_HEIGHT) / 2
        );
        
        petLabel = new JLabel();
        petLabel.setHorizontalAlignment(JLabel.CENTER);
        petLabel.setVerticalAlignment(JLabel.CENTER);
        add(petLabel);
        
        // Add mouse listeners for dragging
        petLabel.addMouseListener(this);
        petLabel.addMouseMotionListener(this);
        
        setVisible(true);
    }
    
    private void loadAnimations() {
        idleFrames = new ArrayList<>();
        walkFrames = new ArrayList<>();
        
        // Create simple colored circles as placeholder animations
        // In a real application, you would load actual sprite images or GIFs
        
        // Idle animation frames (different colors)
        Color[] idleColors = {
            new Color(255, 100, 100, 200),
            new Color(255, 120, 120, 200),
            new Color(255, 80, 80, 200)
        };
        
        for (Color color : idleColors) {
            BufferedImage frame = createCircleImage(color, 80);
            idleFrames.add(new ImageIcon(frame));
        }
        
        // Walk animation frames (moving effect)
        Color[] walkColors = {
            new Color(100, 255, 100, 200),
            new Color(120, 255, 120, 200),
            new Color(80, 255, 80, 200),
            new Color(140, 255, 140, 200)
        };
        
        for (Color color : walkColors) {
            BufferedImage frame = createCircleImage(color, 75);
            walkFrames.add(new ImageIcon(frame));
        }
        
        // Set initial frame
        if (!idleFrames.isEmpty()) {
            petLabel.setIcon(idleFrames.get(0));
        }
    }
    
    private BufferedImage createCircleImage(Color color, int size) {
        BufferedImage image = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw circle
        g2d.setColor(color);
        int x = (WINDOW_WIDTH - size) / 2;
        int y = (WINDOW_HEIGHT - size) / 2;
        g2d.fillOval(x, y, size, size);
        
        // Add cute eyes
        g2d.setColor(Color.BLACK);
        int eyeSize = size / 8;
        int eyeY = y + size / 3;
        g2d.fillOval(x + size / 3 - eyeSize / 2, eyeY, eyeSize, eyeSize);
        g2d.fillOval(x + 2 * size / 3 - eyeSize / 2, eyeY, eyeSize, eyeSize);
        
        // Add mouth
        g2d.setStroke(new BasicStroke(2));
        int mouthY = y + 2 * size / 3;
        g2d.drawArc(x + size / 3, mouthY - size / 8, size / 3, size / 4, 0, -180);
        
        g2d.dispose();
        return image;
    }
    
    private void setupWindow() {
        // JWindow doesn't have setDefaultCloseOperation, handle with window listener
        
        // Add window listener to handle closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }
    
    private void setupTrayIcon() {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray is not supported");
            return;
        }
        
        systemTray = SystemTray.getSystemTray();
        
        // Create tray icon image
        BufferedImage trayImage = createCircleImage(new Color(100, 150, 255, 255), 16);
        trayIcon = new TrayIcon(trayImage, "Desktop Pet");
        trayIcon.setImageAutoSize(true);
        
        // Create popup menu
        PopupMenu popup = new PopupMenu();
        
        MenuItem showItem = new MenuItem("Show Pet");
        showItem.addActionListener(e -> setVisible(true));
        popup.add(showItem);
        
        MenuItem hideItem = new MenuItem("Hide Pet");
        hideItem.addActionListener(e -> setVisible(false));
        popup.add(hideItem);
        
        popup.addSeparator();
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());
        popup.add(exitItem);
        
        trayIcon.setPopupMenu(popup);
        
        // Add double-click listener to show/hide pet
        trayIcon.addActionListener(e -> setVisible(!isVisible()));
        
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added: " + e.getMessage());
        }
    }
    
    private void startAnimations() {
        animationTimer = new Timer(ANIMATION_DELAY, e -> updateAnimation());
        animationTimer.start();
    }
    
    private void startRandomMovement() {
        movementTimer = new Timer(3000 + random.nextInt(4000), e -> startRandomWalk());
        movementTimer.start();
    }
    
    private void updateAnimation() {
        List<ImageIcon> currentFrames = isWalking ? walkFrames : idleFrames;
        
        if (!currentFrames.isEmpty()) {
            currentFrame = (currentFrame + 1) % currentFrames.size();
            petLabel.setIcon(currentFrames.get(currentFrame));
        }
    }
    
    private void startRandomWalk() {
        if (isDragging) return;
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        targetX = random.nextInt(screenSize.width - WINDOW_WIDTH);
        targetY = random.nextInt(screenSize.height - WINDOW_HEIGHT);
        
        isWalking = true;
        
        Timer walkTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Point current = getLocation();
                int dx = targetX - current.x;
                int dy = targetY - current.y;
                
                if (Math.abs(dx) < 5 && Math.abs(dy) < 5) {
                    // Reached target
                    isWalking = false;
                    ((Timer) e.getSource()).stop();
                    return;
                }
                
                // Move towards target
                int stepX = dx == 0 ? 0 : (dx > 0 ? 2 : -2);
                int stepY = dy == 0 ? 0 : (dy > 0 ? 2 : -2);
                
                setLocation(current.x + stepX, current.y + stepY);
            }
        });
        walkTimer.start();
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        mouseOffset = e.getPoint();
        isDragging = true;
        isWalking = false; // Stop random walking when dragging
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        isDragging = false;
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (isDragging) {
            Point windowLocation = getLocation();
            Point mouseOnScreen = e.getLocationOnScreen();
            setLocation(
                mouseOnScreen.x - mouseOffset.x,
                mouseOnScreen.y - mouseOffset.y
            );
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            // Double-click to make pet jump
            Point current = getLocation();
            
            // Jump animation
            Timer jumpTimer = new Timer(20, new ActionListener() {
                int jumpStep = 0;
                int originalY = current.y;
                
                @Override
                public void actionPerformed(ActionEvent evt) {
                    jumpStep++;
                    int jumpHeight = (int) (30 * Math.sin(Math.PI * jumpStep / 20));
                    setLocation(current.x, originalY - jumpHeight);
                    
                    if (jumpStep >= 20) {
                        setLocation(current.x, originalY);
                        ((Timer) evt.getSource()).stop();
                    }
                }
            });
            jumpTimer.start();
        }
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
    
    @Override
    public void mouseMoved(MouseEvent e) {}
    
    private void exitApplication() {
        if (animationTimer != null) animationTimer.stop();
        if (movementTimer != null) movementTimer.stop();
        if (systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
        }
        System.exit(0);
    }
    
    public static void main(String[] args) {
        // Set system look and feel
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
            new DesktopPet();
        });
    }
} 