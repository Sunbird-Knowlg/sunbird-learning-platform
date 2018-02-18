package org.ekstep.graph.util;

import java.util.ArrayList;
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

	public static List<Map<String, Object>> getNodes(String graphId, String path, String query) {
		List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();
		Driver driver = DriverUtil.getDriver(path);
		try (Session session = driver.session()) {
			StatementResult result = session.run(query);
			if (null != result) {
				for (Record record : result.list()) {
					if (null != record) {
						Value nodeValue = record.get("ee");
						if (null != nodeValue && StringUtils.equalsIgnoreCase("NODE", nodeValue.type().name())) {
							Node node = record.get("ee").asNode();
							Map<String, Object> metadata = new HashMap<String, Object>();
							Iterable<String> keys = node.keys();
							for (String key : keys) {
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
									} else {
										metadata.put(key, value.asObject());
											/*if (StringUtils.equalsIgnoreCase(key, "IL_UNIQUE_ID"))
											identifier = node.get(key).asString();*/	
									}		
								}
							}
						nodes.add(metadata);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nodes;
	}
	
	public static List<Map<String, Object>> getAllNodes(String graphId, String path) {
		return getNodes(graphId, path, getAllNodesQuery(graphId));
	}

	public static List<Map<String, Object>> getResourceContentNodes(String graphId, String path, int offset, int maxSize) {
		return getNodes(graphId, path, getResourceContentNodesQuery(graphId, offset, maxSize));
	}
	
	private static String getAllNodesQuery(String graphId) {
		StringBuilder query = new StringBuilder();
		query.append("MATCH (ee:" + graphId + ") RETURN ee");
		return query.toString();
	}
	
	private static String getResourceContentNodesQuery(String graphId, int offset, int maxSize) {
		StringBuilder query = new StringBuilder();
		query.append("MATCH (ee:" + graphId + "{IL_FUNC_OBJECT_TYPE:'Content', status:'Live'}) where ee.mimeType in [\"application/pdf\", \"application/epub\", \"video/x-youtube\", \"video/mp4\", \"video/webm\"] RETURN ee ");
		
		if (offset > 0) {
			query.append("SKIP ").append(offset).append(" ");
        }
        if (maxSize > 0) {
        	query.append("LIMIT ").append(maxSize).append(" ");
        }
        
        System.out.println(query);
		return query.toString();
	}
}
