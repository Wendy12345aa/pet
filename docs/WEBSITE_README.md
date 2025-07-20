# Desktop Pet Website

A modern, responsive website for showcasing the Desktop Pet application. This website is designed to be hosted on GitHub Pages and provides an attractive landing page for users to learn about and download the application.

## Features

- **Modern Design**: Clean, professional design with smooth animations
- **Responsive Layout**: Works perfectly on desktop, tablet, and mobile devices
- **Interactive Elements**: Smooth scrolling, hover effects, and engaging animations
- **Download Section**: Easy access to the executable file and source code
- **Feature Showcase**: Highlights the key features of the Desktop Pet application
- **Screenshot Gallery**: Visual preview of the application in action

## File Structure

```
├── index.html          # Main HTML file
├── styles.css          # CSS styles and responsive design
├── script.js           # JavaScript for interactivity
├── Image/              # Application screenshots and images
│   ├── chibi01.png
│   ├── chibi02.png
│   └── chibi03.png
└── DesktopPet-EXE/     # Executable files for download
    └── DesktopPet.exe
```

## Deployment to GitHub Pages

### Method 1: Automatic Deployment (Recommended)

1. **Push to GitHub**: Upload all website files to your repository
2. **Enable GitHub Pages**:
   - Go to your repository on GitHub
   - Click on "Settings" tab
   - Scroll down to "Pages" section
   - Under "Source", select "Deploy from a branch"
   - Choose "main" branch and "/ (root)" folder
   - Click "Save"

3. **Access Your Website**: Your website will be available at `https://yourusername.github.io/pet`

### Method 2: Manual Setup

1. Create a new branch called `gh-pages`
2. Upload all website files to this branch
3. Enable GitHub Pages from the `gh-pages` branch

## Customization

### Updating Content

1. **Edit `index.html`** to change:
   - Application description
   - Feature descriptions
   - Download links
   - Contact information

2. **Update `styles.css`** to modify:
   - Colors and themes
   - Layout and spacing
   - Typography
   - Animations

3. **Modify `script.js`** to add:
   - Additional interactive features
   - Analytics tracking
   - Custom animations

### Changing Colors

The website uses a purple/indigo color scheme. To change colors, update these CSS variables in `styles.css`:

```css
/* Primary colors */
--primary-color: #6366f1;
--secondary-color: #8b5cf6;
--accent-color: #10b981;

/* Background gradients */
--hero-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
--download-gradient: linear-gradient(135deg, #1e293b 0%, #334155 100%);
```

### Adding New Sections

To add new sections, follow this template:

```html
<section id="new-section" class="new-section">
    <div class="container">
        <h2>Section Title</h2>
        <div class="section-content">
            <!-- Your content here -->
        </div>
    </div>
</section>
```

And add corresponding CSS:

```css
.new-section {
    padding: 80px 0;
    background: #f8fafc;
}

.new-section h2 {
    text-align: center;
    font-size: 2.5rem;
    margin-bottom: 60px;
    color: #1e293b;
}
```

## Required Files

### Images
Make sure these images are available in the `Image/` folder:
- `chibi01.png` - Main pet character
- `chibi02.png` - Alternative pet character
- `chibi03.png` - Another pet character

### Executable
Ensure the executable file is in the correct location:
- `DesktopPet-EXE/DesktopPet.exe`

## Browser Compatibility

The website is compatible with:
- Chrome 60+
- Firefox 55+
- Safari 12+
- Edge 79+

## Performance Optimization

The website is optimized for:
- Fast loading times
- Smooth animations
- Mobile performance
- SEO best practices

## Troubleshooting

### Images Not Loading
- Check that image files exist in the correct paths
- Verify file names match exactly (case-sensitive)
- Ensure images are committed to the repository

### Download Links Not Working
- Verify the executable file path is correct
- Check that the file is committed to the repository
- Test the download link directly

### Styling Issues
- Clear browser cache
- Check for CSS syntax errors
- Verify all CSS files are properly linked

## Analytics (Optional)

To add Google Analytics:

1. Get your tracking ID from Google Analytics
2. Add this code to the `<head>` section of `index.html`:

```html
<!-- Google Analytics -->
<script async src="https://www.googletagmanager.com/gtag/js?id=GA_TRACKING_ID"></script>
<script>
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());
  gtag('config', 'GA_TRACKING_ID');
</script>
```

Replace `GA_TRACKING_ID` with your actual tracking ID.

## Support

For issues with the website:
1. Check the browser console for JavaScript errors
2. Verify all files are properly uploaded
3. Test on different browsers and devices
4. Check GitHub Pages status and settings

## License

This website template is open source and available under the same license as the Desktop Pet application. 