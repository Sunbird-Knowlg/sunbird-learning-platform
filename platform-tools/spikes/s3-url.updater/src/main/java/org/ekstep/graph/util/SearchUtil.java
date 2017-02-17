package org.ekstep.graph.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

public class SearchUtil {

	public static Map<String, Map<String, Object>> getAllNodes(String graphId, String path) {
		Map<String, Map<String, Object>> nodes = new HashMap<String, Map<String, Object>>();
		Driver driver = DriverUtil.getDriver(path);
		try (Session session = driver.session()) {
			StatementResult result = session.run(getAllNodesQuery(graphId));
			if (null != result) {
				for (Record record : result.list()) {
					if (null != record) {
						Value nodeValue = record.get("ee");
						if (null != nodeValue && StringUtils.equalsIgnoreCase("NODE", nodeValue.type().name())) {
							Node node = record.get("ee").asNode();
							Map<String, Object> metadata = new HashMap<String, Object>();
							Iterable<String> keys = node.keys();
							String identifier = null;
							for (String key : keys) {
								if (StringUtils.equalsIgnoreCase(key, "IL_UNIQUE_ID"))
									identifier = node.get(key).asString();
								else {
									Value value = node.get(key);
									if (null != value) {
										if (StringUtils.startsWithIgnoreCase(value.type().name(), "LIST")) {
											List<Object> list = value.asList();
											if (null != list && list.size() > 0) {
												Object obj = list.get(0);
												if (obj instanceof String) {
													metadata.put(key, list.toArray(new String[0]));
												} else if (obj instanceof Number) {
													metadata.put(key, list.toArray(new Number[0]));
												} else if (obj instanceof Boolean) {
													metadata.put(key, list.toArray(new Boolean[0]));
												} else {
													metadata.put(key, list.toArray(new Object[0]));
												}
											}
										} else
											metadata.put(key, value.asObject());
									}
								}
								if (StringUtils.isNotBlank(identifier))
									nodes.put(identifier, metadata);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nodes;
	}

	private static String getAllNodesQuery(String graphId) {
		StringBuilder query = new StringBuilder();
		query.append("MATCH (ee:" + graphId + ") RETURN ee");
		return query.toString();
	}
}
