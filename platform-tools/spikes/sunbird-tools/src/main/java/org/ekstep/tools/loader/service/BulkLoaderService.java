/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import java.io.File;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author feroz
 */
public class BulkLoaderService implements ProgressCallback {
    static Logger logger = LogManager.getLogger(BulkLoaderService.class);
    
    private File csvFile;
    private File tfmFile;
    private String keyColumn;
    private List<String> lookupFiles;
    private String userID;

    /**
     * @return the csvFile
     */
    public File getCsvFile() {
        return csvFile;
    }

    /**
     * @param csvFile the csvFile to set
     */
    public void setCsvFile(File csvFile) {
        this.csvFile = csvFile;
    }

    
    /**
     * @return the tfmFile
     */
    public File getTfmFile() {
        return tfmFile;
    }

    /**
     * @param tfmFile the tfmFile to set
     */
    public void setTfmFile(File tfmFile) {
        this.tfmFile = tfmFile;
    }
    
    /**
     * @return the keyColumn
     */
    public String getKeyColumn() {
        return keyColumn;
    }

    /**
     * @param keyColumn the keyColumn to set
     */
    public void setKeyColumn(String keyColumn) {
        this.keyColumn = keyColumn;
    }

    /**
     * @return the userID
     */
    public String getUserID() {
        return userID;
    }

    /**
     * @param userID the userID to set
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }
    
    public void execute(ProgressCallback callback) throws Exception {
        Source source = new CsvParserSource(csvFile, keyColumn);
        Processor mapper = new JsonMapperProcessor(getTfmFile());
        Processor merger = new JsonMergeProcessor();
        Destination destination = new SysoutDestination();
        
        logger.info("Step 1 of 4 - Reading input data");
        List<Record> data = source.process(callback);
        
        logger.info("Step 2 of 4 - Transforming input data");
        data = mapper.process(data, callback);
        
        logger.info("Step 3 of 4 - Merging input data rows");
        data = merger.process(data, callback);
        
        logger.info("Step 4 of 4 - Loading the transformed data");
        destination.process(data, callback);
    }

    @Override
    public void progress(int totalSteps, int completedSteps) {
        if (completedSteps % 2 == 0) {
            if (completedSteps > totalSteps) completedSteps = totalSteps;
            double progress = (double) completedSteps / (double) totalSteps * 100d;
            String progStr = String.format("%.2f",progress);
            logger.info("      Completed " + progStr + " %");
        }
    }
}
