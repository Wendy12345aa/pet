/**
 * BubbleManager - Handles bubble generation and animation around the pet
 */
class BubbleManager {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.bubbles = [];
        this.maxBubbles = 20;
        this.bubbleSpawnRate = 0.3; // Chance to spawn a bubble each frame when pet is moving
    }
    
    /**
     * Generate a new bubble near the pet
     */
    createBubble(x, y) {
        const bubble = {
            x: x + (Math.random() - 0.5) * 40, // Random offset from pet
            y: y + Math.random() * 20, // Slightly above pet
            size: Math.random() * 8 + 4, // Random size between 4-12px
            speed: Math.random() * 2 + 1, // Random speed
            opacity: Math.random() * 0.5 + 0.3, // Random opacity
            wobble: Math.random() * 0.1, // Random wobble factor
            wobbleSpeed: Math.random() * 0.1 + 0.05, // Random wobble speed
            wobbleOffset: Math.random() * Math.PI * 2 // Random wobble start
        };
        
        this.bubbles.push(bubble);
        
        // Keep only maxBubbles
        if (this.bubbles.length > this.maxBubbles) {
            this.bubbles.shift();
        }
    }
    
    /**
     * Update all bubbles
     */
    update() {
        for (let i = this.bubbles.length - 1; i >= 0; i--) {
            const bubble = this.bubbles[i];
            
            // Move bubble upward
            bubble.y -= bubble.speed;
            
            // Add wobble
            bubble.wobbleOffset += bubble.wobbleSpeed;
            bubble.x += Math.sin(bubble.wobbleOffset) * bubble.wobble;
            
            // Fade out as it rises
            bubble.opacity -= 0.005;
            
            // Remove bubble if it's off screen or too transparent
            if (bubble.y < -20 || bubble.opacity <= 0) {
                this.bubbles.splice(i, 1);
            }
        }
    }
    
    /**
     * Draw all bubbles
     */
    draw() {
        // Check if dark mode is active
        const isDarkMode = document.documentElement.getAttribute('data-theme') === 'dark';
        
        this.bubbles.forEach(bubble => {
            this.ctx.save();
            
            // Set bubble style based on theme
            this.ctx.globalAlpha = bubble.opacity;
            
            if (isDarkMode) {
                // Dark mode bubble colors - blue/cyan theme
                this.ctx.fillStyle = 'rgba(100, 150, 255, 0.8)';
                this.ctx.strokeStyle = 'rgba(100, 150, 255, 0.4)';
            } else {
                // Light mode bubble colors - white theme
                this.ctx.fillStyle = 'rgba(255, 255, 255, 0.8)';
                this.ctx.strokeStyle = 'rgba(255, 255, 255, 0.4)';
            }
            
            this.ctx.lineWidth = 1;
            
            // Draw bubble
            this.ctx.beginPath();
            this.ctx.arc(bubble.x, bubble.y, bubble.size, 0, Math.PI * 2);
            this.ctx.fill();
            this.ctx.stroke();
            
            // Add highlight
            if (isDarkMode) {
                this.ctx.fillStyle = 'rgba(150, 200, 255, 0.6)';
            } else {
                this.ctx.fillStyle = 'rgba(255, 255, 255, 0.6)';
            }
            
            this.ctx.beginPath();
            this.ctx.arc(bubble.x - bubble.size * 0.3, bubble.y - bubble.size * 0.3, bubble.size * 0.3, 0, Math.PI * 2);
            this.ctx.fill();
            
            this.ctx.restore();
        });
    }
    
    /**
     * Try to spawn bubbles when pet is moving
     */
    trySpawnBubble(petX, petY, isMoving) {
        if (isMoving && Math.random() < this.bubbleSpawnRate) {
            this.createBubble(petX, petY);
        }
    }
    
    /**
     * Clear all bubbles
     */
    clear() {
        this.bubbles = [];
    }
} 