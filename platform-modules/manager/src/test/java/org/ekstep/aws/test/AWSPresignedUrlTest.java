package org.ekstep.aws.test;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.junit.Test;

import com.amazonaws.HttpMethod;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

public class AWSPresignedUrlTest {

	// TODO: Get these details from config or request.
	String bucketName = "ekstep-public-dev";
	String objectKey = "awstest/test.png";
	int SECONDS = 600;

	@Test
	public void generatePresignedUrl() {
		AmazonS3Client s3 = new AmazonS3Client();
		Region region = Region.getRegion(Regions.AP_SOUTH_1);
		s3.setRegion(region);
		Date expiration = new Date();
		long milliSeconds = expiration.getTime();
		milliSeconds += 1000 * SECONDS;
		expiration.setTime(milliSeconds);
		GeneratePresignedUrlRequest presignedUrlRequest = new GeneratePresignedUrlRequest(bucketName,
				objectKey);
		presignedUrlRequest.setMethod(HttpMethod.PUT);
		presignedUrlRequest.setExpiration(expiration);
		URL url = s3.generatePresignedUrl(presignedUrlRequest);
		System.out.println("Pre-Signed URL = " + url.toString());
	}

	@Test
	public void testUploadWithPresignedUrl() {
		//TODO: Add urlStr before testing.
		String urlStr = "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/awstest/test.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20170710T133335Z&X-Amz-SignedHeaders=host&X-Amz-Expires=3599&X-Amz-Credential=AKIAI3QJF6EBIQEQ56MA%2F20170710%2Fap-south-1%2Fs3%2Faws4_request&X-Amz-Signature=0878145777f1a06a59afa0a85eca187a31bd6087a01af652fc2cd2a0388674f7";
		try {
			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("PUT");
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write("This text uploaded as object.");
			out.close();
			int responseCode = connection.getResponseCode();
			System.out.println("Service returned response code " + responseCode);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
