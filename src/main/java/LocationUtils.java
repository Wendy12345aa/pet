import java.awt.*;
import java.util.Random;

/**
 * Utility class for location and bounds calculations
 * 
 * This class extracts the location/bounds logic from AdvancedDesktopPet
 * to make it more testable and reusable.
 */
public class LocationUtils {
    
    /**
     * Check if a location is valid (not extremely off-screen)
     */
    public static boolean isLocationValid(Point location) {
        return location != null && 
               location.x > -10000 && location.x < 10000 && 
               location.y > -10000 && location.y < 10000;
    }
    
    /**
     * Check if pet is truly lost (way off screen)
     */
    public static boolean isPetTrulyLost(Point currentLocation, Rectangle combinedBounds) {
        // Allow much more tolerance for cross-screen movement
        int bigTolerance = 200;
        
        boolean wayOffScreen = currentLocation.x < combinedBounds.x - bigTolerance ||
                              currentLocation.y < combinedBounds.y - bigTolerance ||
                              currentLocation.x > combinedBounds.x + combinedBounds.width + bigTolerance ||
                              currentLocation.y > combinedBounds.y + combinedBounds.height + bigTolerance;
        
        // Also check if pet is completely invisible
        boolean completelyInvisible = !isLocationPartiallyVisible(currentLocation);
        
        // Only consider pet "truly lost" if it's way off screen AND completely invisible
        return wayOffScreen && completelyInvisible;
    }
    
