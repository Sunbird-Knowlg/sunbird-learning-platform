/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import java.io.File;
import java.net.URL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author feroz
 */
public class BulkLoaderServiceTest {
    
    public BulkLoaderServiceTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    public File getFile(String name) throws Exception {
        URL url = this.getClass().getResource(name);
        File file = new File(url.toURI());
        return file;
    }
    
    @Test
    public void simpleTest() throws Exception {
        File csvData = getFile("/sample.csv");
        File transformation = getFile("/sample.twig");
        String keyColumn = "code";
        String userID = "feroz";
        
        BulkLoaderService svc = new BulkLoaderService();
        svc.setCsvFile(csvData);
        svc.setTfmFile(transformation);
        svc.setKeyColumn(keyColumn);
        svc.setUserID(userID);
        
        svc.execute(svc);
        
    }
}
