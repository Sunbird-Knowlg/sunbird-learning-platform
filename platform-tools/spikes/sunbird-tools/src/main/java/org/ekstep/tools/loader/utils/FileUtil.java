/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.utils;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author feroz
 */
public class FileUtil {
    
    public static boolean isLocal(String url) {
        return !isRemote(url);
    }
    
    public static boolean isRemote(String url) {
        return url.startsWith("http");
    }
    
    public static boolean isSecure(String url) {
        return url.startsWith("https");
    }
    
    public static String getFileName(String url) throws MalformedURLException {
        if(isLocal(url)) {
            File file = new File(url);
            return file.getName();
        }
        else {
            URL httpUrl = new URL(url);
            return httpUrl.getFile();
        }
    }
    
    public static boolean existsLocally(String url) {
        
        if(isLocal(url)) {
            File file = new File(url);
            return file.exists();
        }
        
        return false;
    }

    public static URL getURL(String url) throws MalformedURLException {
        if (isLocal(url)) {
            File file = new File(url);
            return file.toURI().toURL();
        }
        else {
            URL httpUrl = new URL(url);
            return httpUrl;
        }
    }
    
    public static String upload(String url, String destination) throws Exception {
        
        if (existsLocally(url)) {
            HttpResponse<JsonNode> jsonResponse = Unirest.post(destination).field("file", new File(url)).asJson();
            String uploadedUrl = jsonResponse.getBody().getObject().getString("fileUrl");
            return uploadedUrl;
        }
        else {
            HttpResponse<JsonNode> jsonResponse = Unirest.post(destination).field("fileUrl", url).asJson();
            String uploadedUrl = jsonResponse.getBody().getObject().getString("fileUrl");
            return uploadedUrl;
        }
    }
    
    public File download(String url, String folder) {
        // Not implemented
        return null;
    }
}
