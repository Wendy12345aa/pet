// NOTE: This is a template test file. To use this:
// 1. Download JUnit JAR from the URL in run_tests.bat
// 2. The LocationUtils class is a new extracted utility class
// 3. Run: run_tests.bat

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.*;
import java.util.Random;

/**
 * Unit tests for LocationUtils class
 * This demonstrates how much easier it is to test extracted business logic
 */
public class LocationUtilsTest {
    
    private Rectangle mockScreenBounds;
    private Point testLocation;
    private int testPetWidth;
    private int testPetHeight;
    private Random testRandom;
    
    @BeforeEach
    void setUp() {
        // Create test data
        mockScreenBounds = new Rectangle(0, 0, 1920, 1080);  // Standard screen
        testLocation = new Point(100, 100);
        testPetWidth = 128;
        testPetHeight = 128;
        testRandom = new Random(42); // Fixed seed for predictable tests
    }
    
    @Test
    void testIsLocationValid() {
        // Test valid locations
        assertTrue(LocationUtils.isLocationValid(new Point(0, 0)));
        assertTrue(LocationUtils.isLocationValid(new Point(1000, 500)));
        assertTrue(LocationUtils.isLocationValid(new Point(-100, -100))); // Slightly off-screen is OK
        
        // Test invalid locations
        assertFalse(LocationUtils.isLocationValid(null));
        assertFalse(LocationUtils.isLocationValid(new Point(-50000, 0))); // Way off-screen
        assertFalse(LocationUtils.isLocationValid(new Point(0, -50000))); // Way off-screen
        assertFalse(LocationUtils.isLocationValid(new Point(50000, 50000))); // Way off-screen
    }
    
    @Test
    void testGetPrimaryScreenBounds() {
        Rectangle bounds = LocationUtils.getPrimaryScreenBounds();
        
        // Should return a valid rectangle
        assertNotNull(bounds);
        assertTrue(bounds.width > 0);
        assertTrue(bounds.height > 0);
        
        // Should be the same every time (stable)
        Rectangle bounds2 = LocationUtils.getPrimaryScreenBounds();
        assertEquals(bounds, bounds2);
    }
    
    @Test
    void testGetCombinedScreenBounds() {
        Rectangle combined = LocationUtils.getCombinedScreenBounds();
        
        // Should return a valid rectangle
        assertNotNull(combined);
        assertTrue(combined.width > 0);
        assertTrue(combined.height > 0);
        
        // Combined bounds should contain or equal primary bounds
        Rectangle primary = LocationUtils.getPrimaryScreenBounds();
        assertTrue(combined.width >= primary.width);
        assertTrue(combined.height >= primary.height);
    }
    
    @Test
    void testGetPrimaryScreenCenter() {
        Point center = LocationUtils.getPrimaryScreenCenter(testPetWidth, testPetHeight);
        
        assertNotNull(center);
        
        // Center should be roughly in the middle of the screen
        Rectangle bounds = LocationUtils.getPrimaryScreenBounds();
        int expectedX = bounds.x + (bounds.width - testPetWidth) / 2;
        int expectedY = bounds.y + (bounds.height - testPetHeight) / 2;
        
        assertEquals(expectedX, center.x);
        assertEquals(expectedY, center.y);
    }
    
    @Test
    void testGetSafeLocation() {
        Point safeLocation = LocationUtils.getSafeLocation(testPetWidth, testPetHeight);
        
        assertNotNull(safeLocation);
        
        // Safe location should be valid
        assertTrue(LocationUtils.isLocationValid(safeLocation));
        
        // Should be on primary screen
        Rectangle primaryBounds = LocationUtils.getPrimaryScreenBounds();
        assertTrue(safeLocation.x >= primaryBounds.x);
        assertTrue(safeLocation.y >= primaryBounds.y);
        assertTrue(safeLocation.x + testPetWidth <= primaryBounds.x + primaryBounds.width);
        assertTrue(safeLocation.y + testPetHeight <= primaryBounds.y + primaryBounds.height);
    }
    
    @Test
    void testEnsurePetFullyVisibleOnCurrentScreen() {
        Rectangle testBounds = new Rectangle(0, 0, 1000, 800);
        
        // Test location already visible - should not change
        Point visibleLocation = new Point(100, 100);
        Point result = LocationUtils.ensurePetFullyVisibleOnCurrentScreen(visibleLocation, testPetWidth, testPetHeight);
        assertEquals(visibleLocation, result);
        
        // Test location too far right - should be clamped
        Point rightLocation = new Point(900, 100);  // Pet would extend to 1028, beyond 1000
        Point rightResult = LocationUtils.ensurePetFullyVisibleOnCurrentScreen(rightLocation, testPetWidth, testPetHeight);
        assertEquals(1000 - testPetWidth, rightResult.x); // Should be clamped to fit
        assertEquals(100, rightResult.y); // Y should not change
        
        // Test location too far down - should be clamped
        Point bottomLocation = new Point(100, 700);  // Pet would extend to 828, beyond 800
        Point bottomResult = LocationUtils.ensurePetFullyVisibleOnCurrentScreen(bottomLocation, testPetWidth, testPetHeight);
        assertEquals(100, bottomResult.x); // X should not change
        assertEquals(800 - testPetHeight, bottomResult.y); // Should be clamped to fit
        
        // Test location too far left - should be clamped
        Point leftLocation = new Point(-50, 100);
        Point leftResult = LocationUtils.ensurePetFullyVisibleOnCurrentScreen(leftLocation, testPetWidth, testPetHeight);
        assertEquals(0, leftResult.x); // Should be clamped to screen edge
        assertEquals(100, leftResult.y); // Y should not change
        
        // Test location too far up - should be clamped
        Point topLocation = new Point(100, -50);
        Point topResult = LocationUtils.ensurePetFullyVisibleOnCurrentScreen(topLocation, testPetWidth, testPetHeight);
        assertEquals(100, topResult.x); // X should not change
        assertEquals(0, topResult.y); // Should be clamped to screen edge
    }
    
