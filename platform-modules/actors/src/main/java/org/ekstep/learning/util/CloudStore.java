package org.ekstep.learning.util;

import java.io.File;
import java.util.Map;

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
import scala.collection.JavaConversions;

public class CloudStore {

private static BaseStorageService storageService = null;
private static String cloudStoreType = Platform.config.getString("cloud_storage_type");
	
	static {
		if(StringUtils.equalsIgnoreCase(cloudStoreType, "azure")) {
			String storageKey = Platform.config.getString("azure_storage_key");
			String storageSecret = Platform.config.getString("azure_storage_secret");
			storageService = StorageServiceFactory.getStorageService(new StorageConfig("azure", storageKey, storageSecret));
		}else if(StringUtils.equalsIgnoreCase(cloudStoreType, "s3")) {
			String storageKey = Platform.config.getString("AWS_ACCESS_KEY_ID");
			String storageSecret = Platform.config.getString("AWS_SECRET_ACCESS_KEY");
			storageService = StorageServiceFactory.getStorageService(new StorageConfig("s3", storageKey, storageSecret));
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
		}else if(StringUtils.equalsIgnoreCase(cloudStoreType, "s3")) {
			return S3PropertyReader.getProperty("s3.public.bucket");
		}else {
			throw new ServerException("ERR_INVALID_CLOUD_STORAGE", "Error while getting container name");
		}
	}
	
	/*public static String[] uploadFile(String folderName, File file) throws Exception {
		file = Slug.createSlugFile(file);
		String url = storageService.upload(getContainerName(), file.getAbsolutePath(), folderName, Option.apply(false), Option.apply(false), Option.empty(), Option.empty());
		return  new String[] { folderName + "/" + file.getName(), url};
	}*/
	
	public static String[] uploadFile(String folderName, File file, boolean slugFile) throws Exception {
		if (BooleanUtils.isTrue(slugFile))
			file = Slug.createSlugFile(file);
		String objectKey = folderName + "/" + file.getName();
		String url = storageService.upload(getContainerName(), file.getAbsolutePath(), objectKey, Option.apply(false), Option.apply(false), Option.empty(), Option.empty());
		return new String[] { objectKey, url};
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
	
	public static String getURL(String bucket, String prefix) {
		String container = getContainerName();
		Blob blob =  (Blob)storageService.getObject(container, prefix, Option.apply(false));
		Map<String, Object> map = scala.collection.JavaConversions.mapAsJavaMap(blob.metadata());
		return (String)map.get("uri");
	}


	public static void deleteFile(String key, boolean isDirectory) throws Exception {
		String container = getContainerName();
		storageService.deleteObject(container, key, Option.apply(isDirectory));
	}

}
