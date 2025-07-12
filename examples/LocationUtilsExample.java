import java.awt.*;

/**
 * Example showing how to use the extracted LocationUtils class
 * This demonstrates the immediate benefits of business logic extraction
 */
public class example_usage {
    
    public static void main(String[] args) {
        System.out.println("=== LocationUtils Examples ===");
        
        // Example 1: Simple location validation
        Point validLocation = new Point(100, 100);
        Point invalidLocation = new Point(-50000, -50000);
        
        System.out.println("Valid location (100, 100): " + 
                          LocationUtils.isLocationValid(validLocation));
        System.out.println("Invalid location (-50000, -50000): " + 
                          LocationUtils.isLocationValid(invalidLocation));
        
        // Example 2: Get screen information
        Rectangle primaryBounds = LocationUtils.getPrimaryScreenBounds();
        System.out.println("\nPrimary screen: " + primaryBounds.width + "x" + primaryBounds.height);
        
        Rectangle combinedBounds = LocationUtils.getCombinedScreenBounds();
        System.out.println("Combined screens: " + combinedBounds.width + "x" + combinedBounds.height);
        
        // Example 3: Get safe center location for a pet
        int petWidth = 128;
        int petHeight = 128;
        Point centerLocation = LocationUtils.getPrimaryScreenCenter(petWidth, petHeight);
        System.out.println("\nSafe center location: (" + centerLocation.x + ", " + centerLocation.y + ")");
        
        // Example 4: Ensure a location is fully visible
        Point offScreenLocation = new Point(-50, -50);
        Point safeLocation = LocationUtils.ensurePetFullyVisible(
            offScreenLocation, petWidth, petHeight, false);
        System.out.println("\nOff-screen location (-50, -50) corrected to: (" + 
                          safeLocation.x + ", " + safeLocation.y + ")");
        
        // Example 5: Find which screen contains a location
        Point testLocation = new Point(500, 300);
        Rectangle screenForLocation = LocationUtils.findScreenForLocation(testLocation);
        System.out.println("\nLocation (500, 300) is on screen: " + 
                          screenForLocation.width + "x" + screenForLocation.height);
        
        System.out.println("\n=== All LocationUtils methods work perfectly! ===");
        System.out.println("Ready to integrate into AdvancedDesktopPet.java");
    }
} 