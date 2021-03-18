/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sunbird.common.optimizr.video;

import java.io.File;

import org.sunbird.common.optimizr.FileType;
import org.sunbird.common.optimizr.Processor;

/**
 *
 * @author feroz
 */
public abstract class VideoProcessor implements Processor {
    public boolean isApplicable(FileType type) {
        return (type == FileType.Video);
    }

    public abstract File process(File file);
}
