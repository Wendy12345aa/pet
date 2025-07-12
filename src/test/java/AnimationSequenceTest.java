// NOTE: This is a template test file. To use this:
// 1. Download JUnit JAR from the URL in run_tests.bat
// 2. The AnimationSequence and AnimationFrame classes are in AdvancedDesktopPet.java
// 3. Run: run_tests.bat

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

/**
 * Unit tests for AnimationSequence class
 * This demonstrates how to test the business logic in your desktop pet application
 */
public class AnimationSequenceTest {
    
    private AnimationSequence idleSequence;
    private AnimationSequence walkingSequence;
    private AnimationFrame frame1;
    private AnimationFrame frame2;
    private AnimationFrame frame3;
    
    @BeforeEach
    void setUp() {
        // Create test sequences
        idleSequence = new AnimationSequence("idle", true);      // Looping
        walkingSequence = new AnimationSequence("walking", false); // Non-looping
        
        // Create test frames with dummy images
        frame1 = new AnimationFrame(createDummyImageIcon(), "frame1.png", 100);
        frame2 = new AnimationFrame(createDummyImageIcon(), "frame2.png", 150);
        frame3 = new AnimationFrame(createDummyImageIcon(), "frame3.png", 200);
    }
    
    /**
     * Helper method to create a dummy ImageIcon for testing
     */
    private ImageIcon createDummyImageIcon() {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        return new ImageIcon(image);
    }
    
    @Test
    void testEmptySequence() {
        // Test initial state of empty sequence
        assertEquals(0, idleSequence.getFrameCount());
        assertNull(idleSequence.getCurrentFrame());
        assertNull(idleSequence.nextFrame());
        assertEquals(0, idleSequence.getCurrentFrameIndex());
    }
    
    @Test
    void testSequenceBasicProperties() {
        // Test basic properties
        assertEquals("idle", idleSequence.getName());
        assertTrue(idleSequence.isLoop());
        
        assertEquals("walking", walkingSequence.getName());
        assertFalse(walkingSequence.isLoop());
    }
    
    @Test
    void testAddFrames() {
        // Test adding frames
        idleSequence.addFrame(frame1);
        idleSequence.addFrame(frame2);
        
        assertEquals(2, idleSequence.getFrameCount());
        assertEquals(frame1, idleSequence.getCurrentFrame());
        assertEquals(0, idleSequence.getCurrentFrameIndex());
    }
    
    @Test
    void testFrameNavigation() {
        // Setup sequence with frames
        idleSequence.addFrame(frame1);
        idleSequence.addFrame(frame2);
        idleSequence.addFrame(frame3);
        
        // Test initial state
        assertEquals(frame1, idleSequence.getCurrentFrame());
        assertEquals(0, idleSequence.getCurrentFrameIndex());
        
        // Test next frame
        assertEquals(frame2, idleSequence.nextFrame());
        assertEquals(1, idleSequence.getCurrentFrameIndex());
        
        // Test another next frame
        assertEquals(frame3, idleSequence.nextFrame());
        assertEquals(2, idleSequence.getCurrentFrameIndex());
    }
    
    @Test
    void testLoopingBehavior() {
        // Test looping sequence behavior
        idleSequence.addFrame(frame1);
        idleSequence.addFrame(frame2);
        
        // Navigate to end of sequence
        assertEquals(frame2, idleSequence.nextFrame());
        assertEquals(1, idleSequence.getCurrentFrameIndex());
        
        // Should loop back to start
        assertEquals(frame1, idleSequence.nextFrame());
        assertEquals(0, idleSequence.getCurrentFrameIndex());
    }
    
    @Test
    void testNonLoopingBehavior() {
        // Test non-looping sequence behavior
        walkingSequence.addFrame(frame1);
        walkingSequence.addFrame(frame2);
        
        // Navigate to end of sequence
        assertEquals(frame2, walkingSequence.nextFrame());
        assertEquals(1, walkingSequence.getCurrentFrameIndex());
        
        // Should stay at last frame (not loop)
        assertEquals(frame2, walkingSequence.nextFrame());
        assertEquals(1, walkingSequence.getCurrentFrameIndex());
    }
    
    @Test
    void testReset() {
        // Setup sequence and navigate away from start
        idleSequence.addFrame(frame1);
        idleSequence.addFrame(frame2);
        idleSequence.addFrame(frame3);
        
        // Navigate to middle
        idleSequence.nextFrame();
        idleSequence.nextFrame();
        assertEquals(2, idleSequence.getCurrentFrameIndex());
        
        // Reset should go back to start
        idleSequence.reset();
        assertEquals(0, idleSequence.getCurrentFrameIndex());
        assertEquals(frame1, idleSequence.getCurrentFrame());
    }
    
    @Test
    void testRemoveFrame() {
        // Setup sequence with frames
        idleSequence.addFrame(frame1);
        idleSequence.addFrame(frame2);
        idleSequence.addFrame(frame3);
        
        assertEquals(3, idleSequence.getFrameCount());
        
        // Remove middle frame
        idleSequence.removeFrame(1);
        assertEquals(2, idleSequence.getFrameCount());
        
        // Verify correct frames remain
        assertEquals(frame1, idleSequence.getCurrentFrame());
        assertEquals(frame3, idleSequence.nextFrame());
    }
    
    @Test
    void testRemoveInvalidIndex() {
        // Setup sequence with one frame
        idleSequence.addFrame(frame1);
        
        // Should handle invalid indices gracefully
        idleSequence.removeFrame(-1);  // Negative index
        idleSequence.removeFrame(5);   // Out of bounds
        
        // Frame should still be there
        assertEquals(1, idleSequence.getFrameCount());
        assertEquals(frame1, idleSequence.getCurrentFrame());
    }
    
    @Test
    void testSetLoopProperty() {
        // Test changing loop property
        assertFalse(walkingSequence.isLoop());
        
        walkingSequence.setLoop(true);
        assertTrue(walkingSequence.isLoop());
        
        walkingSequence.setLoop(false);
        assertFalse(walkingSequence.isLoop());
    }
    
    @Test
    void testFrameListAccess() {
        // Test direct access to frames list
        idleSequence.addFrame(frame1);
        idleSequence.addFrame(frame2);
        
        assertEquals(2, idleSequence.getFrames().size());
        assertEquals(frame1, idleSequence.getFrames().get(0));
        assertEquals(frame2, idleSequence.getFrames().get(1));
    }
} 