package org.sunbird.sync.tool.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.cassandra.connector.util.CassandraConnector;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ServerException;
import org.sunbird.learning.contentstore.ContentStoreParams;
import org.sunbird.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.telemetry.logger.TelemetryManager;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class DialcodeSync {


	private static String indexName = null;
	private static String documentType = null;
	private static String keyspace = null;
	private static String table = null;
	private static String qrImageKeyspace = null;
	private static String qrImageTable = null;

	private static boolean isReplaceString = Platform.config.getBoolean("is_replace_string");
	private static String replaceSrcStringDIALStore = Platform.config.getString("replace_src_string_DIAL_store");
	private static String replaceDestStringDIALStore = Platform.config.getString("replace_dest_string_DIAL_store");

	public DialcodeSync() {
		indexName = Platform.config.hasPath("dialcode.index.name")
				? Platform.config.getString("dialcode.index.name") : "dialcode";
		documentType = Platform.config.hasPath("dialcode.document.type")
				? Platform.config.getString("dialcode.document.type") : "dc";
		keyspace = Platform.config.hasPath("dialcode.keyspace.name")
				? Platform.config.getString("dialcode.keyspace.name") : "sunbirddev_dialcode_store";
		table = Platform.config.hasPath("dialcode.table")
				? Platform.config.getString("dialcode.table") : "dial_code";
		qrImageKeyspace = Platform.config.hasPath("dialcode.qrImageKeyspace.name")
				? Platform.config.getString("dialcode.qrImageKeyspace.name") : "dialcodes";
		qrImageTable = Platform.config.hasPath("dialcode.qrImageTable")
				? Platform.config.getString("dialcode.qrImageTable") : "dialcode_images";
		ElasticSearchUtil.initialiseESClient(indexName, Platform.config.getString("search.es_conn_info"));
	}

	public int sync(List<String> dialcodes) throws Exception {
		System.out.println("DialcodeSync:sync:message:: Total number of Dialcodes to be fetched from cassandra: " + dialcodes.size());
		// Get dialcodes data from cassandra
		Map<String, Object> messages = getDialcodesFromIds(dialcodes);
		if(MapUtils.isEmpty(messages)) {
			System.out.println("DialcodeSync:sync:message:: No dialcodes data fetched from cassandra.");
			return 0;
		}
		System.out.println("DialcodeSync:sync:message:: Total number of Dialcodes data fetched from cassandra: " + messages.size());
		upsertDocument(messages);
		System.out.println("DialcodeSync:sync:Dialcodes synced.");
		return messages.size();
	}

	private void upsertDocument( Map<String, Object> messages) throws Exception {
		ElasticSearchUtil.bulkIndexWithIndexId(indexName, documentType, messages);
	}

	public Map<String, Object> getDialcodesFromIds(List<String> identifiers) {
		try {
			Map<String, Object> messages = new HashMap<String, Object>();
			ResultSet rs = getDialcodesFromDB(identifiers);
			if (null != rs) {
				Map<String, Object> dialCodesFromDB = new HashMap<String, Object>();
				while(rs.iterator().hasNext()) {
					Row row = rs.iterator().next();
					String dialcodeId = (String)row.getString("identifier");
					dialCodesFromDB.put(dialcodeId, row);

					Map<String, Object> syncRequest = new HashMap<String, Object>(){{
						put("identifier", row.getString("identifier"));
						put("channel", row.getString("channel"));
						put("publisher", row.getString("publisher"));
						put("batchcode", row.getString("batchCode"));
						put("status", row.getString("status"));
						put("metadata", row.getString("metadata"));
						put("generated_on", row.getString("generated_on"));
						put("published_on", row.getString("published_on"));
						put("objectType", "DialCode");
					}};

					String imageUrl = getQRImageFromDB(dialcodeId);
					System.out.println("Returned imageUrl: " + imageUrl);
					if(isReplaceString) {
						imageUrl = StringUtils.replaceEach(imageUrl, new String[]{replaceSrcStringDIALStore}, new String[]{replaceDestStringDIALStore});
					}
					System.out.println("Replaced imageUrl: " + imageUrl);
					if(imageUrl != null && !imageUrl.isEmpty()) syncRequest.put("imageUrl", imageUrl);
					messages.put(dialcodeId, syncRequest);
				}
				System.out.println("total dialcodes fetched from cassandra: " + dialCodesFromDB);
				System.out.println("messages: " + JSONUtils.serialize(messages));
				return messages;

			} else {
				return null;
			}
		} catch (Exception e) {
			TelemetryManager.error("Error! Executing get dialcodes: " + e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(),
					"Error fetching dialcode from dialcodes table.", e);
		}
	}

	private ResultSet getDialcodesFromDB(List<String> identifiers) {
		String dialcodes = String.join("', '", identifiers);
		String query = "SELECT * FROM " + keyspace + "." + table + " WHERE identifier IN ('" + dialcodes + "')";
		System.out.println("DialcodeSync::getDialcodesFromDB:: query:: " + query);
		Session session = CassandraConnector.getSession();
		return session.execute(query);
	}

	private String getQRImageFromDB(String dialcodeId) {
		String query = "SELECT url FROM " + qrImageKeyspace + "." + qrImageTable + " WHERE dialcode ='" + dialcodeId + "' ALLOW FILTERING;";
		System.out.println("getQRImageFromDB query: " + query);
		Session session = CassandraConnector.getSession();
		ResultSet rs = session.execute(query);
		while(rs.iterator().hasNext()) {
			Row row = rs.iterator().next();
			System.out.println("getQRImageFromDB url: " + row.getString("url"));
			return row.getString("url");
		}
		return "";
	}


}