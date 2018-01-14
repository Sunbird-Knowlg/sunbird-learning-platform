package org.ekstep.common.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.slugs.Slug;
import org.ekstep.telemetry.logger.TelemetryManager;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * arguments - operation: createStorage, uploadFiles - storageName: name of the
 * storage - file/folder path: applicable only for uploadFiles operation -
 * region name: optional, name of the AWS region
 * 
 * @author rayulu
 * 
 * TODO: right now AWSUploader support upload to public bucket only.
 */
public class AWSUploader {

	private static final String s3Environment = "s3.env";
	private static final String s3Region = "s3.region";
	private static final String s3 = "s3";
	private static final String aws = "amazonaws.com";
	private static final String dotOper = ".";
	private static final String hyphen = "-";
	private static final String forwardSlash = "/";
	private static final String s3AssetFolder = "s3.asset.folder";
	private static final String s3UploadURLExpiry = "s3.upload.url.expiry";
	private static final String defaultBucketType = "public";
	
	public static String getBucketName(String type) {
		return S3PropertyReader.getProperty("s3."+ type +".bucket");
	}
	
	public static String getBucketName() {
		return getBucketName(defaultBucketType);
	}
	

	public static String[] uploadFile(String folderName, File file) throws Exception {
		file = Slug.createSlugFile(file);
		return upload(folderName, file);
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
		TelemetryManager.log("Fetching bucket name:" + bucketName);
		Region region = getS3Region(S3PropertyReader.getProperty(s3Region));
		if (null != region)
			s3.setRegion(region);
		s3.putObject(new PutObjectRequest(bucketName + "/" + folderName, key, file));
		s3.setObjectAcl(bucketName + "/" + folderName, key, CannedAccessControlList.PublicRead);
		URL url = s3.getUrl(bucketName, folderName + "/" + key);
		TelemetryManager.log("AWS Upload '" + file.getName() + "' complete");
		return new String[] { folderName + "/" + key, url.toURI().toString() };
	}

	public static void deleteFile(String key) throws Exception {
		AmazonS3 s3 = new AmazonS3Client();
		Region region = getS3Region(S3PropertyReader.getProperty(s3Region));
		if (null != region)
			s3.setRegion(region);
		String bucketName = getBucketName();
		s3.deleteObject(new DeleteObjectRequest(bucketName, key));
	}

	public static double getObjectSize(String key) throws IOException {
		AmazonS3 s3 = new AmazonS3Client();
		String bucket = getBucketName();
		return s3.getObjectMetadata(bucket, key).getContentLength();
	}

	public static List<String> getObjectList(String prefix, String bucketType) {
		AmazonS3 s3 = new AmazonS3Client();
		TelemetryManager.log("Reading s3 Bucket and Region: " + getBucketName(bucketType) + " : " + s3Environment);
		String bucketName = getBucketName(bucketType);
		ObjectListing listing = s3.listObjects(bucketName, prefix);
		List<S3ObjectSummary> summaries = listing.getObjectSummaries();
		TelemetryManager.log("SummaryData returned from s3 object Listing: " + summaries.size());
		List<String> fileList = new ArrayList<String>();
		while (listing.isTruncated()) {
			listing = s3.listNextBatchOfObjects(listing);
			summaries.addAll(listing.getObjectSummaries());
		}
		for (S3ObjectSummary data : summaries) {
			fileList.add(data.getKey());
		}
		TelemetryManager.log("resource bundles fileList returned from s3" + fileList);
		return fileList;
	}

	public static String updateURL(String url, String oldPublicBucketName, String oldConfigBucketName) {
		String s3Region = Regions.AP_SOUTHEAST_1.name();
		String bucketRegion = S3PropertyReader.getProperty(s3Environment);
		String bucketName = getBucketName();
		TelemetryManager.log("Existing url:" + url);
		TelemetryManager.log("Fetching bucket name for updating urls:" + bucketName);
		if (bucketRegion != null && bucketName != null) {
			String oldPublicStringV1 = oldPublicBucketName + dotOper + s3 + hyphen + Regions.AP_SOUTHEAST_1 + aws;
			String oldPublicStringV2 = s3 + hyphen + Regions.AP_SOUTHEAST_1 + aws + forwardSlash + oldPublicBucketName;
			String oldConfigStringV1 = oldConfigBucketName + dotOper + s3 + hyphen + Regions.AP_SOUTHEAST_1 + aws;
			String oldConfigStringV2 = s3 + hyphen + Regions.AP_SOUTHEAST_1 + aws + forwardSlash + oldConfigBucketName;
			String region = S3PropertyReader.getProperty(s3Region);
			if (null != region)
				s3Region = region;
			String newString = bucketName + dotOper + s3 + hyphen + s3Region + dotOper + aws;
			url = url.replaceAll(oldPublicStringV1, newString);
			url = url.replaceAll(oldPublicStringV2, newString);
			url = url.replaceAll(oldConfigStringV1, newString);
			url = url.replaceAll(oldConfigStringV2, newString);
			TelemetryManager.log("Updated bucket url:" + url);
		}
		return url;
	}

