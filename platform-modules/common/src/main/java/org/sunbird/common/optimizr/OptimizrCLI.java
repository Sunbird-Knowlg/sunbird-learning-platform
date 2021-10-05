package org.sunbird.common.optimizr;

import java.io.File;

import org.sunbird.common.optimizr.audio.MonoChannelProcessor;
import org.sunbird.common.optimizr.image.ResizeImagemagickProcessor;



/**
 * Hello world!
 *
 */
public class OptimizrCLI 
{
    private static String input = "samples/input/org.sunbird.en.elephant.zip";
    private static String output = "samples/output/org.sunbird.en.elephant.zip";
    private static String workDir = "samples/work";
       
/*    public void optimize(String zipFile) throws Exception{
        
    	String inputName=FilenameUtils.removeExtension(Paths.get(zipFile).getFileName().toString());
    	
        File wd = new File(workDir + File.separator + inputName);
        if (!wd.exists())
        	wd.mkdir();
        
        Statistics stats = new Statistics();
        stats.start(new File(zipFile).length());
        
        FileUtils.extract(zipFile, wd.getPath());
        
        RecursiveProcessor recusriveProc = new RecursiveProcessor(stats);
        recusriveProc.addProcessor(new MonoChannelProcessor());
        recusriveProc.addProcessor(new ResizeImagemagickProcessor());
        //recusriveProc.addProcessor(new ResolutionProcessor());
        
        recusriveProc.process(wd);
        
        FileUtils.compress(zipFile, wd.getPath());
        stats.end(new File(zipFile).length());
        
        stats.print();
        wd.delete();
    }*/
    
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
