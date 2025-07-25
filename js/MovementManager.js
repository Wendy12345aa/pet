/**
 * MovementManager - Handles pet positioning, movement, and screen boundaries
 */
class MovementManager {
    constructor(canvas) {
        this.canvas = canvas;
        this.x = 100;
        this.y = 100;
        this.targetX = 100;
        this.targetY = 100;
        this.speed = 3;
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
     */
    setPosition(x, y) {
        this.x = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, x));
        this.y = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, y));
    }
    
    /**
     * Get current position
     */
    getPosition() {
        return { x: this.x, y: this.y };
    }
    
    /**
     * Set target position
     */
    setTarget(x, y) {
        this.targetX = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, x));
        this.targetY = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, y));
        this.isMoving = true;
        
        // Update facing direction
        this.facingRight = this.targetX > this.x;
    }
    
    /**
     * Update movement
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
     */
    startRandomMovement() {
        if (this.isMoving) return;
        let randomX, randomY, distance;
        const minDistance = 50; // Minimum distance to avoid teleporting
        do {
            randomX = this.bounds.minX + Math.random() * (this.bounds.maxX - this.bounds.minX);
            randomY = this.bounds.minY + Math.random() * (this.bounds.maxY - this.bounds.minY);
            distance = Math.sqrt((randomX - this.x) ** 2 + (randomY - this.y) ** 2);
        } while (distance < minDistance);
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
     */
    setSpeed(speed) {
        this.speed = speed;
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