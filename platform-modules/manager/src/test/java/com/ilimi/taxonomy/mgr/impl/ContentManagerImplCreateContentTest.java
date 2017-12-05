package com.ilimi.taxonomy.mgr.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.service.common.GraphOperation;
import org.ekstep.graph.service.util.DriverUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ilimi.taxonomy.mgr.IContentManager;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ContentManagerImplCreateContentTest {

	@Autowired
	IContentManager contentManager;
	
	@AfterClass
	public static void afterClass() {
		Driver driver = DriverUtil.getDriver("domain", GraphOperation.WRITE);
		try (Session session = driver.session()) {
			session.run("MATCH (n:domain{}) WHERE n.IL_UNIQUE_ID STARTS WITH 'CONTENT_CREATE_TEST' DETACH DELETE n"); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create Content without contentDisposition and contentEncoding should take default values.
	 * @throws Exception 
	 */
	@Test
	public void testCreateContentWithDefaultEncodingAndDisposition() throws Exception {
		String contentId = "CONTENT_CREATE_TEST_1";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("identifier", contentId);
		map.put("osId", "org.ekstep.quiz.app");
		map.put("mediaType", "content");
		map.put("visibility", "Default");
		map.put("description", "Books for learning about colours, animals, fruits, vegetables, shapes");
		map.put("name", "Learning Books");
		List<String> languages = new ArrayList<String>();
		languages.add("English");
		map.put("language", languages);
		map.put("contentType", "Story");
		map.put("code", "org.ekstep.feb03.story.learningbooks");
		map.put("mimeType", "application/vnd.ekstep.ecml-archive");
		Response response = contentManager.createContent(map);
		Assert.assertTrue(StringUtils.equalsIgnoreCase("OK", response.getResponseCode().name()));
		Map<String, Object> metadata = getMetadata(contentId);
		Assert.assertTrue(StringUtils.equalsIgnoreCase("gzip", (String) metadata.get("contentEncoding")));
		Assert.assertTrue(StringUtils.equalsIgnoreCase("inline", (String) metadata.get("contentDisposition")));
	}
	
	
	@Test
	public void testCreateContentWithKeywords() throws Exception {
		String contentId = "CONTENT_CREATE_TEST_2";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("identifier", contentId);
		map.put("osId", "org.ekstep.quiz.app");
		map.put("mediaType", "content");
		map.put("visibility", "Default");
		map.put("description", "Books for learning about colours, animals, fruits, vegetables, shapes");
		map.put("name", "Learning Books");
		List<String> languages = new ArrayList<String>();
		languages.add("English");
		map.put("language", languages);
		map.put("contentType", "Story");
		map.put("code", "org.ekstep.feb03.story.learningbooks");
		map.put("mimeType", "application/vnd.ekstep.ecml-archive");
		map.put("tags", Arrays.asList("colors", "animals"));
		Response response = contentManager.createContent(map);
		Assert.assertTrue(StringUtils.equalsIgnoreCase("OK", response.getResponseCode().name()));
		Map<String, Object> metadata = getMetadata(contentId);
		Assert.assertTrue(metadata.containsKey("keywords"));
	}
	
	private Map<String, Object> getMetadata(String contentId) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Driver driver = DriverUtil.getDriver("domain", GraphOperation.READ);

		try (Session session = driver.session()) {
			String QUERY = "MATCH (n:domain) WHERE n.IL_UNIQUE_ID = { IL_UNIQUE_ID } RETURN n";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("IL_UNIQUE_ID", contentId);
			StatementResult result = session.run(QUERY, params);
			List<Record> records = result.list();
			if (null != result) {
				if (null != records && records.size() > 0) {
					Record record = records.get(0);
					org.neo4j.driver.v1.types.Node neo4JNode = record.get("n").asNode();
					returnMap.putAll(neo4JNode.asMap());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnMap;

	}
}
