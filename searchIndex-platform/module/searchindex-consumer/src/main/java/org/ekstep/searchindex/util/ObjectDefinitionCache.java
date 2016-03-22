package org.ekstep.searchindex.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class ObjectDefinitionCache {

	@SuppressWarnings("rawtypes")
	private static Map<String, Map> definitionMap = new HashMap<String, Map>();
	private static ObjectMapper mapper = new ObjectMapper();
	private static ConsumerUtil consumerUtil = new ConsumerUtil();

	@SuppressWarnings({ "unchecked" })
	public static Map<String, Object> getDefinitionNode(String objectType, String graphId) throws Exception {
		Map<String, Object> definition = definitionMap.get(objectType);
		if (definition == null) {
			definition = getDefinitionFromGraph(objectType, graphId);
			definitionMap.put(objectType, definition);
		}
		return definition;
	}

	private static Map<String, Object> getDefinitionFromGraph(String objectType, String graphId) throws Exception {
		String url = consumerUtil.getConsumerConfig().consumerInit.ekstepPlatformURI + "/taxonomy/" + graphId + "/definition/"
				+ objectType;
		String result = consumerUtil.makeHTTPGetRequest(url);
		Map<String, Object> definitionObject = mapper.readValue(result,
				new TypeReference<Map<String, Object>>() {
				});
		Map<String, Object> definition = retrieveProperties(definitionObject);
		return definition;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map<String, Object> retrieveProperties(Map<String, Object> definitionObject) throws Exception {
		if (definitionObject == null) {
			throw new Exception("Unable to find Defintion object.");
		}
		Map<String, Object> definition = new HashMap<String, Object>();
		Map result = (Map) definitionObject.get("result");
		if (result == null) {
			throw new Exception("Result in response is empty");
		}
		Map definitionNode = (Map) result.get("definition_node");
		if (definitionNode == null) {
			throw new Exception("Definition node in result is empty");
		}
		List<Map> propertiesList = (List<Map>) definitionNode.get("properties");
		if (propertiesList == null || propertiesList.isEmpty()) {
			throw new Exception("Properties List in Definition node is empty");
		}
		for (Map propertyMap : propertiesList) {
			definition.put((String) propertyMap.get("propertyName"), propertyMap);
		}
		return definition;
	}

	public static Map<String, Object> resyncDefinition(String objectType, String graphId) throws Exception {
	    System.out.println("resyncDefinition : " + objectType + " -- " + graphId);
		Map<String, Object> definition = getDefinitionFromGraph(objectType, graphId);
		definitionMap.put(objectType, definition);
		return definition;
	}

	public static void main(String[] args) throws Exception {
		getDefinitionNode("Word", "en");
		getDefinitionNode("Word", "en");
	}

}
