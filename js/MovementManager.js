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
        this.speed = 1.5; // ENHANCED: Reduced from 3 to 1.5 for smoother movement
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
        
        // ENHANCED: Improved movement interpolation variables
        this.interpolationFactor = 0.08; // Reduced for smoother movement
        this.lastUpdateTime = 0;
        this.frameTime = 16.67; // 60 FPS baseline for frame-rate independent movement
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
     * ENHANCED: Smooth position setting to prevent teleporting
     * SOLUTION: Use interpolation or gradual movement
     */
    setPosition(x, y) {
        // ENHANCED: Validate position change magnitude
        const oldX = this.x;
        const oldY = this.y;
        const distance = Math.sqrt((x - oldX) ** 2 + (y - oldY) ** 2);
        
        if (distance > 50) { // Reduced threshold for better detection
            console.warn('Large position change detected:', distance);
        }
        
        // ENHANCED: Use smooth interpolation instead of direct setting
        const clampedX = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, x));
        const clampedY = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, y));
        
        // Apply smooth interpolation
        this.x += (clampedX - this.x) * this.interpolationFactor;
        this.y += (clampedY - this.y) * this.interpolationFactor;
        
        // Update facing direction based on movement
        if (clampedX > oldX) {
            this.facingRight = true;
        } else if (clampedX < oldX) {
            this.facingRight = false;
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
     * ENHANCED: Better target validation to prevent teleporting
     * SOLUTION: Add distance validation and gradual target setting
     */
    setTarget(x, y) {
        const clampedX = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, x));
        const clampedY = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, y));
        
        // ENHANCED: Validate target distance before setting
        const distance = Math.sqrt((clampedX - this.x) ** 2 + (clampedY - this.y) ** 2);
        
        if (distance > 150) { // Reduced maximum distance
            console.warn('Target too far from current position:', distance);
            // ENHANCED: Use a closer target if too far
            const maxDistance = 150;
            const angle = Math.atan2(clampedY - this.y, clampedX - this.x);
            const newTargetX = this.x + Math.cos(angle) * maxDistance;
            const newTargetY = this.y + Math.sin(angle) * maxDistance;
            
            this.targetX = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, newTargetX));
            this.targetY = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, newTargetY));
        } else {
            this.targetX = clampedX;
            this.targetY = clampedY;
        }
        
        this.isMoving = true;
        
        // Update facing direction
        this.facingRight = this.targetX > this.x;
    }
    
    /**
     * Update movement
     * 
     * ENHANCED: Frame-rate independent movement with smooth interpolation
     * SOLUTION: Implement smooth interpolation and frame-rate independent movement
     */
    update(deltaTime = this.frameTime) {
        // Prevent movement if being dragged
        if (this.petEngine && this.petEngine.interactionManager && this.petEngine.interactionManager.isDraggingPet()) {
            this.isMoving = false;
            return;
        }
        if (!this.isMoving) return;
        
        // ENHANCED: Frame-rate independent movement
        const frameTime = deltaTime / this.frameTime; // Normalize to 60 FPS
        const adjustedSpeed = this.speed * frameTime;
        
        const dx = this.targetX - this.x;
        const dy = this.targetY - this.y;
        const distance = Math.sqrt(dx * dx + dy * dy);
        
        // ENHANCED: Use adjusted speed for smoother movement
        if (distance < adjustedSpeed) {
            // Reached target
            this.x = this.targetX;
            this.y = this.targetY;
            this.isMoving = false;
            return;
        }
        
        // ENHANCED: Smooth interpolation movement
        const angle = Math.atan2(dy, dx);
        const newX = this.x + Math.cos(angle) * adjustedSpeed;
        const newY = this.y + Math.sin(angle) * adjustedSpeed;
        
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
     * ENHANCED: Better distance constraints to prevent teleporting
     * SOLUTION: Add distance constraints and smooth pathfinding
     */
    startRandomMovement() {
        if (this.isMoving) return;
        let randomX, randomY, distance;
        const minDistance = 30; // ENHANCED: Reduced minimum distance
        const maxDistance = 120; // ENHANCED: Reduced maximum distance for smoother movement
        let attempts = 0;
        const maxAttempts = 15; // ENHANCED: Increased attempts for better positioning
        
        do {
            randomX = this.bounds.minX + Math.random() * (this.bounds.maxX - this.bounds.minX);
            randomY = this.bounds.minY + Math.random() * (this.bounds.maxY - this.bounds.minY);
            distance = Math.sqrt((randomX - this.x) ** 2 + (randomY - this.y) ** 2);
            attempts++;
            
            // ENHANCED: Better distance validation
            if (distance > maxDistance) {
                continue;
            }
        } while (distance < minDistance && attempts < maxAttempts);
        
        // ENHANCED: Better fallback positioning
        if (attempts >= maxAttempts) {
            console.log('Using fallback random position');
            // Generate a position within a smaller radius
            const fallbackRadius = 80;
            const angle = Math.random() * Math.PI * 2;
            const fallbackDistance = minDistance + Math.random() * (fallbackRadius - minDistance);
            
            randomX = this.x + Math.cos(angle) * fallbackDistance;
            randomY = this.y + Math.sin(angle) * fallbackDistance;
            
            // Clamp to bounds
            randomX = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, randomX));
            randomY = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, randomY));
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
     * ENHANCED: Better speed validation to prevent teleporting
     * SOLUTION: Add speed validation and limits
     */
    setSpeed(speed) {
        // ENHANCED: More conservative speed limits
        const minSpeed = 0.3;
        const maxSpeed = 3.0;
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