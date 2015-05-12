package com.ilimi.graph.dac.mgr.impl;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;
import static com.ilimi.graph.dac.util.Neo4jGraphUtil.getNodeByUniqueId;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.tooling.GlobalGraphOperations;

import akka.actor.ActorRef;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.BaseValueObjectMap;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.exception.GraphEngineErrorCodes;
import com.ilimi.graph.common.exception.ResourceNotFoundException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;
import com.ilimi.graph.dac.mgr.IGraphDACGraphMgr;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.dac.util.Neo4jGraphUtil;
import com.ilimi.graph.dac.util.RelationType;
import com.ilimi.graph.importer.ImportData;

public class GraphDACGraphMgrImpl extends BaseGraphManager implements IGraphDACGraphMgr {

    protected void invokeMethod(Request request, ActorRef parent) {
        String methodName = request.getOperation();
        try {
            Method method = GraphDACActorPoolMgr.getMethod(GraphDACManagers.DAC_GRAPH_MANAGER, methodName);
            if (null == method) {
                throw new ClientException(GraphDACErrorCodes.ERR_GRAPH_INVALID_OPERATION.name(), "Operation '" + methodName + "' not found");
            } else {
                method.invoke(this, request);
            }
        } catch (Exception e) {
            ERROR(e, parent);
        }
    }

