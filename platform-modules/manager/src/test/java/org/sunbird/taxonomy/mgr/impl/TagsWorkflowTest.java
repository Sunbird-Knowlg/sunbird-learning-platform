package org.sunbird.taxonomy.mgr.impl;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.sunbird.common.dto.Response;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;



// Enable it after adding embedded-redis. 
@Ignore
public class TagsWorkflowTest {
	ContentManagerImpl mgr = new ContentManagerImpl();
	ObjectMapper mapper = new ObjectMapper();
	String createValidContentWithTags = "{\"osId\":\"org.sunbird.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"tags\":[\"akshara\"],\"description\":\"Test Epub content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Epub\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test epub content\",\"mimeType\":\"application/epub\"}";
	String createValidContentWithKeywords = "{\"osId\":\"org.sunbird.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"keywords\":[\"akshara\"],\"description\":\"Test Epub content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Epub\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test epub content\",\"mimeType\":\"application/epub\"}";
	private static String channelId = "in.ekstep";
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void createContentWithTagsTest() throws Exception{
		
		Map<String,Object> messageData = mapper.readValue(createValidContentWithTags, new TypeReference<Map<String, Object>>() {
		});
		Response response = mgr.create(messageData, channelId);
		String node_id = (String)response.getResult().get("node_id");
		Response resp = mgr.find(node_id, null, null);
		Map<String,Object> content = (Map)resp.getResult().get("content");
//		assertEquals(true, content.containsKey("keywords"));
	    assertEquals(false, resp.getResult().containsKey("keywords"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void createContentWithKeywords() throws Exception{
		Map<String,Object> messageData = mapper.readValue(createValidContentWithKeywords, new TypeReference<Map<String, Object>>() {
		});
		Response response = mgr.create(messageData, channelId);
		String node_id = (String)response.getResult().get("node_id");
		Response resp = mgr.find(node_id, null, null);
		Map<String,Object> content = (Map)resp.getResult().get("content");
//		assertEquals(true, content.containsKey("keywords"));
	}
}
