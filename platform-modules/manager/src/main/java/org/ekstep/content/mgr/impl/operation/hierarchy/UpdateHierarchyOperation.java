package org.ekstep.content.mgr.impl.operation.hierarchy;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.graph.common.DateUtils;
import org.ekstep.graph.common.Identifier;
import org.ekstep.graph.dac.enums.AuditProperties;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.RelationTypes;
import org.ekstep.graph.dac.enums.SystemNodeTypes;
import org.ekstep.graph.dac.enums.SystemProperties;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.model.node.RelationDefinition;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class UpdateHierarchyOperation extends BaseContentManager {

    @SuppressWarnings("unchecked")
    public Response updateHierarchy(Map<String, Object> data) {
        String graphId = TAXONOMY_ID;
        if (null != data && !data.isEmpty()) {
            Map<String, Object> modifiedNodes = (Map<String, Object>) data.get("nodesModified");
            Map<String, Object> hierarchy = (Map<String, Object>) data.get("hierarchy");
            Map<String, String> idMap = new HashMap<String, String>();
            Map<String, String> newIdMap = new HashMap<String, String>();
            Map<String, Node> nodeMap = new HashMap<String, Node>();
            String rootNodeId = null;
            if (null != modifiedNodes && !modifiedNodes.isEmpty()) {
                DefinitionDTO definition = getDefinition(graphId, CONTENT_OBJECT_TYPE);
                Map<String, RelationDefinition> inRelDefMap = new HashMap<String, RelationDefinition>();
                Map<String, RelationDefinition> outRelDefMap = new HashMap<String, RelationDefinition>();
                getRelationDefMaps(definition, inRelDefMap, outRelDefMap);
                for (Entry<String, Object> entry : modifiedNodes.entrySet()) {
                    Map<String, Object> map = (Map<String, Object>) entry.getValue();
                    Response nodeResponse = createNodeObject(graphId, entry, idMap, nodeMap, newIdMap, definition,
                            inRelDefMap, outRelDefMap);
                    if (null != nodeResponse)
                        return nodeResponse;
                    Boolean root = (Boolean) map.get("root");
                    if (BooleanUtils.isTrue(root))
                        rootNodeId = idMap.get(entry.getKey());
                }
            }
            if (null != hierarchy && !hierarchy.isEmpty()) {
                for (Entry<String, Object> entry : hierarchy.entrySet()) {
                    updateNodeHierarchyRelations(graphId, entry, idMap, nodeMap);
                    if (StringUtils.isBlank(rootNodeId)) {
                        Map<String, Object> map = (Map<String, Object>) entry.getValue();
                        Boolean root = (Boolean) map.get("root");
                        if (BooleanUtils.isTrue(root))
                            rootNodeId = idMap.get(entry.getKey());
                    }
                }
            }
            if (null != nodeMap && !nodeMap.isEmpty()) {
                // Any change for the hierarchy structure of the graph should update rootNode versionKey.
                String versionKey = "";
                if(StringUtils.isNotBlank(rootNodeId)) {
                    Node rootNode = nodeMap.get(rootNodeId);
                    versionKey = System.currentTimeMillis() + "";
                    rootNode.getMetadata().put(GraphDACParams.versionKey.name(), versionKey);
                } else {
                    throw new ClientException("ERR_INVALID_ROOT_ID", "Please Provide Valid Root Node Identifier");
                }
                List<Node> nodes = new ArrayList<Node>(nodeMap.values());
                Request request = getRequest(graphId, GraphEngineManagers.GRAPH_MANAGER, "bulkUpdateNodes");
                request.put(GraphDACParams.nodes.name(), nodes);
                TelemetryManager.log("Sending bulk update request | Total nodes: " + nodes.size());
                Response response = getResponse(request);
                if (StringUtils.isNotBlank(rootNodeId)) {
                    if (StringUtils.endsWithIgnoreCase(rootNodeId, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX))
                        rootNodeId = rootNodeId.replace(DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX, "");
                    response.put(ContentAPIParams.content_id.name(), rootNodeId);
                    if (StringUtils.isNotBlank(versionKey))
                        response.put(GraphDACParams.versionKey.name(), versionKey);
                }
                if (null != newIdMap && !newIdMap.isEmpty())
                    response.put(ContentAPIParams.identifiers.name(), newIdMap);
                return response;
            }
        } else {
            throw new ClientException("ERR_INVALID_HIERARCHY_DATA", "Hierarchy data is empty");
        }
        return new Response();
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
    private Response createNodeObject(String graphId, Entry<String, Object> entry, Map<String, String> idMap,
                                      Map<String, Node> nodeMap, Map<String, String> newIdMap, DefinitionDTO definition,
                                      Map<String, RelationDefinition> inRelDefMap, Map<String, RelationDefinition> outRelDefMap) {
        String nodeId = entry.getKey();
        String id = nodeId;
        String objectType = CONTENT_OBJECT_TYPE;
        Node tmpnode = null;
        Map<String, Object> map = (Map<String, Object>) entry.getValue();
        Boolean isNew = (Boolean) map.get("isNew");
        if (BooleanUtils.isTrue(isNew)) {
            id = Identifier.getIdentifier(graphId, Identifier.getUniqueIdFromTimestamp());
            newIdMap.put(nodeId, id);
        } else {
            tmpnode = getNodeForOperation(id, "create");
            if (null != tmpnode && StringUtils.isNotBlank(tmpnode.getIdentifier())) {
                id = tmpnode.getIdentifier();
                objectType = tmpnode.getObjectType();
            } else {
                throw new ResourceNotFoundException("ERR_CONTENT_NOT_FOUND",
                        "Content not found with identifier: " + id);
            }
        }
        idMap.put(nodeId, id);
        Map<String, Object> metadata = Optional.ofNullable(map.get("metadata")).map(e -> (Map<String, Object>) e).orElse(new HashMap<String, Object>()) ;

        if (metadata.containsKey("dialcodes")) {
            metadata.remove("dialcodes");
        }
        metadata.put("identifier", id);
        metadata.put("objectType", objectType);
        if (BooleanUtils.isTrue(isNew)) {
            metadata.put("isNew", true);
            metadata.put("code", nodeId);
            metadata.put("status", "Draft");
            metadata.put(GraphDACParams.versionKey.name(), System.currentTimeMillis() + "");
            metadata.put(AuditProperties.createdOn.name(), DateUtils.formatCurrentDate());
            metadata.put(AuditProperties.lastStatusChangedOn.name(), DateUtils.formatCurrentDate());
            Boolean root = (Boolean) map.get("root");
            if (BooleanUtils.isNotTrue(root))
                metadata.put("visibility", "Parent");
        }
        metadata.put(AuditProperties.lastUpdatedOn.name(), DateUtils.formatCurrentDate());
        Response validateNodeResponse = validateNode(graphId, nodeId, metadata, tmpnode, definition);
        if (checkError(validateNodeResponse))
            return validateNodeResponse;
        try {
            Node node = ConvertToGraphNode.convertToGraphNode(metadata, definition, null);
            node.setGraphId(graphId);
            node.setNodeType(SystemNodeTypes.DATA_NODE.name());
            getRelationsToBeDeleted(node, metadata, inRelDefMap, outRelDefMap);
            nodeMap.put(id, node);
        } catch (Exception e) {
            throw new ClientException("ERR_CREATE_CONTENT_OBJECT", "Error creating content for the node: " + nodeId, e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void updateNodeHierarchyRelations(String graphId, Entry<String, Object> entry, Map<String, String> idMap,
                                              Map<String, Node> nodeMap) {
        String nodeId = entry.getKey();
        String id = idMap.get(nodeId);
        if (StringUtils.isBlank(id)) {
            Map<String, Object> map = (Map<String, Object>) entry.getValue();
            Boolean root = (Boolean) map.get("root");
            Node tmpnode = getNodeForUpdateHierarchy(graphId, nodeId, "update", root);
            if (null != tmpnode) {
                id = tmpnode.getIdentifier();
                tmpnode.setOutRelations(null);
                tmpnode.setInRelations(null);
                String visibility = (String) tmpnode.getMetadata().get("visibility");
                if (StringUtils.equalsIgnoreCase("Parent", visibility) || BooleanUtils.isTrue(root)) {
                    idMap.put(nodeId, id);
                    nodeMap.put(id, tmpnode);
                }
            } else {
                throw new ResourceNotFoundException("ERR_CONTENT_NOT_FOUND",
                        "Content not found with identifier: " + id);
            }
        }
        if (StringUtils.isNotBlank(id)) {
            Node node = nodeMap.get(id);
            if (null != node) {
                Map<String, Object> map = (Map<String, Object>) entry.getValue();
                List<String> children = (List<String>) map.get("children");
                children = children.stream().distinct().collect(toList());
                if (null != children) {
                    List<Relation> outRelations = node.getOutRelations();
                    if (null == outRelations)
                        outRelations = new ArrayList<Relation>();
                    int index = 1;
                    for (String childId : children) {
                        if (idMap.containsKey(childId)) {
                            childId = idMap.get(childId);
                        } else {
                            Node nodeForRelation = getNodeForUpdateHierarchy(graphId, childId, "update", false);
                            childId = nodeForRelation.getIdentifier();
                        }

                        Relation rel = new Relation(id, RelationTypes.SEQUENCE_MEMBERSHIP.relationName(), childId);
                        Map<String, Object> metadata = new HashMap<String, Object>();
                        metadata.put(SystemProperties.IL_SEQUENCE_INDEX.name(), index);
                        index += 1;
                        rel.setMetadata(metadata);
                        outRelations.add(rel);
                    }
                    Relation dummyContentRelation = new Relation(id, RelationTypes.SEQUENCE_MEMBERSHIP.relationName(),
                            null);
                    dummyContentRelation.setEndNodeObjectType(CONTENT_OBJECT_TYPE);
                    outRelations.add(dummyContentRelation);
                    Relation dummyContentImageRelation = new Relation(id,
                            RelationTypes.SEQUENCE_MEMBERSHIP.relationName(), null);
                    dummyContentImageRelation.setEndNodeObjectType(CONTENT_IMAGE_OBJECT_TYPE);
                    outRelations.add(dummyContentImageRelation);
                    node.setOutRelations(outRelations);
                }
            }
        }
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

    // Method is introduced to decide whether image node should be created for
    // the content or not.
    private Node getNodeForUpdateHierarchy(String taxonomyId, String contentId, String operation, boolean isRoot) {
        Response response;
        if (isRoot)
            return getNodeForOperation(contentId, operation);
        else {
            response = getDataNode(taxonomyId, contentId);

            TelemetryManager.log("Checking for Fetched Content Node (Not Image Node) for Content Id: " + contentId);
            if (checkError(response)) {
                throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
                        "Error! While Fetching the Content for Operation | [Content Id: " + contentId + "]");
            } else {
                Node node = (Node) response.get(GraphDACParams.node.name());
                if ("Parent".equalsIgnoreCase(node.getMetadata().get("visibility").toString())) {
                    return getNodeForOperation(contentId, operation);
                } else {
                    return node;
                }
            }
        }
    }

}
