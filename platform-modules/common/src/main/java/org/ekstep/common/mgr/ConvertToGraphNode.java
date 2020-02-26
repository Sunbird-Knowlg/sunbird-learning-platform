package org.ekstep.common.mgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.dto.NodeDTO;
import org.ekstep.graph.dac.enums.SystemProperties;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.model.node.RelationDefinition;
import org.ekstep.telemetry.logger.TelemetryManager;

public class ConvertToGraphNode {
	
	private static ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Node convertToGraphNode(Map<String, Object> map, DefinitionDTO definition, Node graphNode) throws Exception {
		Node node = new Node();
        if (null != map && !map.isEmpty()) {
            Map<String, String> inRelDefMap = new HashMap<String, String>();
            Map<String, String> outRelDefMap = new HashMap<String, String>();
            getRelDefMaps(definition, inRelDefMap, outRelDefMap);
            Map<String, List<Relation>> dbRelations = getDBRelations(definition, graphNode, map);
            List<Relation> inRelations = dbRelations.get("in");
            List<Relation> outRelations = dbRelations.get("out");
            
            Map<String, Object> metadata = new HashMap<String, Object>();
            for (Entry<String, Object> entry : map.entrySet()) {
                if (StringUtils.equalsIgnoreCase("identifier", entry.getKey())) {
                    node.setIdentifier((String) entry.getValue());
                } else if (StringUtils.equalsIgnoreCase("objectType", entry.getKey())) {
                    node.setObjectType((String) entry.getValue());
                } else if (inRelDefMap.containsKey(entry.getKey())) {
                    try {
                        String objectStr = mapper.writeValueAsString(entry.getValue());
						List<Map> list = mapper.readValue(objectStr, List.class);
                        if (null != list) {
                        	if (null == inRelations)
                                inRelations = new ArrayList<Relation>();
                            for (Map obj : list) {
                                NodeDTO dto = (NodeDTO) mapper.convertValue(obj, NodeDTO.class);
                                Relation relation = new Relation(dto.getIdentifier(), inRelDefMap.get(entry.getKey()), null);
                                if (null != dto.getIndex() && dto.getIndex().intValue() >= 0) {
                                    Map<String, Object> relMetadata = new HashMap<String, Object>();
                                    relMetadata.put(SystemProperties.IL_SEQUENCE_INDEX.name(), dto.getIndex());
                                    relation.setMetadata(relMetadata);
                                }
                                inRelations
                                        .add(relation);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                } else if (outRelDefMap.containsKey(entry.getKey())) {
                    try {
                        String objectStr = mapper.writeValueAsString(entry.getValue());
                        List<Map> list = mapper.readValue(objectStr, List.class);
                        if (null != list) {
                        	if (null == outRelations)
                                outRelations = new ArrayList<Relation>();
                            for (Map obj : list) {
                                NodeDTO dto = (NodeDTO) mapper.convertValue(obj, NodeDTO.class);
                                Relation relation = new Relation(null, outRelDefMap.get(entry.getKey()), dto.getIdentifier());
                                if (null != dto.getIndex() && dto.getIndex().intValue() >= 0) {
                                    Map<String, Object> relMetadata = new HashMap<String, Object>();
                                    relMetadata.put(SystemProperties.IL_SEQUENCE_INDEX.name(), dto.getIndex());
                                    relation.setMetadata(relMetadata);
                                }
                                outRelations.add(relation);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                } else {
                    metadata.put(entry.getKey(), entry.getValue());
                }
            }
            node.setInRelations(inRelations);
            node.setOutRelations(outRelations);
            node.setMetadata(metadata);
        }
        return node;
	}
	
	private static void getRelDefMaps(DefinitionDTO definition, Map<String, String> inRelDefMap,
            Map<String, String> outRelDefMap) {
        if (null != definition) {
            if (null != definition.getInRelations() && !definition.getInRelations().isEmpty()) {
                for (RelationDefinition rDef : definition.getInRelations()) {
                    if (StringUtils.isNotBlank(rDef.getTitle()) && StringUtils.isNotBlank(rDef.getRelationName())) {
                        inRelDefMap.put(rDef.getTitle(), rDef.getRelationName());
                    }
                }
            }
            if (null != definition.getOutRelations() && !definition.getOutRelations().isEmpty()) {
                for (RelationDefinition rDef : definition.getOutRelations()) {
                    if (StringUtils.isNotBlank(rDef.getTitle()) && StringUtils.isNotBlank(rDef.getRelationName())) {
                        outRelDefMap.put(rDef.getTitle(), rDef.getRelationName());
                    }
                }
            }
        }
    }
    
    private static Map<String, List<Relation>> getDBRelations(DefinitionDTO definition, Node graphNode, Map<String, Object> map) {
    	List<Relation> inRelations = null;
        List<Relation> outRelations = null;
    	if (null != graphNode) {
    		Map<String, String> inRelDefMap = new HashMap<String, String>();
            Map<String, String> outRelDefMap = new HashMap<String, String>();
            getRelationDefinitionMaps(definition, inRelDefMap, outRelDefMap);
            if (null != graphNode.getInRelations()) {
        		for (Relation inRel : graphNode.getInRelations()) {
        			String key = inRel.getRelationType() + inRel.getStartNodeObjectType();
                    if (inRelDefMap.containsKey(key)) {
                    	String value = inRelDefMap.get(key);
                    	if (!map.containsKey(value)) {
                    		if (null == inRelations)
                    			inRelations = new ArrayList<Relation>();
                    		TelemetryManager.log("adding " + value + " to inRelations");
                    		inRelations.add(inRel);
                    	}
                    }
        		}
        	}
            if (null != graphNode.getOutRelations()) {
        		for (Relation outRel : graphNode.getOutRelations()) {
        			String key = outRel.getRelationType() + outRel.getEndNodeObjectType();
                    if (outRelDefMap.containsKey(key)) {
                    	String value = outRelDefMap.get(key);
                    	if (!map.containsKey(value)) {
                    		if (null == outRelations)
                    			outRelations = new ArrayList<Relation>();
                    		TelemetryManager.log("adding " + value + " to outRelations");
                    		outRelations.add(outRel);
                    	}
                    }
        		}
        	}
    	}
    	Map<String, List<Relation>> relationMaps = new HashMap<String, List<Relation>>();
    	relationMaps.put("in", inRelations);
    	relationMaps.put("out", outRelations);
    	return relationMaps;
    }
    
    private static void getRelationDefinitionMaps(DefinitionDTO definition, Map<String, String> inRelDefMap,
            Map<String, String> outRelDefMap) {
        if (null != definition) {
            if (null != definition.getInRelations() && !definition.getInRelations().isEmpty()) {
                for (RelationDefinition rDef : definition.getInRelations()) {
                    getRelationDefinitionKey(rDef, inRelDefMap);
                }
            }
            if (null != definition.getOutRelations() && !definition.getOutRelations().isEmpty()) {
                for (RelationDefinition rDef : definition.getOutRelations()) {
                    getRelationDefinitionKey(rDef, outRelDefMap);
                }
            }
        }
    }

    private static void getRelationDefinitionKey(RelationDefinition rDef, Map<String, String> relDefMap) {
        if (null != rDef.getObjectTypes() && !rDef.getObjectTypes().isEmpty()) {
            for (String type : rDef.getObjectTypes()) {
                String key = rDef.getRelationName() + type;
                relDefMap.put(key, rDef.getTitle());
            }
        }
    }
}
