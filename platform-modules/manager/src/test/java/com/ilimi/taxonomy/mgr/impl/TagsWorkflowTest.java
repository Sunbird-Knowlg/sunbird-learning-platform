package com.ilimi.taxonomy.mgr.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.content.common.TestSuitSetup;


// Enable it after adding embedded-redis. 
@Ignore
public class TagsWorkflowTest extends BaseGraphSpec {
	ContentManagerImpl mgr = new ContentManagerImpl();
	ObjectMapper mapper = new ObjectMapper();
	String createValidContentWithTags = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"tags\":[\"akshara\"],\"description\":\"Test Epub content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Epub\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test epub content\",\"mimeType\":\"application/epub\"}";
	String createValidContentWithKeywords = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"keywords\":[\"akshara\"],\"description\":\"Test Epub content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Epub\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test epub content\",\"mimeType\":\"application/epub\"}";
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void createContentWithTagsTest() throws JsonParseException, JsonMappingException, IOException{
		
		Map<String,Object> messageData = mapper.readValue(createValidContentWithTags, new TypeReference<Map<String, Object>>() {
		});
		Response response = mgr.createContent(messageData);
		String node_id = (String)response.getResult().get("node_id");
		Response resp = mgr.find("domain", node_id, null, null);
		Map<String,Object> content = (Map)resp.getResult().get("content");
//		assertEquals(true, content.containsKey("keywords"));
	    assertEquals(false, resp.getResult().containsKey("keywords"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void createContentWithKeywords() throws JsonParseException, JsonMappingException, IOException{
		Map<String,Object> messageData = mapper.readValue(createValidContentWithKeywords, new TypeReference<Map<String, Object>>() {
		});
		Response response = mgr.createContent(messageData);
		String node_id = (String)response.getResult().get("node_id");
		Response resp = mgr.find("domain", node_id, null, null);
		Map<String,Object> content = (Map)resp.getResult().get("content");
//		assertEquals(true, content.containsKey("keywords"));
	}
}
