// If your project uses packages, add the correct one here, e.g.:
// package pet;

import javax.swing.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class CharacterFileManager {
    private static final String[] SUPPORTED_FORMATS = {".png", ".jpg", ".jpeg", ".gif"};
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB max
    /**
     * Copy and organize imported images into character set directory
     */
    public static boolean importImageFiles(File[] imageFiles, String targetDirectory, String animationType) {
        try {
            File targetDir = new File(targetDirectory);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            // Create animation subdirectory
            File animationDir = new File(targetDir, animationType);
            if (!animationDir.exists()) {
                animationDir.mkdirs();
            }
            int frameIndex = 0;
            for (File imageFile : imageFiles) {
                if (isValidImageFile(imageFile)) {
                    String extension = getFileExtension(imageFile.getName());
                    String targetFileName = String.format("%s_frame_%03d%s", animationType, frameIndex, extension);
                    File targetFile = new File(animationDir, targetFileName);
                    // Copy file
                    copyFile(imageFile, targetFile);
                    frameIndex++;
                    System.out.println("Imported: " + imageFile.getName() + " -> " + targetFileName);
                }
            }
            return frameIndex > 0; // Return true if at least one file was imported
        } catch (Exception e) {
            System.out.println("Error importing image files: " + e.getMessage());
            return false;
        }
    }
    /**
     * Load animation sequence from directory
     */
    public static AnimationSequence loadAnimationFromDirectory(File animationDir, String animationType, boolean loop) {
        AnimationSequence sequence = new AnimationSequence(animationType, loop);
        try {
            if (!animationDir.exists() || !animationDir.isDirectory()) {
                return sequence;
            }
            // Get all image files and sort them
            File[] imageFiles = animationDir.listFiles((dir, name) -> {
                String lowercaseName = name.toLowerCase();
                for (String format : SUPPORTED_FORMATS) {
                    if (lowercaseName.endsWith(format)) {
                        return true;
                    }
                }
                return false;
            });
            if (imageFiles != null) {
                // Sort files by name to ensure correct frame order
                java.util.Arrays.sort(imageFiles);
                for (File imageFile : imageFiles) {
                    ImageIcon image = loadAndScaleImagePreserveAspect(imageFile, 256); // Use larger max size
                    if (image != null) {
                        AnimationFrame frame = new AnimationFrame(image, imageFile.getAbsolutePath(), 150); // Default duration
                        sequence.addFrame(frame);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading animation from directory: " + e.getMessage());
        }
        return sequence;
    }
    /**
     * Save character set metadata to file
     */
    public static boolean saveCharacterSetMetadata(CharacterSet characterSet) {
        try {
            File setDir = new File(characterSet.getSetPath());
            if (!setDir.exists()) {
                setDir.mkdirs();
            }
            File metadataFile = new File(setDir, "metadata.properties");
            // Create properties content
            StringBuilder content = new StringBuilder();
            content.append("name=").append(characterSet.getName()).append("\n");
            content.append("description=").append(characterSet.getDescription()).append("\n");
            content.append("author=").append(characterSet.getAuthorName()).append("\n");
            content.append("created=").append(System.currentTimeMillis()).append("\n");
            content.append("version=1.0").append("\n");
            // Write to file
            java.nio.file.Files.write(metadataFile.toPath(), content.toString().getBytes());
            return true;
        } catch (Exception e) {
            System.out.println("Error saving character set metadata: " + e.getMessage());
            return false;
        }
    }
    /**
     * Load character set metadata from file
     */
    public static Map<String, String> loadCharacterSetMetadata(File setDirectory) {
        Map<String, String> metadata = new HashMap<>();
        try {
            File metadataFile = new File(setDirectory, "metadata.properties");
            if (metadataFile.exists()) {
                List<String> lines = java.nio.file.Files.readAllLines(metadataFile.toPath());
                for (String line : lines) {
                    if (line.contains("=")) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            metadata.put(parts[0].trim(), parts[1].trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading character set metadata: " + e.getMessage());
        }
        return metadata;
    }
    /**
     * Validate image file
     */
    public static boolean isValidImageFile(File file) {
        if (!file.exists() || !file.isFile()) {
            return false;
        }
        // Check file size
        if (file.length() > MAX_FILE_SIZE) {
            System.out.println("File too large: " + file.getName());
            return false;
        }
        // Check file extension
        String fileName = file.getName().toLowerCase();
        for (String format : SUPPORTED_FORMATS) {
            if (fileName.endsWith(format)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Get file extension
     */
    public static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }
    /**
     * Copy file from source to destination
     */
    public static void copyFile(File source, File destination) throws Exception {
        java.nio.file.Files.copy(source.toPath(), destination.toPath(), 
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
    /**
     * Load and scale image to specified dimensions
     */
    public static ImageIcon loadAndScaleImage(File imageFile, int width, int height) {
        try {
            ImageIcon originalIcon = new ImageIcon(imageFile.getAbsolutePath());
            if (originalIcon.getImage() != null) {
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + imageFile.getName() + " - " + e.getMessage());
        }
        return null;
    }
    /**
     * Load image with preserved aspect ratio, targeting a specific maximum size
     */
    public static ImageIcon loadAndScaleImagePreserveAspect(File imageFile, int maxSize) {
        try {
            ImageIcon originalIcon = new ImageIcon(imageFile.getAbsolutePath());
            if (originalIcon.getImage() != null) {
                int originalWidth = originalIcon.getIconWidth();
                int originalHeight = originalIcon.getIconHeight();
                // If original image is reasonable size, use it directly (more generous range)
                if (originalWidth <= maxSize && originalHeight <= maxSize && 
                    originalWidth >= maxSize/4 && originalHeight >= maxSize/4) {
                    return originalIcon;
                }
                // Calculate new dimensions preserving aspect ratio
                double aspectRatio = (double) originalWidth / originalHeight;
                int newWidth, newHeight;
                if (originalWidth > originalHeight) {
                    newWidth = maxSize;
                    newHeight = (int) (maxSize / aspectRatio);
                } else {
                    newHeight = maxSize;
                    newWidth = (int) (maxSize * aspectRatio);
                }
                Image scaledImage = originalIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + imageFile.getName() + " - " + e.getMessage());
        }
        return null;
    }
    /**
     * Create directory structure for character set
     */
    public static boolean createCharacterSetDirectoryStructure(String setPath) {
        try {
            // Main set directory
            File setDir = new File(setPath);
            if (!setDir.exists() && !setDir.mkdirs()) {
                return false;
            }
            // Animation subdirectories
            String[] animationTypes = {"idle", "walking", "special", "pain"};
            for (String animationType : animationTypes) {
                File animationDir = new File(setDir, animationType);
                if (!animationDir.exists() && !animationDir.mkdirs()) {
                    return false;
                }
            }
            System.out.println("Created character set directory structure: " + setPath);
            return true;
        } catch (Exception e) {
            System.out.println("Error creating directory structure: " + e.getMessage());
            return false;
        }
    }
    /**
     * Delete character set directory and all contents
     */
    public static boolean deleteCharacterSet(String setPath) {
        try {
            File setDir = new File(setPath);
            return deleteDirectoryRecursively(setDir);
        } catch (Exception e) {
            System.out.println("Error deleting character set: " + e.getMessage());
            return false;
        }
    }
    private static boolean deleteDirectoryRecursively(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectoryRecursively(file);
                }
            }
        }
        return directory.delete();
    }
} 