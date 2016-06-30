/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.ecml.optimizr.audio;

import java.io.File;
import org.ekstep.ecml.optimizr.FileType;
import org.ekstep.ecml.optimizr.Processor;

/**
 *
 * @author feroz
 */
public abstract class AudioProcessor implements Processor {
    public boolean isApplicable(FileType type) {
        return (type == FileType.Audio);
    }

    public abstract File process(File file);
}
