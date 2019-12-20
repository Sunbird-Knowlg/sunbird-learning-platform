package org.ekstep.searchindex.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.Platform;
import org.ekstep.telemetry.logger.TelemetryManager;

@SuppressWarnings("rawtypes")
public class ObjectDefinitionCache {

	private static Map<String, Map> definitionMap = new HashMap<String, Map>();
	private static Map<String, Map> metadataMap = new HashMap<String, Map>();
	private static Map<String, Map<String, String>> relationMap = new HashMap<String, Map<String, String>>();
	private static ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings({ "unchecked" })
	public static Map<String, Object> getDefinitionNode(String objectType, String graphId) throws Exception {
		Map<String, Object> definition = null;
		if (definition == null) {
			getDefinitionFromGraph(objectType, graphId);
			definition = definitionMap.get(objectType);
		}
		TelemetryManager.log("DefinitionMap "+ definitionMap);
		return definition;
	}
	
    public static Map<String, String> getRelationDefinition(String objectType, String graphId) throws Exception {
        Map<String, String> definition = null;
        if (definition == null) {
            getDefinitionFromGraph(objectType, graphId);
            definition = relationMap.get(objectType);
        }
        return definition;
    }

	@SuppressWarnings({ "unchecked" })
	public static Map<String, Object> getMetaData(String objectType, String graphId) throws Exception {
		Map<String, Object> metadata = null;
		if (metadata == null) {
			getDefinitionFromGraph(objectType, graphId);
			metadata = metadataMap.get(objectType);
		}
		return metadata;
	}
	
    public static void setDefinitionNode(String objectType, Map<String, Object> definition) {
		definitionMap.put(objectType, definition);
	}
	
    public static void setRelationDefinition(String objectType, Map<String, String> definition) {
            relationMap.put(objectType, definition);
    }
    
	private static void getDefinitionFromGraph(String objectType, String graphId) throws Exception {
		String url = Platform.config.getString("platform-api-url") + "/taxonomy/" + graphId + "/definition/"
				+ objectType;
		String result = HTTPUtil.makeGetRequest(url);
		Map<String, Object> definitionObject = mapper.readValue(result,
				new TypeReference<Map<String, Object>>() {
				});
		if (definitionObject == null) {
            throw new Exception("Unable to find Definition object.");
        }
        Map resultMap = (Map) definitionObject.get("result");
        if (resultMap == null) {
            throw new Exception("Result in response is empty");
        }
        Map definitionNode = (Map) resultMap.get("definition_node");
        if (definitionNode == null) {
            throw new Exception("Definition node in result is empty");
        }
		Map<String, Object> definition = retrieveProperties(definitionNode);
		definitionMap.put(objectType, definition);

		Map<String, Object> metadata = retrieveMetadata(definitionNode);
		metadataMap.put(objectType, metadata);
		
		Map<String, String> relationDefinition = retrieveRelations(definitionNode, "IN", "inRelations");
		relationDefinition.putAll(retrieveRelations(definitionNode, "OUT", "outRelations"));
		relationMap.put(objectType, relationDefinition);
	}

	@SuppressWarnings({ "unchecked" })
	private static Map<String, Object> retrieveProperties(Map definitionNode) throws Exception {
	    Map<String, Object> definition = new HashMap<String, Object>();
		List<Map> propertiesList = (List<Map>) definitionNode.get("properties");
		if (propertiesList == null || propertiesList.isEmpty()) {
			throw new Exception("Properties List in Definition node is empty");
		}
		for (Map propertyMap : propertiesList) {
			definition.put((String) propertyMap.get("propertyName"), propertyMap);
		}
		return definition;
	}
	
	@SuppressWarnings({ "unchecked" })
	private static Map<String, Object> retrieveMetadata(Map definitionNode) throws Exception {
		Map<String, Object> metadata = (Map) definitionNode.get("metadata");
		return metadata;
	}
	
	@SuppressWarnings({ "unchecked" })
    private static Map<String, String> retrieveRelations(Map definitionNode, String direction, String relationProperty) throws Exception {
        Map<String, String> definition = new HashMap<String, String>();
        List<Map> inRelsList = (List<Map>) definitionNode.get(relationProperty);
        if (null != inRelsList && !inRelsList.isEmpty()) {
            for (Map relMap : inRelsList) {
                List<String> objectTypes = (List<String>) relMap.get("objectTypes");
                if (null != objectTypes && !objectTypes.isEmpty()) {
                    for (String type : objectTypes) {
                        String key = direction + "_" + type + "_" + (String)relMap.get("relationName");
                        definition.put(key, (String)relMap.get("title"));
                    }
                }
            }
        }
        return definition;
    }
	
	public static Map<String, Map> getDefinitionMap() {
		return definitionMap;
	}

    public static void resyncDefinition(String objectType, String graphId) throws Exception {
		getDefinitionFromGraph(objectType, graphId);
	}
    
    @SuppressWarnings("unchecked")
	public static Map<String, Object> getMetaData(String objectType) throws Exception {
    		Map<String, Object> metadata = metadataMap.get(objectType);
    		String graphId = "domain";
		if(null == metadata){
			getDefinitionFromGraph(objectType, graphId);
			metadata = metadataMap.get(objectType);
		}
		return metadata;
	}
}
