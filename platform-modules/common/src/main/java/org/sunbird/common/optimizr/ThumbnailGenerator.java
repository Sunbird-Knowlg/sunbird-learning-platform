package org.sunbird.common.optimizr;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.sunbird.telemetry.logger.TelemetryManager;
import org.imgscalr.Scalr;

import org.sunbird.common.Platform;

/**
 * Utility class to generate thumbnails from a given image. Provides overloaded
 * methods to work with File objects, or take an input file name and returns the
 * generated thumbnail file. These are meant to simplify the caller cycles.
 *
 * @author feroz
 */
public class ThumbnailGenerator {

    // Constant - thumbnail size (150px in max dimension)
    private static final int THUMBNAIL_SIZE = Platform.config.hasPath("max.thumbnail.size.pixels")?
            Platform.config.getInt("max.thumbnail.size.pixels"): 56;

    /**
     * Recursively traverses the given directory and generates thumbnails for all
     * the images in the directory. Returns the count of thumbnails generated. 
     * 
     * @param dir Directory to traverse
     * @return Number of thumbnails successfully generated
     */
    public static int process(File dir) {
        int count = 0;
        if (dir.isDirectory()) {
            
            // Check the files in the directory
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                
                // Recurse in nested subfolders
                if (files[i].isDirectory()) {
                    int rcount = process(files[i]);
                    count += rcount;
                    continue;
                }
                
                // Check the file type before generating thumbnail
                File file = files[i];
                FileType type = FileUtils.getFileType(file);
                
                // Generate thumbnail for images
                if (type == FileType.Image) {
                    
                    // Skip other thumbnails in the same folder
                    if (! file.getName().contains(".thumb.")) {
                        boolean success = ThumbnailGenerator.generate(file);
                        if (success) count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    /**
     * Takes a file name as input and generates the thumbnail. The thumbnail is
     * generated in the same location as the input file, with the extension
     * .thumb.xxx (e.g. image logo.png will generate logo.thumb.png)
     *
     * @param inFile Input file name
     * @return True if thumbnail is successfully generated
     */
    public static boolean generate(String inFile) {
        if (inFile == null) {
            TelemetryManager.log("Input for thumbnail generation is null");
            return false;
        }

        try {
            File file = new File(inFile);
            boolean success = generate(file);
            return success;
        } catch (Exception ex) {
            TelemetryManager.error("Failed to generate thumbnail for " + inFile+ " :: Error message:" + ex.getMessage());
            return false;
        }
    }
    
    /**
     * Takes an input file and generates the thumbnail for this. The thumbnail
     * is generated in the same location as the input file, with the extension
     * .thumb.xxx (e.g. image logo.png will generate logo.thumb.png)
     *
     * @param inFile Input file
     * @return True is thumbnail is successfully generated
     */
    public static boolean generate(File inFile) {
        if (inFile == null) {
            TelemetryManager.log("Input for thumbnail generation is null");
            return false;
        }

        try {
            String thumbFile = FileUtils.getThumbnailFileName(inFile);
            File outFile = new File(thumbFile);
            boolean success = generate(inFile, outFile);
            return success;
        } catch (Exception ex) {
            TelemetryManager.log("Failed to generate thumbnail for " + inFile.getName() + " :: Error message: "+ ex.getMessage());
            return false;
        }
    }

    /**
     * Provides a utility method to generate the thumbnail for a given input
     * Returns true if the thumbnail was generated successfully, and false if it
     * fails to generate the thumbnail. This allows the caller code to remain
     * clean.
     *
     * @param inFile Input image file
     * @param outFile Thumbnail that is generated
     * @return True if thumbnail is generated successfully, false otherwise
     */
    public static boolean generate(File inFile, File outFile) {
        
        boolean done = false;
        
        if ((inFile == null) || (outFile == null)) {
            TelemetryManager.log("Input for thumbnail generation is null");
            done = false;
        } else {
            try {
                BufferedImage srcImage = ImageIO.read(inFile); // Load image

                int width = srcImage.getWidth();
                int height = srcImage.getHeight();

                // Scale the image only if it is bigger than thumbnail
                if ((height > THUMBNAIL_SIZE) || (width > THUMBNAIL_SIZE)) {
                    BufferedImage scaledImage = Scalr.resize(srcImage, THUMBNAIL_SIZE); // Scale image
                    ImageIO.write(scaledImage, "png", outFile);
                    done = true;
                }
                else {
                    // Image is smaller than thumbnail, no need to scale
                    ImageIO.write(srcImage, "png", outFile);
                    done = false;
                }
            } catch (Exception ex) {
                TelemetryManager.log("Failed to generate thumbnail for " + inFile.getName() + " Error message: " + ex.getMessage());
                done = false;
            }
        }
        
        return done;
    }
}
