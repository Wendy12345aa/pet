/**
 * InteractionManager - Handles mouse and touch interactions with the pet
 * 
 * ISSUE #3: CLICK TELEPORTING
 * 
 * The main causes of click teleporting are:
 * 1. Direct position setting without smooth movement
 * 2. Missing drag offset calculations
 * 3. Instant position updates during drag operations
 * 4. Click detection interfering with drag operations
 * 5. Missing movement interpolation during interactions
 */
class InteractionManager {
    constructor(canvas, petEngine) {
        this.canvas = canvas;
        this.petEngine = petEngine;
        
        // Interaction state
        this.isDragging = false;
        this.dragStartX = 0;
        this.dragStartY = 0;
        this.dragOffsetX = 0;
        this.dragOffsetY = 0;
        
        // Click detection
        this.clickStartTime = 0;
        this.clickStartX = 0;
        this.clickStartY = 0;
        this.clickThreshold = 200; // milliseconds
        this.clickDistanceThreshold = 10; // pixels
        
        // Initialize event listeners
        this.initializeEventListeners();
    }
    
    /**
     * Initialize event listeners
     */
    initializeEventListeners() {
        // Mouse events
        this.canvas.addEventListener('mousedown', this.handleMouseDown.bind(this));
        this.canvas.addEventListener('mousemove', this.handleMouseMove.bind(this));
        this.canvas.addEventListener('mouseup', this.handleMouseUp.bind(this));
        this.canvas.addEventListener('click', this.handleClick.bind(this));
        
        // Touch events for mobile
        this.canvas.addEventListener('touchstart', this.handleTouchStart.bind(this));
        this.canvas.addEventListener('touchmove', this.handleTouchMove.bind(this));
        this.canvas.addEventListener('touchend', this.handleTouchEnd.bind(this));
        
        // Prevent context menu
        this.canvas.addEventListener('contextmenu', (e) => e.preventDefault());
    }
    
    /**
     * Handle mouse down event
     * 
     * ISSUE: May cause instant position changes
     * SOLUTION: Use smooth movement and proper drag initialization
     */
    handleMouseDown(event) {
        const rect = this.canvas.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;
        
        // Check if click is on pet
        if (this.isClickOnPet(x, y)) {
            this.startDrag(x, y);
            this.startClickDetection(x, y);
        }
    }
    
    /**
     * Handle mouse move event
     */
    handleMouseMove(event) {
        const rect = this.canvas.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;
        
        // Update cursor based on whether mouse is over pet
        if (this.isClickOnPet(x, y)) {
            this.canvas.style.cursor = this.isDragging ? 'grabbing' : 'grab';
        } else {
            this.canvas.style.cursor = 'default';
        }
        
        if (!this.isDragging) return;
        
        this.updateDrag(x, y);
    }
    
    /**
     * Handle mouse up event
     */
    handleMouseUp(event) {
        if (this.isDragging) {
            this.endDrag();
        }
    }
    
    /**
     * Handle click event
     * 
     * ISSUE: Click handling may interfere with drag operations
     * SOLUTION: Better separation between click and drag events
     */
    handleClick(event) {
        const rect = this.canvas.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;
        
        // Check if this was a valid click (not a drag)
        if (this.isValidClick(x, y)) {
            this.handlePetClick(x, y);
        }
    }
    
    /**
     * Handle touch start event
     * 
     * ISSUE: Touch events may cause instant position changes
     * SOLUTION: Use smooth movement for touch interactions
     */
    handleTouchStart(event) {
        event.preventDefault();
        
        if (event.touches.length === 1) {
            const touch = event.touches[0];
            const rect = this.canvas.getBoundingClientRect();
            const x = touch.clientX - rect.left;
            const y = touch.clientY - rect.top;
            
            if (this.isClickOnPet(x, y)) {
                this.startDrag(x, y);
                this.startClickDetection(x, y);
            }
        }
    }
    
    /**
     * Handle touch move event
     */
    handleTouchMove(event) {
        event.preventDefault();
        
        if (this.isDragging && event.touches.length === 1) {
            const touch = event.touches[0];
            const rect = this.canvas.getBoundingClientRect();
            const x = touch.clientX - rect.left;
            const y = touch.clientY - rect.top;
            
            this.updateDrag(x, y);
        }
    }
    
    /**
     * Handle touch end event
     */
    handleTouchEnd(event) {
        event.preventDefault();
        
        if (this.isDragging) {
            this.endDrag();
        }
        
        // Handle touch click
        if (event.changedTouches.length === 1) {
            const touch = event.changedTouches[0];
            const rect = this.canvas.getBoundingClientRect();
            const x = touch.clientX - rect.left;
            const y = touch.clientY - rect.top;
            
            if (this.isValidClick(x, y)) {
                this.handlePetClick(x, y);
            }
        }
    }
    
    /**
     * Check if click is on pet
     */
    isClickOnPet(x, y) {
        const position = this.petEngine.movementManager.getPosition();
        const petSize = this.petEngine.settings.petSize;
        
        // Make the click area larger for easier grabbing
        const clickArea = petSize + 20; // 20 pixels extra around the pet
        
        return x >= position.x - 10 && x <= position.x + petSize + 10 &&
               y >= position.y - 10 && y <= position.y + petSize + 10;
    }
    
