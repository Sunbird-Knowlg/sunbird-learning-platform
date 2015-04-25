package com.ilimi.graph.dac.mgr.impl;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.tooling.GlobalGraphOperations;

import akka.actor.ActorRef;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.dto.BaseValueObject;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.BooleanValue;
import com.ilimi.graph.common.dto.Identifier;
import com.ilimi.graph.common.dto.LongIdentifier;
import com.ilimi.graph.common.dto.Property;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;
import com.ilimi.graph.dac.mgr.IGraphDACSearchMgr;
import com.ilimi.graph.dac.model.Graph;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.RelationTraversal;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.SubGraph;
import com.ilimi.graph.dac.model.Traverser;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.dac.util.Neo4jGraphUtil;
import com.ilimi.graph.dac.util.RelationType;

public class GraphDACSearchMgrImpl extends BaseGraphManager implements IGraphDACSearchMgr {

    protected void invokeMethod(Request request, ActorRef parent) {
        String methodName = request.getOperation();
        try {
            Method method = GraphDACActorPoolMgr.getMethod(GraphDACManagers.DAC_SEARCH_MANAGER, methodName);
            if (null == method) {
                throw new ClientException("ERR_GRAPH_INVALID_OPERATION", "Operation '" + methodName + "' not found");
            } else {
                method.invoke(this, request);
            }
        } catch (Exception e) {
            ERROR(e, parent);
        }
    }

    @Override
    public void getNodeById(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        LongIdentifier nodeId = (LongIdentifier) request.get(GraphDACParams.NODE_ID.name());
        BooleanValue getTags = (BooleanValue) request.get(GraphDACParams.GET_TAGS.name());
        if (!validateRequired(nodeId))
            throw new ClientException(GraphDACErrorCodes.ERR_DAC_GET_NODE_EXCEPTION.name(), "Required parameters are missing");
        Transaction tx = null;
        try {
            GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
            tx = graphDb.beginTx();
            org.neo4j.graphdb.Node neo4jNode = graphDb.getNodeById(nodeId.getId());
            tx.success();
            Node node = new Node(graphId, neo4jNode);
            if (null != getTags && null != getTags.getValue() && getTags.getValue().booleanValue())
                setTags(neo4jNode, node);
            OK(GraphDACParams.NODE.name(), node, getSender());
        } catch (Exception e) {
            if (null != tx)
                tx.failure();
            ERROR(e, getSender());
        } finally {
            if (null != tx)
                tx.close();
        }
    }

