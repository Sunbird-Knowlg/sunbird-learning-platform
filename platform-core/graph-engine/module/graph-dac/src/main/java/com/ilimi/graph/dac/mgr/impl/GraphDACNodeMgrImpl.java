package com.ilimi.graph.dac.mgr.impl;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;
import static com.ilimi.graph.dac.util.Neo4jGraphUtil.getNodeByUniqueId;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.graph.common.DateUtils;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.AuditProperties;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;
import com.ilimi.graph.dac.mgr.IGraphDACNodeMgr;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.dac.util.Neo4jGraphUtil;

import akka.actor.ActorRef;

public class GraphDACNodeMgrImpl extends BaseGraphManager implements IGraphDACNodeMgr {

    protected void invokeMethod(Request request, ActorRef parent) {
        String methodName = request.getOperation();
        try {
            Method method = GraphDACActorPoolMgr.getMethod(GraphDACManagers.DAC_NODE_MANAGER, methodName);
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
    public void upsertNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        com.ilimi.graph.dac.model.Node node = (com.ilimi.graph.dac.model.Node) request.get(GraphDACParams.node.name());
        Transaction tx = null;
        if (null == node || StringUtils.isBlank(node.getNodeType()) || StringUtils.isBlank(node.getIdentifier()))
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(), "Invalid input node");
        else {
            try {
                String date = DateUtils.formatCurrentDate();
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Node neo4jNode = null;
                try {
                    neo4jNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, node.getIdentifier());
                } catch (ResourceNotFoundException e) {
                    neo4jNode = graphDb.createNode(NODE_LABEL);
                    if (StringUtils.isBlank(node.getIdentifier()))
                        node.setIdentifier(graphId + "_" + neo4jNode.getId());
                    neo4jNode.setProperty(SystemProperties.IL_UNIQUE_ID.name(), node.getIdentifier());
                    neo4jNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), node.getNodeType());
                    neo4jNode.setProperty(AuditProperties.createdOn.name(), date);
                    if (StringUtils.isNotBlank(node.getObjectType()))
                        neo4jNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), node.getObjectType());
                }
                setNodeData(graphDb, node, neo4jNode);
                neo4jNode.setProperty(AuditProperties.lastUpdatedOn.name(), date);
                tx.success();
                tx.close();
                OK(GraphDACParams.node_id.name(), node.getIdentifier(), getSender());
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
    public void addNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        com.ilimi.graph.dac.model.Node node = (com.ilimi.graph.dac.model.Node) request.get(GraphDACParams.node.name());
        Transaction tx = null;
        if (null == node || StringUtils.isBlank(node.getNodeType()))
            throw new ClientException(GraphDACErrorCodes.ERR_CREATE_NODE_MISSING_REQ_PARAMS.name(), "Invalid input node");
        else {
            try {
                String date = DateUtils.formatCurrentDate();
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Node neo4jNode = graphDb.createNode(NODE_LABEL);
                if (StringUtils.isBlank(node.getIdentifier()))
                    node.setIdentifier(graphId + "_" + neo4jNode.getId());
                neo4jNode.setProperty(SystemProperties.IL_UNIQUE_ID.name(), node.getIdentifier());
                neo4jNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), node.getNodeType());
                if (StringUtils.isNotBlank(node.getObjectType()))
                    neo4jNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), node.getObjectType());
                setNodeData(graphDb, node, neo4jNode);
                neo4jNode.setProperty(AuditProperties.createdOn.name(), date);
                neo4jNode.setProperty(AuditProperties.lastUpdatedOn.name(), date);
                tx.success();
                tx.close();
                OK(GraphDACParams.node_id.name(), node.getIdentifier(), getSender());
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
    public void updateNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        com.ilimi.graph.dac.model.Node node = (com.ilimi.graph.dac.model.Node) request.get(GraphDACParams.node.name());
        Transaction tx = null;
        if (null == node || StringUtils.isBlank(node.getNodeType()) || StringUtils.isBlank(node.getIdentifier()))
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(), "Invalid input node");
        else {
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Node neo4jNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, node.getIdentifier());
                setNodeData(graphDb, node, neo4jNode);
                neo4jNode.setProperty(AuditProperties.lastUpdatedOn.name(), DateUtils.formatCurrentDate());
                tx.success();
                tx.close();
                OK(GraphDACParams.node_id.name(), node.getIdentifier(), getSender());
            } catch (Exception e) {
                if (null != tx) {
                    tx.failure();
                    tx.close();
                }
                ERROR(e, getSender());
            }
        }
    }

    private void setNodeData(GraphDatabaseService graphDb, com.ilimi.graph.dac.model.Node node, Node neo4jNode) {
        Map<String, Object> metadata = node.getMetadata();
        if (null != metadata && metadata.size() > 0) {
            for (Entry<String, Object> entry : metadata.entrySet()) {
                if (null == entry.getValue()) {
                    neo4jNode.removeProperty(entry.getKey());
                } else {
                    neo4jNode.setProperty(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void importNodes(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        List<com.ilimi.graph.dac.model.Node> nodes = (List<com.ilimi.graph.dac.model.Node>) request
                .get(GraphDACParams.node_list.name());
        Transaction tx = null;
        if (!validateRequired(nodes))
            throw new ClientException(GraphDACErrorCodes.ERR_IMPORT_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        else {
            try {
                String date = DateUtils.formatCurrentDate();
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                for (com.ilimi.graph.dac.model.Node node : nodes) {
                    Node neo4jNode = null;
                    try {
                        neo4jNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, node.getIdentifier());
                    } catch (ResourceNotFoundException e) {
                        neo4jNode = graphDb.createNode(NODE_LABEL);
                        neo4jNode.setProperty(AuditProperties.createdOn.name(), date);
                    }
                    if (StringUtils.isBlank(node.getIdentifier()))
                        node.setIdentifier(graphId + "_" + neo4jNode.getId());
                    neo4jNode.setProperty(SystemProperties.IL_UNIQUE_ID.name(), node.getIdentifier());
                    neo4jNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), node.getNodeType());
                    if (StringUtils.isNotBlank(node.getObjectType()))
                        neo4jNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), node.getObjectType());
                    setNodeData(graphDb, node, neo4jNode);
                    neo4jNode.setProperty(AuditProperties.lastUpdatedOn.name(), date);
                }
                tx.success();
                OK(getSender());
            } catch (Exception e) {
                e.printStackTrace();
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
    public void updatePropertyValue(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        Property property = (Property) request.get(GraphDACParams.metadata.name());
        if (!validateRequired(nodeId, property)) {
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Node node = getNodeByUniqueId(graphDb, nodeId);
                //tx.acquireWriteLock(node);
                if (null == property.getPropertyValue())
                    node.removeProperty(property.getPropertyName());
                else
                    node.setProperty(property.getPropertyName(), property.getPropertyValue());
                node.setProperty(AuditProperties.lastUpdatedOn.name(), DateUtils.formatCurrentDate());
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

    @SuppressWarnings("unchecked")
    @Override
    public void updatePropertyValues(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        Map<String, Object> metadata = (Map<String, Object>) request.get(GraphDACParams.metadata.name());
        if (!validateRequired(nodeId, metadata)) {
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                if (null != metadata && metadata.size() > 0) {
                    Node node = getNodeByUniqueId(graphDb, nodeId);
                    //tx.acquireWriteLock(node);
                    for (Entry<String, Object> entry : metadata.entrySet()) {
                        if (null == entry.getValue())
                            node.removeProperty(entry.getKey());
                        else
                            node.setProperty(entry.getKey(), entry.getValue());
                    }
                    node.setProperty(AuditProperties.lastUpdatedOn.name(), DateUtils.formatCurrentDate());
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
    public void removePropertyValue(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        String key = (String) request.get(GraphDACParams.property_key.name());
        if (!validateRequired(nodeId, key)) {
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Node node = getNodeByUniqueId(graphDb, nodeId);
                //tx.acquireWriteLock(node);
                node.removeProperty(key);
                node.setProperty(AuditProperties.lastUpdatedOn.name(), DateUtils.formatCurrentDate());
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

    @SuppressWarnings("unchecked")
    @Override
    public void removePropertyValues(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        List<String> keys = (List<String>) request.get(GraphDACParams.property_keys.name());
        if (!validateRequired(nodeId, keys)) {
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Node node = getNodeByUniqueId(graphDb, nodeId);
                for (String key : keys) {
                    node.removeProperty(key);
                }
                node.setProperty(AuditProperties.lastUpdatedOn.name(), DateUtils.formatCurrentDate());
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
    public void deleteNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        if (!validateRequired(nodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DELETE_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            Transaction tx = null;
            try {
                GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
                tx = graphDb.beginTx();
                Node node = getNodeByUniqueId(graphDb, nodeId);
                Iterable<Relationship> rels = node.getRelationships();
                if (null != rels) {
                    for (Relationship rel : rels) {
                        rel.delete();
                    }
                }
                node.delete();
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

}
