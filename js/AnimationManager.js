/**
 * AnimationManager - Handles sprite loading and frame-based animations
 * 
 * ISSUE #1: IMAGE DISTORTION/DEFORMATION
 * 
 * The main causes of image distortion are:
 * 1. Incorrect aspect ratio handling when drawing images
 * 2. Forced resizing without maintaining proportions
 * 3. Canvas drawing without considering original image dimensions
 * 4. Missing image loading error handling
 */
class AnimationManager {
    constructor() {
        this.sprites = new Map(); // Map of animation name to array of frames
        this.currentAnimation = 'idle';
        this.currentFrame = 0;
        this.frameCount = 0;
        this.animationSpeed = 150; // milliseconds per frame
        this.lastFrameTime = 0;
        this.isPlaying = false;
        
        // Animation states with frame counts
        this.animations = {
            idle: { frames: 1, speed: 150 },
            walking: { frames: 4, speed: 100 }, // 4 walking frames
            pain: { frames: 2, speed: 120 }
        };
        
        // Default sprite data (fallback)
        this.defaultSprites = {
            idle: [this.createDefaultSprite('#4CAF50', '😊')],
            walking: [
                this.createDefaultSprite('#2196F3', '🚶'),
                this.createDefaultSprite('#2196F3', '🚶'),
                this.createDefaultSprite('#2196F3', '🚶'),
                this.createDefaultSprite('#2196F3', '🚶')
            ],
            pain: [
                this.createDefaultSprite('#F44336', '😵'),
                this.createDefaultSprite('#F44336', '😵')
            ]
        };
    }
    
    /**
     * Load sprite images for animations
     * 
     * POTENTIAL ISSUE: Image loading may fail silently, causing distortion
     * SOLUTION: Add better error handling and fallback mechanisms
     */
    async loadSprites() {
        try {
            // Try to load from existing character sets first
            await this.loadFromCharacterSets();
            
            // If no character sets found, use default sprites
            if (this.sprites.size === 0) {
                console.log('No character sets found, using default sprites');
                this.sprites = new Map(Object.entries(this.defaultSprites));
            }
            
            // Ensure all animations have at least one frame
            for (const [animationName, animation] of Object.entries(this.animations)) {
                if (!this.sprites.has(animationName)) {
                    this.sprites.set(animationName, this.defaultSprites[animationName]);
                }
            }
            
            // Initialize animation state
            this.currentFrame = 0;
            this.frameCount = this.animations[this.currentAnimation].frames;
            this.animationSpeed = this.animations[this.currentAnimation].speed;
            
            // Log the final sprites map
            for (const [key, frames] of this.sprites.entries()) {
                console.log(`Animation '${key}' loaded with ${frames.length} frames.`);
            }
            
            console.log(`Loaded ${this.sprites.size} animation states`);
            return true;
        } catch (error) {
            console.error('Error loading sprites:', error);
            this.sprites = new Map(Object.entries(this.defaultSprites));
            return false;
        }
    }
    
    /**
     * Try to load sprites from existing character sets
     * 
     * ISSUE: Hardcoded character path may cause loading failures
     * SOLUTION: Make character path configurable or dynamic
     */
    async loadFromCharacterSets() {
        // Only use the requested character set
        const characterPath = 'resources/CharacterSets/Pets/New_pet_1753150496386';
        console.log('Starting to load character set from:', characterPath);
        
        try {
            const loaded = await this.loadCharacterSet(characterPath);
            if (loaded) {
                console.log(`Successfully loaded character set from ${characterPath}`);
                return true;
            } else {
                console.warn(`Failed to load character set from ${characterPath}`);
            }
        } catch (error) {
            console.warn(`Error loading character set from ${characterPath}:`, error);
        }
        
        // Fallback to single images from Image folder
        console.log('Falling back to Image folder sprites');
        const spritePaths = {
            idle: 'Image/chibi01.png',
            walking: 'Image/chibi02.png',
            pain: 'Image/chibi03.png'
        };
        for (const [state, path] of Object.entries(spritePaths)) {
            try {
                console.log(`Attempting to load fallback sprite: ${path}`);
                const img = await this.loadImage(path);
                if (img) {
                    this.sprites.set(state, [img]);
                    console.log(`Loaded fallback ${state} sprite from ${path}`);
                } else {
                    console.warn(`Failed to load fallback sprite: ${path}`);
                }
            } catch (error) {
                console.warn(`Error loading fallback sprite ${path}:`, error);
            }
        }
    }
    
    /**
     * Load all frames for a given animation from a character set
     * 
     * ISSUE: Hardcoded frame counts may not match actual files
     * SOLUTION: Dynamically detect frame count or make it configurable
     */
    async loadCharacterSet(characterPath) {
        const animations = ['idle', 'walking', 'pain']; // No special
        let loadedAny = false;
        
        console.log('Loading character set animations:', animations);
        
        for (const animation of animations) {
            try {
                console.log(`Loading animation: ${animation}`);
                const frames = await this.loadAnimationFrames(characterPath, animation);
                if (frames.length > 0) {
                    this.sprites.set(animation, frames);
                    this.animations[animation].frames = frames.length;
                    console.log(`Successfully loaded ${animation} animation with ${frames.length} frames`);
                    loadedAny = true;
                } else {
                    console.warn(`No frames loaded for animation: ${animation}`);
                }
            } catch (error) {
                console.warn(`Failed to load ${animation} animation:`, error);
            }
        }
        return loadedAny;
    }
    
