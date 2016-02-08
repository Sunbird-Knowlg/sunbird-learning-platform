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
import org.neo4j.graphdb.Lock;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.tooling.GlobalGraphOperations;

import akka.actor.ActorRef;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.graph.common.DateUtils;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.exception.GraphEngineErrorCodes;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.AuditProperties;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
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
                throw new ClientException(GraphDACErrorCodes.ERR_GRAPH_INVALID_OPERATION.name(),
                        "Operation '" + methodName + "' not found");
            } else {
                method.invoke(this, request);
            }
        } catch (Exception e) {
            ERROR(e, parent);
        }
    }

    @Override
    public void createGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        if (StringUtils.isBlank(graphId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_GRAPH_ID.name(), "Graph Id cannot be blank");
        } else if (Neo4jGraphFactory.graphExists(graphId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GRAPH_ALREADY_EXISTS.name(),
                    "Graph '" + graphId + "' already exists");
        } else {
            try {
                Neo4jGraphFactory.createGraph(graphId);
                Neo4jGraphFactory.getGraphDb(graphId);
                OK(GraphDACParams.graph_id.name(), graphId, getSender());
            } catch (Exception e) {
                ERROR(e, getSender());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void createUniqueConstraint(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        List<String> indexProperties = (List<String>) request.get(GraphDACParams.property_keys.name());
        if (!validateRequired(indexProperties)) {
            throw new ClientException(GraphDACErrorCodes.ERR_CREATE_UNIQUE_CONSTRAINT_MISSING_REQ_PARAMS.name(),
                    "Required Parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Schema schema = graphDb.schema();
                for (String prop : indexProperties) {
                    schema.constraintFor(NODE_LABEL).assertPropertyIsUnique(prop).create();
                }
                tx.success();
                tx.close();
                OK(GraphDACParams.graph_id.name(), graphId, getSender());
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
    public void createIndex(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        List<String> indexProperties = (List<String>) request.get(GraphDACParams.property_keys.name());
        if (!validateRequired(indexProperties)) {
            throw new ClientException(GraphDACErrorCodes.ERR_CREATE_INDEX_MISSING_REQ_PARAMS.name(),
                    "Required Parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Schema schema = graphDb.schema();
                for (String prop : indexProperties) {
                    schema.indexFor(NODE_LABEL).on(prop).create();
                }
                tx.success();
                tx.close();
                OK(GraphDACParams.graph_id.name(), graphId, getSender());
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
    public void deleteGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        if (StringUtils.isBlank(graphId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_GRAPH_ID.name(), "Graph Id cannot be blank");
        } else if (!Neo4jGraphFactory.graphExists(graphId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GRAPH_NOT_FOUND.name(),
                    "Graph '" + graphId + "' not found to delete.");
        } else {
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                if (null != graphDb) {
                    Neo4jGraphFactory.shutdownGraph(graphId);
                }
                Neo4jGraphFactory.deleteGraph(graphId);
                OK(GraphDACParams.graph_id.name(), graphId, getSender());
            } catch (Exception e) {
                ERROR(e, getSender());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void addOutgoingRelations(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        List<String> endNodeIds = (List<String>) request.get(GraphDACParams.end_node_id.name());
        if (!validateRequired(startNodeId, relationType, endNodeIds)) {
            throw new ClientException(GraphDACErrorCodes.ERR_CREATE_RELATION_MISSING_REQ_PARAMS.name(),
                    "Required Parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Node startNode = getNodeByUniqueId(graphDb, startNodeId);
                RelationType relType = new RelationType(relationType);
                for (String endNodeId : endNodeIds) {
                    Relationship relation = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType,
                            endNodeId);
                    if (null == relation) {
                        Node endNode = getNodeByUniqueId(graphDb, endNodeId);
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        List<String> startNodeIds = (List<String>) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        if (!validateRequired(startNodeIds, relationType, endNodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_CREATE_RELATION_MISSING_REQ_PARAMS.name(),
                    "Required Parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Node endNode = getNodeByUniqueId(graphDb, endNodeId);
                RelationType relType = new RelationType(relationType);
                for (String startNodeId : startNodeIds) {
                    Relationship relation = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType,
                            endNodeId);
                    if (null == relation) {
                        Node startNode = getNodeByUniqueId(graphDb, startNodeId);
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        Map<String, Object> metadata = (Map<String, Object>) request.get(GraphDACParams.metadata.name());
        if (!validateRequired(startNodeId, relationType, endNodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_CREATE_RELATION_MISSING_REQ_PARAMS.name(),
                    "Required Parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                int index = 0;
                RelationType relation = new RelationType(relationType);
                Node startNode = getNodeByUniqueId(graphDb, startNodeId);
                Lock lock = null;
                if (StringUtils.equalsIgnoreCase(RelationTypes.SEQUENCE_MEMBERSHIP.relationName(),relationType)) {
                    System.out.println("acquiring lock for " + relationType + " -- " + startNodeId + " -- " + endNodeId);
                    lock = tx.acquireReadLock(startNode);
                }
                Relationship dbRel = null;
                Iterable<Relationship> relations = startNode.getRelationships(Direction.OUTGOING, relation);
                if (null != relations) {
                    for (Relationship rel : relations) {
                        Object relEndNodeId = rel.getEndNode().getProperty(SystemProperties.IL_UNIQUE_ID.name());
                        String strEndNodeId = (null == relEndNodeId) ? null : relEndNodeId.toString();
                        if (StringUtils.equalsIgnoreCase(RelationTypes.SEQUENCE_MEMBERSHIP.relationName(),
                                relationType))
                            index += 1;
                        if (StringUtils.equals(endNodeId, strEndNodeId)) {
                            dbRel = rel;
                            break;
                        }
                    }
                }
                if (null == dbRel) {
                    Node endNode = getNodeByUniqueId(graphDb, endNodeId);
                    Relationship rel = startNode.createRelationshipTo(endNode, relation);
                    if (StringUtils.equalsIgnoreCase(RelationTypes.SEQUENCE_MEMBERSHIP.relationName(), relationType))
                        rel.setProperty(SystemProperties.IL_SEQUENCE_INDEX.name(), index);
                    if (null != metadata && metadata.size() > 0) {
                        for (Entry<String, Object> entry : metadata.entrySet()) {
                            rel.setProperty(entry.getKey(), entry.getValue());
                        }
                    }
                } else {
                    if (null != metadata && metadata.size() > 0) {
                        for (Entry<String, Object> entry : metadata.entrySet()) {
                            dbRel.setProperty(entry.getKey(), entry.getValue());
                        }
                    }
                }
                if (null != lock)
                    lock.release();
                tx.success();
                tx.close();
                OK(GraphDACParams.graph_id.name(), graphId, getSender());
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        List<String> startNodeIds = (List<String>) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        if (!validateRequired(startNodeIds, relationType, endNodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DELETE_RELATION_MISSING_REQ_PARAMS.name(),
                    "Required Parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                for (String startNodeId : startNodeIds) {
                    Relationship relation = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType,
                            endNodeId);
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        List<String> endNodeIds = (List<String>) request.get(GraphDACParams.end_node_id.name());
        if (!validateRequired(startNodeId, relationType, endNodeIds)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DELETE_RELATION_MISSING_REQ_PARAMS.name(),
                    "Required Parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                for (String endNodeId : endNodeIds) {
                    Relationship relation = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType,
                            endNodeId);
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        if (!validateRequired(startNodeId, relationType, endNodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DELETE_RELATION_MISSING_REQ_PARAMS.name(),
                    "Required Variables are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Relationship rel = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType, endNodeId);
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        Map<String, Object> metadata = (Map<String, Object>) request.get(GraphDACParams.metadata.name());
        if (!validateRequired(startNodeId, relationType, endNodeId, metadata)) {
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_RELATION_MISSING_REQ_PARAMS.name(),
                    "Required Variables are missing");
        } else if (null != metadata && metadata.size() > 0) {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Relationship rel = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType, endNodeId);
                if (null != rel) {
                    tx.acquireWriteLock(rel);
                    for (Entry<String, Object> entry : metadata.entrySet()) {
                        rel.setProperty(entry.getKey(), entry.getValue());
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
        } else {
            OK(getSender());
        }
    }

    @Override
    public void removeRelationMetadata(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        String key = (String) request.get(GraphDACParams.property_key.name());
        if (!validateRequired(startNodeId, relationType, endNodeId, key)) {
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_RELATION_MISSING_REQ_PARAMS.name(),
                    "Required Variables are missing");
        } else if (StringUtils.isNotBlank(key)) {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Relationship rel = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType, endNodeId);
                if (null != rel && rel.hasProperty(key))
                    rel.removeProperty(key);
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
        } else {
            OK(getSender());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void createCollection(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String collectionId = (String) request.get(GraphDACParams.collection_id.name());
        com.ilimi.graph.dac.model.Node collection = (com.ilimi.graph.dac.model.Node) request
                .get(GraphDACParams.node.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        List<String> members = (List<String>) request.get(GraphDACParams.members.name());
        String indexProperty = (String) request.get(GraphDACParams.index.name());
        if (!validateRequired(collectionId, members)) {
            throw new ClientException(GraphDACErrorCodes.ERR_CREATE_COLLECTION_MISSING_REQ_PARAMS.name(),
                    "Required Variables are missing");
        } else {
            Transaction tx = null;
            try {
                String date = DateUtils.formatCurrentDate();
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                RelationType relation = new RelationType(relationType);
                Node startNode = null;
                try {
                    startNode = getNodeByUniqueId(graphDb, collectionId);
                } catch (ResourceNotFoundException e) {
                    if (null != collection && StringUtils.isNotBlank(collection.getIdentifier())) {
                        startNode = graphDb.createNode(NODE_LABEL);
                        startNode.setProperty(SystemProperties.IL_UNIQUE_ID.name(), collection.getIdentifier());
                        startNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), collection.getNodeType());
                        if (StringUtils.isNotBlank(collection.getObjectType()))
                            startNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(),
                                    collection.getObjectType());
                        Map<String, Object> metadata = collection.getMetadata();
                        if (null != metadata && metadata.size() > 0) {
                            for (Entry<String, Object> entry : metadata.entrySet()) {
                                startNode.setProperty(entry.getKey(), entry.getValue());
                            }
                        }
                        startNode.setProperty(AuditProperties.createdOn.name(), date);
                    } else {
                        throw new ClientException(GraphDACErrorCodes.ERR_CREATE_COLLECTION_MISSING_REQ_PARAMS.name(),
                                "Failed to create Collection node");
                    }
                }
                startNode.setProperty(AuditProperties.lastUpdatedOn.name(), date);
                Iterable<Relationship> relations = startNode.getRelationships(Direction.OUTGOING, relation);
                if (null != relations) {
                    for (Relationship rel : relations) {
                        Object relEndNodeId = rel.getEndNode().getProperty(SystemProperties.IL_UNIQUE_ID.name());
                        String strEndNodeId = (null == relEndNodeId) ? null : relEndNodeId.toString();
                        if (StringUtils.isNotBlank(strEndNodeId) && members.contains(strEndNodeId)) {
                            members.remove(strEndNodeId);
                        }
                    }
                }
                if (!members.isEmpty()) {
                    int i = 1;
                    for (String memberId : members) {
                        Node endNode = getNodeByUniqueId(graphDb, memberId);
                        Relationship rel = startNode.createRelationshipTo(endNode, relation);
                        if (validateRequired(indexProperty)) {
                            rel.setProperty(indexProperty, i);
                            i += 1;
                        }
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

    public void deleteCollection(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String collectionId = (String) request.get(GraphDACParams.collection_id.name());
        if (!validateRequired(collectionId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DELETE_COLLECTION_MISSING_REQ_PARAMS.name(),
                    "Required Variables are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Node collection = Neo4jGraphUtil.getNodeByUniqueId(graphDb, collectionId);
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
    public void importGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        ImportData input = (ImportData) request.get(GraphDACParams.import_input_object.name());
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
                Map<String, Map<String, List<Relationship>>> existingRelations = getExistingRelations(
                        graphOps.getAllRelationships());

                List<com.ilimi.graph.dac.model.Node> importedNodes = new ArrayList<com.ilimi.graph.dac.model.Node>(
                        input.getDataNodes());

                int nodesCount = createNodes(request, graphDb, existingNodes, importedNodes);
                int relationsCount = createRelations(request, graphDb, existingRelations, existingNodes, importedNodes,
                        messages);

                upsertRootNode(graphDb, graphId, existingNodes, nodesCount, relationsCount);

                tx.success();
                tx.close();
                OK(GraphDACParams.messages.name(), messages, getSender());
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
                String startNodeId = (String) relationship.getStartNode()
                        .getProperty(SystemProperties.IL_UNIQUE_ID.name());
                String relationType = relationship.getType().name();
                if (existingRelations.containsKey(startNodeId)) {
                    Map<String, List<Relationship>> relationMap = existingRelations.get(startNodeId);
                    if (relationMap.containsKey(relationType)) {
                        List<Relationship> relationList = relationMap.get(relationType);
                        relationList.add(relationship);
                    } else {
                        List<Relationship> relationList = new ArrayList<Relationship>();
                        relationList.add(relationship);
                        relationMap.put(relationType, relationList);
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
        String date = DateUtils.formatCurrentDate();
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
                    neo4jNode.setProperty(AuditProperties.createdOn.name(), date);
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
                neo4jNode.setProperty(AuditProperties.lastUpdatedOn.name(), date);
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
                Map<String, Map<String, Relation>> nodeRelation = new HashMap<String, Map<String, Relation>>();
                for (Relation rel : nodeRelations) {
                    String relType = rel.getRelationType();
                    Map<String, Relation> relMap = nodeRelation.get(relType);
                    if (null == relMap) {
                        relMap = new HashMap<String, Relation>();
                        nodeRelation.put(relType, relMap);
                    }
                    if (nodeRelMap.containsKey(relType)) {
                        List<String> endNodeIds = nodeRelMap.get(relType);
                        if (endNodeIds == null) {
                            endNodeIds = new ArrayList<String>();
                            nodeRelMap.put(relType, endNodeIds);
                        }
                        if (StringUtils.isNotBlank(rel.getEndNodeId())) {
                            endNodeIds.add(rel.getEndNodeId().trim());
                            relMap.put(rel.getEndNodeId().trim(), rel);
                        }
                    } else {
                        List<String> endNodeIds = new ArrayList<String>();
                        if (StringUtils.isNotBlank(rel.getEndNodeId())) {
                            endNodeIds.add(rel.getEndNodeId().trim());
                            relMap.put(rel.getEndNodeId().trim(), rel);
                        }
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
                            Map<String, Relation> relMap = nodeRelation.get(relType);
                            for (Relationship rel : relationMap.get(relType)) {
                                String endNodeId = (String) rel.getEndNode()
                                        .getProperty(SystemProperties.IL_UNIQUE_ID.name());
                                if (relEndNodeIds.contains(endNodeId)) {
                                    relEndNodeIds.remove(endNodeId);
                                    setRelationMetadata(rel, relMap.get(endNodeId));
                                } else {
                                    rel.delete();
                                    relationsCount--;
                                }
                            }
                            for (String endNodeId : relEndNodeIds) {
                                Node otherNode = existingNodes.get(endNodeId);
                                if (otherNode != null) {
                                    RelationType relation = new RelationType(relType);
                                    Relationship neo4jRel = neo4jNode.createRelationshipTo(otherNode, relation);
                                    setRelationMetadata(neo4jRel, relMap.get(endNodeId));
                                    relationsCount++;
                                } else {
                                    List<String> rowMsgs = messages.get(uniqueId);
                                    if (rowMsgs == null) {
                                        rowMsgs = new ArrayList<String>();
                                        messages.put(uniqueId, rowMsgs);
                                    }
                                    rowMsgs.add(
                                            "Node with id: " + endNodeId + " not found to create relation:" + relType);
                                }

                            }
                        } else {
                            for (Relationship rel : relationMap.get(relType)) {
                                rel.delete();
                                relationsCount--;
                            }
                        }
                    }
                    for (String relType : nodeRelMap.keySet()) {
                        if (!relationMap.containsKey(relType)) {
                            relationsCount += createNewRelations(neo4jNode, nodeRelMap, relType, nodeRelation, uniqueId,
                                    existingNodes, messages);
                        }
                    }
                } else {
                    for (String relType : nodeRelMap.keySet()) {
                        relationsCount += createNewRelations(neo4jNode, nodeRelMap, relType, nodeRelation, uniqueId,
                                existingNodes, messages);
                    }
                }
            }
        }
        return relationsCount;
    }

    private int createNewRelations(Node neo4jNode, Map<String, List<String>> nodeRelMap, String relType,
            Map<String, Map<String, Relation>> nodeRelation, String uniqueId, Map<String, Node> existingNodes,
            Map<String, List<String>> messages) {
        int relationsCount = 0;
        List<String> relEndNodeIds = nodeRelMap.get(relType);
        Map<String, Relation> relMap = nodeRelation.get(relType);
        for (String endNodeId : relEndNodeIds) {
            Node otherNode = existingNodes.get(endNodeId);
            if (null == otherNode) {
                List<String> rowMsgs = messages.get(uniqueId);
                if (rowMsgs == null) {
                    rowMsgs = new ArrayList<String>();
                    messages.put(uniqueId, rowMsgs);
                }
                rowMsgs.add("Node with id: " + endNodeId + " not found to create relation:" + relType);
            } else {
                RelationType relation = new RelationType(relType);
                Relationship neo4jRel = neo4jNode.createRelationshipTo(otherNode, relation);
                setRelationMetadata(neo4jRel, relMap.get(endNodeId));
                relationsCount++;
            }
        }
        return relationsCount;
    }

    private void setRelationMetadata(Relationship neo4jRel, Relation newRel) {
        if (null != newRel && null != newRel.getMetadata() && !newRel.getMetadata().isEmpty()) {
            for (Entry<String, Object> entry : newRel.getMetadata().entrySet()) {
                neo4jRel.setProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    private void upsertRootNode(GraphDatabaseService graphDb, String graphId, Map<String, Node> existingNodes,
            Integer nodesCount, Integer relationsCount) {
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
