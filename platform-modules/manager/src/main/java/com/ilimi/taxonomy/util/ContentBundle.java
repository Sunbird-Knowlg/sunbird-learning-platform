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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.UnzipUtility;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.ilimi.common.exception.ServerException;
import com.ilimi.taxonomy.enums.ContentErrorCodes;

@Component
public class ContentBundle {

    private static final String bucketName = "ekstep-public";
    private static final String ecarFolderName = "ecar_files";

    private ObjectMapper mapper = new ObjectMapper();

    protected static final String URL_FIELD = "URL";
    protected static final String BUNDLE_PATH = "/data/contentBundle";
    protected static final String BUNDLE_MANIFEST_FILE_NAME = "manifest.json";

    @Async
    public void asyncCreateContentBundle(List<Map<String, Object>> contents, List<String> children, String fileName,
            String version) {
        try {
            System.out.println("Async method invoked....");
            createContentBundle(contents, children, fileName, version);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] createContentBundle(List<Map<String, Object>> contents, List<String> children, String fileName,
            String version) {
        String bundleFileName = BUNDLE_PATH + File.separator + fileName;
        List<String> urlFields = new ArrayList<String>();
        urlFields.add("appIcon");
        urlFields.add("grayScaleAppIcon");
        urlFields.add("posterImage");
        Map<Object, List<String>> downloadUrls = new HashMap<Object, List<String>>();
        for (Map<String, Object> content : contents) {
            String identifier = (String) content.get("identifier");
            if (children.contains(identifier))
                content.put("visibility", "Parent");
            urlFields.add("artifactUrl");
            for (Map.Entry<String, Object> entry : content.entrySet()) {
                if (urlFields.contains(entry.getKey())) {
                    Object val = entry.getValue();
                    if (val instanceof File) {
                        File file = (File) val;
                        addDownloadUrl(downloadUrls, val, identifier);
                        entry.setValue(identifier.trim() + File.separator + file.getName());
                    } else if (HttpDownloadUtility.isValidUrl(val)) {
                        addDownloadUrl(downloadUrls, val, identifier);
                        String file = entry.getValue().toString()
                                .substring(entry.getValue().toString().lastIndexOf('/') + 1);
                        if (file.endsWith(".ecar")) {
                            entry.setValue(identifier.trim() + File.separator + identifier.trim() + ".zip");
                        } else {
                            entry.setValue(identifier.trim() + File.separator + Slug.makeSlug(file, true));
                        }
                    }
                }
            }
            Object artifactUrl = content.get("artifactUrl");
            content.put("downloadUrl", artifactUrl);
        }
        String bundlePath = BUNDLE_PATH + File.separator + System.currentTimeMillis() + "_temp";
        List<File> downloadedFiles = getContentBundle(downloadUrls, bundlePath);
        try {
            if (StringUtils.isBlank(version))
                version = "1.0";
            String header = "{ \"id\": \"ekstep.content.archive\", \"ver\": \"" + version + "\", \"ts\": \""
                    + getResponseTimestamp() + "\", \"params\": { \"resmsgid\": \"" + getUUID()
                    + "\"}, \"archive\": { \"count\": " + contents.size() + ", \"ttl\": 24, \"items\": ";
            String manifestJSON = header + mapper.writeValueAsString(contents) + "}}";
            File manifestFile = createManifestFile(manifestJSON, bundlePath);
            if (null != manifestFile) {
                downloadedFiles.add(manifestFile);
            }
            if (null != downloadedFiles) {
                try {
                    FileOutputStream stream = new FileOutputStream(bundleFileName);
                    stream.write(createECAR(downloadedFiles));
                    stream.close();
                    File contentBundle = new File(bundleFileName);
                    String[] url = AWSUploader.uploadFile(bucketName, ecarFolderName, contentBundle);
                    System.out.println("AWS Upload is complete.... on URL : " + url);
                    downloadedFiles.add(contentBundle);
                    return url;
                } catch (Throwable e) {
                	e.printStackTrace();
                    throw e;
                } finally {
                    HttpDownloadUtility.DeleteFiles(downloadedFiles);
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException(ContentErrorCodes.ERR_ECAR_BUNDLE_FAILED.name(), e.getMessage());
        }
    }
    
    private void addDownloadUrl(Map<Object, List<String>> downloadUrls, Object val, String identifier) {
    	List<String> ids = downloadUrls.get(val);
    	if (null == ids) {
    		ids = new ArrayList<String>();
    		downloadUrls.put(val, ids);
    	}
    	ids.add(identifier.trim());
    }

    private List<File> getContentBundle(final Map<Object, List<String>> downloadUrls, final String bundlePath) {
        List<File> files = new ArrayList<File>();
        try {
            ExecutorService pool = Executors.newFixedThreadPool(10);
            List<Callable<List<File>>> tasks = new ArrayList<Callable<List<File>>>(downloadUrls.size());
            for (final Object val : downloadUrls.keySet()) {
                tasks.add(new Callable<List<File>>() {
                    public List<File> call() throws Exception {
                    	List<String> ids = downloadUrls.get(val);
                    	List<File> files = new ArrayList<File>();
                    	for (String id : ids) {
                    		String destPath = bundlePath + File.separator + id;
                            createDirectoryIfNeeded(destPath);
                            if (val instanceof File) {
                                File file = (File) val;
                                File newFile = new File(destPath + File.separator + file.getName());
                                FileUtils.copyFile(file, newFile);
                                files.add(newFile);
                            } else {
                                String url = val.toString();
                                if (url.endsWith(".ecar")) {
                                    File ecarFile = HttpDownloadUtility.downloadFile(url, destPath + "_ecar");
                                    UnzipUtility unzipper = new UnzipUtility();
                                    unzipper.unzip(ecarFile.getPath(), destPath + "_ecar");
                                    File ecarFolder = new File(destPath + "_ecar" + File.separator + id);
                                    File[] fileList = ecarFolder.listFiles();
                                    File zipFile = null;
                                    if (null != fileList && fileList.length > 0) {
                                        for (File f : fileList) {
                                            if (f.getName().endsWith(".zip")) {
                                                zipFile = f;
                                            }
                                        }
                                    }
                                    if (null != zipFile) {
                                        String newFileName = id + ".zip";
                                        File contentDir = new File(destPath);
                                        if (!contentDir.exists())
                                            contentDir.mkdirs();
                                        zipFile.renameTo(new File(contentDir + File.separator + newFileName));
                                        File ecarTemp = new File(destPath + "_ecar");
                                        FileUtils.deleteDirectory(ecarTemp);
                                        File newFile = new File(contentDir + File.separator + newFileName);
                                        files.add(newFile);
                                    } else {
                                        // do nothing
                                    }
                                } else {
                                	File newFile = HttpDownloadUtility.downloadFile(url, destPath);
                                	if (null != newFile)
                                		files.add(newFile);
                                }
                            }
                    	}
                    	return files;
                    }
                });
            }
            List<Future<List<File>>> results = pool.invokeAll(tasks);
            for (Future<List<File>> ff : results) {
            	List<File> f = ff.get();
                if (null != f && !f.isEmpty())
                    files.addAll(f);
            }
            pool.shutdown();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return files;
    }
    
    private byte[] createECAR(List<File> files) throws IOException {
        // creating byteArray stream, make it bufforable and passing this buffor
        // to ZipOutputStream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);
        // packing files
        for (File file : files) {
        	if (null != file) {
        		String fileName = null;
                if (file.getName().toLowerCase().endsWith("manifest.json")) {
                    fileName = file.getName();
                } else {
                    fileName = file.getParent().substring(file.getParent().lastIndexOf(File.separator) + 1) + File.separator
                            + file.getName();
                }
                // new zip entry and copying inputstream with file to
                // zipOutputStream, after all closing streams
                zipOutputStream.putNextEntry(new ZipEntry(fileName));
                FileInputStream fileInputStream = new FileInputStream(file);

                IOUtils.copy(fileInputStream, zipOutputStream);

                fileInputStream.close();
                zipOutputStream.closeEntry();
        	}
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

    private File createManifestFile(String manifestContent, String bundlePath) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(bundlePath + File.separator + BUNDLE_MANIFEST_FILE_NAME));
            writer.write(manifestContent);
            return new File(bundlePath + File.separator + BUNDLE_MANIFEST_FILE_NAME);
        } catch (IOException ioe) {
            return null;
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException ioe) {
            }
        }
    }

    private void createDirectoryIfNeeded(String directoryName) {
        File theDir = new File(directoryName);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
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