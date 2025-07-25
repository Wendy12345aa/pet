/**
 * Main application entry point
 */
class WebPetApp {
    constructor() {
        this.canvas = document.getElementById('petCanvas');
        this.petEngine = null;
        this.isRunning = false;
        
        // Set canvas to full window size
        this.resizeCanvas();
        
        // Handle window resize
        window.addEventListener('resize', () => this.resizeCanvas());
        
        // Initialize the application
        this.initialize();
    }
    
    /**
     * Resize canvas to fill the window
     */
    resizeCanvas() {
        this.canvas.width = window.innerWidth;
        this.canvas.height = window.innerHeight;
        
        // Define max pet size (doubled)
        const MAX_PET_SIZE = 256;
        // Always use the maximum size
        const newSize = MAX_PET_SIZE;
        
        // Update pet engine bounds if it exists
        if (this.petEngine) {
            // Update pet size if it changed significantly
            const currentSize = this.petEngine.getSettings().petSize;
            if (Math.abs(currentSize - newSize) > 10) {
                this.petEngine.updatePetSize(newSize);
            }
            this.petEngine.updateBounds();
            // Clamp pet position to new bounds
            const movementManager = this.petEngine.movementManager;
            const pos = movementManager.getPosition();
            movementManager.setPosition(pos.x, pos.y); // This will clamp to bounds
            // If pet is at the edge, trigger a new random movement
            const bounds = movementManager.getBounds();
            if (
                pos.x <= bounds.minX || pos.x >= bounds.maxX ||
                pos.y <= bounds.minY || pos.y >= bounds.maxY
            ) {
                movementManager.startRandomMovement();
            }
        }
    }
    
    /**
     * Initialize the application
     */
    async initialize() {
        try {
            console.log('Initializing Web Pet App...');
            
            // Create pet engine
            this.petEngine = new PetEngine(this.canvas);
            
            // Wait for engine to initialize
            await this.petEngine.initialize();
            
            // Setup UI event listeners
            this.setupEventListeners();
            
            // Start the pet engine
            this.start();
            
            console.log('Web Pet App initialized successfully');
            
        } catch (error) {
            console.error('Error initializing Web Pet App:', error);
        }
    }
    
    /**
     * Setup UI event listeners
     */
    setupEventListeners() {
        // Toggle pet button
        const toggleBtn = document.getElementById('togglePet');
        toggleBtn.addEventListener('click', () => this.togglePet());
        
        // Settings button
        const settingsBtn = document.getElementById('settings');
        settingsBtn.addEventListener('click', () => this.showSettings());
        
        // Close settings button
        const closeSettingsBtn = document.getElementById('closeSettings');
        closeSettingsBtn.addEventListener('click', () => this.hideSettings());
        
        // Settings sliders
        this.setupSettingsSliders();
    }
    
    /**
     * Setup settings sliders
     */
    setupSettingsSliders() {
        // Size slider
        const sizeSlider = document.getElementById('sizeSlider');
        const sizeValue = document.getElementById('sizeValue');
        
        sizeSlider.addEventListener('input', (e) => {
            const size = parseInt(e.target.value);
            sizeValue.textContent = size;
            this.petEngine.updatePetSize(size);
        });
        
        // Animation speed slider
        const speedSlider = document.getElementById('speedSlider');
        const speedValue = document.getElementById('speedValue');
        
        speedSlider.addEventListener('input', (e) => {
            const speed = parseInt(e.target.value);
            speedValue.textContent = speed;
            this.petEngine.updateAnimationSpeed(speed);
        });
        
        // Movement speed slider
        const moveSpeedSlider = document.getElementById('moveSpeedSlider');
        const moveSpeedValue = document.getElementById('moveSpeedValue');
        
        moveSpeedSlider.addEventListener('input', (e) => {
            const speed = parseInt(e.target.value);
            moveSpeedValue.textContent = speed;
            this.petEngine.updateMovementSpeed(speed);
        });
    }
    
    /**
     * Start the pet
     */
    start() {
        if (this.isRunning) return;
        
        this.petEngine.start();
        this.isRunning = true;
        
        const toggleBtn = document.getElementById('togglePet');
        toggleBtn.textContent = 'Stop Pet';
        
        console.log('Pet started');
    }
    
    /**
     * Stop the pet
     */
    stop() {
        if (!this.isRunning) return;
        
        this.petEngine.stop();
        this.isRunning = false;
        
        const toggleBtn = document.getElementById('togglePet');
        toggleBtn.textContent = 'Start Pet';
        
        console.log('Pet stopped');
    }
    
    /**
     * Toggle pet on/off
     */
    togglePet() {
        if (this.isRunning) {
            this.stop();
        } else {
            this.start();
        }
    }
    
    /**
     * Show settings panel
     */
    showSettings() {
        const settingsPanel = document.getElementById('settingsPanel');
        settingsPanel.classList.remove('hidden');
    }
    
    /**
     * Hide settings panel
     */
    hideSettings() {
        const settingsPanel = document.getElementById('settingsPanel');
        settingsPanel.classList.add('hidden');
    }
    
    /**
     * Get current pet status
     */
    getPetStatus() {
        if (!this.petEngine) return null;
        
        return {
            isRunning: this.isRunning,
            behavior: this.petEngine.getCurrentBehavior(),
            position: this.petEngine.getPosition(),
            settings: this.petEngine.getSettings()
        };
    }
    
    /**
     * Cleanup application
     */
    cleanup() {
        if (this.petEngine) {
            this.petEngine.cleanup();
        }
    }
}

// Initialize the application when the page loads
document.addEventListener('DOMContentLoaded', () => {
    window.webPetApp = new WebPetApp();
});

// Cleanup on page unload
window.addEventListener('beforeunload', () => {
    if (window.webPetApp) {
        window.webPetApp.cleanup();
    }
}); 