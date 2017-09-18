/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import org.ekstep.tools.loader.utils.JsonUtil;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author feroz
 */
public class JsonMergeProcessor implements Processor {
    static Logger logger = LogManager.getLogger(JsonMergeProcessor.class);
    private LinkedListMultimap<String, Record> mergeTable = null;
    

    @Override
    public List<Record> process(List<Record> data, ProgressCallback callback) throws Exception {
        return merge(data, callback);
    }
    
    public List<Record> merge(List<Record> raw, ProgressCallback callback) {
        long begin = System.currentTimeMillis();
        
        String lastKey = "";
        mergeTable = LinkedListMultimap.create();
        
        // Pass 1 - Collect all the elements of the same key 
        for (Record record : raw) {
            String rowKey = record.getKey();
            
            // When the row doesn't have a key, assume it is the last key
            if (rowKey.isEmpty()) {
                rowKey = lastKey;
            }
            // When the row key is different from last key, insert
            else if (!record.getKey().equalsIgnoreCase(lastKey)) {
                lastKey = rowKey;
            }
            
            mergeTable.put(rowKey, record);
        }
        
        // Pass 2 - Merge the records into one
        List<Record> output = new ArrayList<>();
        int totalRows = mergeTable.keySet().size();
        int rowNum = 0;
        
        for (String key : mergeTable.keySet()) {
            rowNum++;
            
            List<Record> mergeableRecords = mergeTable.get(key);
            Record mergedRecord = mergeRow(mergeableRecords);
            if (mergedRecord != null) output.add(mergedRecord);
            
            callback.progress(totalRows, rowNum);
        }
        
        long end = System.currentTimeMillis();
        logger.info("Merge completed, processed " + totalRows + " records in " + (end - begin) + " ms");
        return output;
    }
    
    public Record mergeRow(List<Record> mergeableRecords) {
        
        // It shouldn't happen, but just in case
        if (mergeableRecords.isEmpty()) return null;
        
        // If there is only one record, nothing to merge
        if (mergeableRecords.size() == 1) return mergeableRecords.get(0);
        
        // There are more than one records. Merge all subsequent ones into the first record
        Record merged = mergeableRecords.remove(0);
        JsonObject target = merged.getJsonData();
        
        for (Record mergeableRecord : mergeableRecords) {
            JsonObject source = mergeableRecord.getJsonData();
            JsonUtil.deepMerge(source, target, JsonUtil.NullStrategy.Merge);
        }
        
        // All subsequent entries are merged into the first record
        return merged;
    }

}
