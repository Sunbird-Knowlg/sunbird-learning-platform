package org.ekstep.ecml.optimizr;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.ecml.optimizr.audio.MonoChannelProcessor;
import org.ekstep.ecml.optimizr.image.ResizeImagemagickProcessor;

import com.ilimi.assessment.controller.AssessmentItemController;
import com.ilimi.taxonomy.util.HttpDownloadUtility;

public class Optimizr {

	private static Logger LOGGER = LogManager.getLogger(Optimizr.class.getName());
	
	private static final String tempFileLocation = "/data/contentBundle/";
	
	public static void main(String ap[]) throws Exception{
		Optimizr optimizr=new Optimizr();
		File output = optimizr.optimizeECAR("https://ekstep-public.s3-ap-southeast-1.amazonaws.com/ecar_files/org.ekstep.story.hi.elephant_1458713044510.ecar");
		System.out.println(output.getCanonicalPath());
	}
	
	public File optimizeECAR(String url)  throws Exception{
		LOGGER.info("optimizeECAR URL"+url);
		String tempFileDwn = tempFileLocation + System.currentTimeMillis() + "_temp";
		File ecarFile = HttpDownloadUtility.downloadFile(url, tempFileDwn);
		LOGGER.info("optimizeECAR ecarFile -"+ecarFile.getPath());
		return optimizeECAR(ecarFile);
	}
	
	private File optimizeECAR(File input) throws Exception{
		String inputFieName = FilenameUtils.removeExtension(input.getName());
		String temp = input.getParent() + File.separator + inputFieName;
		File tempDir = new File(temp);
		if(!tempDir.exists())
			tempDir.mkdir();
		FileUtils.extract(input, temp);
		optimzeZip(tempDir);
		
		String outputFileName = inputFieName + ".min";
		String output = input.getParent() + File.separator + outputFileName + ".ecar";
		FileUtils.compress(output, temp);
		input.delete();
		delete(tempDir);
		return new File(output);
	}
	
    public void optimzeZip(File dir) throws Exception {
    	if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (!files[i].isDirectory()) {
                	if(FilenameUtils.getExtension(files[i].getName()).equalsIgnoreCase("zip")){
                		optimize(files[i]);
                	}
                }else{
                	optimzeZip(files[i]);
                    continue;
                }
            }            
        }
    }
    
    private void optimize(File zipFile) throws Exception{
        
		String inputFieName = FilenameUtils.removeExtension(zipFile.getName());
		String temp = zipFile.getParent() + File.separator + inputFieName;
        File tempDir = new File(temp);
        if (!tempDir.exists())
        	tempDir.mkdir();
        
        Statistics stats = new Statistics();
        stats.start(zipFile.length());
        
        FileUtils.extract(zipFile, tempDir.getPath());
        
        RecursiveProcessor recusriveProc = new RecursiveProcessor(stats);
        recusriveProc.addProcessor(new MonoChannelProcessor());
        recusriveProc.addProcessor(new ResizeImagemagickProcessor());
        //recusriveProc.addProcessor(new ResolutionProcessor());
        
        recusriveProc.process(tempDir);
        
        FileUtils.compress(zipFile.getPath(), tempDir.getPath());
        stats.end(zipFile.length());
        
        stats.print();
        delete(tempDir);
    }
	
    
    public void delete(File file) throws IOException {
        if (file.isDirectory()) {
            // directory is empty, then delete it
            if (file.list().length == 0) {
                file.delete();
            } else {
                // list all the directory contents
                String files[] = file.list();
                for (String temp : files) {
                    // construct the file structure
                    File fileDelete = new File(file, temp);
                    // recursive delete
                    delete(fileDelete);
                }
                // check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                }
            }

        } else {
            // if file, then delete it
            file.delete();
        }
    }
}
