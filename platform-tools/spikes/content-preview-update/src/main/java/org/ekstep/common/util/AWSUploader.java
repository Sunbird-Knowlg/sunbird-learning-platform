package org.ekstep.common.util;

import java.io.File;
import java.net.URL;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.slugs.Slug;
import org.ekstep.content.util.ContentPreviewURLUpdater;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class AWSUploader {

	private static final String s3Region = "AP_SOUTH_1";
		
	public static String getBucketName() {
		return ContentPreviewURLUpdater.aws_bucket;
	}
		
	public static String[] uploadFile(String folderName, File file, boolean slugFile) throws Exception {
		if (BooleanUtils.isTrue(slugFile))
			file = Slug.createSlugFile(file);
		return upload(folderName, file);
	}
	
	private static String[] upload(String folderName, File file) throws Exception {
		AmazonS3Client s3 = new AmazonS3Client();
		String key = file.getName();
		String bucketName = getBucketName();
		Region region = getS3Region(s3Region);
		if (null != region)
			s3.setRegion(region);
		s3.putObject(new PutObjectRequest(bucketName + "/" + folderName, key, file));
		s3.setObjectAcl(bucketName + "/" + folderName, key, CannedAccessControlList.PublicRead);
		URL url = s3.getUrl(bucketName, folderName + "/" + key);
		return new String[] { folderName + "/" + key, url.toURI().toString() };
	}

	public static void deleteFile(String key) throws Exception {
		AmazonS3 s3 = new AmazonS3Client();
		Region region = getS3Region(s3Region);
		if (null != region)
			s3.setRegion(region);
		String bucketName = getBucketName();
		s3.deleteObject(new DeleteObjectRequest(bucketName, key));
	}
	
	private static Region getS3Region(String name) {
		if (StringUtils.isNotBlank(name)) {
			if (StringUtils.equalsIgnoreCase(Regions.AP_SOUTH_1.name(), name))
				return Region.getRegion(Regions.AP_SOUTH_1);
			else if (StringUtils.equalsIgnoreCase(Regions.AP_SOUTHEAST_1.name(), name))
				return Region.getRegion(Regions.AP_SOUTHEAST_1);
			else if (StringUtils.equalsIgnoreCase(Regions.AP_SOUTHEAST_2.name(), name))
				return Region.getRegion(Regions.AP_SOUTHEAST_2);
			else if (StringUtils.equalsIgnoreCase(Regions.AP_NORTHEAST_1.name(), name))
				return Region.getRegion(Regions.AP_NORTHEAST_1);
			else if (StringUtils.equalsIgnoreCase(Regions.AP_NORTHEAST_2.name(), name))
				return Region.getRegion(Regions.AP_NORTHEAST_2);
			else
				return Region.getRegion(Regions.AP_SOUTHEAST_1);
		} else
			return Region.getRegion(Regions.AP_SOUTHEAST_1);
	}
	
	public static String getURL(String prefix) {
		String bucket = getBucketName();
		AmazonS3 s3client = new AmazonS3Client();
		Region region = getS3Region(s3Region);
		if (null != region)
			s3client.setRegion(region);
		return s3client.getUrl(bucket, prefix).toString();
	}

	public static boolean checkAwsFolderExists(String awsFolder) {
		String bucket = getBucketName();
		AmazonS3 s3 = new AmazonS3Client();
		Region region = getS3Region(s3Region);
		if (null != region)
			s3.setRegion(region);
		return s3.doesBucketExist(bucket) 
		       && !s3.listObjects(bucket, awsFolder).getObjectSummaries().isEmpty();
	}

}