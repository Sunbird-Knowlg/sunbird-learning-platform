/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

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
		Unirest.clearDefaultHeaders();
        if (existsLocally(url)) {
			HttpResponse<JsonNode> jsonResponse = Unirest.put(destination).field("file", new File(url)).asJson();
			String uploadUrl = null;
			if (200 == jsonResponse.getStatus()) {
				uploadUrl = destination.split("\\?")[0];
			}
			return uploadUrl;
        }
        else {
			HttpResponse<JsonNode> jsonResponse = Unirest.put(destination).field("fileUrl", url).asJson();
			String uploadedUrl = null;
			if (200 == jsonResponse.getStatus()) {
				uploadedUrl = destination.split("\\?")[0];
			}
			return uploadedUrl;
        }
    }
    
    public File download(String url, String folder) {
        // Not implemented
        return null;
    }
}
