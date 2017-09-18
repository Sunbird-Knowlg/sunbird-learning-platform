/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import java.io.File;
import java.net.URL;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author feroz
 */
public class FileUtilsTest {
    
    public FileUtilsTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testLocal() throws Exception {
        String fileName = "sample.csv";
        File file = new File(fileName);
        if (file.exists()) System.out.println("Exists");
        else System.out.println("Does not exist");
        
        URL url = file.toURI().toURL();
        String protocol = url.getProtocol();
        System.out.println(protocol);
        
        String url1 = "https://google.com";
        URL url2 = new URL(url1);
        protocol = url2.getProtocol();
        System.out.println(protocol);
        
        file = new File("https://google.com");
        url = file.toURI().toURL();
        protocol = url.getProtocol();
        System.out.println("-> " + protocol);
        
        String url3 = "sample.csv";
        url2 = new URL(url3);
        protocol = url2.getProtocol();
        System.out.println(protocol);
        
    }
}
