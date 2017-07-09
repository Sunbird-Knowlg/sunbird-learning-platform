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
	String objectKey = "awstest/test.txt";
	int SECONDS = 120;

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

	public void testUploadWithPresignedUrl() {
		//TODO: Add urlStr before testing.
		String urlStr = "";
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
