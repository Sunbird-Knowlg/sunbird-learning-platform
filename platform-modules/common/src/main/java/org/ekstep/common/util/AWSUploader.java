package org.ekstep.common.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.slugs.Slug;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * arguments 
 * - operation: createStorage, uploadFiles 
 * - storageName: name of the storage 
 * - file/folder path: applicable only for uploadFiles operation
 * - region name: optional, name of the AWS region
 * 
 * @author rayulu
 * 
 */
public class AWSUploader {
	
	private static Logger LOGGER = LogManager.getLogger(AWSUploader.class.getName());
	
	private static final String s3Bucket = "s3.bucket";
	private static final String s3Region = "s3.region";
	private static final String s3 = "s3";
	private static final String aws = "amazonaws.com";
	private static final String dotOper = ".";
	private static final String hyphen = "-";
    
	public static String[] uploadFile(String folderName, File file) throws Exception {
		file = Slug.createSlugFile(file);
		AmazonS3Client s3 = new AmazonS3Client();
		String key = file.getName();
		String bucketRegion = S3PropertyReader.getProperty(s3Region);
		String bucketName = S3PropertyReader.getProperty(bucketRegion, s3Bucket);
		LOGGER.info("Fetching bucket name:"+bucketName);
		s3.putObject(new PutObjectRequest(bucketName+"/"+folderName, key, file));
		s3.setObjectAcl(bucketName+"/"+folderName, key, CannedAccessControlList.PublicRead);
		URL url = s3.getUrl(bucketName, folderName+"/"+key);
		LOGGER.info("AWS Upload '" + file.getName() + "' complete");
		return new String[] {folderName+"/"+key, url.toURI().toString()};
	}

    public static void deleteFile(String key) throws Exception {
        AmazonS3 s3 = new AmazonS3Client();
        String bucketRegion = S3PropertyReader.getProperty(s3Region);
        String bucketName = S3PropertyReader.getProperty(bucketRegion, s3Bucket);
        s3.deleteObject(new DeleteObjectRequest(bucketName, key));
    }
    
    public static double getObjectSize(String key)
            throws IOException {
    	AmazonS3 s3 = new AmazonS3Client();
    	String bucketRegion = S3PropertyReader.getProperty(s3Region);
        String bucket = S3PropertyReader.getProperty(bucketRegion, s3Bucket);
        return s3.getObjectMetadata(bucket, key).getContentLength();
    }
    
    public static List<String> getObjectList(String prefix){
    	AmazonS3 s3 = new AmazonS3Client();
    	String bucketRegion = S3PropertyReader.getProperty(s3Region);
        String bucketName = S3PropertyReader.getProperty(bucketRegion, s3Bucket);
    	ObjectListing listing = s3.listObjects( bucketName, prefix );
    	List<S3ObjectSummary> summaries = listing.getObjectSummaries();
    	List<String> fileList = new ArrayList<String>();	
    	while (listing.isTruncated()) {
    	   listing = s3.listNextBatchOfObjects (listing);
    	   summaries.addAll (listing.getObjectSummaries());
    	}
    	for(S3ObjectSummary data : summaries) {
    		fileList.add(data.getKey());
    	}
		return fileList;
    }
    
    public static String updateURL(String url, String oldBucketName)
    {
    	String bucketRegion = S3PropertyReader.getProperty(s3Region);
        String bucketName = S3PropertyReader.getProperty(bucketRegion, s3Bucket);
        String updatedUrl = url;
        LOGGER.info("Existing url:"+url);
        LOGGER.info("Fetching bucket name for updating urls:"+bucketName);
        if(bucketRegion!=null && bucketName!=null){
        String oldString = oldBucketName + dotOper + s3 + hyphen + Regions.AP_SOUTHEAST_1 + aws;
        String newString = bucketName + dotOper + s3 + dotOper + aws;
        updatedUrl = url.replace(oldString, newString);
        LOGGER.info("Updated bucket url:"+updatedUrl);
        }
        return updatedUrl;
    }

}
