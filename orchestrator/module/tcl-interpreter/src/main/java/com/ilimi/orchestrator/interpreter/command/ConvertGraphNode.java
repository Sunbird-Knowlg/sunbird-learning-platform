package com.ilimi.orchestrator.interpreter.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.graph.common.JSONUtils;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.graph.model.node.MetadataDefinition;
import com.ilimi.graph.model.node.RelationDefinition;
import com.ilimi.orchestrator.interpreter.ICommand;

public class ConvertGraphNode extends BaseSystemCommand implements ICommand, Command {

	
	
    @Override
    public String getCommandName() {
        return "convert_graph_node";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        if (argv.length >= 3) {
            try {
                TclObject tclObject1 = argv[1];
                TclObject tclObject2 = argv[2];
                if (null == tclObject1 || null == tclObject2) {
                    throw new TclException(interp, "Null arguments to " + getCommandName());
                } else {
                    Object obj1 = ReflectObject.get(interp, tclObject1);
                    Node node = (Node) obj1;
                    Object obj2 = ReflectObject.get(interp, tclObject2);
                    DefinitionDTO def = (DefinitionDTO) obj2;
                    List<String> fields = null;
                    if (argv.length > 3) {
                        TclObject tclObject3 = argv[3];
                        Object obj3 = ReflectObject.get(interp, tclObject3);
                        fields = (List<String>) obj3;
                    } else {
                        if (null != def && null != def.getMetadata()) {
                        	try {
                        		String[] arr = (String[]) def.getMetadata().get("fields");
                        		if (null != arr && arr.length > 0) {
                                    fields = new ArrayList<String>();
                                    for (String field : arr)
                                    	fields.add(field);
                                }
                        	} catch (Exception e) {
                        		List<String> arr = (List<String>) def.getMetadata().get("fields");
                        		if (null != arr && arr.size() > 0) {
                                    fields = new ArrayList<String>();
                                    fields.addAll(arr);
                                }
                        	}
                        }
                    }
                    Map<String, Object> map = convertGraphNode(node, node.getGraphId(), def, fields);
                    TclObject tclResp = ReflectObject.newInstance(interp, map.getClass(), map);
                    interp.setResult(tclResp);
                }

            } catch (Exception e) {
            	e.printStackTrace();
                throw new TclException(interp, "Unable to read response: " + e.getMessage());
            }
        } else {
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_resp_value command");
        }
    }

    private Map<String, Object> convertGraphNode(Node node, String domainId, DefinitionDTO definition,
            List<String> fieldList) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (null != node) {
            Map<String, Object> metadata = node.getMetadata();
            if (null != metadata && !metadata.isEmpty()) {
            	List<String> jsonProps = getJSONProperties(definition);
                for (Entry<String, Object> entry : metadata.entrySet()) {
                    if (null != fieldList && !fieldList.isEmpty()) {
                        if (fieldList.contains(entry.getKey())) {
                        	if (jsonProps.contains(entry.getKey().toLowerCase())) {
                        		Object val = JSONUtils.convertJSONString((String) entry.getValue());
                        		PlatformLogger.log("JSON Property " + entry.getKey() + " converted value is " + val);
                                if (null != val)
                                	map.put(entry.getKey(), val);
                        	} else
                        		map.put(entry.getKey(), entry.getValue());
                        }
                    } else {
                        String key = entry.getKey();
                        if (StringUtils.isNotBlank(key)) {
                            char c[] = key.toCharArray();
                            c[0] = Character.toLowerCase(c[0]);
                            key = new String(c);
                            if (jsonProps.contains(key.toLowerCase())) {
                            	Object val = JSONUtils.convertJSONString((String) entry.getValue());
                            	PlatformLogger.log("JSON Property " + key + " converted value is " + val);
                                if (null != val)
                                	map.put(key, val);
                            } else
                            	map.put(key, entry.getValue());
                        }
                    }
                }
            }
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
                        list.add(new NodeDTO(inRel.getStartNodeId(), inRel.getStartNodeName(), getDescription(inRel.getStartNodeMetadata()),
                                inRel.getStartNodeObjectType(), inRel.getRelationType()));
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
                        list.add(new NodeDTO(outRel.getEndNodeId(), outRel.getEndNodeName(), getDescription(outRel.getEndNodeMetadata()),
                                outRel.getEndNodeObjectType(), outRel.getRelationType(), outRel.getMetadata()));
                    }
                }
                updateReturnMap(map, outRelMap, outRelDefMap);
            }
            if (null != node.getTags() && !node.getTags().isEmpty())
                map.put("tags", node.getTags());
            map.put("identifier", node.getIdentifier());
        }
        return map;
    }
    
    private List<String> getJSONProperties(DefinitionDTO definition) {
        List<String> props = new ArrayList<String>();
        if (null != definition && null != definition.getProperties()) {
            for (MetadataDefinition mDef : definition.getProperties()) {
                if (StringUtils.equalsIgnoreCase("json", mDef.getDataType()) && StringUtils.isNotBlank(mDef.getPropertyName())) {
                    props.add(mDef.getPropertyName().toLowerCase());
                }
            }
        }
        PlatformLogger.log("JSON properties: " , props);
        return props;
    }
    
    private String getDescription(Map<String, Object> metadata) {
        if (null != metadata && !metadata.isEmpty()) {
            return (String) metadata.get("description");
        }
        return null;
    }

    private void updateReturnMap(Map<String, Object> map, Map<String, List<NodeDTO>> relMap,
            Map<String, String> relDefMap) {
        if (null != relMap && !relMap.isEmpty()) {
            for (Entry<String, List<NodeDTO>> entry : relMap.entrySet()) {
                if (relDefMap.containsKey(entry.getKey())) {
                    map.put(relDefMap.get(entry.getKey()), entry.getValue());
                }
            }
        }
    }

    private void getRelationDefinitionMaps(DefinitionDTO definition, Map<String, String> inRelDefMap,
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

    private void getRelationDefinitionKey(RelationDefinition rDef, Map<String, String> relDefMap) {
        if (null != rDef.getObjectTypes() && !rDef.getObjectTypes().isEmpty()) {
            for (String type : rDef.getObjectTypes()) {
                String key = rDef.getRelationName() + type;
                relDefMap.put(key, rDef.getTitle());
            }
        }
    }

}
