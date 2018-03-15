package org.ekstep.sync.tool.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.enums.CompositeSearchParams;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.SystemNodeTypes;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;

public class CompositeIndexGenerateMessage {

	public static Map<String, Object> getKafkaMessage(Node node) {
	    Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> transactionData = new HashMap<String, Object>();
        if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
            Map<String, Object> propertyMap = new HashMap<String, Object>();
            for (Entry<String, Object> entry : node.getMetadata().entrySet()) {
            	String key = entry.getKey();
            	if (StringUtils.isNotBlank(key)) {
            		Map<String, Object> valueMap=new HashMap<String, Object>();
                    valueMap.put("ov", null); // old value
                    valueMap.put("nv", entry.getValue()); // new value
                    // temporary check to not sync body and editorState
                    if (!StringUtils.equalsIgnoreCase("body", key) && !StringUtils.equalsIgnoreCase("editorState", key))
                    	propertyMap.put(entry.getKey(), valueMap);
            	}
            }
            transactionData.put(CompositeSearchParams.properties.name(), propertyMap);
        } else
            transactionData.put(CompositeSearchParams.properties.name(), new HashMap<String, Object>());
        
        
        // add IN relations
        List<Map<String, Object>> relations = new ArrayList<Map<String, Object>>();
        if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
            for (Relation rel : node.getInRelations()) {
                Map<String, Object> relMap = new HashMap<String, Object>();
                relMap.put("rel", rel.getRelationType());
                relMap.put("id", rel.getStartNodeId());
                relMap.put("dir", "IN");
                relMap.put("type", rel.getStartNodeObjectType());
                relMap.put("label", getLabel(rel.getStartNodeMetadata()));
                relations.add(relMap);
            }
        }
        
        // add OUT relations
        if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
            for (Relation rel : node.getOutRelations()) {
                Map<String, Object> relMap = new HashMap<String, Object>();
                relMap.put("rel", rel.getRelationType());
                relMap.put("id", rel.getEndNodeId());
                relMap.put("dir", "OUT");
                relMap.put("type", rel.getEndNodeObjectType());
                relMap.put("label", getLabel(rel.getEndNodeMetadata()));
                relations.add(relMap);
            }
        }
        transactionData.put(CompositeSearchParams.addedRelations.name(), relations);
        map.put(CompositeSearchParams.operationType.name(), GraphDACParams.UPDATE.name());
        map.put(CompositeSearchParams.graphId.name(), node.getGraphId());
        map.put(CompositeSearchParams.nodeGraphId.name(), node.getId());
        map.put(CompositeSearchParams.nodeUniqueId.name(), node.getIdentifier());
        map.put(CompositeSearchParams.objectType.name(), node.getObjectType());
        map.put(CompositeSearchParams.nodeType.name(), SystemNodeTypes.DATA_NODE.name());
        map.put(CompositeSearchParams.transactionData.name(), transactionData);
        map.put(CompositeSearchParams.syncMessage.name(), true);
        return map;
	}
	
	private static String getLabel(Map<String, Object> metadata) {
		if (null != metadata && !metadata.isEmpty()) {
			if (StringUtils.isNotBlank((String) metadata.get("name")))
				return (String) metadata.get("name");
			else if (StringUtils.isNotBlank((String) metadata.get("lemma")))
				return (String) metadata.get("lemma");
			else if (StringUtils.isNotBlank((String) metadata.get("title")))
				return (String) metadata.get("title");
			else if (StringUtils.isNotBlank((String) metadata.get("gloss")))
				return (String) metadata.get("gloss");
		}
		return "";
	}
}
