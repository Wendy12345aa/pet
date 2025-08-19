/**
 * PetEngine - Main engine that coordinates all pet systems
 * 
 * COORDINATION OF ISSUES:
 * 
 * This engine coordinates all three main issues:
 * 1. Image distortion - handled in drawPet method
 * 2. Teleporting movement - coordinated through movementManager
 * 3. Click teleporting - coordinated through interactionManager
 * 
 * The engine needs to ensure smooth coordination between all systems
 * to prevent conflicts that cause the issues.
 */
class PetEngine {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        
        // Settings
        this.settings = {
            petSize: 256, // ISSUE: Large size may cause scaling issues
            animationSpeed: 150,
            movementSpeed: 1.5, // ENHANCED: Reduced from 3 to 1.5 for smoother movement
            isRunning: false,
            showPositionIndicator: true // ENHANCED: Toggle for position indicator
        };
        
        // Initialize managers
        this.animationManager = new AnimationManager();
        this.movementManager = new MovementManager(canvas);
        this.behaviorManager = new BehaviorManager(this.animationManager, this.movementManager);
        this.interactionManager = new InteractionManager(canvas, this);
        this.bubbleManager = new BubbleManager(canvas);
        
        // Game loop
        this.lastTime = 0;
        this.animationId = null;
        