    @Test
    void testGetClosestValidLocation() {
        // Test single-screen mode
        Point requestedLocation = new Point(-100, -100); // Off-screen
        Point closestSingle = LocationUtils.getClosestValidLocation(requestedLocation, testPetWidth, testPetHeight, false);
        
        assertNotNull(closestSingle);
        assertTrue(LocationUtils.isLocationValid(closestSingle));
        
        // Should be clamped to screen bounds
        Rectangle primaryBounds = LocationUtils.getPrimaryScreenBounds();
        assertTrue(closestSingle.x >= primaryBounds.x);
        assertTrue(closestSingle.y >= primaryBounds.y);
    }
    
    @Test
    void testGetSafeTarget() {
        Point currentLocation = new Point(500, 400);
        Point safeTarget = LocationUtils.getSafeTarget(currentLocation, testPetWidth, testPetHeight, false, testRandom);
        
        assertNotNull(safeTarget);
        assertTrue(LocationUtils.isLocationValid(safeTarget));
        
        // Target should be different from current location (not stuck)
        assertNotEquals(currentLocation, safeTarget);
        
        // Target should be reasonably far from current location (at least 150 pixels)
        double distance = currentLocation.distance(safeTarget);
        assertTrue(distance >= 150, "Target should be at least 150 pixels away, was: " + distance);
    }
    
    @Test
    void testIsMovementValid() {
        Point current = new Point(500, 400);
        Point newLocationValid = new Point(510, 410); // Small valid movement
        Point newLocationInvalid = new Point(50000, 50000); // Way off-screen
        
        // Test valid movement (single-screen mode)
        boolean validResult = LocationUtils.isMovementValid(current, newLocationValid, testPetWidth, testPetHeight, false, 600, 500);
        assertTrue(validResult);
        
        // Test invalid movement (single-screen mode)
        boolean invalidResult = LocationUtils.isMovementValid(current, newLocationInvalid, testPetWidth, testPetHeight, false, 600, 500);
        assertFalse(invalidResult);
    }
    
    @Test
    void testIsLocationPartiallyVisible() {
        // Test with default pet size
        Point visibleLocation = new Point(100, 100);
        assertTrue(LocationUtils.isLocationPartiallyVisible(visibleLocation));
        
        // Test with custom pet size
        assertTrue(LocationUtils.isLocationPartiallyVisible(visibleLocation, testPetWidth, testPetHeight));
        
        // Test off-screen location
        Point offScreenLocation = new Point(-50000, -50000);
        assertFalse(LocationUtils.isLocationPartiallyVisible(offScreenLocation, testPetWidth, testPetHeight));
    }
    
    @Test
    void testIsLocationOnAnyScreen() {
        // Test location on screen
        Point onScreenLocation = new Point(100, 100);
        assertTrue(LocationUtils.isLocationOnAnyScreen(onScreenLocation, testPetWidth, testPetHeight));
        
        // Test location off all screens
        Point offScreenLocation = new Point(-50000, -50000);
        assertFalse(LocationUtils.isLocationOnAnyScreen(offScreenLocation, testPetWidth, testPetHeight));
    }
    
    @Test
    void testFindScreenForLocation() {
        Point locationOnScreen = new Point(100, 100);
        Rectangle screenForLocation = LocationUtils.findScreenForLocation(locationOnScreen);
        
        assertNotNull(screenForLocation);
        assertTrue(screenForLocation.contains(locationOnScreen));
        
        // Test off-screen location - should return primary screen
        Point offScreenLocation = new Point(-50000, -50000);
        Rectangle screenForOffLocation = LocationUtils.findScreenForLocation(offScreenLocation);
        assertNotNull(screenForOffLocation);
        assertEquals(LocationUtils.getPrimaryScreenBounds(), screenForOffLocation);
    }
    
    @Test
    void testIsPetTrulyLost() {
        Rectangle testCombinedBounds = new Rectangle(0, 0, 1920, 1080);
        
        // Test pet that's only slightly off-screen (not truly lost)
        Point slightlyOff = new Point(-50, -50);
        assertFalse(LocationUtils.isPetTrulyLost(slightlyOff, testCombinedBounds));
        
        // Test pet that's way off-screen (truly lost)
        Point wayOff = new Point(-50000, -50000);
        assertTrue(LocationUtils.isPetTrulyLost(wayOff, testCombinedBounds));
        
        // Test pet that's on-screen (definitely not lost)
        Point onScreen = new Point(500, 400);
        assertFalse(LocationUtils.isPetTrulyLost(onScreen, testCombinedBounds));
    }
    
    @Test
    void testEdgeCases() {
        // Test with very small pet size
        Point result1 = LocationUtils.getSafeLocation(1, 1);
        assertNotNull(result1);
        assertTrue(LocationUtils.isLocationValid(result1));
        
        // Test with very large pet size
        Point result2 = LocationUtils.getSafeLocation(5000, 5000);
        assertNotNull(result2);
        assertTrue(LocationUtils.isLocationValid(result2));
        
        // Test with zero size (edge case)
        Point result3 = LocationUtils.getSafeLocation(0, 0);
        assertNotNull(result3);
        assertTrue(LocationUtils.isLocationValid(result3));
    }
} 