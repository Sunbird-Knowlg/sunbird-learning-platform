/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sunbird.common.optimizr.image;

import java.io.File;

import org.sunbird.common.optimizr.FileType;
import org.sunbird.common.optimizr.Processor;

/**
 *
 * @author feroz
 */
public abstract class ImageProcessor implements Processor {

    public boolean isApplicable(FileType type) {
        return (type == FileType.Image);
    }

    public abstract File process(File file);
}