        // Initialize engine
        this.initialize();
    }
    
    /**
     * Initialize the pet engine
     * 
     * ISSUE: Initialization may cause instant position changes
     * SOLUTION: Use smooth initialization with gradual setup
     */
    async initialize() {
        try {
            console.log('Initializing Pet Engine...');
            
            // Load sprites
            await this.animationManager.loadSprites();
            
            // ENHANCED: Set pet engine reference in movement manager
            this.movementManager.petEngine = this;
            
            // ENHANCED: Force refresh bounds for wide screens
            this.movementManager.refreshBounds();
            
            // Start animation
            this.animationManager.play();
            
            // Position pet at center
            const center = this.movementManager.getCanvasCenter();
            this.movementManager.setPosition(center.x, center.y);
            
            console.log('Pet Engine initialized successfully');
            
        } catch (error) {
            console.error('Error initializing Pet Engine:', error);
        }
    }
    
    /**
     * Start the pet engine
     */
    start() {
        if (this.settings.isRunning) return;
        
        this.settings.isRunning = true;
        this.lastTime = performance.now();
        this.gameLoop();
        
        console.log('Pet Engine started');
    }
    
    /**
     * Stop the pet engine
     */
    stop() {
        this.settings.isRunning = false;
        
        if (this.animationId) {
            cancelAnimationFrame(this.animationId);
            this.animationId = null;
        }
        
        console.log('Pet Engine stopped');
    }
    
    /**
     * Main game loop
     * 
     * ISSUE: Frame rate may be too high causing jerky movement
     * SOLUTION: Use frame-rate independent updates
     */
    gameLoop(currentTime = performance.now()) {
        if (!this.settings.isRunning) return;
        
        // Calculate delta time
        const deltaTime = currentTime - this.lastTime;
        this.lastTime = currentTime;
        
        // Update all systems
        this.update(deltaTime);
        
        // Render
        this.render();
        
        // Continue loop
        this.animationId = requestAnimationFrame(this.gameLoop.bind(this));
    }
    
    /**
     * Update all systems
     * 
     * ENHANCED: Frame-rate independent updates to prevent teleporting
     * SOLUTION: Ensure proper update order and coordination
     */
    update(deltaTime) {
        // Update animation
        this.animationManager.update(performance.now());
        
        // ENHANCED: Pass deltaTime for frame-rate independent movement
        this.movementManager.update(deltaTime);
        
        // Update behavior
        this.behaviorManager.update();
        
        // Update bubbles
        this.bubbleManager.update();
        
        // Try to spawn bubbles when pet is moving
        const position = this.movementManager.getPosition();
        const isMoving = this.movementManager.isPetMoving();
        this.bubbleManager.trySpawnBubble(position.x, position.y, isMoving);
    }
    
    /**
     * Render the pet
     * 
     * ISSUE: Rendering may cause visual artifacts or distortion
     * SOLUTION: Ensure proper rendering order and quality
     */
    render() {
        // Clear canvas
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        
        // REMOVED: Debug test rectangles that were cluttering the display
        
        // Get current position (this is the smooth internal position)
        const position = this.movementManager.getPosition();
        
        // Get current frame
        const sprite = this.animationManager.getCurrentFrame();
        
        // ENHANCED: Draw pet at the same smooth position as the indicator
        // CRITICAL FIX: Always use internal position for both character and indicator
        this.drawPet(sprite, position.x, position.y);
        
        // Draw bubbles
        this.bubbleManager.draw();
        
        // DISABLED: Debug information for clean display
        // this.movementManager.drawDebugInfo();
    }
    
    /**
     * Draw the pet sprite
     * 
     * ISSUE #1: IMAGE DISTORTION - This is where image scaling happens
     * 
     * The main causes of distortion here are:
     * 1. Forced resizing without maintaining aspect ratio
     * 2. Incorrect canvas drawing parameters
     * 3. Missing image quality settings
     * 4. Improper scaling calculations
     * 
     * SOLUTION: Implement proper aspect ratio preservation and smooth scaling
     */
    drawPet(sprite, x, y) {
        // DISABLED: Debug logging for clean console
        // console.log('DEBUG drawPet called with:', { x, y, sprite, spriteType: sprite ? sprite.constructor.name : 'null', canvasSize: { width: this.canvas.width, height: this.canvas.height } });
        
        // ENHANCED: Always draw a visible character, even if sprite fails
        if (!sprite) {
            console.log('DEBUG: No sprite provided, drawing fallback rectangle');
            // Draw a fallback rectangle so we can see the pet is working
            this.ctx.fillStyle = 'red';
            this.ctx.fillRect(x, y, this.settings.petSize, this.settings.petSize);
            this.ctx.fillStyle = 'white';
            this.ctx.font = '20px Arial';
            this.ctx.fillText('PET', x + 10, y + 40);
            
            // Draw position indicator for fallback character
            this.drawPositionIndicator(x, y, this.settings.petSize, this.settings.petSize);
            return;
        }
        
        // DISABLED: Debug logging for clean console
        // console.log('DEBUG: Drawing character at', x, y, 'with sprite:', sprite);
        
        // REMOVED: Fallback rectangle that was causing visual confusion
        // The sprite is loading correctly, so we don't need this debug element
        
        // DISABLED: Debug logging for clean console
        // Position and sprite type logging removed
        
        // ENHANCED: Proper sprite flipping without position interference
        // Calculate proper scaling to maintain aspect ratio
        let drawWidth = this.settings.petSize;
        let drawHeight = this.settings.petSize;
        
        if (sprite instanceof HTMLImageElement) {
            // Calculate aspect ratio preserving scaling
            const aspectRatio = sprite.naturalWidth / sprite.naturalHeight;
            if (aspectRatio > 1) {
                // Image is wider than tall
                drawHeight = this.settings.petSize / aspectRatio;
            } else {
                // Image is taller than wide
                drawWidth = this.settings.petSize * aspectRatio;
            }
        }
        
        // ENHANCED: Safe sprite flipping using a different approach
        // Instead of transforming the canvas, we'll use a different drawing method
        if (sprite instanceof HTMLCanvasElement) {
            // Canvas sprite - use direct drawing
            // DISABLED: Debug logging for clean console
            this.ctx.drawImage(sprite, x, y, drawWidth, drawHeight);
        } else if (sprite instanceof HTMLImageElement) {
            // Image sprite with safe flipping
            // DISABLED: Debug logging for clean console
            
            if (!this.movementManager.facingRight) {
                // SAFE FLIPPING: Use a temporary canvas to flip the image
                // This avoids transforming the main canvas context
                const tempCanvas = document.createElement('canvas');
                const tempCtx = tempCanvas.getContext('2d');
                
                // Set temp canvas size
                tempCanvas.width = drawWidth;
                tempCanvas.height = drawHeight;
                
                // Flip the image on the temp canvas
                tempCtx.scale(-1, 1);
                tempCtx.drawImage(sprite, -drawWidth, 0, drawWidth, drawHeight);
                
                // Draw the flipped temp canvas to main canvas
                this.ctx.drawImage(tempCanvas, x, y);
            } else {
                // Draw normally when facing right
                this.ctx.drawImage(sprite, x, y, drawWidth, drawHeight);
            }
        } else {
            console.warn('Unknown sprite type:', typeof sprite);
            // Draw fallback rectangle
            this.ctx.fillStyle = 'blue';
            this.ctx.fillRect(x, y, this.settings.petSize, this.settings.petSize);
        }
        
        // DISABLED: Position indicator for clean display
        // this.drawPositionIndicator(x, y, drawWidth, drawHeight);
        
        // REMOVED: Debug crosshair that was causing visual confusion
    }
    
    /**
     * ENHANCED: Draw real-time position indicator above pet
     */
    drawPositionIndicator(x, y, petWidth, petHeight) {
        // Only draw if enabled
        if (!this.settings.showPositionIndicator) return;
        
        this.ctx.save();
        
        // Get current internal position
        const internalPos = this.movementManager.getPosition();
        
        // CRITICAL FIX: Use the SAME position for both character and indicator
        // This ensures they are always aligned
        const indicatorX = x + petWidth / 2;
        const indicatorY = y - 30; // 30px above pet
        
        // Draw background bubble
        this.ctx.fillStyle = 'rgba(0, 0, 0, 0.8)';
        this.ctx.strokeStyle = 'rgba(255, 255, 255, 0.9)';
        this.ctx.lineWidth = 2;
        
        // Create rounded rectangle background
        const text = `(${Math.round(x)}, ${Math.round(y)})`;
        const textMetrics = this.ctx.measureText(text);
        const padding = 8;
        const rectWidth = textMetrics.width + padding * 2;
        const rectHeight = 20;
        const radius = 10;
        
        // Draw rounded rectangle (with fallback for older browsers)
        this.ctx.beginPath();
        if (this.ctx.roundRect) {
            this.ctx.roundRect(indicatorX - rectWidth / 2, indicatorY - rectHeight / 2, rectWidth, rectHeight, radius);
        } else {
            // Fallback: draw regular rectangle
            this.ctx.rect(indicatorX - rectWidth / 2, indicatorY - rectHeight / 2, rectWidth, rectHeight);
        }
        this.ctx.fill();
        this.ctx.stroke();
        
        // Draw position text
        this.ctx.fillStyle = 'rgba(255, 255, 255, 1.0)';
        this.ctx.font = '12px monospace';
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'middle';
        this.ctx.fillText(text, indicatorX, indicatorY);
        
        // ENHANCED: Show internal position for debugging
        const positionMismatch = Math.abs(internalPos.x - x) > 5 || Math.abs(internalPos.y - y) > 5;
        
        // Draw internal position indicator (blue) to show where system thinks character should be
        this.ctx.fillStyle = 'rgba(0, 0, 255, 0.8)';
        this.ctx.beginPath();
        this.ctx.arc(internalPos.x + petWidth / 2, internalPos.y - 50, 6, 0, Math.PI * 2);
        this.ctx.fill();
        
        // Draw internal position text
        this.ctx.fillStyle = 'rgba(0, 0, 255, 1.0)';
        this.ctx.font = '10px monospace';
        this.ctx.fillText('I', internalPos.x + petWidth / 2, internalPos.y - 50);
        
        // ENHANCED: Show mismatch warning if positions don't match
        if (positionMismatch) {
            // Draw warning indicator on the black indicator
            this.ctx.fillStyle = 'rgba(255, 0, 0, 0.8)';
            this.ctx.beginPath();
            this.ctx.arc(indicatorX + rectWidth / 2 + 10, indicatorY, 6, 0, Math.PI * 2);
            this.ctx.fill();
            
            // Draw warning text
            this.ctx.fillStyle = 'rgba(255, 0, 0, 1.0)';
            this.ctx.font = '10px monospace';
            this.ctx.fillText('!', indicatorX + rectWidth / 2 + 10, indicatorY);
            
            // ENHANCED: Show the internal coordinates
            this.ctx.fillStyle = 'rgba(255, 255, 0, 1.0)';
            this.ctx.font = '10px monospace';
            this.ctx.fillText(`Internal: (${Math.round(internalPos.x)}, ${Math.round(internalPos.y)})`, internalPos.x + petWidth / 2, internalPos.y - 70);
        }
        
        this.ctx.restore();
    }
    
    /**
     * ENHANCED: Draw debug crosshair to show exact drawing position
     */
    drawDebugCrosshair(x, y) {
        this.ctx.save();
        
        // Draw a crosshair at the exact position where character should be drawn
        this.ctx.strokeStyle = 'rgba(255, 0, 255, 0.8)'; // Magenta
        this.ctx.lineWidth = 2;
        
        const size = 20;
        
        // Horizontal line
        this.ctx.beginPath();
        this.ctx.moveTo(x - size, y);
        this.ctx.lineTo(x + size, y);
        this.ctx.stroke();
        
        // Vertical line
        this.ctx.beginPath();
        this.ctx.moveTo(x, y - size);
        this.ctx.lineTo(x, y + size);
        this.ctx.stroke();
        
        // Center dot
        this.ctx.fillStyle = 'rgba(255, 0, 255, 0.8)';
        this.ctx.beginPath();
        this.ctx.arc(x, y, 3, 0, Math.PI * 2);
        this.ctx.fill();
        
        this.ctx.restore();
    }
    
    /**
     * Update pet size
     * 
     * ISSUE: Size changes may cause instant position adjustments
     * SOLUTION: Use smooth size transitions
     */
    updatePetSize(newSize) {
        this.settings.petSize = newSize;
        this.movementManager.updateBounds(newSize);
        
        // Update center position if pet is at center
        const currentPos = this.movementManager.getPosition();
        const center = this.movementManager.getCanvasCenter();
        
        if (Math.abs(currentPos.x - center.x) < 10 && Math.abs(currentPos.y - center.y) < 10) {
            this.movementManager.setPosition(center.x, center.y);
        }
    }
    
    /**
     * Update bounds when canvas is resized
     */
    updateBounds() {
        this.movementManager.updateBounds(this.settings.petSize);
    }
    
    /**
     * Update animation speed
     */
    updateAnimationSpeed(newSpeed) {
        this.settings.animationSpeed = newSpeed;
        this.animationManager.setSpeed(newSpeed);
    }
    
    /**
     * Update movement speed
     * 
     * ISSUE: Speed changes may cause jerky movement
     * SOLUTION: Use smooth speed transitions
     */
    updateMovementSpeed(newSpeed) {
        this.settings.movementSpeed = newSpeed;
        this.movementManager.setSpeed(newSpeed);
    }
    
    /**
     * Trigger special animation
     */
    triggerSpecialAnimation() {
        this.behaviorManager.playSpecialAnimation();
    }
    
    /**
     * Trigger pain animation
     */
    triggerPain() {
        this.behaviorManager.triggerPain();
    }
    
    /**
     * Force idle behavior
     */
    forceIdle() {
        this.behaviorManager.forceIdle();
    }
    
    /**
     * Force walking behavior
     */
    forceWalking() {
        this.behaviorManager.forceWalking();
    }
    
    /**
     * Get current behavior
     */
    getCurrentBehavior() {
        return this.behaviorManager.getCurrentBehavior();
    }
    
    /**
     * Get current position
     */
    getPosition() {
        return this.movementManager.getPosition();
    }
    
    /**
     * Set position
     * 
     * ISSUE: Direct position setting may cause teleporting
     * SOLUTION: Use smooth movement instead of direct positioning
     */
    setPosition(x, y) {
        this.movementManager.setPosition(x, y);
    }
    
    /**
     * Move to target
     * 
     * ISSUE: May cause instant movement
     * SOLUTION: Use smooth movement with proper speed
     */
    moveToTarget(x, y) {
        this.movementManager.setTarget(x, y);
    }
    
    /**
     * Start random movement
     * 
     * ISSUE: May cause teleporting if targets are too far
     * SOLUTION: Use constrained random movement
     */
    startRandomMovement() {
        this.movementManager.startRandomMovement();
    }
    
    /**
     * Stop movement
     */
    stopMovement() {
        this.movementManager.stopMovement();
    }
    
    /**
     * Get settings
     */
    getSettings() {
        return { ...this.settings };
    }
    
    /**
     * Update settings
     * 
     * ISSUE: Setting changes may cause instant behavior changes
     * SOLUTION: Use smooth transitions for setting changes
     */
    updateSettings(newSettings) {
        this.settings = { ...this.settings, ...newSettings };
        
        // Apply setting changes
        if (newSettings.petSize !== undefined) {
            this.updatePetSize(newSettings.petSize);
        }
        if (newSettings.animationSpeed !== undefined) {
            this.updateAnimationSpeed(newSettings.animationSpeed);
        }
        if (newSettings.movementSpeed !== undefined) {
            this.updateMovementSpeed(newSettings.movementSpeed);
        }
    }
    
    /**
     * ENHANCED: Toggle debug mode
     */
    toggleDebug() {
        this.movementManager.toggleDebug();
    }
    
    /**
     * ENHANCED: Force sync position to fix position tracking bug
     */
    forceSyncPosition() {
        // Get current visual position (where character appears to be)
        const currentPos = this.movementManager.getPosition();
        
        // If character appears to be at left edge but system thinks it's on right
        if (currentPos.x > 500 && this.movementManager.isAtScreenEdge()) {
            // Force sync to left side
            this.movementManager.forceSyncPosition(50, currentPos.y);
            console.log('Position synced to left side');
        } else {
            console.log('Position sync not needed or character not at edge');
        }
    }
    
    /**
     * ENHANCED: Toggle position indicator
     */
    togglePositionIndicator() {
        this.settings.showPositionIndicator = !this.settings.showPositionIndicator;
        console.log(`Position indicator: ${this.settings.showPositionIndicator ? 'ON' : 'OFF'}`);
    }
    
    /**
     * Cleanup resources
     */
    cleanup() {
        this.stop();
        this.behaviorManager.cleanup();
        this.interactionManager.cleanup();
    }
} 