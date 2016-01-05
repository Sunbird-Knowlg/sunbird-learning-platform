package com.ilimi.taxonomy.util;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.google.gson.*;

@Component
public class ContentBundle {
	
	private static final String bucketName = "ekstep-public";
    private static final String ecarFolderName = "ecar_files";
    
    
    protected static final String URL_FIELD = "URL";
    protected static final String BUNDLE_PATH = "/data/contentBundle";
    protected static final String BUNDLE_MANIFEST_FILE_NAME = "manifest.json";
    
	@Async
    public void asyncCreateContentBundle(List<Map<String, Object>> contents, String bundleFileName) {
    	System.out.println("Async method invoked....");
    	List<String> urlFields = new ArrayList<String>();
    	urlFields.add("appIcon");
    	urlFields.add("grayScaleAppIcon");
    	urlFields.add("posterImage");
    	urlFields.add("downloadUrl");
    	Map<String, String> downloadUrls = new HashMap<String, String>();
        for (Map<String, Object> content : contents) {
        	for (Map.Entry<String, Object> entry : content.entrySet()) {
        	    if (urlFields.contains(entry.getKey())) {
        	    	downloadUrls.put(entry.getValue().toString(), (String) content.get("identifier"));
        	    	entry.setValue(entry.getValue().toString().substring(entry.getValue().toString().lastIndexOf('/') + 1));
        	    }
        	}
        }
        List<File> downloadedFiles = getContentBundle(downloadUrls);
        Gson gs = new Gson();
        String header = "{ \"id\": \"ekstep.content.archive\", \"ver\": \"1.0\", \"ts\": \"" + getResponseTimestamp() + "\", \"params\": { \"resmsgid\": \"" + getUUID() + "\"}, \"archive\": { \"count\": " + contents.size() + ", \"ttl\": 24, \"items\": ";
        String manifestoJSON = header + gs.toJson(contents) + "}}";
        File manifestFile = createManifestFile(manifestoJSON);
        if (null != manifestFile) {
        	downloadedFiles.add(manifestFile);
        }
        if (null != downloadedFiles) {
        	try {
        		FileOutputStream stream = new FileOutputStream(bundleFileName);
        		stream.write(createECAR(downloadedFiles));
        		stream.close();
        		File contentBundle = new File(bundleFileName);
        		String[] url =  AWSUploader.uploadFile(bucketName, ecarFolderName, contentBundle);
        		System.out.println("AWS Upload is complete.... on URL : " + url.toString());
        	} catch(Exception e) {
        		e.printStackTrace();
        	} finally {
        		HttpDownloadUtility.DeleteFiles(downloadedFiles);
        	}
        }
    }
	
	private List<File> getContentBundle(final Map<String, String> downloadUrls) {
    	try {
    		ExecutorService pool = Executors.newFixedThreadPool(10);
            List<Callable<File>> tasks = new ArrayList<Callable<File>>(downloadUrls.size());
            List<File> files = new ArrayList<File>();
            for (final String url : downloadUrls.keySet()) {
                tasks.add(new Callable<File>() {
                    public File call() throws Exception {
                    	createDirectoryIfNeeded(BUNDLE_PATH + File.separator + downloadUrls.get(url));
                        return HttpDownloadUtility.downloadFile(url, BUNDLE_PATH + File.separator + downloadUrls.get(url));
                    }
                });
            }
            List<Future<File>> results = pool.invokeAll(tasks);
            for (Future<File> ff : results) {
                files.add(ff.get());
            }
            pool.shutdown();
            return files;
    	} catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
		return null;
    }
    
    private byte[] createECAR(List<File> files) throws IOException{
        //creating byteArray stream, make it bufforable and passing this buffor to ZipOutputStream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);

        //packing files
        for (File file : files) {
        	String fileName = null;
        	if (file.getName().toLowerCase().endsWith("manifest.json")) {
        		fileName = file.getName();
        	} else {
        		fileName = file.getParent().substring(file.getParent().lastIndexOf(File.separator) + 1) + File.separator + file.getName();
        	}
        		
            //new zip entry and copying inputstream with file to zipOutputStream, after all closing streams
            zipOutputStream.putNextEntry(new ZipEntry(fileName));
            FileInputStream fileInputStream = new FileInputStream(file);

            IOUtils.copy(fileInputStream, zipOutputStream);

            fileInputStream.close();
            zipOutputStream.closeEntry();
        }

        if (zipOutputStream != null) {
            zipOutputStream.finish();
            zipOutputStream.flush();
            IOUtils.closeQuietly(zipOutputStream);
        }
        IOUtils.closeQuietly(bufferedOutputStream);
        IOUtils.closeQuietly(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
    
    private File createManifestFile(String manifestContent) {
    	BufferedWriter writer = null;
    	try {
    	    writer = new BufferedWriter(new FileWriter(BUNDLE_PATH + File.separator + BUNDLE_MANIFEST_FILE_NAME));
    	    writer.write(manifestContent);
    	    return new File(BUNDLE_PATH + File.separator + BUNDLE_MANIFEST_FILE_NAME);
    	} catch (IOException ioe) {
    		return null;
    	}
    	finally {
    	    try {
    	        if ( writer != null)
    	        writer.close( );
    	    } catch (IOException ioe) {}
    	}
    }
    
    private void createDirectoryIfNeeded(String directoryName)
    {
    	File theDir = new File(directoryName);
    	// if the directory does not exist, create it
    	if (!theDir.exists()) {
    		System.out.println("creating directory: " + directoryName);
    		theDir.mkdir();
    	}
    }
    
    private String getResponseTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'XXX");
        return sdf.format(new Date());
    }

    private String getUUID() {
        UUID uid = UUID.randomUUID();
        return uid.toString();
    }
}