    /**
     * Load all frames for a given animation from a character set
     * 
     * ISSUE: Hardcoded frame counts may cause missing or extra frames
     * SOLUTION: Use metadata files or dynamic frame detection
     */
    async loadAnimationFrames(characterPath, animationName) {
        // Hardcoded frame counts for New_pet_1753150496386
        // ISSUE: This is brittle and may not match actual files
        const frameCounts = {
            idle: 2,
            walking: 4,
            pain: 2
        };
        const frames = [];
        const count = frameCounts[animationName] || 1;
        
        console.log(`Loading ${count} frames for animation: ${animationName}`);
        
        for (let i = 0; i < count; i++) {
            const framePath = `${characterPath}/${animationName}/${animationName}_frame_00${i}.png`;
            try {
                console.log(`Attempting to load: ${framePath}`);
                const img = await this.loadImage(framePath);
                if (img) {
                    frames.push(img);
                    console.log(`Successfully loaded frame: ${framePath}`);
                } else {
                    console.warn(`Image object not created for: ${framePath}`);
                }
            } catch (error) {
                console.warn(`Failed to load frame: ${framePath}`, error);
            }

        }
        
        console.log(`Total frames loaded for ${animationName}: ${frames.length}`);
        return frames;
    }
    
    /**
     * Load an image and return a Promise that resolves when loaded
     * 
     * ISSUE: No validation of image dimensions or quality
     * SOLUTION: Add image validation and error handling
     */
    loadImage(src) {
        return new Promise((resolve, reject) => {
            const img = new window.Image();
            img.onload = () => {
                // ISSUE: No validation of loaded image
                // SOLUTION: Add dimension checks and quality validation
                console.log(`Image loaded successfully: ${src}, dimensions: ${img.naturalWidth}x${img.naturalHeight}`);
                resolve(img);
            };
            img.onerror = (e) => {
                console.error(`Failed to load image: ${src}`, e);
                reject(e);
            };
            img.src = src;
        });
    }
    
    /**
     * Create a default sprite with emoji
     * 
     * This creates a canvas-based sprite as fallback
     * ISSUE: Canvas sprites may not scale properly
     * SOLUTION: Ensure proper scaling in drawPet method
     */
    createDefaultSprite(color, emoji) {
        const canvas = document.createElement('canvas');
        canvas.width = 128;
        canvas.height = 128;
        const ctx = canvas.getContext('2d');
        
        // Draw background circle
        ctx.fillStyle = color;
        ctx.beginPath();
        ctx.arc(64, 64, 60, 0, Math.PI * 2);
        ctx.fill();
        
        // Draw emoji
        ctx.font = '48px Arial';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(emoji, 64, 64);
        
        return canvas;
    }
    
    /**
     * Set current animation state
     */
    setAnimation(animationName) {
        if (this.animations[animationName] && this.currentAnimation !== animationName) {
            this.currentAnimation = animationName;
            this.currentFrame = 0;
            this.frameCount = this.animations[animationName].frames;
            this.animationSpeed = this.animations[animationName].speed;
            console.log(`Switched to ${animationName} animation`);
        }
    }
    
    /**
     * Update animation frame
     */
    update(currentTime) {
        if (!this.isPlaying) return;
        
        if (currentTime - this.lastFrameTime >= this.animationSpeed) {
            this.currentFrame = (this.currentFrame + 1) % this.frameCount;
            this.lastFrameTime = currentTime;
        }
    }
    
    /**
     * Get current frame image
     * 
     * ISSUE: No validation of frame data before returning
     * SOLUTION: Add frame validation and better error handling
     */
    getCurrentFrame() {
        const frames = this.sprites.get(this.currentAnimation);
        
        // Debug log (only once to avoid spam)
        if (!this._debugLogged) {
            console.log('DEBUG getCurrentFrame:', {
                currentAnimation: this.currentAnimation,
                frames: frames,
                spritesMapSize: this.sprites.size,
                spritesKeys: Array.from(this.sprites.keys())
            });
            this._debugLogged = true;
        }
        
        if (!frames || frames.length === 0) {
            console.warn(`No frames found for animation: ${this.currentAnimation}`);
            console.log('Available animations in sprites map:', Array.from(this.sprites.keys()));
            return this.defaultSprites.idle[0];
        }
        
        // Get the current frame from the array
        const frameIndex = this.currentFrame % frames.length;
        const frame = frames[frameIndex];
        
        // Debug: Log when frame is null
        if (!frame) {
            console.warn('DEBUG: Frame is null!', {
                frameIndex: frameIndex,
                framesLength: frames.length,
                currentFrame: this.currentFrame,
                frames: frames
            });
        }
        
        return frame;
    }
    
    /**
     * Start animation
     */
    play() {
        this.isPlaying = true;
        this.lastFrameTime = performance.now();
    }
    
    /**
     * Stop animation
     */
    stop() {
        this.isPlaying = false;
    }
    
    /**
     * Set animation speed
     */
    setSpeed(speed) {
        this.animationSpeed = speed;
        // Update all animation speeds
        for (const anim of Object.values(this.animations)) {
            anim.speed = speed;
        }
    }
    
    /**
     * Get current animation state
     */
    getCurrentAnimation() {
        return this.currentAnimation;
    }
    
    /**
     * Check if animation is playing
     */
    isAnimationPlaying() {
        return this.isPlaying;
    }
}
