package org.ekstep.jobs.samza.util;

import org.ekstep.common.exception.ServerException;
import org.sunbird.cloud.storage.IStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;

import org.apache.samza.config.Config;
import scala.Some;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class CloudStorageUtil {

    public static String uploadFile(Config config, String container, String path, File file, boolean isPublic, boolean isDirectory) {
        IStorageService storageService = getStorageService(config);
        int retryCount = config.getInt("cloud_upload_retry_count");
        String objectKey = path + file.getName();
        String url = storageService.upload(container,
                file.getAbsolutePath(),
                objectKey,
                Some.apply(isPublic),
                Some.apply(isDirectory),
                Some.empty(),
                Some.apply(retryCount),1);
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

    private static IStorageService getStorageService(Config config) {
        String storageType = config.get("cloud_storage_type");
        String storageKey;
        String storageSecret;
        if (storageType.equalsIgnoreCase("azure")) {
            storageKey = config.get("azure_storage_key");
            storageSecret = config.get("azure_storage_secret");
        } else if (storageType.equalsIgnoreCase("aws")) {
            storageKey = config.get("aws_storage_key");
            storageSecret = config.get("aws_storage_secret");
        } else {
            throw new ServerException("ERR_INVALID_CLOUD_STORAGE", "Error while initialising cloud storage");
        }

        StorageConfig storageConfig = new StorageConfig(storageType, storageKey, storageSecret);
        IStorageService storageService = StorageServiceFactory.getStorageService(storageConfig);
        return storageService;
    }
}