	public static boolean copyObjects(String sourceBucketName, String sourceKey, String destinationBucketName,
			String destinationKey) {
		boolean isCopied = true;
		AmazonS3 s3client = new AmazonS3Client();
		try {
			// Copying object
			CopyObjectRequest copyObjRequest = new CopyObjectRequest(sourceBucketName, sourceKey, destinationBucketName,
					destinationKey);
			TelemetryManager.log("[AWS UPLOADER UTILITY | Copy Objects] : Copying object.");
			s3client.copyObject(copyObjRequest);
			s3client.setObjectAcl(destinationBucketName, destinationKey, CannedAccessControlList.PublicRead);
		} catch (AmazonServiceException ase) {
			TelemetryManager.log("Caught an AmazonServiceException, " + "which means your request made it "
					+ "to Amazon S3, but was rejected with an error " + "response for some reason.");
			TelemetryManager.log("Error Message:    " + ase.getMessage());
			TelemetryManager.log("HTTP Status Code: " + ase.getStatusCode());
			TelemetryManager.log("AWS Error Code:   " + ase.getErrorCode());
			TelemetryManager.log("Error Type:       " + ase.getErrorType());
			TelemetryManager.log("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			TelemetryManager.log("Caught an AmazonClientException, " + "which means the client encountered "
					+ "an internal error while trying to " + " communicate with S3, "
					+ "such as not being able to access the network.");
			TelemetryManager.error("Error Message: " + ace.getMessage(), ace);
		}
		return isCopied;
	}

	public static Map<String, String> copyObjectsByPrefix(String sourceBucketName, String destinationBucketName,
			String sourcePrefix, String destinationPrefix) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			AmazonS3 s3client = new AmazonS3Client();
			ObjectListing objectListing = s3client
					.listObjects(new ListObjectsRequest().withBucketName(sourceBucketName).withPrefix(sourcePrefix));
			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				TelemetryManager.log("[AWS UPLOADER UTILITY | Copying By Prefix]: " + objectSummary.getKey());

				// Source Key
				String source = objectSummary.getKey();
				TelemetryManager.log("[AWS UPLOADER UTILITY | Source]: " + source);

				// Destination Key
				String destination = source.replace(sourcePrefix, destinationPrefix);
				TelemetryManager.log("[AWS UPLOADER UTILITY | Destination]: " + destination);

				// Copying the Objects
				CopyObjectRequest copyObjRequest = new CopyObjectRequest(sourceBucketName, source,
						destinationBucketName, destination);
				s3client.copyObject(copyObjRequest);
				s3client.setObjectAcl(destinationBucketName, destination, CannedAccessControlList.PublicRead);

				// Populate the Map to return
				map.put(source, destination);
			}
		} catch (AmazonServiceException ase) {
			TelemetryManager.log("Caught an AmazonServiceException, " + "which means your request made it "
					+ "to Amazon S3, but was rejected with an error " + "response for some reason.");
			TelemetryManager.log("Error Message:    " + ase.getMessage());
			TelemetryManager.log("HTTP Status Code: " + ase.getStatusCode());
			TelemetryManager.log("AWS Error Code:   " + ase.getErrorCode());
			TelemetryManager.log("Error Type:       " + ase.getErrorType());
			TelemetryManager.log("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			TelemetryManager.error("Caught an AmazonClientException, " + "which means the client encountered "
					+ "an internal error while trying to " + " communicate with S3, "
					+ "such as not being able to access the network: "+ ace.getMessage(), ace);
		}
		return map;
	}

	public static String preSignedURL(String contentId, String fileName) {
		AmazonS3Client s3 = new AmazonS3Client();
		String bucketName = getBucketName();
		Region region = getS3Region(S3PropertyReader.getProperty(s3Region));
		s3.setRegion(region);
		Date expiration = new Date();
		long milliSeconds = expiration.getTime();
		int SECONDS = Integer.parseInt(S3PropertyReader.getProperty(s3UploadURLExpiry));
		milliSeconds += 1000 * SECONDS;
		expiration.setTime(milliSeconds);
		String objectKey = S3PropertyReader.getProperty(s3AssetFolder)+"/"+contentId+"/"+ Slug.makeSlug(fileName);
		GeneratePresignedUrlRequest presignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey);
		presignedUrlRequest.setMethod(HttpMethod.PUT);
		presignedUrlRequest.setExpiration(expiration);
		URL url = s3.generatePresignedUrl(presignedUrlRequest);
		return url.toString();
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

}
