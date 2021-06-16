package org.ekstep.common.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ServerException;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;
import scala.Option;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommonCloudStore {



    private static BaseStorageService storageService = null;
    private static String cloudStoreType = Platform.config.getString("cloud_storage_type");

    static {

        if(StringUtils.equalsIgnoreCase(cloudStoreType, "azure")) {
            String storageKey = Platform.config.getString("azure_storage_key");
            String storageSecret = Platform.config.getString("azure_storage_secret");
            storageService = StorageServiceFactory.getStorageService(new StorageConfig(cloudStoreType, storageKey, storageSecret, Option.empty()));
        }else if(StringUtils.equalsIgnoreCase(cloudStoreType, "aws")) {
            String storageKey = Platform.config.getString("aws_storage_key");
            String storageSecret = Platform.config.getString("aws_storage_secret");
            storageService = StorageServiceFactory.getStorageService(new StorageConfig(cloudStoreType, storageKey, storageSecret, Option.empty()));
        }else if(StringUtils.equalsIgnoreCase(cloudStoreType, "cephs3")) {
            String storageKey = Platform.config.getString("cephs3_storage_key");
            String storageSecret = Platform.config.getString("cephs3_storage_secret");
            String endPoint = Platform.config.getString("cephs3_storage_endpoint");
            storageService = StorageServiceFactory.getStorageService(new StorageConfig(cloudStoreType, storageKey, storageSecret, Option.apply(endPoint)));
        }else {
            throw new ServerException("ERR_INVALID_CLOUD_STORAGE", "Error while initialising cloud storage");
        }
    }

    public static BaseStorageService getCloudStoreService() {
        return storageService;
    }

    public static String getContainerName() {
        if(StringUtils.equalsIgnoreCase(cloudStoreType, "azure")) {
            return Platform.config.getString("azure_storage_container");
        }else if(StringUtils.equalsIgnoreCase(cloudStoreType, "aws")) {
            return S3PropertyReader.getProperty("aws_storage_container");
        }else if(StringUtils.equalsIgnoreCase(cloudStoreType, "cephs3")) {
            return S3PropertyReader.getProperty("cephs3_storage_container");
        }else {
            throw new ServerException("ERR_INVALID_CLOUD_STORAGE", "Error while getting container name");
        }
    }

    public static File download(String artifactUrl, String pathToSave) throws IOException {

        String localPath = (pathToSave!=null && !pathToSave.isEmpty())? pathToSave + File.separator : "tmp/" + artifactUrl.trim() + File.separator;
        String filePath = null;
        String[] fileUrl = artifactUrl.split("/");
        String container = fileUrl[3];

        if(artifactUrl.contains("http")){
            String[] elements = Arrays.copyOfRange(fileUrl, 4, fileUrl.length);
            filePath = String.join(File.separator, elements);
        } else {
            filePath = artifactUrl;
        }
        storageService.download(container, filePath, localPath, Option.apply(false));
        String filename = fileUrl[fileUrl.length - 1];
        File file = new File(localPath + filename);
        return file;

    }

    public static Map<String, Object> getMetadata(String artifactUrl) throws Exception{

        File file = download(artifactUrl, "");
        long fileSize = FileUtils.sizeOf(file);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("size", new MimetypesFileTypeMap().getContentType(file));
        metadata.put("type", fileSize);
        return metadata;
    }

}
