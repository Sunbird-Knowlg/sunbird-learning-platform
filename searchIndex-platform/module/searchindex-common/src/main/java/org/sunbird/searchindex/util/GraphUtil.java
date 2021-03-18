package org.sunbird.searchindex.util;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.sunbird.common.Platform;

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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, Object> getDataNode(String graphId, String identifier) throws Exception {
		String url = Platform.config.getString("platform-api-url") + "/v1/graph/" + graphId + "/datanodes/"
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

}
