package org.ekstep.aws.test;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
@Ignore
public class AWSUploadTest {

	@Test
	public void uploadTest() {
		try {
			AmazonS3Client s3 = new AmazonS3Client();
			Region region = Region.getRegion(Regions.AP_SOUTH_1);
	        s3.setRegion(region);
			File file = new File("src/test/resources/test.jpg");
			String key = file.getName();
			String bucketName = "ekstep-public-dev";
			String folderName = "awstest";
			s3.putObject(new PutObjectRequest(bucketName+"/"+folderName, key, file));
	        s3.setObjectAcl(bucketName+"/"+folderName, key, CannedAccessControlList.PublicRead);
	        URL url = s3.getUrl(bucketName, folderName+"/"+key);
			System.out.println("URL is : " + url.toURI().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void listTest() {
		try {
			String bucketName = "ekstep-public-dev";
			String prefix = "awstest";
			AmazonS3Client s3 = new AmazonS3Client();
			ObjectListing listing = s3.listObjects( bucketName, prefix );
	    	List<S3ObjectSummary> summaries = listing.getObjectSummaries();
	    	while (listing.isTruncated()) {
	     	   listing = s3.listNextBatchOfObjects (listing);
	     	   summaries.addAll(listing.getObjectSummaries());
	     	}
	     	for(S3ObjectSummary data : summaries) {
	     		System.out.println(data.getKey());
	     	}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void deleteTest() {
		try {
			AmazonS3Client s3 = new AmazonS3Client();
			Region region = Region.getRegion(Regions.AP_SOUTH_1);
	        s3.setRegion(region);
			String key = "test.jpg";
			String bucketName = "ekstep-public-dev";
			String folderName = "awstest";
	        s3.deleteObject(new DeleteObjectRequest(bucketName+"/"+folderName, key));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
