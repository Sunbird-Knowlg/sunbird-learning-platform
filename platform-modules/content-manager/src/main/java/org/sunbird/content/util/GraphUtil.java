package org.sunbird.content.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sunbird.graph.dac.model.Node;

public class GraphUtil {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, String> retrieveRelations(Map definitionNode, String direction, String relationProperty){
		Map<String, String> definition = new HashMap<String, String>();
		List<Map> inRelsList = (List<Map>) definitionNode.get(relationProperty);
		if (null != inRelsList && !inRelsList.isEmpty()) {
			for (Map relMap : inRelsList) {
				List<String> objectTypes = (List<String>) relMap.get("objectTypes");
				if (null != objectTypes && !objectTypes.isEmpty()) {
					for (String type : objectTypes) {
						String key = direction + "_" + type + "_" + (String) relMap.get("relationName");
						definition.put(key, (String) relMap.get("title"));
					}
				}
			}
		}
		return definition;
	}

	@SuppressWarnings("rawtypes")
	public static Map<String, String> getRelationMap(String objectType, Map definitionNode) {
		Map<String, String> relationDefinition = retrieveRelations(definitionNode, "IN", "inRelations");
		relationDefinition.putAll(retrieveRelations(definitionNode, "OUT", "outRelations"));
		return relationDefinition;
	}

	public static List<String> getAllObjectTypes(List<Node> nodes) {
		return nodes.stream().map(x -> x.getObjectType()).distinct().collect(Collectors.toList());
	}
}
