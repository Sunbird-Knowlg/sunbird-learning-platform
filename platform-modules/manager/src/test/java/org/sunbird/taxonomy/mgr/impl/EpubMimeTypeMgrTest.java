package org.sunbird.taxonomy.mgr.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.graph.engine.common.GraphEngineTestSetup;
import org.sunbird.learning.router.LearningRequestRouterPool;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EpubMimeTypeMgrTest extends GraphEngineTestSetup {

	ContentManagerImpl mgr = new ContentManagerImpl();
	String createEpubContent = "{\"osId\":\"org.sunbird.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Test Epub content\",\"name\":\"Epub\",\"language\":[\"English\"],\"contentType\":\"Resource\",\"code\":\"test epub content\",\"mimeType\":\"application/epub\"}";
	String requestForReview = "{\"request\":{\"content\":{\"lastPublishedBy\":\"Ekstep\"}}}";
	private String PROCESSING = "Processing";
	private String PENDING = "Pending";
	private static String channelId = "in.ekstep";
	ObjectMapper mapper = new ObjectMapper();
	String node_id = "";

	@BeforeClass
	public static void init() throws Exception {
		loadDefinition("definitions/content_definition.json", "definitions/concept_definition.json",
				"definitions/dimension_definition.json", "definitions/domain_definition.json");
	}

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void createEpubContent() throws Exception {
		Map<String, Object> messageData = mapper.readValue(createEpubContent, new TypeReference<Map<String, Object>>() {
		});
		Response result = mgr.create(messageData, channelId);
		node_id = (String) result.getResult().get("node_id");
	}

	@Test
	public void testEpubUploadEpubContent() {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("Contents/sample.epub").getFile());

		Response resp = mgr.upload(node_id, file, null);
		Map<String, Object> mapData = resp.getResult();
		assertEquals(ResponseCode.OK, resp.getResponseCode());
		assertEquals(true, mapData.containsKey("content_url"));
		String artifactUrl = (String) mapData.get("content_url");
		assertEquals(true, artifactUrl.endsWith("index.epub"));
	}

	@Test
	public void testEpubUploadContentWithInvalidZip() {
		exception.expect(ClientException.class);
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("Contents/content_validator_01.zip").getFile());
		mgr.upload(node_id, file, null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Ignore
	@Test
	public void testReviewForEpubContent() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("Contents/sample4.epub").getFile());

		mgr.upload(node_id, file, null);
		Map<String, Object> contentMap = new HashMap<String, Object>();
		contentMap.put("lastPublishedBy", "ilimi");
		LearningRequestRouterPool.init();
		Request request = new Request();
		request.setContext(contentMap);

		Response response = mgr.review(node_id, request);
		assertEquals(ResponseCode.OK, response.getResponseCode());
		List<String> fields = new ArrayList<String>();
		fields.add("status");
		Response res = mgr.find(node_id, null, fields);

		Map<String, Object> reviewResult = res.getResult();
		assertEquals(true, reviewResult.containsKey("content"));
		Map<String, Object> contents = (Map) reviewResult.get("content");
		assertEquals(true, contents.containsKey("status"));
		assertEquals("Review", contents.get("status"));
	}

	@SuppressWarnings("unchecked")
	@Ignore
	@Test
	public void testPublishForEpubContent() throws InterruptedException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("Contents/index.epub").getFile());

		mgr.upload(node_id, file, null);
		Map<String, Object> contentMap = new HashMap<String, Object>();
		contentMap.put("lastPublishedBy", "ilimi");
		LearningRequestRouterPool.init();

		Response response = mgr.publish(node_id, contentMap);
		assertEquals(ResponseCode.OK, response.getResponseCode());
		List<String> fields = new ArrayList<String>();
		fields.add("status");
		fields.add("downloadUrl");
		Response res = mgr.find(node_id, null, fields);

		Map<String, Object> publishResult = res.getResult();
		assertEquals(true, publishResult.containsKey("content"));
		Map<String, Object> contents = (Map) publishResult.get("content");
		assertEquals(true, contents.containsKey("status"));
		if (contents.get("status").equals(PROCESSING) && contents.get("status").equals(PENDING)) {
			for (int i = 1000; i <= 5000; i = i + 1000) {
				try {
					Thread.sleep(i);
				} catch (InterruptedException e) {
					System.out.println(e);
				}
				Response getContent = mgr.find(node_id, null, fields);
				Map<String, Object> data = getContent.getResult();
				Map<String, Object> contentData = (Map) data.get("content");
				if (contentData.get("status").equals(PROCESSING) && contentData.get("status").equals(PENDING)) {
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
