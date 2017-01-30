package org.ekstep.graph.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

public class NodeUtil {

	public static void updateNode(String graphId, String path, String identifier, Map<String, Object> metadata) {
		if (StringUtils.isNotBlank(identifier) && null != metadata && !metadata.isEmpty()) {
			Map<String, Object> paramValuesMap = new HashMap<String, Object>();
			String query = getUpdateNodeQuery(graphId, identifier, metadata, paramValuesMap);
			Driver driver = DriverUtil.getDriver(path);
			try (Session session = driver.session()) {
				StatementResult result = session.run(query, paramValuesMap);
				if (null != result)
					System.out.println("Node updated - " + identifier + " - " + result.list());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static String getUpdateNodeQuery(String graphId, String id, Map<String, Object> metadata,
			Map<String, Object> paramValuesMap) {
		StringBuilder query = new StringBuilder();
		query.append("MATCH (ee:" + graphId + " {IL_UNIQUE_ID: '" + id + "'}) ");
		query.append("SET ");
		for (Entry<String, Object> entry : metadata.entrySet()) {
			query.append("ee." + entry.getKey() + " =  { MD_" + entry.getKey() + " }, ");
			paramValuesMap.put("MD_" + entry.getKey(), entry.getValue());
		}
		return StringUtils.removeEnd(query.toString(), ", ");
	}
}
