package org.ekstep.ecml.optimizr;

import java.io.File;
import org.ekstep.ecml.optimizr.audio.MonoChannelProcessor;
import org.ekstep.ecml.optimizr.image.ResizeImagemagickProcessor;



/**
 * Hello world!
 *
 */
public class OptimizrCLI 
{
    private static String input = "samples/input/assets.zip";
    private static String output = "samples/output/assets.zip";
    private static String workDir = "samples/work";
    
    public static void main( String[] args ) throws Exception
    {
        // Delete workDir if exists
        File wd = new File(workDir);
        if (wd.exists()) wd.delete();
        
        Statistics stats = new Statistics();
        stats.start(new File(input).length());
        
        FileUtils.extract(input, workDir);
        
        RecursiveProcessor recusriveProc = new RecursiveProcessor(stats);
        recusriveProc.addProcessor(new MonoChannelProcessor());
        recusriveProc.addProcessor(new ResizeImagemagickProcessor());
        //recusriveProc.addProcessor(new ResolutionProcessor());
        
        recusriveProc.process(new File(workDir));
        
        FileUtils.compress(output, workDir);
        stats.end(new File(output).length());
        
        stats.print();
        
    }
}
