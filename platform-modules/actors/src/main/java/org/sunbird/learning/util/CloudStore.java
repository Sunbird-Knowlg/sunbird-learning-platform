package org.sunbird.learning.util;

import java.io.File;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.Slug;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.util.S3PropertyReader;
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
			scala.Option<String> endpoint = scala.Option.apply("");
			storageService = StorageServiceFactory.getStorageService(new StorageConfig(cloudStoreType, storageKey, storageSecret,endpoint,""));
		}else if(StringUtils.equalsIgnoreCase(cloudStoreType, "aws")) {
			String storageKey = Platform.config.getString("aws_storage_key");
			String storageSecret = Platform.config.getString("aws_storage_secret");
			scala.Option<String> endpoint = scala.Option.apply("");
			storageService = StorageServiceFactory.getStorageService(new StorageConfig(cloudStoreType, storageKey, storageSecret,endpoint,""));
		}else if(StringUtils.equalsIgnoreCase(cloudStoreType, "oci")) {
			String storageKey = Platform.config.getString("oci_storage_key");
			String storageSecret = Platform.config.getString("oci_storage_secret");
			scala.Option<String> endpoint = scala.Option.apply(Platform.config.getString("oci_storage_endpoint"));
			storageService = StorageServiceFactory.getStorageService(new StorageConfig(cloudStoreType, storageKey, storageSecret,endpoint,""));
		}
		else {
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
		}else if(StringUtils.equalsIgnoreCase(cloudStoreType, "oci")) {
			return S3PropertyReader.getProperty("oci_storage_container");
		}
		else {
			throw new ServerException("ERR_INVALID_CLOUD_STORAGE", "Error while getting container name");
		}
	}
	
	public static String[] uploadFile(String folderName, File file, boolean slugFile) throws Exception {
		if (BooleanUtils.isTrue(slugFile))
			file = Slug.createSlugFile(file);
		String objectKey = folderName + "/" + file.getName();
		String container = getContainerName();
		scala.Option<Object> isdirectory = scala.Option.apply(false);
		scala.Option<Object> attempt = scala.Option.apply(1);
		scala.Option<Object> retry = scala.Option.apply(5);
		scala.Option<Object> ttl = scala.Option.apply(null);
		String url = storageService.upload(container, file.getAbsolutePath(), objectKey, isdirectory, attempt, retry, ttl);
		return new String[] { objectKey, url};
	}

	public static String[] uploadDirectory(String folderName, File directory, boolean slugFile) {
		File file = directory;
		if (BooleanUtils.isTrue(slugFile))
			file = Slug.createSlugFile(file);
		String container = getContainerName();
		String objectKey = folderName + File.separator;
		scala.Option<Object> isdirectory = scala.Option.apply(true);
		scala.Option<Object> attempt = scala.Option.apply(1);
		scala.Option<Object> retry = scala.Option.apply(5);
		scala.Option<Object> ttl = scala.Option.apply(null);
		String url = storageService.upload(container, file.getAbsolutePath(), objectKey, isdirectory, attempt, retry, ttl);
		return new String[] { objectKey, url };
	}


	public static Future<List<String>> uploadH5pDirectory(String folderName, File directory, boolean slugFile,
														  ExecutionContext context) {
		File file = directory;
		if (BooleanUtils.isTrue(slugFile))
			file = Slug.createSlugFile(file);
		String container = getContainerName();
		String objectKey = folderName + File.separator;
		scala.Option<Object> ispublic = scala.Option.apply(false);
		scala.Option<Object> ttl = scala.Option.apply(null);
		scala.Option<Object> retry = scala.Option.apply(null);
		return (Future<List<String>>) storageService.uploadFolder(container, file.getAbsolutePath(), objectKey, ispublic, ttl, retry, 1, context);
	}
	
	public static double getObjectSize(String key) throws Exception {
		String container = getContainerName();
		Blob blob = null;
		scala.Option<Object> withpayload = scala.Option.apply(false);
		blob = (Blob) storageService.getObject(container, key, withpayload);
		return blob.contentLength();
	}
	
	public static void copyObjectsByPrefix(String sourcePrefix, String destinationPrefix, boolean isFolder) {
		String container = getContainerName();
		scala.Option<Object> isdirectory = scala.Option.apply(isFolder);
		storageService.copyObjects(container, sourcePrefix, container, destinationPrefix, isdirectory);
	}
	
	public static String getURI(String prefix, Option<Object> isDirectory) {
		String container = getContainerName();
		return storageService.getUri(container, prefix, isDirectory);
	}


	public static void deleteFile(String key, boolean isDirectory) throws Exception {
		String container = getContainerName();
		scala.Option<Object> isdirectory = scala.Option.apply(isDirectory);
		storageService.deleteObject(container, key, isdirectory);
	}

}
