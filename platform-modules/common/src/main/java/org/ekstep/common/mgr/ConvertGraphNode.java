package org.ekstep.common.mgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.NodeDTO;
import org.ekstep.graph.common.JSONUtils;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.model.node.MetadataDefinition;
import org.ekstep.graph.model.node.RelationDefinition;
import org.ekstep.telemetry.logger.TelemetryManager;

public class ConvertGraphNode {
	
    public static Map<String, Object> convertGraphNode(Node node, String domainId, DefinitionDTO definition,
            List<String> fieldList) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (null != node) {
            Map<String, Object> metadata = node.getMetadata();
            Object sysLastUpdatedOn = metadata.remove(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name());
            if (null != metadata && !metadata.isEmpty()) {
            	List<String> jsonProps = getJSONProperties(definition);
                for (Entry<String, Object> entry : metadata.entrySet()) {
                    if (null != fieldList && !fieldList.isEmpty()) {
                        if (fieldList.contains(entry.getKey()))
                        	if (jsonProps.contains(entry.getKey().toLowerCase())) {
                        		Object val = JSONUtils.convertJSONString((String) entry.getValue());
                        		TelemetryManager.log("JSON Property " + entry.getKey() + " converted value is " + val);
                                if (null != val)
                                	map.put(entry.getKey(), val);
                        	} else 
                        		map.put(entry.getKey(), entry.getValue());
                    } else {
                        String key = entry.getKey();
                        if (StringUtils.isNotBlank(key)) {
                            char c[] = key.toCharArray();
                            c[0] = Character.toLowerCase(c[0]);
                            key = new String(c);
                            if (jsonProps.contains(key.toLowerCase())) {
                                Object val = entry.getValue();
                                if (val instanceof String) {
                                     val = JSONUtils.convertJSONString((String) entry.getValue());
                                }
                            	TelemetryManager.log("JSON Property " + key + " converted value is " + val);
                                if (null != val)
                                	map.put(key, val);
                            } else
                            	map.put(key, entry.getValue());
                        }
                    }
                }
            }
            //TODO: Remove the property after inspection.
            if (sysLastUpdatedOn != null)
                map.put(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name(),sysLastUpdatedOn);

            Map<String, String> inRelDefMap = new HashMap<String, String>();
            Map<String, String> outRelDefMap = new HashMap<String, String>();
            getRelationDefinitionMaps(definition, inRelDefMap, outRelDefMap);
            if (null != node.getInRelations()) {
                Map<String, List<NodeDTO>> inRelMap = new HashMap<String, List<NodeDTO>>();
                for (Relation inRel : node.getInRelations()) {
                    String key = inRel.getRelationType() + inRel.getStartNodeObjectType();
                    if (inRelDefMap.containsKey(key)) {
                        List<NodeDTO> list = inRelMap.get(key);
                        if (null == list) {
                            list = new ArrayList<NodeDTO>();
                            inRelMap.put(inRel.getRelationType() + inRel.getStartNodeObjectType(), list);
                        }
						String objectType = inRel.getStartNodeObjectType();
						String id = inRel.getStartNodeId();
						if (StringUtils.endsWith(objectType, "Image")) {
							objectType = objectType.replace("Image", "");
							id = id.replace(".img", "");
						}
						list.add(new NodeDTO(id, inRel.getStartNodeName(), getDescription(inRel.getStartNodeMetadata()),
								objectType, inRel.getRelationType(), getStatus(inRel.getStartNodeMetadata())));
                    }
                }
                updateReturnMap(map, inRelMap, inRelDefMap);
            }
            if (null != node.getOutRelations()) {
                Map<String, List<NodeDTO>> outRelMap = new HashMap<String, List<NodeDTO>>();
                for (Relation outRel : node.getOutRelations()) {
                    String key = outRel.getRelationType() + outRel.getEndNodeObjectType();
                    if (outRelDefMap.containsKey(key)) {
                        List<NodeDTO> list = outRelMap.get(key);
                        if (null == list) {
                            list = new ArrayList<NodeDTO>();
                            outRelMap.put(key, list);
                        }
						String objectType = outRel.getEndNodeObjectType();
						String id = outRel.getEndNodeId();
						if (StringUtils.endsWith(objectType, "Image")) {
							if (isVisibilityDefault(outRel.getEndNodeMetadata())) {
								objectType = objectType.replace("Image", "");
								id = id.replace(".img", "");
								NodeDTO child = new NodeDTO(id, outRel.getEndNodeName(),
										getDescription(outRel.getEndNodeMetadata()), outRel.getEndNodeObjectType(),
										outRel.getRelationType(), outRel.getMetadata(), getStatus(outRel.getEndNodeMetadata()));
								list.add(child);
							}

						} else {
							NodeDTO child = new NodeDTO(id, outRel.getEndNodeName(),
									getDescription(outRel.getEndNodeMetadata()), outRel.getEndNodeObjectType(),
									outRel.getRelationType(), outRel.getMetadata(), getStatus(outRel.getEndNodeMetadata()));
							list.add(child);
						}
                    }
                }
                updateReturnMap(map, outRelMap, outRelDefMap);
            }
            map.put("identifier", node.getIdentifier());
        }
        return map;
    }

    private static boolean isVisibilityDefault(Map<String, Object> endNodeMetadata) {
        if(null != endNodeMetadata && !endNodeMetadata.isEmpty()) {
            return  !StringUtils.equalsIgnoreCase("Default",(String) endNodeMetadata.get("visibility"));
        }
        return false;
    }

    public static Map<String, Object> convertGraphNodeWithoutRelations(Node node, String domainId, DefinitionDTO definition,
            List<String> fieldList) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (null != node) {
            Map<String, Object> metadata = node.getMetadata();
            if (null != metadata && !metadata.isEmpty()) {
                for (Entry<String, Object> entry : metadata.entrySet()) {
                    if (null != fieldList && !fieldList.isEmpty()) {
                        if (fieldList.contains(entry.getKey()))
                            map.put(entry.getKey(), entry.getValue());
                    } else {
                        String key = entry.getKey();
                        if (StringUtils.isNotBlank(key)) {
                            char c[] = key.toCharArray();
                            c[0] = Character.toLowerCase(c[0]);
                            key = new String(c);
                            map.put(key, entry.getValue());
                        }
                    }
                }
            }

            map.put("identifier", node.getIdentifier());
        }
        return map;
    }
    
    private static String getDescription(Map<String, Object> metadata) {
        if (null != metadata && !metadata.isEmpty()) {
            return (String) metadata.get("description");
        }
        return null;
    }
    private static String getStatus(Map<String, Object> metadata) {
    		if(MapUtils.isNotEmpty(metadata)) {
    			return (String) metadata.get("status");
    		}
        return null;
    }

    private static void updateReturnMap(Map<String, Object> map, Map<String, List<NodeDTO>> relMap,
            Map<String, String> relDefMap) {
        if (null != relMap && !relMap.isEmpty()) {
            for (Entry<String, List<NodeDTO>> entry : relMap.entrySet()) {
                if (relDefMap.containsKey(entry.getKey())) {
					String returnKey = relDefMap.get(entry.getKey());
					if (map.containsKey(returnKey)) {
						List<NodeDTO> nodes = (List<NodeDTO>) map.get(returnKey);
						nodes.addAll(entry.getValue());
						map.put(returnKey, nodes);
					} else {
						map.put(returnKey, entry.getValue());
					}
                }
            }
        } else if (null != relDefMap && !relDefMap.isEmpty()) {
        	List<Object> list = new ArrayList<Object>();
        	for (String val : relDefMap.values()) {
        		if (StringUtils.isNotBlank(val))
        			map.put(val, list);
        	}
        }
    }

    public static void getRelationDefinitionMaps(DefinitionDTO definition, Map<String, String> inRelDefMap,
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
    
    private static List<String> getJSONProperties(DefinitionDTO definition) {
        List<String> props = new ArrayList<String>();
        if (null != definition && null != definition.getProperties()) {
            for (MetadataDefinition mDef : definition.getProperties()) {
                if (StringUtils.equalsIgnoreCase("json", mDef.getDataType()) && StringUtils.isNotBlank(mDef.getPropertyName())) {
                    props.add(mDef.getPropertyName().toLowerCase());
                }
            }
        }
        TelemetryManager.log("JSON properties: " + props);
        return props;
    }
    
    public static void filterNodeRelationships(Map<String, Object> responseMap, DefinitionDTO definition) {
		if(null != definition) {
			if(CollectionUtils.isNotEmpty(definition.getInRelations())) {
				List<String> inRelations = new ArrayList<>();
				definition.getInRelations().stream().forEach(rel -> inRelations.add(rel.getTitle()));
				inRelations.stream().forEach(rel -> {
					List<NodeDTO> relMetaData = (List<NodeDTO>) responseMap.get(rel);
					if(CollectionUtils.isNotEmpty(relMetaData)) {
						Predicate<NodeDTO> predicate = p -> StringUtils.isNotBlank(p.getStatus()) && !StringUtils.equalsIgnoreCase((String)p.getStatus(), "Live");
						relMetaData.removeIf(predicate);
					}
				});
			}
			if(CollectionUtils.isNotEmpty(definition.getOutRelations())) {
				List<String> outRelations = new ArrayList<>();
				definition.getOutRelations().stream().forEach(rel -> outRelations.add(rel.getTitle()));
				outRelations.stream().forEach(rel -> {
					List<NodeDTO> relMetaData = (List<NodeDTO>) responseMap.get(rel);
					if(CollectionUtils.isNotEmpty(relMetaData)) {
						Predicate<NodeDTO> predicate = p -> StringUtils.isNotBlank(p.getStatus()) && !StringUtils.equalsIgnoreCase((String)p.getStatus(), "Live");
						relMetaData.removeIf(predicate);
					}
				});
			}
		}
	}
    
}