    @Override
    public void createGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        if (StringUtils.isBlank(graphId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_GRAPH_ID.name(), "Graph Id cannot be blank");
        } else if (Neo4jGraphFactory.graphExists(graphId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GRAPH_ALREADY_EXISTS.name(), "Graph '" + graphId + "' already exists");
        } else {
            Transaction tx = null;
            try {
                Neo4jGraphFactory.createGraph(graphId);
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Schema schema = graphDb.schema();
                schema.indexFor(NODE_LABEL).on(SystemProperties.IL_SYS_NODE_TYPE.name()).create();
                schema.indexFor(NODE_LABEL).on(SystemProperties.IL_FUNC_OBJECT_TYPE.name()).create();
                schema.constraintFor(NODE_LABEL).assertPropertyIsUnique(SystemProperties.IL_UNIQUE_ID.name()).create();
                tx.success();
                OK(GraphDACParams.GRAPH_ID.name(), new StringValue(graphId), getSender());
            } catch (Exception e) {
                if (null != tx)
                    tx.failure();
                ERROR(e, getSender());
            } finally {
                if (null != tx)
                    tx.close();
            }
        }
    }

    @Override
    public void deleteGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        if (StringUtils.isBlank(graphId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_GRAPH_ID.name(), "Graph Id cannot be blank");
        } else if (!Neo4jGraphFactory.graphExists(graphId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GRAPH_NOT_FOUND.name(), "Graph '" + graphId + "' not found to delete.");
        } else {
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                if (null != graphDb) {
                    Neo4jGraphFactory.shutdownGraph(graphId);
                }
                Neo4jGraphFactory.deleteGraph(graphId);
                OK(GraphDACParams.GRAPH_ID.name(), new StringValue(graphId), getSender());
            } catch (Exception e) {
                ERROR(e, getSender());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void addOutgoingRelations(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue startNodeId = (StringValue) request.get(GraphDACParams.START_NODE_ID.name());
        StringValue relationType = (StringValue) request.get(GraphDACParams.RELATION_TYPE.name());
        BaseValueObjectList<StringValue> endNodeIds = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.END_NODE_ID.name());
        if (!validateRequired(startNodeId, relationType, endNodeIds)) {
            throw new ClientException(GraphDACErrorCodes.ERR_CREATE_RELATION_MISSING_REQ_PARAMS.name(), "Required Parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Node startNode = getNodeByUniqueId(graphDb, startNodeId.getId());
                RelationType relType = new RelationType(relationType.getId());
                for (StringValue endNodeId : endNodeIds.getValueObjectList()) {
                    Relationship relation = Neo4jGraphUtil.getRelationship(graphDb, startNodeId.getId(), relationType.getId(),
                            endNodeId.getId());
                    if (null == relation) {
                        Node endNode = getNodeByUniqueId(graphDb, endNodeId.getId());
                        startNode.createRelationshipTo(endNode, relType);
                    }
                }
                tx.success();
                tx.close();
                OK(getSender());
            } catch (Exception e) {
                if (null != tx) {
                    tx.failure();
                    tx.close();
                }
                ERROR(e, getSender());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void addIncomingRelations(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        BaseValueObjectList<StringValue> startNodeIds = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.START_NODE_ID.name());
        StringValue relationType = (StringValue) request.get(GraphDACParams.RELATION_TYPE.name());
        StringValue endNodeId = (StringValue) request.get(GraphDACParams.END_NODE_ID.name());
        if (!validateRequired(startNodeIds, relationType, endNodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_CREATE_RELATION_MISSING_REQ_PARAMS.name(), "Required Parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Node endNode = getNodeByUniqueId(graphDb, endNodeId.getId());
                RelationType relType = new RelationType(relationType.getId());
                for (StringValue startNodeId : startNodeIds.getValueObjectList()) {
                    Relationship relation = Neo4jGraphUtil.getRelationship(graphDb, startNodeId.getId(), relationType.getId(),
                            endNodeId.getId());
                    if (null == relation) {
                        Node startNode = getNodeByUniqueId(graphDb, startNodeId.getId());
                        startNode.createRelationshipTo(endNode, relType);
                    }
                }
                tx.success();
                tx.close();
                OK(getSender());
            } catch (Exception e) {
                if (null != tx) {
                    tx.failure();
                    tx.close();
                }
                ERROR(e, getSender());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addRelation(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue startNodeId = (StringValue) request.get(GraphDACParams.START_NODE_ID.name());
        StringValue relationType = (StringValue) request.get(GraphDACParams.RELATION_TYPE.name());
        StringValue endNodeId = (StringValue) request.get(GraphDACParams.END_NODE_ID.name());
        BaseValueObjectMap<Object> metadata = (BaseValueObjectMap<Object>) request.get(GraphDACParams.METADATA.name());
        if (!validateRequired(startNodeId, relationType, endNodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_CREATE_RELATION_MISSING_REQ_PARAMS.name(), "Required Parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                RelationType relation = new RelationType(relationType.getId());
                Node startNode = getNodeByUniqueId(graphDb, startNodeId.getId());
                Relationship dbRel = null;
                Iterable<Relationship> relations = startNode.getRelationships(Direction.OUTGOING, relation);
                if (null != relations) {
                    for (Relationship rel : relations) {
                        Object relEndNodeId = rel.getEndNode().getProperty(SystemProperties.IL_UNIQUE_ID.name());
                        String strEndNodeId = (null == relEndNodeId) ? null : relEndNodeId.toString();
                        if (StringUtils.equals(endNodeId.getId(), strEndNodeId)) {
                            dbRel = rel;
                            break;
                        }
                    }
                }
                if (null == dbRel) {
                    Node endNode = getNodeByUniqueId(graphDb, endNodeId.getId());
                    Relationship rel = startNode.createRelationshipTo(endNode, relation);
                    if (null != metadata && null != metadata.getBaseValueMap() && metadata.getBaseValueMap().size() > 0) {
                        for (Entry<String, Object> entry : metadata.getBaseValueMap().entrySet()) {
                            rel.setProperty(entry.getKey(), entry.getValue());
                        }
                    }
                } else {
                    if (null != metadata && null != metadata.getBaseValueMap() && metadata.getBaseValueMap().size() > 0) {
                        for (Entry<String, Object> entry : metadata.getBaseValueMap().entrySet()) {
                            dbRel.setProperty(entry.getKey(), entry.getValue());
                        }
                    }
                }
                tx.success();
                tx.close();
                OK(GraphDACParams.GRAPH_ID.name(), new StringValue(graphId), getSender());
            } catch (Exception e) {
                if (null != tx) {
                    tx.failure();
                    tx.close();
                }
                ERROR(e, getSender());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deleteIncomingRelations(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        BaseValueObjectList<StringValue> startNodeIds = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.START_NODE_ID.name());
        StringValue relationType = (StringValue) request.get(GraphDACParams.RELATION_TYPE.name());
        StringValue endNodeId = (StringValue) request.get(GraphDACParams.END_NODE_ID.name());
        if (!validateRequired(startNodeIds, relationType, endNodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DELETE_RELATION_MISSING_REQ_PARAMS.name(), "Required Parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                for (StringValue startNodeId : startNodeIds.getValueObjectList()) {
                    Relationship relation = Neo4jGraphUtil.getRelationship(graphDb, startNodeId.getId(), relationType.getId(),
                            endNodeId.getId());
                    if (null != relation) {
                        relation.delete();
                    }
                }
                tx.success();
                tx.close();
                OK(getSender());
            } catch (Exception e) {
                if (null != tx) {
                    tx.failure();
                    tx.close();
                }
                ERROR(e, getSender());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deleteOutgoingRelations(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue startNodeId = (StringValue) request.get(GraphDACParams.START_NODE_ID.name());
        StringValue relationType = (StringValue) request.get(GraphDACParams.RELATION_TYPE.name());
        BaseValueObjectList<StringValue> endNodeIds = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.END_NODE_ID.name());
        if (!validateRequired(startNodeId, relationType, endNodeIds)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DELETE_RELATION_MISSING_REQ_PARAMS.name(), "Required Parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                for (StringValue endNodeId : endNodeIds.getValueObjectList()) {
                    Relationship relation = Neo4jGraphUtil.getRelationship(graphDb, startNodeId.getId(), relationType.getId(),
                            endNodeId.getId());
                    if (null != relation) {
                        relation.delete();
                    }
                }
                tx.success();
                tx.close();
                OK(getSender());
            } catch (Exception e) {
                if (null != tx) {
                    tx.failure();
                    tx.close();
                }
                ERROR(e, getSender());
            }
        }
    }

    @Override
    public void deleteRelation(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue startNodeId = (StringValue) request.get(GraphDACParams.START_NODE_ID.name());
        StringValue relationType = (StringValue) request.get(GraphDACParams.RELATION_TYPE.name());
        StringValue endNodeId = (StringValue) request.get(GraphDACParams.END_NODE_ID.name());
        if (!validateRequired(startNodeId, relationType, endNodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DELETE_RELATION_MISSING_REQ_PARAMS.name(), "Required Variables are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Relationship rel = Neo4jGraphUtil.getRelationship(graphDb, startNodeId.getId(), relationType.getId(), endNodeId.getId());
                if (null != rel) {
                    rel.delete();
                }
                tx.success();
                tx.close();
                OK(getSender());
            } catch (Exception e) {
                if (null != tx) {
                    tx.failure();
                    tx.close();
                }
                ERROR(e, getSender());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateRelation(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue startNodeId = (StringValue) request.get(GraphDACParams.START_NODE_ID.name());
        StringValue relationType = (StringValue) request.get(GraphDACParams.RELATION_TYPE.name());
        StringValue endNodeId = (StringValue) request.get(GraphDACParams.END_NODE_ID.name());
        BaseValueObjectMap<Object> metadata = (BaseValueObjectMap<Object>) request.get(GraphDACParams.METADATA.name());
        if (!validateRequired(startNodeId, relationType, endNodeId, metadata)) {
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_RELATION_MISSING_REQ_PARAMS.name(), "Required Variables are missing");
        } else if (null != metadata && null != metadata.getBaseValueMap() && metadata.getBaseValueMap().size() > 0) {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Relationship rel = Neo4jGraphUtil.getRelationship(graphDb, startNodeId.getId(), relationType.getId(), endNodeId.getId());
                if (null != rel) {
                    tx.acquireWriteLock(rel);
                    for (Entry<String, Object> entry : metadata.getBaseValueMap().entrySet()) {
                        rel.setProperty(entry.getKey(), entry.getValue());
                    }
                }
                tx.success();
                OK(getSender());
            } catch (Exception e) {
                if (null != tx)
                    tx.failure();
                ERROR(e, getSender());
            } finally {
                if (null != tx)
                    tx.close();
            }
        } else {
            OK(getSender());
        }
    }

    @Override
    public void removeRelationMetadata(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue startNodeId = (StringValue) request.get(GraphDACParams.START_NODE_ID.name());
        StringValue relationType = (StringValue) request.get(GraphDACParams.RELATION_TYPE.name());
        StringValue endNodeId = (StringValue) request.get(GraphDACParams.END_NODE_ID.name());
        StringValue key = (StringValue) request.get(GraphDACParams.PROPERTY_KEY.name());
        if (!validateRequired(startNodeId, relationType, endNodeId, key)) {
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_RELATION_MISSING_REQ_PARAMS.name(), "Required Variables are missing");
        } else if (StringUtils.isNotBlank(key.getId())) {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Relationship rel = Neo4jGraphUtil.getRelationship(graphDb, startNodeId.getId(), relationType.getId(), endNodeId.getId());
                if (null != rel && rel.hasProperty(key.getId()))
                    rel.removeProperty(key.getId());
                tx.success();
                OK(getSender());
            } catch (Exception e) {
                if (null != tx)
                    tx.failure();
                ERROR(e, getSender());
            } finally {
                if (null != tx)
                    tx.close();
            }
        } else {
            OK(getSender());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void createCollection(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue collectionId = (StringValue) request.get(GraphDACParams.COLLECTION_ID.name());
        com.ilimi.graph.dac.model.Node collection = (com.ilimi.graph.dac.model.Node) request.get(GraphDACParams.NODE.name());
        StringValue relationType = (StringValue) request.get(GraphDACParams.RELATION_TYPE.name());
        BaseValueObjectList<StringValue> members = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.MEMBERS.name());
        StringValue indexProperty = (StringValue) request.get(GraphDACParams.INDEX.name());
        if (!validateRequired(collectionId, members)) {
            throw new ClientException(GraphDACErrorCodes.ERR_CREATE_COLLECTION_MISSING_REQ_PARAMS.name(), "Required Variables are missing");
        } else {
            Transaction tx = null;
            try {
                List<String> memberIds = new ArrayList<String>();
                for (StringValue memberId : members.getValueObjectList()) {
                    memberIds.add(memberId.getId());
                }
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                RelationType relation = new RelationType(relationType.getId());
                Node startNode = null;
                try {
                    startNode = getNodeByUniqueId(graphDb, collectionId.getId());
                } catch (ResourceNotFoundException e) {
                    if (null != collection && StringUtils.isNotBlank(collection.getIdentifier())) {
                        startNode = graphDb.createNode(NODE_LABEL);
                        startNode.setProperty(SystemProperties.IL_UNIQUE_ID.name(), collection.getIdentifier());
                        startNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), collection.getNodeType());
                        if (StringUtils.isNotBlank(collection.getObjectType()))
                            startNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), collection.getObjectType());
                        Map<String, Object> metadata = collection.getMetadata();
                        if (null != metadata && metadata.size() > 0) {
                            for (Entry<String, Object> entry : metadata.entrySet()) {
                                startNode.setProperty(entry.getKey(), entry.getValue());
                            }
                        }
                    } else {
                        throw new ClientException(GraphDACErrorCodes.ERR_CREATE_COLLECTION_MISSING_REQ_PARAMS.name(),
                                "Failed to create Collection node");
                    }
                }
                Iterable<Relationship> relations = startNode.getRelationships(Direction.OUTGOING, relation);
                if (null != relations) {
                    for (Relationship rel : relations) {
                        Object relEndNodeId = rel.getEndNode().getProperty(SystemProperties.IL_UNIQUE_ID.name());
                        String strEndNodeId = (null == relEndNodeId) ? null : relEndNodeId.toString();
                        if (StringUtils.isNotBlank(strEndNodeId) && memberIds.contains(strEndNodeId)) {
                            memberIds.remove(strEndNodeId);
                        }
                    }
                }
                if (!memberIds.isEmpty()) {
                    int i = 1;
                    for (String memberId : memberIds) {
                        Node endNode = getNodeByUniqueId(graphDb, memberId);
                        Relationship rel = startNode.createRelationshipTo(endNode, relation);
                        if (validateRequired(indexProperty)) {
                            rel.setProperty(indexProperty.getId(), i);
                            i += 1;
                        }
                    }
                }
                tx.success();
                OK(getSender());
            } catch (Exception e) {
                if (null != tx)
                    tx.failure();
                ERROR(e, getSender());
            } finally {
                if (null != tx)
                    tx.close();
            }
        }
    }

    public void deleteCollection(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue collectionId = (StringValue) request.get(GraphDACParams.COLLECTION_ID.name());
        if (!validateRequired(collectionId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DELETE_COLLECTION_MISSING_REQ_PARAMS.name(), "Required Variables are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Node collection = Neo4jGraphUtil.getNodeByUniqueId(graphDb, collectionId.getId());
                if (null != collection) {
                    Iterable<Relationship> rels = collection.getRelationships();
                    if (null != rels) {
                        for (Relationship rel : rels) {
                            rel.delete();
                        }
                    }
                    collection.delete();
                }
                tx.success();
                OK(getSender());
            } catch (Exception e) {
                if (null != tx)
                    tx.failure();
                ERROR(e, getSender());
            } finally {
                if (null != tx)
                    tx.close();
            }
        }
    }

    @Override
    public void importGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        ImportData input = (ImportData) request.get(GraphDACParams.IMPORT_INPUT_OBJECT.name());
        Transaction tx = null;
        if (StringUtils.isBlank(graphId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_GRAPH_ID.name(), "Graph Id cannot be blank");
        } else {
            try {
                Map<String, List<String>> messages = new HashMap<String, List<String>>();
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();

                GlobalGraphOperations graphOps = GlobalGraphOperations.at(graphDb);
                Map<String, Node> existingNodes = getExistingNodes(graphOps.getAllNodes());
                Map<String, Map<String, List<Relationship>>> existingRelations = getExistingRelations(graphOps.getAllRelationships());

                List<com.ilimi.graph.dac.model.Node> importedNodes = new ArrayList<com.ilimi.graph.dac.model.Node>(input.getDataNodes());

                int nodesCount = createNodes(request, graphDb, existingNodes, importedNodes);
                int relationsCount = createRelations(request, graphDb, existingRelations, existingNodes, importedNodes, messages);

                upsertRootNode(graphDb, graphId, existingNodes, nodesCount, relationsCount);

                tx.success();
                tx.close();
                OK(GraphDACParams.MESSAGES.name(), new BaseValueObjectMap<List<String>>(messages), getSender());
            } catch (Exception e) {
                e.printStackTrace();
                if (null != tx) {
                    tx.failure();
                    tx.close();
                }
                ERROR(e, getSender());
            }
        }
    }

    private Map<String, Node> getExistingNodes(Iterable<Node> dbNodes) {
        Map<String, Node> existingNodes = new HashMap<String, Node>();
        if (null != dbNodes && null != dbNodes.iterator()) {
            for (Node dbNode : dbNodes) {
                existingNodes.put(dbNode.getProperty(SystemProperties.IL_UNIQUE_ID.name()).toString(), dbNode);
            }
        }
        return existingNodes;
    }

    private Map<String, Map<String, List<Relationship>>> getExistingRelations(Iterable<Relationship> dbRelations) {
        Map<String, Map<String, List<Relationship>>> existingRelations = new HashMap<String, Map<String, List<Relationship>>>();
        if (null != dbRelations && null != dbRelations.iterator()) {
            for (Relationship relationship : dbRelations) {
                String startNodeId = (String) relationship.getStartNode().getProperty(SystemProperties.IL_UNIQUE_ID.name());
                String relationType = relationship.getType().name();
                if (existingRelations.containsKey(startNodeId)) {
                    Map<String, List<Relationship>> relationMap = existingRelations.get(startNodeId);
                    if (relationMap.containsKey(relationType)) {
                        List<Relationship> relationList = relationMap.get(relationType);
                        relationList.add(relationship);
                    }
                } else {
                    Map<String, List<Relationship>> relationMap = new HashMap<String, List<Relationship>>();
                    List<Relationship> relationList = new ArrayList<Relationship>();
                    relationList.add(relationship);
                    relationMap.put(relationType, relationList);
                    existingRelations.put(startNodeId, relationMap);
                }
            }
        }
        return existingRelations;
    }

    private int createNodes(Request request, GraphDatabaseService graphDb, Map<String, Node> existingNodes,
            List<com.ilimi.graph.dac.model.Node> nodes) {
        int nodesCount = 0;
        for (com.ilimi.graph.dac.model.Node node : nodes) {
            if (null == node || StringUtils.isBlank(node.getIdentifier()) || StringUtils.isBlank(node.getNodeType())) {
                // ERROR(GraphDACErrorCodes.ERR_CREATE_NODE_MISSING_REQ_PARAMS.name(),
                // "Invalid input node", request, getSender());
            } else {
                Node neo4jNode = null;
                if (existingNodes.containsKey(node.getIdentifier())) {
                    neo4jNode = existingNodes.get(node.getIdentifier());
                } else {
                    neo4jNode = graphDb.createNode(NODE_LABEL);
                    nodesCount++;
                }
                neo4jNode.setProperty(SystemProperties.IL_UNIQUE_ID.name(), node.getIdentifier());
                neo4jNode.setProperty("identifier", node.getIdentifier());
                neo4jNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), node.getNodeType());
                if (StringUtils.isNotBlank(node.getObjectType()))
                    neo4jNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), node.getObjectType());
                Map<String, Object> metadata = node.getMetadata();
                if (null != metadata && metadata.size() > 0) {
                    for (Entry<String, Object> entry : metadata.entrySet()) {
                        if (null == entry.getValue() && neo4jNode.hasProperty(entry.getKey())) {
                            neo4jNode.removeProperty(entry.getKey());
                        } else if (null != entry.getValue()) {
                            neo4jNode.setProperty(entry.getKey(), entry.getValue());
                        }
                    }
                }
                existingNodes.put(node.getIdentifier(), neo4jNode);
            }
        }
        return nodesCount;
    }

    private int createRelations(Request request, GraphDatabaseService graphDb,
            Map<String, Map<String, List<Relationship>>> existingRelations, Map<String, Node> existingNodes,
            List<com.ilimi.graph.dac.model.Node> nodes, Map<String, List<String>> messages) {
        int relationsCount = 0;
        for (com.ilimi.graph.dac.model.Node node : nodes) {
            List<Relation> nodeRelations = node.getOutRelations();
            if (nodeRelations != null) {
                Map<String, List<String>> nodeRelMap = new HashMap<String, List<String>>();
                for (Relation rel : nodeRelations) {
                    String relType = rel.getRelationType();
                    if (nodeRelMap.containsKey(relType)) {
                        List<String> endNodeIds = nodeRelMap.get(relType);
                        if (endNodeIds == null) {
                            endNodeIds = new ArrayList<String>();
                            if (StringUtils.isNotBlank(rel.getEndNodeId()))
                                endNodeIds.add(rel.getEndNodeId().trim());
                        }
                        if (StringUtils.isNotBlank(rel.getEndNodeId()))
                            endNodeIds.add(rel.getEndNodeId().trim());
                    } else {
                        List<String> endNodeIds = new ArrayList<String>();
                        if (StringUtils.isNotBlank(rel.getEndNodeId()))
                            endNodeIds.add(rel.getEndNodeId().trim());
                        nodeRelMap.put(relType, endNodeIds);
                    }
                }
                // System.out.println("nodeRelMap:"+nodeRelMap);
                String uniqueId = node.getIdentifier();
                Node neo4jNode = existingNodes.get(uniqueId);
                if (existingRelations.containsKey(uniqueId)) {
                    Map<String, List<Relationship>> relationMap = existingRelations.get(uniqueId);
                    for (String relType : relationMap.keySet()) {
                        if (nodeRelMap.containsKey(relType)) {
                            List<String> relEndNodeIds = nodeRelMap.get(relType);
                            for (Relationship rel : relationMap.get(relType)) {
                                String endNodeId = (String) rel.getEndNode().getProperty(SystemProperties.IL_UNIQUE_ID.name());
                                if (relEndNodeIds.contains(endNodeId)) {
                                    relEndNodeIds.remove(endNodeId);
                                } else {
                                    rel.delete();
                                    relationsCount--;
                                }
                            }
                            for (String endNodeId : relEndNodeIds) {
                                Node otherNode = existingNodes.get(endNodeId);
                                if(otherNode != null) {
                                    RelationType relation = new RelationType(relType);
                                    neo4jNode.createRelationshipTo(otherNode, relation);
                                    relationsCount++;
                                } else {
                                    List<String> rowMsgs = messages.get(uniqueId);
                                    if(rowMsgs == null) {
                                        rowMsgs = new ArrayList<String>();
                                        messages.put(uniqueId, rowMsgs);
                                    }
                                    rowMsgs.add("Node with id: "+endNodeId+ " not found to create relation:"+relType);
                                }
                                
                            }
                        } else {
                            for (Relationship rel : relationMap.get(relType)) {
                                rel.delete();
                                relationsCount--;
                            }
                        }
                    }
                } else {
                    for (String relType : nodeRelMap.keySet()) {
                        List<String> relEndNodeIds = nodeRelMap.get(relType);
                        for (String endNodeId : relEndNodeIds) {
                            Node otherNode = existingNodes.get(endNodeId);
                            if (null == otherNode) {
                                List<String> rowMsgs = messages.get(uniqueId);
                                if(rowMsgs == null) {
                                    rowMsgs = new ArrayList<String>();
                                    messages.put(uniqueId, rowMsgs);
                                }
                                rowMsgs.add("Node with id: "+endNodeId+ " not found to create relation:"+relType);
                            } else {
                                RelationType relation = new RelationType(relType);
                                neo4jNode.createRelationshipTo(otherNode, relation);
                                relationsCount++;
                            }
                        }
                    }
                }
            }
        }
        return relationsCount;
    }

    private void upsertRootNode(GraphDatabaseService graphDb, String graphId, Map<String, Node> existingNodes, Integer nodesCount,
            Integer relationsCount) {
        String rootNodeUniqueId = graphId + "_" + SystemNodeTypes.ROOT_NODE.name();
        if (existingNodes.get(rootNodeUniqueId) == null) {
            Node rootNode = graphDb.createNode(NODE_LABEL);
            rootNode.setProperty(SystemProperties.IL_UNIQUE_ID.name(), rootNodeUniqueId);
            rootNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.ROOT_NODE.name());
            rootNode.setProperty("nodesCount", nodesCount);
            rootNode.setProperty("relationsCount", relationsCount);
        } else {
            Node rootNode = existingNodes.get(rootNodeUniqueId);
            int totalNodes = (Integer) rootNode.getProperty("nodesCount") + nodesCount;
            rootNode.setProperty("nodesCount", totalNodes);
            int totalRelations = (Integer) rootNode.getProperty("relationsCount") + relationsCount;
            rootNode.setProperty("relationsCount", totalRelations);
        }
    }

}
