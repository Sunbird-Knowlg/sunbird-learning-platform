package org.sunbird.searchindex.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.commons.codec.Charsets;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.sunbird.common.Platform;
import org.sunbird.telemetry.logger.TelemetryManager;

public class HTTPUtil {

	public static String makeGetRequest(String url) throws Exception {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		request.addHeader("user-id", Platform.config.getString("ekstepPlatformApiUserId"));
		request.addHeader("Content-Type", "application/json");
		HttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() != 200) {
			//System.out.println("URL is " + url + "Status Code: " + response.getStatusLine().getStatusCode());
			TelemetryManager.info("URL is " + url + "Status Code: " + response.getStatusLine().getStatusCode());
			throw new Exception("Ekstep service unavailable: " + response.getStatusLine().getStatusCode() + " : "
					+ response.getStatusLine().getReasonPhrase());
		}
		BufferedReader rd = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent(), Charsets.UTF_8));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		return result.toString();
	}

	public static String makePostRequest(String url, String body) throws Exception {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		post.addHeader("user-id", Platform.config.getString("ekstepPlatformApiUserId"));
		post.addHeader("Content-Type", "application/json; charset=utf-8");
		post.setEntity(new StringEntity(body,  Charsets.UTF_8.name()));
		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new Exception("Ekstep service unavailable: " + response.getStatusLine().getStatusCode() + " : "
					+ response.getStatusLine().getReasonPhrase());
		}
		BufferedReader rd = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent(), Charsets.UTF_8));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		return result.toString();
	}

	public static String makePatchRequest(String url, String body) throws Exception {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPatch patch = new HttpPatch(url);
		patch.addHeader("user-id", Platform.config.getString("ekstepPlatformApiUserId"));
		patch.addHeader("Content-Type", "application/json");
		patch.setEntity(new StringEntity(body));

		HttpResponse response = client.execute(patch);
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new Exception("Ekstep service unavailable: " + response.getStatusLine().getStatusCode() + " : "
					+ response.getStatusLine().getReasonPhrase());
		}
		BufferedReader rd = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent(), Charsets.UTF_8));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		return result.toString();
	}

	public static String makePostRequestUploadFile(String url, File file) throws Exception {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		post.addHeader("user-id", Platform.config.getString("ekstepPlatformApiUserId"));
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("ContentType", "Multipart FileUpload", ContentType.TEXT_PLAIN);
		builder.addBinaryBody("file", new FileInputStream(file), ContentType.APPLICATION_OCTET_STREAM, file.getName());
		post.setEntity(builder.build());

		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new Exception("Ekstep service unavailable: " + response.getStatusLine().getStatusCode() + " : "
					+ response.getStatusLine().getReasonPhrase());
		}
		BufferedReader rd = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent(), Charsets.UTF_8));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		return result.toString();
	}
}
