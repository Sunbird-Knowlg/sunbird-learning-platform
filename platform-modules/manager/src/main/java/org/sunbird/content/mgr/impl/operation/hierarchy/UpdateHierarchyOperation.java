package org.sunbird.content.mgr.impl.operation.hierarchy;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.mgr.ConvertGraphNode;
import org.sunbird.common.mgr.ConvertToGraphNode;
import org.sunbird.graph.common.DateUtils;
import org.sunbird.graph.common.Identifier;
import org.sunbird.graph.dac.enums.AuditProperties;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.graph.model.node.RelationDefinition;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.learning.common.enums.ContentErrorCodes;
import org.sunbird.learning.hierarchy.store.HierarchyStore;
import org.sunbird.taxonomy.mgr.impl.BaseContentManager;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class UpdateHierarchyOperation extends BaseContentManager {

    private HierarchyStore hierarchyStore = new HierarchyStore();

    public Response updateHierarchy(Map<String, Object> data) {
        if (MapUtils.isEmpty(data)) {
            throw new ClientException("ERR_INVALID_HIERARCHY_DATA", "Hierarchy data is empty");
        } else {
            Map<String, Object> nodesModified = (Map<String, Object>) data.get(ContentAPIParams.nodesModified.name());
            Map<String, Object> hierarchyData = (Map<String, Object>) data.get(ContentAPIParams.hierarchy.name());
            if(MapUtils.isEmpty(nodesModified) && MapUtils.isEmpty(hierarchyData))
                throw new ClientException("ERR_INVALID_HIERARCHY_DATA", "Hierarchy data is empty");

            String rootId = getRootId(nodesModified, hierarchyData);
            Map<String, String> idMap = new HashMap<>();
            Map<String, Object> hierarchyResponse = hierarchyStore.getHierarchy(rootId +DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX);

            Response response = getUpdatedHierarchy(rootId, nodesModified, hierarchyData,
                    hierarchyResponse,
                    idMap);
            return response;
        }

    }

    private Response getUpdatedHierarchy(String rootId, Map<String, Object> nodesModified, Map<String, Object>
            hierarchyData, Map<String, Object> hierarchyResponse, Map<String, String> idMap) {
        DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
        List<Node> nodeList = getNodeMapFromHierarchy(hierarchyResponse, definition, rootId);
        Map<String, RelationDefinition> inRelDefMap = new HashMap<>();
        Map<String, RelationDefinition> outRelDefMap = new HashMap<>();
        getRelationDefMaps(definition, inRelDefMap, outRelDefMap);
        updateNodesModified(nodesModified, idMap, nodeList, definition, inRelDefMap, outRelDefMap, rootId);
        List<Map<String, Object>> children = prepareHierarchy(nodeList, rootId, hierarchyData,
                definition, idMap);

        Map<String, Object> data = new HashMap<String, Object>() {{
            put(ContentAPIParams.identifier.name(), rootId);
            put(ContentAPIParams.children.name(), children);
        }};

        //Adding to remove outrelation while updating node in update hierarchy
        Node node = getTempNode(nodeList, rootId);
        node.setOutRelations(null);
        Response rootNodeResponse = updateDataNode(node);
        if(checkError(rootNodeResponse)) {
            return rootNodeResponse;
        }
        hierarchyStore.saveOrUpdateHierarchy(rootId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX, data);
        Response response = OK();
        response.put(ContentAPIParams.content_id.name(), rootId);
        response.put(ContentAPIParams.identifiers.name(), idMap);

        return response;
    }

    private List<Map<String,Object>> prepareHierarchy(List<Node> nodeList, String rootId, Map<String, Object> hierarchyData, DefinitionDTO definition, Map<String, String> idMap) {
        List<Map<String,Object>> contentList = new ArrayList<>();
        if(MapUtils.isNotEmpty(hierarchyData)) {
            Map<String, List<String>> childIdMap = hierarchyData.keySet().stream().collect(Collectors.toMap((key)
                    -> ((null != idMap.get(key) ? idMap.get(key) : key))
            ,(key) ->  ((List<String>) ((Map<String, Object>)hierarchyData.get(key)).get("children")).stream().map
                                (id -> (null != idMap.get(id) ? idMap.get(id) : id)).collect(toList())));

            Set<String> childNodes = new HashSet<>();
            List<Node> updatedNodeList = new ArrayList<Node>() {{
                add(getTempNode(nodeList, rootId));
            }};
            updateNodeList(updatedNodeList, rootId, new HashMap<String, Object>(){{
                put(ContentAPIParams.depth.name(), 0);
            }});
            updateDepthIndexParent(childIdMap.get(rootId), 1, rootId, nodeList, childIdMap, childNodes, updatedNodeList);
            updateNodeList(nodeList, rootId, new HashMap<String, Object>(){{
                put(ContentAPIParams.childNodes.name(), new ArrayList<String>(childNodes));
                put(ContentAPIParams.depth.name(), 0);
            }});
            contentList = getContentList(updatedNodeList, definition);
        } else {
            updateNodeList(nodeList, rootId, new HashMap<String, Object>(){{ put(ContentAPIParams.depth.name(), 0);}});
            contentList = getContentList(nodeList, definition);
        }
        List<Map<String,Object>> filteredContentList = contentList.stream().filter(content -> (null != content.get("depth"))).collect(toList());
        Map<String, Object> collectionHierarchy = util.constructHierarchy(filteredContentList);
        util.hierarchyCleanUp(collectionHierarchy);
        return (List<Map<String, Object>>) collectionHierarchy.get(ContentAPIParams.children.name());
    }

    private List<Map<String,Object>> getContentList(List<Node> nodeList, DefinitionDTO definition) {
       return  nodeList.stream().map(node -> {
           Map<String, Object> contentMap = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID,
                definition, null);
           contentMap.remove("collections");
           contentMap.remove("children");
           contentMap.remove("usedByContent");
           contentMap.remove("item_sets");
           contentMap.remove("methods");
           contentMap.remove("libraries");
           contentMap.remove("editorState");
           String id = (String) contentMap.get(ContentAPIParams.identifier.name());
           contentMap.put(ContentAPIParams.identifier.name(), id.replace(".img",""));
           return contentMap;
       }).collect(toList());
    }

    private void updateDepthIndexParent(List<String> childrenIds, int depth, String parent, List<Node> nodeList, Map<String, List<String>> hierarchy, Set<String> childNodes, List<Node> updatedNodeList) {
        int index =1;
        for(String childId: childrenIds) {
            Node tmpNode = getTempNode(nodeList, childId);

            if(null != tmpNode && StringUtils.equalsIgnoreCase("Parent", (String) tmpNode.getMetadata().get(ContentAPIParams.visibility.name()))) {
                tmpNode.getMetadata().put(ContentAPIParams.depth.name(), depth);
                tmpNode.getMetadata().put(ContentAPIParams.parent.name(), parent);
                tmpNode.getMetadata().put(ContentAPIParams.index.name(), index);
                updatedNodeList.add(tmpNode);
            } else {
                tmpNode = getContentNode(TAXONOMY_ID, childId, null);
                tmpNode.getMetadata().put(ContentAPIParams.depth.name(), depth);
                tmpNode.getMetadata().put(ContentAPIParams.parent.name(), parent);
                tmpNode.getMetadata().put(ContentAPIParams.index.name(), index);
                updatedNodeList.add(tmpNode);
            }
            childNodes.add(childId);
            index +=1;

            if (CollectionUtils.isNotEmpty(hierarchy.get(childId))) {
                updateDepthIndexParent(hierarchy.get(childId), (((Integer) tmpNode.getMetadata().get(ContentAPIParams.depth.name())) + 1),
                        childId, nodeList, hierarchy, childNodes, updatedNodeList);
            }
        }

    }

    private void updateNodesModified(Map<String, Object> nodesModified, Map<String, String> idMap, List<Node> nodeList, DefinitionDTO definition, Map<String, RelationDefinition> inRelDefMap, Map<String,
            RelationDefinition> outRelDefMap, String rootId) {
        if(MapUtils.isNotEmpty((Map<String, Object>)nodesModified.get(rootId)) && MapUtils.isNotEmpty((Map<String, Object>) ((Map<String, Object>)nodesModified.get(rootId))
                .get("metadata"))){
        		Map<String, Object> metadata = (Map<String, Object>) ((Map<String, Object>)nodesModified.get(rootId))
            .get("metadata");
        		metadata.remove(ContentAPIParams.versionKey.name());
                updateNodeList(nodeList, rootId, metadata);
        }
        nodesModified.remove(rootId);
        nodesModified.entrySet().forEach(entry -> {
            Map<String, Object> map = (Map<String, Object>) entry.getValue();
            createNodeObject(entry, idMap, nodeList, new HashMap<>(), definition,
                    inRelDefMap, outRelDefMap);
        });
    }


    private List<Node> getNodeMapFromHierarchy(Map<String, Object> hierarchyResponse, DefinitionDTO definition, String
            rootId) {
        List<Node> nodeList = new ArrayList<>();
        Node rootNode = getNodeForOperation(rootId, "updateHierarchy");
        if(!StringUtils.equalsIgnoreCase(COLLECTION_MIME_TYPE , (String) rootNode.getMetadata().get(ContentAPIParams
                .mimeType.name()))) {
            TelemetryManager.error("UpdateHierarchyOperation.getNodeMapFromHierarchy() :: invalid mimeType for root " +
                    "id : " + rootId);
            throw new ClientException("ERR_INVALID_ROOT_ID", "Invalid MimeType for Root Node Identifier  : " + rootId);
        }

        if(null == rootNode.getMetadata().get("version") || ((Number)rootNode.getMetadata().get("version")).intValue() < 2) {
            TelemetryManager.error("UpdateHierarchyOperation.getNodeMapFromHierarchy() :: invalid content version for root " +
                    "id : " + rootId);
            throw new ClientException("ERR_INVALID_ROOT_ID", "The collection version is not up to date " + rootId);
        }

        rootNode.getMetadata().put(ContentAPIParams.version.name(), LATEST_CONTENT_VERSION);
        nodeList.add(rootNode);
        if (MapUtils.isNotEmpty(hierarchyResponse)) {
            List<Map<String, Object>> children = (List<Map<String, Object>>) hierarchyResponse
                    .get("children");
            getNodeMap(children, nodeList, definition);
            return nodeList;
        }
        return nodeList;
    }

    private void getNodeMap(List<Map<String, Object>> children, List<Node> nodeList, DefinitionDTO definition) {
        if (CollectionUtils.isNotEmpty(children)) {
            children.forEach(child -> {
                Node node = null;
                try {
                    if(StringUtils.equalsIgnoreCase("Default", (String) child.get(ContentAPIParams.visibility.name()))) {
                        node = getContentNode(TAXONOMY_ID, (String) child.get(ContentAPIParams.identifier.name()), null);
                        node.getMetadata().put(ContentAPIParams.depth.name(), child.get(ContentAPIParams.depth.name()));
                        node.getMetadata().put(ContentAPIParams.parent.name(), child.get(ContentAPIParams.parent.name()));
                        node.getMetadata().put(ContentAPIParams.index.name(), child.get(ContentAPIParams.index.name()));
                    }else {
                        Map<String, Object> childData = new HashMap<>();
                        childData.putAll(child);
                        childData.remove(ContentAPIParams.children.name());
                        node = ConvertToGraphNode.convertToGraphNode(childData, definition, null);
                    }
                    nodeList.add(node);
                } catch (Exception e) {
                    TelemetryManager.error("UpdateHierarchyOperation.getNodeMap() :: Error which converting to nodeMap ", e);
                }
                getNodeMap((List<Map<String, Object>>) child.get(ContentAPIParams.children.name()), nodeList, definition);
            });
        }
    }

    private String getRootId(Map<String, Object> nodesModified, Map<String, Object> hierarchyData) {
        String rootId = nodesModified.keySet().stream().filter(key -> BooleanUtils.isTrue((Boolean) ((Map<String,
                Object>) nodesModified.get(key)).get(ContentAPIParams.root.name()))).findFirst().orElse(null);

        if (StringUtils.isBlank(rootId)){
            if(MapUtils.isNotEmpty(hierarchyData)){
                rootId = hierarchyData.keySet().stream().filter(key -> BooleanUtils.isTrue((Boolean) ((Map<String,
                        Object>) hierarchyData.get(key)).get(ContentAPIParams.root.name()))).findFirst().orElse(null);
            }
            if (StringUtils.isBlank(rootId))
                throw new ClientException("ERR_INVALID_ROOT_ID", "Please Provide Valid Root Node Identifier");
        }
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
                                      List<Node> nodeList, Map<String, String> newIdMap, DefinitionDTO definition,
                                      Map<String, RelationDefinition> inRelDefMap, Map<String, RelationDefinition> outRelDefMap) {
        String nodeId = entry.getKey();
        String id = nodeId;
        Node tmpnode = null;
        Map<String, Object> map = (Map<String, Object>) entry.getValue();
        if (BooleanUtils.isTrue((Boolean) map.get(ContentAPIParams.isNew.name()))) {
            id = Identifier.getIdentifier(TAXONOMY_ID, Identifier.getUniqueIdFromTimestamp());
            newIdMap.put(nodeId, id);
        } else {
            tmpnode = getTempNode(nodeList, id);
            if (null != tmpnode && StringUtils.isNotBlank(tmpnode.getIdentifier())) {
                id = tmpnode.getIdentifier();
            } else {
                throw new ResourceNotFoundException("ERR_CONTENT_NOT_FOUND",
                        "Content not found with identifier: " + id);
            }
        }
        idMap.put(nodeId, id);
        Map<String, Object> metadata = Optional.ofNullable(map.get(ContentAPIParams.metadata.name())).map(e -> (Map<String, Object>) e).orElse(new HashMap<String, Object>());

        if (metadata.containsKey(ContentAPIParams.dialcodes.name())) {
            metadata.remove(ContentAPIParams.dialcodes.name());
        }
        metadata.put(ContentAPIParams.identifier.name(), id);
        metadata.put(ContentAPIParams.objectType.name(), CONTENT_OBJECT_TYPE);
        if (BooleanUtils.isTrue((Boolean) map.get(ContentAPIParams.isNew.name()))) {
            metadata.put(ContentAPIParams.code.name(), nodeId);
            metadata.put(GraphDACParams.versionKey.name(), System.currentTimeMillis() + "");
            metadata.put(AuditProperties.createdOn.name(), DateUtils.formatCurrentDate());
            metadata.put(AuditProperties.lastStatusChangedOn.name(), DateUtils.formatCurrentDate());
            if (BooleanUtils.isNotTrue((Boolean) map.get(ContentAPIParams.root.name())))
                metadata.put(ContentAPIParams.visibility.name(), "Parent");
        }
        metadata.put(ContentAPIParams.status.name(), "Draft");
        metadata.put(AuditProperties.lastUpdatedOn.name(), DateUtils.formatCurrentDate());
        Response validateNodeResponse = validateNode(TAXONOMY_ID, nodeId, metadata, tmpnode, definition);
        if (checkError(validateNodeResponse)){
            TelemetryManager.error("Validation Error for ID : " + id + " :: " + validateNodeResponse.getParams().getErr() +" :: " + validateNodeResponse.getParams().getErrmsg() + " :: "+ validateNodeResponse.getResult());
            throw new ClientException(validateNodeResponse.getParams().getErr(), validateNodeResponse.getParams().getErrmsg() + " " + validateNodeResponse.getResult());
        }
        try {
            if(null != tmpnode) {
                updateNodeList(nodeList, id, metadata);
            } else {
                Node node = ConvertToGraphNode.convertToGraphNode(metadata, definition, null);
                node.setGraphId(TAXONOMY_ID);
                node.setObjectType(CONTENT_OBJECT_TYPE);
                node.setNodeType(SystemNodeTypes.DATA_NODE.name());
                getRelationsToBeDeleted(node, metadata, inRelDefMap, outRelDefMap);
                nodeList.add(node);
            }
        } catch (Exception e) {
            TelemetryManager.error("Error creating content for the node: " + nodeId, e);
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

        node.getMetadata().remove(ContentAPIParams.depth.name());
        node.getMetadata().remove(ContentAPIParams.parent.name());
        node.getMetadata().remove(ContentAPIParams.index.name());
        node.getMetadata().remove("sYS_INTERNAL_LAST_UPDATED_ON");
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

    private Node getTempNode(List<Node> nodeList, String id) {
        List<Node> tempList = nodeList.stream().filter(node -> StringUtils.startsWith(node.getIdentifier(), id)).collect(toList());
        if(CollectionUtils.isNotEmpty(tempList)){
            return tempList.get(0);
        }
        return null;
    }


    private void updateNodeList(List<Node> nodeList, String id, Map<String, Object> metadata) {
        nodeList.forEach(node -> {
            if(node.getIdentifier().startsWith(id)){
                node.getMetadata().putAll(metadata);
            }
        });

    }

}
