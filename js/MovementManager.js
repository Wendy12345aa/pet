/**
 * MovementManager - Handles pet positioning, movement, and screen boundaries
 * 
 * ISSUE #2: TELEPORTING MOVEMENT
 * 
 * The main causes of teleporting movement are:
 * 1. Sudden position changes without smooth interpolation
 * 2. Movement speed too high causing large position jumps
 * 3. Missing movement interpolation between frames
 * 4. Random movement generating positions too far apart
 * 5. Click interactions causing instant position changes
 */
class MovementManager {
    constructor(canvas) {
        this.canvas = canvas;
        this.x = 100;
        this.y = 100;
        this.targetX = 100;
        this.targetY = 100;
        this.speed = 3; // ISSUE: This speed may be too high for smooth movement
        this.isMoving = false;
        this.facingRight = true;
        
        // Movement bounds
        this.bounds = {
            minX: 0,
            minY: 0,
            maxX: canvas.width - 128, // Default pet size
            maxY: canvas.height - 128
        };
        
        // Movement timer
        this.movementTimer = null;
        this.lastMovementTime = 0;
        
        // ISSUE: Missing movement interpolation variables
        // SOLUTION: Add interpolation for smoother movement
        this.interpolationFactor = 0.1; // For smooth movement interpolation
        this.lastUpdateTime = 0;
    }
    
    /**
     * Update movement bounds based on canvas size and pet size
     */
    updateBounds(petSize) {
        this.bounds = {
            minX: 0,
            minY: 0,
            maxX: Math.max(0, this.canvas.width - petSize),
            maxY: Math.max(0, this.canvas.height - petSize)
        };
        
        // Debug logging
        console.log('Bounds updated:', {
            canvasWidth: this.canvas.width,
            canvasHeight: this.canvas.height,
            petSize: petSize,
            bounds: this.bounds
        });
    }
    
