/**
 * BehaviorManager - Handles different pet behaviors and state transitions
 */
class BehaviorManager {
    constructor(animationManager, movementManager) {
        this.animationManager = animationManager;
        this.movementManager = movementManager;
        
        // Behavior states
        this.behaviors = {
            IDLE: 'idle',
            WALKING: 'walking',
            PAIN: 'pain'
        };
        
        this.currentBehavior = this.behaviors.IDLE;
        this.previousBehavior = this.behaviors.IDLE;
        
        // Behavior timers
        this.idleTimer = null;
        this.behaviorChangeInterval = 3000; // 3 seconds instead of 1 second
        
        // Initialize behavior system
        this.initializeBehaviors();
    }
    
    /**
     * Initialize behavior system
     */
    initializeBehaviors() {
        // Start idle behavior timer
        this.startIdleTimer();
    }
    
    /**
     * Update behavior based on current state
     */
    update() {
        // Check if pet is moving and update behavior accordingly
        if (this.movementManager.isPetMoving()) {
            if (this.currentBehavior !== this.behaviors.WALKING) {
                this.setBehavior(this.behaviors.WALKING);
            }
        } else {
            if (this.currentBehavior === this.behaviors.WALKING) {
                // Pet finished walking to target, wait longer before starting new movement
                setTimeout(() => {
                    if (!this.movementManager.isPetMoving() && !this.isBeingDragged()) {
                        this.movementManager.startRandomMovement();
                    }
                }, 2000); // 2 second delay instead of 0.5
            } else if (this.currentBehavior === this.behaviors.IDLE) {
                // Pet is idle, much lower chance to start walking
                if (Math.random() < 0.3 && !this.isBeingDragged()) { // 30% chance instead of 80%
                    this.movementManager.startRandomMovement();
                }
            }
        }
    }
    
    /**
     * Set current behavior
     */
    setBehavior(behavior) {
        if (this.behaviors[behavior] && this.currentBehavior !== behavior) {
            this.previousBehavior = this.currentBehavior;
            this.currentBehavior = behavior;
            
            // Update animation
            this.animationManager.setAnimation(behavior);
            
            console.log(`Behavior changed: ${this.previousBehavior} -> ${this.currentBehavior}`);
        }
    }
    
    /**
     * Get current behavior
     */
    getCurrentBehavior() {
        return this.currentBehavior;
    }
    
    /**
     * Start idle behavior timer
     */
    startIdleTimer() {
        this.idleTimer = setInterval(() => {
            if (this.currentBehavior === this.behaviors.IDLE && 
                !this.movementManager.isPetMoving() && 
                !this.isBeingDragged()) {
                // Much lower chance to start walking
                if (Math.random() < 0.3) { // 30% chance instead of 80%
                    this.movementManager.startRandomMovement();
                }
            }
        }, this.behaviorChangeInterval);
    }
    
    /**
     * Trigger pain behavior
     */
    triggerPain() {
        if (this.currentBehavior === this.behaviors.PAIN) return;
        
        this.setBehavior(this.behaviors.PAIN);
        
        // Stop movement during pain
        this.movementManager.stopMovement();
        
        // Return to idle after pain animation
        setTimeout(() => {
            if (this.currentBehavior === this.behaviors.PAIN) {
                this.setBehavior(this.behaviors.IDLE);
            }
        }, 1500); // 1.5 seconds of pain
    }
    
    /**
     * Force pet to stay idle
     */
    forceIdle() {
        this.setBehavior(this.behaviors.IDLE);
        // Clear any pending movement timers
        if (this.idleTimer) {
            clearInterval(this.idleTimer);
            this.startIdleTimer();
        }
    }
    
    /**
     * Resume normal behavior after being forced idle
     */
    resumeNormalBehavior() {
        // Just restart the idle timer to resume normal behavior
        if (this.idleTimer) {
            clearInterval(this.idleTimer);
            this.startIdleTimer();
        }
    }
    
    /**
     * Force walking behavior
     */
    forceWalking() {
        this.setBehavior(this.behaviors.WALKING);
        this.movementManager.startRandomMovement();
    }
    
    /**
     * Check if pet is in pain
     */
    isInPain() {
        return this.currentBehavior === this.behaviors.PAIN;
    }
    
    /**
     * Check if pet is idle
     */
    isIdle() {
        return this.currentBehavior === this.behaviors.IDLE;
    }
    
    /**
     * Check if pet is walking
     */
    isWalking() {
        return this.currentBehavior === this.behaviors.WALKING;
    }
    
    /**
     * Get behavior change interval
     */
    getBehaviorChangeInterval() {
        return this.behaviorChangeInterval;
    }
    
    /**
     * Set behavior change interval
     */
    setBehaviorChangeInterval(interval) {
        this.behaviorChangeInterval = interval;
        
        // Restart timers with new interval
        if (this.idleTimer) {
            clearInterval(this.idleTimer);
            this.startIdleTimer();
        }
    }
    
    /**
     * Cleanup timers
     */
    cleanup() {
        if (this.idleTimer) {
            clearInterval(this.idleTimer);
            this.idleTimer = null;
        }
    }

    /**
     * Check if pet is being dragged
     */
    isBeingDragged() {
        // Check if interaction manager exists and is dragging
        if (this.movementManager.petEngine && this.movementManager.petEngine.interactionManager) {
            return this.movementManager.petEngine.interactionManager.isDraggingPet();
        }
        return false;
    }
} 