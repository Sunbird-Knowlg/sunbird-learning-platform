/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sunbird.common.optimizr.image;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.sunbird.common.optimizr.FileUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;

/**
 *
 * @author feroz
 */
public class ResizeImagemagickProcessor extends ImageProcessor {

	
	public File process(File file, double targetResolution, int width, int height, String outputFileNameSuffix){

        try {
            // File names
            String inputFileName = file.getAbsolutePath();
            String outputFileName = file.getAbsolutePath().replaceAll("\\.", "\\."+outputFileNameSuffix+"\\.");
            outputFileName = FilenameUtils.getName(outputFileName);

            // set optimize width and height
            int ow = width;
            int oh = height;
            
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(inputFileName);
            op.resize(ow, oh);
            if((int)targetResolution > 0)
            	op.resample((int)targetResolution);
            op.addImage(outputFileName);

            // execute the operation
            cmd.run(op);
            
            // replace the file
            if(outputFileNameSuffix.equalsIgnoreCase("out"))
            	FileUtils.replace(file, new File(outputFileName));
            else{
            	file = new File(outputFileName);
            }
            	
            
            return file;
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
	
    @Override
    public File process(File file) {
        try {
	    	String inputFileName = file.getAbsolutePath();
	        Info imageInfo = new Info(inputFileName,false);
	        // Find the image size and resolution
	        int width = imageInfo.getImageWidth();
	        int height = imageInfo.getImageHeight();
	        
	        String resString = imageInfo.getProperty("Resolution");
	        double xresd = 150; // Assume default 150 ppi
            if (resString != null) {
                String res[] = resString.split("x");
                String xres = (res.length > 0 ? res[0] : "150");
                xresd = Double.parseDouble(xres);
            }
            
            // Resize 50%
            int ow = width/2;
            int oh = height/2;
	        
	        // Target resolution - reduce to half
            double targetResolution = xresd/2;

            return process(file, targetResolution, ow, oh, "out");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

		return null;
    }

}
