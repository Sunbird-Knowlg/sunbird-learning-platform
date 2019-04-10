package org.ekstep.content.mgr.impl.operation.hierarchy;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.graph.common.DateUtils;
import org.ekstep.graph.common.Identifier;
import org.ekstep.graph.dac.enums.AuditProperties;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.SystemNodeTypes;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.model.node.RelationDefinition;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class UpdateHierarchyOperation extends BaseContentManager {

    private HierarchyStore hierarchyStore = new HierarchyStore();

    public Response updateHierarchy(Map<String, Object> data) {
        if (MapUtils.isEmpty(data) && MapUtils.isEmpty((Map<String, Object>) data.get("nodesModified"))) {
            throw new ClientException("ERR_INVALID_HIERARCHY_DATA", "Hierarchy data is empty");
        } else {
            Map<String, Object> nodesModified = (Map<String, Object>) data.get("nodesModified");
            Map<String, Object> hierarchyData = (Map<String, Object>) data.get("hierarchy");
            String rootId = getRootId(nodesModified);
            Map<String, String> idMap = new HashMap<>();
            Map<String, Object> hierarchyResponse = hierarchyStore.getHierarchy(rootId +DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX);

            Response response = getUpdatedHierarchy(rootId, nodesModified, hierarchyData,
                    hierarchyResponse,
                    idMap);
            return response;
        }

    }

    private Response getUpdatedHierarchy(String rootId, Map<String, Object> nodesModified, Map<String, Object>
            hierarchyData,
                                                    Map<String, Object> hierarchyResponse, Map<String, String> idMap) {
        DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
        Map<String, Node> nodeMap = getNodeMapFromHierarchy(hierarchyResponse, definition, rootId);
        Map<String, RelationDefinition> inRelDefMap = new HashMap<>();
        Map<String, RelationDefinition> outRelDefMap = new HashMap<>();
        getRelationDefMaps(definition, inRelDefMap, outRelDefMap);
        updateNodesModified(nodesModified, idMap, nodeMap, definition, inRelDefMap, outRelDefMap, rootId);
        List<Map<String, Object>> children = prepareHierarchy(nodeMap, rootId, hierarchyData,
                definition, idMap);

        Map<String, Object> data = new HashMap<String, Object>() {{
            put("identifier", rootId);
            put("children", children);
        }};

        Response rootNodeResponse = updateDataNode(nodeMap.get(rootId));
        if(checkError(rootNodeResponse)) {
            return rootNodeResponse;
        }
        hierarchyStore.saveOrUpdateHierarchy(rootId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX, data);
        Response response = OK();
        response.put("content_id", rootId);
        response.put("identifiers", idMap);

        return response;
    }

    private List<Map<String,Object>> prepareHierarchy(Map<String, Node> nodeMap, String rootId, Map<String, Object> hierarchyData, DefinitionDTO definition, Map<String, String> idMap) {

        if(MapUtils.isNotEmpty(hierarchyData)) {
            Map<String, List<String>> childIdMap = hierarchyData.keySet().stream().collect(Collectors.toMap((key)
                    -> ((null != idMap.get(key) ? idMap.get(key) : key))
            ,(key) ->  ((List<String>) ((Map<String, Object>)hierarchyData.get(key)).get("children")).stream().map
                                (id -> (null != idMap.get(id) ? idMap.get(id) : id)).collect(toList())));

            nodeMap.get(rootId).getMetadata().put("depth", 0);
            List<String> childNodes = new ArrayList<>();
            updateDepthIndexParent(childIdMap.get(rootId), 1, rootId, nodeMap, childIdMap, childNodes);
            nodeMap.get(rootId).getMetadata().put("childNodes", childNodes);
        }
        Map<String, Object> collectionHierarchy = util.constructHierarchy(getContentList(nodeMap, definition));
        return (List<Map<String, Object>>) collectionHierarchy.get("children");
    }

    private List<Map<String,Object>> getContentList(Map<String, Node> nodeMap, DefinitionDTO definition) {
       return  nodeMap.keySet().stream().map(key -> ConvertGraphNode.convertGraphNode(nodeMap.get(key), TAXONOMY_ID,
                definition, null)).collect(toList());
    }

    private void updateDepthIndexParent(List<String> childrenIds, int depth, String parent, Map<String, Node>
            nodeMap, Map<String, List<String>> hierarchy, List<String> childNodes) {
        int index =1;
        for(String childId: childrenIds) {
            if(null ==  nodeMap.get(childId).getMetadata().get("depth")) {
                nodeMap.get(childId).getMetadata().put("depth", depth);
                nodeMap.get(childId).getMetadata().put("parent", parent);
                nodeMap.get(childId).getMetadata().put("index", index);
            }
            childNodes.add(childId);
            index +=1;

            if (CollectionUtils.isNotEmpty(hierarchy.get(childId))) {
                updateDepthIndexParent(hierarchy.get(childId), (((Integer) nodeMap.get(childId).getMetadata().get("depth")) + 1),
                        childId, nodeMap, hierarchy, childNodes);
            }
        }

    }

    private void updateNodesModified(Map<String, Object> nodesModified, Map<String, String> idMap, Map<String, Node>
            nodeMap, DefinitionDTO definition, Map<String, RelationDefinition> inRelDefMap, Map<String,
            RelationDefinition> outRelDefMap, String rootId) {
        nodeMap.get(rootId).getMetadata().putAll((Map<String, Object>) ((Map<String, Object>)nodesModified.get(rootId))
                .get("metadata"));
        nodesModified.remove(rootId);
        nodesModified.entrySet().forEach(entry -> {
            Map<String, Object> map = (Map<String, Object>) entry.getValue();
            createNodeObject(entry, idMap, nodeMap, new HashMap<>(), definition,
                    inRelDefMap, outRelDefMap);
        });
    }


    private Map<String, Node> getNodeMapFromHierarchy(Map<String, Object> hierarchyResponse, DefinitionDTO definition, String
            rootId) {
        Map<String, Node> nodeMap = new HashMap<>();
        nodeMap.put(rootId, getContentNode(TAXONOMY_ID, rootId, "edit"));
        if (MapUtils.isNotEmpty(hierarchyResponse)) {
            List<Map<String, Object>> children = (List<Map<String, Object>>) hierarchyResponse
                    .get("children");
            getNodeMap(children, nodeMap, definition);
            return nodeMap;
        }
        return nodeMap;
    }

    private void getNodeMap(List<Map<String, Object>> children, Map<String, Node> nodeMap, DefinitionDTO definition) {
        if (CollectionUtils.isNotEmpty(children)) {
            children.forEach(child -> {
                Node node = null;
                try {
                    if(StringUtils.equalsIgnoreCase("Default", (String) child.get("visibility"))) {
                        node = getContentNode(TAXONOMY_ID, (String) child.get("identifier"), null);
                    }else {
                        Map<String, Object> childData = new HashMap<>();
                        childData.putAll(child);
                        childData.remove("children");
                        node = ConvertToGraphNode.convertToGraphNode(childData, definition, null);
                    }
                    nodeMap.put(node.getIdentifier(), node);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                getNodeMap((List<Map<String, Object>>) child.get("children"), nodeMap, definition);

            });
        }
    }

    private String getRootId(Map<String, Object> nodesModified) {
        String rootId = nodesModified.keySet().stream().filter(key -> BooleanUtils.isTrue((Boolean) ((Map<String,
                Object>) nodesModified.get(key)).get("root"))).findFirst().orElse(null);
        if (StringUtils.isBlank(rootId))
            throw new ClientException("ERR_INVALID_ROOT_ID", "Please Provide Valid Root Node Identifier");
        return rootId;
    }


    private void getRelationDefMaps(DefinitionDTO definition, Map<String, RelationDefinition> inRelDefMap,
                                    Map<String, RelationDefinition> outRelDefMap) {
        if (null != definition) {
            if (null != definition.getInRelations() && !definition.getInRelations().isEmpty()) {
                for (RelationDefinition rDef : definition.getInRelations()) {
                    if (StringUtils.isNotBlank(rDef.getTitle()) && null != rDef.getObjectTypes())
                        inRelDefMap.put(rDef.getTitle(), rDef);
                }
            }
            if (null != definition.getOutRelations() && !definition.getOutRelations().isEmpty()) {
                for (RelationDefinition rDef : definition.getOutRelations()) {
                    if (StringUtils.isNotBlank(rDef.getTitle()) && null != rDef.getObjectTypes())
                        outRelDefMap.put(rDef.getTitle(), rDef);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Response createNodeObject(Entry<String, Object> entry, Map<String, String> idMap,
                                      Map<String, Node> nodeMap, Map<String, String> newIdMap, DefinitionDTO definition,
                                      Map<String, RelationDefinition> inRelDefMap, Map<String, RelationDefinition> outRelDefMap) {
        String nodeId = entry.getKey();
        String id = nodeId;
        Node tmpnode = null;
        Map<String, Object> map = (Map<String, Object>) entry.getValue();
        Boolean isNew = (Boolean) map.get("isNew");
        if (BooleanUtils.isTrue(isNew)) {
            id = Identifier.getIdentifier(TAXONOMY_ID, Identifier.getUniqueIdFromTimestamp());
            newIdMap.put(nodeId, id);
        } else {
            tmpnode = nodeMap.get(id);
            if (null != tmpnode && StringUtils.isNotBlank(tmpnode.getIdentifier())) {
                id = tmpnode.getIdentifier();
            } else {
                throw new ResourceNotFoundException("ERR_CONTENT_NOT_FOUND",
                        "Content not found with identifier: " + id);
            }
        }
        idMap.put(nodeId, id);
        Map<String, Object> metadata = Optional.ofNullable(map.get("metadata")).map(e -> (Map<String, Object>) e).orElse(new HashMap<String, Object>());

        if (metadata.containsKey("dialcodes")) {
            metadata.remove("dialcodes");
        }
        metadata.put("identifier", id);
        metadata.put("objectType", CONTENT_OBJECT_TYPE);
        if (BooleanUtils.isTrue(isNew)) {
            metadata.put("code", nodeId);
            metadata.put(GraphDACParams.versionKey.name(), System.currentTimeMillis() + "");
            metadata.put(AuditProperties.createdOn.name(), DateUtils.formatCurrentDate());
            metadata.put(AuditProperties.lastStatusChangedOn.name(), DateUtils.formatCurrentDate());
            Boolean root = (Boolean) map.get("root");
            if (BooleanUtils.isNotTrue(root))
                metadata.put("visibility", "Parent");
        }
        metadata.put("status", "Draft");
        metadata.put(AuditProperties.lastUpdatedOn.name(), DateUtils.formatCurrentDate());
        Response validateNodeResponse = validateNode(TAXONOMY_ID, nodeId, metadata, tmpnode, definition);
        if (checkError(validateNodeResponse))
            return validateNodeResponse;
        try {
            if(null != tmpnode) {
                tmpnode.getMetadata().putAll(metadata);
                nodeMap.put(id, tmpnode);
            } else {
                Node node = ConvertToGraphNode.convertToGraphNode(metadata, definition, null);
                node.setGraphId(TAXONOMY_ID);
                node.setNodeType(SystemNodeTypes.DATA_NODE.name());
                getRelationsToBeDeleted(node, metadata, inRelDefMap, outRelDefMap);
                nodeMap.put(id, node);
            }

        } catch (Exception e) {
            throw new ClientException("ERR_CREATE_CONTENT_OBJECT", "Error creating content for the node: " + nodeId, e);
        }
        return null;
    }

    private Response validateNode(String graphId, String nodeId, Map<String, Object> metadata, Node tmpnode,
                                  DefinitionDTO definition) {
        Node node = null;
        try {
            node = ConvertToGraphNode.convertToGraphNode(metadata, definition, null);
        } catch (Exception e) {
            throw new ClientException("ERR_CREATE_CONTENT_OBJECT", "Error creating content for the node: " + nodeId, e);
        }
        if (null == tmpnode) {
            tmpnode = new Node();
            tmpnode.setGraphId(graphId);
            tmpnode.setObjectType(CONTENT_OBJECT_TYPE);
        }
        if (null != tmpnode.getMetadata() && !tmpnode.getMetadata().isEmpty()) {
            if (null == node.getMetadata())
                node.setMetadata(tmpnode.getMetadata());
            else {
                for (Entry<String, Object> entry : tmpnode.getMetadata().entrySet()) {
                    if (!node.getMetadata().containsKey(entry.getKey()))
                        node.getMetadata().put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (null == node.getInRelations())
            node.setInRelations(tmpnode.getInRelations());
        if (null == node.getOutRelations())
            node.setOutRelations(tmpnode.getOutRelations());
        Request request = getRequest(graphId, GraphEngineManagers.NODE_MANAGER, "validateNode");
        request.put(GraphDACParams.node.name(), node);
        Response response = getResponse(request);
        return response;
    }

    private void getRelationsToBeDeleted(Node node, Map<String, Object> metadata,
                                         Map<String, RelationDefinition> inRelDefMap, Map<String, RelationDefinition> outRelDefMap) {
        if (null != metadata) {
            List<Relation> inRelations = node.getInRelations();
            if (null == inRelations)
                inRelations = new ArrayList<Relation>();
            List<Relation> outRelations = node.getOutRelations();
            if (null == outRelations)
                outRelations = new ArrayList<Relation>();
            for (Entry<String, Object> entry : metadata.entrySet()) {
                if (inRelDefMap.containsKey(entry.getKey())) {
                    RelationDefinition rDef = inRelDefMap.get(entry.getKey());
                    List<String> objectTypes = rDef.getObjectTypes();
                    if (null != objectTypes) {
                        for (String objectType : objectTypes) {
                            Relation dummyInRelation = new Relation(null, rDef.getRelationName(), node.getIdentifier());
                            dummyInRelation.setStartNodeObjectType(objectType);
                            inRelations.add(dummyInRelation);
                        }
                    }
                } else if (outRelDefMap.containsKey(entry.getKey())) {
                    RelationDefinition rDef = outRelDefMap.get(entry.getKey());
                    List<String> objectTypes = rDef.getObjectTypes();
                    if (null != objectTypes) {
                        for (String objectType : objectTypes) {
                            Relation dummyOutRelation = new Relation(node.getIdentifier(), rDef.getRelationName(),
                                    null);
                            dummyOutRelation.setEndNodeObjectType(objectType);
                            outRelations.add(dummyOutRelation);
                        }
                    }
                }
            }
            if (!inRelations.isEmpty())
                node.setInRelations(inRelations);
            if (!outRelations.isEmpty())
                node.setOutRelations(outRelations);
        }
    }
}
