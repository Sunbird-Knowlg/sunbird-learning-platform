package org.sunbird.jobs.samza.util;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ServerException;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;

import scala.Option;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class CloudStorageUtil {

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
        }
        else if(StringUtils.equalsIgnoreCase(cloudStoreType, "cephs3")) {
            String storageKey = Platform.config.getString("cephs3_storage_key");
            String storageSecret = Platform.config.getString("cephs3_storage_secret");
            String endPoint = Platform.config.getString("cephs3_storage_endpoint");
            storageService = StorageServiceFactory.getStorageService(new StorageConfig(cloudStoreType, storageKey, storageSecret, Option.apply(endPoint)));
        }
        else {
            throw new ServerException("ERR_INVALID_CLOUD_STORAGE", "Error while initialising cloud storage");
        }
    }

    public static String uploadFile(String container, String path, File file, boolean isDirectory) {
        int retryCount = Platform.config.getInt("cloud_upload_retry_count");
        String objectKey = path + file.getName();
        String url = storageService.upload(container,
                file.getAbsolutePath(),
                objectKey,
                Option.apply(isDirectory),
                Option.apply(1),
                Option.apply(retryCount),Option.empty());
        return url;
    }

    public static void downloadFile(String downloadUrl, File fileToSave) throws IOException {
        URL url = new URL(downloadUrl);
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(fileToSave);
        FileChannel fileChannel = fileOutputStream.getChannel();
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        fileChannel.close();
        fileOutputStream.close();
        readableByteChannel.close();
    }

}