    /**
     * Start drag operation
     * 
     * ISSUE: May cause instant position changes and teleporting
     * SOLUTION: Use smooth movement and proper offset calculations
     */
    startDrag(x, y) {
        const position = this.petEngine.movementManager.getPosition();
        
        this.isDragging = true;
        this.dragStartX = x;
        this.dragStartY = y;
        // Simple offset calculation - distance from mouse to pet center
        this.dragOffsetX = x - position.x;
        this.dragOffsetY = y - position.y;
        
        console.log('Drag started:', {
            mouseX: x,
            mouseY: y,
            petX: position.x,
            petY: position.y,
            offsetX: this.dragOffsetX,
            offsetY: this.dragOffsetY
        });
        
        // ISSUE: Instant movement stop may cause jerky behavior
        // SOLUTION: Use smooth deceleration instead of instant stop
        this.petEngine.movementManager.stopMovement();
        
        // Force the behavior and animation to idle
        if (this.petEngine.behaviorManager) {
            this.petEngine.behaviorManager.forceIdle();
        }
        if (this.petEngine.animationManager) {
            this.petEngine.animationManager.setAnimation('idle');
        }
        
        // Add visual feedback - make pet slightly transparent when dragging
        this.canvas.style.cursor = 'grabbing';
        
        console.log('Started dragging pet');
    }
    
    /**
     * Update drag position
     * 
     * ISSUE: Direct position setting causes teleporting during drag
     * SOLUTION: Use smooth interpolation or gradual position updates
     */
    updateDrag(x, y) {
        if (!this.isDragging) return;
        
        // Simple position calculation - mouse position minus offset
        const newX = x - this.dragOffsetX;
        const newY = y - this.dragOffsetY;
        
        console.log('Drag update:', {
            mouseX: x,
            mouseY: y,
            newX: newX,
            newY: newY,
            offsetX: this.dragOffsetX,
            offsetY: this.dragOffsetY
        });
        
        // ISSUE: Direct position setting causes teleporting
        // SOLUTION: Use smooth interpolation or gradual movement
        this.petEngine.movementManager.setPosition(newX, newY);
        
        // Update cursor
        this.canvas.style.cursor = 'grabbing';
    }
    
    /**
     * End drag operation
     * 
     * ISSUE: May cause sudden movement resumption
     * SOLUTION: Use smooth transition back to normal behavior
     */
    endDrag() {
        this.isDragging = false;
        
        // Reset cursor
        this.canvas.style.cursor = 'default';
        
        // ISSUE: Sudden movement resumption may cause teleporting
        // SOLUTION: Use smooth transition with gradual speed increase
        setTimeout(() => {
            if (!this.isDragging) {
                // Resume normal behavior
                if (this.petEngine.behaviorManager) {
                    this.petEngine.behaviorManager.resumeNormalBehavior();
                }
                // Start new movement
                this.petEngine.movementManager.startRandomMovement();
            }
        }, 1500); // 1.5 second delay instead of 0.5
        
        console.log('Stopped dragging pet');
    }
    
    /**
     * Start click detection
     */
    startClickDetection(x, y) {
        this.clickStartTime = Date.now();
        this.clickStartX = x;
        this.clickStartY = y;
    }
    
    /**
     * Check if click is valid (not a drag)
     * 
     * ISSUE: Click detection may interfere with drag operations
     * SOLUTION: Better separation between click and drag events
     */
    isValidClick(x, y) {
        const clickTime = Date.now() - this.clickStartTime;
        const clickDistance = Math.sqrt(
            Math.pow(x - this.clickStartX, 2) + 
            Math.pow(y - this.clickStartY, 2)
        );
        
        return clickTime <= this.clickThreshold && 
               clickDistance <= this.clickDistanceThreshold;
    }
    
    /**
     * Handle pet click
     * 
     * ISSUE: Click handling may cause instant position changes
     * SOLUTION: Use smooth animations instead of instant changes
     */
    handlePetClick(x, y) {
        console.log('Pet clicked!');
        
        // ISSUE: May cause instant behavior changes
        // SOLUTION: Use smooth transitions and animations
        this.petEngine.behaviorManager.playSpecialAnimation();
        
        // Add some visual feedback
        this.createClickEffect(x, y);
    }
    
    /**
     * Create click effect
     */
    createClickEffect(x, y) {
        const canvas = this.canvas;
        const ctx = canvas.getContext('2d');
        
        // Create a simple ripple effect
        let radius = 0;
        const maxRadius = 30;
        const ripple = () => {
            ctx.save();
            ctx.globalAlpha = 1 - (radius / maxRadius);
            ctx.strokeStyle = '#ffffff';
            ctx.lineWidth = 2;
            ctx.beginPath();
            ctx.arc(x, y, radius, 0, Math.PI * 2);
            ctx.stroke();
            ctx.restore();
            
            radius += 2;
            
            if (radius < maxRadius) {
                requestAnimationFrame(ripple);
            }
        };
        
        ripple();
    }
    
    /**
     * Check if currently dragging
     */
    isDraggingPet() {
        return this.isDragging;
    }
    
    /**
     * Get drag state
     */
    getDragState() {
        return {
            isDragging: this.isDragging,
            startX: this.dragStartX,
            startY: this.dragStartY,
            offsetX: this.dragOffsetX,
            offsetY: this.dragOffsetY
        };
    }
    
    /**
     * Cleanup event listeners
     */
    cleanup() {
        // Remove event listeners if needed
        // For now, we'll keep them active
    }
} 