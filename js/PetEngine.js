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
            isRunning: false
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
            
            // Update bounds based on pet size
            this.movementManager.updateBounds(this.settings.petSize);
            
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
        
        // Get current position
        const position = this.movementManager.getPosition();
        
        // Get current frame
        const sprite = this.animationManager.getCurrentFrame();
        
        // Draw pet
        this.drawPet(sprite, position.x, position.y);
        
        // Draw bubbles
        this.bubbleManager.draw();
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
        if (!sprite) {
            // Draw a fallback rectangle so we can see the pet is working
            this.ctx.fillStyle = 'red';
            this.ctx.fillRect(x, y, this.settings.petSize, this.settings.petSize);
            this.ctx.fillStyle = 'white';
            this.ctx.font = '20px Arial';
            this.ctx.fillText('PET', x + 10, y + 40);
            return;
        }
        
        // Debug: Log sprite type once
        if (!this._spriteTypeLogged) {
            console.log('DEBUG drawPet sprite:', {
                sprite: sprite,
                type: typeof sprite,
                constructor: sprite.constructor.name,
                isImage: sprite instanceof HTMLImageElement,
                isCanvas: sprite instanceof HTMLCanvasElement,
                complete: sprite.complete,
                naturalWidth: sprite.naturalWidth,
                naturalHeight: sprite.naturalHeight
            });
            this._spriteTypeLogged = true;
        }
        
        this.ctx.save();
        
        // Check if pet should be flipped
        if (!this.movementManager.facingRight) {
            this.ctx.scale(-1, 1);
            this.ctx.translate(-x - this.settings.petSize, 0);
        }
        
        // ISSUE: Direct scaling without aspect ratio preservation
        // SOLUTION: Calculate proper scaling to maintain aspect ratio
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
        
        // Draw the sprite with proper scaling
        if (sprite instanceof HTMLCanvasElement) {
            // Canvas sprite
            this.ctx.drawImage(sprite, x, y, drawWidth, drawHeight);
        } else if (sprite instanceof HTMLImageElement) {
            // Image sprite with aspect ratio preservation
            this.ctx.drawImage(sprite, x, y, drawWidth, drawHeight);
        } else {
            console.warn('Unknown sprite type:', typeof sprite);
            // Draw fallback rectangle
            this.ctx.fillStyle = 'blue';
            this.ctx.fillRect(x, y, this.settings.petSize, this.settings.petSize);
        }
        
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
     * Cleanup resources
     */
    cleanup() {
        this.stop();
        this.behaviorManager.cleanup();
        this.interactionManager.cleanup();
    }
} 