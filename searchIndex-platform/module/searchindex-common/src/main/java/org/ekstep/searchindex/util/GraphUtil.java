package org.ekstep.searchindex.util;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

// TODO: Auto-generated Javadoc
/**
 * The Class GraphUtil, provides graph related operations like getDataNode.
 *
 * @author karthik
 */
public class GraphUtil {

	/** The mapper. */
	private static ObjectMapper mapper = new ObjectMapper();

	/**
	 * Gets the data node.
	 *
	 * @param graphId
	 *            the graph id
	 * @param identifier
	 *            the identifier
	 * @return the data node
	 * @throws Exception
	 *             the exception
	 */
	public static Map<String, Object> getDataNode(String graphId, String identifier) throws Exception {
		String url = PropertiesUtil.getProperty("platform-api-url") + "/v1/graph/" + graphId + "/datanodes/"
				+ identifier;
		String result = HTTPUtil.makeGetRequest(url);
		Map<String, Object> definitionObject = mapper.readValue(result, new TypeReference<Map<String, Object>>() {
		});
		if (definitionObject == null) {
			throw new Exception("Unable to find Node object.");
		}
		Map resultMap = (Map) definitionObject.get("result");
		if (resultMap == null) {
			throw new Exception("Result in response is empty");
		}
		Map<String, Object> dataNode = (Map<String, Object>) resultMap.get("node");
		if (dataNode == null) {
			throw new Exception("node in result is empty");
		}

		return dataNode;
	}

	/**
	 * Update data node.
	 *
	 * @param graphId
	 *            the graph id
	 * @param identifier
	 *            the identifier
	 * @param ObjectType
	 *            the object type
	 * @param metadata
	 *            the metadata
	 * @throws Exception
	 *             the exception
	 */
	public static void updateDataNode(String graphId, String identifier, String ObjectType,
			Map<String, Object> metadata) throws Exception {
		String url = PropertiesUtil.getProperty("platform-api-url") + "/v1/graph/" + graphId + "/datanode/"
				+ identifier;

		Map<String, Object> requestBodyMap = new HashMap<String, Object>();
		Map<String, Object> requestMap = new HashMap<String, Object>();
		requestMap.put("nodeType", "DATA_NODE");
		requestMap.put("objectType", ObjectType);
		requestMap.put("metadata", metadata);
		requestBodyMap.put("request", requestMap);

		String requestBody = mapper.writeValueAsString(requestBodyMap);

		HTTPUtil.makePatchRequest(url, requestBody);

	}
}
