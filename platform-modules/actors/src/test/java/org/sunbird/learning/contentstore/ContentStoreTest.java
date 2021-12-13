package org.sunbird.learning.contentstore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.sunbird.cassandra.CassandraTestSetup;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Ignore
public class ContentStoreTest extends CassandraTestSetup {
	static final String keyspace = Platform.config.hasPath("content.keyspace.name")
			? Platform.config.getString("content.keyspace.name")
			: "content_store";
	static final String table = "content_data";

	private static String createKeyspace = "CREATE KEYSPACE IF NOT EXISTS " + keyspace
			+ " WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}";
	private static String createTable = "CREATE TABLE IF NOT EXISTS " + keyspace + "." + table
			+ " (content_id text, last_updated_on timestamp, body blob, oldBody blob, stageIcons blob,screenshots blob, externallink text, PRIMARY KEY (content_id));";

	ContentStore contentStore = new ContentStore();

	@BeforeClass
	public static void setup() throws Exception {
		executeScript(createKeyspace, createTable);
	}

	@Test
	public void testContentBodySave() {
		String identifier = "test_content";
		String body = "test_content_body";
		contentStore.updateContentBody(identifier, body);
		ResultSet resultSet = getSession().execute(
				"SELECT content_id, blobAsText(body) as body FROM " + keyspace + "." + table + " WHERE content_id='"
						+ identifier + "';");
		List<Row> rows = resultSet.all();
		int count = rows.size();
		Assert.assertTrue(count == 1);
		Row row = rows.get(0);
		Assert.assertTrue(identifier.equals(row.getString("content_id")));
		Assert.assertTrue(body.equals(row.getString("body")));
	}

	@Test
	public void testContentBodySaveAndGet() {
		String identifier = "test_content";
		String body = "test_content_body";
		contentStore.updateContentBody(identifier, body);
		String returnedBody = contentStore.getContentBody(identifier);
		Assert.assertTrue(body.equals(returnedBody));
	}

	@Test(expected = ClientException.class)
	public void testContentPropSaveInvalidRequest() {
		contentStore.updateContentProperty("test_content", "", "");
	}

	@Test(expected = ServerException.class)
	public void testContentPropSaveInvalidRequest2() {
		contentStore.updateContentProperty("", "body", "test_content_body");
	}

	@Test
	public void testUpdateContentProps() {
		String identifier = "test_content";
		String body = "test_content_body";
		Map<String, Object> map = new HashMap<>();
		map.put("body", body);
		contentStore.updateContentProperties(identifier, map);
		ResultSet resultSet = getSession().execute(
				"SELECT content_id, blobAsText(body) as body FROM " + keyspace + "." + table + " WHERE content_id='"
						+ identifier + "';");
		List<Row> rows = resultSet.all();
		int count = rows.size();
		Assert.assertTrue(count == 1);
		Row row = rows.get(0);
		Assert.assertTrue(identifier.equals(row.getString("content_id")));
		Assert.assertTrue(body.equals(row.getString("body")));
	}
	
	@Test(expected = ClientException.class)
	public void testUpdateContentPropsEmptyPropertyNameToSave() {
		String identifier = "test_content";
		String body = "test_content_body";
		Map<String, Object> map = new HashMap<>();
		map.put("", body);
		contentStore.updateContentProperties(identifier, map);
	}

	@Test(expected = ClientException.class)
	public void testUpdateContentPropsEmptyPropsMapToSave() {
		contentStore.updateContentProperties("test_content", null);
	}
	
	@Test(expected = ServerException.class)
	public void testUpdateContentPropsWithEmptyId() {
		String body = "test_content_body";
		Map<String, Object> map = new HashMap<>();
		map.put("body", body);
		contentStore.updateContentBody("", body);
	}
	
	@Test
	public void testGetContentProps() {
		String identifier = "test_content";
		String body = "test_content_body";
		contentStore.updateContentBody(identifier, body);
		Map<String, Object> map = contentStore.getContentProperties(identifier, Arrays.asList("body"));
		Assert.assertTrue(body.equals(map.get("body")));
	}
	
	@Test(expected = ClientException.class)
	public void testGetContentPropsWithEmptyProps() {
		String identifier = "test_content";
		String body = "test_content_body";
		contentStore.updateContentBody(identifier, body);
		contentStore.getContentProperties(identifier, Arrays.asList());
	}
	
	@Test(expected = ServerException.class)
	public void testGetContentPropsWithoutId() {
		String identifier = "test_content";
		String body = "test_content_body";
		contentStore.updateContentBody(identifier, body);
		contentStore.getContentProperties("", Arrays.asList("body"));
	}
	
	@Test
	public void testUpdateExternalLink() throws JsonParseException, JsonMappingException, IOException {
		String identifier = "test_content";
		List<Map<String, Object>> externalLinkList = new ArrayList<Map<String,Object>>();
		Map<String, Object> externalLink = new HashMap<>();
		externalLink.put("id", "test_asset");
		externalLink.put("src", "https://www.youtube.com/watch?v=Gi2nuTLse7M");
		externalLink.put("type", "youtube");
		externalLinkList.add(externalLink);
		
		contentStore.updateExternalLink(identifier, externalLinkList);
		ResultSet resultSet = getSession().execute(
				"SELECT content_id, externallink FROM " + keyspace + "." + table + " WHERE content_id='"
						+ identifier + "';");
		List<Row> rows = resultSet.all();
		int count = rows.size();
		Assert.assertTrue(count == 1);
		Row row = rows.get(0);
		Assert.assertTrue(identifier.equals(row.getString("content_id")));
		String extLink = row.getString("externallink");
		ObjectMapper o = new ObjectMapper();
		List<Map<String, Object>> extLinkList = o.readValue(extLink, List.class);
		Assert.assertEquals("https://www.youtube.com/watch?v=Gi2nuTLse7M", extLinkList.get(0).get("src"));
	}
}