    /**
     * Check if any part of the pet is visible on any screen
     */
    public static boolean isLocationPartiallyVisible(Point location, int petWidth, int petHeight) {
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
    
    /**
     * Overloaded method that calls the main method with default dimensions
     */
    public static boolean isLocationPartiallyVisible(Point location) {
        return isLocationPartiallyVisible(location, 128, 128); // Default pet size
    }
    
    /**
     * Check if pet would be completely within any screen
     */
    public static boolean isLocationOnAnyScreen(Point location, int petWidth, int petHeight) {
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
    
    /**
     * Get the primary screen bounds
     */
    public static Rectangle getPrimaryScreenBounds() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice primaryDevice = ge.getDefaultScreenDevice();
        return primaryDevice.getDefaultConfiguration().getBounds();
    }
    
    /**
     * Get combined bounds of all screens
     */
    public static Rectangle getCombinedScreenBounds() {
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
    
    /**
     * Find which screen contains the given location
     */
    public static Rectangle findScreenForLocation(Point location) {
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
    
    /**
     * Get center point of primary screen
     */
    public static Point getPrimaryScreenCenter(int petWidth, int petHeight) {
        Rectangle bounds = getPrimaryScreenBounds();
        return new Point(
            bounds.x + (bounds.width - petWidth) / 2,
            bounds.y + (bounds.height - petHeight) / 2
        );
    }
    
    /**
     * Get a safe location on the primary screen
     */
    public static Point getSafeLocation(int petWidth, int petHeight) {
        Rectangle primaryBounds = getPrimaryScreenBounds();
        
        // Use center of primary screen with some offset
        int safeX = primaryBounds.x + primaryBounds.width / 2 - petWidth / 2;
        int safeY = primaryBounds.y + primaryBounds.height / 2 - petHeight / 2;
        
        Point safeLocation = new Point(safeX, safeY);
        return ensurePetFullyVisible(safeLocation, petWidth, petHeight, false);
    }
    
    /**
     * Get closest valid location for a requested point
     */
    public static Point getClosestValidLocation(Point requestedLocation, int petWidth, int petHeight, boolean allowCrossScreen) {
        if (allowCrossScreen) {
            return getClosestValidLocationOnAnyScreen(requestedLocation, petWidth, petHeight);
        } else {
            // Single-screen behavior - find current screen first
            Rectangle screenBounds = findScreenForLocation(requestedLocation);
            
            int validX = Math.max(screenBounds.x, 
                         Math.min(requestedLocation.x, screenBounds.x + screenBounds.width - petWidth));
            int validY = Math.max(screenBounds.y, 
                         Math.min(requestedLocation.y, screenBounds.y + screenBounds.height - petHeight));
            
            return new Point(validX, validY);
        }
    }
    
    /**
     * Get closest valid location on any screen
     */
    public static Point getClosestValidLocationOnAnyScreen(Point requestedLocation, int petWidth, int petHeight) {
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
        
        return closestLocation != null ? closestLocation : getPrimaryScreenCenter(petWidth, petHeight);
    }
    
    /**
     * Ensure pet is fully visible on screen
     */
    public static Point ensurePetFullyVisible(Point currentLocation, int petWidth, int petHeight, boolean allowCrossScreen) {
        if (allowCrossScreen) {
            return ensurePetFullyVisibleOnAnyScreen(currentLocation, petWidth, petHeight);
        } else {
            return ensurePetFullyVisibleOnCurrentScreen(currentLocation, petWidth, petHeight);
        }
    }
    
    /**
     * Ensure pet is fully visible on current screen
     */
    public static Point ensurePetFullyVisibleOnCurrentScreen(Point currentLocation, int petWidth, int petHeight) {
        Rectangle screenBounds = findScreenForLocation(currentLocation);
        
        // Calculate the bounds of the pet
        int petRight = currentLocation.x + petWidth;
        int petBottom = currentLocation.y + petHeight;
        
        // Check if pet is being cut off
        int newX = currentLocation.x;
        int newY = currentLocation.y;
        
        // Check right edge (pet extends beyond right edge)
        if (petRight > screenBounds.x + screenBounds.width) {
            newX = screenBounds.x + screenBounds.width - petWidth;
        }
        
        // Check bottom edge (pet extends beyond bottom edge)
        if (petBottom > screenBounds.y + screenBounds.height) {
            newY = screenBounds.y + screenBounds.height - petHeight;
        }
        
        // Check left edge (pet extends beyond left edge)
        if (newX < screenBounds.x) {
            newX = screenBounds.x;
        }
        
        // Check top edge (pet extends beyond top edge)
        if (newY < screenBounds.y) {
            newY = screenBounds.y;
        }
        
        return new Point(newX, newY);
    }
    
    /**
     * Ensure pet is fully visible on any screen
     */
    public static Point ensurePetFullyVisibleOnAnyScreen(Point currentLocation, int petWidth, int petHeight) {
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
        
        // Pet is not fully visible on any screen, place in center of primary screen
        return getPrimaryScreenCenter(petWidth, petHeight);
    }
    
    /**
     * Generate a safe target location with padding from edges
     */
    public static Point getSafeTarget(Point currentLocation, int petWidth, int petHeight, boolean allowCrossScreen, Random random) {
        Rectangle screenBounds;
        
        if (allowCrossScreen) {
            // Find which screen the pet is on or use primary
            screenBounds = findScreenForLocation(currentLocation);
        } else {
            screenBounds = findScreenForLocation(currentLocation);
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
        return ensurePetFullyVisible(target, petWidth, petHeight, allowCrossScreen);
    }
    
    /**
     * Check if movement from current to new location is valid
     */
    public static boolean isMovementValid(Point current, Point newLocation, int petWidth, int petHeight, 
                                        boolean allowCrossScreen, int targetX, int targetY) {
        if (allowCrossScreen) {
            // For cross-screen movement, be more permissive
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
            return newDistance < currentDistance || isLocationOnAnyScreen(newLocation, petWidth, petHeight) || (newDistance - currentDistance < 20);
        } else {
            // Single-screen validation with some edge tolerance
            Rectangle screenBounds = findScreenForLocation(current);
            int edgeTolerance = 25; // Allow pet to go slightly beyond screen edge
            
            return newLocation.x >= screenBounds.x - edgeTolerance && 
                   newLocation.y >= screenBounds.y - edgeTolerance && 
                   newLocation.x + petWidth <= screenBounds.x + screenBounds.width + edgeTolerance && 
                   newLocation.y + petHeight <= screenBounds.y + screenBounds.height + edgeTolerance;
        }
    }
} 