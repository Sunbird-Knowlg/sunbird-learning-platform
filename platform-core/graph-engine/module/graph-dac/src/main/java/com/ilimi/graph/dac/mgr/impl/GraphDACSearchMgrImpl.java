package com.ilimi.graph.dac.mgr.impl;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluators;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;
import com.ilimi.graph.dac.mgr.IGraphDACSearchMgr;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.Graph;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.RelationTraversal;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.SubGraph;
import com.ilimi.graph.dac.model.Traverser;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.dac.util.Neo4jGraphUtil;
import com.ilimi.graph.dac.util.RelationType;

import akka.actor.ActorRef;

public class GraphDACSearchMgrImpl extends BaseGraphManager implements IGraphDACSearchMgr {

    protected void invokeMethod(Request request, ActorRef parent) {
        String methodName = request.getOperation();
        try {
            Method method = GraphDACActorPoolMgr.getMethod(GraphDACManagers.DAC_SEARCH_MANAGER, methodName);
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
    public void getNodeById(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        Long nodeId = (Long) request.get(GraphDACParams.node_id.name());
        Boolean getTags = (Boolean) request.get(GraphDACParams.get_tags.name());
        if (!validateRequired(nodeId))
            throw new ClientException(GraphDACErrorCodes.ERR_GET_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        Transaction tx = null;
        try {
            GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
            tx = graphDb.beginTx();
            org.neo4j.graphdb.Node neo4jNode = graphDb.getNodeById(nodeId);
            tx.success();
            Node node = new Node(graphId, neo4jNode);
            if (null != getTags && getTags.booleanValue())
                setTags(neo4jNode, node);
            OK(GraphDACParams.node.name(), node, getSender());
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        Boolean getTags = (Boolean) request.get(GraphDACParams.get_tags.name());
        if (!validateRequired(nodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GET_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
                tx = graphDb.beginTx();
                org.neo4j.graphdb.Node neo4jNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, nodeId);
                Node node = new Node(graphId, neo4jNode);
                if (null != getTags && getTags.booleanValue())
                    setTags(neo4jNode, node);
                tx.success();
                OK(GraphDACParams.node.name(), node, getSender());
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

    /*@SuppressWarnings("unchecked")
    @Override
    public void getNodesByUniqueIds(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        List<String> nodeIds = (List<String>) request.get(GraphDACParams.node_ids.name());
        Boolean getTags = (Boolean) request.get(GraphDACParams.get_tags.name());
        if (!validateRequired(nodeIds)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GET_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                Set<String> uniqueIds = new HashSet<String>();
                for (String id : nodeIds) {
                    uniqueIds.add(id);
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
                                if (null != getTags && getTags.booleanValue())
                                    setTags(neo4jNode, node);
                            }
                        }
                    }
                    result.close();
                }
                tx.success();
                OK(GraphDACParams.node_list.name(), nodes, getSender());
            } catch (Exception e) {
                if (null != tx)
                    tx.failure();
                ERROR(e, getSender());
            } finally {
                if (null != tx)
                    tx.close();
            }
        }
    }*/

    @Override
    public void getNodesByProperty(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        Property property = (Property) request.get(GraphDACParams.metadata.name());
        Boolean getTags = (Boolean) request.get(GraphDACParams.get_tags.name());
        if (!validateRequired(property)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GET_NODE_LIST_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
                tx = graphDb.beginTx();
                ResourceIterator<org.neo4j.graphdb.Node> nodes = graphDb.findNodes(NODE_LABEL, property.getPropertyName(),
                        property.getPropertyValue());
                List<Node> nodeList = null;
                if (null != nodes) {
                    nodeList = new ArrayList<Node>();
                    while (nodes.hasNext()) {
                        org.neo4j.graphdb.Node neo4jNode = nodes.next();
                        Node node = new Node(graphId, neo4jNode);
                        if (null != getTags && getTags.booleanValue())
                            setTags(neo4jNode, node);
                        nodeList.add(node);
                        nodes.close();
                    }
                    nodes.close();
                }
                tx.success();
                OK(GraphDACParams.node_list.name(), nodeList, getSender());
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
    
    @SuppressWarnings("unchecked")
	@Override
	public void getNodesByUniqueIds(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		List<String> nodeIds = (List<String>) request.get(GraphDACParams.node_ids.name());
		if (!validateRequired(nodeIds)) {
			throw new ClientException(GraphDACErrorCodes.ERR_GET_NODE_LIST_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing");
		} else {
			SearchCriteria sc = new SearchCriteria();
			MetadataCriterion mc = MetadataCriterion.create(Arrays.asList(new Filter("identifier", SearchConditions.OP_IN, nodeIds)));
			sc.addMetadata(mc);
			sc.setCountQuery(false);
			Transaction tx = null;
            try {
                Map<String, Object> params = sc.getParams();
                String query = sc.getQuery();
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
                tx = graphDb.beginTx();
                Result result = graphDb.execute(query, params);
                List<Node> nodes = new ArrayList<Node>();
                if (null != result) {
                    while (result.hasNext()) {
                        Map<String, Object> map = result.next();
                        if (null != map && !map.isEmpty()) {
                            Object o = map.values().iterator().next();
                            if (o instanceof org.neo4j.graphdb.Node) {
                                org.neo4j.graphdb.Node dbNode = (org.neo4j.graphdb.Node) o;
                                Node node = new Node(graphId, dbNode);
                                setTags(dbNode, node);
                                nodes.add(node);
                            }
                        }
                    }
                    result.close();
                }
                tx.success();
                OK(GraphDACParams.node_list.name(), nodes, getSender());
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        String key = (String) request.get(GraphDACParams.property_key.name());
        if (!validateRequired(nodeId, key)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GET_NODE_PROPERTY_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
                tx = graphDb.beginTx();
                Property property = null;
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("nodeId", nodeId);
                Result result = graphDb.execute("MATCH (n:NODE) WHERE n." + SystemProperties.IL_UNIQUE_ID.name()
                        + "  in {nodeId} RETURN n." + key, params);
                if (null != result) {
                    while (result.hasNext()) {
                        Map<String, Object> map = result.next();
                        if (null != map && !map.isEmpty()) {
                            Object obj = map.values().iterator().next();
                            property = new Property(key, obj);
                        }
                    }
                    result.close();
                }
                tx.success();
                OK(GraphDACParams.property.name(), property, getSender());
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        Transaction tx = null;
        try {
            GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
            tx = graphDb.beginTx();
            Iterable<org.neo4j.graphdb.Node> dbNodes = graphDb.getAllNodes();
            List<Node> nodes = new ArrayList<Node>();
            if (null != dbNodes && null != dbNodes.iterator()) {
                for (org.neo4j.graphdb.Node dbNode : dbNodes) {
                    nodes.add(new Node(graphId, dbNode));
                }
            }
            tx.success();
            OK(GraphDACParams.node_list.name(), nodes, getSender());
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        Transaction tx = null;
        try {
            GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
            tx = graphDb.beginTx();
//            GlobalGraphOperations graphOps = GlobalGraphOperations.at(graphDb);
            Iterable<Relationship> dbRelations = graphDb.getAllRelationships();
            List<Relation> relations = new ArrayList<Relation>();
            if (null != dbRelations && null != dbRelations.iterator()) {
                for (Relationship dbRel : dbRelations) {
                    relations.add(new Relation(graphId, dbRel));
                }
            }
            tx.success();
            OK(GraphDACParams.relations.name(), relations, getSender());
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        String key = (String) request.get(GraphDACParams.property_key.name());
        if (!validateRequired(startNodeId, relationType, endNodeId, key)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GET_RELATIONS_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
                tx = graphDb.beginTx();
                Object value = null;
                Relationship rel = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType, endNodeId);
                if (null != rel)
                    value = rel.getProperty(key);
                tx.success();
                Property property = new Property(key, value);
                OK(GraphDACParams.property.name(), property, getSender());
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        if (!validateRequired(startNodeId, relationType, endNodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GET_RELATIONS_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
                tx = graphDb.beginTx();
                Relation relation = null;
                Relationship rel = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType, endNodeId);
                if (null != rel)
                    relation = new Relation(graphId, rel);
                tx.success();
                OK(GraphDACParams.relation.name(), relation, getSender());
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        if (!validateRequired(startNodeId, relationType, endNodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_CHECK_LOOP_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
                tx = graphDb.beginTx();
                org.neo4j.graphdb.Node startNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, startNodeId);
                org.neo4j.graphdb.Node endNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, endNodeId);
                ResourceIterable<org.neo4j.graphdb.Node> pathNodes = graphDb.traversalDescription().breadthFirst()
                        .relationships(new RelationType(relationType), Direction.OUTGOING)
                        .evaluator(Evaluators.pruneWhereEndNodeIs(endNode)).traverse(startNode).nodes();

                Map<String, Object> voMap = new HashMap<String, Object>();
                if (null != pathNodes && null != pathNodes.iterator()) {
                    for (org.neo4j.graphdb.Node node : pathNodes) {
                        String uniqueId = (String) node.getProperty(SystemProperties.IL_UNIQUE_ID.name(), null);
                        if (StringUtils.equals(endNodeId, uniqueId)) {
                            voMap.put(GraphDACParams.loop.name(), new Boolean(true));
                            voMap.put(GraphDACParams.message.name(), startNodeId + " and " + endNodeId + " are connected by relation: "
                                    + relationType);
                            break;
                        }
                    }
                    pathNodes.iterator().close();
                }
                if (voMap.get(GraphDACParams.loop.name()) == null) {
                    voMap.put(GraphDACParams.loop.name(), false);
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
    @SuppressWarnings("unchecked")
    public void executeQuery(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String query = (String) request.get(GraphDACParams.query.name());
        Map<String, Object> paramMap = (Map<String, Object>) request.get(GraphDACParams.params.name());
        if (!validateRequired(query)) {
            throw new ClientException(GraphDACErrorCodes.ERR_SEARCH_NODES_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
                tx = graphDb.beginTx();
                Map<String, Object> params = new HashMap<String, Object>();
                if (validateRequired(paramMap))
                    params = paramMap;
                Result result = graphDb.execute(query, params);
                List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
                if (null != result) {
                    while (result.hasNext()) {
                        Map<String, Object> map = result.next();
                        if (null != map && !map.isEmpty()) {
                            resultList.add(map);
                        }
                    }
                    result.close();
                }
                tx.success();
                OK(GraphDACParams.results.name(), resultList, getSender());
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        SearchCriteria sc = (SearchCriteria) request.get(GraphDACParams.search_criteria.name());
        Boolean getTags = (Boolean) request.get(GraphDACParams.get_tags.name());
        if (!validateRequired(sc)) {
            throw new ClientException(GraphDACErrorCodes.ERR_SEARCH_NODES_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                sc.setCountQuery(false);
                List<String> fields = sc.getFields();
                boolean returnNode = true;
                if (null != fields && !fields.isEmpty())
                    returnNode = false;
                Map<String, Object> params = sc.getParams();
                String query = sc.getQuery();
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
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
                                    if (null != getTags && getTags.booleanValue())
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
                OK(GraphDACParams.node_list.name(), nodes, getSender());
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
    public void getNodesCount(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        SearchCriteria sc = (SearchCriteria) request.get(GraphDACParams.search_criteria.name());
        if (!validateRequired(sc)) {
            throw new ClientException(GraphDACErrorCodes.ERR_SEARCH_NODES_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                Long count = (long) 0;
                sc.setCountQuery(true);
                Map<String, Object> params = sc.getParams();
                String query = sc.getQuery();
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
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
                OK(GraphDACParams.count.name(), count, getSender());
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        Traverser traverser = (Traverser) request.get(GraphDACParams.traversal_description.name());
        if (!validateRequired(traverser)) {
            throw new ClientException(GraphDACErrorCodes.ERR_TRAVERSAL_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
                tx = graphDb.beginTx();
                SubGraph subGraph = traverser.traverse();
                tx.success();
                OK(GraphDACParams.sub_graph.name(), subGraph, getSender());
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
    public void traverseSubGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        Traverser traverser = (Traverser) request.get(GraphDACParams.traversal_description.name());
        if (!validateRequired(traverser)) {
            throw new ClientException(GraphDACErrorCodes.ERR_TRAVERSAL_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
                tx = graphDb.beginTx();
                Graph subGraph = traverser.getSubGraph();
                tx.success();
                OK(GraphDACParams.sub_graph.name(), subGraph, getSender());
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        Integer depth = (Integer) request.get(GraphDACParams.depth.name());
        if (!validateRequired(startNodeId, relationType)) {
            throw new ClientException(GraphDACErrorCodes.ERR_TRAVERSAL_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
                tx = graphDb.beginTx();
                Traverser traverser = new Traverser(graphId, startNodeId);
                traverser.traverseRelation(new RelationTraversal(relationType, RelationTraversal.DIRECTION_OUT));
                if (null != depth && depth.intValue() > 0) {
                    traverser.toDepth(depth);
                }
                Graph subGraph = traverser.getSubGraph();
                tx.success();
                OK(GraphDACParams.sub_graph.name(), subGraph, getSender());
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
