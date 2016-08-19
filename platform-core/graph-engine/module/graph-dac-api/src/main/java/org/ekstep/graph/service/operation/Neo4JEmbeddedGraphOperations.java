package org.ekstep.graph.service.operation;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;
import static com.ilimi.graph.dac.util.Neo4jGraphUtil.getNodeByUniqueId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.graph.common.DateUtils;
import com.ilimi.graph.common.Identifier;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.dac.enums.AuditProperties;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.dac.util.Neo4jGraphUtil;
import com.ilimi.graph.dac.util.RelationType;
import com.ilimi.graph.importer.ImportData;

public class Neo4JEmbeddedGraphOperations extends BaseOperations {

	private static Logger LOGGER = LogManager.getLogger(Neo4JEmbeddedGraphOperations.class.getName());

	public void createGraph(String graphId, Request request) {
		LOGGER.info("Creating Graph Id: " + graphId);
		Neo4jGraphFactory.createGraph(graphId);
		Neo4jGraphFactory.getGraphDb(graphId, request);
	}

	public void createGraphUniqueContraint(String graphId, List<String> indexProperties, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Schema schema = graphDb.schema();
			for (String prop : indexProperties) {
				schema.constraintFor(NODE_LABEL).assertPropertyIsUnique(prop).create();
			}
			tx.success();
		}
	}

	public void createIndex(String graphId, List<String> indexProperties, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Schema schema = graphDb.schema();
			for (String prop : indexProperties) {
				schema.indexFor(NODE_LABEL).on(prop).create();
			}
			tx.success();
		}
	}

	public void deleteGraph(String graphId, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			tx.success();
		}
	}

	@SuppressWarnings("unchecked")
	public void createRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			int index = 0;
			Map<String, Object> metadata = (Map<String, Object>) request.get(GraphDACParams.metadata.name());
			RelationType relation = new RelationType(relationType);
			Node startNode = getNodeByUniqueId(graphDb, startNodeId);
			Relationship dbRel = null;
			Iterable<Relationship> relations = startNode.getRelationships(Direction.OUTGOING, relation);
			if (null != relations) {
				for (Relationship rel : relations) {
					Object relEndNodeId = rel.getEndNode().getProperty(SystemProperties.IL_UNIQUE_ID.name());
					String strEndNodeId = (null == relEndNodeId) ? null : relEndNodeId.toString();
					if (StringUtils.equalsIgnoreCase(RelationTypes.SEQUENCE_MEMBERSHIP.relationName(), relationType))
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
			tx.success();
		}
	}

	@SuppressWarnings("unchecked")
	public void updateRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Relationship rel = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType, endNodeId);
			if (null != rel) {
				Map<String, Object> metadata = (Map<String, Object>) request.get(GraphDACParams.metadata.name());
				for (Entry<String, Object> entry : metadata.entrySet()) {
					rel.setProperty(entry.getKey(), entry.getValue());
				}
			}
			tx.success();
		}
	}

	public void deleteRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Relationship rel = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType, endNodeId);
			if (null != rel) {
				rel.delete();
			}
			tx.success();
		}
	}

	public void createIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Node endNode = getNodeByUniqueId(graphDb, endNodeId);
			RelationType relType = new RelationType(relationType);
			for (String startNodeId : startNodeIds) {
				Relationship relation = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType, endNodeId);
				if (null == relation) {
					Node startNode = getNodeByUniqueId(graphDb, startNodeId);
					startNode.createRelationshipTo(endNode, relType);
				}
			}
			tx.success();
		}
	}

	public void createOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Node startNode = getNodeByUniqueId(graphDb, startNodeId);
			RelationType relType = new RelationType(relationType);
			for (String endNodeId : endNodeIds) {
				Relationship relation = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType, endNodeId);
				if (null == relation) {
					Node endNode = getNodeByUniqueId(graphDb, endNodeId);
					startNode.createRelationshipTo(endNode, relType);
				}
			}
			tx.success();
		}
	}

	public void deleteIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			for (String startNodeId : startNodeIds) {
				Relationship relation = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType, endNodeId);
				if (null != relation) {
					relation.delete();
				}
			}
			tx.success();
		}
	}

	public void deleteOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			for (String endNodeId : endNodeIds) {
				Relationship relation = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType, endNodeId);
				if (null != relation) {
					relation.delete();
				}
			}
			tx.success();
		}
	}

	public void removeRelationMetadataByKey(String graphId, String startNodeId, String endNodeId, String relationType,
			String key, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Relationship rel = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType, endNodeId);
			if (null != rel && rel.hasProperty(key))
				rel.removeProperty(key);
			tx.success();
		}
	}

	public void createCollection(String graphId, String collectionId, com.ilimi.graph.dac.model.Node collection,
			String relationType, List<String> members, String indexProperty, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			String date = DateUtils.formatCurrentDate();
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
						startNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), collection.getObjectType());
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
		}
	}

	public void deleteCollection(String graphId, String collectionId, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
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
		}
	}

	public void importGraph(String graphId, String taskId, ImportData input, Map<String, List<String>> messages,
			Request request) throws Exception {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Map<String, Node> existingNodes = new HashMap<String, Node>();
			Map<String, Map<String, List<Relationship>>> existingRelations = new HashMap<String, Map<String, List<Relationship>>>();
			List<com.ilimi.graph.dac.model.Node> importedNodes = new ArrayList<com.ilimi.graph.dac.model.Node>(
					input.getDataNodes());
			int nodesCount = createNodes(request, graphDb, existingNodes, existingRelations, importedNodes);
			int relationsCount = createRelations(request, graphDb, existingRelations, existingNodes, importedNodes,
					messages);
			upsertRootNode(graphDb, graphId, existingNodes, nodesCount, relationsCount);
			tx.success();
			if (taskId != null) {
				updateTaskStatus(graphDb, taskId, "Completed");
			}
		}
	}

	private void updateTaskStatus(GraphDatabaseService graphDb, String taskId, String string) throws Exception {
		Node taskNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, taskId);
		Transaction tx = null;
		try {
			tx = graphDb.beginTx();
			taskNode.setProperty(GraphEngineParams.status.name(), GraphEngineParams.Completed.name());
			tx.success();
		} catch (Exception e) {
			if (null != tx) {
				tx.failure();
				tx.close();
			}
			throw new Exception(e);
		} finally {
			if (null != tx) {
				tx.close();
			}
		}
	}

	private Map<String, Map<String, List<Relationship>>> getExistingRelations(Iterable<Relationship> dbRelations,
			Map<String, Map<String, List<Relationship>>> existingRelations) {
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
			Map<String, Map<String, List<Relationship>>> existingRelations,
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
					neo4jNode = graphDb.findNode(NODE_LABEL, SystemProperties.IL_UNIQUE_ID.name(),
							node.getIdentifier());
					if (null == neo4jNode) {
						neo4jNode = graphDb.createNode(NODE_LABEL);
						neo4jNode.setProperty(AuditProperties.createdOn.name(), date);
						nodesCount++;
					}
					existingNodes.put(node.getIdentifier(), neo4jNode);
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
				getExistingRelations(neo4jNode.getRelationships(), existingRelations);
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
									otherNode = graphDb.findNode(NODE_LABEL, SystemProperties.IL_UNIQUE_ID.name(),
											endNodeId);
									if (null == otherNode) {
										List<String> rowMsgs = messages.get(uniqueId);
										if (rowMsgs == null) {
											rowMsgs = new ArrayList<String>();
											messages.put(uniqueId, rowMsgs);
										}
										rowMsgs.add("Node with id: " + endNodeId + " not found to create relation:"
												+ relType);
									} else {
										existingNodes.put(endNodeId, otherNode);
										RelationType relation = new RelationType(relType);
										Relationship neo4jRel = neo4jNode.createRelationshipTo(otherNode, relation);
										setRelationMetadata(neo4jRel, relMap.get(endNodeId));
										relationsCount++;
									}
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
									existingNodes, messages, graphDb);
						}
					}
				} else {
					for (String relType : nodeRelMap.keySet()) {
						relationsCount += createNewRelations(neo4jNode, nodeRelMap, relType, nodeRelation, uniqueId,
								existingNodes, messages, graphDb);
					}
				}
			}
		}
		return relationsCount;
	}

	private int createNewRelations(Node neo4jNode, Map<String, List<String>> nodeRelMap, String relType,
			Map<String, Map<String, Relation>> nodeRelation, String uniqueId, Map<String, Node> existingNodes,
			Map<String, List<String>> messages, GraphDatabaseService graphDb) {
		int relationsCount = 0;
		List<String> relEndNodeIds = nodeRelMap.get(relType);
		Map<String, Relation> relMap = nodeRelation.get(relType);
		for (String endNodeId : relEndNodeIds) {
			Node otherNode = existingNodes.get(endNodeId);
			if (null == otherNode) {
				otherNode = graphDb.findNode(NODE_LABEL, SystemProperties.IL_UNIQUE_ID.name(), endNodeId);
				if (null == otherNode) {
					List<String> rowMsgs = messages.get(uniqueId);
					if (rowMsgs == null) {
						rowMsgs = new ArrayList<String>();
						messages.put(uniqueId, rowMsgs);
					}
					rowMsgs.add("Node with id: " + endNodeId + " not found to create relation:" + relType);
				} else {
					existingNodes.put(endNodeId, otherNode);
					RelationType relation = new RelationType(relType);
					Relationship neo4jRel = neo4jNode.createRelationshipTo(otherNode, relation);
					setRelationMetadata(neo4jRel, relMap.get(endNodeId));
					relationsCount++;
				}
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
		String rootNodeUniqueId = Identifier.getIdentifier(graphId, SystemNodeTypes.ROOT_NODE.name());
		Node rootNode = null;
		if (existingNodes.get(rootNodeUniqueId) == null) {
			rootNode = graphDb.findNode(NODE_LABEL, SystemProperties.IL_UNIQUE_ID.name(), rootNodeUniqueId);
			if (null == rootNode) {
				rootNode = graphDb.createNode(NODE_LABEL);
				rootNode.setProperty(SystemProperties.IL_UNIQUE_ID.name(), rootNodeUniqueId);
				rootNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.ROOT_NODE.name());
				rootNode.setProperty("nodesCount", 0);
				rootNode.setProperty("relationsCount", 0);
			}
		} else {
			rootNode = existingNodes.get(rootNodeUniqueId);
		}
		int totalNodes = (Integer) rootNode.getProperty("nodesCount") + nodesCount;
		rootNode.setProperty("nodesCount", totalNodes);
		int totalRelations = (Integer) rootNode.getProperty("relationsCount") + relationsCount;
		rootNode.setProperty("relationsCount", totalRelations);
	}

}
