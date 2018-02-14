package org.ekstep.learning.contentstore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.cassandra.CassandraTestSetup;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

public class ContentStoreTest extends CassandraTestSetup {

	private static String createKeyspace = "CREATE KEYSPACE IF NOT EXISTS content_store WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}";
	private static String createTable = "CREATE TABLE IF NOT EXISTS content_store.content_data (content_id text, last_updated_on timestamp, body blob, oldBody blob, stageIcons blob, PRIMARY KEY (content_id));";

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
				"SELECT content_id, blobAsText(body) as body FROM content_store.content_data WHERE content_id='"
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
				"SELECT content_id, blobAsText(body) as body FROM content_store.content_data WHERE content_id='"
						+ identifier + "';");
		List<Row> rows = resultSet.all();
		int count = rows.size();
		Assert.assertTrue(count == 1);
		Row row = rows.get(0);
		Assert.assertTrue(identifier.equals(row.getString("content_id")));
		Assert.assertTrue(body.equals(row.getString("body")));
	}

}