    /**
     * Set pet position
     * 
     * ISSUE: Direct position setting can cause teleporting
     * SOLUTION: Use smooth interpolation or gradual movement
     */
    setPosition(x, y) {
        // ISSUE: Instant position change causes teleporting
        // SOLUTION: Use interpolation or gradual movement
        this.x = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, x));
        this.y = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, y));
        
        // ISSUE: No validation of position change magnitude
        // SOLUTION: Add distance checking to prevent large jumps
        const distance = Math.sqrt((x - this.x) ** 2 + (y - this.y) ** 2);
        if (distance > 100) { // Arbitrary threshold
            console.warn('Large position change detected:', distance);
        }
    }
    
    /**
     * Get current position
     */
    getPosition() {
        return { x: this.x, y: this.y };
    }
    
    /**
     * Set target position
     * 
     * ISSUE: Target setting may cause immediate large movements
     * SOLUTION: Add distance validation and gradual target setting
     */
    setTarget(x, y) {
        this.targetX = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, x));
        this.targetY = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, y));
        this.isMoving = true;
        
        // Update facing direction
        this.facingRight = this.targetX > this.x;
        
        // ISSUE: No validation of target distance
        // SOLUTION: Add maximum movement distance validation
        const distance = Math.sqrt((this.targetX - this.x) ** 2 + (this.targetY - this.y) ** 2);
        if (distance > 200) { // Arbitrary maximum distance
            console.warn('Target too far from current position:', distance);
        }
    }
    
    /**
     * Update movement
     * 
     * ISSUE: Movement may be too fast or jerky
     * SOLUTION: Implement smooth interpolation and frame-rate independent movement
     */
    update() {
        // Prevent movement if being dragged
        if (this.petEngine && this.petEngine.interactionManager && this.petEngine.interactionManager.isDraggingPet()) {
            this.isMoving = false;
            return;
        }
        if (!this.isMoving) return;
        
        const dx = this.targetX - this.x;
        const dy = this.targetY - this.y;
        const distance = Math.sqrt(dx * dx + dy * dy);
        
        // ISSUE: Movement speed may be too high causing teleporting
        // SOLUTION: Use frame-rate independent movement and lower speed
        if (distance < this.speed) {
            // Reached target
            this.x = this.targetX;
            this.y = this.targetY;
            this.isMoving = false;
            return;
        }
        
        // Move towards target
        const angle = Math.atan2(dy, dx);
        const newX = this.x + Math.cos(angle) * this.speed;
        const newY = this.y + Math.sin(angle) * this.speed;
        
        // Update facing direction based on movement direction
        if (newX > this.x) {
            this.facingRight = true;
        } else if (newX < this.x) {
            this.facingRight = false;
        }
        
        // Check if new position would be outside bounds and clamp it
        const clampedX = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, newX));
        const clampedY = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, newY));
        
        // Update position with clamped values
        this.x = clampedX;
        this.y = clampedY;
        
        // If we hit a boundary, stop movement
        if (clampedX !== newX || clampedY !== newY) {
            this.isMoving = false;
        }
    }
    
    /**
     * Start random movement
     * 
     * ISSUE: Random movement can generate positions too far apart
     * SOLUTION: Add distance constraints and smooth pathfinding
     */
    startRandomMovement() {
        if (this.isMoving) return;
        let randomX, randomY, distance;
        const minDistance = 50; // Minimum distance to avoid teleporting
        const maxDistance = 150; // ISSUE: No maximum distance constraint
        let attempts = 0;
        const maxAttempts = 10; // Prevent infinite loops
        
        do {
            randomX = this.bounds.minX + Math.random() * (this.bounds.maxX - this.bounds.minX);
            randomY = this.bounds.minY + Math.random() * (this.bounds.maxY - this.bounds.minY);
            distance = Math.sqrt((randomX - this.x) ** 2 + (randomY - this.y) ** 2);
            attempts++;
            
            // ISSUE: No maximum distance check
            // SOLUTION: Add maximum distance constraint
            if (distance > maxDistance) {
                console.warn('Random target too far, regenerating...');
                continue;
            }
        } while (distance < minDistance && attempts < maxAttempts);
        
        // ISSUE: May still generate far targets if max attempts reached
        // SOLUTION: Use closest valid position if max attempts reached
        if (attempts >= maxAttempts) {
            console.warn('Could not find suitable random position, using closest valid');
            // Find closest position within bounds
            randomX = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, this.x + (Math.random() - 0.5) * 100));
            randomY = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, this.y + (Math.random() - 0.5) * 100));
        }
        
        this.setTarget(randomX, randomY);
    }
    
    /**
     * Stop movement
     */
    stopMovement() {
        this.isMoving = false;
        this.targetX = this.x;
        this.targetY = this.y;
    }
    
    /**
     * Check if pet is moving
     */
    isPetMoving() {
        return this.isMoving;
    }
    
    /**
     * Get facing direction
     */
    getFacingDirection() {
        return this.facingRight ? 'right' : 'left';
    }
    
    /**
     * Set movement speed
     * 
     * ISSUE: Speed may be too high causing teleporting
     * SOLUTION: Add speed validation and limits
     */
    setSpeed(speed) {
        // ISSUE: No speed validation
        // SOLUTION: Add minimum and maximum speed limits
        const minSpeed = 0.5;
        const maxSpeed = 5.0;
        this.speed = Math.max(minSpeed, Math.min(maxSpeed, speed));
        
        console.log(`Movement speed set to: ${this.speed}`);
    }
    
    /**
     * Get movement speed
     */
    getSpeed() {
        return this.speed;
    }
    
    /**
     * Check if position is within bounds
     */
    isPositionValid(x, y) {
        return x >= this.bounds.minX && x <= this.bounds.maxX &&
               y >= this.bounds.minY && y <= this.bounds.maxY;
    }
    
    /**
     * Get distance to target
     */
    getDistanceToTarget() {
        const dx = this.targetX - this.x;
        const dy = this.targetY - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Get center position of canvas
     */
    getCanvasCenter() {
        return {
            x: this.canvas.width / 2 - 64, // Half pet size
            y: this.canvas.height / 2 - 64
        };
    }
    
    /**
     * Move pet to center of canvas
     * 
     * ISSUE: May cause teleporting if pet is far from center
     * SOLUTION: Use smooth movement instead of direct positioning
     */
    moveToCenter() {
        const center = this.getCanvasCenter();
        this.setTarget(center.x, center.y);
    }
    
    /**
     * Get bounds information
     */
    getBounds() {
        return { ...this.bounds };
    }
} 