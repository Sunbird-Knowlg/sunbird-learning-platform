/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author feroz
 */
public class CsvParserSource implements Source {
    
    static Logger logger = LogManager.getLogger(CsvParserSource.class);
    private File csvFile = null;
    private String keyColumn = null;
    
    public CsvParserSource(File file, String keyCol) {
        this.csvFile = file;
        this.keyColumn = keyCol;
    }
    
    @Override
    public List<Record> process(ProgressCallback callback) throws Exception {
        return parseData(csvFile, keyColumn, callback);
    }
    
    public List<Record> parseData(File file, String keyCol, ProgressCallback callback) throws IOException {
        
        CSVParser parser = null;
                
        try {
            // Open the file for parsing
            long begin = System.currentTimeMillis();
            logger.info("Parsing CSV file: " + file.getName());
            
            CSVFormat format = CSVFormat.EXCEL.withHeader().withIgnoreSurroundingSpaces().withTrim().withIgnoreEmptyLines();
            
            parser = CSVParser.parse(file, Charset.defaultCharset(), format);
            List<CSVRecord> records = parser.getRecords();
            
            // Prepare the return dataset
            List<Record> data = new ArrayList<>();
            int rowNum = 0; // Assuming first row is header
            int totalRows = records.size();

            // Iterate over parsed records to convert them to a list of maps
            for (final CSVRecord record : records) {
                
                // Next row
                rowNum++; 
                Map<String, String> row = record.toMap();
                
                // Get the primary key column from the CSV data
                String key = row.get(keyCol);
                Record parsedRecord = new Record(key);
                parsedRecord.setCsvData(row);
                data.add(parsedRecord);
                
                callback.progress(totalRows, rowNum);
            }
            
            long end = System.currentTimeMillis();
            logger.info("Read " + data.size() + " records from csv in " + (end - begin) + " ms");
            return data;
            
        } finally {
            if (parser != null) parser.close();
        }
    } 
    
    public Map<String, String> parseLookup(File file) throws IOException {
        CSVParser parser = null;
                
        try {
            // Open the file for parsing
            logger.info("Parsing lookup CSV file: " + file.getName());
            parser = CSVParser.parse(file, Charset.defaultCharset(), CSVFormat.EXCEL.withHeader());
            
            // Prepare the return dataset
            Map<String, String> lookupData = new HashMap<>();
            int rowNum = 1; // Assuming first row is header
            
            // Iterate over parsed records to convert them to a list of maps
            for (final CSVRecord record : parser) {
                
                // Next row
                rowNum++; 
                Map<String, String> row = new HashMap<>();
                
                try {
                    String key = record.get(0);
                    String val = record.get(1);
                    
                    // Add to lookup table only if both key and value are non blank
                    if (StringUtils.isNoneBlank(key, val)) {
                        lookupData.put(key, val);
                    }
                }
                catch (Exception ex) {
                    // Ignore - rows that fail to parse because of incorrect column mappings
                    logger.warn("Failed to parse row: " + rowNum + ", " + record);
                }
            }
            
            logger.info("Read " + lookupData.size() + " records from csv");
            return lookupData;
            
        } finally {
            if (parser != null) parser.close();
        }
    }

    
}
