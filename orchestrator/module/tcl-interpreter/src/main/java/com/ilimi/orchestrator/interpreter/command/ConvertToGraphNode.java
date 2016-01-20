package com.ilimi.orchestrator.interpreter.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import com.ilimi.common.dto.NodeDTO;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.graph.model.node.RelationDefinition;
import com.ilimi.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class ConvertToGraphNode extends BaseSystemCommand implements ICommand, Command {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getCommandName() {
        return "convert_to_graph_node";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        if (argv.length == 3) {
            try {
                TclObject tclObject1 = argv[1];
                TclObject tclObject2 = argv[2];
                if (null == tclObject1 || null == tclObject2) {
                    throw new TclException(interp, "Null arguments to " + getCommandName());
                } else {
                    Object obj1 = ReflectObject.get(interp, tclObject1);
                    Map<String, Object> map = (Map<String, Object>) obj1;
                    Object obj2 = ReflectObject.get(interp, tclObject2);
                    DefinitionDTO def = (DefinitionDTO) obj2;
                    Node node = convertToGraphNode(map, def);
                    TclObject tclResp = ReflectObject.newInstance(interp, node.getClass(), node);
                    interp.setResult(tclResp);
                }

            } catch (Exception e) {
                throw new TclException(interp, "Unable to read response: " + e.getMessage());
            }
        } else {
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_resp_value command");
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Node convertToGraphNode(Map<String, Object> map, DefinitionDTO definition) {
        Node node = new Node();
        if (null != map && !map.isEmpty()) {
            Map<String, String> inRelDefMap = new HashMap<String, String>();
            Map<String, String> outRelDefMap = new HashMap<String, String>();
            getRelDefMaps(definition, inRelDefMap, outRelDefMap);
            List<Relation> inRelations = new ArrayList<Relation>();
            List<Relation> outRelations = new ArrayList<Relation>();
            Map<String, Object> metadata = new HashMap<String, Object>();
            for (Entry<String, Object> entry : map.entrySet()) {
                if (StringUtils.equalsIgnoreCase("identifier", entry.getKey())) {
                    node.setIdentifier((String) entry.getValue());
                } else if (StringUtils.equalsIgnoreCase("objectType", entry.getKey())) {
                    node.setObjectType((String) entry.getValue());
                } else if (StringUtils.equalsIgnoreCase("tags", entry.getKey())) {
                    try {
                        String objectStr = mapper.writeValueAsString(entry.getValue());
                        List<String> tags = mapper.readValue(objectStr, List.class);
                        if (null != tags && !tags.isEmpty())
                            node.setTags(tags);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (inRelDefMap.containsKey(entry.getKey())) {
                    try {
                        String objectStr = mapper.writeValueAsString(entry.getValue());
                        List<Map> list = mapper.readValue(objectStr, List.class);
                        if (null != list && !list.isEmpty()) {
                            for (Map obj : list) {
                                NodeDTO dto = (NodeDTO) mapper.convertValue(obj, NodeDTO.class);
                                inRelations
                                        .add(new Relation(dto.getIdentifier(), inRelDefMap.get(entry.getKey()), null));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (outRelDefMap.containsKey(entry.getKey())) {
                    try {
                        String objectStr = mapper.writeValueAsString(entry.getValue());
                        List<Map> list = mapper.readValue(objectStr, List.class);
                        if (null != list && !list.isEmpty()) {
                            for (Map obj : list) {
                                NodeDTO dto = (NodeDTO) mapper.convertValue(obj, NodeDTO.class);
                                outRelations
                                        .add(new Relation(null, outRelDefMap.get(entry.getKey()), dto.getIdentifier()));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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

    private void getRelDefMaps(DefinitionDTO definition, Map<String, String> inRelDefMap,
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
}
