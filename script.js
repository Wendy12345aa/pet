// Smooth scrolling for navigation links
document.addEventListener('DOMContentLoaded', function() {
    // Smooth scrolling for anchor links
    const links = document.querySelectorAll('a[href^="#"]');
    links.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            const targetSection = document.querySelector(targetId);
            
            if (targetSection) {
                const offsetTop = targetSection.offsetTop - 70; // Account for fixed navbar
                window.scrollTo({
                    top: offsetTop,
                    behavior: 'smooth'
                });
            }
        });
    });

    // Navbar background change on scroll
    const navbar = document.querySelector('.navbar');
    window.addEventListener('scroll', function() {
        if (window.scrollY > 50) {
            navbar.style.background = 'rgba(255, 255, 255, 0.98)';
            navbar.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.1)';
        } else {
            navbar.style.background = 'rgba(255, 255, 255, 0.95)';
            navbar.style.boxShadow = 'none';
        }
    });

    // Add loading animation to feature cards
    const featureCards = document.querySelectorAll('.feature-card');
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);

    featureCards.forEach(card => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(card);
    });

    // Download button click tracking (optional)
    const downloadButtons = document.querySelectorAll('.btn-download');
    downloadButtons.forEach(button => {
        button.addEventListener('click', function() {
            // You can add analytics tracking here
            console.log('Download button clicked:', this.textContent.trim());
        });
    });

    // Add hover effect to screenshot items
    const screenshotItems = document.querySelectorAll('.screenshot-item');
    screenshotItems.forEach(item => {
        item.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px) scale(1.02)';
        });
        
        item.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });

    // Mobile menu toggle (if needed in the future)
    const mobileMenuToggle = document.querySelector('.mobile-menu-toggle');
    const navLinks = document.querySelector('.nav-links');
    
    if (mobileMenuToggle) {
        mobileMenuToggle.addEventListener('click', function() {
            navLinks.classList.toggle('active');
        });
    }

    // Add parallax effect to hero section
    window.addEventListener('scroll', function() {
        const scrolled = window.pageYOffset;
        const hero = document.querySelector('.hero');
        if (hero) {
            const rate = scrolled * -0.5;
            hero.style.transform = `translateY(${rate}px)`;
        }
    });

    // Add typing effect to hero title (optional)
    const heroTitle = document.querySelector('.hero-content h1');
    if (heroTitle) {
        const text = heroTitle.textContent;
        heroTitle.textContent = '';
        heroTitle.style.borderRight = '2px solid white';
        
        let i = 0;
        const typeWriter = () => {
            if (i < text.length) {
                heroTitle.textContent += text.charAt(i);
                i++;
                setTimeout(typeWriter, 100);
            } else {
                heroTitle.style.borderRight = 'none';
            }
        };
        
        // Start typing effect after a short delay
        setTimeout(typeWriter, 500);
    }

    // Add cyberpunk scroll progress indicator
    const progressBar = document.createElement('div');
    progressBar.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 0%;
        height: 4px;
        background: linear-gradient(90deg, #00ffff, #ff00ff, #ffff00, #00ffff);
        background-size: 200% 100%;
        z-index: 1001;
        transition: width 0.1s ease;
        animation: progressGlow 2s linear infinite;
        box-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
    `;
    document.body.appendChild(progressBar);

    window.addEventListener('scroll', function() {
        const scrollTop = window.pageYOffset;
        const docHeight = document.body.offsetHeight - window.innerHeight;
        const scrollPercent = (scrollTop / docHeight) * 100;
        progressBar.style.width = scrollPercent + '%';
    });

    // Add fade-in animation for sections
    const sections = document.querySelectorAll('section');
    const sectionObserver = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, { threshold: 0.1 });

    sections.forEach(section => {
        section.style.opacity = '0';
        section.style.transform = 'translateY(30px)';
        section.style.transition = 'opacity 0.8s ease, transform 0.8s ease';
        sectionObserver.observe(section);
    });
});

// Add cyberpunk interactive elements
document.addEventListener('DOMContentLoaded', function() {
    // Make the demo pet interactive with cyberpunk effects
    const demoPet = document.querySelector('.demo-pet');
    if (demoPet) {
        demoPet.addEventListener('click', function() {
            this.style.transform = 'scale(1.2) rotate(10deg)';
            this.style.filter = 'drop-shadow(0 0 30px #ff00ff) hue-rotate(180deg)';
            
            // Add glitch effect
            this.classList.add('glitch-effect');
            
            setTimeout(() => {
                this.style.transform = 'scale(1) rotate(0deg)';
                this.style.filter = 'drop-shadow(0 0 20px #00ffff)';
                this.classList.remove('glitch-effect');
            }, 300);
        });
    }

    // Add cyberpunk particle effect to download buttons
    const downloadButtons = document.querySelectorAll('.btn-download');
    downloadButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            // Create cyberpunk particle effect
            for (let i = 0; i < 8; i++) {
                const particle = document.createElement('div');
                const colors = ['#00ffff', '#ff00ff', '#ffff00', '#00ff00'];
                const randomColor = colors[Math.floor(Math.random() * colors.length)];
                
                particle.style.cssText = `
                    position: absolute;
                    width: 6px;
                    height: 6px;
                    background: ${randomColor};
                    border-radius: 50%;
                    pointer-events: none;
                    animation: particle 0.8s ease-out forwards;
                    box-shadow: 0 0 10px ${randomColor};
                `;
                
                const rect = this.getBoundingClientRect();
                particle.style.left = rect.left + rect.width / 2 + 'px';
                particle.style.top = rect.top + rect.height / 2 + 'px';
                
                document.body.appendChild(particle);
                
                setTimeout(() => {
                    particle.remove();
                }, 800);
            }
        });
    });

    // Add glitch effect to feature cards on hover
    const featureCards = document.querySelectorAll('.feature-card');
    featureCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.animation = 'glitchText 0.2s ease-in-out';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.animation = '';
        });
    });

    // Add matrix-style text effect to titles
    const titles = document.querySelectorAll('h1, h2, h3');
    titles.forEach(title => {
        title.addEventListener('mouseenter', function() {
            this.style.textShadow = '0 0 20px #00ffff, 0 0 30px #00ffff, 0 0 40px #00ffff';
        });
        
        title.addEventListener('mouseleave', function() {
            this.style.textShadow = '0 0 20px #00ffff';
        });
    });

    // Add cyberpunk sound effect simulation (visual feedback)
    const buttons = document.querySelectorAll('.btn');
    buttons.forEach(button => {
        button.addEventListener('click', function() {
            // Create a ripple effect
            const ripple = document.createElement('div');
            ripple.style.cssText = `
                position: absolute;
                top: 50%;
                left: 50%;
                width: 0;
                height: 0;
                border-radius: 50%;
                background: rgba(0, 255, 255, 0.3);
                transform: translate(-50%, -50%);
                animation: ripple 0.6s ease-out;
                pointer-events: none;
            `;
            
            this.appendChild(ripple);
            
            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });
});

// Add ripple animation
const rippleStyle = document.createElement('style');
rippleStyle.textContent = `
    @keyframes ripple {
        0% {
            width: 0;
            height: 0;
            opacity: 1;
        }
        100% {
            width: 300px;
            height: 300px;
            opacity: 0;
        }
    }
`;
document.head.appendChild(rippleStyle);

// Walking Character Animation
class WalkingCharacter {
    constructor() {
        this.character = document.getElementById('walkingCharacter');
        this.sprite = document.getElementById('characterSprite');
        this.currentFrame = 0;
        this.walkingFrames = [
            'resources/CharacterSets/Pets/ayano/walking/walking_frame_000.png',
            'resources/CharacterSets/Pets/ayano/walking/walking_frame_001.png',
            'resources/CharacterSets/Pets/ayano/walking/walking_frame_002.png'
        ];
        this.idleFrames = [
            'resources/CharacterSets/Pets/ayano/idle/idle_frame_000.png'
        ];
        this.isWalking = true;
        this.walkSpeed = 200; // milliseconds per frame
        this.currentDirection = 1; // 1 for right, -1 for left
        this.init();
    }

    init() {
        this.startWalkingAnimation();
        this.addInteraction();
        this.startDirectionTracking();
    }

    startWalkingAnimation() {
        setInterval(() => {
            if (this.isWalking) {
                this.nextFrame();
            }
        }, this.walkSpeed);
    }

    nextFrame() {
        this.currentFrame = (this.currentFrame + 1) % this.walkingFrames.length;
        this.sprite.src = this.walkingFrames[this.currentFrame];
    }

    setWalking(walking) {
        this.isWalking = walking;
        if (walking) {
            this.sprite.src = this.walkingFrames[this.currentFrame];
        } else {
            this.sprite.src = this.idleFrames[0];
        }
    }

    startDirectionTracking() {
        // Track animation progress to determine direction
        let lastX = -100;
        this.character.addEventListener('animationiteration', () => {
            // Reset direction at the start of each cycle
            this.setDirection(1);
        });

        // Monitor position changes during animation with fast updates
        const observer = new MutationObserver(() => {
            const computedStyle = window.getComputedStyle(this.character);
            const transform = computedStyle.transform;
            
            if (transform && transform !== 'none') {
                const matrix = transform.match(/matrix.*\((.+)\)/);
                if (matrix) {
                    const values = matrix[1].split(', ');
                    const translateX = parseFloat(values[4]);
                    
                    if (translateX > lastX + 5) {
                        this.setDirection(1); // Moving right
                    } else if (translateX < lastX - 5) {
                        this.setDirection(-1); // Moving left
                    }
                    lastX = translateX;
                }
            }
        });

        observer.observe(this.character, {
            attributes: true,
            attributeFilter: ['style']
        });
    }

    setDirection(direction) {
        if (this.currentDirection !== direction) {
            this.currentDirection = direction;
            // Apply very fast flip transition
            this.sprite.style.transform = `scaleX(${direction})`;
        }
    }

    addInteraction() {
        this.character.addEventListener('mouseenter', () => {
            this.setWalking(false);
            this.character.style.animationPlayState = 'paused';
        });

        this.character.addEventListener('mouseleave', () => {
            this.setWalking(true);
            this.character.style.animationPlayState = 'running';
        });

        this.character.addEventListener('click', () => {
            this.character.style.animationPlayState = 'paused';
            setTimeout(() => {
                this.character.style.animationPlayState = 'running';
            }, 1000);
        });
    }
}

// Page Navigation System
let currentPage = 'home-page';

function showPage(pageId) {
    // Hide current page
    const currentPageElement = document.getElementById(currentPage);
    if (currentPageElement) {
        currentPageElement.classList.remove('active');
        currentPageElement.classList.add('slide-out');
    }

    // Show new page
    const newPageElement = document.getElementById(pageId);
    if (newPageElement) {
        newPageElement.classList.remove('slide-out');
        newPageElement.classList.add('active');
        currentPage = pageId;
    }

    // Update navigation active state
    updateNavigation(pageId);
}

function updateNavigation(activePage) {
    // Remove active class from all nav links
    const navLinks = document.querySelectorAll('.nav-links a');
    navLinks.forEach(link => link.classList.remove('active'));

    // Add active class to current page link
    const activeLink = document.querySelector(`[onclick="showPage('${activePage}')"]`);
    if (activeLink) {
        activeLink.classList.add('active');
    }
}

// Language switching functionality
let currentLanguage = 'en';

function toggleLanguage() {
    currentLanguage = currentLanguage === 'en' ? 'zh' : 'en';
    updateLanguage();
    updateLanguageButton();
}

function updateLanguage() {
    const elements = document.querySelectorAll('[data-en][data-zh]');
    elements.forEach(element => {
        const text = element.getAttribute(`data-${currentLanguage}`);
        if (text) {
            element.textContent = text;
        }
    });
}

function updateLanguageButton() {
    const langText = document.querySelector('.lang-text');
    if (langText) {
        langText.textContent = currentLanguage === 'en' ? 'EN' : '中';
    }
}

// Initialize walking character when page loads
document.addEventListener('DOMContentLoaded', () => {
    new WalkingCharacter();
    
    // Show home page by default
    showPage('home-page');
    
    // Initialize language
    updateLanguage();

    // Hamburger menu toggle
    const mobileMenuToggle = document.querySelector('.mobile-menu-toggle');
    const navLinksWrapper = document.querySelector('.nav-links-wrapper');
    const navLinks = document.querySelectorAll('.nav-links a');
    if (mobileMenuToggle && navLinksWrapper) {
        mobileMenuToggle.addEventListener('click', function() {
            navLinksWrapper.classList.toggle('active');
        });
        navLinks.forEach(link => {
            link.addEventListener('click', function() {
                if (window.innerWidth <= 768) {
                    navLinksWrapper.classList.remove('active');
                }
            });
        });
    }
});

// Floating Fish Animation
const floatingFish = document.getElementById('floatingFish');
let isFishInteractive = false;

if (floatingFish) {
    floatingFish.addEventListener('click', () => {
        if (!isFishInteractive) {
            isFishInteractive = true;
            floatingFish.classList.add('interactive');
            
            // Change fish color temporarily
            const fishIcon = floatingFish.querySelector('i');
            fishIcon.style.color = '#10b981';
            
            setTimeout(() => {
                isFishInteractive = false;
                floatingFish.classList.remove('interactive');
                fishIcon.style.color = '';
            }, 500);
        }
    });
}

// Create explosion effect for character interaction
function createExplosion(element) {
    const rect = element.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;
    
    for (let i = 0; i < 12; i++) {
        const particle = document.createElement('div');
        const angle = (i / 12) * Math.PI * 2;
        const distance = 50 + Math.random() * 30;
        const colors = ['#00ffff', '#ff00ff', '#ffff00', '#00ff00'];
        const randomColor = colors[Math.floor(Math.random() * colors.length)];
        
        particle.style.cssText = `
            position: fixed;
            width: 8px;
            height: 8px;
            background: ${randomColor};
            border-radius: 50%;
            left: ${centerX}px;
            top: ${centerY}px;
            pointer-events: none;
            z-index: 1000;
            box-shadow: 0 0 15px ${randomColor};
            animation: explosionParticle 1s ease-out forwards;
        `;
        
        document.body.appendChild(particle);
        
        // Animate particle movement
        setTimeout(() => {
            const endX = centerX + Math.cos(angle) * distance;
            const endY = centerY + Math.sin(angle) * distance;
            particle.style.transform = `translate(${endX - centerX}px, ${endY - centerY}px)`;
        }, 10);
        
        setTimeout(() => {
            particle.remove();
        }, 1000);
    }
}

// Add explosion animation
const explosionStyle = document.createElement('style');
explosionStyle.textContent = `
    @keyframes explosionParticle {
        0% {
            opacity: 1;
            transform: scale(1);
        }
        100% {
            opacity: 0;
            transform: scale(0);
        }
    }
`;
document.head.appendChild(explosionStyle);

// Add CSS for cyberpunk animations
const style = document.createElement('style');
style.textContent = `
    @keyframes particle {
        0% {
            transform: translate(0, 0) scale(1);
            opacity: 1;
        }
        100% {
            transform: translate(${Math.random() * 100 - 50}px, ${Math.random() * 100 - 50}px) scale(0);
            opacity: 0;
        }
    }
    
    @keyframes progressGlow {
        0% { background-position: 0% 50%; }
        100% { background-position: 200% 50%; }
    }
    
    @keyframes glitchText {
        0%, 100% { transform: translate(0); }
        20% { transform: translate(-2px, 2px); }
        40% { transform: translate(-2px, -2px); }
        60% { transform: translate(2px, 2px); }
        80% { transform: translate(2px, -2px); }
    }
    
    .glitch-effect {
        animation: glitchText 0.3s ease-in-out;
    }
`;
document.head.appendChild(style); 