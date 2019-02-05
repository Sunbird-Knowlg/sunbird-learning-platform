package org.ekstep.learning.util;

import java.io.File;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.Slug;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.S3PropertyReader;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.Model.Blob;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;

import scala.Option;
import scala.collection.immutable.List;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

public class CloudStore {

private static BaseStorageService storageService = null;
private static String cloudStoreType = Platform.config.getString("cloud_storage_type");
	
	static {

		if(StringUtils.equalsIgnoreCase(cloudStoreType, "azure")) {
			String storageKey = Platform.config.getString("azure_storage_key");
			String storageSecret = Platform.config.getString("azure_storage_secret");
			storageService = StorageServiceFactory.getStorageService(new StorageConfig(cloudStoreType, storageKey, storageSecret));
		}else if(StringUtils.equalsIgnoreCase(cloudStoreType, "aws")) {
			String storageKey = Platform.config.getString("aws_storage_key");
			String storageSecret = Platform.config.getString("aws_storage_secret");
			storageService = StorageServiceFactory.getStorageService(new StorageConfig(cloudStoreType, storageKey, storageSecret));
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
		}else {
			throw new ServerException("ERR_INVALID_CLOUD_STORAGE", "Error while getting container name");
		}
	}
	
	public static String[] uploadFile(String folderName, File file, boolean slugFile) throws Exception {
		if (BooleanUtils.isTrue(slugFile))
			file = Slug.createSlugFile(file);
		String objectKey = folderName + "/" + file.getName();
		String container = getContainerName();
		String url = storageService.upload(container, file.getAbsolutePath(), objectKey, Option.apply(false), Option.apply(false), Option.empty(), Option.apply(5), 1);
		return new String[] { objectKey, url};
	}

	public static String[] uploadDirectory(String folderName, File directory, boolean slugFile) {
		File file = directory;
		if (BooleanUtils.isTrue(slugFile))
			file = Slug.createSlugFile(file);
		String container = getContainerName();
		String objectKey = folderName + File.separator;
		String url = storageService.upload(container, file.getAbsolutePath(), objectKey, Option.apply(false), Option.apply(true), Option.empty(), Option.apply(5), 1);
		return new String[] { objectKey, url };
	}


	public static Future<List<String>> uploadH5pDirectory(String folderName, File directory, boolean slugFile,
														  ExecutionContext context) {
		File file = directory;
		if (BooleanUtils.isTrue(slugFile))
			file = Slug.createSlugFile(file);
		String container = getContainerName();
		String objectKey = folderName + File.separator;
		return (Future<List<String>>) storageService.uploadFolder(container, file.getAbsolutePath(), objectKey, Option.apply(false), Option
						.empty(), Option.empty(), 1, context);
	}
	
	public static double getObjectSize(String key) throws Exception {
		String container = getContainerName();
		Blob blob = null;
		blob = (Blob) storageService.getObject(container, key, Option.apply(false));
		return blob.contentLength();
	}
	
	public static void copyObjectsByPrefix(String sourcePrefix, String destinationPrefix) {
		String container = getContainerName();
		storageService.copyObjects(container, sourcePrefix, container, destinationPrefix, Option.apply(true));
	}
	
	public static String getURI(String prefix, Option<Object> isDirectory) {
		String container = getContainerName();
		return storageService.getUri(container, prefix, isDirectory);
	}


	public static void deleteFile(String key, boolean isDirectory) throws Exception {
		String container = getContainerName();
		storageService.deleteObject(container, key, Option.apply(isDirectory));
	}

}
