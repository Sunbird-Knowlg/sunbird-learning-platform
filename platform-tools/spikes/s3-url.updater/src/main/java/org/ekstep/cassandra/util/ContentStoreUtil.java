package org.ekstep.cassandra.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class ContentStoreUtil {

	public static Map<String, Map<String, Object>> getAllContent() {
		Map<String, Map<String, Object>> contents = new HashMap<String, Map<String, Object>>();
		Session session = CassandraConnector.getSession();
		String query = getSelectQuery();
		if (StringUtils.isNotBlank(query)) {
			PreparedStatement ps = session.prepare(query);
			BoundStatement bound = ps.bind();
			try {
				ResultSet rs = session.execute(bound);
				if (null != rs) {
					while (rs.iterator().hasNext()) {
						Row row = rs.iterator().next();
						Map<String, Object> metadata = new HashMap<String, Object>();
						String body = row.getString("body_text");
						String oldBody = row.getString("oldBody_text");
						metadata.put("body", body);
						metadata.put("oldBody", oldBody);
						String contentId = row.getString("content_id");
						contents.put(contentId, metadata);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return contents;
	}

	public static void updateContent(String contentId, Map<String, Object> map) {
		Session session = CassandraConnector.getSession();
		if (null != map && !map.isEmpty()) {
			String query = getUpdateQuery(map.keySet());
			if (StringUtils.isNotBlank(query)) {
				PreparedStatement ps = session.prepare(query);
				Object[] values = new Object[map.size() + 1];
				int i = 0;
				for (Entry<String, Object> entry : map.entrySet()) {
					String value = (String) entry.getValue();
					values[i] = value;
					i += 1;
				}
				values[i] = contentId;
				BoundStatement bound = ps.bind(values);
				try {
					session.execute(bound);
					System.out.println("Content updated - " + contentId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static String getSelectQuery() {
		StringBuilder sb = new StringBuilder();
		sb.append("select blobAsText(body) as body_text, blobAsText(oldBody) as oldBody_text, content_id");
		sb.append(" from content_store.content_data");
		return sb.toString();
	}

	private static String getUpdateQuery(Set<String> properties) {
		StringBuilder sb = new StringBuilder();
		if (null != properties && !properties.isEmpty()) {
			sb.append("UPDATE content_store.content_data SET last_updated_on = dateOf(now()), ");
			StringBuilder updateFields = new StringBuilder();
			for (String property : properties) {
				if (StringUtils.isNotBlank(property))
					updateFields.append(property.trim()).append(" = textAsBlob(?), ");
			}
			sb.append(StringUtils.removeEnd(updateFields.toString(), ", "));
			sb.append(" where content_id = ?");
		}
		return sb.toString();
	}
}
