/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.common.optimizr;

import java.io.File;

/**
 *
 * @author feroz
 */
public interface Processor {
    public boolean isApplicable(FileType type);
    public File process(File file);
}
