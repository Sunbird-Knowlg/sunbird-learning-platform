package org.sunbird.common.optimizr.image;

import java.io.File;

import org.im4java.core.Info;

public class ImageResolutionUtil {

	public static boolean isImageOptimizable(File file, int dimentionX, int dimentionY) throws Exception{
		
        // File names
        String inputFileName = file.getAbsolutePath();
        
        Info imageInfo = new Info(inputFileName,false);
        // Find the image size and resolution
        int width = imageInfo.getImageWidth();
        int height = imageInfo.getImageHeight();
        
        if(dimentionX < width && dimentionY < height){
        	return true;
        }

		return false;
	}
	
	public static double getOptimalDPI(File file, int dpi) throws Exception{
		
        // File names
        String inputFileName = file.getAbsolutePath();
        
        Info imageInfo = new Info(inputFileName,false);
        String resString = imageInfo.getProperty("Resolution");
        if (resString != null) {
            String res[] = resString.split("x");
            if(res.length > 0){
            	double xresd = Double.parseDouble(res[0]);
            	if ( xresd < (double)dpi) {
            		return xresd; 
            	} else {
            		return (double)dpi;
            	}
            }
        }
        return (double)0;
	}
}
