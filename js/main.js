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
        // Settings button
        const settingsBtn = document.getElementById('settings');
        settingsBtn.addEventListener('click', () => this.showSettings());
        
        // Close settings button
        const closeSettingsBtn = document.getElementById('closeSettings');
        closeSettingsBtn.addEventListener('click', () => this.hideSettings());
        
        // Dark mode toggle
        const darkModeBtn = document.getElementById('darkModeToggle');
        darkModeBtn.addEventListener('click', () => this.toggleDarkMode());
        
        // Settings sliders
        this.setupSettingsSliders();
        
        // Try Online button
        const tryOnlineBtn = document.getElementById('tryOnlineBtn');
        if (tryOnlineBtn) {
            tryOnlineBtn.addEventListener('click', () => {
                const overlay = document.getElementById('rpgTitleOverlay');
                if (overlay) overlay.style.display = 'none';
                // Show the top-right download button
                const topDownloadBtn = document.getElementById('downloadExeTopBtn');
                if (topDownloadBtn) topDownloadBtn.style.display = 'inline-flex';
            });
        }
        
        // Theme toggle in settings panel
        const themeToggle = document.getElementById('themeToggle');
        if (themeToggle) {
            themeToggle.addEventListener('change', (e) => {
                const isDark = e.target.checked;
                const newTheme = isDark ? 'dark' : 'light';
                document.documentElement.setAttribute('data-theme', newTheme);
                document.getElementById('app').setAttribute('data-theme', newTheme);
                localStorage.setItem('theme', newTheme);
                this.updateDarkModeButton(isDark);
            });
        }
        
        // Initialize theme from localStorage
        this.initializeTheme();

        // Pet character selector
        const petSelector = document.getElementById('petSelector');
        if (petSelector) {
            petSelector.addEventListener('change', async (e) => {
                const petName = e.target.value;
                // Update the animation manager to use the new pet character set
                if (this.petEngine && this.petEngine.animationManager) {
                    await this.petEngine.animationManager.loadFromCharacterSets(`resources/CharacterSets/Pets/${petName}`);
                    // Reset animation state
                    this.petEngine.animationManager.setAnimation('idle');
                    // Optionally reset pet position to center
                    const center = this.petEngine.movementManager.getCanvasCenter();
                    this.petEngine.movementManager.setPosition(center.x, center.y);
                }
            });
        }

        // Translation dictionary
        const translations = {
            en: {
                title: 'Your Digital Companion',
                tryOnline: 'Try Online',
                download: 'Download EXE',
                settings: 'Settings',
                togglePet: 'Toggle Pet',
                darkMode: 'Dark Mode',
                petSettings: 'Pet Settings',
                size: 'Size',
                animationSpeed: 'Animation Speed',
                movementSpeed: 'Movement Speed',
                petCharacter: 'Pet Character',
                language: 'Language',
                close: 'Close',
            },
            zh: {
                title: '你的数字伙伴',
                tryOnline: '在线体验',
                download: '下载 EXE',
                settings: '设置',
                togglePet: '切换宠物',
                darkMode: '夜间模式',
                petSettings: '宠物设置',
                size: '大小',
                animationSpeed: '动画速度',
                movementSpeed: '移动速度',
                petCharacter: '宠物角色',
                language: '语言',
                close: '关闭',
            }
        };

        function updateLanguageUI(lang) {
            const t = translations[lang] || translations.en;
            document.getElementById('rpgTitle').textContent = t.title;
            const tryOnlineBtn = document.getElementById('tryOnlineBtn');
            if (tryOnlineBtn) tryOnlineBtn.textContent = t.tryOnline;
            const downloadBtn = document.getElementById('rpgDownloadBtn');
            if (downloadBtn) downloadBtn.textContent = t.download;
            const settingsBtn = document.getElementById('settings');
            if (settingsBtn) settingsBtn.textContent = t.settings;
            const togglePetBtn = document.getElementById('togglePet');
            if (togglePetBtn) togglePetBtn.textContent = t.togglePet;
            const themeLabel = document.querySelector('label[for="themeToggle"]');
            if (themeLabel) themeLabel.textContent = t.darkMode;
            const petSettings = document.querySelector('#settingsPanel h3');
            if (petSettings) petSettings.textContent = t.petSettings;
            // Dynamic labels
            const sizeValue = document.getElementById('sizeValue').textContent;
            const sizeLabel = document.querySelector('label[for="sizeSlider"]');
            if (sizeLabel) sizeLabel.innerHTML = `${t.size}: <span id='sizeValue'>${sizeValue}</span>px`;
            const speedValue = document.getElementById('speedValue').textContent;
            const animLabel = document.querySelector('label[for="speedSlider"]');
            if (animLabel) animLabel.innerHTML = `${t.animationSpeed}: <span id='speedValue'>${speedValue}</span>ms`;
            const moveSpeedValue = document.getElementById('moveSpeedValue').textContent;
            const moveLabel = document.querySelector('label[for="moveSpeedSlider"]');
            if (moveLabel) moveLabel.innerHTML = `${t.movementSpeed}: <span id='moveSpeedValue'>${moveSpeedValue}</span>px`;
            const petCharLabel = document.querySelector('label[for="petSelector"]');
            if (petCharLabel) petCharLabel.textContent = t.petCharacter;
            const langLabel = document.querySelector('label[for="languageSelector"]');
            if (langLabel) langLabel.textContent = t.language;
            const closeBtn = document.getElementById('closeSettings');
            if (closeBtn) closeBtn.textContent = t.close;
        }

        // Language selector
        const languageSelector = document.getElementById('languageSelector');
        if (languageSelector) {
            languageSelector.addEventListener('change', (e) => {
                const lang = e.target.value;
                localStorage.setItem('language', lang);
                updateLanguageUI(lang);
            });
        }
        // On load, set language from localStorage
        const savedLang = localStorage.getItem('language') || 'en';
        if (languageSelector) languageSelector.value = savedLang;
        updateLanguageUI(savedLang);
    }
    
    /**
     * Initialize theme from localStorage
     */
    initializeTheme() {
        const savedTheme = localStorage.getItem('theme');
        const isDark = savedTheme === 'dark';
        document.documentElement.setAttribute('data-theme', savedTheme || 'light');
        document.getElementById('app').setAttribute('data-theme', savedTheme || 'light');
        this.updateDarkModeButton(isDark);
        // Sync settings panel toggle
        const themeToggle = document.getElementById('themeToggle');
        if (themeToggle) themeToggle.checked = isDark;
    }
    
    /**
     * Toggle dark mode
     */
    toggleDarkMode() {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        
        document.documentElement.setAttribute('data-theme', newTheme);
        document.getElementById('app').setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);
        
        this.updateDarkModeButton(newTheme === 'dark');
        // Sync settings panel toggle
        const themeToggle = document.getElementById('themeToggle');
        if (themeToggle) themeToggle.checked = (newTheme === 'dark');
    }
    
    /**
     * Update dark mode button appearance
     */
    updateDarkModeButton(isDark) {
        const darkModeBtn = document.getElementById('darkModeToggle');
        const icon = darkModeBtn.querySelector('i');
        const text = darkModeBtn.querySelector('span');
        
        if (isDark) {
            icon.className = 'fas fa-sun';
            text.textContent = 'Light Mode';
        } else {
            icon.className = 'fas fa-moon';
            text.textContent = 'Dark Mode';
        }
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
        
        // const toggleBtn = document.getElementById('togglePet'); // This line is removed
        // toggleBtn.textContent = 'Stop Pet'; // This line is removed
        
        console.log('Pet started');
    }
    
    /**
     * Stop the pet
     */
    stop() {
        if (!this.isRunning) return;
        
        this.petEngine.stop();
        this.isRunning = false;
        
        // const toggleBtn = document.getElementById('togglePet'); // This line is removed
        // toggleBtn.textContent = 'Start Pet'; // This line is removed
        
        console.log('Pet stopped');
    }
    
    /**
     * Toggle pet on/off
     */
    // togglePet() { // This function is removed
    //     if (this.isRunning) {
    //         this.stop();
    //     } else {
    //         this.start();
    //     }
    // }
    
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