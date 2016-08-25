package org.ekstep.graph.service.operation;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;
import static com.ilimi.graph.dac.util.Neo4jGraphUtil.getNodeByUniqueId;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.DateUtils;
import com.ilimi.graph.common.Identifier;
import com.ilimi.graph.dac.enums.AuditProperties;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.dac.util.Neo4jGraphUtil;

public class Neo4JEmbeddedNodeOperations {

	private static Logger LOGGER = LogManager.getLogger(Neo4JEmbeddedNodeOperations.class.getName());

	public void upsertNode(String graphId, com.ilimi.graph.dac.model.Node node, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			String date = DateUtils.formatCurrentDate();
			LOGGER.info("Date: " + date);
			Node neo4jNode = null;
			try {
				neo4jNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, node.getIdentifier());
				validateNodeUpdate(neo4jNode, node);
			} catch (ResourceNotFoundException e) {
				neo4jNode = graphDb.createNode(NODE_LABEL);
				if (StringUtils.isBlank(node.getIdentifier()))
					node.setIdentifier(Identifier.getIdentifier(graphId, neo4jNode.getId()));
				neo4jNode.setProperty(SystemProperties.IL_UNIQUE_ID.name(), node.getIdentifier());
				neo4jNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), node.getNodeType());
				neo4jNode.setProperty(AuditProperties.createdOn.name(), date);
				if (StringUtils.isNotBlank(node.getObjectType()))
					neo4jNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), node.getObjectType());
			}
			setNodeData(graphDb, node, neo4jNode);
			neo4jNode.setProperty(AuditProperties.lastUpdatedOn.name(), date);
			tx.success();
		}
	}

	public void addNode(String graphId, com.ilimi.graph.dac.model.Node node, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			String date = DateUtils.formatCurrentDate();
			Node neo4jNode = graphDb.createNode(NODE_LABEL);
			if (StringUtils.isBlank(node.getIdentifier()))
				node.setIdentifier(Identifier.getIdentifier(graphId, neo4jNode.getId()));
			neo4jNode.setProperty(SystemProperties.IL_UNIQUE_ID.name(), node.getIdentifier());
			neo4jNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), node.getNodeType());
			if (StringUtils.isNotBlank(node.getObjectType()))
				neo4jNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), node.getObjectType());
			setNodeData(graphDb, node, neo4jNode);
			neo4jNode.setProperty(AuditProperties.createdOn.name(), date);
			neo4jNode.setProperty(AuditProperties.lastUpdatedOn.name(), date);
			tx.success();
		}
	}

	public void updateNode(String graphId, com.ilimi.graph.dac.model.Node node, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Node neo4jNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, node.getIdentifier());
			validateNodeUpdate(neo4jNode, node);
			setNodeData(graphDb, node, neo4jNode);
			neo4jNode.setProperty(AuditProperties.lastUpdatedOn.name(), DateUtils.formatCurrentDate());
			tx.success();
		}
	}

	public void importNodes(String graphId, List<com.ilimi.graph.dac.model.Node> nodes, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			String date = DateUtils.formatCurrentDate();
			for (com.ilimi.graph.dac.model.Node node : nodes) {
				Node neo4jNode = null;
				try {
					neo4jNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, node.getIdentifier());
				} catch (ResourceNotFoundException e) {
					neo4jNode = graphDb.createNode(NODE_LABEL);
					neo4jNode.setProperty(AuditProperties.createdOn.name(), date);
				}
				if (StringUtils.isBlank(node.getIdentifier()))
					node.setIdentifier(Identifier.getIdentifier(graphId, neo4jNode.getId()));
				neo4jNode.setProperty(SystemProperties.IL_UNIQUE_ID.name(), node.getIdentifier());
				neo4jNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), node.getNodeType());
				if (StringUtils.isNotBlank(node.getObjectType()))
					neo4jNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), node.getObjectType());
				setNodeData(graphDb, node, neo4jNode);
				neo4jNode.setProperty(AuditProperties.lastUpdatedOn.name(), date);
			}
			tx.success();
		}
	}

	public void updatePropertyValue(String graphId, String nodeId, Property property, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Node node = getNodeByUniqueId(graphDb, nodeId);
			if (null == property.getPropertyValue())
				node.removeProperty(property.getPropertyName());
			else
				node.setProperty(property.getPropertyName(), property.getPropertyValue());
			node.setProperty(AuditProperties.lastUpdatedOn.name(), DateUtils.formatCurrentDate());
			tx.success();
		}
	}

	public void updatePropertyValues(String graphId, String nodeId, Map<String, Object> metadata, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			if (null != metadata && metadata.size() > 0) {
				Node node = getNodeByUniqueId(graphDb, nodeId);
				for (Entry<String, Object> entry : metadata.entrySet()) {
					if (null == entry.getValue())
						node.removeProperty(entry.getKey());
					else
						node.setProperty(entry.getKey(), entry.getValue());
				}
				node.setProperty(AuditProperties.lastUpdatedOn.name(), DateUtils.formatCurrentDate());
			}
			tx.success();
		}
	}

	public void removePropertyValue(String graphId, String nodeId, String key, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Node node = getNodeByUniqueId(graphDb, nodeId);
			node.removeProperty(key);
			node.setProperty(AuditProperties.lastUpdatedOn.name(), DateUtils.formatCurrentDate());
			tx.success();
		}
	}

	public void removePropertyValues(String graphId, String nodeId, List<String> keys, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Node node = getNodeByUniqueId(graphDb, nodeId);
			for (String key : keys) {
				node.removeProperty(key);
			}
			node.setProperty(AuditProperties.lastUpdatedOn.name(), DateUtils.formatCurrentDate());
			tx.success();
		}
	}

	public void deleteNode(String graphId, String nodeId, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Node node = getNodeByUniqueId(graphDb, nodeId);
			Iterable<Relationship> rels = node.getRelationships();
			if (null != rels) {
				for (Relationship rel : rels) {
					rel.delete();
				}
			}
			node.delete();
			tx.success();
		}
	}

	private void validateNodeUpdate(org.neo4j.graphdb.Node neo4jNode, com.ilimi.graph.dac.model.Node node) {
		if (neo4jNode.hasProperty(SystemProperties.IL_SYS_NODE_TYPE.name())) {
			String nodeType = (String) neo4jNode.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name());
			if (!StringUtils.equals(nodeType, node.getNodeType()))
				throw new ServerException(GraphDACErrorCodes.ERR_UPDATE_NODE_INVALID_NODE_TYPE.name(),
						"Cannot update a node of type " + nodeType + " to " + node.getNodeType());
		}
		if (neo4jNode.hasProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name())) {
			String objectType = (String) neo4jNode.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
			if (!StringUtils.equals(objectType, node.getObjectType()))
				throw new ServerException(GraphDACErrorCodes.ERR_UPDATE_NODE_INVALID_NODE_TYPE.name(),
						"Cannot update a node of type " + objectType + " to " + node.getObjectType());
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

}
