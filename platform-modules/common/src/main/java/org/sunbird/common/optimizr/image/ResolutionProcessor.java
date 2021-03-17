/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sunbird.common.optimizr.image;

import java.io.File;

import org.sunbird.common.optimizr.FileUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;

/**
 *
 * @author feroz
 */
public class ResolutionProcessor extends ImageProcessor {

    @Override
    public File process(File file) {

        try {
            // File names
            String inputFileName = file.getAbsolutePath();
            String outputFileName = FileUtils.getOutputFileName(file);

            // Find the image resolution
            Info imageInfo = new Info(inputFileName,false);
            String resString = imageInfo.getProperty("Resolution");
            double xresd = 150; // Assume default 150 ppi
            if (resString != null) {
                String res[] = resString.split("x");
                String xres = (res.length > 0 ? res[0] : "150");
                xresd = Double.parseDouble(xres);
            }

            // Target - reduce to half
            double targetResolution = xresd/2;
            
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(inputFileName);
            op.resample((int)targetResolution);
            op.addImage(outputFileName);

            // execute the operation
            cmd.run(op);

            // replace the file
            FileUtils.replace(file, new File(outputFileName));
            return file;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
