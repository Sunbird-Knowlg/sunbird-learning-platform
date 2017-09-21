/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import com.google.common.io.Resources;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author feroz
 */
public class CsvParserTest {

    public CsvParserTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    @Test
    public void testParse() throws IOException {
        URL resource = Resources.getResource("sample.csv");
        final CSVParser parser = CSVParser.parse(resource, Charset.defaultCharset(), CSVFormat.EXCEL.withHeader());
        try {
            for (final CSVRecord record : parser) {
                final String string = record.get("name");
                System.out.println(string);
            }
        } finally {
            parser.close();
        }
    }
    
    @Test
    public void testParseMap() throws IOException {
        URL resource = Resources.getResource("sample.csv");
        List<Map<String, String>> data = new ArrayList<>();
        
        final CSVParser parser = CSVParser.parse(resource, Charset.defaultCharset(), CSVFormat.EXCEL.withHeader());
        try {
            Map<String, Integer> header = parser.getHeaderMap();
            int rowNum = 1; // Assuming first row is header
            
            for (final CSVRecord record : parser) {
                Map<String, String> row = new HashMap<>();
                rowNum++; // Next row
                
                try {
                    for (String key : header.keySet()) {
                        String val = record.get(key);
                        row.put(key, val);
                    }
                }
                catch (Exception ex) {
                    // Ignore - rows that fail to parse
                    System.out.println("Failed to parse row: " + rowNum + ", " + record);
                }
                
                
                data.add(row);
            }
            
            System.out.println(data);
        } finally {
            parser.close();
        }
    }
}
