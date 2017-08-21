package com.ilimi.taxonomy.mgr.impl;

import java.io.File;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.learning.router.LearningRequestRouterPool;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.taxonomy.content.common.BaseGraphSpec;
import com.ilimi.taxonomy.content.common.BaseTest;

public class EpubMimeTypeMgrTest extends BaseGraphSpec {

	ContentManagerImpl mgr = new ContentManagerImpl();
	String createEpubContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Test Epub content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Epub\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test epub content\",\"mimeType\":\"application/epub\"}";
	String requestForReview = "{\"request\":{\"content\":{\"lastPublishedBy\":\"Ekstep\"}}}";
	
	ObjectMapper mapper = new ObjectMapper();
	String node_id = "";
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void createEpubContent() throws JsonParseException, JsonMappingException, IOException{
		Map<String,Object> messageData = mapper.readValue(createEpubContent, new TypeReference<Map<String, Object>>() {
			});
		Response result =  mgr.createContent(messageData);
		node_id = (String)result.getResult().get("node_id");
	}
	
	@Test
	public void testEpubUploadEpubContent() {	
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("Contents/sample.epub").getFile());
		
		Response resp = mgr.upload(node_id, "domain", file);
		Map<String,Object> mapData = resp.getResult();
		assertEquals(ResponseCode.OK, resp.getResponseCode());
		assertEquals(true, mapData.containsKey("content_url"));
		String artifactUrl = (String)mapData.get("content_url");
		assertEquals(true, artifactUrl.endsWith("index.epub"));
	}
	
	@Test
	public void testEpubUploadContentWithInvalidZip() {	
		exception.expect(ClientException.class);
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("Contents/content_validator_01.zip").getFile());
		mgr.upload(node_id, "domain", file);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testReviewForEpubContent() {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("Contents/sample4.epub").getFile());
		
		mgr.upload(node_id, "domain", file);
		Map<String,Object> contentMap = new HashMap<String,Object>();
		contentMap.put("lastPublishedBy", "ilimi");
		LearningRequestRouterPool.init();
		Request request = new Request();
		request.setContext(contentMap);

		Response response = mgr.review("domain", node_id, request);
		assertEquals(ResponseCode.OK, response.getResponseCode());
		List<String> fields = new ArrayList<String>();
		fields.add("status");
		Response res = mgr.find("domain", node_id, null, fields);

		Map<String,Object> reviewResult = res.getResult();
		assertEquals(true, reviewResult.containsKey("content"));
		Map<String,Object> contents = (Map)reviewResult.get("content");
		assertEquals(true, contents.containsKey("status"));
		assertEquals("Review", contents.get("status"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testPublishForEpubContent() throws InterruptedException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("Contents/index.epub").getFile());
		
		mgr.upload(node_id, "domain", file);
		Map<String,Object> contentMap = new HashMap<String,Object>();
		contentMap.put("lastPublishedBy", "ilimi");
		LearningRequestRouterPool.init();

		Response response = mgr.publish("domain", node_id, contentMap);
		assertEquals(ResponseCode.OK, response.getResponseCode());
		List<String> fields = new ArrayList<String>();
		fields.add("status");
		fields.add("downloadUrl");
		Response res = mgr.find("domain", node_id, null, fields);

		Map<String,Object> publishResult = res.getResult();
		assertEquals(true, publishResult.containsKey("content"));
		Map<String,Object> contents = (Map)publishResult.get("content");
		assertEquals(true, contents.containsKey("status"));
		if (contents.get("status").equals("Processing")) {
			for (int i = 1000; i <= 5000; i = i + 1000) {
				try {
					Thread.sleep(i);
				} catch (InterruptedException e) {
					System.out.println(e);
				}
				Response getContent = mgr.find("domain", node_id, null, fields);
                Map<String,Object> data = getContent.getResult();
                Map<String,Object> contentData = (Map) data.get("content");
				if (contentData.get("status").equals("Processing")) {
					i++;
				}
				if (contentData.get("status").equals("Live")) {
					Assert.assertTrue(contentData.get("status").equals("Live"));
					Assert.assertEquals(true, contentData.containsKey("downloadUrl"));
				}
			}
		}
		assertEquals("Live", contents.get("status"));
		Assert.assertEquals(true, contents.containsKey("downloadUrl"));
	}
}
