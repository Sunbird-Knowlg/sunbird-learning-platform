/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.ecml.optimizr;

import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author feroz
 */
public class ThumbnailCLI {
    private static final Logger logger = LogManager.getLogger();
    private static String input = "samples/thumbnails";
    
    public static void main( String[] args ) throws Exception {
        long beg = System.currentTimeMillis();
        File file = new File(input);
        int count = ThumbnailGenerator.process(file);
        long end = System.currentTimeMillis();
        
        logger.info("Generated {} thumbnails in {} ms", count, (end - beg));
    }
    
    
}
