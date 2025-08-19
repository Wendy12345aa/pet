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
        this.ctx = canvas.getContext('2d');
        
        // Position and movement
        this.x = 0;
        this.y = 0;
        this.targetX = 0;
        this.targetY = 0;
        this.isMoving = false;
        this.facingRight = true;
        
        // Movement settings
        this.speed = 1.5; // ENHANCED: Reduced from 3 to 1.5 for smoother movement
        this.interpolationFactor = 0.08; // ENHANCED: Reduced from 0.1 to 0.08
        this.frameTime = 16.67; // 60 FPS reference
        
        // Bounds
        this.bounds = {
            minX: 0,
            minY: 0,
            maxX: canvas.width - 128, // Default pet size
            maxY: canvas.height - 128
        };
        
        // ENHANCED: Debug logging system
        this.debugLog = [];
        this.maxDebugEntries = 10;
        this.debugEnabled = true;
        
        // Reference to pet engine for bounds updates
        this.petEngine = null;
    }
    
    /**
     * Update movement bounds based on canvas size and pet size
     */
    updateBounds(petSize) {
        // ENHANCED: Better bounds calculation for wide screens
        const canvasWidth = this.canvas.width || window.innerWidth;
        const canvasHeight = this.canvas.height || window.innerHeight;
        
        this.bounds = {
            minX: 0,
            minY: 0,
            maxX: Math.max(0, canvasWidth - petSize),
            maxY: Math.max(0, canvasHeight - petSize)
        };
        
        // ENHANCED: Debug logging with more details
        console.log('Bounds updated:', {
            canvasWidth: canvasWidth,
            canvasHeight: canvasHeight,
            petSize: petSize,
            bounds: this.bounds,
            availableWidth: this.bounds.maxX - this.bounds.minX,
            availableHeight: this.bounds.maxY - this.bounds.minY
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
            this.addDebugLog('LARGE POSITION CHANGE', { distance, oldPos: { x: oldX, y: oldY }, newPos: { x, y } });
        }
        
        // ENHANCED: Better bounds checking with debugging
        const clampedX = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, x));
        const clampedY = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, y));
        
        // ENHANCED: Debug bounds clamping
        if (x !== clampedX || y !== clampedY) {
            console.log('Position clamped:', {
                requested: { x, y },
                clamped: { x: clampedX, y: clampedY },
                bounds: this.bounds
            });
            this.addDebugLog('POSITION CLAMPED', { requested: { x, y }, clamped: { x: clampedX, y: clampedY } });
        }
        
        // ENHANCED: Track interpolation changes
        const beforeInterpolationX = this.x;
        const beforeInterpolationY = this.y;
        
        // Apply smooth interpolation
        this.x += (clampedX - this.x) * this.interpolationFactor;
        this.y += (clampedY - this.y) * this.interpolationFactor;
        
        // ENHANCED: Detect large interpolation changes
        const interpolationChange = Math.sqrt((this.x - beforeInterpolationX) ** 2 + (this.y - beforeInterpolationY) ** 2);
        if (interpolationChange > 15) {
            this.addDebugLog('LARGE INTERPOLATION CHANGE', {
                beforeInterpolation: { x: beforeInterpolationX, y: beforeInterpolationY },
                afterInterpolation: { x: this.x, y: this.y },
                change: interpolationChange,
                clampedTarget: { x: clampedX, y: clampedY }
            });
        }
        
        // Update facing direction based on movement
        if (clampedX > oldX) {
            this.facingRight = true;
        } else if (clampedX < oldX) {
            this.facingRight = false;
        }
        
        this.addDebugLog('POSITION SET', { oldPos: { x: oldX, y: oldY }, newPos: { x: this.x, y: this.y } });
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
        const oldTargetX = this.targetX;
        const oldTargetY = this.targetY;
        
        const clampedX = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, x));
        const clampedY = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, y));
        
        // ENHANCED: More lenient distance validation
        const distance = Math.sqrt((clampedX - this.x) ** 2 + (clampedY - this.y) ** 2);
        
        // ENHANCED: Only reject targets that are extremely far (increased threshold)
        if (distance > 300) { // Increased from 150 to 300
            console.warn('Target extremely far from current position:', distance);
            this.addDebugLog('TARGET EXTREMELY FAR', { distance, requested: { x, y }, clamped: { x: clampedX, y: clampedY } });
            
            // ENHANCED: Use a closer target if extremely far
            const maxDistance = 200; // Increased from 150 to 200
            const angle = Math.atan2(clampedY - this.y, clampedX - this.x);
            const newTargetX = this.x + Math.cos(angle) * maxDistance;
            const newTargetY = this.y + Math.sin(angle) * maxDistance;
            
            this.targetX = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, newTargetX));
            this.targetY = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, newTargetY));
        } else {
            // ENHANCED: Accept the target even if it's moderately far
            this.targetX = clampedX;
            this.targetY = clampedY;
        }
        
        this.isMoving = true;
        
        // Update facing direction
        this.facingRight = this.targetX > this.x;
        
        this.addDebugLog('TARGET SET', {
            oldTarget: { x: Math.round(oldTargetX), y: Math.round(oldTargetY) },
            newTarget: { x: Math.round(this.targetX), y: Math.round(this.targetY) },
            distance: Math.round(distance),
            facingRight: this.facingRight,
            location: `From (${Math.round(oldTargetX)}, ${Math.round(oldTargetY)}) to (${Math.round(this.targetX)}, ${Math.round(this.targetY)})`
        });
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
            const oldX = this.x;
            const oldY = this.y;
            
            this.x = this.targetX;
            this.y = this.targetY;
            
            // ENHANCED: Log target reaching
            const targetReachDistance = Math.sqrt((this.x - oldX) ** 2 + (this.y - oldY) ** 2);
            if (targetReachDistance > 10) {
                this.addDebugLog('TARGET REACHED', {
                    oldPos: { x: oldX, y: oldY },
                    newPos: { x: this.x, y: this.y },
                    distance: targetReachDistance,
                    target: { x: this.targetX, y: this.targetY }
                });
            }
            
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
        
        // ENHANCED: Boundary bouncing instead of stopping
        // Check if new position would be outside bounds
        let hitHorizontal = false;
        let hitVertical = false;
        
        if (newX < this.bounds.minX || newX > this.bounds.maxX) {
            hitHorizontal = true;
            this.addDebugLog('HORIZONTAL BOUNDARY HIT', {
                newX: Math.round(newX),
                bounds: { minX: this.bounds.minX, maxX: this.bounds.maxX },
                currentX: Math.round(this.x),
                location: `At (${Math.round(this.x)}, ${Math.round(this.y)}) trying to move to X=${Math.round(newX)}`
            });
        }
        
        if (newY < this.bounds.minY || newY > this.bounds.maxY) {
            hitVertical = true;
            this.addDebugLog('VERTICAL BOUNDARY HIT', {
                newY: Math.round(newY),
                bounds: { minY: this.bounds.minY, maxY: this.bounds.maxY },
                currentY: Math.round(this.y),
                location: `At (${Math.round(this.x)}, ${Math.round(this.y)}) trying to move to Y=${Math.round(newY)}`
            });
        }
        
        if (hitHorizontal || hitVertical) {
            // ENHANCED: Simple direction reversal instead of teleporting
            // Just reverse the current movement direction
            const oldTargetX = this.targetX;
            const oldTargetY = this.targetY;
            
            if (hitHorizontal) {
                // Reverse horizontal direction
                const currentDx = this.targetX - this.x;
                this.targetX = this.x - currentDx;
            }
            
            if (hitVertical) {
                // Reverse vertical direction
                const currentDy = this.targetY - this.y;
                this.targetY = this.y - currentDy;
            }
            
            // Clamp new target to bounds
            this.targetX = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, this.targetX));
            this.targetY = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, this.targetY));
            
            // Update facing direction based on new target
            this.facingRight = this.targetX > this.x;
            
            console.log('Boundary bounce - direction reversed:', {
                hitHorizontal,
                hitVertical,
                newTarget: { x: this.targetX, y: this.targetY },
                facingRight: this.facingRight
            });
            
            this.addDebugLog('BOUNDARY BOUNCE', {
                hitHorizontal,
                hitVertical,
                oldTarget: { x: oldTargetX, y: oldTargetY },
                newTarget: { x: this.targetX, y: this.targetY },
                facingRight: this.facingRight
            });
        }
        
        // ENHANCED: Track position changes for teleporting detection
        const oldX = this.x;
        const oldY = this.y;
        
        // Clamp position to bounds (safety check)
        this.x = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, newX));
        this.y = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, newY));
        
        // ENHANCED: Detect sudden position changes (teleporting)
        const positionChange = Math.sqrt((this.x - oldX) ** 2 + (this.y - oldY) ** 2);
        if (positionChange > 20) { // Detect changes larger than 20px
            this.addDebugLog('SUDDEN POSITION CHANGE', {
                oldPos: { x: oldX, y: oldY },
                newPos: { x: this.x, y: this.y },
                change: positionChange,
                wasClamped: (this.x !== newX || this.y !== newY)
            });
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
        const minDistance = 50; // ENHANCED: Increased minimum distance for more meaningful movement
        const maxDistance = 200; // ENHANCED: Increased maximum distance for better exploration
        let attempts = 0;
        const maxAttempts = 20; // ENHANCED: Increased attempts for better positioning
        
        do {
            // ENHANCED: Generate targets in a more controlled way
            const angle = Math.random() * Math.PI * 2;
            const targetDistance = minDistance + Math.random() * (maxDistance - minDistance);
            
            randomX = this.x + Math.cos(angle) * targetDistance;
            randomY = this.y + Math.sin(angle) * targetDistance;
            
            // Clamp to bounds
            randomX = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, randomX));
            randomY = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, randomY));
            
            distance = Math.sqrt((randomX - this.x) ** 2 + (randomY - this.y) ** 2);
            attempts++;
            
            // ENHANCED: Accept if distance is reasonable
            if (distance >= minDistance && distance <= maxDistance) {
                break;
            }
        } while (attempts < maxAttempts);
        
        // ENHANCED: Better fallback positioning
        if (attempts >= maxAttempts) {
            console.log('Using fallback random position');
            // Generate a position within a smaller radius
            const fallbackRadius = 100;
            const angle = Math.random() * Math.PI * 2;
            const fallbackDistance = minDistance + Math.random() * (fallbackRadius - minDistance);
            
            randomX = this.x + Math.cos(angle) * fallbackDistance;
            randomY = this.y + Math.sin(angle) * fallbackDistance;
            
            // Clamp to bounds
            randomX = Math.max(this.bounds.minX, Math.min(this.bounds.maxX, randomX));
            randomY = Math.max(this.bounds.minY, Math.min(this.bounds.maxY, randomY));
        }
        
        this.addDebugLog('RANDOM MOVEMENT STARTED', {
            currentPos: { x: Math.round(this.x), y: Math.round(this.y) },
            targetPos: { x: Math.round(randomX), y: Math.round(randomY) },
            distance: Math.round(Math.sqrt((randomX - this.x) ** 2 + (randomY - this.y) ** 2)),
            attempts,
            location: `From (${Math.round(this.x)}, ${Math.round(this.y)}) to (${Math.round(randomX)}, ${Math.round(randomY)})`
        });
        
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
        // ENHANCED: Use actual canvas dimensions for center calculation
        const canvasWidth = this.canvas.width || window.innerWidth;
        const canvasHeight = this.canvas.height || window.innerHeight;
        const petSize = this.petEngine ? this.petEngine.settings.petSize : 128;
        
        return {
            x: canvasWidth / 2 - petSize / 2,
            y: canvasHeight / 2 - petSize / 2
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
    
    /**
     * ENHANCED: Force refresh bounds for wide screens
     * This ensures bounds are properly calculated for the current screen size
     */
    refreshBounds() {
        const currentPetSize = this.petEngine ? this.petEngine.settings.petSize : 128;
        this.updateBounds(currentPetSize);
        
        console.log('Bounds refreshed for wide screen:', {
            canvasWidth: this.canvas.width,
            canvasHeight: this.canvas.height,
            windowWidth: window.innerWidth,
            windowHeight: window.innerHeight,
            bounds: this.bounds
        });
    }
    
    /**
     * ENHANCED: Add debug log entry
     */
    addDebugLog(message, data = {}) {
        if (!this.debugEnabled) return;
        
        const timestamp = Date.now();
        const entry = {
            timestamp,
            message,
            data: {
                currentPosition: { x: Math.round(this.x), y: Math.round(this.y) },
                currentTarget: { x: Math.round(this.targetX), y: Math.round(this.targetY) },
                isMoving: this.isMoving,
                facingRight: this.facingRight,
                bounds: this.bounds,
                ...data
            }
        };
        
        this.debugLog.push(entry);
        
        // Keep only the latest entries
        if (this.debugLog.length > this.maxDebugEntries) {
            this.debugLog.shift();
        }
        
        console.log(`[DEBUG] ${message}`, entry.data);
    }
    
    /**
     * ENHANCED: Draw debug information on screen
     */
    drawDebugInfo() {
        if (!this.debugEnabled) return;
        
        const ctx = this.ctx;
        const fontSize = 12;
        const lineHeight = fontSize + 2;
        let yOffset = 10;
        
        // Set text style
        ctx.fillStyle = 'rgba(255, 255, 255, 0.9)';
        ctx.strokeStyle = 'rgba(0, 0, 0, 0.8)';
        ctx.lineWidth = 2;
        ctx.font = `${fontSize}px monospace`;
        ctx.textAlign = 'left';
        
        // Draw background
        const debugWidth = 300;
        const debugHeight = (this.debugLog.length + 3) * lineHeight + 20;
        ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
        ctx.fillRect(10, 10, debugWidth, debugHeight);
        
        // Draw border
        ctx.strokeStyle = 'rgba(255, 255, 255, 0.5)';
        ctx.strokeRect(10, 10, debugWidth, debugHeight);
        
        // Reset text style
        ctx.fillStyle = 'rgba(255, 255, 255, 0.9)';
        ctx.strokeStyle = 'rgba(0, 0, 0, 0.8)';
        
        // Draw current status with enhanced location info
        ctx.fillStyle = 'rgba(255, 255, 255, 1.0)';
        ctx.fillText(`📍 Current: (${Math.round(this.x)}, ${Math.round(this.y)})`, 15, yOffset += lineHeight);
        ctx.fillText(`🎯 Target: (${Math.round(this.targetX)}, ${Math.round(this.targetY)})`, 15, yOffset += lineHeight);
        ctx.fillText(`🚶 Moving: ${this.isMoving} | 👀 Facing: ${this.facingRight ? 'Right' : 'Left'}`, 15, yOffset += lineHeight);
        
        // Draw recent debug log
        yOffset += 5;
        ctx.fillText('Recent Events:', 15, yOffset += lineHeight);
        
        for (let i = this.debugLog.length - 1; i >= 0 && i >= this.debugLog.length - 5; i--) {
            const entry = this.debugLog[i];
            const timeAgo = Date.now() - entry.timestamp;
            const timeStr = timeAgo < 1000 ? `${timeAgo}ms ago` : `${Math.round(timeAgo/1000)}s ago`;
            
            ctx.fillStyle = 'rgba(255, 255, 0, 0.9)';
            ctx.fillText(`${timeStr}: ${entry.message}`, 15, yOffset += lineHeight);
            
            // Show location context for each event
            if (entry.data.currentPosition) {
                ctx.fillStyle = 'rgba(200, 255, 200, 0.8)';
                ctx.fillText(`   📍 At: (${entry.data.currentPosition.x}, ${entry.data.currentPosition.y})`, 15, yOffset += lineHeight);
            }
            
            // Show additional location info for specific events
            if (entry.data.oldPos && entry.data.newPos) {
                ctx.fillStyle = 'rgba(255, 200, 200, 0.8)';
                ctx.fillText(`   🔄 From: (${entry.data.oldPos.x}, ${entry.data.oldPos.y}) → To: (${entry.data.newPos.x}, ${entry.data.newPos.y})`, 15, yOffset += lineHeight);
            }
            
            ctx.fillStyle = 'rgba(255, 255, 255, 0.9)';
        }
        
        // Draw bounds info
        yOffset += 5;
        ctx.fillStyle = 'rgba(0, 255, 255, 0.9)';
        ctx.fillText(`Bounds: X(${this.bounds.minX}-${this.bounds.maxX}) Y(${this.bounds.minY}-${this.bounds.maxY})`, 15, yOffset += lineHeight);
    }
    
    /**
     * ENHANCED: Toggle debug mode
     */
    toggleDebug() {
        this.debugEnabled = !this.debugEnabled;
        console.log(`Debug mode: ${this.debugEnabled ? 'ON' : 'OFF'}`);
    }
    
    /**
     * ENHANCED: Track all position changes for debugging
     */
    _trackPositionChange(oldX, oldY, newX, newY, reason) {
        if (!this.debugEnabled) return;
        
        const distance = Math.sqrt((newX - oldX) ** 2 + (newY - oldY) ** 2);
        if (distance > 5) { // Only log significant changes
            this.addDebugLog(`POSITION CHANGE: ${reason}`, {
                oldPos: { x: oldX, y: oldY },
                newPos: { x: newX, y: newY },
                distance: distance
            });
        }
    }
    
    /**
     * ENHANCED: Force sync position to match visual position
     * This fixes the position tracking bug where visual and internal positions are out of sync
     */
    forceSyncPosition(visualX, visualY) {
        const oldX = this.x;
        const oldY = this.y;
        
        // Force set the position to match visual position
        this.x = visualX;
        this.y = visualY;
        
        // Also update target to prevent immediate movement
        this.targetX = visualX;
        this.targetY = visualY;
        
        this.addDebugLog('FORCE SYNC POSITION', {
            oldPos: { x: oldX, y: oldY },
            newPos: { x: this.x, y: this.y },
            reason: 'Visual and internal position mismatch detected'
        });
        
        console.log('Position synced:', {
            old: { x: oldX, y: oldY },
            new: { x: this.x, y: this.y },
            visual: { x: visualX, y: visualY }
        });
    }
    
    /**
     * ENHANCED: Get visual position (what user actually sees)
     */
    getVisualPosition() {
        return { x: this.x, y: this.y };
    }
    
    /**
     * ENHANCED: Check if position is at screen edge
     */
    isAtScreenEdge() {
        const margin = 50; // Consider "at edge" if within 50px of boundary
        return (
            this.x <= this.bounds.minX + margin ||
            this.x >= this.bounds.maxX - margin ||
            this.y <= this.bounds.minY + margin ||
            this.y >= this.bounds.maxY - margin
        );
    }
    

} 