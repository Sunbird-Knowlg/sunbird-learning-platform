/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import com.google.gson.JsonObject;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author feroz
 */
public class SysoutDestination implements Destination {
    
    static Logger logger = LogManager.getLogger(SysoutDestination.class);
    
    @Override
    public void process(List<Record> data, ProgressCallback callback) throws Exception {
        
        int totalRows = data.size();
        int rowNum = 0;
        
        System.out.println();
        for (Record record : data) {
            rowNum++;
            print(record);
            callback.progress(totalRows, rowNum);
        }
        System.out.println();
        
        logger.info("Dry-run completed with " + totalRows + " records.");
    }
    
    private void print(Record record) {
        JsonObject jsonObj = record.getJsonData();
        System.out.println(jsonObj);
    }
}