    @Override
    public void getNodeByUniqueId(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue nodeId = (StringValue) request.get(GraphDACParams.NODE_ID.name());
        BooleanValue getTags = (BooleanValue) request.get(GraphDACParams.GET_TAGS.name());
        if (!validateRequired(nodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DAC_GET_NODE_EXCEPTION.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                org.neo4j.graphdb.Node neo4jNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, nodeId.getId());
                Node node = new Node(graphId, neo4jNode);
                if (null != getTags && null != getTags.getValue() && getTags.getValue().booleanValue())
                    setTags(neo4jNode, node);
                tx.success();
                OK(GraphDACParams.NODE.name(), node, getSender());
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

    private void setTags(org.neo4j.graphdb.Node neo4jNode, Node node) {
        Iterable<Relationship> inRels = neo4jNode.getRelationships(Direction.INCOMING);
        if (null != inRels) {
            List<String> tags = new ArrayList<String>();
            for (Relationship rel : inRels) {
                if (StringUtils.equals(RelationTypes.SET_MEMBERSHIP.relationName(), rel.getType().name())) {
                    org.neo4j.graphdb.Node startNode = rel.getStartNode();
                    String nodeType = (String) startNode.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), null);
                    if (StringUtils.equalsIgnoreCase(SystemNodeTypes.TAG.name(), nodeType)) {
                        String tag = (String) startNode.getProperty(SystemProperties.IL_TAG_NAME.name(), null);
                        if (StringUtils.isNotBlank(tag))
                            tags.add(tag);
                    }
                }
            }
            node.setTags(tags);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getNodesByUniqueIds(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        BaseValueObjectList<StringValue> nodeIds = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.NODE_IDS.name());
        BooleanValue getTags = (BooleanValue) request.get(GraphDACParams.GET_TAGS.name());
        if (!validateRequired(nodeIds)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DAC_GET_NODE_EXCEPTION.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                Set<String> uniqueIds = new HashSet<String>();
                for (StringValue id : nodeIds.getValueObjectList()) {
                    uniqueIds.add(id.getId());
                }
                params.put("uniqueIds", uniqueIds);
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Result result = graphDb.execute("MATCH (n:NODE) WHERE n." + SystemProperties.IL_UNIQUE_ID.name()
                        + "  in {uniqueIds} RETURN n", params);
                List<Node> nodes = new ArrayList<Node>();
                if (null != result) {
                    while (result.hasNext()) {
                        Map<String, Object> map = result.next();
                        if (null != map && !map.isEmpty()) {
                            Object obj = map.values().iterator().next();
                            if (obj instanceof org.neo4j.graphdb.Node) {
                                org.neo4j.graphdb.Node neo4jNode = (org.neo4j.graphdb.Node) obj;
                                Node node = new Node(graphId, neo4jNode);
                                nodes.add(new Node(graphId, neo4jNode));
                                if (null != getTags && null != getTags.getValue() && getTags.getValue().booleanValue())
                                    setTags(neo4jNode, node);
                            }
                        }
                    }
                    result.close();
                }
                tx.success();
                OK(GraphDACParams.NODE_LIST.name(), new BaseValueObjectList<Node>(nodes), getSender());
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
    public void getNodesByProperty(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        Property property = (Property) request.get(GraphDACParams.METADATA.name());
        BooleanValue getTags = (BooleanValue) request.get(GraphDACParams.GET_TAGS.name());
        if (!validateRequired(property)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DAC_GET_NODE_LIST_EXCEPTION.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                ResourceIterator<org.neo4j.graphdb.Node> nodes = graphDb.findNodes(NODE_LABEL, property.getPropertyName(),
                        property.getPropertyValue());
                List<Node> nodeList = null;
                if (null != nodes) {
                    nodeList = new ArrayList<Node>();
                    while (nodes.hasNext()) {
                        org.neo4j.graphdb.Node neo4jNode = nodes.next();
                        Node node = new Node(graphId, neo4jNode);
                        if (null != getTags && null != getTags.getValue() && getTags.getValue().booleanValue())
                            setTags(neo4jNode, node);
                        nodeList.add(node);
                        nodes.close();
                    }
                    nodes.close();
                }
                tx.success();
                BaseValueObjectList<Node> list = new BaseValueObjectList<Node>(nodeList);
                OK(GraphDACParams.NODE_LIST.name(), list, getSender());
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
    public void getNodeProperty(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue nodeId = (StringValue) request.get(GraphDACParams.NODE_ID.name());
        StringValue key = (StringValue) request.get(GraphDACParams.PROPERTY_KEY.name());
        if (!validateRequired(nodeId, key)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DAC_GET_NODE_PROPERTY.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Property property = null;
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("nodeId", nodeId.getId());
                Result result = graphDb.execute("MATCH (n:NODE) WHERE n." + SystemProperties.IL_UNIQUE_ID.name()
                        + "  in {nodeId} RETURN n." + key.getId(), params);
                if (null != result) {
                    while (result.hasNext()) {
                        Map<String, Object> map = result.next();
                        if (null != map && !map.isEmpty()) {
                            Object obj = map.values().iterator().next();
                            property = new Property(key.getId(), obj);
                        }
                    }
                    result.close();
                }
                tx.success();
                OK(GraphDACParams.PROPERTY.name(), property, getSender());
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
    public void getAllNodes(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        Transaction tx = null;
        try {
            GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
            tx = graphDb.beginTx();
            GlobalGraphOperations graphOps = GlobalGraphOperations.at(graphDb);
            Iterable<org.neo4j.graphdb.Node> dbNodes = graphOps.getAllNodes();
            List<Node> nodes = new ArrayList<Node>();
            if (null != dbNodes && null != dbNodes.iterator()) {
                for (org.neo4j.graphdb.Node dbNode : dbNodes) {
                    nodes.add(new Node(graphId, dbNode));
                }
            }
            tx.success();
            BaseValueObjectList<Node> list = new BaseValueObjectList<Node>(nodes);
            OK(GraphDACParams.NODE_LIST.name(), list, getSender());
        } catch (Exception e) {
            if (null != tx)
                tx.failure();
            ERROR(e, getSender());
        } finally {
            if (null != tx)
                tx.close();
        }
    }

    @Override
    public void getAllRelations(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        Transaction tx = null;
        try {
            GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
            tx = graphDb.beginTx();
            GlobalGraphOperations graphOps = GlobalGraphOperations.at(graphDb);
            Iterable<Relationship> dbRelations = graphOps.getAllRelationships();
            List<Relation> relations = new ArrayList<Relation>();
            if (null != dbRelations && null != dbRelations.iterator()) {
                for (Relationship dbRel : dbRelations) {
                    relations.add(new Relation(graphId, dbRel));
                }
            }
            tx.success();
            BaseValueObjectList<Relation> list = new BaseValueObjectList<Relation>(relations);
            OK(GraphDACParams.RELATIONS.name(), list, getSender());
        } catch (Exception e) {
            if (null != tx)
                tx.failure();
            ERROR(e, getSender());
        } finally {
            if (null != tx)
                tx.close();
        }
    }

    @Override
    public void getRelationProperty(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue startNodeId = (StringValue) request.get(GraphDACParams.START_NODE_ID.name());
        StringValue relationType = (StringValue) request.get(GraphDACParams.RELATION_TYPE.name());
        StringValue endNodeId = (StringValue) request.get(GraphDACParams.END_NODE_ID.name());
        StringValue key = (StringValue) request.get(GraphDACParams.PROPERTY_KEY.name());
        if (!validateRequired(startNodeId, relationType, endNodeId, key)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DAC_GET_RELATIONS_EXCEPTION.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Object value = null;
                Relationship rel = Neo4jGraphUtil.getRelationship(graphDb, startNodeId.getId(), relationType.getId(), endNodeId.getId());
                if (null != rel)
                    value = rel.getProperty(key.getId());
                tx.success();
                Property property = new Property(key.getId(), value);
                OK(GraphDACParams.PROPERTY.name(), property, getSender());
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
    public void getRelation(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue startNodeId = (StringValue) request.get(GraphDACParams.START_NODE_ID.name());
        StringValue relationType = (StringValue) request.get(GraphDACParams.RELATION_TYPE.name());
        StringValue endNodeId = (StringValue) request.get(GraphDACParams.END_NODE_ID.name());
        if (!validateRequired(startNodeId, relationType, endNodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DAC_GET_RELATIONS_EXCEPTION.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Relation relation = null;
                Relationship rel = Neo4jGraphUtil.getRelationship(graphDb, startNodeId.getId(), relationType.getId(), endNodeId.getId());
                if (null != rel)
                    relation = new Relation(graphId, rel);
                tx.success();
                OK(GraphDACParams.RELATION.name(), relation, getSender());
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
    public void checkCyclicLoop(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue startNodeId = (StringValue) request.get(GraphDACParams.START_NODE_ID.name());
        StringValue relationType = (StringValue) request.get(GraphDACParams.RELATION_TYPE.name());
        StringValue endNodeId = (StringValue) request.get(GraphDACParams.END_NODE_ID.name());
        if (!validateRequired(startNodeId, relationType, endNodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DAC_CHECK_LOOP.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                org.neo4j.graphdb.Node startNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, startNodeId.getId());
                org.neo4j.graphdb.Node endNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, endNodeId.getId());
                ResourceIterable<org.neo4j.graphdb.Node> pathNodes = graphDb.traversalDescription().breadthFirst()
                        .relationships(new RelationType(relationType.getId()), Direction.OUTGOING)
                        .evaluator(Evaluators.pruneWhereEndNodeIs(endNode)).traverse(startNode).nodes();

                Map<String, BaseValueObject> voMap = new HashMap<String, BaseValueObject>();
                if (null != pathNodes && null != pathNodes.iterator()) {
                    for (org.neo4j.graphdb.Node node : pathNodes) {
                        String uniqueId = (String) node.getProperty(SystemProperties.IL_UNIQUE_ID.name(), null);
                        if (StringUtils.equals(endNodeId.getId(), uniqueId)) {
                            voMap.put(GraphDACParams.LOOP.name(), new BooleanValue(true));
                            voMap.put(GraphDACParams.MESSAGE.name(), new StringValue(startNodeId.getId() + " and " + endNodeId.getId()
                                    + " are connected by relation: " + relationType.getId()));
                            break;
                        }
                    }
                    pathNodes.iterator().close();
                }
                if (voMap.get(GraphDACParams.LOOP.name()) == null) {
                    voMap.put(GraphDACParams.LOOP.name(), new BooleanValue(false));
                }
                tx.success();
                OK(voMap, getSender());
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
    public void searchNodes(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        SearchCriteria sc = (SearchCriteria) request.get(GraphDACParams.SEARCH_CRITERIA.name());
        BooleanValue getTags = (BooleanValue) request.get(GraphDACParams.GET_TAGS.name());
        if (!validateRequired(sc)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DAC_SEARCH_NODES.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                sc.countQuery(false);
                List<String> fields = sc.getFields();
                boolean returnNode = true;
                if (null != fields && !fields.isEmpty()) {
                    returnNode = false;
                    if (!fields.contains(SystemProperties.IL_SYS_NODE_TYPE.name()))
                        sc.returnField(SystemProperties.IL_SYS_NODE_TYPE.name());
                    if (!fields.contains(SystemProperties.IL_UNIQUE_ID.name()))
                        sc.returnField(SystemProperties.IL_UNIQUE_ID.name());
                    if (!fields.contains(SystemProperties.IL_FUNC_OBJECT_TYPE.name()))
                        sc.returnField(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
                }
                Map<String, Object> params = sc.getParams();
                String query = sc.getQuery();
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Result result = graphDb.execute(query, params);
                List<Node> nodes = new ArrayList<Node>();
                if (null != result) {
                    while (result.hasNext()) {
                        Map<String, Object> map = result.next();
                        if (null != map && !map.isEmpty()) {
                            if (returnNode) {
                                Object o = map.values().iterator().next();
                                if (o instanceof org.neo4j.graphdb.Node) {
                                    org.neo4j.graphdb.Node dbNode = (org.neo4j.graphdb.Node) o;
                                    Node node = new Node(graphId, dbNode);
                                    if (null != getTags && null != getTags.getValue() && getTags.getValue().booleanValue())
                                        setTags(dbNode, node);
                                    nodes.add(node);
                                }
                            } else {
                                Node node = new Node(graphId, map);
                                nodes.add(node);
                            }
                        }
                    }
                    result.close();
                }
                tx.success();
                OK(GraphDACParams.NODE_LIST.name(), new BaseValueObjectList<Node>(nodes), getSender());
            } catch (Exception e) {
                if (null != tx)
                    tx.failure();
                e.printStackTrace();
                ERROR(e, getSender());
            } finally {
                if (null != tx)
                    tx.close();
            }
        }
    }

    @Override
    public void getNodesCount(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        SearchCriteria sc = (SearchCriteria) request.get(GraphDACParams.SEARCH_CRITERIA.name());
        if (!validateRequired(sc)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DAC_SEARCH_NODES.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                Long count = (long) 0;
                sc.countQuery(true);
                Map<String, Object> params = sc.getParams();
                String query = sc.getQuery();
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Result result = graphDb.execute(query, params);
                if (null != result && result.hasNext()) {
                    Map<String, Object> map = result.next();
                    if (null != map && !map.isEmpty()) {
                        for (Entry<String, Object> entry : map.entrySet()) {
                            Object obj = entry.getValue();
                            try {
                                count = Long.valueOf(obj.toString());
                            } catch (Exception e) {
                            }
                        }
                    }
                    result.close();
                }
                tx.success();
                OK(GraphDACParams.COUNT.name(), new LongIdentifier(count), getSender());
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
    public void traverse(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        Traverser traverser = (Traverser) request.get(GraphDACParams.TRAVERSAL_DESCRIPTION.name());
        if (!validateRequired(traverser)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DAC_TRAVERSAL.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                SubGraph subGraph = traverser.traverse();
                tx.success();
                OK(GraphDACParams.SUB_GRAPH.name(), subGraph, getSender());
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
    public void getSubGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue startNodeId = (StringValue) request.get(GraphDACParams.START_NODE_ID.name());
        StringValue relationType = (StringValue) request.get(GraphDACParams.RELATION_TYPE.name());
        Identifier depth = (Identifier) request.get(GraphDACParams.DEPTH.name());
        if (!validateRequired(startNodeId, relationType)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DAC_TRAVERSAL.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Traverser traverser = new Traverser(graphId, startNodeId.getId());
                traverser.traverseRelation(new RelationTraversal(relationType.getId(), RelationTraversal.DIRECTION_OUT));
                if (null != depth && null != depth.getId() && depth.getId().intValue() > 0) {
                    traverser.toDepth(depth.getId());
                }
                Graph subGraph = traverser.getSubGraph();
                tx.success();
                OK(GraphDACParams.SUB_GRAPH.name(), subGraph, getSender());
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

}
