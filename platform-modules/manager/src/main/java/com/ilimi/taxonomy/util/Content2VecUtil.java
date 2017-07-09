package com.ilimi.taxonomy.util;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.content.util.PropertiesUtil;

import com.ilimi.common.dto.CoverageIgnore;
import com.ilimi.common.dto.Request;
import com.ilimi.common.logger.PlatformLogger;

@Deprecated
@CoverageIgnore
public class Content2VecUtil {

	
	private static ObjectMapper mapper = new ObjectMapper();
	
	@CoverageIgnore
	public static void invokeContent2Vec(String contentId, final String event) {
		ExecutorService pool = null;
		try {
			PlatformLogger.log("Call Content2Vec API: " + contentId + " | Event: " + event);
			String url = PropertiesUtil.getProperty("CONTENT_TO_VEC_URL");
			if (StringUtils.isNotBlank(url) && StringUtils.isNotBlank(contentId)) {
				url += "/" + contentId;
				PlatformLogger.log("Content2Vec API URL: " + url);
				final String endPoint = url;
				pool = Executors.newFixedThreadPool(1);
				pool.execute(new Runnable() {
					@Override
					public void run() {
						Request request = new Request();
						if (StringUtils.isNotBlank(event))
							request.put("event", event);
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
			PlatformLogger.log("Content2Vec API: " + url + " | responseCode: " + response.getStatusLine().getStatusCode());
		} catch (Exception e) {
			PlatformLogger.log("Error calling content2vec api", e.getMessage(), e);
		}
	}
}
