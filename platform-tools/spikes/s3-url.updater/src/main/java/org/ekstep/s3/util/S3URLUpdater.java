package org.ekstep.s3.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.cassandra.util.CassandraConnector;
import org.ekstep.cassandra.util.ContentStoreUtil;
import org.ekstep.graph.util.NodeUtil;
import org.ekstep.graph.util.SearchUtil;

public class S3URLUpdater {

	private static final String oldRegion = "ap-southeast-1";
	private static final String newRegion = "ap-south-1";
	private static final String oldPublicBucketName = "ekstep-public";
	private static final String oldConfigBucketName = "ekstep-config";
	private static final String s3 = "s3";
	private static final String aws = "amazonaws.com";
	private static final String dotOper = ".";
	private static final String hyphen = "-";
	private static final String forwardSlash = "/";

	/**
	 * @param args
	 *            graph-ids neo4j-address cassandra-host environment
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (null != args && args.length == 4) {
			String graphIds = args[0];
			if (StringUtils.isBlank(graphIds))
				throw new Exception("Invalid list of graph ids");
			String path = args[1];
			if (StringUtils.isBlank(path) || !path.contains(":"))
				throw new Exception("Invalid neo4j path");
			String cassandraHost = args[2];
			if (StringUtils.isBlank(cassandraHost) || cassandraHost.contains(":"))
				throw new Exception("Invalid cassandra host");
			String env = args[3];
			if (StringUtils.isBlank(env))
				throw new Exception("Invalid environment value");
			List<String> list = Arrays.asList(args[0].split("\\s*,\\s*"));
			for (String graphId : list) {
				if (StringUtils.isNotBlank(graphId)) {
					if (StringUtils.equals("domain", graphId)) {
						CassandraConnector.init(cassandraHost);
						updateCassandraData(env);
						CassandraConnector.close();
					}
					updateGraphData(graphId, path, env);
				}
			}
		} else {
			System.out.println("No of arguments should be four - comma separated graph_ids, neo4j bolt path, "
					+ "cassandra host, and environment. Ex. en,domain,hi localhost:7687 10.2.10.56 dev");
			throw new Exception("No of arguments should be four - comma separated graph_ids, neo4j bolt path, "
					+ "cassandra host, and environment. Ex. en,domain,hi localhost:7687 10.2.10.56 dev");
		}
	}

	private static void updateCassandraData(String env) {
		Map<String, Map<String, Object>> nodes = ContentStoreUtil.getAllContent();
		if (null != nodes && !nodes.isEmpty()) {
			System.out.println("get content size: " + nodes.size());
			for (Entry<String, Map<String, Object>> entry : nodes.entrySet()) {
				String id = entry.getKey();
				Map<String, Object> node = entry.getValue();
				if (null != node && !node.isEmpty()) {
					Map<String, Object> metadata = new HashMap<String, Object>();
					updateContentProperty("body", node, metadata, env);
					updateContentProperty("oldBody", node, metadata, env);
					if (null != metadata && !metadata.isEmpty()) {
						ContentStoreUtil.updateContent(id, metadata);
					}
				}
			}
		}
	}

	private static void updateContentProperty(String property, Map<String, Object> node, Map<String, Object> metadata,
			String env) {
		Object bodyVal = node.get(property);
		String updatedBody = updateS3URL(bodyVal, env);
		if (StringUtils.isNotBlank(updatedBody))
			metadata.put(property, updatedBody);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void updateGraphData(String graphId, String path, String env) {
		Map<String, Map<String, Object>> nodes = SearchUtil.getAllNodes(graphId, path);
		if (null != nodes && !nodes.isEmpty()) {
			System.out.println("get nodes: " + nodes.size());
			for (Entry<String, Map<String, Object>> entry : nodes.entrySet()) {
				String id = entry.getKey();
				Map<String, Object> node = entry.getValue();
				if (null != node && !node.isEmpty()) {
					Map<String, Object> metadata = new HashMap<String, Object>();
					for (UrlProperties prop : UrlProperties.values()) {
						String propName = prop.toString();
						if (null != node.get(propName)) {
							Object propertyVal = node.get(propName);
							if (propertyVal instanceof String) {
								String updatedUrl = updateS3URL(propertyVal, env);
								if (StringUtils.isNotBlank(updatedUrl))
									metadata.put(propName, updatedUrl);
							} else if (propertyVal instanceof Map) {
								Map updatedMap = new HashMap<String, Object>();
								boolean updated = updateMapProperty(propertyVal, env, updatedMap);
								if (updated)
									metadata.put(propName, updatedMap);
							} else if (propertyVal instanceof List) {
								List propertyList = (List) propertyVal;
								List newList = new ArrayList();
								boolean updated = false;
								for (Object property : propertyList) {
									if (property instanceof String) {
										String updatedUrl = updateS3URL(property, env);
										if (StringUtils.isNotBlank(updatedUrl)) {
											updated = true;
											newList.add(updatedUrl);
										} else
											newList.add(property);
									} else if (property instanceof Map) {
										Map updatedMap = new HashMap<String, Object>();
										boolean mapUpdated = updateMapProperty(property, env, updatedMap);
										if (mapUpdated) {
											updated = true;
											newList.add(updatedMap);
										} else
											newList.add(property);
									}
								}
								if (updated)
									metadata.put(propName, newList);
							} else if (propertyVal instanceof Object[]) {
								Object[] propertyList = (Object[]) propertyVal;
								List newList = new ArrayList();
								boolean updated = false;
								for (Object property : propertyList) {
									if (property instanceof String) {
										String updatedUrl = updateS3URL(property, env);
										if (StringUtils.isNotBlank(updatedUrl)) {
											updated = true;
											newList.add(updatedUrl);
										} else
											newList.add(property);
									} else if (property instanceof Map) {
										Map updatedMap = new HashMap<String, Object>();
										boolean mapUpdated = updateMapProperty(property, env, updatedMap);
										if (mapUpdated) {
											updated = true;
											newList.add(updatedMap);
										} else
											newList.add(property);
									}
								}
								if (updated)
									metadata.put(propName, newList);
							}
						}
					}
					if (null != metadata && !metadata.isEmpty()) {
						NodeUtil.updateNode(graphId, path, id, metadata);
					}
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static boolean updateMapProperty(Object propertyVal, String env, Map finalMap) {
		boolean updated = false;
		Map propMap = (Map) propertyVal;
		for (Object mapObject : propMap.entrySet()) {
			Map.Entry entry = (Map.Entry) mapObject;
			Object mapValue = entry.getValue();
			String updatedUrl = updateS3URL(mapValue, env);
			if (StringUtils.isNotBlank(updatedUrl)) {
				updated = true;
				finalMap.put(entry.getKey(), updatedUrl);
			} else {
				finalMap.put(entry.getKey(), mapValue);
			}
		}
		return updated;
	}

	private static String updateS3URL(Object propertyVal, String env) {
		String oldPublicStringV1 = oldPublicBucketName + dotOper + s3 + hyphen + oldRegion + dotOper + aws;
		String oldPublicStringV2 = s3 + hyphen + oldRegion + dotOper + aws + forwardSlash + oldPublicBucketName;
		String oldConfigStringV1 = oldConfigBucketName + dotOper + s3 + hyphen + oldRegion + dotOper + aws;
		String oldConfigStringV2 = s3 + hyphen + oldRegion + dotOper + aws + forwardSlash + oldConfigBucketName;
		String newPublicString = oldPublicBucketName + hyphen + env + dotOper + s3 + hyphen + newRegion + dotOper + aws;
		String newConfigString = oldConfigBucketName + hyphen + env + dotOper + s3 + hyphen + newRegion + dotOper + aws;
		if (null != propertyVal && propertyVal instanceof String) {
			String url = propertyVal.toString();
			if (url.contains(oldPublicStringV1) || url.contains(oldPublicStringV2) || url.contains(oldConfigStringV1)
					|| url.contains(oldConfigStringV2)) {
				url = url.replaceAll(oldPublicStringV1, newPublicString);
				url = url.replaceAll(oldPublicStringV2, newPublicString);
				url = url.replaceAll(oldConfigStringV1, newConfigString);
				url = url.replaceAll(oldConfigStringV2, newConfigString);
				return url;
			}
		}
		return null;
	}
}