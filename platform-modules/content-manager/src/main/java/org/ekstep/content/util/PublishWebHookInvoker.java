package org.ekstep.content.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.util.S3PropertyReader;

import com.ilimi.common.dto.CoverageIgnore;
import com.ilimi.common.dto.Request;
import com.ilimi.common.logger.PlatformLogger;

@CoverageIgnore
public class PublishWebHookInvoker {

	
	private static ObjectMapper mapper = new ObjectMapper();

	private static Map<String, String> urlMap = new HashMap<String, String>();

	static {
		urlMap.put("dev",
				"http://internal-DEV-CP-ELB-1664506176.ap-south-1.elb.amazonaws.com/index.php?option=com_ekcontent&task=contentform.publishWebHook");
		urlMap.put("qa",
				"http://internal-QA-CP-ELB-1799862814.ap-south-1.elb.amazonaws.com/index.php?option=com_ekcontent&task=contentform.publishWebHook");
		urlMap.put("prod",
				"http://internal-PROD-PORTAL-ELB-1656752128.ap-south-1.elb.amazonaws.com:8000/index.php?option=com_ekcontent&task=contentform.publishWebHook");
	}

	@CoverageIgnore
	public static void invokePublishWebKook(String contentId, final String status, final String error) {
		ExecutorService pool = null;
		try {
			PlatformLogger.log("Call PublishWebHook API: " + contentId + " | Status: " + status + " | Error: " + error);
			String env = S3PropertyReader.getProperty("s3.env");
			String url = urlMap.get(env);
			PlatformLogger.log("PublishWebHook API URL: " + url + " | Environment: " + env);
			if (StringUtils.isNotBlank(url) && StringUtils.isNotBlank(contentId)) {
				final String endPoint = url;
				pool = Executors.newFixedThreadPool(1);
				pool.execute(new Runnable() {
					@Override
					public void run() {
						Request request = new Request();
						request.put("identifier", contentId);
						if (StringUtils.isNotBlank(status))
							request.put("status", status);
						if (StringUtils.isNotBlank(error))
							request.put("publishError", error);
						makeHTTPPostRequest(endPoint, request);
					}
				});
			}
		} catch (Exception e) {
			PlatformLogger.log("Error sending Content2Vec request", e.getMessage(), e);
		} finally {
			if (null != pool)
				pool.shutdown();
		}
	}

	@CoverageIgnore
	private static void makeHTTPPostRequest(String url, Request request) {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(url);
			post.addHeader("Content-Type", "application/json");
			if (null != request) {
				String body = mapper.writeValueAsString(request);
				post.setEntity(new StringEntity(body));
			}
			HttpResponse response = client.execute(post);
			PlatformLogger.log("PublishWebHook API: " + url + " | responseCode: " , response.getStatusLine().getStatusCode());
		} catch (Exception e) {
			PlatformLogger.log("Error calling PublishWebHook api", e.getMessage(), e);
		}
	}
}
