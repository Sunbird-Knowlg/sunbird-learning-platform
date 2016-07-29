package org.ekstep.common.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.slugs.Slug;

import com.amazonaws.regions.Region;
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
    
    public static String[] uploadFile(String bucketName, String folderName, File file) throws Exception {
        file = Slug.createSlugFile(file);
        AmazonS3Client s3 = new AmazonS3Client();
        Region region = Region.getRegion(Regions.AP_SOUTHEAST_1);
        s3.setRegion(region);
        String key = file.getName();
        s3.putObject(new PutObjectRequest(bucketName+"/"+folderName, key, file));
        s3.setObjectAcl(bucketName+"/"+folderName, key, CannedAccessControlList.PublicRead);
        URL url = s3.getUrl(bucketName, folderName+"/"+key);
        LOGGER.info("AWS Upload '" + file.getName() + "' complete");
        return new String[] {folderName+"/"+key, url.toURI().toString()};
    }
    
    public static void deleteFile(String bucketName, String key) throws Exception {
        AmazonS3 s3 = new AmazonS3Client();
        s3.deleteObject(new DeleteObjectRequest(bucketName, key));
    }
    
    public static double getObjectSize(String bucket, String key)
            throws IOException {
    	AmazonS3 s3 = new AmazonS3Client();
        return s3.getObjectMetadata(bucket, key).getContentLength();
    }
    
    public static List<String> getObjectList(String bucketName, String prefix){
    	 AmazonS3 s3 = new AmazonS3Client();
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
}
